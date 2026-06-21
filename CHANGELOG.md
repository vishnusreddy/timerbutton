# Changelog

## Unreleased

- Improved Compose label overflow handling.
- Reworked documentation into focused usage docs, wiki-ready deep dives, roadmap, contribution, and security notes.
- Removed generated Android Studio sample tests from the demo app.

## 0.2.0 - 2026-06-21

- Split publishing into `timerbutton-core`, `timerbutton-compose`, and `timerbutton-view`.
- Kept `timerbutton` as a compatibility bundle for apps that want both Compose and XML APIs from one dependency.
- Updated README usage docs and Maven Central descriptions for the split artifacts.

## 0.1.0 - 2026-06-20

- Initial Maven Central release of TimerButton.
- Compose `TimerButton` and `rememberTimerButtonState` APIs.
- XML/View `TimerButtonView` API.
- Shared timer engine, lifecycle callbacks, progress directions, and progress modes.
