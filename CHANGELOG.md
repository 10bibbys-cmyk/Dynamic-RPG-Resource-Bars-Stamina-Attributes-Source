# Dynamic RPG Resource Bars — Stamina Attributes Addon 1.0.2.1

**Stable maintenance release · Minecraft 1.20.1 · Fabric · Client-side**

This update adopts the Path of the Ascended four-number release scheme and improves documentation, source availability and third-party attribution without changing the working HUD implementation.

## Changes
- Promoted the tested `1.0.2.1a` candidate to **1.0.2.1**.
- Updated the public description to reflect exact dependencies, the armour text ALWAYS correction and the separate stamina-bar editor.
- Added a public source link and clarified the independent addon code and upstream artwork licensing.
- Documented configuration, installation and client-only requirements.

## Gameplay and compatibility
- **No Java combat or HUD code changes.** All Java classes, eight Mixins, cyan stamina assets and configuration schema are unchanged from the live **1.0.2** baseline and tested alpha.
- Stamina Attributes remains authoritative for current/max stamina and gameplay.
- Normal hunger and other Dynamic RPG Resource Bars HUD settings remain intact.

## Verification
The 1.0.2.1a build passed Java 17 CI and source checks. The Path of the Ascended maintainer confirmed the in-game stamina bar, editor settings, native HUD suppression, hunger preservation, armour text and other outlined regression checks worked as expected. The stable artifact only changes version metadata and release documents.

**Requirements:** Minecraft 1.20.1, Fabric Loader 0.16.14+, Fabric API, Dynamic RPG Resource Bars 0.8.1, Stamina Attributes 1.6.0, Java 17+.

**Install:** Replace the old addon JAR in the client mods folder. Keep your existing `config/dynamic_resource_bars_stamina_attributes.properties` file. Dedicated servers do not need this addon.
