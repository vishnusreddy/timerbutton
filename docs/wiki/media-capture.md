# Media Capture

The repository includes a script for regenerating README screenshots from a connected Android device:

```bash
python3 scripts/capture_readme_media.py
```

It launches the sample app, taps visible timers, captures PNG screenshots, crops device bars, and writes:

- `docs/media/screenshots/showcase-running.png`
- `docs/media/screenshots/directions-running.png`
- `docs/media/screenshots/multiple-and-xml-running.png`
- component crops in `docs/media/components/`

The script requires Pillow. Local scratch captures belong in `artifacts/`, which is ignored by git.
