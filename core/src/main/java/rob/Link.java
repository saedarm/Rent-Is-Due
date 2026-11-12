package rob;

import com.badlogic.gdx.utils.Array;

import rob.rules.Session;

/** The wire to the table. Online it's a WebSocket (see the launchers); offline it's a Session inside the client. */
public interface Link {
    void send(String msg);
    /** Next message from the table, or null. */
    String poll();
    boolean open();
    /** Why it broke, or null if it hasn't. */
    String error();
    void close();
    default void tick(float dt) { }

    /** Offline play: the whole table runs in here. */
    class Local implements Link {
        final Array<String> q = new Array<>();
        final Session s = new Session((conn, json) -> q.add(json), false);
        public void send(String msg) { s.cmd(0, msg); }
        public String poll() { return q.size > 0 ? q.removeIndex(0) : null; }
        public boolean open() { return true; }
        public String error() { return null; }
        public void close() { }
        public void tick(float dt) { s.tick(dt); }
    }
}
