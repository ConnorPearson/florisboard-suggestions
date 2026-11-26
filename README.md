<img align="left" width="80" height="80"
src=".github/repo_icon.png" alt="App icon">

# FlorisBoard Suggestions [![Contributor Covenant](https://img.shields.io/badge/Contributor%20Covenant-2.1-4baaaa.svg)](CODE_OF_CONDUCT.md)

A personal fork of [FlorisBoard](https://github.com/florisboard/florisboard), the free and open-source keyboard for Android 8.0+ devices. This fork adds on-device word suggestions, an adjustable keyboard height, larger emoji page controls and QWERTY long-press hints.

> [!IMPORTANT]
> This is an unofficial fork. It is not affiliated with or endorsed by the FlorisBoard project, and it is not published to any app store. For the official keyboard, releases and support, please use the [upstream project](https://github.com/florisboard/florisboard).

## What is different from upstream

### Word suggestions
- While you type, the smartbar suggests the most common words that start with the letters you have typed. Tapping a suggestion replaces the partial word and adds a space.
- Suggestions come from a bundled English word-frequency dictionary (about 50,000 words) and are computed entirely on the device.
- Capitalization follows what you typed (`Hel` suggests `Hello`, `HEL` suggests `HELLO`).
- Turn on **Display suggestions** in the app settings. It is off by default.

**Limitations:** English only. This is word completion, not prediction: there is no next-word prediction, no learning from what you type, no autocorrect and no spell checking.

### Keyboard height
- A new **Keyboard height** slider (75% to 150%) in the keyboard settings scales the height of the keyboard rows and the smartbar.

### Emoji page
- Emojis, the emoji grid, and the skin-tone and history popups now scale in proportion to the keyboard font size slider.
- The **ABC** and **backspace** buttons on the emoji page are larger and also scale with the font size slider. The bottom row grows taller when the font size is above 100%.
- The built-in themes were updated to support this: less vertical padding on the bottom row buttons, and a font size added to the themes that had none.

### QWERTY layout
- Long-press hints: the top row shows `1`-`0` and the home row shows `@ # $ % & - + ( )`. Long-press a key to insert the hinted character. The existing accent popups are still available in the same popup.
- The keyboard now loads the QWERTY layout. Upstream's placeholder loader was hardcoded to a QWERTZ test layout. The layout is still fixed and is not chosen by language.

## Building

Install the Android SDK and point `sdk.dir` in `local.properties` at it. The project pins its CMake version in `gradle/tools.versions.toml`, so install that version from the SDK Manager (or adjust it locally).

```
./gradlew assembleDebug            # debug build, app id dev.patrickgold.florisboard.debug
./gradlew assembleBeta             # minified build, app id dev.patrickgold.florisboard.beta (unsigned)
./gradlew :app:testDebugUnitTest   # unit tests
```

There are no prebuilt releases. Builds from this fork are not signed with the official FlorisBoard release key, so the store links, update channels and signing certificate hashes in the upstream README do not apply here. The `beta` build is unsigned, so you have to sign it yourself before it can be installed.

## AI assistance

The changes in this fork were written with an AI coding assistant at the direction of the repository owner. FlorisBoard's [AI policy](AI_POLICY.md) does not allow AI-written code or commit messages in contributions to the upstream project, so these changes are not intended for upstream and should not be submitted there.

## Upstream

Everything not listed above is unchanged from [florisboard/florisboard](https://github.com/florisboard/florisboard). The full list of changes is in this repository's commit history. For the Addons Store, permissions and roadmap, see the upstream documentation: [docs.florisboard.org](https://docs.florisboard.org) and [ROADMAP.md](ROADMAP.md).

## Used libraries, components and icons
* [AndroidX libraries](https://github.com/androidx/androidx) by
  [Android Jetpack](https://github.com/androidx)
* [AboutLibraries](https://github.com/mikepenz/AboutLibraries) by
  [mikepenz](https://github.com/mikepenz)
* [Google Material icons](https://github.com/google/material-design-icons) by
  [Google](https://github.com/google)
* [JetPref preference library](https://github.com/patrickgold/jetpref) by
  [patrickgold](https://github.com/patrickgold)
* [KotlinX coroutines library](https://github.com/Kotlin/kotlinx.coroutines) by
  [Kotlin](https://github.com/Kotlin)
* [KotlinX serialization library](https://github.com/Kotlin/kotlinx.serialization) by
  [Kotlin](https://github.com/Kotlin)

Many thanks to [Nikolay Anzarov](https://www.behance.net/nikolayanzarov) ([@BloodRaven0](https://github.com/BloodRaven0)) for designing and providing the main app icons to this project!

## License
```
Copyright 2020-2026 The FlorisBoard Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

This is a modified version of FlorisBoard. The modifications are described above and recorded in the commit history.

Thanks to [The FlorisBoard Contributors](https://github.com/florisboard/florisboard/graphs/contributors) for making this project possible!
