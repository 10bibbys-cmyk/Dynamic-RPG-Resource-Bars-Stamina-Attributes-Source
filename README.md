# Dynamic RPG Resource Bars — Stamina Attributes Addon

**Source snapshot: 1.0.2.1 stable**  
**Minecraft 1.20.1 · Fabric · Java 17+ · Client-side**

A compatibility addon for **Dynamic RPG Resource Bars 0.8.1** and **Stamina Attributes 1.6.0**, developed for **Path of the Ascended**. It provides an independent cyan stamina bar with the real Stamina Attributes values while leaving the vanilla hunger display and stamina gameplay unchanged.

This public repository contains a **clean source snapshot** corresponding to release 1.0.2.1. It does not mirror private development history.

> **Source available for transparency; not broadly licensed for redistribution.** Original Path of the Ascended material is All Rights Reserved, subject to the inspection permissions in [LICENSE](LICENSE). Upstream GPLv3 code and restricted artwork retain their original legal terms. See [NOTICE.md](NOTICE.md).

## Features

- Separate display of current and maximum Stamina Attributes values
- Dynamic RPG Resource Bars HUD editor integration, with independent placement, resizing and appearance
- Configurable background, foreground, text, horizontal/vertical fill and drain direction
- Automatic suppression of the duplicate native Stamina Attributes HUD
- Correct armour text visibility when the upstream setting is **ALWAYS**
- Optional held-item-name tooltip suppression, disabled by default
- Existing cyan stamina texture; normal hunger bar remains visible

The addon does **not** change stamina regeneration, stamina costs, combat, attributes or player movement.

## Requirements

- Minecraft **1.20.1**
- Fabric Loader **0.16.14 or newer** and Fabric API
- **Dynamic RPG Resource Bars 0.8.1**
- **Stamina Attributes 1.6.0**
- **Java 17+**

This is a **client-only** mod. It does not need to run on a dedicated server. Its integration targets the exact upstream versions listed above.

## Settings

The addon stores its separate stamina-bar configuration in:

`config/dynamic_resource_bars_stamina_attributes.properties`

Open the original **Dynamic RPG Resource Bars HUD editor** and right-click the Stamina Attributes bar for its separate settings. Existing configuration can be retained when upgrading.

## Public source files

- `src/main/java/dev/drbsaaddon/` — integration and HUD editor code
- `src/main/resources/` — Fabric metadata, Mixins, cyan stamina artwork and animation metadata
- `build.gradle`, `settings.gradle`, `gradle.properties` — source build configuration
- `BUILDING.md` — build verification
- `CHANGELOG.md` — stable release notes
- `LICENSE`, `NOTICE.md` — rights, upstream attribution and artwork conditions

## Building

Install JDK 17 and Gradle 8.8, then run:

```sh
gradle --no-daemon clean build
```

Gradle downloads the required dependencies from the declared repositories; their binary JARs are not included in this repository. Building privately for inspection does not grant independent redistribution rights to original Path of the Ascended material.

## Release verification

The previous maintenance alpha **1.0.2.1a** was tested in Path of the Ascended. The maintainer confirmed stamina rendering, HUD editing, suppression of the old stamina bar, hunger preservation, armour text and the optional tooltip behaviour.

**1.0.2.1 contains no change to gameplay classes, Mixins, artwork or the configuration schema relative to 1.0.2.** Only version metadata and release documentation were updated.

The original mod's source/code and artwork licences remain in force for any affected portions of this addon. The project is independent of the original developers.

**AI-assisted development disclosure:** AI-assisted tools were used to support the project. The final implementation was reviewed and tested by its maintainer.
