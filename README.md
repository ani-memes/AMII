# AMII

> Anime Meme IDE Integration

![Build](https://github.com/Unthrottled/AMII/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Verify the [pluginGroup](/gradle.properties), [plugin ID](/src/main/resources/META-INF/plugin.xml) and [sources package](/src/main/kotlin).
- [X] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html).
- [ ] [Publish a plugin manually](https://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/publishing_plugin.html) for the first time.
- [ ] Set the Plugin ID in the above README badges.
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).
- [X] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->
Give your IDE more personality and bring back joy to programming!<br/><br/>
Upon installation, our Meme Inference Knowledge Unit (or MIKU for short)
will begin interact with you as you build code.
MIKU knows when your programs fail to run or tests pass/fail.
Your new companion has the ability to react to these events.
Which will most likely take the form of an anime meme of your favorite character(s)!<br/><br/>

Integrate Anime Memes to your IDE today!
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:

  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "AMII"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/Unthrottled/AMII/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---

# Documentation

## Features

- Interactions
  - Startup Greeting
  - Test Results
  - Build Tasks
  - Waiting
  - Exit Codes
  - Logs
  - On-Demand

- Personality
  - Frustration
  - Smug
  - Status

- Offline Mode

## Configuration

- Sound
- Content
- Display
- Asset Sync


---


# Local Development
local dev
- http://localhost:4000/public/
- http://localhost:4566/demo-bucket

# Attributions

Project uses icons from [Twemoji](https://github.com/twitter/twemoji).
Graphics licensed under CC-BY 4.0: https://creativecommons.org/licenses/by/4.0/

Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
