package main.entities;

import main.Logger;

//TODO: CREATE "ITEM" CLASS, MAKE KEY AN ITEM INSTEAD OF ENTITY
public class Key extends Entity{

    //Key: a super simple example of an object that the player can pick up
    //Only a default constructor, makes all keys identical
    public Key(){
        super("key",16,0);
    }

    //collide(): check for collision with an entity, and do something if collide
    public boolean collide(Entity e){
        //collision: whether the collision happened or not
        boolean collision = super.collide(e);

        //if collide,
        if(collision){
            //check if the collision is with the player
            if(e.isPlayer()){
                //if so, the player picked up the key!
                //TODO: REPLACE CONSOLE MESSAGE WITH ON-SCREEN DISPLAY
                Logger.log(1,"you found a key!");

                //add the key to the player
                ((Player)e).addKey();

                //kill the key entity
                this.die();
            }
        }

        //pass forward super.collide
        return collision;
    }
}
