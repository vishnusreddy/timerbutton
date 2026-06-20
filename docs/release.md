# Release Guide

TimerButton publishes to Maven Central as:

```kotlin
implementation("com.goeslocal:timerbutton:0.1.0")
```

## One-Time Setup

1. Create a Central Portal account at <https://central.sonatype.com/>.
2. Register and verify the `com.goeslocal` namespace with the DNS `TXT` record Central gives you.
3. Generate a Central Portal user token.
4. Install GPG, create a signing key, and publish the public key to a keyserver.

```bash
brew install gnupg
gpg --full-generate-key
gpg --list-secret-keys --keyid-format LONG
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
gpg --armor --export-secret-keys <KEY_ID>
```

## Local Secrets

Put secrets in `~/.gradle/gradle.properties`, not in this repository:

```properties
mavenCentralUsername=<central-token-username>
mavenCentralPassword=<central-token-password>
signingInMemoryKey=<ascii-armored-private-key>
signingInMemoryKeyPassword=<gpg-key-password>
```

## GitHub Actions Secrets

For tag-based publishing, add these repository secrets:

```text
MAVEN_CENTRAL_USERNAME
MAVEN_CENTRAL_PASSWORD
SIGNING_KEY
SIGNING_PASSWORD
```

## Verify Locally

```bash
./gradlew clean :timerbutton:testDebugUnitTest :timerbutton:assembleRelease :timerbutton:publishToMavenLocal
```

## Publish Manually

```bash
./gradlew publishAndReleaseToMavenCentral
```

Maven Central can take 10 to 30 minutes before a published artifact is available.

## Publish From GitHub Actions

Update the version and `CHANGELOG.md`, commit the release, then tag it:

```bash
git tag v<version>
git push origin v<version>
```

Do not create a tag for a version that has already been published to Maven Central. Maven Central versions are immutable.
