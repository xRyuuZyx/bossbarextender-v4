# BossBar Extender — fit all UIStudio bars into the current window

This Fabric client mod keeps vanilla `BossBarHud.render` in control. It does not cancel or replace the render method and does not register the `uistudio:screen` payload. Keep the separate `uistudio_screen_sync` mod that already registers/sends that payload; do not install another copy of the same payload registration.

## What changed

- Keeps the existing redirect that extends vanilla's one-third-screen cutoff to the configured part of the scaled GUI height.
- Repositions both each vanilla BossBar and its title from the original 19-pixel row coordinates to compact row coordinates when the active bar count and current window height require it.
- Uses a 14-pixel minimum row step to reduce overlap between text labels and the BossBar bar itself.
- Leaves vanilla/resource-pack drawing in place, so UIStudio's custom font and bossbar-sprite pack continue through the ordinary renderer.

## Limitation

The mod can fit all currently received bars only while the window has enough physical GUI height. When the window is exceptionally short or the server sends many more bars than the display can accommodate at a 14px step, some rows may still clip. In that situation, maximize the game window or reduce the number of rows / move some HUD elements to ActionBar.

## Build

Use Java 21 and Gradle 9.2.1. `gradle build` creates the client JAR in `build/libs`.

The included Gradle properties target Minecraft 1.21.11. Other versions in the advertised 1.21.1–1.21.11 range require version-specific build and runtime checks.
