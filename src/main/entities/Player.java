package main.entities;

import main.*;

public class Player extends Entity{

    //TODO: GAMEPLAY, MORE ENTITY TYPES
    //this will hold player-exclusive stuff

    //DEFAULT_x: default values for the player. they always start with this.
    private static final String DEFAULT_NAME = "player";
    private static final int DEFAULT_MOVESPEED = 2;
    private static final int DEFAULT_SIZE = 16;

    //TODO: MOVE TO SETTINGS FILE FOR EASIER DEVELOPMENT (SHOULD BE CHANGED TO WORK WITH SPRITE)
    private static final int DEFAULT_TOP_MARGIN = 3;
    private static final int DEFAULT_LEFT_MARGIN = 2;
    private static final int DEFAULT_BOTTOM_MARGIN = 1;
    private static final int DEFAULT_RIGHT_MARGIN = 2;

    //TODO: Inventory FOR ALL ENTITIES?
    //TODO: GAMEPLAY DESIGN
    private int keys = 0;
    
    //DEFAULT CONSTRUCTOR OVERRIDE
    //no other constructor, we want the player to always be the same (unless future multiplayer support)
    public Player() {
        super();

        this.name = DEFAULT_NAME;
        this.size = DEFAULT_SIZE;
        this.moveSpeed = DEFAULT_MOVESPEED;

        //since the player is always directional,
        //initialize the directionality
        setImagesWithDirectionality(FileHandler.PLAYER_TEXTURE);

        //init the collider
        initColliderRectangle(DEFAULT_TOP_MARGIN, DEFAULT_LEFT_MARGIN, DEFAULT_BOTTOM_MARGIN, DEFAULT_RIGHT_MARGIN);

        //if NOCOLLIDE is enabled, disable the player's collision.
        if(Main.DEBUG_NOCOLLIDE) this.hasCollision = false;

    }

    //update() OVERRIDE: adding the additional functionality of control input.
    public void update(){
        handleControls(state.controller);
        super.update();
        checkCollisions();
    }

    private void checkCollisions() {
        for(Entity e : state.getEntities()){
            if(e == null) continue;
            e.collide(this);
        }
        /*
        for(Tile t : state.tilesAround(this.x, this.y)){
            if(t == null) continue;
            if(t.isInteractable()){
                ((TileObject)t).collide(this);
            }
        }
        */
    }

    //handleControls(): looks at the ControlHandler object and acts accordingly.
    private void handleControls(ControlHandler controller){

        //take user input to move player
        switch(Main.CONTROL_TYPE){
            //if using the mouse:
            case MOUSE:
                //MOUSE CONTROLS! player follows mouse clicks/drags

                //if holding down left click,
                if(controller.isLeftClick) {
                    //grab the mouse's coordinates
                    float targetX = controller.mouseX;
                    float targetY = controller.mouseY;

                    //make it relative to the camera's position in the world
                    //(screen space -> world space)
                    targetX += state.panel.cameraX;
                    targetY += state.panel.cameraY;

                    //make it scale properly with Main.RENDER_SCALE
                    targetX /= Main.RENDER_SCALE;
                    targetY /= Main.RENDER_SCALE;

                    //center it on the player
                    targetX = topLeftEdgeToCenter(targetX);
                    targetY = topLeftEdgeToCenter(targetY);

                    //after all those calculations, set that point to the Entity's target coordinate
                    this.moveTargetPoint(targetX, targetY);
                }
                break;  //END case MOUSE

            //if using keyboard:
            case KEYBOARD:
                //WASD CONTROLS! or whatever keybinds are in ControlHandler

                //moveX/moveY: how far to move relative to the player's position
                float moveX = 0;
                float moveY = 0;

                //for each of the four directions, if the button is held down,
                //we add/subtract one moveSpeed in the correct direction
                //reminder that (0,0) is the top-left corner!!! right/down is positive, up/left is negative!
                //TODO: FIX THIS, WHY SHOULD DOWN BE POSITIVE? GamePanel
                if (controller.up) moveY -= moveSpeed;
                if (controller.left) moveX -= moveSpeed;
                if (controller.down) moveY += moveSpeed;
                if (controller.right) moveX += moveSpeed;

                //after getting the values, move that way.
                this.moveRelative(moveX, moveY);

                break;  //END case KEYBOARD

        }//end switch statement
    }

    //isPlayer(): returns true if the current entity is a player.
    public boolean isPlayer(){
        return true;
    }


    //getKeys(): returns how many keys the player is holding
    public int getKeys() {
        return keys;
    }

    //addKey(): add one key to the player's inventory
    public void addKey(){
        keys++;
    }

    //useKey(): consume one key, if it exists. returns true if successful, false if not enough keys
    public boolean useKey(){
        return(useKeys(1));
    }

    //useKeyS(): consume some amount of keys, if they exist. returns true if successful, false if not enough keys
    public boolean useKeys(int count){
        if(getKeys() < count) return false;
        else{
            keys -= count;
            return true;
        }
    }
}