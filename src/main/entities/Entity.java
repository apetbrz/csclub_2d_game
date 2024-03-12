package main.entities;
import main.*;
import main.world.Tile;
import main.world.TileObject;

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

                //TODO: THIS MOVEMENT CODE SUCKS. HORRIBLY. PLEASE RE-DO
                //TODO: FIX COLLIDER DRIFT

                //moveX, moveY: x/y values normalized (to prevent diagonal movement being ~1.4x the speed)
                float moveX = (deltaX / vectorMagnitude) * moveSpeed;
                float moveY = (deltaY / vectorMagnitude) * moveSpeed;

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

    protected void moveHorizontal(float moveX){
        this.x += moveX;
        moveCollider(moveX, 0);
    }
    protected void moveVertical(float moveY){
        this.y += moveY;
        moveCollider(0, moveY);
    }

    protected void move(float moveX, float moveY){
        this.x += moveX;
        this.y += moveY;
        moveCollider(moveX,moveY);
    }
    protected void moveCollider(float moveX, float moveY){
        collider.setRect(collider.getX() + moveX, collider.getY() + moveY, collider.getWidth(), collider.getHeight());
    }

    protected void moveWithTerrainCollision(float moveX, float moveY){
        //TODO: MAKE THIS WORK:
        //SHOULD:
        //1) check collision
        //2) if found, calculate overlap distance
        //3) then move that distance backwards

        Tile[] tilesAround = state.tilesAround(this.x,this.y);

        moveHorizontalWithCollision(moveX, tilesAround);
        moveVerticalWithCollision(moveY, tilesAround);
    }

    protected void moveHorizontalWithCollision(float moveX, Tile[] horizontalTiles){
        move(moveX, 0);
        for(Tile t : horizontalTiles){
            if(t == null || !t.hasCollision) continue;

            if(this.collider.intersects(t.collider)) {

                if(t.isInteractable()) ((TileObject)t).collide(this);

                int collisionDirection = t.collider.outcode(this.collider.getCenterX(),this.collider.getCenterY());

                float tx1 = (float) t.collider.getX();
                float tx2 = tx1 + (float) t.collider.getWidth();

                float ex1 = (float) this.collider.getX();
                float ex2 = ex1 + (float) this.collider.getWidth();

                if (moveX != 0 && (collisionDirection & Rectangle2D.OUT_RIGHT) != 0) this.move(tx2 - ex1, 0);
                if (moveX != 0 && (collisionDirection & Rectangle2D.OUT_LEFT) != 0) this.move(0 - (ex2 - tx1), 0);

            }
        }
    }
    protected void moveVerticalWithCollision(float moveY, Tile[] verticalTiles){
        move(0, moveY);
        for(Tile t : verticalTiles){
            if(t == null || !t.hasCollision) continue;

            if(this.collider.intersects(t.collider)) {

                if(t.isInteractable()) ((TileObject)t).collide(this);

                int collisionDirection = t.collider.outcode(this.collider.getCenterX(),this.collider.getCenterY());

                float ty1 = (float) t.collider.getY();
                float ty2 = ty1 + (float) t.collider.getHeight();

                float ey1 = (float) this.collider.getY();
                float ey2 = ey1 + (float) this.collider.getHeight();

                if (moveY != 0 && (collisionDirection & Rectangle2D.OUT_BOTTOM) != 0) this.move(0, ty2 - ey1);
                if (moveY != 0 && (collisionDirection & Rectangle2D.OUT_TOP) != 0) this.move(0, 0 - (ey2 - ty1));

            }
        }
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

    //collide(): returns a boolean value of whether the entity collides with the target
    //can be overriden to add extra functionality to entity types
    //TODO: DISTANCE CHECK BEFORE COLLISION CHECK
    public boolean collide(Entity target){
        return this.collider.intersects(target.collider);
    }

    //die(): die
    public void die(){
        this.isAlive = false;
    }
}