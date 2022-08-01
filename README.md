<div align="center">
    <img height="256px" src="https://amii.assets.unthrottled.io/visuals/mocking/aqua_mocking_laugh.gif" ></img>
</div>

# AMII (Anime Meme IDE Integration)

![Build](https://github.com/ani-memes/AMII/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/15865-amii.svg)](https://plugins.jetbrains.com/plugin/15865-amii)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/15865-amii.svg)](https://plugins.jetbrains.com/plugin/15865-amii)

<!-- Plugin description -->
Give your IDE more personality and have <emphasis>more</emphasis> fun programming with the **A**nime **M**eme **I**DE
**I**ntegration! (AMII)<br/><br/>
Upon installation, our Meme Inference Knowledge Unit (or MIKU for short)
will begin interact with you as you build code. MIKU knows when your programs fail to run or tests pass/fail. Your new
companion has the ability to react to these events. Which will most likely take the form of an anime meme of your:
waifu, husbando, and/or favorite character(s)!<br/><br/>

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:

  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Anime Memes"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/ani-memes/AMII/releases/latest) and install it manually using
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
    - [Boredom](#boredom)
    - [Disappointment](#disappointment)
    - [Status](#status)
  - [Minimal Mode](#minimal-mode)
  - [Discreet Mode](#discreet-mode)
  - [Info On Click](#info-on-click)
  - [Show Previous Meme](#show-previous-meme)
  - [Offline Mode](#offline-mode)
  - [Clear Memes](#clear-memes)
  - [Rider Support](#rider-support)
  - [Android Studio Support](#android-studio-support)
  - [Custom Assets](#custom-assets)
    - [Auto Labeling](#auto-labeling)
    - [Suggestive Content](#suggestive-content)
- [Configuration](#configuration)
  - [Sound](#sound)
  - [Content](#content)
  - [Display](#display)
  - [Asset Sync](#asset-sync)
    - [Asset View](#asset-view)
- [Extras](#extras)
  - [The Doki Theme](#the-doki-theme)
  - [Waifu Motivator](#the-doki-theme)
  - [Release Channel](#want-amii-updates-sooner)
- [Attributions](#attributions)

---

# Features

Here is a comprehensive list of all the current functionality that AMII has to offer.

## Interactions

This is the proverbial meat and potatoes of the plugin. You will have choicest of anime memes delivered right to your
IDE as you program to your heart's content.

**MIKU**

As mention previously, our Meme Inference Knowledge Unit (or MIKU for short)
has the ability interact with you as you build code. MIKU's preferred method of communication is Anime Memes.

If you are wondering what things you can do to cause MIKU give you memes, well just look below!

_All events are configurable to be enabled/disabled, see [configuration](#configuration) for more details_

### Startup Greeting

Just opening up a project in your IDE is cause for celebration. Generally, MIKU is excited to see you again. It gets
dark when you are gone, so stick around a bit!

![Startup Greeting](./readmeAssets/project_load.gif)

> Note: All waiting notifications are set to the `timed` dismissal option.
> See the [dismissal](#dismissal) section for more details

### Test Results

Tests pass and tests fail, that's just a fact of life. You know what's better than red x's and green check marks?

> Anime Memes

**Test Pass**
![Test Pass](./readmeAssets/test_pass.gif)

**Test Failures**
![Test Failure](./readmeAssets/test_fail.gif)

### Build Tasks

This is an IDE, right? Well, that means you can build code right from your editor. It just so happens, that builds
happen to fail from time to time. You did put in that semicolon, right?

![Build Failures](./readmeAssets/build.gif)

Well MIKU knows when your builds fail too, so expect a response as well.

**Build Pass**

When you get your marbles all back, the next time you build successfully (after a build failure) MIKU will give you a
pat on the back.

### Waiting

What do you mean you don't code all the time? You mean that there are periods of time when you aren't using your IDE?

![Waiting](./readmeAssets/waiting.gif)

Well MIKU gets lonely, or a bit bored when you are gone.

> Note:
> - All waiting notifications are set to the `focus loss` dismissal option.
    > See the [dismissal](#dismissal) section for more details.
> - Notifications by default are set to center,
    > but can be configured in the [settings](#configuration).

### Exit Codes

So you got your code to build and deploy. However, the program has a catastrophic error, which caused your poor
application to terminate with a sad exit code.

![Exit Codes](./readmeAssets/exit_code.gif)

As you probably guessed by now, MIKU is always watching, and has the ability to reply to your mistakes.

#### Negative Exit Code Reactions

Really, anything that exits with a non-zero value means that your program died unexpectedly. So rather than having to
supply an exhaustive list of exit codes, MIKU will just react negativly to any code that is either:
__Ignored__ or __Positive__.

**Ignored Exit Codes**

Programs that exit with:

- **0**: Exited without issue
- **130**: You terminated the proces (e.g. pressed the stop button)

are part of the default allowed exit codes, MIKU will not react negatively to these (but can if you want to).

#### Positive Exit Code Reactions

If you want a pat on the back when your program terminates correctly, your domestic virtual servant can be configured to
do that as well.

### Silence Breaker

So you've been working diligently building your code, but not using any features of your IDE. Such as building, testing,
or running your project. Well MIKU likes to remind you every so often that they exist.

You can specify how long you can go without seeing a meme. After that, MIKU will give you one!

### Logs

Do you work on a project that takes a billion years for the application to start? Good news! Your days of staring at
your logs are over.

![Logs](./readmeAssets/log_watcher.gif)

You can ask MIKU nicely to watch the logs for you. Expect a notification whenever your phrase appears in your logged
output!

### On-Demand

`Tools | AMII Options | Show random Ani-Meme`

I suppose if you are bored, or just want to show off your Anime Memes, you have the ability to get memes on demand.

![On-demand](./readmeAssets/on_demand.gif)

## Personality

Don't get me wrong, having anime memes displayed in my IDE is awesome, but you know what's better? Having custom
tailored reactions, almost as if the memes displayed where hand-picked just for you!

As it just so happens, MIKU has various installed personality cores which enables such functionality.

### Frustration

MIKU is a fairly moody state machine and has many reactions to various events such as:

- Waiting for you to come back when you are away for some time
- Being really happy when your test pass.
- Becoming upset when your builds break and tests fail.

Thanks to advancements in technology, MIKU now also has the ability to feel your frustration when **things aren't
working, WHY ARE THEY NOT WORKING!!**. MIKU figures its good to inject a little humor into the mix and show you that
they are frustrated as well.

**Frustration**
![Frustration](./readmeAssets/frustration.gif)

As a bonus, they also have the capability from evolving from being frustrated to full-blown rage. This only happens when
you have been triggering negative events in the frustration state for a given period.

**Rage**
![Rage](./readmeAssets/enraged.gif)

Not every person wants their companion to get frustrated. So you have the ability to disable this part of their
personality, by preventing them from ever being frustrated in the first place! 😄

**Take a Chill Pill!**

`Tools | AMII Options | Relax MIKU`

Have you accidentally upset MIKU? I know I have (coding is hard). Well thankfully there is a `Relax MIKU` action that
works as described. This will reset the personality core's state so that you can continue to mess up as you please.

In addition, MIKU's frustration will also cool-down over time without the need for your intervention.

### Smug

Have you ever been in a rut where anything that you do just winds up failing? Remember that feeling of finally fixing
the issue?

Well when you finally get all your ducks in a row, you and MIKU may feel a bit smug.

For instance, if your test fails to run, the next time your tests pass, you have a chance of getting a _smug_ reaction.

![Smugumin](./readmeAssets/smug.gif)

### Boredom

The longer that you are away from your IDE, the more MIKU get bored.

They will start of waiting patiently for your return. However, as time passes, you'll see that they can't entertain
themselves forever.

Don't be surprised if you come back, and they are sleeping!

### Disappointment

With the power of technology, you now have the ability to both disappoint your parents _and_ your new virtual assistant!
I'm joking, for real though MIKU is programmed to have a hard time putting up with problems. When all of your tests have
been passing, it may be a bit _shocking_ to discover that your test has failed. Even after debugging, and you fail to
fix it, MIKU is going to feel a bit _disappointed_ that things aren't working. You best believe if things continue to
not work, MIKU is going to stop feeling bad and just be _mildly disappointed_. Careful though friend, we don't want it
to [evolve into frustration!](#frustration).

![Disappointment Chain](./readmeAssets/disappointment_chain.gif)

Only you have the power to not disappoint your new virtual friend, so work harder!

### Status

Ever want to know how MIKU is feeling at the moment? They have the ability to display their current emotional state in
your status bar.

![Status Bar Mood](./readmeAssets/mood_status_bar.png)

## Minimal Mode

MIKU can be pretty chatty sometimes, especially if you are trying to figure out how to get your integration test to
work. With `Minimal Mode` you have the ability to tell MIKU to only react to events that are different. So when your
tests fail a bunch of times, you will only see one failure reaction. However, whenever you break your build, or your
tests pass, you'll get a notification then.

## Discreet Mode

<img src="https://user-images.githubusercontent.com/15972415/132107791-3d87fa95-3b36-46b1-a49f-ff45a1fb032c.png" alt="See No Problem" align="right" />

Are you still a closeted weeb? Do you still feel shame about liking anime? Does your job require you to not have fun?
Instead of addressing the real problems, you can just tell MIKU to pretend to be invisible, with `Discreet Mode`!
They understand, and will clear any anime content from the IDE, and will even hide the mood in the status bar. That way
you do not have to explain anything to anyone. When the coast is clear, just uncheck the config or toggle the action,
and MIKU will re-appear and resume their duties as your virtual companion.

This plugin is also integrated with [The Doki Theme](https://github.com/doki-theme/doki-theme-jetbrains#discreet-mode),
for the ultimate shame hiding experience.
Enabling/disabling `Discreet Mode` in The Doki Theme will enable/disable `Discrete Mode` for this plugin.

## Info On Click

Curious about the source of a reaction supplied by MIKU?
This feature is enabled by default, and you have the ability to configure it via the settings menu, or even in the
information notification.
Just click inside the active meme, and you will get a notification about the source in the lower right-hand corner.
I have tried to tag as many assets as possible with accurate information.
However, there are some assets that I do not know the source for, sorry in advance if you wanted to know the anime!

**Note**: Clicking on a meme, changes the dismissal
mode. [Please see this documentation for more information.](#dismissal)

![Info on click](./readmeAssets/info_on_click.gif)

## Show Previous Meme

Just in case you missed something, you now have the ability to tell MIKU, to show their previous reaction.
Whether you missed you chance to [show info on click](#info-on-click) or you just want to see the reaction again.
The `Show Previous Meme` action is accessible via
<kbd>Tools</kbd> > <kbd>AMII Options</kbd> > <kbd>Show Previous Meme</kbd>

## Offline Mode

If you ever find yourself coding without any internet, don't worry friend, you can take MIKU with you. All interactions
that you have seen so far have been stored in a safe place on your computer, just for such an occasion!

## Clear Memes

For whatever reason, if you have a dispatched meme that is invulnerable to going away, have no fear friend!
That is exactly what the `Clear Memes` was made for, accessible via
<kbd>Tools</kbd> > <kbd>AMII Options</kbd> > <kbd>Clear Memes</kbd>

## Rider Support

The Rider IDE is a special snowflake that requires extra love and attention to get AMII to work. If you've installed the
plugin on Rider, you'll probably have already been prompted to install
the [Anime Memes - Rider Extension](https://github.com/ani-memes/amii-rider-extension). If you missed out,
please [be sure to install](https://github.com/ani-memes/amii-rider-extension/tree/main#installation), so you aren't
missing out on any functionality.

<div align="center">
    <img height="256px" src="https://resources.jetbrains.com/storage/products/rider/img/meta/rider_logo_300x300.png" ></img>
</div>

## Android Studio Support

Android Studio is also a special snowflake that requires extra love and attention to get AMII to work. If you've
installed the plugin on Android Studio, you'll probably have already been prompted to install
the [Anime Memes - Android Extension](https://github.com/ani-memes/amii-android-extension). If you missed out,
please [be sure to install](https://github.com/ani-memes/amii-android-extension/tree/main#installation), so you aren't
missing out on any functionality.

<div align="center">
    <img height="256px" src="https://1.bp.blogspot.com/-LgTa-xDiknI/X4EflN56boI/AAAAAAAAPuk/24YyKnqiGkwRS9-_9suPKkfsAwO4wHYEgCLcBGAsYHQ/s0/image9.png" ></img>
</div>

## Custom Assets

Do you have a specific set of memes that you would like MIKU to be able to use?
Good news! You can use the `Custom Assets` feature to add memes to your heart's content.

Heck, the memes don't even have to be anime related!

### Using Custom assets

There are a specific set of requirements in order for MIKU to be able to use your custom content.

- The asset must be somewhere in your defined `Custom Assets Directory` (MIKU searches recursively).
- The image must be a `GIF`.
- The asset must be tagged with at least one category (assets can be tagged with more than one category).

#### Meme Categories

See the details summary below to see examples of assets associated with various categories.

<details>
  <summary>Meme Category Examples</summary>

### Acknowledgement

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/ack/isla_plastic_memories.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/ok/Hachikuji_ok.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/thumbs_up/thumbs_up_one.gif) |

### Alert

Something Happened!

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/alert/cat_ears_wiggle_one.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/alert/pointing_two.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/alert/finger_guns_one.gif) |

### Bored

You've been gone for a while, come back!

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/bored/karyl-kyaru_waiting.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/bored/spy-x-family-spy-family_anya_wait.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/bored/bored_three3.gif) |

### Celebration

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/approval/zero_two_nodding.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/success/spear_yeet_full.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/celebration/caramelldansen.gif) |

### Disappointment (probably the most used category)

y u do dis?

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/crying/crying_reaction_four.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/crying/crying_reaction_six.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/dissapointment/yotsuba_no.gif) |

### Enraged

hurry up and find cover!

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/enraged/yuno_snapped.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/enraged/demon_rem.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/enraged/hayase-nagatoro-nagatoro-angry.gif) |

### Frustration

me angy

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/table_slam/aqua_anime-konosuba.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/frustration/frustration_two.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/frustrated/frustration_male_one.gif) |

### Happy

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/excited/jahy-jahysama_4.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/happy/ilulu-maid-dragon.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/happy/tonikaku-kawaii-tonikaku.gif) |

### Mocking

YA DUN MESSED UP A-A-RON!!!1

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/amused/zero_two_amused.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/mocking/rin_mocking.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/mocking/aqua_mocking_laugh.gif) |

### Motivation

When you need just a little push in the right direction

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/thumsb_up/natsuko_honda_thumbs.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/thumbs_up/kakashi_thumbs_up.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/pointing/acknowledgment_one.gif) |

### Patiently Waiting

When you've been gone for a little bit.

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/waiting/senko-waiting.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/waiting/aleh.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/waiting/narumi-wotaku.gif) |

### Pouting

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/pout/yotsuba_pout.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/pouting/itsuki_pouting_two.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/pout/ichika_pout.gif) |

### Shocked

When you've been doing well for a while, and you break something.

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/shocked/rikka_shocked.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/shocked/hifumi_surprised.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/shocked/ram_rem_shocked.gif) |

### Smug

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/smug/kyaru-sky.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/smug/love-blushing.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/smug/utaha-saekano.gif) |

### Tired

When you've been gone for a really long time. You've seen these a bunch :)

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/tired/princess-connect-priconne_sleep.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/tired/senko-sleep.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/tired/miss-kobayashi-dragon-maid-s-ilulu.gif) |

### Welcoming

Whenever you open a new project

| Example One | Example Two | Example Three|
| --- | --- | --- |
| ![exampleOne](https://amii.assets.unthrottled.io/visuals/welcome/miia_greeting.gif) | ![exampleTwo](https://amii.assets.unthrottled.io/visuals/welcome/mana_hello.gif) | ![exampleThree](https://amii.assets.unthrottled.io/visuals/welcome/chika_hey.gif) |

</details>


### How to do:

1. Place `GIF` images in your specified `Custom Assets Directory`.
2. Open up the `Custom Content` settings menu.
3. Use the `Only show untagged items`, to filter the list to memes that need to be tagged.
4. Add a category to your untagged assets.
5. Use the `Read Assets` refresh button to pick up any new changes while the settings menu is open

**Note**: You can rename assets, you just have to use the `Rescan custom assets directory` for MIKU to get the updated
file path.
Otherwise, that meme won't work anymore when MIKU tries to use it.

#### Rescan

This allows MIKU to pick up any new assets you add to your custom assets directory.
Since it is an expensive operation MIKU will only completely/recursively Rescan your custom assets directory when:

- Your IDE first starts
- You open up the `Custom Assets` settings menu tab.
- You trigger the `Rescan custom assets directory` action.

### Auto Labeling

Since it is a lot of work to use a menu to tag assets and most assets belong to one category, I figured this feature
might be handy.

When `Create Auto Labeled Directories` is enabled, MIKU will create all directories, associated with a specific asset
category in your custom assets directory.

Just add memes to the appropriate directories and when MIKU scans the custom assets directories, they'll add the
corresponding category to the asset automagically.

### Suggestive Content

I primarily built the `Custom Assets` feature because I am a degenerate.
This way I can have saucy anime content, without worrying about the plugin being removed for breaking any terms of
service. Meaning this plugin will never come bundled with any NSFW content, but you can add it if you want!

So if you are also an individual of culture you can tag various assets as `Suggestive`.
If you use `Auto Labeling` (I don't see why you would not), there will be a `suggestive` directory created.
In the `suggestive` directory, you'll see child directories. that correspond to same categories at the top level.

Those directories work the same way as the regular auto-tag directories. Just plop your assets into those directories.
When MIKU scans your custom assets directory, they will auto tag the categories & mark the asset as `Suggestive`.

I also added `Toggle suggestive mode` action that allows you to quickly switch to appearing like a pure and innocent weeb.
No assets tagged as suggestive will show up (even in the settings menu).
When the coast is clear, you can switch back to being a degenerate.

# Configuration

`Tools | AMII Options | Show AMII's Settings`

Any way you want it, that's the way you need it!
AMII has a lot of customization that allows you to tailor the experience to your preferences.

## Sound

Haven't you heard? Well if you haven't, some of your interactions with MIKU may involve a related sound clip.

Not everybody wants to have their music interrupted as they are coding, so you can turn off all sound. You even have the
ability to turn the volume up and down as well!

## Content

**Preferred Gender**

We all have our own likes and dislikes (waifus, husbandos, giant robots, etc), well MIKU has the strokes for different
folks. This section will only show memes that contain **any** of the preferred gender.

**Preferred Characters**

Only want to see content with your main squeeze? Well you can nicely ask MIKU to only show images of your preferred
character. However, your favorite character may **not** be in an asset that MIKU can use to express their feelings.
Rather than getting nothing, you'll get another random image that matches your other preferences!

**Blacklisted Characters**

You don't like the same things I like?? The nerve, how dare you!

Just kidding! 😃

I kind of figured that may happen, so I also added a `Character Blacklist`. Which prevents _any_ content containing the
selected characters from showing up!

**Note**: the **blacklist** takes preference over **preferences**. So if there is content with blacklisted characters
and preferred characters, well then you don't get that content shown to you.

## Display

MIKU has to put your memes somewhere on your screen. So here's how you can request to have your memes work the way you
want them too.

**Position**

Each block represents a section where you want your meme to be anchored on your IDE's screen.

### Dismissal

Memes have to come and go, if they didn't exit then, it would be a bit hard to do any work. Here's what each mode does.

**Timed**

So MIKU wants each meme to cycle at least once. Some memes have a longer duration than others. Here you can specify the
minimum amount of time you want each meme to appear on the screen.

> **Tip**: If you want your `timed` meme to hang around for longer, just click on the meme!
> That will convert the dismissal mode to `focus loss`.
> Also handy for making long memes disappear sooner!

**Focus Loss**

Rather than letting MIKU decide on your meme duration, put yourself into control. Memes created with the focus loss
dismissal option will only disappear when you start coding or click outside the meme.

Because the meme's disappear when you are working, sometimes you can accidentally dismiss your meme. So each meme is
given a configurable duration where they are invulnerable to dismissal. Which should buy you enough time to stop and
enjoy it!

**Dimension Capping**

Some memes, provided by this plugin, are big and sometimes could get in the way. Thankfully, if you find that this is
the case, you can cap the maximum dimensions of the memes to be displayed.

MIKU wants to maintain the original aspect ratio of the image, so they will take the largest dimension and cap it to
that. That way you can still see the same image, just smaller.

Here is a sample of setting the dimensions capped at 200 width and height.

![Dimension Capping](readmeAssets/dimension_capping.gif)

**Note**: if you don't want both dimensions to be capped (just one, when enabled), just use a `-1` as the value. That
way MIKU knows to ignore that dimension when calculating re-sized dimensions.

## Asset Sync

`Tools | AMII Options | Syncronize Assets`

Did you ask to have a new asset added?

Well you can start using that asset right away, using this action. This updates your local lists of available assets to
be the most current.

**Auto-Sync**: AMII is programmed to automatically update once every day, to bring you the freshest and dankest anime
memes on the reg.

### Asset View

Did you know that you can see all of the assets AMII uses
here: [https://amii-assets.unthrottled.io/](https://amii-assets.unthrottled.io/)?

---

# Extras!

<div align="center">
    <img src="https://doki.assets.unthrottled.io/misc/logo.svg" ></img>
</div>

## The Doki Theme

Do you need more anime waifus in your life? Well I have a solution just for that
problem, [The Doki Theme](https://github.com/doki-theme)!
Decorate all your favorite tools with your favorite character(s)!

Available for any [JetBrains IDE](https://github.com/doki-theme/doki-theme-jetbrains).

![Doki Theme Jetbrains](https://github.com/doki-theme/doki-theme-jetbrains/raw/master/assets/screenshots/themes.webp)

## Waifu Motivator

<p align="center"><img src="https://raw.githubusercontent.com/waifu-motivator/waifu-motivator-plugin/master/images/wmp_logo.png" height="256px" alt="Waifu Motivator Plugin Logo"></p>

<p align="center">A collection of open-sourced Jetbrains IDE plugins that bring <i>Waifus</i> in to help keep your motivation to complete during your coding challenges.</p>

Available for any [JetBrains IDE](https://github.com/waifu-motivator/waifu-motivator-plugin).

## Want AMII updates sooner?

I have a [canary release channel](https://github.com/Unthrottled/jetbrains-plugin-repository) that you can set up to get
the latest and greatest!

---

# Attributions

Project uses icons from [Twemoji](https://github.com/twitter/twemoji). Graphics licensed under CC-BY
4.0: https://creativecommons.org/licenses/by/4.0/

Plugin based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
