package main.entities;

import main.Logger;
import main.RNG;

public class ScaredyCat extends Entity{
    public ScaredyCat(){
        super();
    }
    public ScaredyCat(String name, int size, float moveSpeed, boolean directional) {
        super(name, size, moveSpeed, directional);
    }

    @Override
    public void update(){
        float distanceToPlayer = distanceTo(state.player);
        if(distanceToPlayer < 32){
            float[] selfCenter = getCenter();
            float[] targetCenter = state.player.getCenter();

            float dX = selfCenter[0] - targetCenter[0];
            float dY = selfCenter[1] - targetCenter[1];
            this.moveRelative(dX + RNG.maxDistance((int) moveSpeed), dY + RNG.maxDistance((int) moveSpeed));
        }
        super.update();
    }
}
