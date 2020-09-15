# Lunar Client Spoofer
Makes any server using a public Lunar Client API think you're using Lunar Client.

Disclaimer: this does not bypass Lunar Client's anti-cheat protection. This mod will not work on protected servers officially supported by Lunar Client.

## Installation 
Get the latest release from the [releases](https://github.com/LeviDevs/LunarClientSpoofer/releases) page and put it in your mods folder.

## FAQ

##### Does this bypass Lunar Network's check?
In short: yes. This mod will bypass their check to see if you're on Lunar Client, but will **not** bypass their anti-cheat protection.

##### Is this mod a cheat?
This mod does not provide any unfair advantages. All it does is make the server think you're using Lunar Client.
Servers that screenshare typically do not freeze players running Lunar Client.

##### What public API's does this bypass?
There are no public API's that this mod does not work with to my knowledge. If you find an API that this mod does not
work with, you may open an [issue](https://github.com/LeviDevs/LunarClientSpoofer/issues).

##### Is there a way to toggle it on/off?
No. You'll need to remove it from your mods folder and restart Minecraft for the mod to be removed.

## Compatibility
This mod *only* works on 1.7.10. Support for 1.8.9 is planned.\
Made for Forge 1.7.10 version 1558 but works on 1664.

This mod has no known conflicts, but may have conflicts with mods that edit Minecraft's networking.
If you find a conflict, please open an [issue](https://github.com/LeviDevs/LunarClientSpoofer/issues) with
a list of your mods.

## Developers
Compiling is trivial. Simply clone, `gradlew setupDecompWorkspace`, `gradlew idea` (or `gradlew eclipse` for Eclipse users),
`gradlew build`. You can use `gradlew genIntellijRuns` to generate run scripts for IntelliJ.\

If you're using Linux, you'll need to use `./gradlew` instead of `gradlew`. You may also need to `chmod +x ./gradlew` to run it.

You may need to restart IntelliJ after running the above commands so IntelliJ re-indexes the project.

This project uses Java 8, **not** Java 6.
