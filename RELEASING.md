# Releasing new app versions

The process to release a new version of the app is fairly simple. There are some things to keep in
mind though. This document explains the complete process for a release from the start to end.

## Version names

Each release should follow the [Semantic Versioning] standard. The version code used by the app is
automatically generated from this. For pre-releases (beta versions) the idenfifier should **always**
be "beta" followed by an incremental number. The version code generation does not support different
pre-release types. An example release version order could be: 0.13.0-beta.1, 0.13.0-beta.2, 0.13.0,
0.13.1

## Beta & Patch releases

In normal circumstances the beta releases are only used for minor releases like 0.13.0 and not for
patch releases like 0.13.1.

## Release process

1. Create release branch (release-x.y.z, where x and y are to be replaced with the major and minor
   version).
    1. If the branch is already made in a previous release, update the branch by rebasing it on top
       of master (beta, minor) or cherrypick commits into it (patch).
2. Write a changelog
    1. Start with [nielsvanvelzen/jellyfin-changelog] to generate the changelog.
    2. For beta releases add the "beta information" section:
       ```markdown
       :bug: **Beta information**
       
       Beta versions are not guaranteed to work and may not work as expected. We encourage users to create detailed bug reports if any problems arise. Read our [blog post](https://jellyfin.org/posts/android-betas/) for more information about our Android beta programs. Important to know is that our beta releases are only available on Google Play.
       ```
    3. Add a paragraph about the update explaining new features or significant changes.
3. Write a blog post if this is a minor release
    - Should be made ahead of time so it can be published immediatly after release
    - Submit as pull request to the jellyfin-blog repository
4. Create GitHub release
    - Tag and release title should be the version starting with a "v" (like v0.13.0-beta.2).
    - Description should be the changelog from step 2.
    - Do not mark as pre-release
    - Create discussion for minor releases only
5. Upload Android App bundle to Google Play store
6. Upload Android Release APK to Amazon Appstore

[Semantic Versioning]: https://semver.org/spec/v2.0.0.html

[nielsvanvelzen/jellyfin-changelog]: https://github.com/nielsvanvelzen/jellyfin-changelog
