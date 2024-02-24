package main;

import main.entities.*;
import main.world.Map;
import main.world.Tile;

import java.util.ArrayList;

public class GameState {

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

    public GameState() {
        //we cannot create the map until we read the file
        loadedMap = null;

        //load the map
        //TODO: map selection
        loadedMap = FileHandler.loadMap(Main.MAP_SELECTION);

        //create the entity array, with an initial size of 16
        //TODO: MASTER CONSTANTS CLASS
        entityArray = new ArrayList<>(16);

        //create the player
        player = new Player();

        //add the player to the array
        entityArray.add(player);

        //link all entities to this GameState object,
        //so that all entities can read the state
        //this is called "global state"
        Entity.linkGameState(this);

        //set the player's location to the spawn point provided by the map.
        player.setLocation(loadedMap.spawnX, loadedMap.spawnY);

        //as an example Entity, i created and added 12 Critters
        //thank gabby for choosing the number twelve
        //TODO: ADD ENTITY SPAWNING TO MAP FILES
        Critter lilguy1 = new Critter("friend",8,1);
        lilguy1.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy1);
        Critter lilguy2 = new Critter("friend",8,1);
        lilguy2.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy2);
        Critter lilguy3 = new Critter("friend",8,1);
        lilguy3.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy3);
        Critter lilguy4 = new Critter("friend",8,1);
        lilguy4.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy4);
        Critter lilguy5 = new Critter("friend",8,1);
        lilguy5.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy5);
        Critter lilguy6 = new Critter("friend",8,1);
        lilguy6.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy6);
        Critter lilguy7 = new Critter("friend",8,1);
        lilguy7.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy7);
        Critter lilguy8 = new Critter("friend",8,1);
        lilguy8.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy8);
        Critter lilguy9 = new Critter("friend",8,1);
        lilguy9.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy9);
        Critter lilguy10 = new Critter("friend",8,1);
        lilguy10.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy10);
        Critter lilguy11 = new Critter("friend",8,1);
        lilguy11.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy11);
        Critter lilguy12 = new Critter("friend",8,1);
        lilguy12.setLocation((int)player.x,(int)player.y);
        entityArray.add(lilguy12);
    }

    //update(): ran once per frame, where all the game processing happens
    public void update() {
        //update every entity loaded
        for (Entity u : entityArray) {
            u.update();
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
}