"""Synthesizes the sound effects into assets/sfx/*.wav. Needs numpy.   python3 tools/make_sfx.py
   Or drop your own .wav with the same name into assets/sfx/."""
import os, wave
import numpy as np

SR = 22050
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "assets", "sfx")
rng = np.random.default_rng(7)

def t(sec): return np.arange(int(SR * sec)) / SR
def env(x, a=0.005, r=0.08):
    n = len(x); e = np.ones(n); na, nr = min(int(SR * a), n // 2), min(int(SR * r), n // 2)
    e[:na] = np.linspace(0, 1, na); e[-nr:] = np.linspace(1, 0, nr)
    return x * e
def tone(f, sec, shape="sine", decay=0.0):
    x = t(sec); ph = 2 * np.pi * f * x
    y = {"sine": np.sin(ph), "square": np.sign(np.sin(ph)) * 0.5, "saw": (2 * ((f * x) % 1) - 1) * 0.6,
         "bell": np.sin(ph) + 0.5 * np.sin(2.76 * ph) + 0.25 * np.sin(5.4 * ph)}[shape]
    if decay: y = y * np.exp(-decay * x)
    return env(y)
def slide(f0, f1, sec, shape="saw"):
    x = t(sec); f = np.linspace(f0, f1, len(x)); ph = 2 * np.pi * np.cumsum(f) / SR
    return env(np.sin(ph) if shape == "sine" else (2 * ((ph / (2 * np.pi)) % 1) - 1) * 0.6)
def noise(sec, decay=30.0): return env(rng.uniform(-1, 1, int(SR * sec)) * np.exp(-decay * t(sec)), 0.001, 0.01)
def seq(*parts): return np.concatenate(parts)
def rest(sec): return np.zeros(int(SR * sec))
def mix(*parts):
    n = max(len(p) for p in parts); y = np.zeros(n)
    for p in parts: y[:len(p)] += p
    return y
def save(name, y, vol=0.7):
    y = y / max(1e-9, np.max(np.abs(y))) * vol
    with wave.open(os.path.join(OUT, name + ".wav"), "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes((y * 32767).astype(np.int16).tobytes())

os.makedirs(OUT, exist_ok=True)
save("dice", seq(*[seq(noise(0.035, 60) * (0.5 + 0.5 * rng.random()), rest(0.02 + 0.05 * rng.random())) for _ in range(11)], noise(0.08, 25)))
save("hop", tone(520, 0.05, "sine", 30), 0.35)
save("coin", seq(tone(1320, 0.07, "bell", 20), tone(1760, 0.22, "bell", 14)), 0.5)
save("card", noise(0.22, 9) * np.linspace(0.2, 1, int(SR * 0.22)), 0.45)
save("buy", seq(tone(880, 0.09, "bell", 12), tone(1175, 0.09, "bell", 12), tone(1760, 0.45, "bell", 7)))
save("up", seq(*[tone(f, 0.08, "square", 6) for f in (523, 659, 784, 1047)], tone(1319, 0.25, "square", 8)), 0.45)
save("lose", seq(slide(294, 277, 0.3), slide(277, 262, 0.3), slide(262, 247, 0.3), slide(247, 196, 0.8)), 0.55)
save("jail", seq(tone(140, 0.35, "square"), rest(0.05), slide(600, 300, 0.25), slide(560, 280, 0.25), slide(520, 180, 0.7)), 0.6)
save("pop", slide(300, 900, 0.07, "sine"), 0.4)
save("click", tone(700, 0.03, "square", 40), 0.3)
save("turn", seq(tone(784, 0.12, "bell", 10), tone(1047, 0.4, "bell", 7)), 0.5)
save("win", seq(*[tone(f, 0.13, "square", 4) for f in (523, 659, 784)], tone(1047, 0.3, "square", 3), tone(784, 0.12, "square", 4), tone(1047, 0.7, "square", 3)), 0.5)
save("hood", seq(*[tone(f, 0.1, "bell", 6) for f in (523, 659, 784, 1047)], mix(tone(1047, 0.7, "bell", 4), tone(1319, 0.7, "bell", 4), tone(1568, 0.7, "bell", 4))), 0.55)
save("deal", seq(tone(659, 0.25, "bell", 6), tone(523, 0.6, "bell", 5)), 0.55)
save("no", seq(tone(150, 0.12, "saw"), rest(0.04), tone(120, 0.25, "saw")), 0.45)
print(sorted(os.listdir(OUT)))
