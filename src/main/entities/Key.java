package main.entities;

import main.Logger;

public class Key extends Entity{
    public Key(){
        super("key",16,0);
    }

    public boolean collide(Entity e){
        boolean collision = super.collide(e);

        if(collision){
            if(e.isPlayer()){
                Logger.log(1,"you found a key!");
                ((Player)e).addKey();
                this.die();
            }
        }

        return collision;
    }
}
