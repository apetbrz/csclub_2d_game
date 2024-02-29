package main.entities;

public class Snail extends Entity{

    //a second example prefab entity
    //the snail chases the player

    public Snail(){
        super();
    }
    public Snail(String name, int size, float moveSpeed, boolean directional) {
        super(name, size, moveSpeed, directional);
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

    private void chasePlayer(){
        moveTargetPoint(state.player.getCenter());
    }
}
