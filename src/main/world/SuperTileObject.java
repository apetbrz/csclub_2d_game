package main.world;

import main.FileHandler;
import main.entities.Entity;

import java.awt.image.BufferedImage;

public class SuperTileObject extends Tile{

    //SuperTileObject: the superclass for all tiles that can be interacted with

    //imageOriginal: a copy of the tile's image, as a backup
    protected BufferedImage imageOriginal;

    //imageAlt: the image for the TileObject's alternate state
    protected BufferedImage imageAlt;

    public SuperTileObject(TileType tileType, int x, int y) {
        super(tileType, x, y);
        imageOriginal = this.image;
        imageAlt = FileHandler.loadImage(this.type.fileNameAlt);
    }

    //collide(): the method called when the tile is touched by an entity.
    //to be overridden!!! to add functionality!
    public boolean collide(Entity e){
        if(this.collider.intersects(e.collider)){
            interact(e);
            return this.hasCollision;
        }
        else return false;
    }

    //interact(): the method called for tile interaction functionality
    //to be overridden!!! to add functionality!!!
    public void interact(Entity e){

    }

    @Override
    public boolean isInteractable(){
        return true;
    }
}

