# Contributing to Jellyfin for Android TV

Thank you for your interest in contributing to the Jellyfin Android TV project! We welcome all contributions, including bug reports, feature
requests, code, and documentation.

This project is part of the [Jellyfin][jellyfin-website] ecosystem. Please review our [Community Standards][community-standards]
and [LLM Policies][llm-policies] before participating.

## How can I contribute?

### Reporting issues

If you find a bug or have a feature request, please [open an issue][new-issue] on GitHub. Before opening a new issue, please check if it has
already been reported.

**Important:** If you have playback or network-related problems, or need help with configuration, please visit
our [troubleshooting chat][troubleshooting-chat] or [forum][jellyfin-forum] first. Many common issues can be resolved through community
support without needing a formal bug report. Our issue templates require that you discuss the issue in one of these places before opening a
report.

When reporting a bug, please use the appropriate issue template and provide all requested information, including:

- A clear and descriptive title.
- Steps to reproduce the issue.
- Expected and actual behavior.
- Device information (e.g., Nvidia Shield, Sony TV, Fire TV Stick).
- Jellyfin server and Android TV app versions.
- Relevant logs if possible.

### Translating

Translations are managed through [Weblate][weblate]. We cannot accept changes to translation files via GitHub pull requests. Please visit
our Weblate instance to contribute to translations.

### Documentation

The documentation for Jellyfin is available on [our website][jellyfin-website] and lives in a [different repository][docs-repo].
Documentation about the apps code is part of the codebase using comments.

### Code

### Developing code

If you have a larger change in mind, please open an issue first or use our [Android development chat][troubleshooting-chat] so we can
discuss the implementation before you start.

#### Getting started

1. Fork the repository on GitHub.
2. Clone your fork locally.
3. Use [Android Studio][android-studio] to open the project. It includes all required dependencies for development and building.
4. Create a new branch for your changes (e.g., `git checkout -b feature/my-new-feature`).

#### Building

You can build the project using the Gradle wrapper:

```shell
./gradlew assembleDebug
```

The APK will be located in the `/app/build/outputs/apk/debug` directory.

#### Code style and linting

This project uses [Detekt][detekt] for static code analysis. You can run it locally using:

```shell
./gradlew detekt
```

We do not strictly adhere to all Detekt recommendations; it is primarily used as a tool to maintain code quality and consistency.

### Branching strategy

- The `master` branch is the primary development branch and the target for all pull requests.
- `master` is considered **unstable**.
- For production-ready code, refer to the latest `release-x.y.z` branch.
- Maintainers will cherry-pick selected changes from `master` into release branches for patch releases.

### Pull requests

1. Ensure your code compiles and passes all tests.
2. Follow the "Fork and PR" methodology.
3. Target your pull request towards the `master` branch.
4. Provide a clear description of your changes and link any related issues. **Pull requests that do not follow our description template will
   be closed.**
5. Once submitted, a maintainer will review your PR and provide feedback. We appreciate your patience and help in reviewing other pull
   requests while you wait.

## Contributors

See [CONTRIBUTORS.md](CONTRIBUTORS.md) for a list of people who have contributed to this project, and don't forget to add yourself when
contributing!

[jellyfin-website]: https://jellyfin.org

[community-standards]: https://jellyfin.org/docs/general/contributing/community-standards/

[llm-policies]: https://jellyfin.org/docs/general/contributing/llm-policies#llm-code-contributions-to-official-projects

[new-issue]: https://github.com/jellyfin/jellyfin-androidtv/issues

[troubleshooting-chat]: https://jellyfin.org/contact

[jellyfin-forum]: https://forum.jellyfin.org/

[weblate]: https://translate.jellyfin.org/projects/jellyfin-android/jellyfin-androidtv/

[docs-repo]: https://github.com/jellyfin/jellyfin.org/

[android-studio]: https://developer.android.com/studio

[detekt]: https://detekt.dev/
