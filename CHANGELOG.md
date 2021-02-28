<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# AMII Changelog

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
[See the updated documentation for more details.](https://github.com/Unthrottled/AMII#exit-codes)

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

- The number of available MIKUs. Each project gets it's very own MIKU.
  They function independently of each other and have their own moods.

### Fixed

- Idle personality when in `Minimal Mode`

## [0.6.0]

### Added

- Minimal mode, you know have the ability to tell MIKU to only react to different events.
Please check the <a href="https://github.com/Unthrottled/AMII#minimal-mode">documentation</a> for more details.

## [0.5.3]

### Changed

- How the AFK gifs are dismissed. They only get dismissed when the project it is on gains focus again. Not when any project gains focus.

## [0.5.2]

### Fixed

- Not being able to set frustration probability
- Not being able to disable sound
- The idle meme display position and meme display being persisted incorrectly.

## [0.5.1]

### Changed

- The Highest supported version to be `211.*`
- Plugin's primary name to `Anime Memes` to help not confuse [Waifu Motivator](https://plugins.jetbrains.com/plugin/13381-waifu-motivator) users.

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
- MacOS rendering issues, [see GitHub issue for more details](https://github.com/Unthrottled/AMII/issues/44)

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

- **Initial Release!!!** Please see the <a href="https://github.com/Unthrottled/AMII#documentation">
documentation</a> for more details!
