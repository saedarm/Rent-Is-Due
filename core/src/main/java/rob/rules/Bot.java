package rob.rules;

/** Computer players, and the autopilot for people who run out the clock. */
public class Bot {
    /** The move for whoever has to answer the prompt, as the words a player would send. */
    public static String choose(Game g) {
        int p = g.who();
        switch (g.kind) {
            case Game.TRADE: return wants(g) ? "yes" : "no";
            case Game.DRAW: return "draw";
            case Game.CARD: return "ok";
            case Game.CHOICE: return g.r.nextInt(100) < (g.ckey.equals("rent") ? (g.cash[p] < g.cArg * 2 ? 70 : 25) : 40) ? "b" : "a";
            case Game.AUCTION: {
                int price = g.aHigh < 0 ? g.aBid : g.aBid + g.aStep, have = 0;
                for (int i = 0; i < Game.N; i++) if (Game.BOARD[i].group == Game.BOARD[g.aSq].group && g.owner[i] == p) have++;
                double top = Game.BOARD[g.aSq].price * (0.85 + 0.25 * have);      // pays more to build a neighbourhood
                return price <= top && g.cash[p] - price >= 80 ? "bid" : "fold";
            }
            case Game.JAIL: return g.cash[p] >= 300 ? "tab" : "eat";
            case Game.ROLL: {
                for (int i = 0; i < g.hand.get(p).size(); i++) {
                    int id = g.hand.get(p).get(i);
                    if (!g.usable(p, id)) continue;
                    boolean pricey = Game.ROBC[id].key.equals("sheriff") && g.cash[p] < g.value(g.cheapest(g.leader(p))) + 150;
                    if (!pricey) return "play " + i;
                }
                String offer = !g.tradingOn || g.traded || g.r.nextInt(3) > 0 ? null : offer(g, p);
                return offer != null ? offer : "roll";
            }
            case Game.BUY: return g.cash[p] - g.pcost >= 60 ? "buy" : "pass";
            case Game.UP: return g.cash[p] - g.pcost >= 150 ? "up" : "skip";
            default: return "";
        }
    }

    /** What a bot does while somebody else is up: now and then, a side bet on their roll. Null for nothing. */
    public static String side(Game g, int p) {
        if (!g.betsOn || p == g.turn || g.bet[p] != 0 || g.cash[p] < 200 || g.r.nextInt(100) >= 30) return null;
        return g.r.nextBoolean() ? "bet hi" : "bet lo";
    }

    /** Two of a neighbourhood and somebody else has the third? Offer cash for it, at a premium. */
    static String offer(Game g, int p) {
        for (int h = 0; h < Game.HOOD.length; h++) {
            int mine = 0, other = -1;
            for (int i = 0; i < Game.N; i++) if (Game.BOARD[i].group == h) { if (g.owner[i] == p) mine++; else other = i; }
            if (mine != 2 || g.owner[other] < 0) continue;
            int cash = Game.r5(g.value(other) * 1.8);
            if (g.cash[p] >= cash + 100) return "trade " + g.owner[other] + " " + cash + " - " + other;
        }
        return null;
    }

    /** Should the player being asked take the trade on the table? */
    static boolean wants(Game g) {
        int me = g.tTo, them = g.turn, in = 0, out = 0;
        for (int sq : g.tGive) in += g.value(sq);
        for (int sq : g.tGet) out += g.value(sq);
        double gain = g.tCash + in - out, need = out * 0.2;
        int[] was = g.owner.clone();
        for (int sq : g.tGive) g.owner[sq] = me;
        for (int sq : g.tGet) g.owner[sq] = them;
        for (int sq : g.tGet) if (g.ownsHood(them, Game.BOARD[sq].group)) need = out * 0.75;      // it finishes their block: make them pay
        for (int sq : g.tGive) if (g.ownsHood(me, Game.BOARD[sq].group)) gain += g.value(sq) * 0.5; // it finishes mine
        System.arraycopy(was, 0, g.owner, 0, was.length);
        return gain >= need;
    }
}
