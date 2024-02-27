package main;

public class Main {
    //TODO: MASTER CONSTANTS CLASS

    //TITLE: the name of the game, shown in the bar at the top of the window
    public static final String TITLE = "cs-club-2d-game INDEV";

    //ICON_FILENAME: the name (without extension) of the image to use as the window icon
    public static final String ICON_FILENAME = "player_down";

    //MAP_SELECTION: the chosen map to load
    //WILL REMOVE AFTER MAP SELECTION IS IMPLEMENTED
    public static final String MAP_SELECTION = "default";

    //TARGET_FPS: the fps to try to render at
    public static final int TARGET_FPS = 60;

    //TILE_SIZE: the size (in pixels) of every game tile.
    //currently, tiles are 32x32 pixels
    public static final int TILE_SIZE = 32;

    //RENDER_SCALE: a multiplier to all pixel values for rendering
    //i use 2, so at TILE_SIZE = 32,
    //the final tiles will be rendered as 64x64 pixels on the screen
    public static final int RENDER_SCALE = 2;

    //CONTROL_TYPE: the control scheme.
    //    0: WASD movement
    //    1: MOUSE CLICK movement
    //NOTE: MOUSE CLICK movement has less development priority right now. its only kept just because
    public static final CONTROLLER CONTROL_TYPE = Main.CONTROLLER.KEYBOARD;
    public enum CONTROLLER{
        KEYBOARD,
        MOUSE
    }

    //FORCE_CENTERED_CAMERA: determines the default state of the camera,
    //whether it always follows the player or not
    public static final boolean FORCE_CENTERED_CAMERA = true;

    //DEBUG_SHOW_HITBOXES: decides if hitboxes are rendered.
    //GREEN boxes have collision
    //RED boxes do not
    public static final boolean DEBUG_SHOW_HITBOXES = false;

    //DEBUG_SHOW_COORDINATES: decides if player coords are rendered.
    //shows coordinates to player and to collider box
    public static final boolean DEBUG_SHOW_COORDINATES = true;

    //DEBUG_NOCOLLIDE: decides if the player has noclip or not
    public static final boolean DEBUG_NOCOLLIDE = false;

    public static void main(String[] args) {
        //create game object
        Game game = new Game();

        //start it
        game.start();
    }

}