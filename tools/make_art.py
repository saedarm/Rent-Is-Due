"""Builds assets/faces.png (playing-piece portraits) and assets/pic/*.png (the pictures on cards and pop-ups).
   Needs Pillow. Photos live in tools/photos; change a crop box in PHOTOS if you swap one.   python3 tools/make_art.py"""
import math, os, random
from PIL import Image, ImageDraw, ImageFont

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "..", "assets")
FONT = "/usr/share/fonts/truetype/google-fonts/Poppins-Bold.ttf"
# file, square crop box (left, top, right, bottom) around the face. Order = character order in Game.java.
PHOTOS = [("rob.png", (96, 36, 222, 162)), ("jules.png", (108, 62, 282, 236)),
          ("sam.jpg", (410, 640, 1530, 1760)), ("jack.jpg", (320, 235, 800, 715))]
INK = (27, 27, 31)
ROB, JULES, SAM, JACK, TODD, BRENDA = range(6)

def font(size): return ImageFont.truetype(FONT, size)

def cartoon_face(skin, hair, extra):
    im = Image.new("RGB", (256, 256), skin); d = ImageDraw.Draw(im)
    d.ellipse([-20, -110, 276, 90], fill=hair)
    for x in (88, 168): d.ellipse([x - 12, 118, x + 12, 142], fill=INK)
    d.arc([88, 150, 168, 210], 20, 160, fill=INK, width=8)
    if extra == "tie":
        d.rectangle([0, 226, 256, 256], fill=(40, 50, 70)); d.polygon([(118, 226), (138, 226), (128, 256)], fill=(200, 40, 40))
        d.rectangle([60, 108, 196, 116], fill=INK)
        for x in (88, 168): d.rectangle([x - 26, 104, x + 26, 148], outline=INK, width=7)
    else:
        d.ellipse([20, 150, 60, 190], fill=(240, 150, 60)); d.ellipse([196, 150, 236, 190], fill=(240, 150, 60))
        d.arc([70, 96, 106, 116], 180, 360, fill=INK, width=6); d.arc([150, 96, 186, 116], 180, 360, fill=INK, width=6)
    return im

def load_faces():
    faces = []
    for name, box in PHOTOS:
        faces.append(Image.open(os.path.join(HERE, "photos", name)).convert("RGB").crop(box).resize((256, 256), Image.LANCZOS))
    faces.append(cartoon_face((236, 200, 170), (70, 60, 50), "tie"))
    faces.append(cartoon_face((245, 205, 175), (235, 190, 80), "earrings"))
    return faces
FACES = load_faces()

def round_face(i, size):
    f = FACES[i].resize((size, size), Image.LANCZOS).convert("RGBA")
    m = Image.new("L", (size * 4, size * 4), 0)
    ImageDraw.Draw(m).ellipse([2, 2, size * 4 - 3, size * 4 - 3], fill=255)
    f.putalpha(m.resize((size, size), Image.LANCZOS))
    return f

def make_faces():
    sheet = Image.new("RGBA", (512, 256), (0, 0, 0, 0))
    for i in range(6): sheet.paste(round_face(i, 128), ((i % 4) * 128, (i // 4) * 128))
    sheet.save(os.path.join(OUT, "faces.png"))

# ---------------------------------------------------------------- picture kit (512x320 canvas, saved at 256x160)
class Pic:
    def __init__(self, sky, ground=None, gy=236):
        self.im = Image.new("RGB", (512, 320), sky); self.d = ImageDraw.Draw(self.im)
        if ground: self.d.rectangle([0, gy, 512, 320], fill=ground)
    def text(self, s, x, y, size=34, fill=INK, stroke=None):
        self.d.text((x, y), s, font=font(size), fill=fill, anchor="mm", stroke_width=5 if stroke else 0, stroke_fill=stroke)
    def house(self, x, y, w=120, h=90, wall=(244, 232, 200), roof=(150, 70, 50), dark=False):
        d = self.d
        d.rectangle([x, y - h, x + w, y], fill=wall, outline=INK, width=5)
        d.polygon([(x - 14, y - h), (x + w / 2, y - h - 54), (x + w + 14, y - h)], fill=roof, outline=INK)
        d.line([(x - 14, y - h), (x + w / 2, y - h - 54), (x + w + 14, y - h), (x - 14, y - h)], fill=INK, width=5)
        d.rectangle([x + w * .58, y - h * .58, x + w * .8, y], fill=(120, 80, 50), outline=INK, width=4)
        d.rectangle([x + w * .14, y - h * .7, x + w * .42, y - h * .32], fill=(60, 60, 80) if dark else (160, 215, 240), outline=INK, width=4)
    def truck(self, x, y, c=(200, 50, 45), s=1.0):
        d = self.d
        d.rounded_rectangle([x, y - 46 * s, x + 150 * s, y - 14 * s], 8, fill=c, outline=INK, width=5)
        d.rounded_rectangle([x + 84 * s, y - 84 * s, x + 142 * s, y - 40 * s], 10, fill=c, outline=INK, width=5)
        d.rectangle([x + 96 * s, y - 74 * s, x + 132 * s, y - 50 * s], fill=(170, 220, 240), outline=INK, width=3)
        for wx in (34, 116): d.ellipse([x + (wx - 18) * s, y - 30 * s, x + (wx + 18) * s, y + 6 * s], fill=INK); d.ellipse([x + (wx - 7) * s, y - 19 * s, x + (wx + 7) * s, y - 5 * s], fill=(200, 200, 200))
    def person(self, x, y, face=ROB, h=170, shirt=(200, 50, 45), arms="down", r=None):
        d = self.d; r = r or int(h * .27); top = y - h + 2 * r
        d.line([(x - 14, y - h * .32), (x - 20, y)], fill=INK, width=12); d.line([(x + 14, y - h * .32), (x + 20, y)], fill=INK, width=12)
        d.rounded_rectangle([x - 30, top - 6, x + 30, y - h * .3], 14, fill=shirt, outline=INK, width=5)
        ay = top + 16; dy = {"down": 50, "up": -56, "out": 0}[arms]
        d.line([(x - 28, ay), (x - 66, ay + dy)], fill=INK, width=11); d.line([(x + 28, ay), (x + 66, ay + dy)], fill=INK, width=11)
        d.ellipse([x - r - 5, y - h - 5, x + r + 5, y - h + 2 * r + 5], fill=INK)
        f = round_face(face, 2 * r); self.im.paste(f, (int(x - r), int(y - h)), f)
    def drum(self, x, y, s=1.0, ang=30):
        d = self.d; a = math.radians(ang); bx, by = x + math.cos(a) * 46 * s, y + math.sin(a) * 46 * s
        d.line([(x, y), (bx, by)], fill=INK, width=int(18 * s)); d.line([(x, y), (bx, by)], fill=(250, 240, 220), width=int(10 * s))
        d.ellipse([bx - 11 * s, by - 11 * s, bx + 11 * s, by + 11 * s], fill=(250, 240, 220), outline=INK, width=4)
        d.ellipse([x - 30 * s, y - 26 * s, x + 30 * s, y + 26 * s], fill=(214, 120, 40), outline=INK, width=5)
        d.arc([x - 18 * s, y - 16 * s, x + 6 * s, y + 4 * s], 180, 280, fill=(250, 190, 110), width=5)
    def burst(self, x, y, r, c, n=12):
        pts = []
        for i in range(n * 2):
            a = math.pi * i / n; rr = r if i % 2 == 0 else r * .55
            pts.append((x + math.cos(a) * rr, y + math.sin(a) * rr))
        self.d.polygon(pts, fill=c, outline=INK); self.d.line(pts + [pts[0]], fill=INK, width=4)
    def bill(self, x, y, s=1.0, c=(90, 190, 110)):
        self.d.rounded_rectangle([x - 44 * s, y - 24 * s, x + 44 * s, y + 24 * s], 6, fill=c, outline=INK, width=4); self.text("$", x, y, int(34 * s))
    def env(self, x, y, s=1.0):
        d = self.d; d.rectangle([x - 50 * s, y - 32 * s, x + 50 * s, y + 32 * s], fill=(250, 245, 225), outline=INK, width=5)
        d.line([(x - 50 * s, y - 32 * s), (x, y + 4 * s), (x + 50 * s, y - 32 * s)], fill=INK, width=4)
    def tower(self, x, y):
        d = self.d
        for dx in (-34, 34): d.line([(x + dx, y), (x + dx * .5, y - 120)], fill=INK, width=8)
        d.ellipse([x - 62, y - 196, x + 62, y - 104], fill=(170, 215, 235), outline=INK, width=5); self.text("H2O", x, y - 150, 30)
    def sign(self, s, x, y, c=(255, 255, 255), size=30, pole=True, fill=INK):
        w = font(size).getlength(s) + 28
        if pole: self.d.line([(x, y), (x, y + 70)], fill=INK, width=8)
        self.d.rounded_rectangle([x - w / 2, y - size * .8, x + w / 2, y + size * .8], 8, fill=c, outline=INK, width=5); self.text(s, x, y, size, fill)
    def phone(self, x, y, lines, c=(40, 44, 52)):
        self.d.rounded_rectangle([x - 70, y - 120, x + 70, y + 120], 18, fill=c, outline=INK, width=5)
        self.d.rectangle([x - 58, y - 98, x + 58, y + 98], fill=(240, 244, 250))
        for i, (s, col) in enumerate(lines): self.text(s, x, y - 60 + i * 44, 21, col)
    def critter(self, x, y, s=1.0, c=(120, 120, 128), mask=True, tail=False):
        d = self.d
        if tail: d.ellipse([x + 16 * s, y - 70 * s, x + 66 * s, y + 10 * s], fill=c, outline=INK, width=4)
        d.ellipse([x - 34 * s, y - 20 * s, x + 34 * s, y + 40 * s], fill=c, outline=INK, width=4)
        for ex in (-22, 22): d.polygon([(x + ex * s - 10 * s, y - 34 * s), (x + ex * s, y - 58 * s), (x + ex * s + 10 * s, y - 34 * s)], fill=c, outline=INK)
        d.ellipse([x - 28 * s, y - 46 * s, x + 28 * s, y + 2 * s], fill=c, outline=INK, width=4)
        if mask: d.rounded_rectangle([x - 26 * s, y - 32 * s, x + 26 * s, y - 16 * s], 6, fill=INK)
        for ex in (-11, 11): d.ellipse([x + ex * s - 5 * s, y - 29 * s, x + ex * s + 5 * s, y - 19 * s], fill=(255, 255, 255))
        d.ellipse([x - 5 * s, y - 14 * s, x + 5 * s, y - 6 * s], fill=INK)
    def ladder(self, x, y, h=150, lean=30):
        d = self.d
        for dx in (0, 34): d.line([(x + dx, y), (x + dx + lean, y - h)], fill=(190, 140, 70), width=8)
        for i in range(1, 6): t = i / 6; d.line([(x + lean * t, y - h * t), (x + 34 + lean * t, y - h * t)], fill=(190, 140, 70), width=6)
    def can(self, x, y, c, label, w=80, h=110, size=22):
        d = self.d; d.rectangle([x - w / 2, y - h, x + w / 2, y], fill=c, outline=INK, width=5)
        d.ellipse([x - w / 2, y - h - 14, x + w / 2, y - h + 14], fill=(210, 210, 215), outline=INK, width=5); self.text(label, x, y - h / 2, size, (255, 255, 255), INK)
    def pool(self, x, y, c):
        self.d.ellipse([x - 150, y - 36, x + 150, y + 36], fill=c, outline=(230, 230, 230), width=10)
    def paper(self, x, y, title, w=130, h=160, c=(255, 255, 255)):
        d = self.d; d.rectangle([x - w / 2, y - h / 2, x + w / 2, y + h / 2], fill=c, outline=INK, width=5); self.text(title, x, y - h / 2 + 28, 26)
        for i in range(4): d.line([(x - w / 2 + 16, y - h / 2 + 60 + i * 22), (x + w / 2 - 16, y - h / 2 + 60 + i * 22)], fill=(150, 150, 160), width=5)
    def dots(self, n, c, seed, r=5, box=(0, 0, 512, 320)):
        rnd = random.Random(seed)
        for _ in range(n):
            x, y = rnd.randint(box[0], box[2]), rnd.randint(box[1], box[3]); self.d.ellipse([x - r, y - r, x + r, y + r], fill=c)
    def save(self, key):
        self.im.resize((256, 160), Image.LANCZOS).save(os.path.join(OUT, "pic", key + ".png"))

SKY, GRASS, DUSK, WHITE = (150, 205, 235), (110, 180, 95), (60, 70, 110), (255, 255, 255)
RED, GOLD, GREEN, BLUE, ORANGE = (200, 50, 45), (242, 194, 48), (60, 170, 100), (45, 110, 200), (240, 140, 40)

def make_pics():
    # ---- town news
    p = Pic((170, 190, 210), (235, 240, 245)); p.house(60, 236); p.house(300, 236, wall=(200, 215, 230)); p.burst(190, 170, 70, (120, 190, 240)); p.dots(60, WHITE, 1); p.text("BRRR", 400, 60, 54, WHITE, INK); p.save("vortex")
    p = Pic(ORANGE)
    for i in range(9): p.drum(70 + (i % 5) * 80 + (i // 5) * 40, 250 - (i // 5) * 50, 1.0, 20 + i * 37)
    p.person(410, 300, ROB, arms="up"); p.sign("ALL YOU CAN EAT", 200, 60, GOLD, 32, False); p.save("allyoucan")
    p = Pic(SKY, GRASS)
    for i, x in enumerate((30, 190, 350)): p.house(x, 236 - i * 12, h=80 + i * 26); p.text("$" * (i + 1), x + 60, 60 - i * 6, 40, GREEN, INK)
    p.save("boom")
    p = Pic(SKY, GRASS); p.tower(130, 250); p.person(330, 300, ROB, arms="out"); p.d.rectangle([4, 4, 508, 316], outline=RED, width=10); p.sign("LIVE", 440, 44, RED, 30, False, WHITE); p.sign("BOIL YOUR WATER", 256, 286, WHITE, 26, False); p.save("boil")
    p = Pic((225, 215, 195)); p.paper(150, 160, "1040", 170, 220); p.person(370, 300, ROB, arms="up"); p.sign("EXTENSION!", 370, 60, GOLD, 30, False); p.save("taxes")
    p = Pic((160, 20, 30)); p.d.rectangle([90, 90, 422, 230], fill=INK, outline=WHITE, width=6); p.text("REDS 5", 256, 130, 48, GOLD); p.text("OTHER GUYS 4", 256, 190, 34, WHITE)
    for x, y, c in ((70, 50, GOLD), (440, 60, WHITE), (256, 40, ORANGE)): p.burst(x, y, 36, c, 9)
    p.save("redswin")
    p = Pic(SKY, GRASS); p.house(40, 236); p.house(340, 236)
    for x, s in ((200, "NO"), (270, "NOPE")): p.sign(s, x, 130, WHITE, 30); p.d.line([(x, 200), (x, 270)], fill=INK, width=8)
    p.env(256, 60, .8); p.d.line([(196, 20), (316, 100)], fill=RED, width=12); p.save("strike")
    p = Pic(DUSK, (70, 110, 80)); p.d.ellipse([40, 30, 240, 230], outline=GOLD, width=10)
    for i in range(8): a = i * math.pi / 4; p.d.line([(140, 130), (140 + 100 * math.cos(a), 130 + 100 * math.sin(a))], fill=GOLD, width=5); p.d.ellipse([128 + 100 * math.cos(a), 118 + 100 * math.sin(a), 152 + 100 * math.cos(a), 142 + 100 * math.sin(a)], fill=RED, outline=INK, width=3)
    p.can(370, 280, (150, 90, 50), "COFFEE", 120, 150, 24); p.bill(350, 120, .8); p.bill(400, 96, .8); p.save("fair")
    p = Pic(SKY, (150, 150, 155)); p.d.rectangle([40, 70, 472, 236], fill=BLUE, outline=INK, width=6); p.text("HARDWARE", 256, 112, 44, WHITE); p.sign("50% OFF", 256, 190, GOLD, 40, False); p.person(90, 306, ROB, 150, arms="up"); p.save("blackfriday")
    p = Pic((20, 22, 40), (30, 34, 50)); p.house(60, 236, wall=(50, 54, 70), roof=(40, 40, 55), dark=True); p.d.polygon([(330, 20), (290, 120), (330, 120), (300, 210), (390, 90), (345, 90), (380, 20)], fill=GOLD, outline=INK)
    p.d.rounded_rectangle([390, 180, 470, 260], 12, fill=WHITE, outline=INK, width=5); p.d.ellipse([420, 210, 440, 230], fill=INK); p.save("outage")
    p = Pic((235, 235, 240)); p.d.line([(40, 70), (160, 130), (250, 110), (460, 270)], fill=RED, width=14); p.d.polygon([(470, 280), (420, 270), (455, 235)], fill=RED); p.house(60, 300, 110, 80); p.sign("25% OFF", 350, 90, GOLD, 34, False); p.save("dip")
    p = Pic((230, 190, 120), (120, 120, 125)); p.truck(280, 250, (240, 200, 60), 1.3); p.dots(70, (200, 90, 30), 3, 9); p.dots(40, (160, 60, 30), 4, 7)
    for i, f in enumerate((ROB, SAM, JACK)): p.person(60 + i * 75, 306, f, 120)
    p.save("leaves")
    # ---- Rob happens
    p = Pic(SKY, (150, 150, 155))
    for i in range(6): p.truck(10 + (i % 3) * 170, 180 + (i // 3) * 110, RED, .95)
    p.text("?", 256, 50, 80, WHITE, INK); p.save("truck")
    p = Pic(SKY, GRASS); p.tower(150, 250); p.person(340, 300, ROB, arms="up"); p.bill(430, 80); p.dots(14, (200, 240, 255), 5, 8, (270, 60, 420, 200)); p.save("tower")
    p = Pic(SKY, GRASS); p.person(256, 306, ROB, 190, arms="out")
    for x in (70, 150, 300, 380): p.house(x, 180, 62, 46)
    p.sign("ANOTHER ONE", 256, 40, GOLD, 30, False); p.save("fourth")
    p = Pic(SKY, (150, 150, 155)); p.truck(110, 270, RED, 1.8); p.d.rectangle([120, 130, 250, 190], fill=(250, 250, 250), outline=INK, width=5); p.d.rectangle([150, 86, 230, 130], fill=(190, 140, 70), outline=INK, width=5)
    f = round_face(ROB, 60); p.im.paste(f, (296, 132), f); p.text("AGAIN?!", 400, 50, 40, WHITE, INK); p.save("moves")
    p = Pic((90, 50, 30)); p.person(256, 306, ROB, 190, arms="up")
    for x in (50, 130, 380, 460): p.can(x, 250, GOLD, "", 50, 90); p.d.rectangle([x - 25, 150, x + 25, 172], fill=WHITE, outline=INK, width=4)
    p.sign("PUT IT ON MINE!", 256, 40, WHITE, 28, False); p.save("round")
    p = Pic((70, 70, 80)); p.d.rounded_rectangle([60, 60, 452, 280], 20, fill=(40, 40, 46), outline=INK, width=6); p.paper(200, 170, "MARCH", 150, 90, (215, 240, 215)); p.d.rectangle([310, 150, 400, 240], fill=WHITE, outline=INK, width=4); p.text("napkins", 355, 195, 18); p.save("glovebox")
    p = Pic((230, 235, 245)); p.phone(150, 160, [("PAY", INK), ("$80", GREEN), ("to: ???", RED)]); p.person(370, 306, ROB, arms="up"); p.text("?!", 440, 60, 60, RED); p.save("venmo")
    p = Pic(SKY, GRASS); p.ladder(60, 250); p.d.rectangle([200, 190, 420, 206], fill=(190, 140, 70), outline=INK, width=4)
    for i in range(5): p.sign(str(i + 1), 150 + i * 70, 110 - (i % 2) * 30, WHITE, 24)
    p.text("0 finished", 256, 280, 34, WHITE, INK); p.save("projects")
    p = Pic((30, 40, 60)); p.d.rectangle([60, 40, 300, 170], fill=INK, outline=WHITE, width=6); p.text("REDS 2", 180, 80, 40, GOLD); p.text("THEM 9", 180, 132, 40, WHITE); p.person(400, 306, ROB); p.can(310, 290, GOLD, "", 44, 70); p.save("reds")
    p = Pic((250, 230, 240)); p.phone(150, 160, [("Jules", (150, 90, 220)), ("k", INK)]); p.person(370, 306, ROB, arms="up")
    for x, y in ((290, 50), (450, 70), (460, 180)): p.burst(x, y, 22, GOLD, 5)
    p.save("k")
    p = Pic(SKY, GRASS); p.house(40, 240, 220, 110); p.ladder(280, 240, 170, 20); p.person(400, 306, ROB, 150, arms="up"); p.dots(20, (200, 90, 30), 8, 7, (30, 100, 270, 135)); p.save("gutters")
    p = Pic(SKY, (150, 150, 155)); p.truck(150, 270, RED, 1.6); p.burst(220, 150, 90, (190, 240, 120)); p.text("SPARKLY", 256, 50, 40, WHITE, INK); p.save("spill")
    p = Pic((30, 20, 60)); p.can(150, 280, GREEN, "ZAP", 110, 190, 40); p.person(370, 306, ROB, 190, arms="up")
    for x, y in ((280, 60), (460, 90), (300, 200)): p.d.polygon([(x, y), (x - 16, y + 40), (x, y + 40), (x - 10, y + 76), (x + 22, y + 30), (x + 6, y + 30), (x + 16, y)], fill=GOLD, outline=INK)
    p.text("9 PM", 150, 44, 38, WHITE); p.save("energy")
    p = Pic(SKY, GRASS); p.critter(150, 220, 1.6, (170, 110, 60), False, True); p.person(380, 306, ROB, arms="out"); p.text("OOH", 380, 60, 54, WHITE, INK); p.save("squirrel")
    p = Pic(SKY, GRASS)
    for i in range(7): p.ladder(20 + i * 62, 270, 150 + (i % 3) * 20, 14)
    p.sign("$10 EACH", 256, 50, GOLD, 34, False); p.save("garage")
    p = Pic((250, 225, 200), (190, 150, 110)); p.d.rounded_rectangle([150, 150, 360, 270], 12, fill=BLUE, outline=INK, width=6); p.d.rectangle([140, 130, 370, 158], fill=WHITE, outline=INK, width=6); p.text("THE GOOD ONE", 256, 210, 24, WHITE); p.text("fine. keep it.", 256, 60, 38, INK); p.save("cooler")
    p = Pic(SKY, GRASS); p.d.polygon([(40, 250), (120, 250), (150, 170), (130, 170), (110, 230), (40, 230)], fill=RED, outline=INK); p.text("11 sec", 95, 140, 30, WHITE, INK)
    p.person(340, 306, ROB, arms="out"); p.d.rectangle([410, 250, 480, 290], fill=GREEN, outline=INK, width=5); p.d.line([(406, 210), (440, 250)], fill=INK, width=8); p.save("relax")
    p = Pic(SKY, GRASS); p.house(250, 240, 210, 120); p.person(150, 306, ROB, arms="out"); p.text("KNOCK", 250, 50, 48, WHITE, INK); p.text("KNOCK", 330, 96, 48, GOLD, INK); p.save("rentsdue")
    p = Pic(SKY, GRASS); p.house(40, 240, 190, 110); p.person(350, 306, TODD, shirt=(60, 70, 90), arms="out"); p.paper(450, 200, "CODE", 100, 130); p.sign("VIOLATION", 256, 40, RED, 34, False, WHITE); p.save("tipoff")
    p = Pic(SKY, GRASS); p.house(260, 240, 200, 110); p.env(210, 250, .9); p.person(110, 306, JACK, shirt=(46, 158, 91), arms="out"); p.text("YOINK", 256, 44, 52, GOLD, INK); p.save("poach")
    p = Pic((235, 225, 200)); p.burst(150, 160, 110, GOLD, 5); p.text("SHERIFF", 150, 160, 28); p.house(310, 280, 150, 100); p.sign("SOLD", 385, 110, RED, 40, False, WHITE); p.save("sheriff")
    p = Pic((240, 235, 225)); p.person(110, 306, ROB, arms="out"); p.d.rounded_rectangle([190, 30, 480, 170], 30, fill=WHITE, outline=INK, width=6); p.text("...and THEN,", 335, 76, 34); p.text("so anyway...", 335, 126, 34)
    p.person(400, 306, SAM, 120, shirt=BLUE); p.text("zzz", 462, 200, 34, BLUE); p.save("story")
    p = Pic((60, 60, 70)); p.person(256, 310, ROB, 210, shirt=INK, arms="out"); p.d.rounded_rectangle([196, 120, 316, 150], 8, fill=INK); p.sign("NO PERMITS", 256, 40, GOLD, 32, False); p.save("guy")
    # ---- tenant trouble
    p = Pic((200, 200, 205), (150, 150, 155)); p.burst(256, 150, 130, ORANGE); p.burst(256, 150, 70, GOLD); p.d.rounded_rectangle([216, 90, 296, 250], 20, fill=(230, 230, 235), outline=INK, width=6); p.text("BOOM", 256, 40, 50, WHITE, INK); p.save("heater")
    p = Pic((225, 215, 195))
    rnd = random.Random(2)
    for i in range(60): x, y = rnd.randint(60, 450), rnd.randint(150, 290) ; p.d.ellipse([x - 22, y - 12, x + 22, y + 12], fill=(200, 200, 210), outline=INK, width=3)
    p.text("ALL. QUARTERS.", 256, 70, 44, INK); p.save("quarters")
    p = Pic((250, 225, 200)); p.env(256, 190, 2.4)
    for i in range(4): p.drum(150 + i * 70, 130, 1.0, 250 + i * 12)
    p.text("RENT", 256, 250, 36, RED); p.save("paidwings")
    p = Pic(DUSK); p.d.polygon([(40, 300), (256, 60), (472, 300)], fill=(190, 150, 110), outline=INK); p.d.line([(40, 300), (256, 60), (472, 300)], fill=INK, width=6)
    for x, y, s in ((256, 180, 1.2), (170, 250, 1.0), (345, 250, 1.0)): p.critter(x, y, s)
    p.save("raccoons")
    p = Pic(SKY, GRASS); p.house(150, 240, 210, 130, (240, 90, 190), (60, 200, 180))
    for x, y, c in ((190, 150, GOLD), (300, 210, GREEN), (330, 140, ORANGE)): p.burst(x, y, 20, c, 6)
    p.text("...huh. nice?", 256, 290, 34, WHITE, INK); p.save("improved")
    p = Pic((235, 225, 205), (180, 150, 120)); p.d.rounded_rectangle([60, 150, 330, 250], 20, fill=(150, 110, 170), outline=INK, width=6); p.d.rounded_rectangle([60, 110, 330, 180], 20, fill=(170, 130, 190), outline=INK, width=6)
    p.d.rounded_rectangle([350, 236, 480, 266], 15, fill=(230, 220, 200), outline=INK, width=5); p.d.ellipse([462, 228, 496, 262], fill=(230, 220, 200), outline=INK, width=5); p.text("ferret included", 256, 50, 36); p.save("skipped")
    p = Pic((235, 235, 240)); p.paper(170, 160, "INSPECT", 190, 230)
    for i in range(4): p.text("X", 110, 132 + i * 22, 24, RED)
    p.person(390, 306, TODD, shirt=(60, 70, 90)); p.save("inspect")
    p = Pic((250, 235, 215)); p.d.ellipse([70, 180, 330, 280], fill=WHITE, outline=INK, width=6)
    for x, y in ((140, 215), (200, 235), (260, 212), (200, 195)): p.d.ellipse([x - 34, y - 22, x + 34, y + 22], fill=(200, 150, 90), outline=INK, width=4); p.dots(4, (90, 50, 30), x, 4, (x - 18, y - 8, x + 18, y + 8))
    p.env(400, 120, 1.0); p.bill(400, 100, .7); p.text("EARLY!", 190, 80, 48, GREEN, INK); p.save("cookies")
    p = Pic(SKY, (150, 150, 155)); p.house(20, 236, 150, 100); p.d.polygon([(200, 230), (470, 230), (430, 280), (240, 280)], fill=WHITE, outline=INK); p.d.line([(200, 230), (470, 230), (430, 280), (240, 280), (200, 230)], fill=INK, width=6)
    p.d.line([(330, 230), (330, 110)], fill=INK, width=8); p.d.polygon([(338, 116), (338, 210), (420, 210)], fill=RED, outline=INK); p.text("month 6", 380, 60, 36, WHITE, INK); p.save("cousin")
    p = Pic((225, 215, 195)); p.paper(256, 170, "TAX BILL", 230, 240); p.text("$$$", 256, 250, 50, RED); p.save("tax")
    p = Pic(SKY, GRASS); p.house(220, 240, 180, 110, dark=True); p.truck(360, 290, BLUE, 1.0); p.person(110, 306, ROB, arms="out"); p.text("knock #14", 256, 40, 40, WHITE, INK); p.save("nobody")
    p = Pic((230, 235, 245)); p.phone(256, 160, [("HOW TO FIX", INK), ("A FURNACE", INK), ("(12 min)", RED)]); p.d.polygon([(420, 120), (420, 200), (490, 160)], fill=RED, outline=INK); p.save("furnace")
    p = Pic(SKY, GRASS, 150); p.pool(256, 230, (120, 190, 70)); p.person(420, 200, ROB, 150, arms="up"); p.can(80, 160, WHITE, "CL", 60, 90, 30); p.text("FINALLY", 200, 50, 46, WHITE, INK); p.save("pool")
    p = Pic((250, 235, 215)); p.env(256, 180, 2.2); p.sign("LATE", 256, 190, RED, 54, False, WHITE); p.save("latefees")
    p = Pic((235, 225, 200)); p.d.line([(0, 200), (230, 160)], fill=INK, width=64); p.d.line([(0, 200), (230, 160)], fill=RED, width=52); p.d.line([(512, 200), (282, 160)], fill=INK, width=64); p.d.line([(512, 200), (282, 160)], fill=BLUE, width=52)
    p.d.ellipse([206, 120, 306, 200], fill=(240, 200, 170), outline=INK, width=6); p.text("no paperwork", 256, 60, 40); p.save("handshake")
    # ---- squares
    p = Pic(SKY, (150, 150, 155)); p.d.rectangle([20, 60, 300, 236], fill=BLUE, outline=INK, width=6); p.text("HARDWARE", 160, 110, 36, WHITE); p.person(400, 306, ROB, arms="out"); p.d.rectangle([330, 230, 390, 290], fill=GOLD, outline=INK, width=5); p.text("1 washer", 160, 180, 30, WHITE); p.save("lowes")
    p = Pic((250, 230, 240)); p.phone(256, 160, [("Jules", (150, 90, 220)), ("calling...", INK)]); f = round_face(JULES, 110); p.im.paste(f, (60, 100), f); f = round_face(ROB, 110); p.im.paste(f, (350, 100), f); p.save("jules")
    p = Pic((250, 230, 240)); p.phone(256, 160, [("Jules", (150, 90, 220)), ("voicemail", RED), ("4:07", INK)]); f = round_face(ROB, 110); p.im.paste(f, (350, 100), f); p.save("voicemail")
    p = Pic(SKY, GRASS); p.house(50, 240, 220, 120, (235, 215, 225)); p.person(390, 306, ROB, arms="out"); p.text("was in the", 256, 36, 32, WHITE, INK); p.text("neighborhood", 256, 76, 32, WHITE, INK); p.save("ex")
    p = Pic(GOLD); p.can(256, 290, (150, 90, 50), "COFFEE", 190, 200, 38)
    for x, y in ((150, 70), (256, 44), (360, 70), (110, 150), (400, 150)): p.bill(x, y, .8)
    p.save("can")
    p = Pic(SKY, GRASS); p.house(150, 250, 210, 130, dark=True); p.sign("BANK'S NOW", 256, 60, (91, 107, 122), 40, False, WHITE); p.save("bank")
    p = Pic((235, 225, 200)); p.paper(256, 170, "I.O.U.", 260, 200, (255, 250, 220)); p.text("$100", 256, 190, 60, RED); p.text("- Rob", 330, 244, 26, INK); p.save("iou")
    # ---- wing jail, one per character
    for ch in range(6):
        p = Pic((230, 120, 40)); f = round_face(ch, 190); p.im.paste(f, (161, 60), f)
        for x in range(96, 440, 54): p.d.rounded_rectangle([x - 10, 10, x + 10, 310], 10, fill=(150, 205, 110), outline=INK, width=4)
        p.drum(60, 250, 1.1, 300); p.drum(455, 250, 1.1, 240); p.d.rectangle([60, 250, 452, 310], fill=INK); p.text("WING JAIL", 256, 280, 44, GOLD); p.save("jail_%d" % ch)
    # ---- how-to-play pages
    p = Pic(SKY, GRASS); p.house(40, 236); p.house(200, 236, wall=(215, 235, 245)); p.house(360, 236, wall=(245, 215, 215)); p.env(100, 262, .5); p.env(260, 262, .5); p.env(420, 262, .5); p.truck(180, 312, RED, .9); p.save("tut_rent")
    p = Pic((31, 111, 92))
    for i, f in enumerate((ROB, JULES, SAM, JACK)): p.person(80 + i * 118, 300, f, 180, shirt=((208, 52, 44), (155, 93, 229), (45, 125, 210), (46, 158, 91))[i], arms="up" if i % 2 == 0 else "down")
    p.save("tut_cast")
    p = Pic((31, 111, 92)); p.d.rounded_rectangle([110, 100, 230, 220], 18, fill=WHITE, outline=INK, width=6); p.d.rounded_rectangle([280, 100, 400, 220], 18, fill=RED, outline=INK, width=6)
    for x, y in ((140, 130), (170, 160), (200, 190)): p.d.ellipse([x - 12, y - 12, x + 12, y + 12], fill=INK)
    for x, y in ((310, 130), (370, 130), (310, 190), (370, 190)): p.d.ellipse([x - 12, y - 12, x + 12, y + 12], fill=WHITE)
    p.save("tut_dice")
    p = Pic((31, 111, 92)); p.house(40, 230, 110, 80); p.house(362, 230, 110, 80, wall=(215, 235, 245)); p.d.line([(170, 150), (340, 150)], fill=GOLD, width=12); p.d.polygon([(340, 130), (340, 170), (372, 150)], fill=GOLD); p.d.line([(340, 210), (170, 210)], fill=WHITE, width=12); p.d.polygon([(170, 190), (170, 230), (138, 210)], fill=WHITE); p.text("DEAL?", 256, 60, 50, WHITE, INK); p.save("tut_trade")

if __name__ == "__main__":
    os.makedirs(os.path.join(OUT, "pic"), exist_ok=True)
    make_faces(); make_pics()
    print(len(os.listdir(os.path.join(OUT, "pic"))), "pictures")


def make_title():
    """The picture on the main menu. Drawn on a double-size canvas so there's room for everybody."""
    p = Pic(SKY)
    p.im = Image.new("RGB", (1024, 640), SKY); p.d = ImageDraw.Draw(p.im)
    p.d.ellipse([820, 30, 940, 150], fill=GOLD, outline=INK, width=5)
    p.d.rectangle([0, 400, 1024, 640], fill=GRASS)
    p.d.rectangle([0, 520, 1024, 640], fill=(150, 150, 155))
    p.tower(70, 410)
    for i, f in enumerate((JULES, None, SAM, None, JACK)):          # five identical brick ranches, three with someone at the window
        x = 330 + i * 140
        p.house(x, 410, 130, 90, (190, 95, 75), (90, 60, 50))
        if f is not None: fc = round_face(f, 44); p.im.paste(fc, (x + 15, 328), fc)
    p.sign("FOR RENT", 535, 296, WHITE, 20); p.sign("ALSO FOR RENT", 815, 296, WHITE, 18)
    p.truck(560, 590, RED, 2.5)
    for i in range(7): p.env(600 + i * 26, 470 - (i % 2) * 14, 0.5)
    p.drum(640, 440, 0.9, 250); p.drum(700, 446, 0.9, 290)
    p.critter(800, 388, 0.8, (170, 110, 60), False, True)            # squirrel on the cab
    p.person(200, 610, ROB, 360, RED, "up", 96)
    p.env(134, 300, 0.7); p.drum(266, 296, 1.0, 280)                 # rent in one hand, a wing in the other
    p.critter(60, 590, 1.0)                                          # raccoon
    p.can(470, 610, (150, 90, 50), "COFFEE", 70, 86, 16); p.bill(470, 506, 0.6)
    p.im.resize((512, 320), Image.LANCZOS).save(os.path.join(OUT, "pic", "title.png"))

make_title()
