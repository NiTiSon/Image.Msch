# Image.Msch
[![Commit test](https://github.com/NiTiSon/Image.Msch/actions/workflows/commitTest.yml/badge.svg)](https://github.com/NiTiSon/Image.Msch/actions/workflows/commitTest.yml)  
This is a mod for the game [Mindustry](https://github.com/Anuken/Mindustry).  
The main purpose of the mod is to add the ability to share schematics (`.msch` files) through PNG images.

> [!TIP]
> This mod isn't tested on Android. You can report bug in [here](https://github.com/NiTiSon/Image.Msch/issues?q=sort%3Aupdated-desc+is%3Aissue+state%3Aopen+).

# Install

## In-game mod menu
1. Open mods menu
2. Click import
3. Select from GitHub
4. Enter "NiTiSon/Image.Msch"

## From Releases
1. Go to the [releases](https://github.com/NiTiSon/Image.Msch/releases) page.
2. Download Image.Msch mod file (ends with `.jar`)
   1. File without suffix is playable for both Desktop and Android versions.
   2. File with `Desktop` suffix is only playable only on Desktop (reduced size).
3. Enter the game
4. Open mod menu and select import
5. Press import from file
6. Choose Image.Msch mod file.

## From Github actions
Github actions stores artifacts from build for some time.  
Most of them are unstable.
[All recent runs available here](https://github.com/NiTiSon/Image.Msch/actions).


# Usage

## Export
This mod adds a new button to the export menu: "Export image".

<img width="382" height="333" alt="image" src="https://github.com/user-attachments/assets/bd448e5b-63cc-4000-a95f-1536d97e305b" />

This button will open a dialog choosing where to save the schematic image.  
Just save it anywhere you like, and share it with your friends (I hope you have some 🪦🌹).

> [!NOTE]  
> I would like to add "Copy image to clipboard" functionality, but the Arc engine doesn't currently support advanced clipboard actions.
 
## Import
Currently, the mod allows two ways of importing image schematics:
1. Through the import menu
2. By dropping an image into the game window (drag-and-drop)

## Limitations
Due to the limitations of SDL2, some QoL functions isn't available in V8 Mindustry.  
Despite this the mod is fully functional.
If you want to get full content, enter the `v9-testing` or `bleeding-edge` beta.

Not available content in V8:
+ Drag-n-drop (not sure if it is safe yet)
+ Copy image to the clipboard (SDL2 just doesn't support non-text clipboard)