package main.entities;
import main.*;
import main.world.Tile;
import org.w3c.dom.css.Rect;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Entity {
    //game: the GameState object, so the entity can interact with other things
    protected static GameState state;

    //name: the name of the entity (used for display)
    public String name;

    //image: the sprite of the entity
    public BufferedImage image;

    //up/left/down/rightImage: these hold the four images used for the four directions
    //if hasDirectionality == false, these are all null.
    private BufferedImage upImage;
    private BufferedImage leftImage;
    private BufferedImage downImage;
    private BufferedImage rightImage;

    //hasDirectionality: states whether the entity rotates/changes image as it changes direction
    public boolean hasDirectionality;

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
    public Entity(String name, int size, int moveSpeed) {
        this();

        this.name = name;
        this.size = size;
        this.moveSpeed = moveSpeed;

        setImage(name);

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
    protected void setImage(String fileName) {
        image = FileHandler.loadImage(fileName);
    }

    //setImagesWithDirectionality(): same as above, but
    //handles the four directional images
    protected void setImagesWithDirectionality(String fileName){
        hasDirectionality = true;

        upImage = FileHandler.loadImage(fileName + FileHandler.DIRECTION_UP_SUFFIX);
        leftImage = FileHandler.loadImage(fileName + FileHandler.DIRECTION_LEFT_SUFFIX);
        downImage = FileHandler.loadImage(fileName + FileHandler.DIRECTION_DOWN_SUFFIX);
        rightImage = FileHandler.loadImage(fileName + FileHandler.DIRECTION_RIGHT_SUFFIX);

        image = downImage;
    }

    //moveTarget(): sets the X and Y main.world coordinates that the entity will move towards automatically
    public void moveTargetPoint(float X, float Y) {
        targetX = centerToCorner(X);
        targetY = centerToCorner(Y);
    }
    public void moveTargetPoint(float[] xy){
        targetX = centerToCorner(xy[0]);
        targetY = centerToCorner(xy[1]);
    }

    //moveVector(): similar to moveTarget(), but relative to the entity itself (directional/WASD controls)
    public void moveRelativeToPosition(float X, float Y){
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
                if(canMoveHorizontal(deltaX)) x = targetX;
                if(canMoveVertical(deltaY)) y = targetY;
            } else {
                //otherwise, move towards target

                //moveX, moveY: x/y values normalized (to prevent diagonal movement being ~1.4x the speed)
                float moveX = (deltaX / vectorMagnitude) * moveSpeed;
                float moveY = (deltaY / vectorMagnitude) * moveSpeed;

                //now, if i just checked for collision and moved, and nothing else,
                //id run into an issue where there would be a gap between the player and walls,
                //if the gap was less than one moveX or moveY away.
                //to fix this, i do this:

                //as long as we haven't moved the full distance given by moveX/moveY,
                //keep checking if we can move *some distance* (remainingDistance), starting at moveX/moveY
                //if we cant (but still have distance to move this frame),
                //divide that "some distance" by two

                //effectively, this is like infinitely adding fractional distances,
                //until we reach the limit of floating point accuracy
                //at which point, we are pixel-perfect against the wall blocking us!

                //X COMPONENT
                //distanceTraveled: keeps track of how far we've moved
                float distanceTraveled = 0;
                //remainingDistance: keeps track of how far we still could maybe move
                float remainingDistance = moveX;

                //so long as we could still potentially move a distance,
                while(remainingDistance != 0){

                    //check if we can move the potential distance
                    if(canMoveHorizontal(remainingDistance)){

                        //if so, move the distance
                        x += remainingDistance;

                        //update collider to new position
                        collider.setRect(collider.getX() + remainingDistance, collider.getY(), collider.getWidth(), collider.getHeight());

                        //and add it to the distanceTraveled
                        distanceTraveled += remainingDistance;
                    }

                    //check to make sure that we haven't traveled too far/are done traveling
                    if(Math.abs(distanceTraveled) > Math.abs(moveX)){
                        //if so, exit the loop
                        break;
                    }

                    //otherwise, divide the potential distance by 2
                    remainingDistance = remainingDistance / 2;
                }

                //Y COMPONENT
                //distanceTraveled: keeps track of how far we've moved
                distanceTraveled = 0;
                //remainingDistance: keeps track of how far we still could maybe move
                remainingDistance = moveY;
                while(remainingDistance != 0){

                    //check if we can move the potential distance
                    if(canMoveVertical(remainingDistance)){

                        //if so, move the distance
                        y += remainingDistance;

                        //update collider to new position
                        collider.setRect(collider.getX(), collider.getY() + remainingDistance, collider.getWidth(), collider.getHeight());

                        //and add it to the distanceTraveled
                        distanceTraveled += remainingDistance;
                    }

                    //check to make sure that we haven't traveled too far/are done traveling
                    if(Math.abs(distanceTraveled) > Math.abs(moveY)){
                        //if so, exit the loop
                        break;
                    }

                    //otherwise, divide the potential distance by 2
                    remainingDistance = remainingDistance / 2;
                }
            }
        }

        //if the entity has directionality enabled,
        //we change the image rendered based on what direction they are moving
        if(hasDirectionality) {
            //currently prioritizes horizontal movement over vertical movement
            //(if moving diagonal, entity will face left/right
            //TODO: keep first direction traveled from standstill (how? we'll see)

            // <-
            if (deltaX < 0) image = leftImage;
            // ->
            else if (deltaX > 0) image = rightImage;
            // ^
            else if (deltaY < 0) image = upImage;
            // V
            else if (deltaY > 0) image = downImage;
        }
    }

    //canMoveHorizontal: checks for collision and returns if we can move or not
    //parameter moveX: the X component of movement to check for
    protected boolean canMoveHorizontal(float moveX) {
        //if we have no collision, we can always move!
        if(!hasCollision) return true;

        //need to check two tiles, one for each "shoulder" of the player.
        Tile t1;
        Tile t2;

        float x = (float) this.collider.getX();
        float y1 = (float) this.collider.getY();
        float y2 = y1 + (float) this.collider.getHeight() - 1 - 1;
        //FOR SOME REASON, there is always a 1-pixel gap
        //on the bottom/right edges
        //only fixed by subtracting one!!! idk why.
        //i had to subtract ANOTHER ONE!!!! bc otherwise movement
        //wouldnt work, if against a wall on the bottom/right side of the player
        //???

        //if moveX < 0, we are moving left
        //if we are moving right, we check from the right edge
        //(this.x is the left edge)
        if (!(moveX < 0)) {
            //FOR SOME REASON, there is always a 1-pixel gap
            //on the bottom/right edges
            //only fixed by subtracting one!!! idk why.
            x = x + (float) this.collider.getWidth() - 1;

        }

        //grab the two tiles to check, one for each "shoulder"
        t1 = state.tileAt(x + moveX, y1);
        t2 = state.tileAt(x + moveX, y2);

        //we can move so long as neither "shoulder" is colliding with a tile
        //if the tiles are null, we treat them as having solid collision
        if(t1 == null || t2 == null) return false;
        boolean collide = t1.hasCollision || t2.hasCollision;
        return !collide;
    }

    //canMoveVertical: checks for collision and returns if we can move or not
    //parameter moveY: the Y component of movement to check for
    protected boolean canMoveVertical(float moveY) {
        //if we have no collision, we can always move!
        if(!hasCollision) return true;

        //need to check two tiles, one for each "shoulder" of the player.
        Tile t1;
        Tile t2;

        float y = (float) this.collider.getY();
        float x1 = (float) this.collider.getX();
        float x2 = x1 + (float) this.collider.getWidth() - 1 - 1;
        //FOR SOME REASON, there is always a 1-pixel gap
        //on the bottom/right edges
        //only fixed by subtracting one!!! idk why.
        //i had to subtract ANOTHER ONE!!!! bc otherwise movement
        //wouldnt work, if against a wall on the bottom/right side of the player
        //???

        //if moveY < 0, we are moving up
        //if we are moving down, we check from the bottom edge
        //(this.y is the top edge)
        if (!(moveY < 0)) {
            //FOR SOME REASON, there is always a 1-pixel gap
            //on the bottom/right edges
            //only fixed by subtracting one!!! idk why.
            y = y + (float) this.collider.getHeight() - 1;
        }
        t1 = state.tileAt(x1, y + moveY);
        t2 = state.tileAt(x2, y + moveY);

        //we can move so long as neither "shoulder" is colliding with a tile
        //if the tiles are null, we treat them as having solid collision
        if(t1 == null || t2 == null) return false;
        boolean collide = t1.hasCollision || t2.hasCollision;
        return !collide;
    }

    //setLocation: moves the entity to the specific coordinates in worldspace,
    //centered on the point
    public void setLocation(int x, int y){
        Logger.log(0,name + " getting set to: " + x + "," + y);
        float deltaX = this.x + valueToCenterOfEntity(x);
        float deltaY = this.y + valueToCenterOfEntity(y);
        this.x += deltaX;
        this.y += deltaY;
        this.targetX = x;
        this.targetY = y;
        collider.setRect(collider.getX() + deltaX, collider.getY() + deltaY, collider.getWidth(), collider.getHeight());
    }
    public void setTileLocation(int x, int y){
        this.setLocation(x * Main.TILE_SIZE, y * Main.TILE_SIZE);
    }

    //valueToCenterOfEntity(): turns an axis value (X/Y)
    //from the top left corner of the entity to the center of the entity
    public float valueToCenterOfEntity(float value){
        return value + (float)this.size/2;
    }

    public float centerToCorner(float value){
        return value - (float)this.size/2;
    }

    //isPlayer(): returns true if the current entity is a player. i override this in Player.java
    public boolean isPlayer(){
        return false;
    }

    protected float[] getCenter() {
        return new float[]{this.valueToCenterOfEntity(this.x), this.valueToCenterOfEntity(this.y)};
    }
}