package edu.stanford.cs108.bunnyworld;

import android.graphics.drawable.BitmapDrawable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface BunnyWorldConstants {
    int AUDIO = 0;
    int IMAGE = 1;
    int FILE_COL_INDEX = 2;
    int NAME_COL = 0;
    int NO_PARENT = -1;
    int RES_ID_COL = 3;
    String GAMES_TABLE = "games";
    String PAGES_TABLE = "pages";
    String SHAPES_TABLE = "shapes";
    String RESOURCE_TABLE = "resources";
    double NO_CHANGE_X = -Double.MAX_VALUE;
    double NO_CHANGE_Y = -Double.MAX_VALUE;
    double NO_CHANGE_WIDTH = -Double.MAX_VALUE;
    double NO_CHANGE_HEIGHT = -Double.MAX_VALUE;

    String DATABASE_NAME = "BunnyWorldDB";

    String[] IMAGE_NAMES = { "carrot", "carrot2", "death", "duck", "fire", "mystic" };
    Integer[] imgResources = { R.drawable.carrot, R.drawable.carrot2, R.drawable.death,
            R.drawable.duck, R.drawable.mystic, R.drawable.firefirefire };
    List<Integer> imgList = Arrays.asList(imgResources);
    String[] AUDIO_NAMES = { "carrotcarrotcarrot", "evillaugh", "fire", "hooray",
            "munch", "munching", "woof" };
    Integer[] audioResources = { R.raw.carrotcarrotcarrot, R.raw.evillaugh, R.raw.fire, R.raw.hooray,
            R.raw.munch, R.raw.munching, R.raw.woof };
    List<Integer> audioList = Arrays.asList(audioResources);

    String[] ACTION_VERBS = { "goto", "play", "hide", "show" };
    String[] TRIGGER_EVENTS = { "onClick",  "onDrop", "onEnter" };
    String VERB_MODIFIER_DELIMITER = " ";
    String ACTION_DELIMITER = ";";
    String EVENT_ACTION_DELIMITER = ": ";
    String SHAPE_ACTION_DELIMITER = "-";
    String TRIGGER_DELIMITER = "\n";
    String regex = "trigger: { (verb modifier; )*}\n";

    int LEFT_SPINNER = 0;
    int RIGHT_SPINNER = 0;
    int EVENT_SPINNER = 0;
    int ACTION_SPINNER = 1;
    int VERB_SPINNER = 0;
    int MODIFIER_SPINNER = 1;
    int ADD_ROW_BUTTON = 2;
    int DELETE_ROW_BUTTON = 3;
}