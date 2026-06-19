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
GIFS = OUT / "gifs"
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


def swipe_up() -> None:
    shell("input swipe 700 2850 700 900 650")
    time.sleep(0.55)


def crop_frame(image: Image.Image, crop: tuple[int, int, int, int]) -> Image.Image:
    frame = image.crop(crop)
    max_width = 720
    if frame.width > max_width:
        ratio = max_width / frame.width
        frame = frame.resize((max_width, int(frame.height * ratio)), Image.Resampling.LANCZOS)
    return frame


def save_gif(
    name: str,
    crop: tuple[int, int, int, int],
    tap_point: tuple[int, int],
    *,
    pre_scrolls: int = 0,
    frames: int = 9,
    interval: float = 0.42,
) -> None:
    launch()
    for _ in range(pre_scrolls):
        swipe_up()
    tap(*tap_point)
    captured: list[Image.Image] = []
    for _ in range(frames):
        captured.append(crop_frame(screenshot(), crop))
        time.sleep(interval)

    gif_path = GIFS / f"{name}.gif"
    captured[0].save(
        gif_path,
        save_all=True,
        append_images=captured[1:],
        duration=int(interval * 1000),
        loop=0,
        optimize=True,
    )
    captured[min(2, len(captured) - 1)].save(COMPONENTS / f"{name}.png")


def save_still(name: str, crop: tuple[int, int, int, int], *, pre_scrolls: int = 0) -> None:
    launch()
    for _ in range(pre_scrolls):
        swipe_up()
    crop_frame(screenshot(), crop).save(COMPONENTS / f"{name}.png")


def main() -> None:
    GIFS.mkdir(parents=True, exist_ok=True)
    COMPONENTS.mkdir(parents=True, exist_ok=True)

    save_gif(
        "primary-progress",
        crop=(60, 675, 1380, 915),
        tap_point=(720, 790),
    )
    save_gif(
        "otp-progress",
        crop=(60, 945, 770, 1155),
        tap_point=(310, 1045),
    )
    save_gif(
        "vertical-progress",
        crop=(65, 1635, 795, 1855),
        tap_point=(420, 1725),
        pre_scrolls=2,
    )
    save_still(
        "xml-button",
        crop=(120, 2660, 1320, 3020),
        pre_scrolls=3,
    )


if __name__ == "__main__":
    main()
