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

- [Interactions](#interactions)
  - [Startup Greeting](#startup-greeting)
  - [Test Results](#test-results)
  - [Build Tasks](#build-tasks)
  - [Waiting](#waiting)
  - [Exit Codes](#exit-codes)
  - [Logs](#logs)
  - [On-Demand](#on-demand)

- [Personality](#personality)
  - [Frustration](#frustration)
  - [Smug](#smug)
  - [Status](#status)

- [Offline Mode](#offline-mode)

## Configuration

- [Sound](#sound)
- [Content](#content)
- [Display](#display)
- [Asset Sync](#asset-sync)
---
# Features
## Interactions
### Startup Greeting
### Test Results
### Build Tasks
### Waiting
### Exit Codes
### Logs
### On-Demand

## Personality

Don't get me wrong, having anime memes displayed in my IDE is awesome, but you know what's better?
Having custom tailored reactions to my current situation.
Almost as if the memes displayed where hand-picked just for you!

Have no fear friend! MIKU various personality cores installed that enable such functionality.

### Frustration

MIKU is a fairly moody state machine and has many reactions to various events such as:
- Waiting for you to come back when you are away for some time
- Being really happy when your test pass.
- Becoming upset when your builds break and tests fail.

Thanks to advancements in technology, MIKU also has the ability to feel your frustration when **things aren't working, WHY ARE THEY NOT WORKING!!**.
MIKU figures its good to inject a little humor into the mix and show you that they are frustrated as well.

As a bonus, they also have the capability from evolving from being frustrated to full-blown rage.
This only happens when you have been triggering events in the frustration state for a given period.

Not every person wants their companion to get frustrated.
So you have the ability to disable this part of their personality,
by preventing them from ever being frustrated in the first place! 😄

**Take a Chill Pill!**

Have you accidentally upset MIKU? I know I have (coding is hard :D).
Well thankfully there is a `Relax MIKU` action that works as described.
This will reset the personality core's state so that you can continue to mess up as you please.

In addition, MIKU's frustration will also cool-down over time without the need for your intervention.

### Smug

Have you ever been in a rut where anything that you do just winds up failing?
Remember that feeling of finally fixing the issue?

Well when you finally get your ducks in a row, MIKU may react start feeling a bit smug.

For instance, if your test fails to run, the next time your tests pass, you have a chance of getting a _smug_ reaction.

### Status

Ever want to know how MIKU is feeling at the moment?
Don't worry about it, they will display their current emotional state in your status bar.

## Offline Mode

# Configuration
## Sound
## Content
## Display
## Asset Sync



# Local Development
local dev
- http://localhost:4000/public/
- http://localhost:4566/demo-bucket

# Attributions

Project uses icons from [Twemoji](https://github.com/twitter/twemoji).
Graphics licensed under CC-BY 4.0: https://creativecommons.org/licenses/by/4.0/

Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
