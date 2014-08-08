# Tetris Clone - Assignment 1 - CSC2003S

## Description
A clone of the original tetris. Made for my first game development assignment.

It uses the [libGDX](http://libgdx.badlogicgames.com/) engine.

The font, [Novamono](https://www.google.com/fonts/specimen/Nova+Mono), is used for all on screen text(including the tetris blocks and grid).

## Instructions
#### To Run:
Unix based OS: `./gradlew desktop:run`

Windows: `gradlew.bat desktop:run`

#### To Build:
Unix based OS: `./gradlew desktop:dist`

Windows: `gradlew.bat desktop:dist`

The built .jar file will be in `desktop/build/libs/`

To run it, change to the above directory and run: `java -jar desktop-1.0.jar`

## Controls

|Key            | Description|
|:--------------|:------------|
|`SPACE`        | Drop the currently controlled block.|
|`LEFT SHIFT`   | Store the currently active block and replace it with the next block. (Can only be done          once per block)|
|`UP ARROW`     | Rotate the currently active block 90<sup>o</sup> clockwise.|
|`RIGHT ARROW`  | Move the currently active block one space to the right.|
|`LEFT ARROW`   | Move the currently active block one space to the left.|
|`ESCAPE`       | Pause the game. Push again to unpause.|
|`Q`            | Quits the game. (After pausing and on the Game Over screen)|
|`R`            | Restart the game. (On the Game Over screen)|
|`ENTER`        | Start the game. (On the Intro screen)|
