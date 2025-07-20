<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# AMII Changelog

## [1.6.1] - 2025-07-20

### Fixed

- Improved IDE frame detection for better Rider compatibility
- Enhanced root pane acquisition with multiple fallback methods
- Fixed meme display issues in JetBrains Rider

## [1.5.0]

### Added

- Lowest supported version is now 2024.3
- Compiles to the 2024.3 build

## [1.4.0]

### Added

- 2024.2 Build Support

## [1.3.1]

### Added

- Removed internal API usage.

## [1.3.0]

### Added

- 2024.1 Build Support

## [1.2.0]

### Added

- 2023.3 Build Support

### Removed

- All older build support


## [1.1.8]

### Added

- Initial 2023.2 Build Support


## [1.1.7] (they put the mamsnrhbr chehfde in the soder)

### Fixed

- Migrated to newer platform APIs

### Changed

- Raised the lowest supported version to 2021.3

## [1.1.6]

### Fixed

- Issues with not being able to open up Android Studio projects.

## [1.1.5]

### Added

- Initial 2023.1 Build Support

## [1.1.4]

### Fixed

- Not being able to open any project from the Welcome Screen on the 2022.3 Beta build.

## [1.1.3]

### Fixed

- Idle Events firing when you aren't actually idle. (Maybe...)

## [1.1.2]

### Fixed

- Reactions when debugging Flutter tests on Windows.

## [1.1.1]

### Fixed

- Issue when the plugin would throw an exception when using the `Search Everywhere` component.

## [1.1.0]

### Added

- 2022.3 Build Support

## [1.0.1]

### Fixed

- Issue
  preventing [Apex unit tests](https://developer.salesforce.com/docs/atlas.en-us.apexcode.meta/apexcode/apex_dev_guide.htm)
  reactions from showing up
  when [using the Illuminated Cloud 2](https://plugins.jetbrains.com/plugin/10253-illuminated-cloud-2) plugin.

## [1.0.0]

### Added

- Custom Content, please <a href="https://github.com/ani-memes/AMII/tree/main#custom-assets">
  documentation</a> for more details!

### Fixed

- `Show Previous Meme` not replaying sound for assets that have sound.

## [0.15.1]

### Fixed

- Jest Tests giving negative reaction even though the tests pass.

## [0.15.0]

### Added

- 2022.2 Build Support

## [0.14.1]

### Fixed

- Spelling mistake in the `Dimension Cap` settings.

## [0.14.0]

### Added

- 2022.1 Build Support
- The ability to right-click a meme view to the meme's asset source page on https://amii-assets.unthrottled.io/

## [0.13.2]

## Fixed

- Issue with dimension capping of assets.

## [0.13.1]

### Fixed

- Images not showing up for users with an `'` in their file path.

## [0.13.0]

### Added

- The ability to view information about the source of a meme on click.
- `Show Previous Meme`: an action that shows the previously shown meme.

## [0.12.3]

### Fixed

- Minor issue with plugin attempting to dismiss active memes. ([#123](https://github.com/ani-memes/AMII/issues/123))

## [0.12.2]

### Fixed

- The settings UI overflow issues, that prevented all content from being seen. (I can scrollbar better)

## [0.12.1]

### Added

- 2021.3 Build support. Plugin only supports 2020.3+ builds now.

## [0.12.0]

### Added

- `Discreet Mode`, when enabled MIKU will clear and not show _any_ anime in the IDE. Also, the mood in the status bar
  will temporarily hide as well.

## [0.11.0]

### Added

- The ability cap the maximum dimensions of a displayed meme while maintaining the original aspect ratio.

## [0.10.6]

### Fixed

- Compatibility problems for older IDEs.

## [0.10.5]

### Changed

- Assets that get downloaded in the background. Preferred assets will only be downloaded, that way I'm not paying for
  users to download assets that they won't see.

### Fixed

- Issue where memes that are shown with no characters whose gender is not preferred. EG, you unchecked male memes, you
  won't get a meme without a female in it.

## [0.10.4]

### Added

- 2021.2 build support!

## [0.10.3]

### Fixed

- Invulnerable memes that stay around forever [#108](https://github.com/ani-memes/AMII/issues/108)

### Added

- A `Clear Notifications` action just in case I didn't actually fix invulnerable memes.
  - Available via `Tools -> AMII Options -> Clear Notifications`

## [0.10.2]

### Added

- Suggestion of the [Anime Memes - Android Extension](https://github.com/ani-memes/amii-android-extension), for AMII's
  Android Studio Users.

### Changed

- Chinese translations to be actually better, thank you, @gzzchh !

## [0.10.1]

## Fixed

- The log watching functionality not watching logs.

## [0.10.0]

### Added

- The ability to mildly disappoint MIKU if you start to mess up a bit. They understand, but you got to fix your stuff.

![Ya dun messed up](https://amii.assets.unthrottled.io/visuals/pouting/miku_pout_kimono.gif)

## [0.9.3]

### Fixed

- Poor offline user experience. No longer propagating exceptions when offline. Thanks for reporting the exceptions!

## [0.9.2]

### Added

- Suggestion of the [Anime Memes - Rider Extension](https://github.com/ani-memes/amii-rider-extension), for AMII's Rider
  Users.

## [0.9.1]

### Changed

- Links to the repository to reflect new home
  of [https://github.com/ani-memes/amii-rider-extension](https://github.com/ani-memes/amii-rider-extension)

### Fixed

- Unit-Test reactions not showing up in Rider. Please install
  the [AMII Rider Extension](https://github.com/ani-memes/amii-rider-extension)

## [0.9.0]

## Fixed

- Plugin not working when the IDE is configured to use an HTTP proxy.

## [0.8.2]

### Fixed

- Correctly initializing settings when using the `Search Everywhere` action.
- The name of the mood status bar icon.

## [0.8.1]

### Fixed

- MIKU attempting to react on a project that is disposed (again...).
- Actually addressing the sound playback errors on Linux
- Attempting to cool down a MIKU from a disposed project.
- MIKU not paying attention to the relax action.

### Changed

- Memes with sound only play as long as the sound clip (when sound is enabled).

## [0.8.0]

### Added

- MIKU now has the ability to react positively to exit codes that you choose.
  [See the updated documentation for more details.](https://github.com/ani-memes/AMII#exit-codes)

## [0.7.3]

### Fixed

- Not being able to set minimal mode in settings.
- Sound playback issues on certain Linux machines.
- Edge case around stacked project idle notification dismissals
- MIKI attempting to react to a project that has already been disposed.

## [0.7.2]

### Fixed

- The issue with the idle notifications not coming up even after not clicking/typing in the project.

## [0.7.1]

### Added

- A periodic background check that updates your local assets more frequently.

## [0.7.0]

### Changed

- The number of available MIKUs. Each project gets it's very own MIKU. They function independently of each other and
  have their own moods.

### Fixed

- Idle personality when in `Minimal Mode`

## [0.6.0]

### Added

- Minimal mode, you know have the ability to tell MIKU to only react to different events. Please check
  the <a href="https://github.com/ani-memes/AMII#minimal-mode">documentation</a> for more details.

## [0.5.3]

### Changed

- How the AFK gifs are dismissed. They only get dismissed when the project it is on gains focus again. Not when any
  project gains focus.

## [0.5.2]

### Fixed

- Not being able to set frustration probability
- Not being able to disable sound
- The idle meme display position and meme display being persisted incorrectly.

## [0.5.1]

### Changed

- The Highest supported version to be `211.*`
- Plugin's primary name to `Anime Memes` to help not
  confuse [Waifu Motivator](https://plugins.jetbrains.com/plugin/13381-waifu-motivator) users.

## [0.5.0]

- Addressed fatal legacy platform issues.

## [0.4.1]

### Changed

- Build process to reflect release flow
- Asset distribution for better multi platform plugin support.
- Reduced starting default volume
- Calibrated MIKU's disappointment emotion core to favor asset distribution.

## [0.4.0]

### Added

- The ability for MIKU to continuously give you a stream of AniMemes (Silence Breaker feature).

### Fixed

- MacOS rendering issues, [see GitHub issue for more details](https://github.com/ani-memes/AMII/issues/44)

## [0.3.1]

### Fixed

- Asset display distribution issue

## [0.3.0]

### Added

- The ability for MIKU to progressively get more bored the longer you are away.
- The ability for MIKU to react to your successful builds after failure!
- Diversity to user's initial experience

### Changed

- The styling of the settings menu

### Removed

- Long duration of bias towards new assets

### Fixed

- MIKU not paying attention to your personality configuration updates.

## [0.2.2]

### Added

- The ability to position idle notifications

### Changed

- Idle events shows MIKU's updated mood

### Fixed

- Issue with Update notification being cut off.

## [0.2.1]

### Added

- Related audio asset to new visual asset download background process.

### Fixed

- Confirming when an error is submitted.

- Issue with attempting to display idle notifications on disposed projects

## [0.2.0]

### Fixed

- Critical first install startup error

## [0.1.0]

- **Initial Release!!!** Please see the <a href="https://github.com/ani-memes/AMII#documentation">
  documentation</a> for more details!
