# Contributing

Thanks for helping improve TimerButton.

## Development

```bash
./gradlew check
```

Focused library tests:

```bash
./gradlew :timerbutton:testDebugUnitTest
```

Local Maven publication check:

```bash
./gradlew :timerbutton:publishToMavenLocal
```

## Guidelines

- Keep the public API small and documented.
- Prefer shared behavior in the internal timer engine over duplicated Compose/XML logic.
- Add or update tests for state-machine behavior.
- Keep README examples short and copy-pasteable.
- Put deeper notes in `docs/wiki/` rather than expanding the README indefinitely.

## Release Checklist

1. Update the version in `timerbutton/build.gradle.kts`.
2. Update `CHANGELOG.md`.
3. Run `./gradlew check`.
4. Run `./gradlew :timerbutton:publishToMavenLocal`.
5. Publish with `./gradlew publishAndReleaseToMavenCentral`.
6. Confirm the artifact is available on Maven Central.
