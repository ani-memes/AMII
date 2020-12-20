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

- [Features](#features)
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
- [Configuration](#configuration)
  - [Sound](#sound)
  - [Content](#content)
  - [Display](#display)
  - [Asset Sync](#asset-sync)
---
# Features

Here is a comprehensive list of all the current functionality that AMII has to offer.

## Interactions

This is the proverbial meat and potatoes of this plugin.
You will have choicest of anime memes delivered right to your IDE as you program to your heart's content.

**MIKU**

As mention previously, our Meme Inference Knowledge Unit (or MIKU for short) hase the ability interact with you as you build code.
MIKU's preferred method of communication is Anime Memes.

If you are wondering what things you can do to cause MIKU give you memes, well just look below!

_All events are configurable to be enabled/disabled, see [configuration](#configuration) for more details_

### Startup Greeting

Just opening up a project in your IDE is cause for celebration.
Generally, MIKU is excited to see you again.
It gets dark when you are gone, so stick around a bit!

> Note: All waiting notifications are set to the `timed` dismissal option. See the [dismissal](#dismissal) section for more details

### Test Results

Tests pass and tests fail, that's just a fact of life.
You know what's better than red x's and green check marks?

Anime Memes

### Build Tasks

You are using an IDE right? Well, that means you can build your code right from your editor.
It just so happens, that builds happen to fail from time to time. You did put in that semicolon, right?

Well MIKU knows when your builds fail too, so expect a response as well.

### Waiting

What do you mean you don't code all the time?
You mean that there are periods of time when you aren't using your IDE?

Well MIKU gets lonely, or a bit bored when you are gone.

> Note: All waiting notifications are set to the `focus loss` dismissal option. See the [dismissal](#dismissal) section for more details

### Exit Codes

The code compiles, but your program has a bug, which caused your poor application to terminate with a sad exit code.
As you probably guessed by now, MIKU is always watching, and has the ability to reply to your mistakes.

**Allowed Exit Codes**

Programs that exit with:

  - **0**: Exited without issue
  - **130**: You terminated (e.g. pressed the stop button)

are part of the default allowed exit codes, MIKU will not react to these (but can if you want to!).

### Logs

Do you work on a project that takes a billion years for the server to start?
Good news! Your days of staring at your logs are over.
You can as MIKU nicely to watch the logs for you. Expect a notification whenever your phrase appears in your logged output!

### On-Demand

I suppose if you are bored, or just want to show off your Anime Memes, you have the ability to get memes on demand.

`Tools | AMII Options | Show random Ani-Meme`

## Personality

Don't get me wrong, having anime memes displayed in my IDE is awesome, but you know what's better?
Having custom tailored reactions to my current development experience.
Almost as if the memes displayed where hand-picked just for you!

Have no fear friend! MIKU various installed personality cores will enable such functionality.

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

If you ever find yourself coding without any internet, don't worry friend, you can take MIKU with you.
All interactions that you have seen so far have been stored in a safe place on your computer, just for such an occasion!

# Configuration

`Tools | AMII Options | Show AMII's Settings`

Any way you want it, that's the way you need it!
AMII has a lot of customization that allows you to tailor the experience to your preferences.

## Sound

Haven't you heard? So of your interactions with MIKU involved a related sound clip.

Not everybody wants to have their music interrupted as they are coding, so you can turn off all sound.
You even have the ability to turn the volume up and down as well!

## Content

**Preferred Gender**

We all have our own likes and dislikes (waifus, husbandos, giant robots, etc), well AMII has the strokes for different folks.
This section will only show Meme's that contain **any** of the preferred gender.

**Preferred Character**

Only want to see content with your main squeeze?
Well you can nicely ask MIKU to only show images of your preferred character.
However, your favorite character may be in an asset that MIKU can use to express their feelings.
Rather than getting nothing, you'll get another random image that matches your preferences!

## Display

MIKU has to put your memes somewhere on your screen.
So here's how you can ask nicely to have your memes work the way you want them too.

**Position**

Each block represents where you want your meme to be anchored on your IDE screen.

### Dismissal

Memes have to come and go, if they didn't exit then it would be a bit hard to do any work.
Here's what each mode does.

**Timed**

So MIKU wants each meme to play at least once.
Some memes have a longer duration than others.
Here you can specify the minimum amount of time you want each meme to appear on the screen.

> If you want your `timed` meme to hang around for longer, just click on the meme!
> That will convert the dismissal mode to `focus loss`.
> Also handy for making long memes disappear sooner!

**Focus Loss**

Rather than letting MIKU decide on your meme duration, put yourself back into control.
Memes created with the focus loss dismissal option will only disappear when you start coding or click outside the meme.

Because the meme's disappear when you are working, sometimes you can accidentally dismiss your meme.
So each meme is given a configurable duration where they are invulnerable to dismissal.
Which should buy you enough time to stop and enjoy it!

## Asset Sync

`Tools | AMII Options | Syncronize Assets`

Did you ask to have a new asset added?
Well you can start using that asset right away, using this action.
This updates your local lists of available assets to be the most current.

**Auto-Sync**: AMII is programmed to automatically update once every day,
to bring you the freshest and dankest anime memes on the reg.

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
