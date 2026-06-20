# Roadmap

TimerButton is intentionally focused: a polished timer-aware button for Compose and XML apps. Planned work should keep that focus.

## Upcoming

- Compose previews for common recipes.
- Optional formatted duration helpers, such as `mm:ss`.
- Public runtime styling setters for `TimerButtonView`.
- More sample screens for OTP, retry, upload, and confirmation flows.
- UI tests for Compose and XML rendering behavior.
- Dokka-generated API docs.

## Under Consideration

- Optional haptic feedback hooks.
- Saved-state helpers for process-death restoration.
- Material motion presets for progress transitions.
- Compose Multiplatform feasibility.

## Not Planned

- Server-enforced cooldown logic.
- Background timers or alarm scheduling.
- Network retry orchestration.
- App-specific OTP, auth, billing, or policy enforcement.

TimerButton should render timer state beautifully; application rules should stay in application code.
