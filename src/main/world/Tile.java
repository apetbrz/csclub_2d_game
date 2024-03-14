package main.world;

import main.FileHandler;
import main.GamePanel;
import main.Main;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Tile {
    //type: the integer type, to keep track of what tiles are what
    public TileType type;

    //x, y: 2D coordinates in main.world space
    public int x;
    public int y;

    //w: width
    public int w;

    //image: the texture of the tile
    public BufferedImage image;

    //hasCollision: whether the tile blocks entities or not
    public boolean hasCollision;

    //collider: used for collision TODO: IMPLEMENT? unused for current iteration of collision code
    public Rectangle2D collider;

    /*
    TILES:
    -1: default tile, do not use!
    0 - Grass
    1 - Rock Wall
    2 - Water
    3 - Sand
    TODO: MORE TYPES
     */

    //CONSTRUCTOR
    public Tile(TileType tileType, int x, int y) {
        //store values
        //check if type is null, if it is, use a default TileType, if not, pass it through
        this.type = (tileType == null ? new TileType() : tileType);
        this.x = x;
        this.y = y;
        this.w = Main.TILE_SIZE;

        //init collider
        int colliderBuffer = 0;
        collider = new Rectangle2D.Float(x-colliderBuffer, y-colliderBuffer, Main.TILE_SIZE+colliderBuffer*2, Main.TILE_SIZE+colliderBuffer*2);

        //get tile information based on type
        image = FileHandler.loadImage(this.type.fileName);
        hasCollision = this.type.hasCollision;
    }

    //similar to Entity::isPlayer
    public boolean isInteractable(){
        return this instanceof SuperTileObject;
    }
}