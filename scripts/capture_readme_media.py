from __future__ import annotations

import io
import subprocess
import time
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
ADB = Path("/Users/vishnu/Library/Android/sdk/platform-tools/adb")
PACKAGE = "com.goeslocal.timerbutton"
ACTIVITY = f"{PACKAGE}/.MainActivity"
OUT = ROOT / "docs" / "media"
SCREENSHOTS = OUT / "screenshots"
COMPONENTS = OUT / "components"


def adb(*args: str, capture: bool = False) -> bytes:
    command = [str(ADB), *args]
    if capture:
        return subprocess.check_output(command)
    subprocess.check_call(command)
    return b""


def shell(command: str) -> None:
    adb("shell", command)


def launch() -> None:
    shell(f"am start -S -n {ACTIVITY}")
    time.sleep(1.0)


def screenshot() -> Image.Image:
    data = adb("exec-out", "screencap", "-p", capture=True)
    return Image.open(io.BytesIO(data)).convert("RGBA")


def tap(x: int, y: int) -> None:
    shell(f"input tap {x} {y}")
    time.sleep(0.08)


def swipe_up() -> None:
    shell("input swipe 700 2850 700 900 650")
    time.sleep(0.45)


def crop_phone_screen(image: Image.Image) -> Image.Image:
    # Remove the device status and gesture bars while keeping the app content intact.
    cropped = image.crop((0, 120, image.width, image.height - 115))
    max_width = 720
    ratio = max_width / cropped.width
    return cropped.resize((max_width, int(cropped.height * ratio)), Image.Resampling.LANCZOS)


def save_screenshot(
    name: str,
    *,
    taps: list[tuple[int, int]],
    pre_scrolls: int = 0,
    settle: float = 0.9,
) -> Image.Image:
    launch()
    for _ in range(pre_scrolls):
        swipe_up()
    for x, y in taps:
        tap(x, y)
    time.sleep(settle)

    frame = crop_phone_screen(screenshot())
    frame.save(SCREENSHOTS / f"{name}.png")
    return frame


def save_component(name: str, frame: Image.Image, crop: tuple[int, int, int, int]) -> None:
    frame.crop(crop).save(COMPONENTS / f"{name}.png")


def main() -> None:
    SCREENSHOTS.mkdir(parents=True, exist_ok=True)
    COMPONENTS.mkdir(parents=True, exist_ok=True)

    showcase = save_screenshot(
        "showcase-running",
        taps=[
            (720, 780),
            (335, 1030),
            (360, 1260),
            (580, 1530),
            (230, 1745),
            (780, 1745),
            (300, 1940),
            (250, 2290),
            (400, 2745),
        ],
    )
    save_component("primary-progress", showcase, (36, 250, 684, 324))
    save_component("otp-progress", showcase, (36, 380, 366, 456))

    directions = save_screenshot(
        "directions-running",
        pre_scrolls=2,
        taps=[
            (160, 225),
            (160, 690),
            (430, 1285),
            (530, 1495),
            (420, 1740),
            (550, 2030),
            (400, 2350),
            (400, 2570),
            (400, 2790),
        ],
    )
    save_component("vertical-progress", directions, (36, 730, 390, 860))

    multiple = save_screenshot(
        "multiple-and-xml-running",
        pre_scrolls=4,
        taps=[
            (520, 550),
            (520, 800),
            (420, 1035),
            (280, 1580),
            (450, 1810),
            (370, 2040),
            (720, 2605),
        ],
    )
    save_component("xml-button", multiple, (66, 1190, 654, 1304))


if __name__ == "__main__":
    main()
