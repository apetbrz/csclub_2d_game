package main.entities;

import main.Logger;

public class Item extends Entity{

    public Item(){
        super();
        moveSpeed = 0;
    }

    public Item(String name, int size) {
        super(name, size, 0);
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
                Logger.log(1,"you found an item: " + this.name + "!");

                //add the key to the player
                ((Player)e).addItem(this);

                //kill the key entity
                this.die();
            }
        }

        //pass forward super.collide
        return collision;
    }

    public String toString(){
        return this.name;
    }

}
