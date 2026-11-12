package rob.desktop;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import rob.Link;
import rob.Main;

/** Desktop window. -Dserver=ws://host/ws picks the server for "Play online", -Dmode=auto watches bots play, -Dname=Jack sets your name. */
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration c = new Lwjgl3ApplicationConfiguration();
        c.setTitle("Rent's Due");
        c.setWindowedMode(1280, 720);
        c.useVsync(true);
        c.setForegroundFPS(60);
        String server = System.getProperty("server", "ws://localhost:8080/ws");
        new Lwjgl3Application(new Main(() -> new WsLink(server), System.getProperty("name"), System.getProperty("mode", "")), c);
    }

    static class WsLink implements Link, WebSocket.Listener {
        final ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();
        final StringBuilder part = new StringBuilder();
        volatile WebSocket ws;
        volatile String error;

        WsLink(String url) {
            HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(url), this)
                    .whenComplete((w, e) -> { if (e != null) error = "Can't reach the server at " + url; else ws = w; });
        }
        @Override public CompletionStage<?> onText(WebSocket w, CharSequence data, boolean last) {
            part.append(data);
            if (last) { q.add(part.toString()); part.setLength(0); }
            w.request(1);
            return null;
        }
        @Override public CompletionStage<?> onClose(WebSocket w, int code, String reason) { error = "The server hung up."; return null; }
        @Override public void onError(WebSocket w, Throwable e) { error = "Lost the server."; }
        public void send(String msg) { if (ws != null) ws.sendText(msg, true); }
        public String poll() { return q.poll(); }
        public boolean open() { return ws != null && error == null; }
        public String error() { return error; }
        public void close() { if (ws != null) ws.abort(); }
    }
}
