package main;

import java.util.concurrent.ThreadLocalRandom;

public class RNG {
    //RNG is a helper class, to deal with randomly generated numbers

    //percentage(): returns a floating point number from 0 to 99.999999999999 or however many digits floats have
    public static float percentage(){
        return ThreadLocalRandom.current().nextFloat() * 100;
    }

    //upTo(): returns a floating point number from 0 to maximum
    public static float upTo(int maximum){
        return ThreadLocalRandom.current().nextFloat(maximum);
    }

    //maxDistance(): returns a floating point number from negative maximum to positive maximum
    public static float maxDistance(int maximum){
        return ThreadLocalRandom.current().nextFloat(-maximum, maximum);
    }

    //TODO: MORE RNG FUNCTIONS. AS I NEED THEM.
}
