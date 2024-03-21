package main.entities;

import main.Logger;
import main.Main;
import main.RNG;
import main.world.Tile;

public class Critter extends Entity{

    //TODO: MORE CONTENT!! THIS IS AN EXAMPLE ENTITY
    //this is a super simple example of a new type of Entity
    //it has its own unique variables, and its own unique functionality
    //(achieved through adding functionality to the update() call)
    //specifically, all a Critter does is randomly walk around. thats it. have fun!!!

    //TODO: PREFAB CRITTERS, FOR MAP LOADING

    //TODO: better default initialization

    //walkChance: a PERCENTAGE chance to change directions once per randomWalk() call
    private float walkChance = 2;

    //walkDistanceMax: the maximum distance in each direction, in each axis (X/Y),
    //that the critter will walk towards randomly.
    private int walkDistanceMax = Main.TILE_SIZE;

    private boolean hasKey = true;

    //constructor overrides
    //TODO: better constructors? depends on what i need
    public Critter(){
        super();
    }
    public Critter(String name, int size, float moveSpeed, boolean directional) {
        super(name, size, moveSpeed, directional);
    }

    //update(): overrides Entity::update, adding new functionality
    @Override
    public void update() {
        //firstly, the critter attempts to walk around randomly.
        this.randomWalk();

        //then, it calls Entity::update, to update everything else about itself
        //always do this for entities!
        super.update();
    }

    //randomWalk(): every call, has a walkChance% chance to
    //choose a random point nearby
    //(up to walkDistanceMax units away in each of the horizontal/vertical axes)
    //and then start walking there
    private void randomWalk(){
        Tile t = state.tileAt(this.getCenter());

        //if we are inside a tile with collision,
        if (t == null || t.hasCollision){
            //don't random walk. aka just sit there.
        }
        //otherwise, grab a random number from 0-99 and see if its less than walkChance (the chance to walk)
        else if (RNG.percentage() < walkChance) {
            //if successful, we calculate two random values,
            //one for X and one for Y
            float randX = RNG.maxDistance(walkDistanceMax);
            float randY = RNG.maxDistance(walkDistanceMax);

            //to avoid getting stuck walking into walls, we do this algorithm:
            //we check to see if the tile (relative to the Critter),
            //in that direction, has collision or not.
            //if it does (for each axis, separately), we invert that axis' direction
            //this does a good enough job of walking away from walls
            if(state.tileAt(x + randX,y).hasCollision) randX = -randX;
            if(state.tileAt(y,y + randY).hasCollision) randY = -randY;

            //finally, we move the Critter that random distance
            moveRelative(randX,randY);
        }
    }

    @Override
    public boolean collide(Entity e){
        //check for collision
        boolean collision = super.collide(e);

        //if yes,
        if(collision){
            //check if its a player that collided
            if(e.isPlayer()) {
                //if so, check if this critter has a key
                if (hasKey) {
                    //if so, write a silly little message
                    Logger.log(1, "the friend gives you a key!");
                    //and give the key to the player
                    hasKey = false;
                    ((Player)e).addItem(new Key());
                }
            }
        }

        //no else

        //pass super.collide output
        return collision;
    }

}
