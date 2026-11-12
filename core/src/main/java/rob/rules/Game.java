package rob.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** All the rules. Draws nothing. Stops with a prompt (kind) whenever it needs a decision; act() answers it. */
public class Game {
    public static final int N = 40, BANK = -1, CAN = -2, MAX_HAND = 3, FRICKERS_SQ = 10, EX_SQ = 35, TAB = 50, BET = 25;
    // square kinds
    public static final int START = 0, PROP = 1, ROBSQ = 2, TENANT = 3, GIG = 4, FRICKERS = 5, COFFEE = 6,
            LOWES = 7, JULES = 8, EX = 9, SQUIRREL = 10, WINGNIGHT = 11;
    // prompt kinds
    public static final int ROLL = 0, BUY = 1, UP = 2, OVER = 3, JAIL = 4, TRADE = 5, BUSY = -1;
    // characters
    public static final int ROB = 0, JULES_CH = 1, SAM = 2, JACK = 3, TODD = 4, BRENDA = 5;

    public static final String[] CH_NAME = {"Uncle Rob", "Jules", "Sam", "Jack", "Hedge Fund Todd", "Flipper Brenda"};
    public static final String[] CH_PERK = {
            "Collects In Person: his envelopes hold 50% more. Nobody stiffs a guy standing on the porch.",
            "Good With Phones: every month, Venmo pulls one month of rent out of her fattest envelope. No driving.",
            "Reconciles The Books: finds +$75 extra every time he passes First of the Month.",
            "Introvert: gone before anyone notices. Pays a third of the rent at other people's rentals.",
            "Cash Offer: rentals cost him 15% less.",
            "Staged It: her rentals count 15% extra in the final tally."};

    /** Neighbourhoods, cheapest first. Own two rentals in one: visitors pay 50% more. Own all three: double, plus the perk. */
    public static final String[] HOOD = {"Huber Heights", "Fairborn", "Vandalia", "Kettering", "Tipp City", "Centerville", "Beavercreek", "Oakwood"};
    public static final String[] HOOD_PERK = {
            "Every Ranch Is The Same Ranch: your envelopes here fill two months at a time.",
            "Base Housing: the Air Force pays on time. +$50 from the bank every month.",
            "Air Show Weekend: visitors here pay triple instead of double.",
            "HOA President: Tenant Trouble cards never cost you money.",
            "Small Town: everybody talks. +$20 whenever somebody else draws a Rob Happens card.",
            "Good Schools: fixing up any of your rentals costs half.",
            "Behind The Mall: +$100 extra every time you pass First of the Month.",
            "Old Money: other players' keeper cards can't touch you."};
    public static final int HUBER = 0, FAIRBORN = 1, VANDALIA = 2, KETTERING = 3, TIPP = 4, CENTERVILLE = 5, BEAVERCREEK = 6, OAKWOOD = 7;

    public static final String[] TAUNTS = {"Rent's due!", "Wings?", "Nice house.", "I know a guy.", "You're killing me."};

    public static final class Sq {
        public final int kind, price, group;
        public final String tag, name;
        Sq(int kind, String tag, String name, int price, int group) {
            this.kind = kind; this.tag = tag; this.name = name; this.price = price; this.group = group;
        }
    }
    static Sq p(String tag, String name, int price, int group) { return new Sq(PROP, tag, name, price, group); }
    static Sq s(int kind, String tag, String name) { return new Sq(kind, tag, name, 0, -1); }

    /** Clockwise from the bottom-left corner; corners are 0, 10, 20, 30. tag = short label on the board, name = the full joke. */
    public static final Sq[] BOARD = {
            s(START, "FIRST OF THE MONTH", "First of the Month. Collect $150 every time you pass."),
            p("Ranch 1", "Brick Ranch #1", 60, 0),
            p("Ranch 2", "Brick Ranch #2 (Identical)", 60, 0),
            s(ROBSQ, "Rob Happens", "Draw a Rob Happens card."),
            p("Ranch 3", "Brick Ranch #3 (Also Identical)", 80, 0),
            s(GIG, "Water Tower Gig", "Side job at the water tower. Roll for pay."),
            p("Smelly Duplex", "The Duplex With The Smell", 100, 1),
            s(TENANT, "Tenant Trouble", "Draw a Tenant Trouble card."),
            p("Garage Apt", "Garage Apartment (No Garage)", 100, 1),
            p("By The Base", "The One By The Base (Jets Included)", 120, 1),
            s(FRICKERS, "FRICKERS", "Just here for takeout: $30 into the Coffee Can, then roll. 5-6 wins the wing contest. Wing Jail is in the back."),
            p("3-Week House", "The House Rob Lived In For 3 Weeks", 140, 2),
            s(LOWES, "LOWE'S", "Went in for one washer. Came out with a pressure washer. $40 into the Coffee Can."),
            p("Vape Shop Apt", "Above The Vape Shop", 140, 2),
            p("Flight Path", "Flight Path Bungalow (Loud)", 160, 2),
            s(JULES, "Call Jules", "Roll a die. 4 or more and she picks up: +$100."),
            p("Green Pool", "The One With The Green Pool", 180, 3),
            s(TENANT, "Tenant Trouble", "Draw a Tenant Trouble card."),
            p("4-Plex", "Four-Plex On Needmore", 180, 3),
            p("HOA Condo", "Condo With The HOA Lady", 200, 3),
            s(COFFEE, "THE COFFEE CAN", "The can of cash in Rob's truck. Land here and it's yours."),
            p("Pond House", "Lake House (It's A Pond)", 220, 4),
            s(ROBSQ, "Rob Happens", "Draw a Rob Happens card."),
            p("Left Half", "Half A Double, Left Half", 220, 4),
            p("Right Half", "Half A Double, Right Half", 240, 4),
            s(SQUIRREL, "SQUIRREL!", "Ooh, what's that? Roll a die. Even: forward that many. Odd: back that many."),
            p("Unfinished Flip", "The Flip Rob Never Finished", 260, 5),
            p("Cul-De-Sac", "Cul-De-Sac Special", 260, 5),
            s(GIG, "Pool Gig", "Pool chlorination job. Roll for pay."),
            p("Split-Level", "The Split-Level (Stairs Everywhere)", 280, 5),
            s(WINGNIGHT, "WING NIGHT", "It got out of hand. Go directly to Wing Jail."),
            p("Mall Condo", "Townhouse Behind The Mall", 300, 6),
            p("Golf Course", "The Nice One By The Golf Course", 300, 6),
            s(TENANT, "Tenant Trouble", "Draw a Tenant Trouble card."),
            p("Bonus Room", "The One With The Bonus Room", 320, 6),
            s(EX, "The Ex's House", "You were 'in the neighborhood.' Fix her garbage disposal. $30 in parts."),
            s(ROBSQ, "Rob Happens", "Draw a Rob Happens card."),
            p("Colonial", "The Oakwood Colonial", 350, 7),
            p("Carriage House", "Carriage House (No Carriage)", 350, 7),
            p("Good House", "The Good House (Don't Touch Anything)", 400, 7)};

    public static final class Card {
        public final String key, title, text;
        public final boolean keep;
        Card(String key, String title, String text, boolean keep) { this.key = key; this.title = title; this.text = text; this.keep = keep; }
    }
    static Card c(String k, String t, String x) { return new Card(k, t, x, false); }
    static Card k(String k, String t, String x) { return new Card(k, t, x, true); }

    public static final Card[] ROBC = {
            c("wings", "WING NIGHT", "It's Tuesday. Or it isn't. Go directly to Wing Jail."),
            c("truck", "WHERE'D I PARK THE TRUCK", "You walk the lot for an hour. Skip your next turn."),
            c("tower", "WATER TOWER CALL", "Emergency chlorination job. You smell like a pool for a week. +$120."),
            c("fourth", "BOUGHT ANOTHER ONE", "You did not need another rental. You buy the cheapest one left, at half price, without looking at it."),
            c("moves", "ROB MOVES AGAIN", "New house, who dis. Swap places with a random player."),
            c("round", "BOUGHT A ROUND", "'Put it on mine!' Pay every other player $20."),
            c("glovebox", "GLOVEBOX CHECK", "Found a rent check from March under the Frickers napkins. +$150."),
            c("venmo", "TRIED THE VENMO", "Sent $80 to the wrong person. A random player says thanks."),
            c("projects", "FIVE PROJECTS, ZERO FINISHED", "Lowe's run. And another. And another. $60 into the Coffee Can."),
            c("reds", "REDS LOST AGAIN", "You stayed for all nine innings. $30 of ballpark beer goes into the Coffee Can."),
            c("k", "JULES TEXTED BACK", "It said 'k'. Best day of the month. +$60."),
            c("gutters", "THE EX NEEDS HER GUTTERS DONE", "You were already in the truck. Go to The Ex's House."),
            c("spill", "CHLORINE SPILL", "In the truck bed. Truck's never been cleaner. -$70."),
            c("energy", "GAS STATION ENERGY DRINK, 9 PM", "Can't sit still. Take another turn right now."),
            c("squirrel", "OOH, WHAT'S THAT", "Forgot what you were doing. Roll a die and move back that many."),
            c("garage", "GARAGE SALE", "Sold three of your seven ladders. +$70."),
            c("cooler", "THE GOOD COOLER", "The ex still has it. You let her keep it. Again. -$40."),
            c("relax", "TRIED TO RELAX", "Sat down for eleven seconds. Got up. Mowed a tenant's lawn. +$40."),
            k("rentsdue", "RENT'S DUE!", "Knock on every door. Every other player pays you $40."),
            k("tipoff", "ANONYMOUS TIP", "Code enforcement visits whoever's winning. They pay $100 into the Coffee Can."),
            k("poach", "POACH THE RENT", "Get there first. Grab the fattest uncollected envelope on somebody else's rental."),
            k("sheriff", "SHERIFF'S SALE", "Buy the leader's cheapest rental off them for what it's worth. They don't get a say."),
            k("story", "LONG STORY", "It has four tangents and no ending. The leader skips their next turn."),
            k("guy", "I KNOW A GUY", "Free upgrade on your best rental. Do not ask about permits.")};

    public static final Card[] TENC = {
            c("heater", "WATER HEATER BLEW", "You fix it yourself. Twice. -$60."),
            c("quarters", "PAID IN QUARTERS", "All of it. Collect every envelope you've got out there, right now."),
            c("paidwings", "PAID IN WINGS", "Tenant paid rent in Frickers wings. You accepted. Your fattest envelope is gone."),
            c("raccoons", "RACCOONS IN THE ATTIC", "A family of five. Not on the lease. -$50."),
            c("improved", "TENANT 'IMPROVED' THE PLACE", "Somehow it's actually nicer. Free upgrade on one of your rentals."),
            c("skipped", "SKIPPED TOWN", "Left a couch and a ferret. One envelope is empty and the couch costs $30 to haul."),
            c("inspect", "SURPRISE INSPECTION", "Pay $20 per rental into the Coffee Can."),
            c("cookies", "GOOD TENANT", "Pays early. Bakes cookies. Every rental you own gets an extra month in the envelope."),
            c("cousin", "COUSIN 'JUST VISITING'", "Month six. He's fixing a boat in the driveway. -$40."),
            c("tax", "PROPERTY TAX BILL", "Pay 5% of what your rentals are worth into the Coffee Can."),
            c("nobody", "NOBODY'S HOME", "Knocked 14 times. Their truck is right there. Skip your next turn waiting."),
            c("furnace", "TENANT FIXED THE FURNACE", "Himself. With a YouTube video. +$80."),
            c("pool", "GREEN POOL", "Finally, a problem you're licensed for. Chlorinate it. +$100."),
            c("latefees", "LATE FEES", "You charge them. They actually pay them. +$15 per rental."),
            c("handshake", "HANDSHAKE LEASE", "No paperwork. Roll a die: 1-3 it goes badly (-$50), 4-6 it's fine (+$90).")};

    public static final Card[] TOWNC = {
            c("vortex", "POLAR VORTEX", "Pipes burst all over town. Everyone pays $15 per rental."),
            c("allyoucan", "ALL-YOU-CAN-EAT WINGS", "Frickers special. Everyone drops $25 into the Coffee Can."),
            c("boom", "HOUSING BOOM", "This month, rent is up 50% when somebody lands on your place."),
            c("boil", "BOIL ADVISORY", "Rob's on the news next to a water tower. Everyone gets $40, and gigs pay double this month."),
            c("taxes", "TAX SEASON", "Everyone pays 10% of their cash into the Coffee Can. Rob files an extension."),
            c("redswin", "REDS WIN!", "Nobody saw that coming. Everyone gets $50."),
            c("strike", "RENT STRIKE", "No new rent goes into the envelopes this month."),
            c("fair", "COUNTY FAIR", "Somebody passes the Coffee Can around. $100 goes in."),
            c("blackfriday", "BLACK FRIDAY AT LOWE'S", "Upgrades are half price this month."),
            c("outage", "POWER OUTAGE", "Everyone rolls one die this month."),
            c("dip", "MARKET DIP", "Rentals nobody owns are 25% off this month."),
            c("leaves", "LEAF PICKUP WEEK", "Everyone's stuck behind the truck. Everybody moves back 2 spaces.")};

    // ---- state ----
    public final int n;
    public final int[] ch, cash, pos, skip, iou, jail;
    public final String[] names;
    public final List<List<Integer>> hand = new ArrayList<>();
    public final int[] owner = new int[N], level = new int[N], env = new int[N];
    public int pot = 100, round = 1, maxRounds, turn = 0, kind = ROLL, town = -1, psq = -1, pcost = 0;
    // a trade on the table: turn player offers tTo; tCash > 0 means the proposer adds cash, < 0 means they ask for it
    public int tTo = -1, tCash;
    public List<Integer> tGive = new ArrayList<>(), tGet = new ArrayList<>();
    public boolean traded, tradingOn = true, betsOn = true;
    /** Side bets on the next roll: +1 high, -1 low, 0 none. */
    public final int[] bet;
    public int rolls;
    boolean extraUsed, again;
    // stats for the end-of-game awards
    final int[] wings, envGot, rentPaid, skipped, deals;
    final List<String> log = new ArrayList<>();
    List<String> beats = new ArrayList<>();
    final Random r;
    final List<Integer> robDeck = new ArrayList<>(), tenDeck = new ArrayList<>(), townDeck = new ArrayList<>();

    public Game(int[] chars, String[] names, int rounds, long seed) { this(chars, names, rounds, seed, 700, true, true); }
    public Game(int[] chars, String[] names, int rounds, long seed, int startCash, boolean trading, boolean bets) {
        tradingOn = trading; betsOn = bets; bet = new int[chars.length];
        n = chars.length; ch = chars; this.names = names; maxRounds = rounds; r = new Random(seed);
        cash = new int[n]; pos = new int[n]; skip = new int[n]; iou = new int[n]; jail = new int[n];
        wings = new int[n]; envGot = new int[n]; rentPaid = new int[n]; skipped = new int[n]; deals = new int[n];
        for (int i = 0; i < n; i++) { cash[i] = startCash + 25 * i; hand.add(new ArrayList<>()); }   // later seats get a little extra
        for (int i = 0; i < N; i++) owner[i] = -1;
        log("Month 1. Go collect some rent.");
        beats.add("banner GO COLLECT SOME RENT");
        beats.add("turn 0");
    }

    // ---- numbers ----
    static int r5(double v) { return (int) Math.round(v / 5.0) * 5; }
    boolean townIs(String key) { return town >= 0 && TOWNC[town].key.equals(key); }
    int d6() { return 1 + r.nextInt(6); }
    /** Who has to answer the prompt. */
    public int who() { return kind == TRADE ? tTo : turn; }

    public boolean ownsHood(int p, int g) {
        if (p < 0) return false;
        for (int i = 0; i < N; i++) if (BOARD[i].group == g && owner[i] != p) return false;
        return true;
    }
    public boolean fullSet(int sq) { return ownsHood(owner[sq], BOARD[sq].group); }

    public int price(int p, int sq) {
        double c = BOARD[sq].price;
        if (townIs("dip")) c *= 0.75;
        if (ch[p] == TODD) c *= 0.85;
        return r5(c);
    }
    public int upCost(int p, int sq) { return upCost(sq, ownsHood(p, CENTERVILLE), townIs("blackfriday")); }
    public static int upCost(int sq, boolean goodSchools, boolean blackFriday) {
        double c = BOARD[sq].price / 2.0;
        if (goodSchools) c /= 2;
        if (blackFriday) c /= 2;
        return Math.max(5, r5(c));
    }
    /** Fixing a place up from anywhere, without landing on it: the contractor charges 50% more. */
    public static int contractor(int sq, boolean goodSchools, boolean blackFriday) { return r5(upCost(sq, goodSchools, blackFriday) * 1.5); }
    /** What a visitor pays for landing here. */
    public int rent(int sq) { return rent(sq, level[sq], have(sq), townIs("boom")); }
    /** How many rentals in this one's neighbourhood its owner holds. */
    public int have(int sq) {
        int c = 0;
        for (int i = 0; i < N; i++) if (owner[sq] >= 0 && BOARD[i].group == BOARD[sq].group && owner[i] == owner[sq]) c++;
        return c;
    }
    /** have = how many of the neighbourhood the owner holds: two is +50%, all three is double. */
    public static int rent(int sq, int level, int have, boolean boom) {
        double v = BOARD[sq].price * 0.25 * (1 + level);
        if (have >= 3) v *= BOARD[sq].group == VANDALIA ? 3 : 2; else if (have == 2) v *= 1.5;
        if (boom) v *= 1.5;
        return Math.max(5, r5(v));
    }
    /** One month of tenant rent in the envelope. */
    public int envUnit(int sq) { return envUnit(sq, level[sq], owner[sq] >= 0 && ch[owner[sq]] == ROB); }
    public static int envUnit(int sq, int level, boolean rob) {
        int u = Math.max(10, r5(BOARD[sq].price * 0.1)) * (2 + level) / 2;
        return rob ? r5(u * 1.5) : u;
    }
    public int value(int sq) { return BOARD[sq].price + level[sq] * BOARD[sq].price / 2; }
    public int count(int p) { int c = 0; for (int i = 0; i < N; i++) if (owner[i] == p) c++; return c; }
    public int holdings(int p) { int v = 0; for (int i = 0; i < N; i++) if (owner[i] == p) v += value(i); return v; }
    public int worth(int p) {
        int h = holdings(p);
        if (ch[p] == BRENDA) h = h * 115 / 100;
        return cash[p] + h - 100 * iou[p];
    }
    /** Whoever is worth the most, not counting p or anyone with Old Money. -1 if there's nobody. */
    public int leader(int p) {
        int best = -1;
        for (int i = 0; i < n; i++) if (i != p && !ownsHood(i, OAKWOOD) && (best < 0 || worth(i) > worth(best))) best = i;
        return best;
    }
    int cheapest(int p) {
        int best = -1;
        for (int i = 0; i < N; i++) if (owner[i] == p && (best < 0 || value(i) < value(best))) best = i;
        return best;
    }
    /** Fattest envelope: on p's own rentals, or (others) on anyone else's who isn't protected. */
    int fattest(int p, boolean others) {
        int best = -1;
        for (int i = 0; i < N; i++) {
            boolean mine = owner[i] == p;
            if (owner[i] < 0 || env[i] == 0 || (others == mine) || (others && ownsHood(owner[i], OAKWOOD))) continue;
            if (best < 0 || env[i] * envUnit(i) > env[best] * envUnit(best)) best = i;
        }
        return best;
    }
    int upgradable(int p, boolean best) {
        int pick = -1; List<Integer> all = new ArrayList<>();
        for (int i = 0; i < N; i++) if (owner[i] == p && level[i] < 2) { all.add(i); if (pick < 0 || BOARD[i].price > BOARD[pick].price) pick = i; }
        return best || all.isEmpty() ? pick : all.get(r.nextInt(all.size()));
    }

    // ---- money and movement ----
    void log(String s) { log.add(s); if (log.size() > 60) log.remove(0); }
    void say(int p, String s) { beats.add("say " + p + " " + s); }

    void gain(int p, int amt, int from) {
        if (amt <= 0) return;
        cash[p] += amt;
        if (from == CAN) pot -= amt;
        beats.add("pay " + from + " " + p + " " + amt);
    }
    /** Pays what it can. Sells rentals to the bank at half value if short; after that it's an IOU. */
    int charge(int p, int amt, int to) {
        if (amt <= 0) return 0;
        while (cash[p] < amt) {
            int sq = cheapest(p);
            if (sq < 0) break;
            int v = value(sq) / 2;
            owner[sq] = -1; level[sq] = 0; env[sq] = 0; cash[p] += v;
            beats.add("lose " + p + " " + sq + " " + v);
            log(names[p] + " dumps " + BOARD[sq].tag + " on the bank for $" + v + ".");
        }
        int paid = amt;
        if (cash[p] < amt) {
            paid = cash[p]; iou[p]++;
            beats.add("pic iou");
            say(p, "I'm good for it!");
            log(names[p] + " is broke. IOU #" + iou[p] + " to Rob (-$100 at the end).");
        }
        cash[p] -= paid;
        if (to >= 0) cash[to] += paid; else if (to == CAN) pot += paid;
        if (paid > 0) beats.add("pay " + p + " " + to + " " + paid);
        return paid;
    }
    void collect(int p, int sq) {
        int amt = env[sq] * envUnit(sq);
        env[sq] = 0; cash[p] += amt; envGot[p] += amt;
        beats.add("env " + p + " " + sq + " " + amt);
    }
    /** Walk forward. Rent is collected by hand: you pick up your envelopes as you pass your own rentals. */
    void forward(int p, int steps) {
        int from = pos[p];
        for (int i = 0; i < steps; i++) {
            int sq = pos[p] = (pos[p] + 1) % N;
            if (sq == 0) {
                beats.add("move " + p + " " + from + " " + sq + " 1"); from = sq;
                gain(p, 150 + (ch[p] == SAM ? 75 : 0) + (ownsHood(p, BEAVERCREEK) ? 100 : 0), BANK);
            } else if (owner[sq] == p && env[sq] > 0) {
                beats.add("move " + p + " " + from + " " + sq + " 1"); from = sq;
                collect(p, sq);
            }
        }
        if (from != pos[p]) beats.add("move " + p + " " + from + " " + pos[p] + " 1");
    }
    void back(int p, int steps) {
        int from = pos[p];
        pos[p] = (pos[p] - steps + N) % N;
        beats.add("move " + p + " " + from + " " + pos[p] + " -1");
    }
    void jump(int p, int sq) {
        if (pos[p] == sq) return;
        beats.add("move " + p + " " + pos[p] + " " + sq + " 0");
        pos[p] = sq;
    }
    int die(int p) { int d = d6(); beats.add("die " + p + " " + d); return d; }

    void toJail(int p) {
        beats.add("jail " + p + " " + pos[p]);
        pos[p] = FRICKERS_SQ; jail[p] = 3; again = false; wings[p]++;
        log(names[p] + " goes to Wing Jail.");
    }

    // ---- turns ----
    public void act(int p, String[] t) {
        if (kind == OVER || p != who() || t.length == 0) return;
        String a = t[0];
        if (kind == TRADE) {
            if (a.equals("yes")) doTrade(); else { say(tTo, "No deal."); log(names[tTo] + " turns " + names[turn] + " down."); beats.add("snd no"); }
            tTo = -1; kind = ROLL;
        } else if (kind == ROLL && a.equals("trade") && t.length >= 5) {
            propose(p, num(t[1]), num(t[2]), list(t[3]), list(t[4]));
        } else if (kind == ROLL && a.equals("play") && t.length > 1) {
            int i = num(t[1]);
            if (i < 0 || i >= hand.get(p).size()) return;
            int id = hand.get(p).get(i);
            if (!usable(p, id)) return;
            hand.get(p).remove(i);
            beats.add("card R " + id + " " + p);
            log(names[p] + " plays " + ROBC[id].title + ".");
            trick(p, id);
        } else if (kind == ROLL && a.equals("roll")) {
            kind = BUSY;
            int d1 = d6(), d2 = townIs("outage") ? 0 : d6();
            beats.add("roll " + p + " " + d1 + " " + d2);
            settleBets(p, d1, d2);
            if (d1 == d2 && extraUsed) {
                say(p, "Way too wound up.");
                log(names[p] + " rolls doubles twice. Somebody get them to Frickers.");
                toJail(p); endTurn();
                return;
            }
            if (d1 == d2) again = true;
            forward(p, d1 + d2);
            land(p, 0);
            if (kind == BUSY) endTurn();
        } else if (kind == JAIL && a.equals("tab")) {
            log(names[p] + " pays the $" + TAB + " tab and walks out of Wing Jail.");
            charge(p, TAB, CAN); jail[p] = 0; kind = ROLL;
        } else if (kind == JAIL && a.equals("eat")) {
            kind = BUSY;
            int d1 = d6(), d2 = townIs("outage") ? 0 : d6();
            beats.add("roll " + p + " " + d1 + " " + d2);
            settleBets(p, d1, d2);
            boolean out = d2 == 0 ? d1 == 6 : d1 == d2;
            if (out) { jail[p] = 0; say(p, "Cleaned the plate!"); log(names[p] + " eats their way out of Wing Jail."); }
            else if (--jail[p] == 0) { say(p, "Kitchen's closed."); log(names[p] + " gets kicked out of Wing Jail and pays the tab."); charge(p, TAB, CAN); }
            else { say(p, "Another basket..."); log(names[p] + " orders another basket. Still in Wing Jail."); endTurn(); return; }
            forward(p, d1 + d2);
            land(p, 0);
            if (kind == BUSY) endTurn();
        } else if (kind == BUY && (a.equals("buy") || a.equals("pass"))) {
            if (a.equals("buy")) buy(p, psq, pcost);
            else log(names[p] + " passes on " + BOARD[psq].tag + ".");
            endTurn();
        } else if (kind == UP && (a.equals("up") || a.equals("skip"))) {
            if (a.equals("up")) { charge(p, pcost, BANK); upgrade(p, psq); }
            endTurn();
        }
    }
    static int num(String s) { try { return Integer.parseInt(s); } catch (NumberFormatException e) { return -1000000; } }
    static List<Integer> list(String s) {
        List<Integer> l = new ArrayList<>();
        if (!s.equals("-")) for (String x : s.split(",")) { int v = num(x); if (!l.contains(v)) l.add(v); }
        return l;
    }

    // ---- things you can do when it isn't your turn ----
    /** Side bets pay out on the roll: high is 8+, low is 6 or less, 7 loses. With one die (power outage): 4-6 high, 1-3 low. */
    void settleBets(int roller, int d1, int d2) {
        rolls++;
        boolean hi = d2 == 0 ? d1 >= 4 : d1 + d2 >= 8, lo = d2 == 0 ? d1 <= 3 : d1 + d2 <= 6;
        for (int q = 0; q < n; q++) if (bet[q] != 0) {
            if (bet[q] > 0 ? hi : lo) { say(q, "Called it!"); gain(q, BET, BANK); }
            else charge(q, BET, CAN);
            bet[q] = 0;
        }
    }
    /** bet hi|lo (not on your own roll), fix <square>, sell <square>. Anybody, any time the game isn't mid-move. */
    public void side(int p, String[] t) {
        if (kind == OVER || kind == BUSY || p < 0 || p >= n || t.length < 2) return;
        int sq = num(t[1]);
        boolean mine = sq >= 0 && sq < N && owner[sq] == p && !(kind == TRADE && (tGive.contains(sq) || tGet.contains(sq)));
        if (t[0].equals("bet") && betsOn && p != turn && (kind == ROLL || kind == JAIL) && bet[p] == 0 && cash[p] >= BET) {
            bet[p] = t[1].equals("hi") ? 1 : -1;
            say(p, "$" + BET + " says " + (bet[p] > 0 ? "high." : "low."));
        } else if (t[0].equals("fix") && mine && level[sq] < 2) {
            int c = contractor(sq, ownsHood(p, CENTERVILLE), townIs("blackfriday"));
            if (cash[p] < c) return;
            log(names[p] + " calls a contractor.");
            charge(p, c, BANK); upgrade(p, sq);
        } else if (t[0].equals("sell") && mine) {
            int v = value(sq) / 2;
            owner[sq] = -1; level[sq] = 0; env[sq] = 0; cash[p] += v;
            beats.add("lose " + p + " " + sq + " " + v);
            log(names[p] + " sells " + BOARD[sq].tag + " to the bank for $" + v + ".");
        }
    }

    // ---- trading ----
    /** One offer per turn, before rolling. cash > 0: the proposer adds cash. cash < 0: the proposer asks for it. */
    void propose(int p, int to, int cash0, List<Integer> give, List<Integer> get) {
        if (!tradingOn || traded || to < 0 || to >= n || to == p || give.size() + get.size() == 0 || give.size() > 4 || get.size() > 4) return;
        for (int sq : give) if (sq < 0 || sq >= N || owner[sq] != p) return;
        for (int sq : get) if (sq < 0 || sq >= N || owner[sq] != to) return;
        if (cash0 < -5000 || cash0 > cash[p] || -cash0 > cash[to]) return;
        traded = true; kind = TRADE; tTo = to; tCash = cash0; tGive = give; tGet = get;
        beats.add("offer " + p + " " + to);
        log(names[p] + " slides " + names[to] + " an offer.");
    }
    void doTrade() {
        int p = turn, to = tTo;
        say(to, "Deal.");
        for (int sq : tGive) { owner[sq] = to; beats.add("take " + to + " " + sq); }
        for (int sq : tGet) { owner[sq] = p; beats.add("take " + p + " " + sq); }
        if (tCash > 0) { cash[p] -= tCash; cash[to] += tCash; beats.add("pay " + p + " " + to + " " + tCash); }
        if (tCash < 0) { cash[to] += tCash; cash[p] -= tCash; beats.add("pay " + to + " " + p + " " + -tCash); }
        deals[p]++; deals[to]++;
        log(names[p] + " and " + names[to] + " shake on it. " + (tGive.size() + tGet.size()) + " rental" + (tGive.size() + tGet.size() == 1 ? " changes" : "s change") + " hands.");
        for (int q : new int[]{p, to}) for (int g = 0; g < HOOD.length; g++) {
            boolean touched = false;
            for (int sq : tGive) if (BOARD[sq].group == g) touched = true;
            for (int sq : tGet) if (BOARD[sq].group == g) touched = true;
            if (touched && ownsHood(q, g)) { beats.add("hood " + q + " " + g); log(names[q] + " owns all of " + HOOD[g] + "."); }
        }
    }

    void buy(int p, int sq, int cost) {
        charge(p, cost, BANK);
        owner[sq] = p; level[sq] = 0; env[sq] = 0;
        beats.add("buy " + p + " " + sq);
        log(names[p] + " buys " + BOARD[sq].name + " for $" + cost + ".");
        if (fullSet(sq)) { beats.add("hood " + p + " " + BOARD[sq].group); log(names[p] + " owns all of " + HOOD[BOARD[sq].group] + "."); }
    }
    static final String[] LEVELS = {"as-is", "fresh paint", "granite countertops"};
    void upgrade(int p, int sq) {
        level[sq]++;
        beats.add("up " + p + " " + sq);
        log(names[p] + " fixes up " + BOARD[sq].tag + ": " + LEVELS[level[sq]] + ".");
    }

    void endTurn() {
        int p = turn;
        psq = -1;
        if (again && !extraUsed && skip[p] == 0 && jail[p] == 0) {
            extraUsed = true; again = false; kind = ROLL;
            say(p, "Can't sit still. Going again!");
            return;
        }
        extraUsed = false; again = false; traded = false;
        while (true) {
            turn = (turn + 1) % n;
            if (turn == 0 && !newRound()) return;
            if (skip[turn] == 0) break;
            skip[turn]--; skipped[turn]++;
            beats.add("skip " + turn);
            log(names[turn] + " skips a turn.");
        }
        kind = jail[turn] > 0 ? JAIL : ROLL;
        beats.add("turn " + turn);
    }

    /** New month: town news, then rent goes into the envelopes. False when the game is over. */
    boolean newRound() {
        round++;
        if (round > maxRounds) {
            round = maxRounds; kind = OVER; town = -1;
            beats.add("over");
            int w = rank()[0];
            log("That's the year. " + names[w] + " wins with $" + worth(w) + ".");
            return false;
        }
        beats.add("banner " + (round == maxRounds ? "LAST MONTH!" : "MONTH " + round));
        town = draw(townDeck, TOWNC.length, null);
        Card c = TOWNC[town];
        beats.add("card W " + town + " -1");
        log("Month " + round + ". Town news: " + c.title + ".");
        switch (c.key) {
            case "vortex": for (int i = 0; i < n; i++) charge(i, 15 * count(i), BANK); break;
            case "allyoucan": for (int i = 0; i < n; i++) charge(i, 25, CAN); break;
            case "taxes": for (int i = 0; i < n; i++) charge(i, r5(cash[i] * 0.1), CAN); break;
            case "redswin": for (int i = 0; i < n; i++) gain(i, 50, BANK); break;
            case "boil": for (int i = 0; i < n; i++) gain(i, 40, BANK); break;
            case "fair": pot += 100; beats.add("pay " + BANK + " " + CAN + " 100"); break;
            case "leaves": for (int i = 0; i < n; i++) if (jail[i] == 0) back(i, 2); break;
            default: break;
        }
        if (!c.key.equals("strike")) for (int i = 0; i < N; i++) if (owner[i] >= 0)
            env[i] = Math.min(3, env[i] + (BOARD[i].group == HUBER && fullSet(i) ? 2 : 1));
        for (int i = 0; i < n; i++) {
            if (ownsHood(i, FAIRBORN)) gain(i, 50, BANK);
            if (ch[i] == JULES_CH) {
                int sq = fattest(i, false);
                if (sq >= 0) {
                    int amt = envUnit(sq);
                    env[sq]--; cash[i] += amt; envGot[i] += amt;
                    beats.add("env " + i + " " + sq + " " + amt + " 1");
                    log(names[i] + " Venmo-requests a month of rent at " + BOARD[sq].tag + ".");
                }
            }
        }
        return true;
    }

    public int[] rank() {
        int[] o = new int[n];
        for (int i = 0; i < n; i++) o[i] = i;
        for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) if (worth(o[j]) > worth(o[i])) { int x = o[i]; o[i] = o[j]; o[j] = x; }
        return o;
    }

    // ---- landing ----
    void land(int p, int depth) {
        int sq = pos[p];
        Sq q = BOARD[sq];
        switch (q.kind) {
            case PROP: {
                int o = owner[sq];
                if (o < 0) {
                    int c = price(p, sq);
                    if (cash[p] >= c) { kind = BUY; psq = sq; pcost = c; }
                    else log(names[p] + " can't afford " + q.tag + ".");
                } else if (o == p) {
                    if (env[sq] > 0) collect(p, sq);
                    int c = upCost(p, sq);
                    if (level[sq] < 2 && cash[p] >= c) { kind = UP; psq = sq; pcost = c; }
                } else {
                    int rent = rent(sq);
                    if (ch[p] == JACK) rent = Math.max(5, r5(rent / 3.0));
                    log(names[p] + " crashes at " + names[o] + "'s " + q.tag + ". Rent: $" + rent + ".");
                    rentPaid[p] += charge(p, rent, o);
                }
                break;
            }
            case ROBSQ: {
                int id = draw(robDeck, ROBC.length, ROBC);
                Card c = ROBC[id];
                beats.add("card R " + id + " " + p);
                for (int i = 0; i < n; i++) if (i != p && ownsHood(i, TIPP)) gain(i, 20, BANK);
                if (!c.keep) { log(names[p] + ": " + c.title + "."); robCard(p, c.key, depth); }
                else if (hand.get(p).size() < MAX_HAND) { hand.get(p).add(id); log(names[p] + " pockets " + c.title + "."); }
                else if (usable(p, id)) { log(names[p] + "'s hands are full, so " + c.title + " happens right now."); trick(p, id); }
                else log(names[p] + "'s hands are full. " + c.title + " blows out the truck window.");
                break;
            }
            case TENANT: {
                int id = draw(tenDeck, TENC.length, null);
                beats.add("card T " + id + " " + p);
                log(names[p] + ": " + TENC[id].title + ".");
                if (count(p) == 0) { say(p, "No tenants, no problems."); gain(p, 20, BANK); }
                else tenantCard(p, TENC[id].key);
                break;
            }
            case GIG: {
                int amt = (20 + 20 * die(p)) * (townIs("boil") ? 2 : 1);
                log(names[p] + " works the " + q.tag + " for $" + amt + ".");
                gain(p, amt, BANK);
                break;
            }
            case FRICKERS: {
                charge(p, 30, CAN);
                if (die(p) >= 5) { say(p, "Wing contest champ!"); log(names[p] + " wins the Frickers wing contest."); gain(p, 120, BANK); }
                else { say(p, "Extra ranch."); log(names[p] + " picks up takeout at Frickers."); }
                break;
            }
            case WINGNIGHT:
                say(p, "Just one basket...");
                toJail(p);
                break;
            case COFFEE:
                if (pot > 0) { beats.add("pic can"); log(names[p] + " finds $" + pot + " in the Coffee Can."); say(p, "Finders keepers."); gain(p, pot, CAN); pot = 50; }
                break;
            case LOWES:
                beats.add("pic lowes");
                log(names[p] + " went into Lowe's for one washer. Came out with a pressure washer.");
                charge(p, 40, CAN);
                break;
            case JULES:
                if (ch[p] == JULES_CH) { beats.add("pic jules"); say(p, "Dad picked up on the first ring."); log(names[p] + " calls her dad. He always picks up."); gain(p, 100, BANK); }
                else if (die(p) >= 4) { beats.add("pic jules"); say(p, "She picked up!"); log(names[p] + " calls Jules. Twelve whole minutes."); gain(p, 100, BANK); }
                else { beats.add("pic voicemail"); say(p, "Voicemail."); log(names[p] + " leaves Jules a four-minute voicemail."); }
                break;
            case EX:
                beats.add("pic ex");
                log(names[p] + " fixes the ex's garbage disposal. No charge. Parts were $30.");
                charge(p, 30, BANK);
                break;
            case SQUIRREL: {
                beats.add("pic squirrel");
                int d = die(p);
                say(p, "SQUIRREL!");
                log(names[p] + " gets distracted and wanders " + (d % 2 == 0 ? "ahead " : "back ") + d + ".");
                if (d % 2 == 0) forward(p, d); else back(p, d);
                if (depth < 2) land(p, depth + 1);
                break;
            }
            default: break;
        }
    }

    int draw(List<Integer> deck, int size, Card[] dup) {
        if (deck.isEmpty()) {
            for (int i = 0; i < size; i++) { deck.add(i); if (dup != null && dup[i].keep) deck.add(i); }   // two of each keeper
            for (int i = deck.size() - 1; i > 0; i--) { int j = r.nextInt(i + 1); int x = deck.get(i); deck.set(i, deck.get(j)); deck.set(j, x); }
        }
        return deck.remove(deck.size() - 1);
    }

    void robCard(int p, String key, int depth) {
        switch (key) {
            case "wings": toJail(p); break;
            case "gutters": jump(p, EX_SQ); if (depth < 2) land(p, depth + 1); break;
            case "truck": skip[p]++; break;
            case "tower": gain(p, 120, BANK); break;
            case "glovebox": gain(p, 150, BANK); break;
            case "k": gain(p, 60, BANK); break;
            case "garage": gain(p, 70, BANK); break;
            case "relax": gain(p, 40, BANK); break;
            case "spill": charge(p, 70, BANK); break;
            case "cooler": charge(p, 40, BANK); break;
            case "projects": charge(p, 60, CAN); break;
            case "reds": charge(p, 30, CAN); break;
            case "energy": again = true; extraUsed = false; break;
            case "round": for (int i = 0; i < n; i++) if (i != p) charge(p, 20, i); break;
            case "venmo": { int q = (p + 1 + r.nextInt(n - 1)) % n; say(q, "Thanks?"); charge(p, 80, q); break; }
            case "squirrel": back(p, die(p)); if (depth < 2) land(p, depth + 1); break;
            case "moves": {
                List<Integer> free = new ArrayList<>();
                for (int i = 0; i < n; i++) if (i != p && jail[i] == 0) free.add(i);
                if (free.isEmpty()) { say(p, "Nobody to swap with."); break; }
                int q = free.get(r.nextInt(free.size())), a = pos[p], b = pos[q];
                jump(p, b); jump(q, a);
                log(names[p] + " and " + names[q] + " swap places.");
                break;
            }
            case "fourth": {
                int sq = -1;
                for (int i = 0; i < N; i++) if (BOARD[i].kind == PROP && owner[i] < 0 && (sq < 0 || BOARD[i].price < BOARD[sq].price)) sq = i;
                int cost = sq < 0 ? 0 : r5(price(p, sq) / 2.0);
                if (sq >= 0 && cash[p] >= cost) buy(p, sq, cost);
                else say(p, sq < 0 ? "Nothing left to buy!" : "Card declined.");
                break;
            }
            default: break;
        }
    }

    /** A Tenant Trouble bill. The Kettering HOA President never pays these. */
    void bill(int p, int amt, int to) {
        if (ownsHood(p, KETTERING)) say(p, "HOA President. Waived.");
        else charge(p, amt, to);
    }
    void tenantCard(int p, String key) {
        switch (key) {
            case "heater": bill(p, 60, BANK); break;
            case "raccoons": bill(p, 50, BANK); break;
            case "cousin": bill(p, 40, BANK); break;
            case "furnace": gain(p, 80, BANK); break;
            case "pool": gain(p, 100, BANK); break;
            case "latefees": gain(p, 15 * count(p), BANK); break;
            case "inspect": bill(p, 20 * count(p), CAN); break;
            case "tax": bill(p, r5(holdings(p) * 0.05), CAN); break;
            case "nobody": skip[p]++; break;
            case "quarters": for (int i = 0; i < N; i++) if (owner[i] == p && env[i] > 0) collect(p, i); break;
            case "cookies": for (int i = 0; i < N; i++) if (owner[i] == p && env[i] < 3) env[i]++; break;
            case "paidwings": { int sq = fattest(p, false); if (sq >= 0) env[sq] = 0; say(p, "Honestly? Worth it."); break; }
            case "skipped": { int sq = fattest(p, false); if (sq >= 0) env[sq] = 0; bill(p, 30, BANK); break; }
            case "improved": { int sq = upgradable(p, false); if (sq >= 0) upgrade(p, sq); else gain(p, 50, BANK); break; }
            case "handshake": if (die(p) >= 4) gain(p, 90, BANK); else bill(p, 50, BANK); break;
            default: break;
        }
    }

    /** Can p play this keeper right now? */
    public boolean usable(int p, int id) {
        int lead = leader(p);
        switch (ROBC[id].key) {
            case "rentsdue": return true;
            case "tipoff": case "story": return lead >= 0;
            case "poach": return fattest(p, true) >= 0;
            case "sheriff": { int sq = lead < 0 ? -1 : cheapest(lead); return sq >= 0 && cash[p] >= value(sq); }
            case "guy": return upgradable(p, true) >= 0;
            default: return false;
        }
    }
    void trick(int p, int id) {
        int lead = leader(p);
        switch (ROBC[id].key) {
            case "rentsdue":
                for (int i = 0; i < n; i++) if (i != p) { if (ownsHood(i, OAKWOOD)) say(i, "Old Money. No."); else charge(i, 40, p); }
                break;
            case "tipoff": say(lead, "Who called the city?!"); charge(lead, 100, CAN); break;
            case "story": skip[lead]++; say(lead, "...and then what happened?"); log(names[lead] + " is stuck listening. Skips a turn."); break;
            case "guy": upgrade(p, upgradable(p, true)); break;
            case "poach": {
                int sq = fattest(p, true), amt = env[sq] * envUnit(sq);
                say(owner[sq], "That was MY envelope!");
                log(names[p] + " grabs $" + amt + " off the porch at " + names[owner[sq]] + "'s " + BOARD[sq].tag + ".");
                env[sq] = 0; cash[p] += amt;
                beats.add("env " + p + " " + sq + " " + amt);
                break;
            }
            case "sheriff": {
                int sq = cheapest(lead), v = value(sq);
                charge(p, v, lead);
                owner[sq] = p;
                beats.add("take " + p + " " + sq);
                log(names[p] + " buys " + BOARD[sq].tag + " out from under " + names[lead] + " for $" + v + ".");
                break;
            }
            default: break;
        }
    }

    // ---- for the screen ----
    public List<String> takeBeats() { List<String> b = beats; beats = new ArrayList<>(); return b; }

    /** Seconds the screen needs to show one beat. The table host waits this long before the next move. */
    public static float dur(String beat) {
        String[] t = beat.split(" ");
        switch (t[0]) {
            case "roll": return 1.5f;
            case "die": return 1.1f;
            case "move": {
                int a = num(t[2]), b = num(t[3]), d = num(t[4]);
                if (d == 0) return 0.7f;
                int steps = d > 0 ? (b - a + N) % N : (a - b + N) % N;
                return 0.15f * (steps == 0 ? N : steps) + 0.15f;
            }
            case "pay": return 0.55f;
            case "env": return 0.35f;
            case "buy": case "take": case "lose": return 0.9f;
            case "up": return 0.7f;
            case "card": return t[1].equals("W") ? 3.8f : 3.2f;
            case "pic": return 1.9f;
            case "banner": return 1.3f;
            case "hood": return 2.8f;
            case "jail": return 3.4f;
            case "say": return 1.1f;
            case "skip": return 1.0f;
            case "offer": return 0.6f;
            case "turn": return 0.35f;
            case "over": return 1.0f;
            default: return 0.3f;
        }
    }

    public String[][] awards() {
        return new String[][]{
                {"Landlord of the Year", "most rentals", best(i -> count(i))},
                {"Frickers Regular", "most trips to Wing Jail", best(i -> wings[i])},
                {"Collects By Hand", "most rent picked up in person", best(i -> envGot[i])},
                {"Couch Surfer", "most rent paid to other people", best(i -> rentPaid[i])},
                {"Let's Make A Deal", "most trades", best(i -> deals[i])},
                {"I'm Good For It", "most IOUs", best(i -> iou[i])}};
    }
    interface Stat { int of(int p); }
    String best(Stat s) {
        int b = 0;
        for (int i = 1; i < n; i++) if (s.of(i) > s.of(b)) b = i;
        return s.of(b) > 0 ? String.valueOf(b) : "-1";
    }

    static String q(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') b.append('\\').append(c); else if (c >= ' ') b.append(c);
        }
        return b.append('"').toString();
    }
    static String arr(int[] a) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < a.length; i++) { if (i > 0) b.append(','); b.append(a[i]); }
        return b.append(']').toString();
    }
    static String arr(List<Integer> a) {
        int[] x = new int[a.size()];
        for (int i = 0; i < x.length; i++) x[i] = a.get(i);
        return arr(x);
    }
    public String toJson() {
        StringBuilder b = new StringBuilder("{");
        b.append("\"round\":").append(round).append(",\"max\":").append(maxRounds).append(",\"turn\":").append(turn)
                .append(",\"who\":").append(who()).append(",\"kind\":").append(kind).append(",\"sq\":").append(psq).append(",\"cost\":").append(pcost)
                .append(",\"pot\":").append(pot).append(",\"town\":").append(town).append(",\"traded\":").append(traded)
                .append(",\"tTo\":").append(tTo).append(",\"tCash\":").append(tCash).append(",\"tGive\":").append(arr(tGive)).append(",\"tGet\":").append(arr(tGet))
                .append(",\"cash\":").append(arr(cash)).append(",\"pos\":").append(arr(pos)).append(",\"skip\":").append(arr(skip))
                .append(",\"jail\":").append(arr(jail)).append(",\"bet\":").append(arr(bet)).append(",\"iou\":").append(arr(iou)).append(",\"owner\":").append(arr(owner))
                .append(",\"level\":").append(arr(level)).append(",\"env\":").append(arr(env));
        int[] w = new int[n];
        for (int i = 0; i < n; i++) w[i] = worth(i);
        b.append(",\"worth\":").append(arr(w)).append(",\"hand\":[");
        for (int i = 0; i < n; i++) { if (i > 0) b.append(','); b.append(arr(hand.get(i))); }
        b.append("],\"log\":[");
        int from = Math.max(0, log.size() - 8);
        for (int i = from; i < log.size(); i++) { if (i > from) b.append(','); b.append(q(log.get(i))); }
        b.append(']');
        if (kind == OVER) {
            b.append(",\"rank\":").append(arr(rank())).append(",\"awards\":[");
            String[][] aw = awards();
            for (int i = 0; i < aw.length; i++) {
                if (i > 0) b.append(',');
                b.append('[').append(q(aw[i][0])).append(',').append(q(aw[i][1])).append(',').append(aw[i][2]).append(']');
            }
            b.append(']');
        }
        return b.append('}').toString();
    }
}
