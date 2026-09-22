# Local character artwork

This public repository contains the game source code. Character artwork is
distributed separately and is excluded from Git by `/assets/characters/`.
Keep the original PNGs on your own machine; do not commit or upload them.

To run the current character implementation, obtain the artwork privately from
the project owner and place it in the following local paths:

```text
assets/characters/kaito/body/idle/_kaito_idle_l.png
assets/characters/kaito/body/idle/_kaito_idle_r.png
assets/characters/kaito/body/walk/_kaito_walk_l00.png ... _kaito_walk_l03.png
assets/characters/kaito/body/walk/_kaito_walk_r00.png ... _kaito_walk_r03.png
assets/characters/kaito/body/dash/_kaito_dash_l00.png ... _kaito_dash_l04.png
assets/characters/kaito/body/dash/_kaito_dash_r00.png ... _kaito_dash_r04.png
assets/characters/kaito/weapons/normal_sword.png
```

The body assets are 64 x 64 pixel frames; the sword is 64 x 12. No Kaito attack
body sequence is currently supplied: combat temporarily uses the idle side pose
with a separately animated sword. A full build runs animation/combat checks and
requires these files. Code alone can be compiled with:

```powershell
.\gradlew.bat core:compileJava lwjgl3:compileJava
```

Once the local artwork is restored, run `./gradlew.bat lwjgl3:run` from the
project root. The existing left/right animations will load normally.

Local game JARs and distribution archives include locally installed artwork.
Do not publish those builds, screenshots, or asset archives if the artwork
must remain private. Git ignore rules do not remove files from compiled builds.

:D
