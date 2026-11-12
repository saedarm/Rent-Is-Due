"""Synthesizes the background loop into assets/music/loop.mp3. Needs numpy and ffmpeg.   python3 tools/make_music.py
   Or drop your own loop.mp3 into assets/music/."""
import os, subprocess, wave
import numpy as np

SR = 22050
BPM = 116
BEAT = 60 / BPM
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "assets", "music")
NOTE = {n: i for i, n in enumerate(["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"])}
def hz(name):  # "E4"
    return 440 * 2 ** ((NOTE[name[:-1]] + 12 * (int(name[-1]) + 1) - 69) / 12)

rng = np.random.default_rng(3)
def voice(f, beats, shape, vol, decay):
    n = int(SR * BEAT * beats); x = np.arange(n) / SR; ph = 2 * np.pi * f * x
    y = {"tri": 2 / np.pi * np.arcsin(np.sin(ph)), "sq": np.sign(np.sin(ph)) * 0.5 + 0.2 * np.sin(ph), "sine": np.sin(ph)}[shape]
    e = np.exp(-decay * x); a = min(n, int(SR * 0.008)); e[:a] *= np.linspace(0, 1, a); r = min(n, int(SR * 0.03)); e[-r:] *= np.linspace(1, 0, r)
    return y * e * vol

# four bars of chords, played four times; the tune changes on the repeat
CHORDS = [("C", ["C3", "E3", "G3"]), ("A", ["A2", "C3", "E3"]), ("F", ["F2", "A2", "C3"]), ("G", ["G2", "B2", "D3"])]
TUNE_A = ["E4 G4 A4 G4 E4 G4 C5 -", "E4 A4 C5 A4 E4 A4 B4 -", "F4 A4 C5 A4 F4 A4 D5 C5", "D4 G4 B4 G4 D5 B4 G4 -"]
TUNE_B = ["C5 - G4 E4 G4 - C5 E5", "E5 - C5 A4 C5 - A4 E4", "F4 - A4 C5 F5 - C5 A4", "G4 B4 D5 G5 D5 B4 G4 -"]

bars = 16
song = np.zeros(int(SR * BEAT * 4 * bars) + SR)
def put(y, beat): i = int(SR * BEAT * beat); song[i:i + len(y)] += y
for bar in range(bars):
    root, chord = CHORDS[bar % 4]; b0 = bar * 4
    for k in (0, 2): put(voice(hz(chord[0]) / 2, 0.9, "tri", 0.55, 3), b0 + k)           # oom
    put(voice(hz(chord[2]) / 2, 0.9, "tri", 0.4, 3), b0 + 2)
    for k in (1, 3):                                                                    # pah
        for nme in chord: put(voice(hz(nme), 0.45, "sq", 0.09, 9), b0 + k)
    tune = (TUNE_A if (bar // 4) % 2 == 0 else TUNE_B)[bar % 4].split()
    for i, nme in enumerate(tune):
        if nme != "-": put(voice(hz(nme), 0.5 if i % 2 else 0.6, "tri", 0.30, 5), b0 + i * 0.5)
    for i in range(8):                                                                  # hi-hat
        n = int(SR * 0.03); put(rng.uniform(-1, 1, n) * np.exp(-90 * np.arange(n) / SR) * (0.07 if i % 2 else 0.04), b0 + i * 0.5)
song = song[:int(SR * BEAT * 4 * bars)]
song = song / np.max(np.abs(song)) * 0.8
os.makedirs(OUT, exist_ok=True)
wav = os.path.join(OUT, "loop.wav")
with wave.open(wav, "wb") as w:
    w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes((song * 32767).astype(np.int16).tobytes())
subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", wav, "-codec:a", "libmp3lame", "-b:a", "64k", os.path.join(OUT, "loop.mp3")], check=True)
os.remove(wav)
print("loop.mp3", round(len(song) / SR, 1), "seconds")
