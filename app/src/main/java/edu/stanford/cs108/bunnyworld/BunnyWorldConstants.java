package edu.stanford.cs108.bunnyworld;

import android.graphics.drawable.BitmapDrawable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BunnyWorldConstants {
    int AUDIO = 0;
    int IMAGE = 1;
    int DIR_TYPE = -1;
    int FILE_COL_INDEX = 2;
    int NAME_COL = 0;
    int NO_PARENT = -1;
    String GAMES_TABLE = "games";
    String PAGES_TABLE = "pages";
    String SHAPES_TABLE = "shapes";
    String RESOURCE_TABLE = "resources";
    double NO_CHANGE_X = -Double.MAX_VALUE;
    double NO_CHANGE_Y = -Double.MAX_VALUE;
    double NO_CHANGE_WIDTH = -Double.MAX_VALUE;
    double NO_CHANGE_HEIGHT = -Double.MAX_VALUE;


    String DATABASE_NAME = "BunnyWorldDB";

    Integer[] imgResources = { R.drawable.carrot, R.drawable.carrot2, R.drawable.death,
            R.drawable.duck, R.drawable.fire, R.drawable.mystic };
    List<Integer> imgList = Arrays.asList(imgResources);
    Integer[] audioResources = { R.raw.carrotcarrotcarrot, R.raw.evillaugh, R.raw.fire, R.raw.hooray,
            R.raw.munch, R.raw.munching, R.raw.woof };
    List<Integer> audioList = Arrays.asList(audioResources);

    String[] actionVerbs = { "goto", "play", "hide", "show" };
    String[] triggerEvents = { "onClick", "onDrop", "onEnter" };
    String VERB_MODIFIER_DELIMITER = " ";
    String ACTION_DELIMITER = "; ";
    String EVENT_ACTION_DELIMITER = ": { ";
    String TRIGGER_DELIMITER = "}\n";
    String regex = "trigger: { (verb modifier; )*}\n";
}
