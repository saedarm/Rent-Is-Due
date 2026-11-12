package rob;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Align;

/**
 * Wing Run: the side game for while you wait. Shirtless Rob runs a grid of Frickers tables eating wings while
 * three salads chase him. Hot sauce makes him fast and lets him flatten the salads for a few seconds.
 * Runs entirely on your own screen; only the wing count is sent to the table, for the Wing King award.
 */
public class WingRun {
    static final int W = 19, H = 7, START_X = 9, START_Y = 4, SALADS = 3;
    static final int[][] HOME = {{0, 0}, {18, 6}, {9, 0}}, DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    static final Color FLOOR = Main.col(0x13262D), TABLE = Main.col(0x7A4A21), TABLE2 = Main.col(0x9A6431), WINGC = Main.col(0xD67828),
            BONE = Main.col(0xFAF0DC), SAUCE = Main.col(0xD0342C), LEAF = Main.col(0x4CAF50), LEAF2 = Main.col(0x9BE19A), SCARED = Main.col(0xB9C8CC);

    final Main main;
    final Ui ui;
    final TextureRegion rob;
    final boolean[][] wing = new boolean[W][H], sauce = new boolean[W][H];
    // Rob: where he is (in squares), the square he's walking to, the way he's going and the way you want him to go
    float rx, ry;
    int tx, ty, dx, dy, wantX, wantY, faceX = 1, faceY;
    final float[] sx = new float[SALADS], sy = new float[SALADS];
    final int[] stx = new int[SALADS], sty = new int[SALADS], sdx = new int[SALADS], sdy = new int[SALADS];
    float turbo, stun, noteT;
    String note = "";
    /** Wings eaten this game, for the Wing King award. */
    public int eaten;
    int basket = 1, left;

    WingRun(Main main, Ui ui, TextureRegion rob) { this.main = main; this.ui = ui; this.rob = rob; reset(); }

    static boolean wall(int x, int y) { return x < 0 || y < 0 || x >= W || y >= H || (x % 2 == 1 && y % 2 == 1); }

    void reset() { eaten = 0; basket = 1; fill(); home(); }
    /** A fresh basket: a wing on every open square, hot sauce in two corners. */
    void fill() {
        left = 0;
        for (int x = 0; x < W; x++) for (int y = 0; y < H; y++) {
            wing[x][y] = !wall(x, y) && !(x == START_X && y == START_Y);
            sauce[x][y] = false;
            if (wing[x][y]) left++;
        }
        sauce[0][6] = sauce[18][0] = true;
        wing[0][6] = wing[18][0] = false; left -= 2;
    }
    void home() {
        rx = tx = START_X; ry = ty = START_Y; dx = dy = 0; turbo = 0;
        for (int i = 0; i < SALADS; i++) { sx[i] = stx[i] = HOME[i][0]; sy[i] = sty[i] = HOME[i][1]; sdx[i] = sdy[i] = 0; }
    }
    void say(String s) { note = s; noteT = 2.2f; }

    /** One frame. Only moves while active (you're waiting and the panel is open). */
    void update(float dt, boolean active) {
        noteT -= dt;
        if (!active) return;
        boolean l = key(Input.Keys.LEFT, Input.Keys.A), r = key(Input.Keys.RIGHT, Input.Keys.D), u = key(Input.Keys.UP, Input.Keys.W), d = key(Input.Keys.DOWN, Input.Keys.S);
        if (l || r || u || d) { wantX = l ? -1 : r ? 1 : 0; wantY = wantX != 0 ? 0 : u ? -1 : 1; }
        if (stun > 0) { stun -= dt; return; }
        turbo = Math.max(0, turbo - dt);

        // Rob
        if (dx == -wantX && dy == -wantY && (dx != 0 || dy != 0)) { tx -= dx; ty -= dy; dx = -dx; dy = -dy; }       // turn around mid-step
        float step = (turbo > 0 ? 8.5f : 6.5f) * dt;
        while (step > 0) {
            float d2 = Math.abs(tx - rx) + Math.abs(ty - ry);
            if (d2 > step) { rx += Math.signum(tx - rx) * step; ry += Math.signum(ty - ry) * step; break; }
            rx = tx; ry = ty; step -= d2;
            arrive();
            if ((wantX != 0 || wantY != 0) && !wall(tx + wantX, ty + wantY)) { dx = wantX; dy = wantY; }
            else if (wall(tx + dx, ty + dy)) { dx = dy = 0; }
            if (dx == 0 && dy == 0) break;
            faceX = dx; faceY = dy; tx += dx; ty += dy;
        }

        // salads
        float speed = Math.min(6f, 3.2f + 0.35f * basket) * (turbo > 0 ? 0.6f : 1);
        for (int i = 0; i < SALADS; i++) {
            float st = speed * dt;
            while (st > 0) {
                float d2 = Math.abs(stx[i] - sx[i]) + Math.abs(sty[i] - sy[i]);
                if (d2 > st) { sx[i] += Math.signum(stx[i] - sx[i]) * st; sy[i] += Math.signum(sty[i] - sy[i]) * st; break; }
                sx[i] = stx[i]; sy[i] = sty[i]; st -= d2;
                steer(i);
                stx[i] += sdx[i]; sty[i] += sdy[i];
                if (sdx[i] == 0 && sdy[i] == 0) break;
            }
            if (Math.abs(sx[i] - rx) + Math.abs(sy[i] - ry) < 0.7f) {
                if (turbo > 0) {
                    sx[i] = stx[i] = HOME[i][0]; sy[i] = sty[i] = HOME[i][1]; sdx[i] = sdy[i] = 0;
                    eaten += 5; say("Flattened a salad. +5"); main.snd("buy");
                } else {
                    eaten = Math.max(0, eaten - 5); stun = 1.3f; say("You ate a salad. Disgusting. -5"); main.snd("no");
                    home();
                    return;
                }
            }
        }
    }
    boolean key(int a, int b) { return Gdx.input.isKeyPressed(a) || Gdx.input.isKeyPressed(b); }

    /** Rob reached a square: eat what's there. */
    void arrive() {
        if (wing[tx][ty]) {
            wing[tx][ty] = false; eaten++; left--;
            if (eaten % 2 == 0) main.snd("hop");
        }
        if (sauce[tx][ty]) { sauce[tx][ty] = false; turbo = 5; say("HOT SAUCE! Go get the salads."); main.snd("up"); }
        if (left <= 0) { basket++; fill(); say("Basket " + (basket - 1) + " cleared. They brought another."); main.snd("hood"); }
    }
    /** A salad at a square picks its next step: toward Rob (away, if he's on hot sauce), with some wandering. */
    void steer(int i) {
        int best = -1, n = 0;
        float bestD = 0;
        int[] ok = new int[4];
        for (int k = 0; k < 4; k++) {
            int nx = stx[i] + DIRS[k][0], ny = sty[i] + DIRS[k][1];
            if (wall(nx, ny) || (DIRS[k][0] == -sdx[i] && DIRS[k][1] == -sdy[i] && (sdx[i] != 0 || sdy[i] != 0))) continue;
            ok[n++] = k;
            float d = Math.abs(nx - tx) + Math.abs(ny - ty);
            if (best < 0 || (turbo > 0 ? d > bestD : d < bestD)) { best = k; bestD = d; }
        }
        if (n == 0) { sdx[i] = -sdx[i]; sdy[i] = -sdy[i]; return; }       // dead end: turn round
        if (MathUtils.random() < 0.3f) best = ok[MathUtils.random(n - 1)];
        sdx[i] = DIRS[best][0]; sdy[i] = DIRS[best][1];
    }

    /** Draws the game inside the given box. paused dims it with a message. */
    void draw(float x, float y, float w, float h, float time, String paused) {
        float cell = Math.min(w / W, (h - 22) / H), ox = x + (w - cell * W) / 2, oy = y + 2;
        ui.rrect(ox - 4, oy - 2, cell * W + 8, cell * H + 4, 8, FLOOR);
        for (int cx = 0; cx < W; cx++) for (int cy = 0; cy < H; cy++) {
            float px = ox + cx * cell, py = oy + (H - 1 - cy) * cell, mx = px + cell / 2, my = py + cell / 2;
            if (wall(cx, cy)) { ui.rrect(px + 2, py + 2, cell - 4, cell - 4, 5, TABLE); ui.rrect(px + 5, py + 6, cell - 10, cell - 10, 4, TABLE2); }
            else if (wing[cx][cy]) { ui.rot(mx + cell * 0.12f, my - cell * 0.1f, cell * 0.26f, cell * 0.08f, -35, BONE); ui.circle(mx - cell * 0.04f, my + cell * 0.03f, cell * 0.15f, WINGC); }
            else if (sauce[cx][cy]) {
                float bob = MathUtils.sin(time * 6) * 2;
                ui.rrect(mx - cell * 0.16f, my - cell * 0.3f + bob, cell * 0.32f, cell * 0.5f, 4, SAUCE);
                ui.rect(mx - cell * 0.07f, my + cell * 0.2f + bob, cell * 0.14f, cell * 0.16f, BONE);
            }
        }
        for (int i = 0; i < SALADS; i++) {       // salads: green bowls, pale and wobbly when Rob has the sauce
            float px = ox + (sx[i] + 0.5f) * cell, py = oy + (H - 1 - sy[i] + 0.5f) * cell + (turbo > 0 ? MathUtils.sin(time * 30 + i) * 1.5f : 0);
            ui.circle(px, py, cell * 0.4f, turbo > 0 ? SCARED : LEAF);
            ui.circle(px - cell * 0.13f, py + cell * 0.1f, cell * 0.14f, LEAF2); ui.circle(px + cell * 0.12f, py - cell * 0.08f, cell * 0.11f, LEAF2);
            ui.circle(px + cell * 0.08f, py + cell * 0.16f, cell * 0.07f, SAUCE);     // a tomato
        }
        // Rob, chomping in the direction he's going
        float px = ox + (rx + 0.5f) * cell, py = oy + (H - 1 - ry + 0.5f) * cell, rad = cell * (turbo > 0 ? 0.84f : 0.74f);      // bigger than a square, on purpose
        if (stun > 0 && (int) (time * 12) % 2 == 0) rad *= 0.8f;
        if (turbo > 0) ui.circle(px, py, rad + 4, ui.a(SAUCE, 0.8f));
        ui.circle(px, py, rad + 2, Color.WHITE);
        ui.img(rob, px - rad, py - rad, rad * 2, rad * 2, 1);
        float ang = MathUtils.atan2(-faceY, faceX) * MathUtils.radiansToDegrees, open = 18 + 16 * MathUtils.sin(time * 16);
        for (int k = -3; k <= 3; k++) {
            float a = (ang + open * k / 3f) * MathUtils.degreesToRadians;
            ui.line(px + MathUtils.cos(a) * rad * 0.25f, py + MathUtils.sin(a) * rad * 0.25f, px + MathUtils.cos(a) * (rad + 3), py + MathUtils.sin(a) * (rad + 3), rad * 0.2f, FLOOR);
        }
        float top = oy + cell * H + 18;
        ui.text("WING RUN", ox, top, 13, Main.GOLD, Align.left, 0);
        ui.text("arrows or WASD", ox + 84, top - 1, 10.5f, Main.SOFT, Align.left, 0);
        ui.text("wings " + eaten + "   basket " + basket, ox + cell * W - 200, top, 13, Color.WHITE, Align.right, 200);
        if (noteT > 0) ui.textC(note, ox + cell * W / 2, oy + cell * H / 2, 15, ui.a(Color.WHITE, Math.min(1, noteT)));
        if (paused != null) {
            ui.rrect(ox - 4, oy - 2, cell * W + 8, cell * H + 4, 8, ui.a(Color.BLACK, 0.6f));
            ui.textC(paused, ox + cell * W / 2, oy + cell * H / 2, 20, Main.GOLD);
        }
    }
}
