"""Builds one labelled contact sheet per system from $DEVICE_TEST_OUT/results.csv.

usage: python -I -X utf8 contactsheet.py [system ...]
Writes $DEVICE_TEST_OUT/sheet-<system>.png (5 columns, cropped to the game area).
"""

import csv
import os
import sys

from PIL import Image, ImageDraw, ImageFont

SHOTS = os.environ.get("DEVICE_TEST_OUT", os.path.join(os.path.dirname(os.path.abspath(__file__)), "out"))
COLORS = {"OK": (60, 170, 90), "DARK": (220, 160, 40)}


def font(size):
    for name in ("C:/Windows/Fonts/msyh.ttc", "C:/Windows/Fonts/arial.ttf"):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def sheet(system, rows):
    folder = system.replace(",", "+")
    tile_w, tile_h, label_h, cols = 320, 300, 44, 5
    count = len(rows)
    lines = (count + cols - 1) // cols
    out = Image.new("RGB", (cols * tile_w, lines * (tile_h + label_h)), (24, 24, 28))
    draw = ImageDraw.Draw(out)
    small = font(15)
    for i, row in enumerate(rows):
        try:
            im = Image.open(f"{SHOTS}/{folder}/{int(row['index']):02d}.png").convert("RGB")
        except OSError:
            continue
        w, h = im.size
        crop = im.crop((0, int(h * 0.04), w, int(h * 0.58))) if h > w else im
        crop.thumbnail((tile_w - 8, tile_h - 8))
        x = (i % cols) * tile_w
        y = (i // cols) * (tile_h + label_h)
        out.paste(crop, (x + (tile_w - crop.width) // 2, y + 4))
        color = COLORS.get(row["status"], (220, 60, 60))
        draw.rectangle([x + 4, y + tile_h, x + tile_w - 4, y + tile_h + label_h - 4], fill=color)
        title = row["title"][:30]
        draw.text((x + 10, y + tile_h + 4), f"{row['status']}  {title}", fill=(255, 255, 255), font=small)
        draw.text((x + 10, y + tile_h + 22), row["file"][:34], fill=(235, 235, 235), font=small)
    path = f"{SHOTS}/sheet-{folder}.png"
    out.save(path)
    return path


def main():
    rows = list(csv.DictReader(open(f"{SHOTS}/results.csv", encoding="utf-8")))
    systems = sys.argv[1:] or sorted({r["system"] for r in rows}, key=lambda s: [r["system"] for r in rows].index(s))
    for system in systems:
        selected = [r for r in rows if r["system"] == system]
        if selected:
            print(sheet(system, selected))


if __name__ == "__main__":
    main()
