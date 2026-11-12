package rob.web;

import java.util.ArrayDeque;

import org.teavm.jso.browser.Location;
import org.teavm.jso.browser.Window;
import org.teavm.jso.websocket.WebSocket;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;

import rob.Link;
import rob.Main;

/** Browser entry point. "Play online" talks to /ws on whatever server the page came from. ?name=Jack fills in your name, ?auto watches bots play. */
public class WebLauncher {
    public static void main(String[] args) {
        WebApplicationConfiguration c = new WebApplicationConfiguration();
        c.width = 0;        // fill the window
        c.height = 0;
        c.antialiasing = true;
        Location at = Window.current().getLocation();
        String url = (at.getProtocol().equals("https:") ? "wss://" : "ws://") + at.getHost() + "/ws";
        String q = at.getSearch(), name = null, mode = q.contains("auto-online") ? "auto-online" : q.contains("auto") ? "auto" : "";
        for (String kv : q.replace("?", "").split("&")) if (kv.startsWith("name=")) name = kv.substring(5).replace("%20", " ").replace("+", " ");
        new WebApplication(new Main(() -> new WsLink(url), name, mode), c);
    }

    static class WsLink implements Link {
        final ArrayDeque<String> q = new ArrayDeque<>();
        final WebSocket ws;
        boolean open;
        String error;

        WsLink(String url) {
            ws = new WebSocket(url);
            ws.onOpen(e -> open = true);
            ws.onMessage(e -> q.add(e.getDataAsString()));
            ws.onClose(e -> { open = false; error = "Lost the server. Is it running?"; });
            ws.onError(e -> { open = false; error = "Can't reach the server. Play vs bots works without one."; });
        }
        public void send(String msg) { if (open) ws.send(msg); }
        public String poll() { return q.poll(); }
        public boolean open() { return open; }
        public String error() { return error; }
        public void close() { open = false; ws.close(); }
    }
}
