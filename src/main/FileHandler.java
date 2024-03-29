package main;

import main.entities.*;
import main.world.Map;
import main.world.Tile;
import main.world.Door;
import main.world.TileType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

public class FileHandler {

    //TEXTURES_FOLDER: what folder to look into for textures, in /src/
    public static final String TEXTURES_FOLDER = "textures/";

    //MAPS_FOLDER: what folder to look into for maps, in /src/
    public static final String MAPS_FOLDER = "maps/";

    //TEXTURE_FILE_EXTENSION: file extension used for texture files
    public static final String TEXTURE_FILE_EXTENSION = ".png";

    //MAP_FILE_EXTENSION: file extension used for map data files
    public static final String MAP_FILE_EXTENSION = ".txt";

    //DEFAULT_TEXTURE: the name of the default fallback texture file, if another texture is missing
    public static final String DEFAULT_TEXTURE = "default";

    //PLAYER_TEXTURE: the name of the player texture file
    public static final String PLAYER_TEXTURE = "player";

    //DIRECTION_x_SUFFIX: added onto the back of a texture file's name,
    //to indicate that the texture has directionality.
    public static final String DIRECTION_UP_SUFFIX = "_up";
    public static final String DIRECTION_LEFT_SUFFIX = "_left";
    public static final String DIRECTION_DOWN_SUFFIX = "_down";
    public static final String DIRECTION_RIGHT_SUFFIX = "_right";
    private static final String TILE_OBJECT_ALTERNATE_SUFFIX = "_alt";

    //loadImage(): takes in a file name (without extension)
    //returns a BufferedImage object of that file, for use in rendering
    public static BufferedImage loadImage(String fileName) {
        //init image and file input variables
        BufferedImage image = null;
        InputStream inputStream = null;

        //get the path to the file
        String filePath = TEXTURES_FOLDER + fileName + TEXTURE_FILE_EXTENSION;

        //try to read the image
        try {
            //get the input stream
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
            assert inputStream != null;

            //if inputStream is null, it failed to load
            image = ImageIO.read(inputStream);
        }
        //if it fails, the image isnt found
        catch (IOException | AssertionError | IllegalArgumentException e) {
            //log this
            Logger.log(1, "IMAGE: " + fileName + " NOT FOUND AT " + filePath);

            //then try to get the default image instead
            try {
                //get the file
                filePath = TEXTURES_FOLDER + DEFAULT_TEXTURE + TEXTURE_FILE_EXTENSION;
                inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);

                //check if it loaded
                assert inputStream != null;
                image = ImageIO.read(inputStream);
            }
            //if *that* fails, the default image isnt there, which is really annoying. so i crash
            catch (IOException | AssertionError f) {
                Logger.log(2, "DEFAULT IMAGE MISSING, PLS FIX", true);
            }
        }

        //return the loaded image
        return image;
    }

    //loadMap(): takes in the name of a map file (without extension)
    //returns a Map object, with each tile type in a 1D array,
    //and an actual map layout stored in a 2D array, to be stored in GameState
    public static Map loadMap(String fileName) {
        //initialize to invalid values, so if something is out of order, we know
        Map outputMap = new Map();
        int mapWidth = -1;
        int mapHeight = -1;

        //get the path to the map file
        String filePath = MAPS_FOLDER + fileName + MAP_FILE_EXTENSION;

        //inputStream: loads our map .txt file
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);

        //if inputStream is null, the file was not found. so we crash
        if(inputStream == null) Logger.log(2, "FAILED TO FIND MAP AT " + filePath, true);
        assert inputStream != null;

        //bufferedReader: reads the file
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        //lines: an array of all the lines in the file
        //we load all the lines in the file to a single String array
        String[] lines = bufferedReader.lines().toArray(String[]::new);

        //since we read everything at once, we go ahead and close the reader, to not waste resources
        try {
            bufferedReader.close();
        } catch (IOException e) {
            Logger.log(1,"IO EXCEPTION ON BUFFEREDREADER CLOSE");
            Arrays.stream(e.getStackTrace()).forEach(element -> Logger.log(1,element.toString()));
        }

        //line: the String value read for the current line
        String line;

        //lineNumber: keeps track of what file line we are on
        int lineNumber = 0;

        //maxLine: how many lines there are in total
        int maxLine = lines.length - 1;

        //loop until out lines in file:
        while(lineNumber < maxLine) {

            //grab the next current line (and increment the count)
            line = lines[lineNumber++];

            //this if-else block checks for the different data prefixes
            //ignore blank lines and lines starting with // (comments)
            if (line.startsWith("//") || line.isBlank()) {
                //this enables comments and ignores empty lines
                continue;
            }

            //size: holds the size of the map //TODO: REMOVE? DYNAMIC SIZING FROM LAYOUT??
            //with the format: [width]x[height] (lowercase x)
            else if (line.startsWith("size:")) {

                //grab the data (stored after the colon) and split it by the 'x' in the middle
                String[] widthAndHeight = line.substring(line.indexOf(':') + 1).trim().split("x");

                //store the values
                mapWidth = Integer.parseInt(widthAndHeight[0].trim());
                mapHeight = Integer.parseInt(widthAndHeight[1].trim());

                Logger.log(0,"map size loaded");
            }

            //tiles: holds the individual data
            //with the format:
            //tiles:
            //[tile code] - [tile name/file name] - [boolean for collision value]
            //[END WITH EMPTY LINE]
            else if (line.startsWith("tiles:")) {

                Logger.log(0,"loading tile data");

                //grab the number of tiles (check lines until we see a blank line)
                int numberOfTiles = 0;
                while(!lines[lineNumber + numberOfTiles].isBlank()){
                    numberOfTiles++;
                }

                //create a new array in the map object, to hold the tile types
                outputMap.tileTypes = new TileType[numberOfTiles];

                //for however many tiles we expect to see,
                for (int i = 0; i < numberOfTiles; i++) {

                    //grab the next current line (and increment the count)
                    if(lineNumber > maxLine) {
                        Logger.log(1, "END OF FILE REACHED, MISSING MAP DATA");
                    }
                    line = lines[lineNumber++];

                    //if we find a blank line, assume that the tile data ended
                    if(line.isBlank()){
                        Logger.log(1, "TOO FEW TILES FOUND!! CHECK TILE COUNT");
                        break;
                    }

                    //split the line up using the dashes
                    String[] tileData = line.split("-");

                    //here is all the relevant data:
                    int tileIndex;
                    String tileName;
                    String tileNameAlt; //used for TileObjects
                    boolean tileHasCollision;

                    //grab the tile information from the split line
                    //if length == 3, its a regular tile
                    if(tileData.length == 3) {
                        //grab the data
                        tileIndex = Integer.parseInt(tileData[0].trim());
                        tileName = tileData[1].trim();
                        tileHasCollision = Boolean.parseBoolean(tileData[2].trim());

                        //create the object
                        TileType tileToLoad = new TileType(tileName, tileHasCollision);

                        //store it
                        outputMap.tileTypes[tileIndex] = tileToLoad;
                    }
                    //if length == 2, its a prefab TileObject
                    else if(tileData.length == 2){
                        //grab the data
                        tileIndex = Integer.parseInt(tileData[0].trim());
                        tileName = tileData[1].trim();
                        tileNameAlt = tileName + TILE_OBJECT_ALTERNATE_SUFFIX;

                        //create the object
                        TileType tileToLoad = new TileType(tileName, tileNameAlt);

                        //store it
                        outputMap.tileTypes[tileIndex] = tileToLoad;
                    }


                }

                Logger.log(0,"tiles loaded");
            }

            //layout: holds the 2D tile array data
            //with the format:
            //each line of text = a row of tiles
            //each tile separated by whitespace (" ")
            else if (line.startsWith("layout:")) {

                Logger.log(0,"loading map layout");

                //if loadedTiles is null, the tile information wasn't
                //loaded before the layout.
                //cannot load without this, so crash
                if (outputMap.tileTypes == null) Logger.log(2, "TILE INFO MISSING BEFORE LAYOUT", true);

                //initialize the 2D array with the given width and height:
                //if width/height are less than one (or negative), throw an error and crash
                if(mapHeight < 1 || mapWidth < 1) Logger.log(2, "MAP SIZE MISSING/INVALID", true);
                //otherwise, initialize the array
                else outputMap.layout = new Tile[mapHeight][mapWidth];

                //loop through the tile data lines
                for (int vertical = 0; vertical < mapHeight; vertical++) {
                    Logger.initProgressBar(0,mapWidth);
                    //grab the next current line (and increment the count)
                    if(lineNumber > maxLine){
                        Logger.log(1,"END OF FILE REACHED, MISSING MAP DATA");
                        break;
                    }
                    line = lines[lineNumber++];

                    //split it by spaces
                    String[] row = line.split(" ");

                    //iterate through each integer in the line
                    //and load the appropriate tile into the 2D array
                    for (int horizontal = 0; horizontal < mapWidth; horizontal++) {
                        //initialize tile value to -1 for default tile
                        int tileValue = -1;

                        //try to grab the next tile value
                        try {
                            tileValue = Integer.parseInt(row[horizontal]);
                        } catch (IndexOutOfBoundsException | NumberFormatException e) {
                            //if invalid data, log it and skip the rest of the loop iteration
                            Logger.log(2, "MAP DATA INVALID AT POSITION " + horizontal + "," + vertical);
                            continue;
                        }

                        //calculate the X and Y coordinates of the tile
                        int x = horizontal * Main.TILE_SIZE;
                        int y = vertical * Main.TILE_SIZE;

                        //if tile value and position is valid, we create and store the tile object
                        Tile newTile;
                        if(outputMap.tileTypes[tileValue].isInteractable){
                            newTile = new Door(outputMap.tileTypes[tileValue], x,y);
                        }
                        else{
                            newTile = new Tile(outputMap.tileTypes[tileValue], x, y);
                        }
                        outputMap.layout[vertical][horizontal] = newTile;
                        Logger.tick(0);
                    }

                    Logger.log(0, "\tfinished line " + vertical);
                }

                Logger.log(0,"map loaded");

                //end layout: section
            }

            //spawn: holds the coordinate of the tile that the player will spawn in
            //with the format: [x coord],[y coord]
            //0,0 represents the top left tile
            else if(line.startsWith("spawn:")){

                //grab the data (stored after the colon) and split it by the ',' in the middle
                String[] tileCoordinates = line.substring(line.indexOf(':') + 1).trim().split(",");

                //store the values
                outputMap.spawnX = Integer.parseInt(tileCoordinates[0].trim());
                outputMap.spawnY = Integer.parseInt(tileCoordinates[1].trim());

                Logger.log(0,"spawn point loaded");

            }

            //entities: holds entities that are spawned on load
            //with the format:
            //entities:
            //[entity/file name] - [prefab AI type] - [size] - [movespeed] - [spawn x coord],[spawn y coord] ( - [boolean entity has directionality] OPTIONAL)
            //[END WITH EMPTY LINE]
            else if(line.startsWith("entities:")){

                Logger.log(0, "loading entities");

                //grab how many entities we expect to see
                int entityCount = 0;
                while(!lines[lineNumber + entityCount].isBlank()){
                    entityCount++;
                }

                //initialize the array
                outputMap.initialEntities = new Entity[entityCount];

                //iterate thru the lines, grabbing every entity
                for(int i = 0; i < entityCount; i++){
                    //grab the next line
                    if(lineNumber > maxLine){
                        Logger.log(1,"END OF FILE REACHED, MISSING ENTITY DATA");
                        break;
                    }
                    line = lines[lineNumber++];

                    //split up the entity's data
                    String[] entityInfo = line.split("-");

                    //grab the relevant values and store them in convenient variables
                    String entityName = null;
                    String entityType = null;
                    int entitySize = -1;
                    float entityMovespeed = -1;
                    String[] entitySpawnInfo;
                    int entitySpawnX;
                    int entitySpawnY;
                    //for the boolean, we assume false, unless there *is* something there
                    boolean entityDirectional = false;

                    switch(entityInfo.length){
                        case 2:
                            entityType = entityInfo[0].trim();
                            entitySpawnInfo = entityInfo[1].split(",");
                            break;
                        case 6:
                            entityDirectional = Boolean.parseBoolean(entityInfo[5].trim());
                        case 5:
                            entityName = entityInfo[0].trim();
                            entityType = entityInfo[1].trim();
                            entitySize = Integer.parseInt(entityInfo[2].trim());
                            entityMovespeed = Float.parseFloat(entityInfo[3].trim());
                            entitySpawnInfo = entityInfo[4].split(",");
                            break;
                        default:
                            Logger.log(2,"INVALID ENTITY AT LINE " + line + ", SKIPPING");
                            continue;
                    }

                    entitySpawnX = Integer.parseInt(entitySpawnInfo[0].trim());
                    entitySpawnY = Integer.parseInt(entitySpawnInfo[1].trim());

                    //check what entity type it is, and create that entity
                    //TODO: MOVE ENTITY TYPING OUT OF FileHandler!!!! SHOULD BE SOMEWHERE EASIER TO MODIFY
                    switch (entityType) {
                        case "critter" -> outputMap.initialEntities[i] = new Critter(entityName, entitySize, entityMovespeed, entityDirectional);
                        case "snail" -> outputMap.initialEntities[i] = new Snail(entityName, entitySize, entityMovespeed, entityDirectional);
                        case "scaredycat" -> outputMap.initialEntities[i] = new ScaredyCat(entityName, entitySize, entityMovespeed, entityDirectional);
                        case "key" -> outputMap.initialEntities[i] = new Key();
                    }

                    //finally, move the entity to its spawn point
                    outputMap.initialEntities[i].setTileLocation(entitySpawnX,entitySpawnY);
                }

                Logger.log(0, "entities loaded");
            }

            //if no other command is met, we have a line that we do not understand
            //log this
            else{
                Logger.log(1,"INVALID MAP LINE AT LINE " + lineNumber);
            }

        }   //end file loop

        Logger.log(0, "checking map");
        //check validity of map, throw a warning if an error is found
        if(!outputMap.isValid()) Logger.log(2, "ERROR IN MAP LAYOUT");

        Logger.log(0, "map loaded!");

        return outputMap;
    }

}