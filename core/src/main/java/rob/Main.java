package rob;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;

import rob.rules.Game;
import rob.rules.Session;

/** The whole client. Shows the last state the table sent and plays its "beats" (dice, hops, cards, coins) in order. */
public class Main extends ApplicationAdapter {
    public interface Net {
        Link connect();
        /** Can this platform read text out loud? (Browsers can.) */
        default boolean canSpeak() { return false; }
        default void speak(String text) { }
    }

    static Color col(int rgb) { return new Color(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, 1); }
    static final Color BG = col(0x10282F), EDGE = col(0x0A1A1F), FELT = col(0x1F6F5C), FELT2 = col(0x1A5F4F), TILE = col(0xF6EEDB),
            INK = col(0x1B1B1F), PAPER = col(0xFFF9E8), GOLD = col(0xF2C230), MONEY = col(0x2FB26A), CASH = col(0x7CE3A1),
            RED = col(0xE04B4B), ROW = col(0x1B3A44), ROW_HOT = col(0x2A5563), SOFT = col(0xA9C1C7), PANEL = col(0x16323B),
            SLATE = col(0x5B6B7A), DIM = new Color(0, 0, 0, 0.62f), CELERY = col(0x96CD6E), WING = col(0xD67828), BONE = col(0xFAF0DC);
    /** Player colours, by character. */
    static final Color[] PC = {col(0xD0342C), col(0x9B5DE5), col(0x2D7DD2), col(0x2E9E5B), col(0x4A5568), col(0xF08A24)};
    /** Neighbourhood colours. */
    static final Color[] GC = {col(0x8B5A2B), col(0x7EC8E3), col(0xE86AA6), col(0xF39C12), col(0xE74C3C), col(0xF4D03F), col(0x27AE60), col(0x2E5FD0)};
    /** Square colours by kind (see Game). */
    static final Color[] KIND = {col(0x9BE19A), TILE, col(0xF7B267), col(0xC3B1F0), col(0x9AD9EA), col(0xF7C948), col(0xD4AE84),
            col(0x86A8D8), col(0xF6BBCB), col(0xDADADA), col(0xBFE88F), col(0xF08A24)};
    static final String[] SYMBOL = {"", "", "?", "!", "$", "", "", "", "", "", "", ""};
    static final String[] SOUNDS = {"dice", "hop", "coin", "card", "buy", "up", "lose", "jail", "pop", "click", "turn", "win", "deal", "no", "hood"};

    static final String[][] TUTORIAL = {
            {"THE POINT", "tut_cast", "You're a landlord in Rob's world. Buy rentals, collect rent, and be worth the most when the year ends (12 months).\n\nWorth is your cash plus what your rentals and upgrades cost. Nobody gets knocked out."},
            {"YOUR TURN", "tut_dice", "Roll two dice and move clockwise.\n\nLand on a rental nobody owns and you can buy it. Pass, and the bank auctions it off to everybody else. Land on somebody else's and you pay them rent. Land on your own and you can fix it up: fresh paint, then granite countertops.\n\nDoubles go again. Doubles twice in a row means you're too wound up, and you go to Wing Jail."},
            {"RENT IS COLLECTED BY HAND", "tut_rent", "Rob doesn't do apps. Every month, tenant rent goes into an envelope on each rental you own, up to three months' worth.\n\nIt's only yours when you drive past: you pick up envelopes as you pass or land on your own rentals. Until then it sits on the porch, where other players' cards can grab it."},
            {"NEIGHBORHOODS", "boom", "Rentals come in neighborhoods of three, from Huber Heights (cheap) up to Oakwood.\n\nOwn two and visitors pay 50% more. Own all three and they pay double, and you get that neighborhood's perk.\n\nThe tracker in the middle of the board shows who has what. Hover any rental to read its perk."},
            {"CARDS AND TOWN NEWS", "rentsdue", "Land on Rob Happens or Tenant Trouble and you click the deck to draw. The card goes up for the whole table (and gets read out loud, if that is on) before you play it.\n\nOther squares give you a choice: the safe option, or a roll of the die.\n\nSome Rob Happens cards are keepers: hold up to three and play one before you roll. They hit whoever's winning.\n\nEvery month also starts with Town News, which hits everybody."},
            {"WING JAIL", "jail_0", "The Wing Night corner, the Wing Night card, or doubles twice in a row sends you to Wing Jail at Frickers.\n\nOn your turn, roll doubles to eat your way out, or pay the $50 tab. After three tries they kick you out and you pay anyway.\n\nYou can't pick up envelopes from in there."},
            {"TRADING", "tut_trade", "Before you roll, hit TRADE (once per turn).\n\nClick your rentals to offer them, click another player's rentals to ask for them, and add or ask for cash. Online they get 20 seconds to take it or leave it.\n\nBots make offers too."},
            {"WHILE YOU WAIT", "tut_dice", "There's stuff to do on other people's turns.\n\nBet $25 on their roll: HIGH is 8 or more, LOW is 6 or less, and a 7 loses. Win and the bank pays you $25. Lose and it goes in the Coffee Can.\n\nClick any rental you own, any time: call a contractor (fix it up from anywhere, for 50% more), call the tenant (they Venmo half the envelope), or sell it to the bank for half.\n\nOr hit WING RUN and help shirtless Rob eat wings while the salads chase him."},
            {"GOING BROKE", "bank", "If you can't pay, the bank takes your cheapest rental for half its value until you can. Out of rentals too? You take an IOU to Rob, worth -$100 at the end.\n\nKeys: Space rolls and says yes. T starts a trade. 1 to 5 are taunts. Esc opens the menu."}};

    static final float BX = 14, BY = 10, BS = 700, C = 92, S = (BS - 2 * C) / 9, CX = BX + BS / 2, CY = BY + BS / 2;
    static final float PX = 730, PW = 536, ROWH = 55;
    static final int COIN = 0, FLOAT = 1, STAMP = 2, RING = 3, BUBBLE = 4, CONFETTI = 5, DRUM = 6;
    static class Fx { int type; float t, dur, x0, y0, x1, y1; String s; Color c; }

    final Net net;
    final boolean auto, autoOnline;
    String name;
    Ui ui;
    Link link;
    boolean joined, online, sent, soundOn = true, musicOn = true, menuOpen, reconnecting;
    Music music;
    boolean settingsOpen, shakeOn = true;
    int sVol = 7, mVol = 4, manageSq = -1;
    float curRate = 1;
    int[] bet = {};
    int pcard = -1, aSq = -1, aHigh = -1, aNext, aBid;
    String pdeck = "R", ckey = "", cText = "", cA = "", cB = "";
    boolean cardHold, speakOn = true, oAuctions = true;
    // table rules, as the lobby last reported them
    int oSpeed, oCash = 700, oClock = 45;
    boolean oFast = true, oTrading = true, oBets = true, oOnline;
    int retries;
    float retryT, bannerT = 99, bannerD, hoodT = 99;
    int hoodP, hoodG, hoverG = -1;
    String bannerS = "";
    float[] flash = {};
    String err;
    float pingT, time, autoT;
    int screen, tutPage = -1;            // screen: 0 title, 1 connecting, 2 table
    Preferences prefs;
    TextureRegion[] face = new TextureRegion[7];      // six characters, then shirtless Rob for Wing Run
    WingRun run;
    boolean runOpen;
    int reported;
    final ObjectMap<String, Texture> pics = new ObjectMap<>();
    final ObjectMap<String, Sound> sfx = new ObjectMap<>();

    // what the table last told us
    int you = -1, ns, rounds = 12, limit;
    boolean lead, hasG, traded;
    String phase = "lobby";
    String[] sName = {}, log = {};
    int[] sCh = {};
    boolean[] sBot = {}, sAway = {};
    int round, max, turn, who, kind, psq, pcost, pot, town, tTo, tCash;
    int[] cash, pos, skip, jail, iou, worth, owner, level, env, rank, tGive = {}, tGet = {};
    int[][] hand;
    JsonValue awards;
    float[] showCash = {};
    float left;

    // building a trade offer
    boolean trading;
    int tP = -1, tCashSel;
    final IntArray giveSel = new IntArray(), getSel = new IntArray();

    // beats being played
    final Array<JsonValue> queue = new Array<>();
    final Array<Fx> fx = new Array<>();
    JsonValue cur;
    String[] beats;
    int bi, mvP = -1, mvTo, dA, dB, cardId, cardWho, lastHop, jailCh;
    int[] mvPath;
    float bt, bd, mvDur, diceT, cardT = 99, cardD, shake, overT, popT = 99, popD, jailT = 99;
    boolean diceOn, mvJump;
    char cardDeck;
    String popKey;
    final float[] R = new float[4], T = new float[2];

    /** mode: "" to play, "auto" to watch bots offline, "auto-online" to sit at the server's table on autopilot. */
    public Main(Net net, String name, String mode) {
        this.net = net; this.name = name == null ? "" : name.length() > 14 ? name.substring(0, 14) : name;
        auto = mode.startsWith("auto"); autoOnline = mode.equals("auto-online");
    }

    @Override public void create() {
        ui = new Ui();
        Texture f = new Texture(Gdx.files.internal("faces.png"), true);
        f.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        for (int i = 0; i < 7; i++) face[i] = new TextureRegion(f, (i % 4) * 128, (i / 4) * 128, 128, 128);
        run = new WingRun(this, ui, face[6]);
        for (String s : SOUNDS) try { sfx.put(s, Gdx.audio.newSound(Gdx.files.internal("sfx/" + s + ".wav"))); } catch (RuntimeException e) { /* play without it */ }
        try { music = Gdx.audio.newMusic(Gdx.files.internal("music/loop.mp3")); music.setLooping(true); music.setVolume(0.3f); } catch (RuntimeException e) { music = null; }
        try {
            prefs = Gdx.app.getPreferences("rents-due");
            soundOn = prefs.getBoolean("sound", true); musicOn = prefs.getBoolean("music", true);
            sVol = prefs.getInteger("svol", 7); mVol = prefs.getInteger("mvol", 4); shakeOn = prefs.getBoolean("shake", true); speakOn = prefs.getBoolean("speak", true);
            if (!auto && !prefs.getBoolean("seen", false)) { tutPage = 0; prefs.putBoolean("seen", true); prefs.flush(); }   // first visit: show how to play
            if (name.isEmpty()) name = prefs.getString("name", "");
        } catch (RuntimeException e) { prefs = null; }
        if (name.isEmpty()) name = "Rob";
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyTyped(char c) {
                if (screen == 3 && tutPage < 0 && !settingsOpen && c >= ' ' && c < 127 && c != '"' && c != '\\' && name.length() < 14) name += c;
                return true;
            }
            @Override public boolean touchDown(int x, int y, int pointer, int button) { ui.pressed = true; return true; }
            @Override public boolean keyDown(int k) {
                if (screen == 3 && tutPage < 0 && !settingsOpen && k == Input.Keys.BACKSPACE && name.length() > 0) name = name.substring(0, name.length() - 1);
                if (k == Input.Keys.ESCAPE) {
                    if (tutPage >= 0) tutPage = -1; else if (settingsOpen) { settingsOpen = false; save(); } else if (manageSq >= 0) manageSq = -1;
                    else if (trading) trading = false; else if (screen == 2) menuOpen = !menuOpen; else if (screen == 3) screen = 0;
                }
                if (k == Input.Keys.T && screen == 2 && !menuOpen && tutPage < 0 && !trading && mine() && kind == Game.ROLL && !traded && oTrading) startTrade();
                if (screen == 2 && hasG && phase.equals("game") && you >= 0 && k >= Input.Keys.NUM_1 && k <= Input.Keys.NUM_5) taunt(k - Input.Keys.NUM_1);
                return true;
            }
        });
        if (auto) play(autoOnline);
    }
    @Override public void resize(int w, int h) { ui.vp.update(w, h, true); }

    void save() {
        if (prefs == null) return;
        try { prefs.putBoolean("sound", soundOn); prefs.putBoolean("music", musicOn); prefs.putInteger("svol", sVol); prefs.putInteger("mvol", mVol); prefs.putBoolean("shake", shakeOn); prefs.putBoolean("speak", speakOn); prefs.putString("name", name); prefs.flush(); } catch (RuntimeException e) { /* no storage, no problem */ }
    }
    void snd(String s) { Sound x = soundOn ? sfx.get(s) : null; if (x != null) x.play(sVol / 10f); }
    /** The picture for a card or pop-up, or null if there isn't one. */
    Texture pic(String key) {
        if (!pics.containsKey(key)) {
            Texture t = null;
            try { if (Gdx.files.internal("pic/" + key + ".png").exists()) t = new Texture(Gdx.files.internal("pic/" + key + ".png")); } catch (RuntimeException e) { t = null; }
            if (t != null) t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            pics.put(key, t);
        }
        return pics.get(key);
    }
    boolean btn(String label, float x, float y, float w, float h, Color c, boolean enabled) {
        boolean hit = ui.button(label, x, y, w, h, c, enabled);
        if (hit) { snd("click"); tune(); ui.click = false; }      // one click, one button
        return hit;
    }
    /** Starts or stops the music to match the setting. Browsers only allow it after a click, so every button calls this. */
    void tune() {
        if (music == null) return;
        try { music.setVolume(mVol / 10f * 0.75f); if (musicOn && !music.isPlaying()) music.play(); else if (!musicOn && music.isPlaying()) music.pause(); } catch (RuntimeException e) { music = null; }
    }

    void play(boolean on) {
        save();
        online = on; err = null; joined = false; sent = false; menuOpen = false; trading = false;
        run.reset(); reported = 0; manageSq = -1; hasG = false; phase = "lobby"; ns = 0; you = -1; cur = null; queue.clear(); fx.clear(); cardT = popT = jailT = 99; diceOn = false; mvP = -1;
        try { link = on ? net.connect() : new Link.Local(); } catch (RuntimeException e) { err = "Couldn't connect: " + e.getMessage(); return; }
        screen = 1;
    }
    void leave(String why) {
        if (link != null) link.close();
        link = null; err = why; screen = 0; menuOpen = false; trading = false; reconnecting = false;
    }
    void send(String s) { if (link != null && link.open()) { link.send(s); sent = true; } }
    void taunt(int k) { if (link != null && link.open()) link.send("taunt " + k); }

    // ---------------------------------------------------------------- talking to the table
    void pump(float dt) {
        if (link == null && reconnecting && (retryT -= dt) <= 0) play(true);
        if (link == null) return;
        link.tick(dt);
        if (link.error() != null) {
            if (online && retries < 3) {        // dropped: knock again with the same name and take the seat back
                retries++; link.close(); link = null; reconnecting = true; retryT = 1.5f; screen = 1;
            } else leave(link.error());
            return;
        }
        if (!joined && link.open()) {
            joined = true;
            link.send("join " + name.trim());
            if (!online) for (int i = 0; i < 3; i++) link.send("bot+");
        }
        if (online && joined && (pingT += dt) > 20) { pingT = 0; link.send("ping"); }
        String m;
        while ((m = link.poll()) != null) { queue.add(new JsonReader().parse(m)); screen = 2; }

        while (cur == null && queue.size > 0) {
            JsonValue msg = queue.removeIndex(0);
            String[] bs = msg.get("beats").asStringArray();
            JsonValue mg = msg.get("g");
            if (cardHold && (mg == null || mg.getInt("kind") != Game.CARD)) { cardHold = false; cardT = 99; }     // the card got played: put it down
            if (!hasG || bs.length == 0 || msg.get("g") == null) apply(msg);
            if (bs.length > 0 && hasG) { cur = msg; curRate = msg.getFloat("rate", 1); beats = bs; bi = -1; nextBeat(); }
        }
        if (cur != null) {
            if (mvP >= 0 && !mvJump && (int) (bt / 0.15f) != lastHop) { lastHop = (int) (bt / 0.15f); snd("hop"); }
            if ((bt += dt * curRate) >= bd) nextBeat();
        } else if (limit > 0 && phase.equals("game")) left = Math.max(0, left - dt);
    }

    void apply(JsonValue m) {
        sent = false; retries = 0; reconnecting = false;
        you = m.getInt("you"); lead = m.getBoolean("lead"); phase = m.getString("phase"); rounds = m.getInt("rounds"); limit = m.getInt("limit");
        JsonValue o = m.get("opts");
        oSpeed = o.getInt("speed"); oFast = o.getBoolean("fast"); oCash = o.getInt("cash"); oTrading = o.getBoolean("trading");
        oBets = o.getBoolean("bets"); oAuctions = o.getBoolean("auctions"); oClock = o.getInt("clock"); oOnline = o.getBoolean("online");
        left = limit;
        JsonValue ss = m.get("seats");
        ns = ss.size; sName = new String[ns]; sCh = new int[ns]; sBot = new boolean[ns]; sAway = new boolean[ns];
        for (int i = 0; i < ns; i++) {
            JsonValue s = ss.get(i);
            sName[i] = s.getString("name"); sCh[i] = s.getInt("ch"); sBot[i] = s.getBoolean("bot"); sAway[i] = s.getBoolean("away");
        }
        JsonValue g = m.get("g");
        hasG = g != null;
        if (!hasG) { trading = false; return; }
        round = g.getInt("round"); max = g.getInt("max"); turn = g.getInt("turn"); who = g.getInt("who"); kind = g.getInt("kind"); psq = g.getInt("sq");
        pcost = g.getInt("cost"); pot = g.getInt("pot"); town = g.getInt("town"); traded = g.getBoolean("traded");
        tTo = g.getInt("tTo"); tCash = g.getInt("tCash"); tGive = g.get("tGive").asIntArray(); tGet = g.get("tGet").asIntArray();
        cash = g.get("cash").asIntArray(); pos = g.get("pos").asIntArray(); skip = g.get("skip").asIntArray(); jail = g.get("jail").asIntArray(); bet = g.get("bet").asIntArray();
        pcard = g.getInt("pcard"); pdeck = g.getString("pdeck"); ckey = g.getString("ckey"); cText = g.getString("cText"); cA = g.getString("cA"); cB = g.getString("cB");
        aSq = g.getInt("aSq"); aHigh = g.getInt("aHigh"); aNext = g.getInt("aNext"); aBid = g.getInt("aBid");
        if (kind == Game.CARD && !cardHold && pcard >= 0) { cardDeck = pdeck.charAt(0); cardId = pcard; cardWho = turn; cardHold = true; cardT = 1; }   // joined mid-card
        iou = g.get("iou").asIntArray(); worth = g.get("worth").asIntArray(); owner = g.get("owner").asIntArray();
        level = g.get("level").asIntArray(); env = g.get("env").asIntArray();
        log = g.get("log").asStringArray();
        hand = new int[cash.length][];
        for (int i = 0; i < cash.length; i++) hand[i] = g.get("hand").get(i).asIntArray();
        rank = g.has("rank") ? g.get("rank").asIntArray() : null;
        awards = g.get("awards");
        if (flash.length != cash.length) flash = new float[cash.length];
        if (showCash.length != cash.length) { showCash = new float[cash.length]; for (int i = 0; i < cash.length; i++) showCash[i] = cash[i]; }
        if (kind != Game.ROLL || turn != you) trading = false;
        if (manageSq >= 0 && (you < 0 || owner[manageSq] != you)) manageSq = -1;
    }

    static int I(String s) { return Integer.parseInt(s); }

    void nextBeat() {
        if (mvP >= 0) { pos[mvP] = mvTo; mvP = -1; }
        bi++; bt = 0;
        if (bi >= beats.length) { apply(cur); cur = null; return; }
        String b = beats[bi];
        String[] t = b.split(" ");
        bd = Game.dur(b);
        switch (t[0]) {
            case "turn": turn = I(t[1]); diceOn = false; if (turn == you) snd("turn"); break;
            case "roll": diceOn = true; diceT = 0; dA = I(t[2]); dB = I(t[3]); snd("dice"); break;
            case "die": diceOn = true; diceT = 0; dA = I(t[2]); dB = 0; snd("dice"); break;
            case "move": {
                int p = I(t[1]), a = I(t[2]), to = I(t[3]), d = I(t[4]);
                mvP = p; mvTo = to; mvJump = d == 0; mvDur = bd; lastHop = -1;
                if (d == 0) mvPath = new int[]{a, to};
                else {
                    int steps = d > 0 ? (to - a + Game.N) % Game.N : (a - to + Game.N) % Game.N;
                    if (steps == 0) steps = Game.N;
                    mvPath = new int[steps + 1];
                    for (int i = 0; i <= steps; i++) mvPath[i] = ((a + d * i) % Game.N + Game.N) % Game.N;
                }
                break;
            }
            case "jail": {
                int p = I(t[1]);
                mvP = p; mvTo = Game.FRICKERS_SQ; mvJump = true; mvDur = 0.8f; mvPath = new int[]{I(t[2]), Game.FRICKERS_SQ};
                jailT = 0; jailCh = sCh[p]; diceOn = false; snd("jail");
                for (int i = 0; i < 26; i++) {
                    Fx f = add(DRUM, 2.6f, MathUtils.random(BX, BX + BS), 740 + MathUtils.random(0, 260), MathUtils.random(-40, 40), MathUtils.random(-420, -300), null, WING);
                    f.t = -MathUtils.random(0f, 0.6f);
                }
                break;
            }
            case "pay": {
                int from = I(t[1]), to = I(t[2]), amt = I(t[3]);
                point(from); float x0 = T[0], y0 = T[1];
                point(to);
                add(COIN, bd, x0, y0, T[0], T[1], null, GOLD); snd("coin");
                if (from == Game.CAN) for (int k = 1; k <= 8; k++) add(COIN, 0.6f, x0 + MathUtils.random(-20, 20), y0, T[0], T[1], null, GOLD).t = -k * 0.09f;   // jackpot
                if (to >= 0) flash[to] = 1;
                if (from >= 0) flash[from] = -1;
                if (to >= 0) { cash[to] += amt; add(FLOAT, 1.2f, T[0], T[1] + 24, 0, 0, "+$" + amt, CASH); }
                else if (to == Game.CAN) { pot += amt; add(FLOAT, 1.2f, T[0], T[1] + 24, 0, 0, "+$" + amt, GOLD); }
                if (from >= 0) { cash[from] -= amt; add(FLOAT, 1.2f, x0, y0 + 24, 0, 0, "-$" + amt, RED); }
                else if (from == Game.CAN) pot -= amt;
                break;
            }
            case "env": {
                int p = I(t[1]), sq = I(t[2]), amt = I(t[3]);
                sq(sq); float x0 = R[0] + R[2] / 2, y0 = R[1] + R[3] / 2;
                tok(p);
                add(COIN, bd, x0, y0, T[0], T[1], null, MONEY); snd("coin");
                add(FLOAT, 1.2f, x0, y0 + 20, 0, 0, "+$" + amt, CASH);
                env[sq] = t.length > 4 ? Math.max(0, env[sq] - 1) : 0; cash[p] += amt; flash[p] = 1;
                break;
            }
            case "buy": case "take": {
                int p = I(t[1]), sq = I(t[2]);
                owner[sq] = p;
                if (t[0].equals("buy")) { level[sq] = 0; env[sq] = 0; }
                stamp(sq, t[0].equals("buy") ? "SOLD!" : "YOINK!", PC[sCh[p]]); snd("buy");
                break;
            }
            case "lose": {
                int p = I(t[1]), sq = I(t[2]);
                owner[sq] = -1; level[sq] = 0; env[sq] = 0; cash[p] += I(t[3]);
                stamp(sq, "BANK'S NOW", SLATE); shake = 0.45f; snd("lose");
                break;
            }
            case "up": {
                int sq = I(t[2]);
                level[sq] = Math.min(2, level[sq] + 1);
                stamp(sq, level[sq] == 1 ? "FRESH PAINT" : "GRANITE!", col(0xB8860B)); snd("up");
                break;
            }
            case "card": cardDeck = t[1].charAt(0); cardId = I(t[2]); cardWho = I(t[3]); cardT = 0; cardD = bd; diceOn = false; snd("card");
                cardHold = t.length > 4;
                if (speakOn && net.canSpeak()) {
                    Game.Card rc = cardDeck == 'R' ? Game.ROBC[cardId] : cardDeck == 'T' ? Game.TENC[cardId] : Game.TOWNC[cardId];
                    net.speak((cardDeck == 'W' ? "Town news. " : "") + rc.title.toLowerCase() + ". " + rc.text);
                }
                break;
            case "auction": snd("deal"); diceOn = false; break;
            case "banner": bannerS = b.substring(7); bannerT = 0; bannerD = bd; snd("turn"); break;
            case "hood": {
                hoodP = I(t[1]); hoodG = I(t[2]); hoodT = 0; snd("hood");
                for (int i = 0; i < Game.N; i++) if (Game.BOARD[i].group == hoodG) { sq(i); add(RING, 1.2f, R[0] + R[2] / 2, R[1] + R[3] / 2, 0, 0, null, GC[hoodG]); }
                for (int i = 0; i < 60; i++) add(CONFETTI, 3f, MathUtils.random(BX, BX + BS), MathUtils.random(720, 860), MathUtils.random(-50, 50), MathUtils.random(-300, -160), null, i % 3 == 0 ? Color.WHITE : GC[hoodG]).t = -MathUtils.random(0f, 0.8f);
                break;
            }
            case "pic": popKey = t[1]; popT = 0; popD = bd; snd("pop"); break;
            case "say": bubble(I(t[1]), b.substring(b.indexOf(' ', 4) + 1)); snd("pop"); break;
            case "skip": bubble(I(t[1]), "Skips a turn."); snd("pop"); break;
            case "offer": snd("deal"); break;
            case "snd": snd(t[1]); break;
            case "over":
                overT = -1; snd("win");
                for (int i = 0; i < 90; i++) {
                    Fx f = add(CONFETTI, 4f, MathUtils.random(100, 1180), MathUtils.random(720, 900), MathUtils.random(-60, 60), MathUtils.random(-260, -120), null, GC[i % GC.length]);
                    f.t = -MathUtils.random(0f, 1.2f);
                }
                break;
            default: break;
        }
    }

    Fx add(int type, float dur, float x0, float y0, float x1, float y1, String s, Color c) {
        Fx f = new Fx();
        f.type = type; f.dur = dur; f.x0 = x0; f.y0 = y0; f.x1 = x1; f.y1 = y1; f.s = s; f.c = c;
        fx.add(f);
        return f;
    }
    void stamp(int sq, String s, Color c) {
        sq(sq);
        float x = MathUtils.clamp(R[0] + R[2] / 2, BX + 60, BX + BS - 60), y = R[1] + R[3] / 2;
        add(RING, 0.6f, x, y, 0, 0, null, c);
        add(STAMP, 1.3f, x, y, 0, 0, s, c);
    }
    void bubble(int p, String s) {
        tok(p);
        add(BUBBLE, 1.9f, T[0], T[1] + 26, 0, 0, s, PC[sCh[p]]);
    }

    // ---------------------------------------------------------------- geometry
    /** Square i as x, y, w, h in R. Square 0 is the bottom-left corner; they run clockwise. */
    void sq(int i) {
        float x, y, w = C, h = C;
        if (i == 0) { x = BX; y = BY; }
        else if (i < 10) { x = BX; y = BY + C + (i - 1) * S; h = S; }
        else if (i == 10) { x = BX; y = BY + BS - C; }
        else if (i < 20) { x = BX + C + (i - 11) * S; y = BY + BS - C; w = S; }
        else if (i == 20) { x = BX + BS - C; y = BY + BS - C; }
        else if (i < 30) { x = BX + BS - C; y = BY + BS - C - (i - 20) * S; h = S; }
        else if (i == 30) { x = BX + BS - C; y = BY; }
        else { x = BX + BS - C - (i - 30) * S; y = BY; w = S; }
        R[0] = x; R[1] = y; R[2] = w; R[3] = h;
    }
    /** Where player p stands on square i, in T. */
    void spot(int p, int i) {
        sq(i);
        T[0] = R[0] + R[2] / 2 + ((p % 3) - 1) * 16;
        T[1] = R[1] + R[3] / 2 + (p < 3 ? 8 : -12) - 2;
    }
    /** Where player p's token is right now, in T. */
    void tok(int p) {
        if (p != mvP) { spot(p, pos[p]); return; }
        float per = mvJump ? mvDur : 0.15f, f = Math.min(bt / per, mvPath.length - 1.001f);
        int k = (int) f;
        float u = f - k;
        spot(p, mvPath[k]); float x0 = T[0], y0 = T[1];
        spot(p, mvPath[k + 1]);
        T[0] = x0 + (T[0] - x0) * u;
        T[1] = y0 + (T[1] - y0) * u + MathUtils.sin(u * MathUtils.PI) * (mvJump ? 80 : 14);
    }
    /** A player's token, the bank (middle of the board) or the Coffee Can, in T. */
    void point(int w) {
        if (w >= 0) tok(w);
        else if (w == Game.CAN) { sq(20); T[0] = R[0] + R[2] / 2; T[1] = R[1] + R[3] / 2; }
        else { T[0] = CX; T[1] = CY + 60; }
    }
    boolean busy() { return cur != null || queue.size > 0; }
    /** Is the decision on the table mine to make right now? */
    boolean mine() { return hasG && phase.equals("game") && you == who && !busy() && !sent; }
    static String money(int v) {
        String s = String.valueOf(Math.abs(v));
        if (s.length() > 3) s = s.substring(0, s.length() - 3) + "," + s.substring(s.length() - 3);
        return (v < 0 ? "-$" : "$") + s;
    }
    String list(int[] sqs) {
        StringBuilder b = new StringBuilder();
        for (int s : sqs) b.append(b.length() > 0 ? ", " : "").append(Game.BOARD[s].tag);
        return b.length() == 0 ? "nothing" : b.toString();
    }

    // ---------------------------------------------------------------- drawing
    @Override public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);
        time += dt;
        pump(dt);
        boolean waiting = hasG && phase.equals("game") && you >= 0 && !mine();
        run.update(dt, runOpen && waiting && !menuOpen && tutPage < 0 && !settingsOpen && !trading);
        if (link != null && link.open() && hasG && phase.equals("game") && you >= 0 && (run.eaten >= reported + 25 || (run.eaten > reported && !waiting))) {
            reported = run.eaten;
            link.send("side wings " + reported);       // for the Wing King award
        }
        for (int i = 0; i < showCash.length && hasG; i++) showCash[i] += (cash[i] - showCash[i]) * Math.min(1, dt * 9);
        float adt = dt * (cur != null ? curRate : 1);       // faster tables play their animations faster
        diceT += adt; cardT += adt; overT += dt; popT += adt; jailT += adt; bannerT += adt; hoodT += adt; shake = Math.max(0, shake - dt);
        for (int i = 0; i < flash.length; i++) flash[i] = flash[i] > 0 ? Math.max(0, flash[i] - dt * 1.6f) : Math.min(0, flash[i] + dt * 1.6f);
        if (jailT > 0.8f && jailT - adt <= 0.8f) shake = 0.5f;
        for (int i = fx.size - 1; i >= 0; i--) { Fx f = fx.get(i); f.t += adt; if (f.t > f.dur) fx.removeIndex(i); }

        ScreenUtils.clear(BG);
        ui.b.getTransformMatrix().idt();
        if (shake > 0 && shakeOn) ui.b.getTransformMatrix().translate(MathUtils.random(-6, 6) * shake * 2, MathUtils.random(-6, 6) * shake * 2, 0);
        ui.b.setTransformMatrix(ui.b.getTransformMatrix());
        ui.begin();
        boolean modal = menuOpen || tutPage >= 0 || settingsOpen, click = ui.click;
        if (modal) ui.click = false;
        if (screen == 0) mainMenu();
        else if (screen == 3) playSetup();
        else if (screen == 4) bye();
        else if (screen == 1 || (screen == 2 && ns == 0 && !hasG))
            ui.textC(reconnecting || retries > 0 ? "Lost the server. Knocking again..." : online ? "Knocking on the server's door..." : "Setting up...", 640, 360, 26, Color.WHITE);
        else if (!hasG) lobby();
        else {
            board(); center(); tokens(); dice(); effects(); offer(); auction(); manage(); card(); popup(); jailShow(); banner(); hoodShow(); panel(); actions();
            if (!modal) tips();
            if (phase.equals("over") && !busy()) over();
            for (Fx f : fx) if (f.type == CONFETTI) effect(f);
        }
        ui.click = click;
        if (menuOpen && tutPage < 0 && !settingsOpen) menu();
        if (settingsOpen && tutPage < 0) settings();
        if (tutPage >= 0) tutorial();
        ui.end();

        if (auto && mine() && (autoT += dt) > 0.3f) {
            autoT = 0;
            send(kind == Game.ROLL ? "act roll" : kind == Game.BUY ? "act buy" : kind == Game.UP ? "act up" : kind == Game.JAIL ? "act eat" : kind == Game.DRAW ? "act draw" : kind == Game.CARD ? "act ok" : kind == Game.CHOICE ? "act a" : kind == Game.AUCTION ? "act bid" : "act yes");
        }
        if (auto && !hasG && ns > 1 && lead && !sent && (autoT += dt) > 1) { autoT = 0; send("start"); }
    }

    void mainMenu() {
        for (int k = 0; k < 16; k++)       // wings drifting down behind everything
            wing((k * 97 + 40) % 1280, 760 - (time * 34 * (1 + k % 3 * 0.35f) + k * 173) % 840, time * 40 * (k % 2 == 0 ? 1 : -1) + k * 50, 0.22f);
        for (int i = 0; i < 8; i++) ui.rect(i * 160, 0, 160, 14, GC[i]);
        ui.textC("RENT'S", 304, 614, 104, EDGE); ui.textC("RENT'S", 300, 620, 104, GOLD);
        ui.textC("DUE", 304, 516, 104, EDGE); ui.textC("DUE", 300, 522, 104, GOLD);
        ui.textC("an Uncle Rob game", 300, 446, 24, Color.WHITE);
        if (btn("PLAY", 150, 326, 300, 78, PC[0], true)) screen = 3;
        if (btn("HOW TO PLAY", 150, 254, 300, 56, col(0x2D7DD2), true)) tutPage = 0;
        if (btn("SETTINGS", 150, 184, 300, 56, SLATE, true)) settingsOpen = true;
        if (btn("QUIT", 150, 118, 300, 52, col(0x7A2E2E), true)) { save(); if (Gdx.app.getType() == Application.ApplicationType.WebGL) screen = 4; else Gdx.app.exit(); }
        if (err != null) ui.text(err, 600, 128, 14, RED, Align.center, 600);
        ui.textC("Twenty-four rentals. Zero apps.", 300, 78, 14, SOFT);
        ui.textC("Rent gets collected by hand, between wing nights.", 300, 58, 14, SOFT);
        Texture t = pic("title");
        if (t != null) {
            float bob = MathUtils.sin(time * 1.6f) * 5;
            ui.rrect(578, 152 + bob, 664, 420, 14, ui.a(Color.BLACK, 0.35f));
            framed(t, 900, 372 + bob, 640, 1);
        }
    }

    /** A browser tab can't close itself, so this is as quit as it gets. */
    void bye() {
        ui.textC("RENT'S STILL DUE.", 640, 430, 64, GOLD);
        ui.textC("You can close this tab now. Rob will find you.", 640, 360, 20, Color.WHITE);
        if (btn("ACTUALLY, ONE MORE GAME", 470, 250, 340, 60, MONEY, true)) screen = 0;
    }

    void playSetup() {
        for (int k = 0; k < 16; k++)       // wings drifting down behind everything
            wing((k * 97 + 40) % 1280, 760 - (time * 34 * (1 + k % 3 * 0.35f) + k * 173) % 840, time * 40 * (k % 2 == 0 ? 1 : -1) + k * 50, 0.22f);
        for (int i = 0; i < 8; i++) ui.rect(i * 160, 0, 160, 14, GC[i]);
        ui.textC("RENT'S DUE", 643, 606, 84, EDGE); ui.textC("RENT'S DUE", 640, 611, 84, GOLD);
        ui.textC("Who's playing?", 640, 526, 26, Color.WHITE);
        ui.textC("Type your name. Sam, Jack, Rob and Jules get their own face.", 640, 486, 13, SOFT);
        ui.rrect(490, 410, 300, 54, 10, PAPER);
        ui.textC(name + ((int) (time * 2) % 2 == 0 ? "|" : " "), 640, 437, 25, INK);
        if (btn("PLAY VS BOTS", 400, 316, 230, 66, MONEY, true)) { retries = 0; play(false); }
        if (btn("PLAY ONLINE", 650, 316, 230, 66, PC[0], true)) { retries = 0; play(true); }
        ui.textC("Vs bots runs right here in the page. Online joins the table on this page's server.", 640, 288, 13, SOFT);
        if (btn("BACK", 565, 214, 150, 48, SLATE, true)) screen = 0;
        if (err != null) ui.textC(err, 640, 186, 15, RED);
        for (int i = 0; i < 6; i++) {
            float x = 340 + i * 120, y = 96 + Math.abs(MathUtils.sin(time * 3 + i * 0.9f)) * 18;
            token(x, y, 28, i, false);
            ui.textC(Game.CH_NAME[i], x, 46, 12, SOFT);
        }
    }

    void settings() {
        ui.rect(0, 0, Ui.W, Ui.H, DIM);
        ui.rrect(386, 136, 508, 448, 20, GOLD);
        ui.rrect(390, 140, 500, 440, 18, PANEL);
        ui.textC("SETTINGS", 640, 544, 34, GOLD);
        for (int r = 0; r < 2; r++) {       // sound, then music: on/off and a volume
            float y = 452 - r * 70;
            boolean on = r == 0 ? soundOn : musicOn;
            int vol = r == 0 ? sVol : mVol;
            if (btn((r == 0 ? "SOUND: " : "MUSIC: ") + (on ? "ON" : "OFF"), 420, y, 210, 52, on ? col(0x2D7DD2) : SLATE, true)) { if (r == 0) soundOn = !soundOn; else musicOn = !musicOn; }
            if (btn("-", 646, y, 52, 52, SLATE, vol > 0)) vol--;
            if (btn("+", 808, y, 52, 52, SLATE, vol < 10)) vol++;
            for (int k = 0; k < 10; k++) ui.rect(708 + k * 9.4f, y + 14, 6, 24, k < vol ? GOLD : ROW);
            if (r == 0) sVol = vol; else mVol = vol;
        }
        if (btn(shakeOn ? "SCREEN SHAKE: ON" : "SCREEN SHAKE: OFF", 420, 312, 440, 52, shakeOn ? col(0x2D7DD2) : SLATE, true)) shakeOn = !shakeOn;
        if (net.canSpeak()) { if (btn(speakOn ? "READ CARDS OUT LOUD: ON" : "READ CARDS OUT LOUD: OFF", 420, 242, 440, 52, speakOn ? col(0x2D7DD2) : SLATE, true)) speakOn = !speakOn; }
        else if (btn("HOW TO PLAY", 420, 242, 440, 52, SLATE, true)) tutPage = 0;
        if (btn("DONE", 540, 164, 200, 52, MONEY, true)) { settingsOpen = false; save(); }
        tune();
    }

    boolean ownsHood(int p, int g) {
        for (int i = 0; i < Game.N; i++) if (Game.BOARD[i].group == g && owner[i] != p) return false;
        return true;
    }
    /** Click one of your own rentals, any time: call a contractor or sell it. */
    void manage() {
        if (manageSq < 0 || !phase.equals("game") || you < 0 || owner[manageSq] != you || kind == Game.TRADE || kind == Game.AUCTION) { manageSq = -1; return; }
        int sq = manageSq, lv = level[sq];
        Game.Sq q = Game.BOARD[sq];
        boolean free = !busy() && !sent;
        int cost = Game.contractor(sq, ownsHood(you, Game.CENTERVILLE), town >= 0 && Game.TOWNC[town].key.equals("blackfriday"));
        int sell = (q.price + lv * q.price / 2) / 2;
        float w = 440, h = 292, x = CX - w / 2, y = CY - 180;
        ui.rrect(x - 3, y - 3, w + 6, h + 6, 16, GC[q.group]);
        ui.rrect(x, y, w, h, 14, PANEL);
        ui.text(q.name, x + 20, y + h - 16, 18, GOLD, Align.left, w - 90);
        ui.text(Game.HOOD[q.group] + "  -  " + (lv == 0 ? "as-is" : lv == 1 ? "fresh paint" : "granite countertops")
                + (env[sq] > 0 ? "  -  $" + env[sq] * Game.envUnit(sq, lv, sCh[you] == Game.ROB) + " in the envelope" : ""), x + 20, y + h - 70, 12.5f, SOFT, Align.left, w - 40);
        if (btn("X", x + w - 52, y + h - 50, 38, 36, SLATE, true)) { manageSq = -1; return; }
        if (btn(lv >= 2 ? "FULLY FIXED UP" : "CALL A CONTRACTOR: " + (lv == 0 ? "PAINT " : "GRANITE ") + money(cost), x + 20, y + 84, w - 40, 48, col(0xB8860B), free && lv < 2 && cash[you] >= cost)) send("side fix " + sq);
        int half = Game.r5pub(env[sq] * Game.envUnit(sq, lv, sCh[you] == Game.ROB) / 2.0);
        if (btn(env[sq] > 0 ? "CALL THE TENANT: THEY VENMO HALF, " + money(half) : "NOTHING IN THE ENVELOPE", x + 20, y + 144, w - 40, 48, col(0x2D7DD2), free && env[sq] > 0)) send("side call " + sq);
        if (btn("SELL TO THE BANK FOR " + money(sell), x + 20, y + 24, w - 40, 48, col(0xA33A3A), free)) { send("side sell " + sq); manageSq = -1; }
    }

    void lobby() {
        ui.textC("RENT'S DUE", 640, 672, 48, GOLD);
        ui.textC(online ? "Lobby. Send everyone this page's address." : "Lobby. You against the bots.", 640, 632, 15, SOFT);
        for (int i = 0; i < ns; i++) {
            float y = 536 - i * 70;
            ui.rrect(50, y, 700, 62, 12, i == you ? ROW_HOT : ROW);
            token(90, y + 31, 23, sCh[i], false);
            ui.text(sName[i] + (i == you ? "  (you)" : ""), 128, y + 52, 19, Color.WHITE, Align.left, 0);
            ui.text("as " + Game.CH_NAME[sCh[i]], 128, y + 25, 12.5f, PC[sCh[i]].cpy().lerp(Color.WHITE, 0.45f), Align.left, 0);
            ui.text(Game.CH_PERK[sCh[i]], 350, y + 48, 12, SOFT, Align.left, 388);
        }
        ui.rrect(770, 150, 460, 448, 16, ROW);
        ui.textC("TABLE RULES", 1000, 572, 20, GOLD);
        ui.textC(lead ? "Click one to change it." : "The lobby leader sets these.", 1000, 546, 12, SOFT);
        String[] key = {"months", "speed", "fast", "cash", "trading", "auctions", "bets", "clock"};
        String[] label = {"MONTHS: " + rounds, "SPEED: " + Session.SPEED_NAME[oSpeed], "BOT TURNS: " + (oFast ? "EXTRA FAST" : "SAME SPEED"),
                "STARTING CASH: " + money(oCash), "TRADING: " + (oTrading ? "ON" : "OFF"), "AUCTIONS: " + (oAuctions ? "ON" : "OFF"), "SIDE BETS: " + (oBets ? "ON" : "OFF"),
                "TURN CLOCK: " + (oClock == 0 ? "OFF" : oClock + " SECONDS")};
        for (int k = 0; k < (oOnline ? 8 : 7); k++) if (btn(label[k], 790, 492 - k * 46, 420, 40, col(0x345A66), true) && lead) send("opt " + key[k]);
        if (btn("QUIT", 50, 40, 110, 52, col(0x7A2E2E), true)) { leave(null); return; }
        if (btn("HOW TO PLAY", 1040, 40, 190, 52, col(0x2D7DD2), true)) tutPage = 0;
        if (you >= 0 && btn("CHANGE CHARACTER", 172, 40, 262, 52, col(0x2D7DD2), true)) send("char");
        if (lead) {
            if (btn("+ BOT", 446, 40, 96, 52, SLATE, ns < 6)) send("bot+");
            if (btn("- BOT", 552, 40, 96, 52, SLATE, true)) send("bot-");
            if (btn("START", 770, 40, 250, 52, PC[0], true)) send("start");
        } else ui.text(you < 0 ? "Table's full. You're watching." : "Waiting for the lobby leader to start.", 450, 74, 16, SOFT, Align.left, 0);
    }

    void outline(float x, float y, float w, float h, float th, Color c) {
        ui.rect(x, y, w, th, c); ui.rect(x, y + h - th, w, th, c); ui.rect(x, y, th, h, c); ui.rect(x + w - th, y, th, h, c);
    }
    static boolean has(int[] a, int v) { for (int x : a) if (x == v) return true; return false; }

    void board() {
        ui.rrect(BX - 6, BY - 6, BS + 12, BS + 12, 12, EDGE);
        ui.rect(BX + C, BY + C, BS - 2 * C, BS - 2 * C, FELT);
        ui.rrect(BX + C + 14, BY + C + 14, BS - 2 * C - 28, BS - 2 * C - 28, 22, FELT2);
        boolean offerOn = phase.equals("game") && kind == Game.TRADE && !busy();
        hoverG = -1;
        for (int i = 0; i < Game.N; i++) { sq(i); if (Game.BOARD[i].kind == Game.PROP && ui.over(R[0], R[1], R[2], R[3])) hoverG = Game.BOARD[i].group; }
        for (int i = 0; i < Game.N; i++) {
            sq(i);
            float x = R[0] + 1, y = R[1] + 1, w = R[2] - 2, h = R[3] - 2;
            Game.Sq q = Game.BOARD[i];
            ui.rect(x, y, w, h, KIND[q.kind]);
            if (q.kind != Game.PROP) {
                boolean corner = i % 10 == 0;
                if (!SYMBOL[q.kind].isEmpty()) ui.textC(SYMBOL[q.kind], x + w / 2, y + h / 2, 52, ui.a(Color.WHITE, 0.5f));
                if (q.kind == Game.COFFEE) {
                    ui.textBox(q.tag, x + 4, y + h * 0.42f, w - 8, h * 0.55f, 13, INK);
                    ui.textC(money(pot), x + w / 2, y + 24, 23, col(0x6B3E12));
                } else if (q.kind == Game.START) {
                    ui.textBox(q.tag, x + 4, y + h * 0.42f, w - 8, h * 0.55f, 13, INK);
                    ui.textC("+$150", x + w / 2, y + 24, 21, col(0x1E7B3C));
                } else if (q.kind == Game.FRICKERS) {
                    ui.textBox(q.tag, x + 4, y + h * 0.56f, w - 8, h * 0.4f, 15, INK);
                    ui.textC("WING JAIL", x + w / 2, y + 12, 11, col(0x7A3B00));
                } else if (q.kind == Game.WINGNIGHT) {
                    ui.textBox(q.tag, x + 4, y + h * 0.42f, w - 8, h * 0.55f, 15, INK);
                    ui.textC("go to jail", x + w / 2, y + 22, 12, Color.WHITE);
                } else ui.textBox(q.tag, x + 3, y, w - 6, h, corner ? 16 : 10.5f, INK);
                continue;
            }
            int side = i / 10, o = owner[i];
            float ax = x, ay = y, aw = w, ah = h, band = 10;
            if (side == 0) { ui.rect(x + w - band, y, band, h, GC[q.group]); aw -= band; }
            else if (side == 1) { ui.rect(x, y, w, band, GC[q.group]); ay += band; ah -= band; }
            else if (side == 2) { ui.rect(x, y, band, h, GC[q.group]); ax += band; aw -= band; }
            else { ui.rect(x, y + h - band, w, band, GC[q.group]); ah -= band; }
            if (o >= 0) { ui.rect(ax, ay, aw, ah, ui.a(PC[sCh[o]], 0.3f)); outline(ax, ay, aw, ah, 3, PC[sCh[o]]); }
            ui.textBox(q.tag, ax + 3, ay + ah * 0.4f, aw - 6, ah * 0.6f, 10.5f, INK);
            float bx = ax + aw / 2, by = ay + ah * 0.21f;
            if (o < 0) ui.textC("$" + q.price, bx, by, 12, col(0x6A6257));
            else {
                for (int k = 0; k < level[i]; k++) ui.circle(ax + 9 + k * 10, ay + ah * 0.4f + 1, 4, col(0xD99A00));
                if (env[i] > 0) {
                    String s = "$" + env[i] * Game.envUnit(i, level[i], sCh[o] == Game.ROB);
                    ui.rrect(bx - 20, by - 8, 40, 16, 5, MONEY);
                    ui.textC(s, bx, by, 11, Color.WHITE);
                }
            }
            boolean picked = trading ? giveSel.contains(i) || getSel.contains(i) : offerOn && (has(tGive, i) || has(tGet, i));
            if (hoverG == q.group && !trading) outline(x, y, w, h, 2, ui.a(Color.WHITE, 0.9f));     // the rest of the neighbourhood you're pointing at
            if (picked) outline(x - 1, y - 1, w + 2, h + 2, 4, Color.WHITE);
            else if (phase.equals("game") && psq == i && !busy()) outline(x - 1, y - 1, w + 2, h + 2, 4, ui.a(GOLD, 0.55f + 0.45f * MathUtils.sin(time * 6)));
            if (!trading && ui.click && phase.equals("game") && you >= 0 && o == you && ui.over(R[0], R[1], R[2], R[3])) { manageSq = i; snd("click"); }   // manage your own rental
            if (trading && ui.click && ui.over(R[0], R[1], R[2], R[3]) && o >= 0) {
                snd("click");
                if (o == you) { if (!giveSel.removeValue(i)) { if (giveSel.size < 4) giveSel.add(i); } }
                else {
                    if (tP != o) { tP = o; getSel.clear(); tCashSel = Math.min(tCashSel, cash[you]); }
                    if (!getSel.removeValue(i)) { if (getSel.size < 4) getSel.add(i); }
                }
            }
        }
    }

    void center() {
        ui.textC("RENT'S DUE", CX + 3, BY + BS - C - 58, 58, EDGE);
        ui.textC("RENT'S DUE", CX, BY + BS - C - 54, 58, GOLD);
        ui.textC("MONTH " + round + " OF " + max, CX, BY + BS - C - 104, 18, Color.WHITE);
        if (btn("MENU", BX + C + 22, BY + BS - C - 52, 78, 30, SLATE, true)) menuOpen = true;
        if (btn("QUIT", BX + BS - C - 100, BY + BS - C - 52, 78, 30, col(0x7A2E2E), true)) menuOpen = true;      // asks first, via the menu
        if (phase.equals("game")) {
            String s = sName[turn] + "'s turn";
            float w = ui.textW(s, 14);
            token(CX - w / 2 - 14, CY + 127, 10, sCh[turn], false);
            ui.text(s, CX - w / 2 + 4, CY + 133, 14, GOLD, Align.left, 0);
        }
        for (int g = 0; g < 8; g++) {       // who has what in each neighbourhood
            float x = CX - 237 + (g % 4) * 120, y = CY + 82 - (g / 4) * 28;
            ui.rrect(x, y, 114, 23, 6, ui.a(EDGE, hoverG == g ? 0.9f : 0.45f));
            ui.rrect(x + 4, y + 4, 8, 15, 3, GC[g]);
            ui.text(Game.HOOD[g], x + 16, y + 16, 9.5f, Color.WHITE, Align.left, 0);
            int k = 0;
            for (int i = 0; i < Game.N; i++) if (Game.BOARD[i].group == g) {
                ui.circle(x + 88 + k * 9, y + 11.5f, 4, owner[i] < 0 ? ui.a(Color.WHITE, 0.22f) : PC[sCh[owner[i]]]);
                k++;
            }
        }
        if (town >= 0) {
            Game.Card c = Game.TOWNC[town];
            ui.rrect(CX - 215, BY + C + 30, 430, 78, 12, ui.a(EDGE, 0.55f));
            ui.text("TOWN NEWS: " + c.title, CX - 203, BY + C + 98, 14, GOLD, Align.left, 0);
            ui.text(c.text, CX - 203, BY + C + 76, 12, Color.WHITE, Align.left, 406);
        }
        deck(CX - 175, CY - 40, KIND[Game.ROBSQ], "ROB HAPPENS", "?");
        deck(CX + 175, CY - 40, KIND[Game.TENANT], "TENANT TROUBLE", "!");
        if (mine() && kind == Game.DRAW) {      // your card: click the deck yourself
            float dx = pdeck.equals("R") ? CX - 175 : CX + 175, grow = 5 + 4 * MathUtils.sin(time * 7);
            outline(dx - 42 - grow, CY - 96 - grow, 84 + grow * 2, 112 + grow * 2, 4, GOLD);
            ui.textC("DRAW", dx, CY - 112, 15, GOLD);
            if (ui.click && ui.over(dx - 48, CY - 102, 96, 124)) { snd("click"); send("act draw"); }
        }
    }
    void deck(float cx, float cy, Color c, String label, String sym) {
        for (int k = 2; k >= 0; k--) ui.rrect(cx - 42 + k * 2, cy - 56 - k * 3, 84, 112, 8, k == 0 ? c : EDGE);
        outline(cx - 36, cy - 50, 72, 100, 2, ui.a(Color.WHITE, 0.6f));
        ui.textC(sym, cx, cy + 14, 48, Color.WHITE);
        ui.textBox(label, cx - 36, cy - 46, 72, 34, 10, INK);
    }

    /** A playing piece: the character's face in a ring of their colour. */
    void token(float x, float y, float r, int ch, boolean hot) {
        if (hot) ui.circle(x, y, r + 8 + 2.5f * MathUtils.sin(time * 7), ui.a(GOLD, 0.85f));
        ui.circle(x + 1.5f, y - 3, r + 4, ui.a(Color.BLACK, 0.35f));
        ui.circle(x, y, r + 4, Color.WHITE);
        ui.circle(x, y, r + 2.5f, PC[ch]);
        ui.img(face[ch], x - r, y - r, r * 2, r * 2, 1);
    }
    void tokens() {
        for (int pass = 0; pass < 2; pass++) for (int p = 0; p < cash.length; p++) {
            if ((p == mvP) != (pass == 1)) continue;
            tok(p);
            float x = T[0], y = T[1] + (p == turn && p != mvP ? Math.abs(MathUtils.sin(time * 4)) * 4 : 0);
            token(x, y, 13, sCh[p], p == turn && phase.equals("game"));
            if (p == top()) crown(x, y + 19, 7);
            if (jail[p] > 0 && p != mvP) for (int k = -1; k <= 1; k++) ui.rrect(x + k * 9 - 2, y - 19, 4, 38, 2, CELERY);   // celery bars
        }
    }

    static final int[][] PIPS = {{}, {4}, {0, 8}, {0, 4, 8}, {0, 2, 6, 8}, {0, 2, 4, 6, 8}, {0, 2, 3, 5, 6, 8}};
    void die(float cx, float cy, float size, int f, float deg, Color body) {
        ui.rot(cx + 3, cy - 5, size, size, deg, ui.a(Color.BLACK, 0.3f));
        ui.rot(cx, cy, size, size, deg, body);
        float cs = MathUtils.cosDeg(deg), sn = MathUtils.sinDeg(deg), g = size * 0.27f;
        for (int k : PIPS[f]) {
            float dx = (k % 3 - 1) * g, dy = (1 - k / 3) * g;
            ui.circle(cx + dx * cs - dy * sn, cy + dx * sn + dy * cs, size * 0.09f, body == Color.WHITE ? INK : Color.WHITE);
        }
    }
    void dice() {
        if (!diceOn) return;
        float roll = dB > 0 ? 0.95f : 0.65f, y = CY - 40;
        boolean rolling = diceT < roll;
        int n = dB > 0 ? 2 : 1;
        for (int i = 0; i < n; i++) {
            float x = CX + (n == 2 ? (i == 0 ? -40 : 40) : 0), deg = 0, yy = y, size = 58;
            int f = i == 0 ? dA : dB;
            if (rolling) {
                float k = 1 - diceT / roll;
                f = 1 + (int) (Math.abs(MathUtils.sin((int) (diceT / 0.07f) * 12.9898f + i * 78.233f) * 43758.5453f) % 6);
                deg = diceT * (i == 0 ? 760 : -640);
                yy += Math.abs(MathUtils.sin(diceT * 11 + i)) * 70 * k;
                x += MathUtils.sin(diceT * 7 + i * 2) * 26 * k;
            } else size *= 1 + 0.25f * Math.max(0, 1 - (diceT - roll) / 0.18f);
            die(x, yy, size, f, deg, i == 0 ? Color.WHITE : PC[0]);
        }
        if (!rolling && n == 2) ui.textC(dA == dB ? "DOUBLES! " + (dA + dB) : String.valueOf(dA + dB), CX, y - 52, 20, GOLD);
    }

    void effects() { for (Fx f : fx) if (f.type != CONFETTI) effect(f); }
    void effect(Fx f) {
        if (f.t < 0) return;
        float u = f.t / f.dur;
        switch (f.type) {
            case COIN:
                for (int k = 0; k < 3; k++) {
                    float v = MathUtils.clamp((f.t - k * 0.07f) / (f.dur - 0.14f), 0, 1);
                    if (v <= 0 || v >= 1) continue;
                    float x = f.x0 + (f.x1 - f.x0) * v, y = f.y0 + (f.y1 - f.y0) * v + MathUtils.sin(v * MathUtils.PI) * 46;
                    ui.circle(x, y, 10, EDGE); ui.circle(x, y, 8.5f, f.c); ui.textC("$", x, y, 12, EDGE);
                }
                break;
            case FLOAT: {
                float a = u < 0.7f ? 1 : (1 - u) / 0.3f, y = f.y0 + 34 * u;
                ui.textC(f.s, f.x0 + 1.5f, y - 1.5f, 21, ui.a(EDGE, a));
                ui.textC(f.s, f.x0, y, 21, ui.a(f.c, a));
                break;
            }
            case RING: ui.circle(f.x0, f.y0, 16 + 70 * u, ui.a(f.c, 0.5f * (1 - u))); break;
            case STAMP: {
                float a = u < 0.75f ? 1 : (1 - u) / 0.25f, sz = 17 * (f.t < 0.16f ? 2.4f - f.t / 0.16f * 1.4f : 1), w = ui.textW(f.s, sz) + 18;
                ui.rrect(f.x0 - w / 2, f.y0 - sz * 0.8f, w, sz * 1.6f, 6, ui.a(f.c, a));
                outline(f.x0 - w / 2 + 3, f.y0 - sz * 0.8f + 3, w - 6, sz * 1.6f - 6, 1.5f, ui.a(Color.WHITE, a * 0.8f));
                ui.textC(f.s, f.x0, f.y0, sz, ui.a(Color.WHITE, a));
                break;
            }
            case BUBBLE: {
                float a = Math.min(1, Math.min(f.t / 0.12f, (f.dur - f.t) / 0.3f)), w = ui.textW(f.s, 13) + 22;
                float x = MathUtils.clamp(f.x0 - w / 2, BX + 4, BX + BS - w - 4), y = f.y0 + 6 * Math.min(1, f.t / 0.12f);
                ui.circle(f.x0, y - 2, 6, ui.a(Color.WHITE, a));
                ui.rrect(x - 2, y - 2, w + 4, 30, 10, ui.a(f.c, a));
                ui.rrect(x, y, w, 26, 8, ui.a(Color.WHITE, a));
                ui.textC(f.s, x + w / 2, y + 13, 13, ui.a(INK, a));
                break;
            }
            case CONFETTI: {
                float x = f.x0 + f.x1 * f.t + MathUtils.sin(f.t * 5 + f.x0) * 20, y = f.y0 + f.y1 * f.t;
                ui.rot(x, y, 12, 7, f.t * 400 + f.x0, f.c);
                break;
            }
            case DRUM: {     // a falling chicken wing
                wing(f.x0 + f.x1 * f.t, f.y0 + f.y1 * f.t, f.t * 300 + f.x0, 1);
                break;
            }
            default: break;
        }
    }

    void wing(float x, float y, float deg, float alpha) {
        float cs = MathUtils.cosDeg(deg), sn = MathUtils.sinDeg(deg);
        ui.rot(x + cs * 14, y + sn * 14, 26, 7, deg, ui.a(BONE, alpha));
        ui.circle(x + cs * 28, y + sn * 28, 6, ui.a(BONE, alpha));
        ui.circle(x, y, 15, ui.a(EDGE, alpha)); ui.circle(x, y, 13, ui.a(WING, alpha));
    }
    /** A little crown for whoever's worth the most. */
    void crown(float x, float y, float s) {
        ui.rect(x - s, y - s * 0.4f, s * 2, s * 0.8f, EDGE);
        ui.rect(x - s + 1, y - s * 0.4f + 1, s * 2 - 2, s * 0.8f - 2, GOLD);
        for (int k = -1; k <= 1; k++) { ui.circle(x + k * s * 0.8f, y + s * 0.6f, s * 0.42f, EDGE); ui.circle(x + k * s * 0.8f, y + s * 0.6f, s * 0.42f - 1, GOLD); }
    }
    /** Who's worth the most right now, once anybody has pulled ahead. -1 at the very start. */
    int top() {
        int b = 0; boolean tie = true;
        for (int i = 1; i < worth.length; i++) { if (worth[i] != worth[b]) tie = false; if (worth[i] > worth[b]) b = i; }
        return tie || round < 2 ? -1 : b;
    }

    /** A big line of text across the board: new month, last month, game start. */
    void banner() {
        if (bannerT >= bannerD) return;
        float in = Math.min(1, bannerT / 0.2f), a = Math.min(1, (bannerD - bannerT) / 0.25f), e = 1 - (1 - in) * (1 - in);
        ui.rect(BX, CY - 46, BS, 92, ui.a(EDGE, 0.82f * a));
        ui.rect(BX, CY + 42, BS, 4, ui.a(GOLD, a)); ui.rect(BX, CY - 46, BS, 4, ui.a(GOLD, a));
        ui.textC(bannerS, CX - (1 - e) * 240, CY, bannerS.length() > 14 ? 40 : 54, ui.a(GOLD, a));
    }
    /** Somebody just finished a neighbourhood. */
    void hoodShow() {
        if (hoodT >= 2.8f) return;
        float in = Math.min(1, hoodT / 0.2f), a = Math.min(1, (2.8f - hoodT) / 0.3f), h = 170 * (0.4f + 0.6f * in);
        ui.rect(BX, CY - h / 2, BS, h, ui.a(EDGE, 0.9f * a));
        ui.rect(BX, CY + h / 2 - 12, BS, 12, ui.a(GC[hoodG], a)); ui.rect(BX, CY - h / 2, BS, 12, ui.a(GC[hoodG], a));
        if (in < 1) return;
        token(BX + 70, CY, 34 + 2 * MathUtils.sin(time * 9), sCh[hoodP], false);
        crown(BX + 70, CY + 46, 12);
        ui.text("ALL OF " + Game.HOOD[hoodG].toUpperCase() + "!", BX + 124, CY + 56, 34, ui.a(GOLD, a), Align.left, 0);
        ui.text(sName[hoodP] + " owns the whole neighborhood. Visitors pay " + (hoodG == Game.VANDALIA ? "triple" : "double") + ".", BX + 124, CY + 14, 14, ui.a(Color.WHITE, a), Align.left, 0);
        ui.text(Game.HOOD_PERK[hoodG], BX + 124, CY - 12, 13, ui.a(SOFT, a), Align.left, BS - 150);
    }

    /** A picture in a white frame. */
    void framed(Texture t, float cx, float cy, float w, float a) {
        float h = w * 0.625f;
        ui.rrect(cx - w / 2 - 8, cy - h / 2 - 8, w + 16, h + 16, 10, ui.a(Color.WHITE, a));
        ui.img(t, cx - w / 2, cy - h / 2, w, h, a);
    }

    void card() {
        if (cardT >= cardD && !cardHold) return;
        Game.Card c = cardDeck == 'R' ? Game.ROBC[cardId] : cardDeck == 'T' ? Game.TENC[cardId] : Game.TOWNC[cardId];
        Color head = cardDeck == 'R' ? col(0xE07B1A) : cardDeck == 'T' ? col(0x7A5BD0) : col(0x1F4E79);
        Texture art = pic(c.key.equals("wings") ? "jail_" + sCh[cardWho] : c.art);
        float in = Math.min(1, cardT / 0.45f), out = cardHold ? 0 : Math.max(0, (cardT - (cardD - 0.3f)) / 0.3f), a = 1 - out;
        float fromX = cardDeck == 'R' ? CX - 175 : cardDeck == 'T' ? CX + 175 : CX, fromY = cardDeck == 'W' ? CY + 200 : CY - 40;
        float e = 1 - (1 - in) * (1 - in), cx = fromX + (CX - fromX) * e, cy = fromY + (CY + 4 - fromY) * e + out * 40;
        float sc = 0.3f + 0.7f * e, w = 300 * sc, h = (art != null ? 450 : 380) * sc, flip = Math.abs(MathUtils.cos(in * MathUtils.PI));
        boolean front = in > 0.5f;
        ui.rect(0, 0, Ui.W, Ui.H, ui.a(Color.BLACK, 0.25f * e * a));
        ui.rrect(cx - w * flip / 2 + 5, cy - h / 2 - 8, w * flip, h, 14, ui.a(Color.BLACK, 0.35f * a));
        ui.rrect(cx - w * flip / 2, cy - h / 2, w * flip, h, 14, ui.a(front ? PAPER : head, a));
        if (!front || in < 1) return;
        float x = cx - w / 2, top = cy + h / 2;
        ui.rrect(x, top - 50, w, 50, 14, ui.a(head, a));
        ui.rect(x, top - 50, w, 20, ui.a(head, a));
        ui.textC(cardDeck == 'R' ? "ROB HAPPENS" : cardDeck == 'T' ? "TENANT TROUBLE" : "TOWN NEWS", cx, top - 25, 19, ui.a(Color.WHITE, a));
        float y = top - 64;
        if (art != null) { ui.rect(cx - 131, y - 166, 262, 166, ui.a(INK, a)); ui.img(art, cx - 128, y - 163, 256, 160, a); y -= 180; }
        y -= ui.text(c.title, x + 16, y, 21, ui.a(INK, a), Align.center, w - 32) + 16;
        ui.rect(x + 40, y + 6, w - 80, 2, ui.a(head, a * 0.6f));
        ui.text(c.text, x + 20, y - 8, 15, ui.a(col(0x3A3A40), a), Align.center, w - 40);
        String foot = cardDeck == 'W' ? "Hits everybody." : c.keep ? "KEEPER. Play it before a roll." : sName[cardWho] + " drew this.";
        ui.textC(foot, cx, cy - h / 2 + 20, 12.5f, ui.a(c.keep ? head : SLATE, a));
    }

    /** A funny picture that pops up over the board for a moment. */
    void popup() {
        if (popT >= popD) return;
        Texture t = pic(popKey);
        if (t == null) return;
        float in = Math.min(1, popT / 0.18f), a = Math.min(1, (popD - popT) / 0.25f), s = 0.6f + 0.4f * in + 0.08f * MathUtils.sin(in * MathUtils.PI);
        framed(t, CX, CY + 20, 400 * s, a);
    }

    /** Somebody just got sent to Wing Jail. */
    void jailShow() {
        if (jailT < 0.8f || jailT > 3.3f) return;
        Texture t = pic("jail_" + jailCh);
        float u = jailT - 0.8f, in = Math.min(1, u / 0.15f), a = Math.min(1, (3.3f - jailT) / 0.3f), drop = (1 - in) * 300;
        ui.rect(0, 0, Ui.W, Ui.H, ui.a(Color.BLACK, 0.3f * a));
        if (t != null) framed(t, CX, CY - 10 + drop, 440, a);
        float sz = 60 * (1 + 0.6f * (1 - in)) + 3 * MathUtils.sin(time * 14);
        ui.textC("WING JAIL!", CX + 4, CY + 186, sz, ui.a(EDGE, a));
        ui.textC("WING JAIL!", CX, CY + 190, sz, ui.a(GOLD, a));
    }

    /** A rental on the block. */
    void auction() {
        if (!phase.equals("game") || kind != Game.AUCTION || busy() || aSq < 0) return;
        Game.Sq q = Game.BOARD[aSq];
        float w = 440, h = 230, x = CX - w / 2, y = CY - 150;
        ui.rrect(x - 3, y - 3, w + 6, h + 6, 16, GC[q.group]);
        ui.rrect(x, y, w, h, 14, PANEL);
        ui.textC("AUCTION", CX, y + h - 26, 24, GOLD);
        ui.text(q.name, x + 20, y + h - 52, 17, Color.WHITE, Align.center, w - 40);
        ui.textC(Game.HOOD[q.group] + "  -  normally $" + q.price, CX, y + h - 108, 13, SOFT);
        if (aHigh >= 0) {
            token(CX - 110, y + 74, 18, sCh[aHigh], false);
            ui.text(sName[aHigh] + " has it at " + money(aBid), CX - 82, y + 82, 17, CASH, Align.left, 0);
        } else ui.textC("Bidding opens at " + money(aBid), CX, y + 76, 17, CASH);
        ui.textC(you == who ? "You're up. " + money(aNext) + " to stay in." : sName[who] + " is up. " + money(aNext) + " to stay in.", CX, y + 28, 14, GOLD);
        sq(aSq);
        outline(R[0], R[1], R[2], R[3], 4, ui.a(GOLD, 0.55f + 0.45f * MathUtils.sin(time * 6)));
    }

    /** The trade on the table. */
    void offer() {
        if (!phase.equals("game") || kind != Game.TRADE || busy() || tTo < 0) return;
        float w = 440, h = 250, x = CX - w / 2, y = CY - 150;
        ui.rrect(x - 3, y - 3, w + 6, h + 6, 16, GOLD);
        ui.rrect(x, y, w, h, 14, PANEL);
        ui.textC("TRADE OFFER", CX, y + h - 26, 24, GOLD);
        token(x + 44, y + h - 76, 20, sCh[turn], false);
        token(x + w - 44, y + h - 76, 20, sCh[tTo], false);
        ui.textC(sName[turn] + "  >  " + sName[tTo], CX, y + h - 76, 17, Color.WHITE);
        ui.text(sName[turn] + " gives: " + list(tGive) + (tCash > 0 ? " + " + money(tCash) : ""), x + 20, y + h - 112, 14, CASH, Align.left, w - 40);
        ui.text(sName[tTo] + " gives: " + list(tGet) + (tCash < 0 ? " + " + money(-tCash) : ""), x + 20, y + h - 166, 14, col(0xF2B5A0), Align.left, w - 40);
        ui.textC(you == tTo ? "Your call." : "Waiting on " + sName[tTo] + "...", CX, y + 22, 14, SOFT);
    }

    void panel() {
        int n = cash.length;
        for (int i = 0; i < n; i++) {
            float y = 706 - i * ROWH - 51;
            boolean hot = i == turn && phase.equals("game");
            ui.rrect(PX, y, PW, 51, 10, hot ? ROW_HOT : ROW);
            if (hot) ui.rrect(PX, y, 6, 51, 3, GOLD);
            if (flash[i] != 0) ui.rrect(PX, y, PW, 51, 10, ui.a(flash[i] > 0 ? MONEY : RED, Math.abs(flash[i]) * 0.4f));
            if (trading && i == tP) outline(PX, y, PW, 51, 2, Color.WHITE);
            token(PX + 34, y + 25.5f, 18, sCh[i], false);
            if (i == top()) crown(PX + 34, y + 47, 6);
            ui.text(sName[i] + (i == you ? "  (you)" : sAway[i] ? "  (bot covering)" : ""), PX + 64, y + 44, 17, Color.WHITE, Align.left, 0);
            int cnt = 0;
            for (int k = 0; k < Game.N; k++) if (owner[k] == i) cnt++;
            String sub = Game.CH_NAME[sCh[i]] + "  -  " + cnt + (cnt == 1 ? " rental" : " rentals");
            if (jail[i] > 0) sub += "  -  in Wing Jail";
            if (skip[i] > 0) sub += "  -  skips next turn";
            if (iou[i] > 0) sub += "  -  IOU x" + iou[i];
            ui.text(sub, PX + 64, y + 20, 11.5f, jail[i] > 0 || skip[i] > 0 || iou[i] > 0 ? col(0xF2B5A0) : SOFT, Align.left, 0);
            for (int k = 0; k < hand[i].length; k++) {
                ui.rrect(PX + 356 + k * 17, y + 24, 13, 19, 3, KIND[Game.ROBSQ]);
                ui.textC("?", PX + 362.5f + k * 17, y + 33.5f, 11, Color.WHITE);
            }
            ui.text(money(Math.round(showCash[i])), PX + PW - 152, y + 45, 22, CASH, Align.right, 140);
            ui.text("worth " + money(worth[i]), PX + PW - 152, y + 19, 11.5f, SOFT, Align.right, 140);
        }
        // log, newest at the bottom
        float top = 706 - n * ROWH - 6, room = top - 222, used = 0;
        if (runOpen && phase.equals("game") && you >= 0) {      // the side game takes the log's spot
            run.draw(PX, 224, PW, top - 224, time, mine() ? "YOUR TURN" : null);
            return;
        }
        int first = log.length;
        while (first > 0) {
            float h = ui.textH(log[first - 1], 12.5f, PW - 16) + 8;
            if (used + h > room) break;
            used += h; first--;
        }
        float y = top;
        for (int i = first; i < log.length; i++) y -= ui.text(log[i], PX + 8, y, 12.5f, i == log.length - 1 ? Color.WHITE : SOFT, Align.left, PW - 16) + 8;
    }

    void actions() {
        if (!phase.equals("game")) return;
        boolean my = mine(), idle = !busy() && !sent, space = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && my && !menuOpen && tutPage < 0;
        float half = PW / 2 - 6, x2 = PX + PW / 2 + 6;

        // the clock (online only)
        if (limit > 0 && idle) {
            ui.rect(PX, 214, PW, 4, ROW);
            ui.rect(PX, 214, PW * left / limit, 4, left < 10 ? RED : GOLD);
            ui.text((int) Math.ceil(left) + "s", PX + PW - 40, 210, 11, left < 10 ? RED : SOFT, Align.right, 40);
        }
        // taunts
        float tw = (PW - 5 * 6) / 6;
        if (you >= 0 && btn(runOpen ? "SHOW LOG" : "WING RUN", PX + 5 * (tw + 6), 124, tw, 24, WING, true)) runOpen = !runOpen;
        for (int k = 0; k < 5 && you >= 0; k++) if (btn(Game.TAUNTS[k], PX + k * (tw + 6), 124, tw, 24, col(0x345A66), true)) taunt(k);

        String msg = "";
        if (trading) {
            msg = tP < 0 ? "Building an offer. Click your own rentals to give them. Click another player's rentals to ask for them."
                    : "Offer to " + sName[tP] + ": you give " + list(giveSel.toArray()) + (tCashSel > 0 ? " + " + money(tCashSel) : "")
                    + ". You get " + list(getSel.toArray()) + (tCashSel < 0 ? " + " + money(-tCashSel) : "") + ".";
            ui.text(msg, PX + 4, 206, 13.5f, Color.WHITE, Align.left, PW - 60);
            if (btn("ASK FOR $50", PX, 78, 150, 36, col(0xC96A12), tP >= 0 && -(tCashSel - 50) <= cash[tP])) tCashSel -= 50;
            ui.textC(tCashSel == 0 ? "no cash" : tCashSel > 0 ? "you add " + money(tCashSel) : "you ask " + money(-tCashSel), PX + PW / 2, 96, 14, Color.WHITE);
            if (btn("ADD $50", PX + PW - 150, 78, 150, 36, MONEY, tCashSel + 50 <= cash[you])) tCashSel += 50;
            if (btn("SEND OFFER", PX, 16, half, 52, MONEY, my && tP >= 0 && giveSel.size + getSel.size > 0)) {
                StringBuilder a = new StringBuilder(), b = new StringBuilder();
                for (int i = 0; i < giveSel.size; i++) a.append(i > 0 ? "," : "").append(giveSel.get(i));
                for (int i = 0; i < getSel.size; i++) b.append(i > 0 ? "," : "").append(getSel.get(i));
                send("act trade " + tP + " " + tCashSel + " " + (a.length() == 0 ? "-" : a) + " " + (b.length() == 0 ? "-" : b));
                trading = false;
            }
            if (btn("CANCEL", x2, 16, half, 52, SLATE, true)) trading = false;
            return;
        }
        if (you < 0) msg = idle ? "You're watching. " + sName[who] + " is up." : "";
        else if (!idle) msg = "";
        else if (kind == Game.TRADE) msg = you == tTo ? sName[turn] + " wants to trade. Look at the board, then take it or leave it."
                : sName[tTo] + " is looking at " + (you == turn ? "your" : sName[turn] + "'s") + " offer...";
        else if (kind == Game.AUCTION) msg = you == who ? "Auction for " + Game.BOARD[aSq].name + ". Bid " + money(aNext) + " or drop out." : "Auction. " + sName[who] + " is deciding.";
        else if (you != turn && kind == Game.DRAW) msg = sName[turn] + " is reaching for the deck...";
        else if (you != turn && kind == Game.CARD) msg = sName[turn] + " drew a card and is reading it to the table.";
        else if (you != turn && kind == Game.CHOICE) msg = sName[turn] + " has a decision to make:\n" + cA + "   or   " + cB;
        else if (you != turn) msg = sName[turn] + " is thinking" + "...".substring(0, 1 + (int) (time * 2.5f) % 3) + "\nWhile you wait: " + (oBets ? "bet on the roll, or " : "") + "click one of your rentals to fix it up or sell it.";
        else if (kind == Game.ROLL) msg = hand[you].length > 0 ? "Your turn. Play a keeper or offer a trade first if you want, then roll." : "Your turn. Offer a trade first if you want, then roll.";
        else if (kind == Game.DRAW) msg = "Click the " + (pdeck.equals("R") ? "Rob Happens" : "Tenant Trouble") + " deck to draw your card.";
        else if (kind == Game.CARD) msg = "Read it to the table. Then do what it says.";
        else if (kind == Game.CHOICE) msg = cText;
        else if (kind == Game.JAIL) msg = "You're in Wing Jail. Roll doubles to eat your way out (" + jail[you] + (jail[you] == 1 ? " try" : " tries") + " left), or pay the $" + Game.TAB + " tab and roll normally.";
        else if (kind == Game.BUY) {
            Game.Sq q = Game.BOARD[psq];
            msg = "Buy " + q.name + " in " + Game.HOOD[q.group] + " for " + money(pcost) + "? Visitors would pay you $" + Game.rent(psq, 0, 1, false)
                    + ". Tenants leave $" + Game.envUnit(psq, 0, sCh[you] == Game.ROB) + " a month in the envelope.";
        } else msg = "Fix up " + Game.BOARD[psq].tag + " for " + money(pcost) + "? " + (level[psq] == 0 ? "Fresh paint." : "Granite countertops.")
                + " Rent and envelopes go up.";
        ui.text(msg, PX + 4, 206, 13.5f, Color.WHITE, Align.left, PW - 60);
        if (you < 0 || you >= cash.length) return;

        float w3 = (PW - 16) / 3;
        for (int k = 0; k < hand[you].length; k++) {
            Game.Card c = Game.ROBC[hand[you][k]];
            if (btn(c.title, PX + k * (w3 + 8), 78, w3, 36, col(0xC96A12), my && kind == Game.ROLL)) send("act play " + k);
        }
        if (kind == Game.TRADE && you == tTo) {
            if (btn("DEAL", PX, 16, half, 52, MONEY, my) || space) send("act yes");
            if (btn("NO DEAL", x2, 16, half, 52, SLATE, my)) send("act no");
        } else if (kind == Game.AUCTION) {
            if (you == who) {
                if (btn("BID " + money(aNext), PX, 16, half, 52, MONEY, my && cash[you] >= aNext) || space) send("act bid");
                if (btn("I'M OUT", x2, 16, half, 52, SLATE, my)) send("act fold");
            }
        } else if (kind == Game.DRAW && you == turn) {
            if (btn("DRAW A CARD", PX, 16, PW, 52, pdeck.equals("R") ? col(0xE07B1A) : col(0x7A5BD0), my) || space) send("act draw");
        } else if (kind == Game.CARD && you == turn) {
            if (btn("OK, DO WHAT IT SAYS", PX, 16, PW, 52, MONEY, my) || space) send("act ok");
        } else if (kind == Game.CHOICE && you == turn) {
            if (btn(cA, PX, 16, half, 52, MONEY, my)) send("act a");
            if (btn(cB, x2, 16, half, 52, col(0xC96A12), my)) send("act b");
        } else if (kind == Game.BUY && you == turn) {
            if (btn("BUY IT", PX, 16, half, 52, MONEY, my) || space) send("act buy");
            if (btn("PASS", x2, 16, half, 52, SLATE, my)) send("act pass");
        } else if (kind == Game.UP && you == turn) {
            if (btn("FIX IT UP", PX, 16, half, 52, col(0xB8860B), my) || space) send("act up");
            if (btn("NAH", x2, 16, half, 52, SLATE, my)) send("act skip");
        } else if (kind == Game.JAIL && you == turn) {
            if (btn("EAT (ROLL DOUBLES)", PX, 16, half, 52, WING, my) || space) send("act eat");
            if (btn("PAY THE $" + Game.TAB + " TAB", x2, 16, half, 52, SLATE, my && cash[you] >= Game.TAB)) send("act tab");
        } else if (you != turn && oBets && (kind == Game.ROLL || kind == Game.JAIL)) {
            boolean can = idle && bet[you] == 0 && cash[you] >= Game.BET;
            if (bet[you] != 0) ui.textC("You bet $" + Game.BET + " on " + (bet[you] > 0 ? "HIGH" : "LOW") + ". Waiting for the roll.", PX + PW / 2, 42, 15, GOLD);
            else {
                if (btn("BET $" + Game.BET + ": HIGH (8+)", PX, 16, half, 52, col(0x7A5BD0), can)) send("side bet hi");
                if (btn("BET $" + Game.BET + ": LOW (6-)", x2, 16, half, 52, col(0x7A5BD0), can)) send("side bet lo");
            }
        } else {
            boolean roll = my && kind == Game.ROLL;
            if (btn("ROLL", PX, 16, PW - 162, 52, PC[0], roll) || (space && roll)) send("act roll");
            if (btn(!oTrading ? "NO TRADES" : traded ? "TRADED" : "TRADE", PX + PW - 150, 16, 150, 52, col(0x2D7DD2), roll && !traded && oTrading)) startTrade();
        }
    }

    void startTrade() { trading = true; tP = -1; tCashSel = 0; giveSel.clear(); getSel.clear(); }

    void tip(String head, String body) {
        float w = 310, h = ui.textH(body, 12.5f, w - 24) + ui.textH(head, 14.5f, w - 24) + 34;
        float x = MathUtils.clamp(ui.mx + 16, 4, Ui.W - w - 4), y = MathUtils.clamp(ui.my - h - 10, 4, Ui.H - h - 4);
        ui.rrect(x - 2, y - 2, w + 4, h + 4, 12, GOLD);
        ui.rrect(x, y, w, h, 10, EDGE);
        float used = ui.text(head, x + 12, y + h - 12, 14.5f, GOLD, Align.left, w - 24);
        ui.text(body, x + 12, y + h - 22 - used, 12.5f, Color.WHITE, Align.left, w - 24);
    }
    void tips() {
        if (cardT < cardD || cardHold || phase.equals("over") || jailT < 3.3f) return;
        for (int i = 0; i < Game.N; i++) {
            sq(i);
            if (!ui.over(R[0], R[1], R[2], R[3])) continue;
            Game.Sq q = Game.BOARD[i];
            if (q.kind != Game.PROP) { tip(q.tag, q.name); return; }
            int o = owner[i];
            int have = 0;
            for (int k = 0; k < Game.N; k++) if (o >= 0 && Game.BOARD[k].group == q.group && owner[k] == o) have++;
            boolean full = have == 3;
            String s = Game.HOOD[q.group] + ". Price $" + q.price + ". Visitors pay $" + Game.rent(i, level[i], have, false) + (have == 2 ? " (+50% for owning two here)" : "")
                    + ". Tenants leave $" + Game.envUnit(i, level[i], o >= 0 && sCh[o] == Game.ROB) + " a month, up to 3 months, until the owner comes by.";
            s += o < 0 ? "\nNobody owns it." : "\nOwner: " + sName[o] + " (" + (level[i] == 0 ? "as-is" : level[i] == 1 ? "fresh paint" : "granite countertops") + ").";
            s += "\n\nOwn two in " + Game.HOOD[q.group] + ": visitors pay 50% more. Own all three" + (full ? " (" + sName[o] + " does)" : "") + ": double, plus " + Game.HOOD_PERK[q.group];
            tip(q.name, s);
            return;
        }
        for (int i = 0; i < cash.length; i++) if (ui.over(PX, 706 - i * ROWH - 51, PW, 51)) {
            StringBuilder s = new StringBuilder(Game.CH_PERK[sCh[i]]);
            for (int id : hand[i]) s.append("\nHolding: ").append(Game.ROBC[id].title);
            tip(sName[i] + " as " + Game.CH_NAME[sCh[i]], s.toString());
            return;
        }
        float w3 = (PW - 16) / 3;
        if (!trading && you >= 0 && you < hand.length) for (int k = 0; k < hand[you].length; k++) if (ui.over(PX + k * (w3 + 8), 78, w3, 36)) {
            Game.Card c = Game.ROBC[hand[you][k]];
            tip(c.title, c.text);
            return;
        }
    }

    void menu() {
        ui.rect(0, 0, Ui.W, Ui.H, DIM);
        ui.rrect(466, 176, 348, 368, 20, GOLD);
        ui.rrect(470, 180, 340, 360, 18, PANEL);
        ui.textC("MENU", 640, 502, 34, GOLD);
        if (btn("BACK TO THE GAME", 500, 410, 280, 52, MONEY, true)) menuOpen = false;
        if (btn("HOW TO PLAY", 500, 344, 280, 52, col(0x2D7DD2), true)) tutPage = 0;
        if (btn("SETTINGS", 500, 278, 280, 52, SLATE, true)) settingsOpen = true;
        if (btn("QUIT TO MAIN MENU", 500, 212, 280, 52, PC[0], true)) leave(null);
    }

    void tutorial() {
        String[] pg = TUTORIAL[tutPage];
        ui.rect(0, 0, Ui.W, Ui.H, DIM);
        ui.rrect(146, 96, 988, 528, 22, GOLD);
        ui.rrect(150, 100, 980, 520, 20, PANEL);
        ui.text("HOW TO PLAY  " + (tutPage + 1) + " / " + TUTORIAL.length, 190, 596, 14, SOFT, Align.left, 0);
        ui.text(pg[0], 190, 566, 34, GOLD, Align.left, 0);
        Texture t = pic(pg[1]);
        if (t != null) framed(t, 400, 360, 400, 1);
        ui.text(pg[2], 640, 500, 16.5f, Color.WHITE, Align.left, 450);
        if (btn("BACK", 190, 124, 150, 50, SLATE, tutPage > 0)) tutPage--;
        if (btn("CLOSE", 565, 124, 150, 50, PC[0], true)) { tutPage = -1; return; }
        if (btn("NEXT", 940, 124, 150, 50, MONEY, tutPage < TUTORIAL.length - 1)) tutPage++;
    }

    void over() {
        ui.rect(0, 0, Ui.W, Ui.H, DIM);
        ui.rrect(186, 56, 908, 608, 22, GOLD);
        ui.rrect(190, 60, 900, 600, 20, PANEL);
        ui.textC("THAT'S THE YEAR", 640, 622, 44, GOLD);
        if (rank == null) return;
        for (int s = -1; s <= 1; s += 2) {      // the winner, twice, wearing the crown
            float x = 640 + s * 330, y = 606 + Math.abs(MathUtils.sin(time * 5)) * 8;
            token(x, y, 30, sCh[rank[0]], false);
            crown(x, y + 42, 11);
        }
        int n = rank.length, best = Math.max(1, worth[rank[0]]);
        ui.textC(sName[rank[0]] + " wins. Wings are on " + sName[rank[n - 1]] + ".", 640, 578, 18, Color.WHITE);
        for (int i = 0; i < n; i++) {
            int p = rank[i];
            float y = 520 - i * 44;
            ui.text((i + 1) + ".", 236, y + 12, 22, i == 0 ? GOLD : SOFT, Align.left, 0);
            token(296, y, 16, sCh[p], false);
            ui.text(sName[p], 326, y + 10, 18, Color.WHITE, Align.left, 0);
            float bw = Math.max(6, 380f * Math.max(0, worth[p]) / best) * MathUtils.clamp(overT / 1.2f, 0.05f, 1);
            ui.rrect(500, y - 13, bw, 26, 6, PC[sCh[p]]);
            ui.text(money(worth[p]), 900, y + 11, 21, CASH, Align.right, 150);
        }
        float ay = 520 - n * 44 - 14;
        for (int i = 0; awards != null && i < awards.size; i++) {
            JsonValue a = awards.get(i);
            int p = a.get(2).asInt();
            float x = 236 + (i % 2) * 420, y = ay - (i / 2) * 36;
            ui.text(a.get(0).asString() + ": " + (p < 0 ? "nobody" : sName[p]), x, y, 15, GOLD, Align.left, 0);
            ui.text(a.get(1).asString(), x, y - 17, 11, SOFT, Align.left, 0);
        }
        if (btn("QUIT", 930, 72, 130, 46, col(0x7A2E2E), true)) { leave(null); return; }
        if (lead) { if (btn("BACK TO THE LOBBY", 490, 72, 300, 46, PC[0], !sent)) send("again"); }
        else ui.textC("Waiting for the lobby leader.", 640, 94, 15, SOFT);
    }
}
