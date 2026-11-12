package rob.rules;

import java.util.ArrayList;
import java.util.List;

/** One table: lobby, seats, lobby leader, bot pacing, turn clock. Runs on the server, and inside the client for offline games. */
public class Session {
    public interface Out { void send(int conn, String json); }

    public static final int MAX_SEATS = 6, TURN_SECONDS = 45, TRADE_SECONDS = 20;
    static final int[] ROUNDS = {12, 16, 8}, CASH = {700, 1000, 500}, CLOCK = {45, 90, 0};
    public static final float[] SPEED = {1f, 1.7f, 3f};
    public static final String[] SPEED_NAME = {"NORMAL", "FAST", "TURBO"};
    static final String[] BOT_NAMES = {"Bot Gene", "Bot Darlene", "Bot Skip", "Bot Tammy", "Bot Earl"};

    static class Seat { String name; int ch; boolean bot, away; int conn = -1; }

    final Out out;
    final boolean timers;
    final List<Seat> seats = new ArrayList<>();
    final List<Integer> conns = new ArrayList<>();     // everyone connected, seated or watching
    final java.util.Map<Integer, String> connName = new java.util.HashMap<>();
    String phase = "lobby";
    // table rules the lobby leader can change
    int rounds = 12, speed = 0, cashIdx = 0, clockIdx = 0;
    boolean fastBots = true, trading = true, bets = true;
    Game g;
    float wait, think, clock;
    int seenRolls = -1;

    public Session(Out out, boolean timers) { this.out = out; this.timers = timers; }

    public synchronized void cmd(int conn, String line) {
        String[] t = line.trim().split(" ");
        Seat me = seatOf(conn);
        boolean lead = me != null && me == leader();
        switch (t[0]) {
            case "join": join(conn, line.length() > 5 ? line.substring(5).trim() : "Player"); break;
            case "char": if (phase.equals("lobby") && me != null) { me.ch = freeChar(me.ch + 1); send(); } break;
            case "bot+":
                if (phase.equals("lobby") && lead && seats.size() < MAX_SEATS) {
                    Seat s = new Seat(); s.bot = true; s.ch = freeChar(0);
                    for (String nm : BOT_NAMES) if (!taken(nm)) { s.name = nm; break; }
                    seats.add(s); send();
                }
                break;
            case "bot-":
                if (phase.equals("lobby") && lead) for (int i = seats.size() - 1; i >= 0; i--) if (seats.get(i).bot) { seats.remove(i); send(); break; }
                break;
            case "opt":
                if (phase.equals("lobby") && lead && t.length > 1) {
                    switch (t[1]) {
                        case "months": for (int i = 0; i < 3; i++) if (ROUNDS[i] == rounds) { rounds = ROUNDS[(i + 1) % 3]; break; } break;
                        case "speed": speed = (speed + 1) % SPEED.length; break;
                        case "fast": fastBots = !fastBots; break;
                        case "cash": cashIdx = (cashIdx + 1) % CASH.length; break;
                        case "trading": trading = !trading; break;
                        case "bets": bets = !bets; break;
                        case "clock": clockIdx = (clockIdx + 1) % CLOCK.length; break;
                        default: break;
                    }
                    send();
                }
                break;
            case "side":
                if (phase.equals("game") && me != null && t.length > 2) {
                    g.side(seats.indexOf(me), new String[]{t[1], t[2]});
                    extra();
                }
                break;
            case "start": if (phase.equals("lobby") && lead) start(); break;
            case "again":
                if (phase.equals("over") && lead) {
                    phase = "lobby"; g = null;
                    for (int i = seats.size() - 1; i >= 0; i--) { Seat s = seats.get(i); if (s.away) seats.remove(i); }
                    for (int c : conns) if (seatOf(c) == null && seats.size() < MAX_SEATS) seat(c, connName.get(c));   // people who were watching
                    send();
                }
                break;
            case "act":
                if (phase.equals("game") && me != null && seats.indexOf(me) == g.who() && t.length > 1) {
                    String[] a = new String[t.length - 1];
                    System.arraycopy(t, 1, a, 0, a.length);
                    step(a);
                }
                break;
            case "taunt":
                if (phase.equals("game") && me != null && t.length > 1) {
                    int k = Game.num(t[1]);
                    if (k >= 0 && k < Game.TAUNTS.length) { List<String> b = new ArrayList<>(); b.add("say " + seats.indexOf(me) + " " + Game.TAUNTS[k]); send(b); }
                }
                break;
            default: break;
        }
    }

    void seat(int conn, String name) {
        String base = name; int k = 2;
        while (taken(name)) name = base + " " + k++;
        int want = 0;       // Sam gets Sam, Jack gets Jack, and so on, if nobody took it
        String low = name.toLowerCase();
        for (int i = 0; i < 4; i++) if (low.contains(new String[]{"rob", "jules", "sam", "jack"}[i])) want = i;
        Seat s = new Seat(); s.name = name; s.conn = conn; s.ch = freeChar(want);
        seats.add(s);
    }

    void join(int conn, String name) {
        if (conns.contains(conn)) return;
        conns.add(conn);
        name = name.replace('"', ' ').replace('\\', ' ').trim();
        if (name.isEmpty()) name = "Player";
        if (name.length() > 14) name = name.substring(0, 14);
        connName.put(conn, name);
        if (!phase.equals("lobby")) {       // mid-game: take your old seat back if you had one, otherwise watch
            for (Seat s : seats) if (s.away && s.name.equalsIgnoreCase(name)) { s.away = false; s.bot = false; s.conn = conn; break; }
            send();
            return;
        }
        if (seats.size() < MAX_SEATS) seat(conn, name);
        send();
    }

    public synchronized void leave(int conn) {
        conns.remove((Integer) conn); connName.remove(conn);
        Seat s = seatOf(conn);
        if (s != null) {
            if (phase.equals("lobby")) seats.remove(s);
            else { s.conn = -1; s.bot = true; s.away = true; }     // a bot covers the seat until they come back
        }
        if (conns.isEmpty()) { seats.clear(); phase = "lobby"; g = null; rounds = 12; speed = cashIdx = clockIdx = 0; fastBots = trading = bets = true; }
        else send();
    }

    void start() {
        if (seats.size() < 2) cmd(leader().conn, "bot+");
        int[] ch = new int[seats.size()]; String[] names = new String[seats.size()];
        for (int i = 0; i < ch.length; i++) { ch[i] = seats.get(i).ch; names[i] = seats.get(i).name; }
        g = new Game(ch, names, rounds, System.nanoTime(), CASH[cashIdx], trading, bets);
        phase = "game"; seenRolls = -1;
        flush(false);
    }

    void step(String[] a) {
        boolean bot = seats.get(g.who()).bot;
        g.act(g.who(), a);
        if (g.kind == Game.OVER) phase = "over";
        flush(bot);
    }

    float rate = 1;
    /** Sends what just happened. rate is how fast screens should play it: the table speed, and faster again for a bot's move. */
    void flush(boolean bot) {
        List<String> beats = g.takeBeats();
        rate = SPEED[speed] * (bot && fastBots ? 1.8f : 1);
        float d = 0.25f;
        for (String b : beats) d += Game.dur(b);
        wait = d / rate; clock = 0; think = 0.6f / rate;
        send(beats);
        if (phase.equals("game") && g.rolls != seenRolls && (g.kind == Game.ROLL || g.kind == Game.JAIL)) {   // a new roll is coming: bots may bet on it
            seenRolls = g.rolls;
            for (int i = 0; i < seats.size(); i++) { String s = seats.get(i).bot ? Bot.side(g, i) : null; if (s != null) g.side(i, s.split(" ")); }
            extra();
        }
    }
    /** Sends the result of something done off-turn, without restarting anybody's clock. */
    void extra() {
        List<String> beats = g.takeBeats();
        for (String b : beats) wait += Game.dur(b) / rate;
        send(beats);
    }

    /** Call a few times a second. Runs the bots and the turn clock. */
    public synchronized void tick(float dt) {
        if (!phase.equals("game")) return;
        if (wait > 0) { wait -= dt; return; }      // screens are still animating the last move
        Seat s = seats.get(g.who());
        if (s.bot) { think -= dt; if (think <= 0) step(Bot.choose(g).split(" ")); }
        else if (limit() > 0 && (clock += dt) > limit()) step((g.kind == Game.TRADE ? "no" : Bot.choose(g)).split(" "));
    }

    public synchronized String status() {
        StringBuilder b = new StringBuilder("Rent's Due server is up. ");
        b.append(phase.equals("lobby") ? "In the lobby: " : "Game on: ");
        for (Seat s : seats) b.append(s.name).append(s.bot ? " (bot)" : "").append(", ");
        return seats.isEmpty() ? b.append("nobody.").toString() : b.substring(0, b.length() - 2) + ".";
    }

    /** Seconds a person gets for the decision on the table. 0 offline. */
    int limit() { return !timers || g == null || CLOCK[clockIdx] == 0 ? 0 : g.kind == Game.TRADE ? TRADE_SECONDS : CLOCK[clockIdx]; }

    Seat seatOf(int conn) { for (Seat s : seats) if (s.conn == conn) return s; return null; }
    Seat leader() { for (Seat s : seats) if (s.conn >= 0) return s; return null; }
    boolean taken(String name) { for (Seat s : seats) if (s.name.equalsIgnoreCase(name)) return true; return false; }
    int freeChar(int from) {
        int nc = Game.CH_NAME.length;
        for (int k = 0; k < nc; k++) {
            int c = (from + k) % nc; boolean used = false;
            for (Seat s : seats) if (s.ch == c) used = true;
            if (!used) return c;
        }
        return from % nc;
    }

    void send() { send(new ArrayList<>()); }
    void send(List<String> beats) {
        StringBuilder b = new StringBuilder();
        b.append(",\"phase\":").append(Game.q(phase)).append(",\"rounds\":").append(rounds).append(",\"limit\":").append(limit()).append(",\"rate\":").append(rate)
                .append(",\"opts\":{\"speed\":").append(speed).append(",\"fast\":").append(fastBots).append(",\"cash\":").append(CASH[cashIdx])
                .append(",\"trading\":").append(trading).append(",\"bets\":").append(bets).append(",\"clock\":").append(CLOCK[clockIdx]).append(",\"online\":").append(timers)
                .append("},\"seats\":[");
        for (int i = 0; i < seats.size(); i++) {
            Seat s = seats.get(i);
            if (i > 0) b.append(',');
            b.append("{\"name\":").append(Game.q(s.name)).append(",\"ch\":").append(s.ch).append(",\"bot\":").append(s.bot).append(",\"away\":").append(s.away).append('}');
        }
        b.append("],\"beats\":[");
        for (int i = 0; i < beats.size(); i++) { if (i > 0) b.append(','); b.append(Game.q(beats.get(i))); }
        b.append(']');
        if (g != null) b.append(",\"g\":").append(g.toJson());
        b.append('}');
        Seat lead = leader();
        for (int c : new ArrayList<>(conns)) {
            Seat s = seatOf(c);
            out.send(c, "{\"you\":" + seats.indexOf(s) + ",\"lead\":" + (s != null && s == lead) + b);
        }
    }
}
