package main.world;

import java.util.Arrays;
import java.util.Objects;

public class Map {

    //TODO: REMOVE TileType CLASS, REPLACE tileTypes WITH Tile[], REPLACE layout WITH int[][]
    public TileType[] tileTypes;
    public Tile[][] layout;

    public int spawnX;
    public int spawnY;

    public Map(){
        tileTypes = null;
        layout = null;
    }

    public boolean isValid(){
        return Arrays.stream(layout) .flatMap(Arrays::stream) .allMatch(Objects::nonNull);
        //this weird return does the following:
        //1. turns layout from a Tile[][] to a Stream<Tile[]> (Arrays.stream(layout))
        //2. takes each Tile[] in the stream, and replaces
        //   with a Stream<Tile> (.flatMap(Arrays::stream))
        //   (these two steps turn the 2d array into a 1d stream)
        //3. checks if all are not null objects (.allMatch(Objects::nonNull)) and returns the boolean result

        //Streams are neat!!

        /*
        for(Tile[] row : layout){
            for(Tile tile : row){
                if(tile == null) return false;
            }
        }
        return true;
        */
    }
}