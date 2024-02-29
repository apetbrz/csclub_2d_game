package main;

import main.entities.*;
import main.world.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static main.Main.TILE_SIZE;
import static main.Main.RENDER_SCALE;

public class GamePanel extends JPanel {

    //TODO: WHY IS DOWN POSITIVE AND UP NEGATIVE? LEFT/RIGHT IS OKAY.

    //state: the GameState object. necessary for communication between state and panel
    private final GameState state;

    //SCREEN_WIDTH_IN_TILES:
    //SCREEN_HEIGHT_IN_TILES:
    //how wide/high the screen is, measured in tiles,
    //to have a static screen size despite differing map sizes
    public static final int SCREEN_WIDTH_IN_TILES = 16;
    public static final int SCREEN_HEIGHT_IN_TILES = 9;

    //DEFAULT_TILE: the tile that the game falls back on
    //if it encounters an error accessing a tile (i.e. out of bounds)
    //TODO: better empty space background, or restrict camera to only inbounds?
    public static final Tile DEFAULT_TILE = new Tile(null, -1, -1);

    //HITBOX_STROKE: the outline used for rendering hitboxes, thru Main.DEBUG_SHOW_HITBOXES
    private static final BasicStroke HITBOX_STROKE = new BasicStroke(RENDER_SCALE);

    //DEFAULT_IMAGE_SCALING_MODE: the scale algorithm to use when scaling images
    //i.e. when scaling entity sprites to their size value
    public static final int DEFAULT_IMAGE_SCALING_MODE = Image.SCALE_REPLICATE;

    //displayFPS: the current FPS value to display on the screen
    public double displayFPS;

    //fps: an array that stores 15 frames of fps values, for averaging
    //not very necessary at this moment, tbh
    public double[] fps;

    //cameraX, cameraY, cameraMoveSpeed:
    //these variables handle the camera location
    //and movement, in the main.world.
    //TODO: camera object? for multiple preset cameras? idk
    public int cameraX;
    public int cameraY;
    public int cameraMoveSpeed = 8;

    //CONSTRUCTOR
    public GamePanel(GameState gameState){
        //super() calls the JPanel constructor
        super();

        //link with GameState
        state = gameState;
        state.linkPanel(this);

        //initialize fps
        fps = new double[16];

        //initialize camera
        cameraX = 0;
        cameraY = 0;

        //calculate width/height of the screen, based on the tiles and render scale
        int width = (SCREEN_WIDTH_IN_TILES * TILE_SIZE) * RENDER_SCALE;
        int height = (SCREEN_HEIGHT_IN_TILES * TILE_SIZE) * RENDER_SCALE;

        //set the size of the game window to this width/height
        //this is the size that the window object in Game will pack itself to
        this.setPreferredSize(new Dimension(width,height));

        //the GamePanel is what accepts user input, and to do this,
        //it needs to be focused (when clicked on)
        this.setFocusable(true);

        //background color (just in case)
        this.setBackground(Color.RED);

        //DoubleBuffering lets the panel render invisibly,
        //and then swap the current image for the new one
        //(only updates the picture once its fully rendered)
        this.setDoubleBuffered(true);

        centerCamera();
    }

    //update(): called once per frame
    //this method should call everything that needs
    //to run every single frame,
    //such as controls and drawing to the screen
    public void update(){
        //move the camera, based on user input
        this.handleCameraMovement(state.controller);

        //repaint: handles the actual drawing to the screen
        this.repaint();
    }

    //paintComponent(): this is what gets called every frame
    //all visual rendering happens here.
    public void paintComponent(Graphics g){
        //without this line, nothing updates:
        super.paintComponent(g);

        //we use Graphics2D instead of Graphics, it has some better 2D tools
        Graphics2D g2 = (Graphics2D)g;

        //draw game stuff, in order from background -> foreground
        drawMap(g2);
        drawEntities(g2);
        drawUI(g2);

        //dispose just cleans up memory
        g2.dispose();
    }

    //drawMap(): draws the background tiles
    private void drawMap(Graphics2D g2D){
        //grab the map's 2D tile array
        Tile[][] map = state.getMapLayout();

        //startingX, startingY: the top left corner tile of the camera's visible space
        //does not include the tile that is partially cut off
        int startingX = cameraX / (TILE_SIZE*RENDER_SCALE);
        int startingY = cameraY / (TILE_SIZE*RENDER_SCALE);

        //nested for loop: iterates through all tiles
        //we start at the starting coordinates minus 1, to account for the partially cut off tiles
        //we iterate through the screen size plus one, to account for the partially cut off tiles
        for(int i = startingY - 1; i < startingY + SCREEN_HEIGHT_IN_TILES + 1; i++){
            for(int j = startingX - 1; j < startingX + SCREEN_WIDTH_IN_TILES + 1 ; j++){

                //image: the tile's image
                BufferedImage image;

                //try/catch tries to grab the tile at the coordinate j,i
                //if out of bounds, it grabs the default tile image instead
                //TODO: replace try/catch with if/else
                try {
                    image = map[i][j].image;
                }catch(ArrayIndexOutOfBoundsException | NullPointerException e){
                    image = DEFAULT_TILE.image;
                }

                //transform: an AffineTransform object
                //that handles scaling and movement of the tile texture
                AffineTransform transform = new AffineTransform();

                //we scale the tile by RENDER_SCALE
                transform.scale(RENDER_SCALE,RENDER_SCALE);

                //move it into place, based on the camera pos and RENDER_SCALE
                int imageY = (TILE_SIZE*i - (cameraY/RENDER_SCALE));
                int imageX = (TILE_SIZE*j - (cameraX/RENDER_SCALE));
                transform.translate(imageX, imageY);

                //draw the tile.
                //TODO MINOR: stitch textures together? to reduce draw call to one?
                g2D.drawImage(image, transform, null);

                //draw hitboxes if desired
                if(Main.DEBUG_SHOW_HITBOXES) {
                    //TODO: replace try/catch with if/else
                    try {
                        g2D.setColor(map[i][j].hasCollision ? Color.RED : Color.GREEN);
                        g2D.setStroke(HITBOX_STROKE);
                        Rectangle2D box = map[i][j].collider;
                        int boxX = (int)(box.getX() - cameraX / RENDER_SCALE) * RENDER_SCALE + (RENDER_SCALE/2);
                        int boxY = (int)(box.getY() - cameraY / RENDER_SCALE) * RENDER_SCALE + (RENDER_SCALE/2);
                        int boxSize = (int)(box.getWidth()) * RENDER_SCALE - (RENDER_SCALE);
                        g2D.drawRect(boxX, boxY, boxSize, boxSize);
                    }catch(ArrayIndexOutOfBoundsException e){
                        //do nothing
                    }
                }//end hitbox draw
            }
        }//end 2D loop
    }

    //drawEntities(): draws all entities loaded in the GameState object
    private void drawEntities(Graphics2D g2D){

        //this AffineTransform object will let us move/scale the image appropriately
        AffineTransform transform;

        //iterate through every entity in the GameState's loaded entities
        //TODO: FIX ORDER, SO PLAYER IS ON TOP?
        for(Entity ent : state.getEntities()){

            //reset the transform
            transform = new AffineTransform();

            //scale to the RENDER_SCALE of the game
            transform.scale(RENDER_SCALE,RENDER_SCALE);

            //grab the entity's coordinates
            //TODO: DECIDE IF I WANT SUB-PIXEL ENTITY RENDERING AT >1 RENDER_SCALE VALUES OR NOT (FLOAT x/y = YES, INT x/y = NO!!)
            float x = ent.x;
            float y = ent.y;

            //translate to the correct position, in screenspace (based on entity and camera positions)
            transform.translate((x - (float)(cameraX / RENDER_SCALE)), (y - (float)(cameraY / RENDER_SCALE)));

            //draw the entity
            g2D.drawImage(ent.image, transform, null);

            //draw hitboxes if desired
            if(Main.DEBUG_SHOW_HITBOXES) {
                g2D.setColor(ent.hasCollision ? Color.GREEN : Color.RED);
                g2D.fillOval((int) ((x - cameraX / RENDER_SCALE) * RENDER_SCALE), (int) ((y - cameraY / RENDER_SCALE) * RENDER_SCALE),2*RENDER_SCALE,2*RENDER_SCALE);

                Rectangle2D box = ent.collider;
                int boxX = (int)(box.getX() - cameraX / RENDER_SCALE) * RENDER_SCALE + (RENDER_SCALE/2);
                int boxY = (int)(box.getY() - cameraY / RENDER_SCALE) * RENDER_SCALE + (RENDER_SCALE/2);
                int boxSize = (int)(box.getWidth()) * RENDER_SCALE - (RENDER_SCALE);
                g2D.drawRect(boxX, boxY, boxSize, boxSize);
            }
        }
    }

    //drawUI(): draw game information, currently only FPS display
    private void drawUI(Graphics2D g2D) {
        //set color and font
        g2D.setColor(Color.WHITE);
        g2D.setFont(g2D.getFont().deriveFont(Font.PLAIN, (float) (g2D.getFont().getSize()/2.0 * RENDER_SCALE)));

        //draw the fps display
        //to cut it off at 2 decimal places, i multiply by 100, cast to int,
        //cast back to double, then divide by 100
        //i.e. 12.34567 -> 1234.567 -> 1234 -> 1234.0 -> 12.34
        g2D.drawString("fps:"+(double)((int)(displayFPS * 100))/100.0, 10, 12*RENDER_SCALE);

        //draw coordinates on-screen if desired (
        if(Main.DEBUG_SHOW_COORDINATES) {
            Player p = state.player;
            g2D.drawString("player: " + p.x + "," + p.y, 64 * RENDER_SCALE, 20);
            g2D.drawString("collider: " + p.collider.getX() + "," + p.collider.getY(), 64 * RENDER_SCALE, 40);
            g2D.drawString("camera: " + cameraX + "," + cameraY, 64 * RENDER_SCALE, 60);
        }
    }

    //updateFPS(): takes the time of the previous frame (in milliseconds)
    //and calculates the FPS from it
    public void updateFPS(double frameTimeMilli) {
        for(int i = 1; i < fps.length; i++){
            fps[i] = fps[i - 1];
        }
        fps[0] = 1000.0/frameTimeMilli;

        displayFPS = 0;
        for(double d : fps){
            displayFPS += d;
        }
        displayFPS = displayFPS / fps.length;
    }

    //handleCameraMovement(): moves camera around, based on controller
    public void handleCameraMovement(ControlHandler controller) {
        //i use a logical XOR with the resetCam boolean and the FORCE_CENTERED_CAMERA setting boolean
        //to determine if the camera gets centered every frame or not
        if(controller.resetCam ^ Main.FORCE_CENTERED_CAMERA){
            centerCamera();
        }
        //otherwise, the camera is free to move with the camera hotkeys
        else {
            if (controller.upCam) cameraY -= cameraMoveSpeed;
            if (controller.leftCam) cameraX -= cameraMoveSpeed;
            if (controller.downCam) cameraY += cameraMoveSpeed;
            if (controller.rightCam) cameraX += cameraMoveSpeed;
        }
    }

    //centerCamera(): center the camera view on the player.
    public void centerCamera(){
        cameraX = ((int) state.player.x * RENDER_SCALE - (RENDER_SCALE * SCREEN_WIDTH_IN_TILES * TILE_SIZE)/2 + RENDER_SCALE*state.player.size/2);
        cameraY = ((int) state.player.y * RENDER_SCALE - (RENDER_SCALE * SCREEN_HEIGHT_IN_TILES * TILE_SIZE)/2 + RENDER_SCALE*state.player.size/2);
    }
}