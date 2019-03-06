package edu.stanford.cs108.bunnyworld;

import java.util.ArrayList;
import java.util.Arrays;

public interface BunnyWorldConstants {
    public static final int AUDIO = 0;
    public static final int IMAGE = 1;
    public static final int FILE_COL_INDEX = 2;
    public static final int NAME_COL = 0;
    public static final int NO_PARENT = -1;
    public static final String GAMES_TABLE = "games";
    public static final String PAGES_TABLE = "pages";
    public static final String SHAPES_TABLE = "shapes";
    public static final String RESOURCE_TABLE = "resources";
    public static final double NO_CHANGE_X = -Double.MAX_VALUE;
    public static final double NO_CHANGE_Y = -Double.MAX_VALUE;
    public static final double NO_CHANGE_WIDTH = -Double.MAX_VALUE;
    public static final double NO_CHANGE_HEIGHT = -Double.MAX_VALUE;

    public static final String DATABASE_NAME = "BunnyWorldDB";
    public static final ArrayList<Integer> imgList = new ArrayList<Integer>(Arrays.asList
            (R.drawable.carrot, R.drawable.carrot2, R.drawable.death, R.drawable.duck,
                    R.drawable.edit_icon, R.drawable.fire, R.drawable.mystic, R.drawable.play_icon));
    public static final ArrayList<Integer> audioList = new ArrayList<Integer>(Arrays.asList
            (R.raw.carrotcarrotcarrot, R.raw.evillaugh, R.raw.fire, R.raw.hooray, R.raw.intro_music,
                    R.raw.munch, R.raw.munching, R.raw.woof));
}
