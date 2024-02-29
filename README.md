# csclub_2d_game
 Welcome! This is a little project I started to introduce game development to the USC Upstate Spring 2024 Computer Science Club

CONTROLS:

WASD for movement<br>
IJKL for camera panning<br>
SPACE to snap camera to player<br>

Files:
- Main: the entrypoint to the game.
- Game: the game object, what actually gets "ran".
- GameState: holds all data and functionality relevant to the game's functionality.
- GamePanel: the "screen" that the game is rendered to.
- ControlHandler: listens to the keyboard/mouse, to enable controls.
- FileHandler: does the loading of files for the other files.
- Logger: handles error message outputs for the other files.
- Entity: the master class for all "entities": players/enemies/npcs/etc.
- Player: the player entity
- Map: a "map" object, storing relevant data for loaded maps.
- Tile: each square in a map
- TileType: used for translating map file data to Tile information
- RNG: used wherever random numbers are wanted, has specific tools

Folders:
- main/: where all the code is located.
- maps/: where map files are located.
- textures/: where texture files are located.
- entities/: where entity types are located.

Notable locations:
- map selection: Main.MAP_SELECTION
- example map (with comments): maps/default.txt