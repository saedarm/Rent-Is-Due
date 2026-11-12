"""Builds the two bitmap fonts in assets/. Needs Pillow.
   python3 tools/make_font.py [path/to/Font.ttf]"""
import sys, os
from PIL import Image, ImageDraw, ImageFont

ttf = sys.argv[1] if len(sys.argv) > 1 else "/usr/share/fonts/truetype/google-fonts/Poppins-Bold.ttf"
out = os.path.join(os.path.dirname(__file__), "..", "assets")
chars = [chr(c) for c in range(32, 127)]

def build(name, size, dim):
    font = ImageFont.truetype(ttf, size)
    asc, desc = font.getmetrics()
    img = Image.new("L", (dim, dim), 0)
    d = ImageDraw.Draw(img)
    x = y = 2; row = 0; lines = []
    for ch in chars:
        l, t, r, b = font.getbbox(ch)
        w, h = max(r - l, 1), max(b - t, 1)
        if x + w + 2 > dim: x = 2; y += row + 2; row = 0
        d.text((x - l, y - t), ch, font=font, fill=255)
        lines.append(f"char id={ord(ch)} x={x} y={y} width={w} height={h} xoffset={l} yoffset={t} "
                     f"xadvance={round(font.getlength(ch))} page=0 chnl=0")
        x += w + 2; row = max(row, h)
    assert y + row < dim, "atlas too small"
    rgba = Image.merge("RGBA", (Image.new("L", img.size, 255),) * 3 + (img,))
    rgba.save(os.path.join(out, name + ".png"))
    with open(os.path.join(out, name + ".fnt"), "w") as f:
        f.write(f'info face="{name}" size={size} bold=1 italic=0 charset="" unicode=0 stretchH=100 smooth=1 aa=1 padding=0,0,0,0 spacing=0,0\n')
        f.write(f"common lineHeight={asc + desc} base={asc} scaleW={dim} scaleH={dim} pages=1 packed=0\n")
        f.write(f'page id=0 file="{name}.png"\n')
        f.write(f"chars count={len(lines)}\n" + "\n".join(lines) + "\n")

build("big", 56, 512)
build("small", 24, 256)
