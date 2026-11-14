# Dynamic RPG Resource Bars — Stamina Attributes Addon

**Give Stamina Attributes its own fully editable resource bar without replacing vanilla hunger.**

This lightweight **client-side Fabric addon for Minecraft 1.20.1** completes the connection between **Dynamic RPG Resource Bars 0.8.1** and **Stamina Attributes 1.6.0**.

It supplies Stamina Attributes' actual current and maximum stamina values to Dynamic RPG Resource Bars and adds a separate cyan stamina display, with the same positioning and appearance controls used by the built-in resource bars.

## Features

- **Accurate stamina:** Displays current and maximum stamina directly from Stamina Attributes.
- **Separate stamina bar:** Keeps the normal hunger display intact rather than replacing it.
- **HUD editor integration:** Move, resize, and configure the stamina bar in Dynamic RPG Resource Bars' existing editor.
- **Independent visual settings:** Adjust anchor, background, foreground, fill direction, text, and visibility without changing the original stamina/hunger settings.
- **Configurable drain direction:** Select right-to-left or left-to-right stamina depletion.
- **No duplicate HUD:** Suppresses Stamina Attributes' original on-screen bar while the addon provides its replacement.
- **Cyan visual style:** Supplies a separate stamina-bar texture while remaining compatible with Dynamic RPG Resource Bars' resource-pack texture system.
- **Armour text correction:** Preserves armour text when Dynamic RPG Resource Bars is configured to show it **ALWAYS**.
- **Optional held-item tooltip suppression:** Off by default; enabled only when you choose it.

This addon changes HUD rendering and configuration only. It does **not** alter stamina regeneration, consumption, attributes, combat or player movement.

## Requirements

- **Minecraft:** Java Edition 1.20.1
- **Mod loader:** Fabric Loader **0.16.14 or newer**
- **Fabric API:** Required
- **Dynamic RPG Resource Bars:** **0.8.1**
- **Stamina Attributes:** **1.6.0**
- **Java:** 17 or newer
- **Environment:** Client-side

The integration targets these specific mod versions. Compatibility with other versions has not been verified.

## Configuration

The independent stamina-bar settings are saved to:

`config/dynamic_resource_bars_stamina_attributes.properties`

Open the **Dynamic RPG Resource Bars HUD editor** and right-click the Stamina Attributes bar to change its appearance and behaviour. You can also adjust its position and size in the editor.

Existing configuration values are retained when updating; no reset is required for this maintenance release.

## Installation

1. Install Fabric Loader, Fabric API, **Dynamic RPG Resource Bars 0.8.1**, and **Stamina Attributes 1.6.0**.
2. Place this addon JAR in your client `mods` folder.
3. Start Minecraft and open the HUD editor to adjust the separate stamina bar.

The addon does not need to be installed on a dedicated server.

## Source Code & Licensing

The source for **Dynamic RPG Resource Bars - Stamina Attributes Addon 1.0.2.1** is available for public inspection on [GitHub](https://github.com/10bibbys-cmyk/Dynamic-RPG-Resource-Bars-Stamina-Attributes-Source).

Our original addon code is **source-available, All Rights Reserved**, not broadly licensed for redistribution or reuse without permission. This does not restrict rights already granted by third-party licences. **Dynamic RPG Resource Bars** code is GPLv3-licensed and its artwork has separate usage conditions; those upstream terms continue to apply to any material derived from the original project. The included cyan stamina texture may only be used as part of an addon that depends on Dynamic RPG Resource Bars, subject to the original artwork licence. See the public repository's [LICENSE](https://github.com/10bibbys-cmyk/Dynamic-RPG-Resource-Bars-Stamina-Attributes-Source/blob/main/LICENSE) and [NOTICE](https://github.com/10bibbys-cmyk/Dynamic-RPG-Resource-Bars-Stamina-Attributes-Source/blob/main/NOTICE.md).

## Compatibility & Credits

This is an independent compatibility addon, not an official component of Dynamic RPG Resource Bars or Stamina Attributes.

**Dynamic RPG Resource Bars** is developed by MuonR. Its code and artwork use different licences; upstream artwork remains subject to the original author's restrictions.

**Stamina Attributes** is developed by TheRedBrain and is licensed under MIT.

**Addon developed for Path of the Ascended**, and shared for other Fabric 1.20.1 modpacks using the same dependencies.

**AI-Assisted Development Disclosure:** AI-assisted tools were used during development and documentation. The addon was reviewed and tested by its developer.
