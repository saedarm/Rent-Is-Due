package rob.server;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import rob.rules.Session;

/** Serves the browser game and runs one table over a WebSocket at /ws. PORT and WEB_DIR come from the environment. */
public class Server {
    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String web = System.getenv().getOrDefault("WEB_DIR", "web/build/dist/js/webapp");
        Map<Integer, WsContext> conns = new ConcurrentHashMap<>();
        AtomicInteger next = new AtomicInteger();
        Session table = new Session((conn, json) -> {
            WsContext c = conns.get(conn);
            try { if (c != null && c.session.isOpen()) c.send(json); } catch (RuntimeException e) { /* they left; onClose cleans up */ }
        }, true);

        Javalin app = Javalin.create(cfg -> {
            if (new File(web).isDirectory()) cfg.staticFiles.add(web, Location.EXTERNAL);
            else System.out.println("No browser build at " + web + ". Run: gradlew web:gdx_teavm_web_js_build");
            cfg.jetty.modifyWebSocketServletFactory(f -> f.setIdleTimeout(Duration.ofMinutes(2)));
        });
        app.get("/status", ctx -> ctx.result(table.status()));
        app.ws("/ws", ws -> {
            ws.onConnect(c -> { int id = next.incrementAndGet(); c.attribute("id", id); conns.put(id, c); System.out.println("connect " + id); });
            ws.onMessage(c -> { Integer id = c.attribute("id"); if (id != null && c.message().length() < 200) table.cmd(id, c.message()); });
            ws.onClose(c -> { Integer id = c.attribute("id"); if (id != null && conns.remove(id) != null) { table.leave(id); System.out.println("leave " + id); } });
            ws.onError(c -> { });
        });
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try { table.tick(0.1f); } catch (RuntimeException e) { e.printStackTrace(); }
        }, 100, 100, TimeUnit.MILLISECONDS);
        app.start(port);
    }
}
