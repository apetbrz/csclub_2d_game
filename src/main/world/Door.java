package main.world;

import main.FileHandler;
import main.entities.Entity;
import main.entities.Player;

import java.awt.image.BufferedImage;

public class Door extends SuperTileObject {

    //Door: an implementation of a TileObject
    //Doors can be opened with keys

    public Door(TileType tileType, int x, int y) {
        super(tileType, x, y);
        imageAlt = FileHandler.loadImage(this.type.fileNameAlt);
    }

    //interact() override: handles opening the door, based on player key count
    public void interact(Entity e){
        if(e.isPlayer()){
            if(((Player)e).useKey()){
                this.image = imageAlt;
                this.hasCollision = false;
            }
        }
    }

}