package main;

import main.entities.*;
import main.world.Map;
import main.world.Tile;
import main.world.TileType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class GameState {
    //game: the "game" object that created this
    public final Game game;

    //controller: the input controller, same controller as the Game object
    public ControlHandler controller;

    //panel: the game screen
    public GamePanel panel;

    //entityArray: a list of all entities currently loaded
    public ArrayList<Entity> entityArray;

    //player: the player entity. "you"
    public Player player;

    //loadedMap: the currently loaded map, as a 2D array of Tile objects
    public Map loadedMap;

    private Tile DEFAULT_TILE = new Tile(null,Integer.MIN_VALUE, Integer.MIN_VALUE);

    public GameState(Game g) {
        game = g;

        //link all entities to this GameState object,
        //so that all entities can read the state
        //basically a "global state"
        Entity.linkGameState(this);

        //we cannot create the map until we read the file
        loadedMap = null;

        //load the map
        loadMap(Main.MAP_SELECTION);

        //as an example Entity, i created and added 12 Critters
        //thank gabby for choosing the number twelve
        //TODO: ADD ENTITY SPAWNING TO MAP FILES
    }

    private void loadMap(String mapSelection){

        //TODO: map selection
        //load the map
        loadedMap = FileHandler.loadMap(Main.MAP_SELECTION);

        //create the entity array, with an initial size of 16
        entityArray = new ArrayList<>(loadedMap.initialEntities.length + 1);

        //create the player
        player = new Player();

        //set the player's location to the spawn point provided by the map.
        player.setTileLocation(loadedMap.spawnX, loadedMap.spawnY);

        //add the player to the array (we add the player at the end, so that they always render on top of others)
        entityArray.add(player);

        //add every other entity to the array
        entityArray.addAll(Arrays.asList(loadedMap.initialEntities));

        //erase the initial entity array from the map, as we no longer need it
        //if we want it back, reload the map
        loadedMap.initialEntities = null;
    }

    //update(): ran once per frame, where all the game processing happens
    public void update() {
        //update every entity loaded

        Iterator<Entity> it = entityArray.iterator();
        while(it.hasNext()){
            Entity e = it.next();
            e.update();
            if(!e.isAlive){
                it.remove();
            }
            if(!player.isAlive){
                game.gameOver();
            }
        }
    }

    //linkController(): binds the controller to the GameState object
    public void linkController(ControlHandler con) {
        controller = con;
    }

    //linkPanel(): binds the panel to the GameState object
    public void linkPanel(GamePanel pan){
        panel = pan;

        //center the camera on the player (at their spawn)
        panel.centerCamera();
    }

    //getMap(): returns the loaded map
    public Tile[][] getMapLayout() {
        return loadedMap.layout;
    }

    //getEntities(): returns the entity array
    public ArrayList<Entity> getEntities() {
        return entityArray;
    }

    //tileAt(): returns the tile at the given worldspace coordinates
    public Tile tileAt(float x, float y){
        //initialize the tile
        Tile t;

        //grab the tile coordinate by dividing by the tile size
        int tileX = (int) (x / Main.TILE_SIZE);
        int tileY = (int) (y / Main.TILE_SIZE);

        //if the pixel coordinate is negative, we have an issue with rounding,
        //which i solve by just decrementing by one.
        //this returns the correct tile
        if(x < 0) tileX--;
        if(y < 0) tileY--;

        //try to grab the tile from the 2D tile array of the map
        try{
            t = getMapLayout()[tileY][tileX];
        }
        //if out of bounds, we are trying to grab a tile outside of the map
        //so grab a null tile instead
        catch(ArrayIndexOutOfBoundsException e){
            t = null;
        }

        //return the grabbed tile
        return t;
    }
    public Tile tileAt(float[] xy){
        return tileAt(xy[0],xy[1]);
    }

    //tileAtTileCoord(): similar to above, but with tile-grid coordinates
    public Tile tileAtTileCoord(int x, int y){
        //initialize the tile
        Tile t;

        //try to grab the tile from the 2D tile array of the map
        try{
            t = getMapLayout()[y][x];
        }
        //if out of bounds, we are trying to grab a tile outside of the map
        //so grab a null tile instead
        catch(ArrayIndexOutOfBoundsException e){
            t = null;
        }

        //return the grabbed tile
        return t;
    }

    //tilesAround(): similar to tileAt(), but returns an array of the 3x3 around the coordinates
    public Tile[] tilesAround(float x, float y){
        //initialize the tile array
        Tile[] outputArray = new Tile[9];
        int iterator = 0;

        //grab the tile coordinate by dividing by the tile size
        int centerX = (int) (x / Main.TILE_SIZE);
        int centerY = (int) (y / Main.TILE_SIZE);

        //if the pixel coordinate is negative, we have an issue with rounding,
        //which i solve by just decrementing by one.
        //this returns the correct tile
        if(x < 0) centerX--;
        if(y < 0) centerY--;

        Tile t;
        //try to grab each of the tiles from the 2D tile array of the map
        for(int row = -1; row <= 1; row++) {
            for(int col = -1; col <= 1; col++) {
                outputArray[iterator] = tileAtTileCoord(centerX + col, centerY + row);
                iterator++;
            }
        }

        //return the grabbed tile
        return outputArray;
    }
    public Tile[] tilesAround(float[] xy){
        return tilesAround(xy[0], xy[1]);
    }
}