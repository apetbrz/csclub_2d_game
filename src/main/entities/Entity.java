package main.entities;
import main.*;
import main.world.SuperTileObject;
import main.world.Tile;
import main.world.Door;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Entity {
    //game: the GameState object, so the entity can interact with other things
    protected static GameState state;

    //name: the name of the entity (used for display)
    public String name;

    //image: the sprite of the entity
    public Image image;

    //up/left/down/rightImage: these hold the four images used for the four directions
    //if hasDirectionality == false, these are all null.
    private Image upImage;
    private Image leftImage;
    private Image downImage;
    private Image rightImage;

    //hasDirectionality: states whether the entity rotates/changes image as it changes direction
    public boolean hasDirectionality;

    //isAlive: true if alive, false if dead.
    public boolean isAlive = true;

    //x, y: 2D coordinate location
    public float x, y;

    //targetX, targetY: the 2D coordinate to travel towards every frame
    public float targetX, targetY;

    //size: render/collision length/width, assumed to be square
    public int size;

    //moveSpeed: distance traveled per update() call (per frame)
    public float moveSpeed;

    //collider: used for checking collision (the "hitbox")
    //TODO: IMPLEMENT, CURRENTLY USING size FOR COLLISIONS
    //TODO: COLLISIONS WITH OTHER ENTITIES AND EFFECT FIELDS
    public Rectangle2D collider;

    //hasCollision: if false, ignores collision
    public boolean hasCollision;

    //CONSTRUCTORS
    //TODO: CLEAN UP CONSTRUCTORS
    public Entity(){
        hasCollision = true;
    }
    public Entity(String name, int size, float moveSpeed) {
        this();

        this.name = name;
        this.size = size;
        this.moveSpeed = moveSpeed;

        setImage(name);

        initColliderRectangle();
    }
    public Entity(String name, int size, float moveSpeed, boolean directional){
        this();

        this.name = name;
        this.size = size;
        this.moveSpeed = moveSpeed;

        if(directional) setImagesWithDirectionality(name);
        else setImage(name);

        initColliderRectangle();
    }

    //initColliderRectangle: initializes the collider, relative to the Entity's pos/size
    protected void initColliderRectangle(){
        collider = new Rectangle2D.Float(x, y, size, size);
    }

    //initCollider(int,int,int,int): initializes the collider, same as above,
    //but allows for four "margin" values, which shrink the collider box, in each of the 4 directions
    //(IN PIXELS)
    //as such:
    /*
    Entity.size square:
    ----------------------------------
    |           topMargin            |
    |left   |---------------| right  |
    |Margin |   collider    | Margin |
    |       |---------------|        |
    |          bottomMargin          |
    ----------------------------------
     */
    protected void initColliderRectangle(int topMargin, int leftMargin, int bottomMargin, int rightMargin){
        collider = new Rectangle2D.Float(x + leftMargin, y + topMargin, size - leftMargin - rightMargin, size - topMargin - bottomMargin);
    }

    //linkGameState(): links every entity globally to the GameState object
    public static void linkGameState(GameState game){
        Entity.state = game;
    }

    //setImage(): takes the name of a file in the textures folder
    //and loads that into the entity's image
    //automatically scales the image to be the entity's size
    //images won't look as good if they scale weird, so try to keep image size equal to entity size
    protected void setImage(String fileName) {
        image = FileHandler.loadImage(fileName).getScaledInstance(size,size, GamePanel.DEFAULT_IMAGE_SCALING_MODE);
    }

    //setImagesWithDirectionality(): same as above, but
    //handles the four directional images
    protected void setImagesWithDirectionality(String fileName){
        hasDirectionality = true;

        upImage = FileHandler.loadImage(fileName + FileHandler.DIRECTION_UP_SUFFIX).getScaledInstance(size,size, GamePanel.DEFAULT_IMAGE_SCALING_MODE);
        leftImage = FileHandler.loadImage(fileName + FileHandler.DIRECTION_LEFT_SUFFIX).getScaledInstance(size,size, GamePanel.DEFAULT_IMAGE_SCALING_MODE);
        downImage = FileHandler.loadImage(fileName + FileHandler.DIRECTION_DOWN_SUFFIX).getScaledInstance(size,size, GamePanel.DEFAULT_IMAGE_SCALING_MODE);
        rightImage = FileHandler.loadImage(fileName + FileHandler.DIRECTION_RIGHT_SUFFIX).getScaledInstance(size,size, GamePanel.DEFAULT_IMAGE_SCALING_MODE);

        image = downImage;
    }

    //moveTargetPoint(): sets the X and Y main.world coordinates that the entity will move towards automatically (centered)
    public void moveTargetPoint(float X, float Y) {
        targetX = centerToTopLeftEdge(X);
        targetY = centerToTopLeftEdge(Y);
    }
    public void moveTargetPoint(float[] xy){
        targetX = centerToTopLeftEdge(xy[0]);
        targetY = centerToTopLeftEdge(xy[1]);
    }

    //moveRelative(): similar to moveTarget(), but relative to the entity itself (useful for directional/WASD controls)
    public void moveRelative(float X, float Y){
        targetX = this.x + X;
        targetY = this.y + Y;
    }

    //update(): called every frame, handles entity logic/movement
    public void update() {
        //move
        updatePosition();
    }

    //updatePosition(): handles movement, towards targetX & targetY
    protected void updatePosition(){
        //deltaX, deltaY: the target to move towards, relative to the entity
        float deltaX = targetX - x;
        float deltaY = targetY - y;

        //so long as there is distance to move, we move.
        if (deltaX != 0 || deltaY != 0) {
            //use pythagoras to get distance to target point
            float vectorMagnitude = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            //if close enough to where one more step overshoots,
            if (vectorMagnitude < moveSpeed) {
                //snap to the target lol
                //only really used for mouse movement!
                //prevents glitchy rapid back-and-forth,
                //when at the target location but each step overshoots
                moveWithTerrainCollision(deltaX,deltaY);
            } else {
                //otherwise, move towards target

                //moveX, moveY: x/y values normalized (to prevent diagonal movement being ~1.4x the speed)
                float moveX = (deltaX / vectorMagnitude) * moveSpeed;
                float moveY = (deltaY / vectorMagnitude) * moveSpeed;

                //actually move:
                moveWithTerrainCollision(moveX,moveY);
            }
        }

        //if the entity has directionality enabled,
        //we change the image rendered based on what direction they are moving
        if(hasDirectionality) {
            //currently prioritizes horizontal movement over vertical movement
            //(if moving diagonal, entity will face left/right)
            //TODO: keep first direction traveled from standstill (how? we'll see)

            //check if we are moving (aka not standing still)
            if (deltaX != 0 || deltaY != 0) {
                //if so:
                //if deltaX is smaller than deltaY
                if(Math.abs(deltaX) < Math.abs(deltaY)){
                    //set the image based on the vertical direction
                    image = deltaY < 0 ? upImage : downImage;
                }
                //otherwise, if deltaX is larger than deltaY
                else{
                    //set the image based on the horizontal direction
                    image = deltaX < 0 ? leftImage : rightImage;
                }
            }
        }
    }

    //move(): move the entity (and its collider) relative to itself
    protected void move(float moveX, float moveY){
        this.x += moveX;
        this.y += moveY;
        collider.setRect(collider.getX() + moveX, collider.getY() + moveY, collider.getWidth(), collider.getHeight());
    }

    //moveWithTerrainCollision(): relative movement, with terrain blocking
    protected void moveWithTerrainCollision(float moveX, float moveY){
        //grab the 9 tiles surrounding the player
        Tile[] tilesAround = state.tilesAround(this.x,this.y);

        //move in each axis independently
        moveHorizontalWithCollision(moveX, tilesAround);
        moveVerticalWithCollision(moveY, tilesAround);
    }

    //moveHorizontal/VerticalWithCollision(): used in above, for separate axes of movement
    protected void moveHorizontalWithCollision(float moveX, Tile[] tilesAround){
        //if no movement, skip all checks.
        if(moveX == 0) return;

        //force the movement
        move(moveX, 0);

        //for each tile around the player,
        for(Tile t : tilesAround){
            //if the tile is null, give up and let the entity move
            //(im not gonna bother fixing this at this moment, just put walls around your maps lol)
            if(t == null){
                continue;
            }

            //if the tile has no collision, skip the check
            else if(!t.hasCollision) continue;

            //otherwise, the tile does have collision
            //check if the entity's collider overlaps the tile's collider
            else if(this.collider.intersects(t.collider)) {
                //if so, the entity is inside the wall

                //interact with the tile, if possible
                interact(t);

                //first, grab which direction the entity is, relative to the tile
                //this is a bit value check, rect1.outcode(point) returns a binary number with the following values OR'd together:
                //0001 - point is LEFT of rect1
                //0010 - point is on TOP of rect1
                //0100 - point is RIGHT of rect1
                //1000 - point is on the BOTTOM of rect1
                //i.e.: 0110 == TOP RIGHT, 1001 == BOTTOM LEFT, etc.
                //we use the entity's center as the point
                int collisionDirection = t.collider.outcode(this.collider.getCenterX(),this.collider.getCenterY());

                //grab relevant coordinates of the colliders:

                //tileLeftEdge == tile collider's LEFT edge
                //tileRightEdge == tile collider's RIGHT edge
                float tileLeftEdge = (float) t.collider.getX();
                float tileRightEdge = tileLeftEdge + (float) t.collider.getWidth();

                //entityLeftEdge == entity collider's LEFT edge
                //entityRightEdge == entity collider's RIGHT edge
                float entityLeftEdge = (float) this.collider.getX();
                float entityRightEdge = entityLeftEdge + (float) this.collider.getWidth();

                //check if the entity is to the right of the tile
                //(if outcode contains OUT_RIGHT, the bitwise & will not be 0)
                if ((collisionDirection & Rectangle2D.OUT_RIGHT) != 0){
                    //calculate the overlap from the right (tile right edge - entity left edge)
                    //and move that distance to the right
                    this.move(tileRightEdge - entityLeftEdge, 0);
                }
                //repeat for the left
                else if ((collisionDirection & Rectangle2D.OUT_LEFT) != 0){
                    //overlap from the left is (entity right edge - tile left edge)
                    //move that distance left (by moving negative distance)
                    this.move(0 - (entityRightEdge - tileLeftEdge), 0);
                }
            }
        }
    }
    protected void moveVerticalWithCollision(float moveY, Tile[] tilesAround){
        //if no movement, skip all checks.
        if(moveY == 0) return;

        //force the movement
        move(0, moveY);

        //for each tile around the player,
        for(Tile t : tilesAround){
            //if the tile is null, give up and let the entity move
            //(im not gonna bother fixing this at this moment, just put walls around your maps lol)
            if(t == null){
                continue;
            }

            //if the tile has no collision, skip the check
            else if(!t.hasCollision) continue;

            //otherwise, the tile does have collision
            //check if the entity's collider overlaps the tile's collider

            else if(this.collider.intersects(t.collider)) {
                //if so, the entity is inside the wall

                //interact with the tile, if possible
                interact(t);

                //first, grab which direction the entity is, relative to the tile
                //this is a bit value check, rect1.outcode(point) returns a binary number with the following values OR'd together:
                //0001 - point is LEFT of rect1
                //0010 - point is on TOP of rect1
                //0100 - point is RIGHT of rect1
                //1000 - point is on the BOTTOM of rect1
                //i.e.: 0110 == TOP RIGHT, 1001 == BOTTOM LEFT, etc.
                //we use the entity's center as the point
                int collisionDirection = t.collider.outcode(this.collider.getCenterX(),this.collider.getCenterY());

                //grab relevant coordinates of the colliders:

                //tileTopEdge == tile collider's TOP edge
                //tileBottomEdge == tile collider's BOTTOM edge
                float tileTopEdge = (float) t.collider.getY();
                float tileBottomEdge = tileTopEdge + (float) t.collider.getHeight();

                //entityTopEdge == entity collider's TOP edge
                //entityBottomEdge == entity collider's BOTTOM edge
                float entityTopEdge = (float) this.collider.getY();
                float entityBottomEdge = entityTopEdge + (float) this.collider.getHeight();

                //check if the entity is below the tile
                //(if outcode contains OUT_BOTTOM, the bitwise & will not be 0)
                if ((collisionDirection & Rectangle2D.OUT_BOTTOM) != 0){
                    //calculate the overlap from the bottom (tile bottom edge - entity top edge)
                    //and move that distance back down
                    this.move(0, tileBottomEdge - entityTopEdge);
                }
                //repeat for above
                else if ((collisionDirection & Rectangle2D.OUT_TOP) != 0){
                    //overlap from the top is (entity bottom edge - tile top edge)
                    //move that distance up (by moving negative distance)
                    this.move(0, 0 - (entityBottomEdge - tileTopEdge));
                }
            }
        }
    }

    private void interact(Tile t) {
        if(!(t instanceof SuperTileObject)) return;
        if(t instanceof Door) ((Door) t).interact(this);
    }

    //setLocation: moves the entity to the specific coordinates in worldspace,
    //centered on the point
    public void setLocation(int x, int y){
        float deltaX = this.x + topLeftEdgeToCenter(x);
        float deltaY = this.y + topLeftEdgeToCenter(y);
        this.x += deltaX;
        this.y += deltaY;
        this.targetX = x;
        this.targetY = y;
        collider.setRect(collider.getX() + deltaX, collider.getY() + deltaY, collider.getWidth(), collider.getHeight());
    }
    public void setTileLocation(int x, int y){
        this.setLocation(x * Main.TILE_SIZE, y * Main.TILE_SIZE);
    }

    //topLeftEdgeToCenter(): takes a coordinate from the top/left edge and returns a centered one
    public float topLeftEdgeToCenter(float value){
        return value + (float)this.size/2;
    }
    //topLeftCornerToCenter(): similar as above, but with an array (assuming [x,y], like a coordinate)
    public float[] topLeftCornerToCenter(float[] coord){
        return new float[]{topLeftEdgeToCenter(coord[0]), topLeftEdgeToCenter(coord[1])};
    }

    //centerToTopLeftEdge: similar to topLeftEdgeToCenter() but the other way around
    public float centerToTopLeftEdge(float value){
        return value - (float)this.size/2;
    }
    //centerToTopLeftCorner(): similar as above, but with an array (assuming [x,y], like a coordinate)
    public float[] centerToTopLeftCorner(float[] coord){
        return new float[]{centerToTopLeftEdge(coord[0]), centerToTopLeftEdge(coord[1])};
    }

    //isPlayer(): returns true if the current entity is a player. i override this in Player.java
    public boolean isPlayer(){
        return false;
    }

    //getCenter(): returns the [x,y] coordinate for the center of the entity
    protected float[] getCenter() {
        return new float[]{this.topLeftEdgeToCenter(this.x), this.topLeftEdgeToCenter(this.y)};
    }

    protected float distanceTo(Entity e){
        float[] selfCenter = getCenter();
        float[] targetCenter = e.getCenter();

        float dX = targetCenter[0] - selfCenter[0];
        float dY = targetCenter[1] - selfCenter[1];

        return (float)Math.sqrt(dX * dX + dY * dY);
    }

    //collide(): returns a boolean value of whether the entity collides with the target
    //can be overriden to add extra functionality to entity types
    public boolean collide(Entity target){
        return this.collider.intersects(target.collider);
    }

    //die(): die
    public void die(){
        this.isAlive = false;
    }
}