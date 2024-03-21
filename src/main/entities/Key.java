package main.entities;

import main.Logger;

//TODO: CREATE "ITEM" CLASS, MAKE KEY AN ITEM INSTEAD OF ENTITY
public class Key extends Item{

    //Key: a super simple example of an object that the player can pick up
    //Only a default constructor, makes all keys identical
    public Key(){
        super("Key", 16);
    }
}
