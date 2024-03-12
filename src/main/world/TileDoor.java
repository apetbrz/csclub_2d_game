package main.world;

import main.FileHandler;
import main.entities.Entity;
import main.entities.Player;

import java.awt.image.BufferedImage;

public class TileDoor extends Tile {

    private final int keysNeededToActivate;
    private final BufferedImage imageAlt;

    public TileDoor(TileType tileType, int x, int y) {
        super(tileType, x, y);
        keysNeededToActivate = tileType.keysNeededToActivate;
        imageAlt = FileHandler.loadImage(this.type.fileNameAlt);
    }

    public void collide(Entity e){
        if(e.isPlayer()){
            if(this.hasCollision && this.collider.intersects(e.collider)){
                if(((Player)e).useKey(keysNeededToActivate)){
                    this.image = imageAlt;
                    this.hasCollision = false;
                }
            }
        }
    }

    @Override
    public boolean isInteractable(){
        return true;
    }

}