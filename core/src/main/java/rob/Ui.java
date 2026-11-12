package rob;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** Rectangles, circles, lines, text and buttons on one SpriteBatch, on a 1280x720 virtual screen. */
public class Ui {
    public static final float W = 1280, H = 720;
    public final SpriteBatch b = new SpriteBatch();
    public final FitViewport vp = new FitViewport(W, H);
    final Texture px, circ;
    final BitmapFont big, small;
    final GlyphLayout gl = new GlyphLayout();
    final Vector2 tmp = new Vector2();
    final Color tc = new Color();
    public float mx, my;
    public boolean click, pressed;      // pressed is set by the input listener, so a very quick tap still counts

    public Ui() {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE); p.fill();
        px = new Texture(p); p.dispose();
        int s = 128;
        p = new Pixmap(s, s, Pixmap.Format.RGBA8888);
        p.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < s; y++) for (int x = 0; x < s; x++) {
            float d = (float) Math.sqrt((x - 63.5f) * (x - 63.5f) + (y - 63.5f) * (y - 63.5f));
            p.drawPixel(x, y, Color.rgba8888(1, 1, 1, Math.max(0, Math.min(1, 63f - d))));
        }
        circ = new Texture(p, true); p.dispose();
        circ.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        big = font("big"); small = font("small");
    }
    static BitmapFont font(String name) {
        Texture t = new Texture(Gdx.files.internal(name + ".png"), true);
        t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        BitmapFont f = new BitmapFont(Gdx.files.internal(name + ".fnt"), new TextureRegion(t), false);
        f.setUseIntegerPositions(false);
        return f;
    }

    public void begin() {
        vp.apply();
        b.setProjectionMatrix(vp.getCamera().combined);
        vp.unproject(tmp.set(Gdx.input.getX(), Gdx.input.getY()));
        mx = tmp.x; my = tmp.y; click = pressed; pressed = false;
        b.begin();
    }
    public void end() { b.end(); }

    public Color a(Color c, float alpha) { return tc.set(c.r, c.g, c.b, c.a * alpha); }
    public boolean over(float x, float y, float w, float h) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }

    public void rect(float x, float y, float w, float h, Color c) { b.setColor(c); b.draw(px, x, y, w, h); }
    public void circle(float cx, float cy, float r, Color c) { b.setColor(c); b.draw(circ, cx - r, cy - r, r * 2, r * 2); }
    /** Rectangle centred on cx,cy and turned by deg. */
    public void rot(float cx, float cy, float w, float h, float deg, Color c) {
        b.setColor(c);
        b.draw(px, cx - w / 2, cy - h / 2, w / 2, h / 2, w, h, 1, 1, deg, 0, 0, 1, 1, false, false);
    }
    public void line(float x1, float y1, float x2, float y2, float th, Color c) {
        float dx = x2 - x1, dy = y2 - y1, len = (float) Math.sqrt(dx * dx + dy * dy);
        rot((x1 + x2) / 2, (y1 + y2) / 2, len, th, (float) Math.toDegrees(Math.atan2(dy, dx)), c);
    }
    public void rrect(float x, float y, float w, float h, float r, Color c) {
        r = Math.min(r, Math.min(w, h) / 2);
        b.setColor(c);
        b.draw(px, x + r, y, w - 2 * r, h);
        b.draw(px, x, y + r, r, h - 2 * r);
        b.draw(px, x + w - r, y + r, r, h - 2 * r);
        b.draw(circ, x, y, r, r, 0, 1, 0.5f, 0.5f);
        b.draw(circ, x + w - r, y, r, r, 0.5f, 1, 1, 0.5f);
        b.draw(circ, x, y + h - r, r, r, 0, 0.5f, 0.5f, 0);
        b.draw(circ, x + w - r, y + h - r, r, r, 0.5f, 0.5f, 1, 0);
    }

    public void img(Texture t, float x, float y, float w, float h, float alpha) { b.setColor(1, 1, 1, alpha); b.draw(t, x, y, w, h); }
    public void img(TextureRegion t, float x, float y, float w, float h, float alpha) { b.setColor(1, 1, 1, alpha); b.draw(t, x, y, w, h); }

    BitmapFont font(float size) {
        BitmapFont f = size <= 20 ? small : big;
        f.getData().setScale(size / (f == small ? 24f : 56f));
        return f;
    }
    /** y is the top of the text. Wraps when width > 0. Returns the height used. */
    public float text(String s, float x, float y, float size, Color c, int align, float width) {
        BitmapFont f = font(size);
        gl.setText(f, s, c, width, align, width > 0);
        f.draw(b, gl, x, y);
        return gl.height;
    }
    public float textH(String s, float size, float width) {
        gl.setText(font(size), s, Color.WHITE, width, Align.left, width > 0);
        return gl.height;
    }
    public float textW(String s, float size) {
        gl.setText(font(size), s);
        return gl.width;
    }
    public void textC(String s, float cx, float cy, float size, Color c) {
        BitmapFont f = font(size);
        gl.setText(f, s, c, 0, Align.center, false);
        f.draw(b, gl, cx, cy + gl.height / 2);
    }
    /** Centred both ways and wrapped inside a box. */
    public void textBox(String s, float x, float y, float w, float h, float size, Color c) {
        BitmapFont f = font(size);
        gl.setText(f, s, c, w, Align.center, true);
        f.draw(b, gl, x, y + h / 2 + gl.height / 2);
    }

    static final Color SHADOW = new Color(0, 0, 0, 0.35f), DISABLED = new Color(0.45f, 0.48f, 0.5f, 1);
    public boolean button(String label, float x, float y, float w, float h, Color c, boolean enabled) {
        boolean hot = enabled && over(x, y, w, h);
        float lift = hot ? 2 : 0;
        rrect(x, y - 4, w, h, 10, SHADOW);
        rrect(x, y + lift, w, h, 10, enabled ? c : DISABLED);
        if (hot) rrect(x, y + lift, w, h, 10, a(Color.WHITE, 0.18f));
        float size = Math.min(22, h * 0.42f);
        while (size > 8 && textW(label, size) > w - 14) size *= 0.94f;       // long labels shrink to fit
        textC(label, x + w / 2, y + lift + h / 2, size, Color.WHITE);
        return hot && click;
    }
}
