package main.entities;

import main.Logger;

public class Snail extends Entity{

    //a second example prefab entity
    //the snail chases the player

    private boolean isCurrentlyTouchingPlayer;

    public Snail(){
        super();
        isCurrentlyTouchingPlayer = false;
    }
    public Snail(String name, int size, float moveSpeed, boolean directional) {
        super(name, size, moveSpeed, directional);
        isCurrentlyTouchingPlayer = false;
    }

    //update(): overrides Entity::update, adding new functionality
    @Override
    public void update() {
        //the snail chases the player
        chasePlayer();

        //then, it calls Entity::update, to update everything else about itself
        //always do this for entities!
        super.update();
    }

    //chasePlayer(): the snail's custom functionality. follows the player's center
    private void chasePlayer(){
        moveTargetPoint(state.player.getCenter());
    }

    //collide(): overrides Entity::collide, adding new functionality
    @Override
    public boolean collide(Entity e){
        //check for collision
        boolean collision = super.collide(e);

        //if yes,
        if(collision){
            //check if this is the first frame of collision
            if(!isCurrentlyTouchingPlayer) {
                //if so, write a silly little message
                Logger.log(2, "the snail caught you!!");
                //and remember the contact
                isCurrentlyTouchingPlayer = true;
            }
        }
        //if no collision, (but touched last frame)
        else if(isCurrentlyTouchingPlayer){
            //remember this
            isCurrentlyTouchingPlayer = false;
        }

        //no else

        //pass super.collide output
        return collision;
    }
}