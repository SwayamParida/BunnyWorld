package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class DatabaseHelper {

    //iVars
/**********************************************/
    //Variable for single instance of DBSingleton
    private static DatabaseHelper single_instance = null;
    public static SQLiteDatabase db;
    private static Context mContext;
    private static final String DATABASE_NAME = "BunnyWorldDB";

    private ArrayList<Integer> imgList = new ArrayList<Integer>(Arrays.asList
            (R.drawable.carrot, R.drawable.carrot2, R.drawable.death, R.drawable.duck,
                    R.drawable.edit_icon, R.drawable.fire, R.drawable.mystic, R.drawable.play_icon));
    private ArrayList<Integer> audioList = new ArrayList<Integer>(Arrays.asList
            (R.raw.carrotcarrotcarrot, R.raw.evillaugh, R.raw.fire, R.raw.hooray, R.raw.intro_music,
                    R.raw.munch, R.raw.munching, R.raw.woof));

    public static final int AUDIO = 0;
    public static final int IMAGE = 1;
    public static final int NO_PARENT = -1;
    public static final String GAMES_TABLE = "games";
    public static final String PAGES_TABLE = "pages";
    public static final String SHAPES_TABLE = "shapes";
    public static final String RESOURCE_TABLE = "resources";
    public static final double NO_CHANGE_X = -Double.MAX_VALUE;
    public static final double NO_CHANGE_Y = -Double.MAX_VALUE;
    public static final double NO_CHANGE_WIDTH = -Double.MAX_VALUE;
    public static final double NO_CHANGE_HEIGHT = -Double.MAX_VALUE;

    private static final int FILE_COL_INDEX = 2;
    private static final int NAME_COL = 0;


/**********************************************/

    /**
     * Private constructor. Sets up database if necessary
     */
    private DatabaseHelper(Context context) {
        db = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type ='table' AND name = 'games';", null);
        if (cursor.getCount() == 0) {
            Button button = (Button) ((Activity)mContext).findViewById(R.id.loadGameBtn);
            button.setEnabled(false);
            initializeDB();
        }
        cursor.close();
    }

    /**
     * Returns the single instance of the DatabaseHelper
     * @param context context of the caller
     */
    public static final DatabaseHelper getInstance(Context context) {
        mContext = context;
        context.deleteDatabase(DATABASE_NAME); //Erases database
        if (single_instance == null) {
            single_instance = new DatabaseHelper(context.getApplicationContext());
        } else {
            Button button = (Button) ((Activity)mContext).findViewById(R.id.loadGameBtn);
            button.setEnabled(true);
        }
        return single_instance;
    }

    /**
     * Adds a new game name to games table.
     * @param gameName string name of the game
     */
    public void addGameToTable(String gameName) {
        String cmd = "INSERT INTO games VALUES ('" + gameName + "', NULL);";
        db.execSQL(cmd);
    }

    /**
     * Given a table name and an entry name, returns boolean regarding whether an entry
     * with given name exists in given table.
     * @return returns true if there exists entryName in tableName.
     */
    public boolean entryExists(String tableName, String entryName) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE name = '" + entryName + "';", null);
        if ((cursor != null) && (cursor.getCount() != 0)) {
            cursor.close();
            return true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    /**
     * Given a table name and an entry name, returns boolean regarding whether an entry
     * with given name exists in given table.
     * @param tableName Name of table to search
     * @param entryName Name of entry to search for
     * @param parent_id Include parent_id to narrow search
     * @return returns true if there exists entryName in tableName.
     */
    public boolean entryExists(String tableName, String entryName, int parent_id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE name = '" + entryName + "' AND parent_id = " + parent_id + ";", null);
        if ((cursor != null) && (cursor.getCount() != 0)) {
            cursor.close();
            return true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    /**
     * When there is no gameName passed into gameExists, returns whether there
     * are any games at all in the games table.
     * @return returns true if there exists at least one game. False otherwise.
     */
    public boolean gameExists() {
        Cursor cursor = db.rawQuery("SELECT * FROM games", null);
        if ((cursor != null) && (cursor.getCount() != 0)) {
            cursor.close();
            return true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    /**
     * Returns the id for a given game.
     * @param tableName String name of the game.
     * @param entryName of the desired entry
     * @param parent_id Must be used if getting id of page or shape. Enter -1 otherwise.
     * @return id of corresponding game. Returns -1 if game does not exist.
     */
    public int getId(String tableName, String entryName, int parent_id) {
        if(!entryExists(tableName, entryName)) return -1;
        String cmd = "SELECT * FROM " + tableName + " WHERE name = '" + entryName + "'";
        if (parent_id != NO_PARENT) {
            cmd += "AND parent_id = " + parent_id;
        }
        cmd += ";";
        Cursor cursor = db.rawQuery(cmd, null);
        cursor.moveToFirst();
        int colIndex = cursor.getColumnIndex("_id");
        int id = cursor.getInt(colIndex);
        cursor.close();
        return id;
    }

    /**
     * Creates empty games, pages, shapes TABLES. Populates resources TABLE with
     * images and audio resources in res library.
     */
    private void initializeDB() {
        String cmd = "CREATE TABLE games (name Text, _id INTEGER PRIMARY KEY AUTOINCREMENT);"; //Create games table
        db.execSQL(cmd);
        cmd = "CREATE TABLE pages (name Text, parent_id INTEGER, _id INTEGER PRIMARY KEY AUTOINCREMENT);"; //Create pages table
        db.execSQL(cmd);
        cmd = "CREATE TABLE shapes (name Text, parent_id INTEGER, res_id INTEGER, x REAL, y REAL, width REAL, height REAL, msg Text, scripts Text, movable BOOLEAN, visible BOOLEAN, _id INTEGER PRIMARY KEY AUTOINCREMENT);"; //Create shapes table
        db.execSQL(cmd);
        cmd = "CREATE TABLE resources (name Text, resType INTEGER, file BLOB NOT NULL, _id INTEGER PRIMARY KEY AUTOINCREMENT);";
        db.execSQL(cmd);
        addAudioResources();
        addImageResources();
    }

    /**
     * Adds all drawable image resources into the database.
     */
    private void addImageResources() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        for (int curr : imgList) {
            String resourceName = mContext.getResources().getResourceEntryName(curr);
            Bitmap bitmap = ((BitmapDrawable) Objects.requireNonNull(mContext.getDrawable(curr))).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] bitmapdata = stream.toByteArray();

            ContentValues cv = new ContentValues();
            cv.put("resourceName", resourceName);
            cv.put("resType", IMAGE);
            cv.put("file", bitmapdata);
            db.insert("resources", null, cv);
            stream.reset();
        }

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds all audio file resources into database
     */
    private void addAudioResources() {
        InputStream inputStream = null;
        FileInputStream fin = null;

        for (int curr : audioList) {
            String name = mContext.getResources().getResourceEntryName(curr);
            inputStream = mContext.getResources().openRawResource(curr);
            File tempFile = null;
            try {
                //Read mp3 resource as a file
                tempFile = File.createTempFile("name", "mp3");
                tempFile.deleteOnExit();
                Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                //Store audio data file as byte[]
                byte[] audioBytes = new byte[(int)tempFile.length()];
                fin = new FileInputStream(tempFile);
                fin.read(audioBytes);

                //Put audio resource into db
                ContentValues cv = new ContentValues();
                cv.put("name", name);
                cv.put("resType", AUDIO);
                cv.put("file", audioBytes);
                db.insert("resources", null, cv);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (inputStream != null) inputStream.close();
            if (fin != null) fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param res_id Resource id of the wanted image
     * @return Bitmap of image res_id in shapes
     */
    public Bitmap getImage(int res_id) {
        String cmd = "SELECT * FROM resources WHERE _id =" + res_id + ";";
        Cursor cur = db.rawQuery(cmd, null);

        if (cur.moveToFirst()) {
            byte[] imgByte = cur.getBlob(FILE_COL_INDEX);
            cur.close();
            return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        }
        if (cur != null && !cur.isClosed()) cur.close();
        return null;
    }

    /**
     * This method will take in a resource id and attempt to convert it into an mp3.
     * @param res_id The resource id for the audio file you are seeking. Ensure that this
     *               is indeed an audio -- not image -- file.
     * @return A file containing the bytecode for your mp3 or null if no such resource was
     * found.
     */
    public File getAudioFile(int res_id) {
        String cmd = "SELECT * FROM resources WHERE _id =" + res_id + ";";
        Cursor cur = db.rawQuery(cmd, null);
        File soundDataFile = null;

        if (cur.moveToFirst()) {
            String resourceName = cur.getString(NAME_COL);
            try {
                soundDataFile = File.createTempFile(resourceName, "mp3");
                byte[] soundBytes = cur.getBlob(FILE_COL_INDEX);
                FileOutputStream fos = new FileOutputStream(soundDataFile);
                fos.write(soundBytes);

                cur.close();
                fos.close();
                return soundDataFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (cur != null && !cur.isClosed()) cur.close();
        return null;
    }

    /**
     * Adds a resource to the database.
     * @param resourceName Desired name for the resource
     * @param dataType Integer representing datatype. AUDIO = 0 / IMAGE = 1
     * @param byteData Resource data as a byte[]
     * @return Returns true if successfully added to the database.
     */
    public boolean addResource(String resourceName, int dataType, byte[] byteData) {
        if (entryExists(RESOURCE_TABLE, resourceName)) { //Checks if resource with name exists already
            Toast.makeText(mContext, "Resource with name '" + resourceName + "' already exists.", Toast.LENGTH_SHORT).show();
            return false;
        }
        ContentValues cv = new ContentValues();
        cv.put("name", resourceName);
        cv.put("resType", dataType);
        cv.put("file", byteData);
        db.insert(RESOURCE_TABLE, null, cv);
        return true;
    }

    /**
     * Changes the name of an entry within the database. Not to be used
     * @param tableName The name where entry to be changed is contained
     * @param _id The id of the entry that is to be changed
     * @param newName The new desired name of the entry
     * @return Returns true if name is successfully changed. Returns false if there is no corresponding
     *         entry to change.
     */
    public boolean changeEntryName(String tableName, int _id, String newName) {
        String cmd = "UPDATE " + tableName + " SET name = '" + newName + "' WHERE _id = '" + _id + "';";
        db.execSQL(cmd);
        return true;
    }

    /**
     * Adds a shape to the shapes table in the database.
     * @param name Shape name
     * @param parent_id Id of the page the shape belongs to
     * @param res_id Id of the resource the shape uses
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Width of shape
     * @param height Height of shape
     * @param msg Message that the shape displays
     * @param scripts Scripts for shape
     * @param moveable Boolean representing whether shape is moveable on page
     * @param visible Boolean representing whether shape is visible on page
     * @return Returns true if shape is successfully added to shapes table.
     */
    public boolean addShape(String name, int parent_id, int res_id, double x, double y, double width,
                            double height, String msg, ArrayList<String> scripts, boolean moveable, boolean visible) {

        if (entryExists(SHAPES_TABLE, name, parent_id)) {
            Toast.makeText(mContext, "Shape with name '" + name + "' already exists.", Toast.LENGTH_SHORT).show();
            return false;
        }
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("parent_id", parent_id);
        cv.put("res_id", res_id);
        cv.put("x", x);
        cv.put("y", y);
        cv.put("width", width);
        cv.put("height", height);
        cv.put("msg", msg);
        cv.put("scripts", scripts.toString());
        cv.put("moveable", moveable);
        cv.put("visible", visible);
        db.insert(SHAPES_TABLE, null, cv);
        return true;
    }

    /**
     * Removes a given entry in a given table from database.
     * @param table Name of table where desired entry is located
     * @param entry_id Unique db id for desired table entry
     * @return Returns true if the number of entries deleted > 0
     */
    public boolean removeEntry(String table, int entry_id) {
        return (db.delete(table, "_id=?", new String[]{Integer.toString(entry_id)}) > 0);
    }

    /**
     * Changes the resource that the shape accesses in the database.
     * @param shape_id id of the shape to be changed
     * @param res_id id of the new image resource to be added to shape
     */
    public void changeShapeImage(int shape_id, int res_id) {
        String cmd = "UPDATE " + SHAPES_TABLE + " SET res_id = " + res_id + " WHERE _id = " + shape_id + ";";
        db.execSQL(cmd);
    }

    /**
     * Updates the location/dimensions of the shape in the database.
     * @param shape_id Id of the shape to be changed
     * @param x X coordinate value. Use NO_CHANGE_X if value is to stay constant.
     * @param y Y coordinate value. Use NO_CHANGE_Y if value is to stay constant.
     * @param width Width of shape. Use NO_CHANGE_WIDTH if value is to stay constant.
     * @param height Height of shape. Use NO_CHANGE_HEIGHT if value is to stay constant.
     */
    public void changeShapeDimensions(int shape_id, double x, double y, double width, double height) {
        boolean commaNeeded = false;
        String myCmd = "UPDATE " + SHAPES_TABLE + " SET ";
        if (x != NO_CHANGE_X) {
            myCmd += "x =" + x;
            commaNeeded = true;
        }
        if (y != NO_CHANGE_Y) {
            if (commaNeeded) myCmd += ", ";
            myCmd += "y = " + y;
            commaNeeded = true;
        }
        if (width != NO_CHANGE_WIDTH) {
            if (commaNeeded) myCmd += ", ";
            myCmd += "width = " + width;
            commaNeeded = true;
        }
        if (height != NO_CHANGE_HEIGHT) {
            if (commaNeeded) myCmd += ", ";
            myCmd += "height = " + height;
        }


        myCmd += " WHERE shape_id = " + shape_id + ";";
        db.execSQL(myCmd);
    }

    /**
     * Updates the message that the shape will display to a user-inputted string in the database
     * @param shape_id Unique id of the shape in the database.
     * @param msg Message the shape is to display.
     */
    public void changeShapeMsg(int shape_id, String msg) {
        String cmd = "UPDATE " + SHAPES_TABLE + " SET msg = '" + msg + "' WHERE _id = " + shape_id + ";";
        db.execSQL(cmd);
    }
    /**
     * Replace the script for a specified shape in database.
     * @param shape_id id for the applicable shape
     * @param scripts ArrayList<String> containing list of wanted scripts for the specified shape
     */
    public void changeShapeScript(int shape_id, ArrayList<String> scripts) {
        String cmd = "UPDATE " + SHAPES_TABLE + " SET scripts = '" + scripts.toString() + "' WHERE _id = " + shape_id + ";";
        db.execSQL(cmd);
    }

    /**
     * Set the visibility boolean for a specified shape in the database.
     * @param shape_id unique db id for the shape
     * @param visible boolean describing shape visibility condition
     */
    public void setShapeVisibility(int shape_id, boolean visible) {
        String cmd = "UPDATE " + SHAPES_TABLE + " SET visible = " + visible + " WHERE _id = " + shape_id + ";";
        db.execSQL(cmd);
    }

    /**
     * Sets the moveable boolean for a specified shape in the database.
     * @param shape_id unique db id for the shape
     * @param moveable boolean describing shape moveable condition
     */
    public void setShapeMovability(int shape_id, boolean moveable) {
        String cmd = "UPDATE " + SHAPES_TABLE + " SET moveable = " + moveable + " WHERE _id = " + shape_id + ";";
        db.execSQL(cmd);
    }

    /**
     * Returns a TextShape from database corresonding with its id.
     * @param shape_id The id of the shape you want to retrieve
     * @param view The view in which you want the shape to appear
     * @return TextShape object
     */
    public TextShape getShape(int shape_id, View view) {
        String getShapeRow = "SELECT * FROM " + SHAPES_TABLE + " WHERE _id = " + shape_id + ";";
        Cursor cursor = db.rawQuery(getShapeRow, null);
        cursor.moveToFirst();

        String name = cursor.getString(NAME_COL);
        int res_id = cursor.getInt(2);
        float x = (float)cursor.getDouble(3);
        float y = (float)cursor.getDouble(4);
        float width = (float)cursor.getDouble(5);
        float height = (float)cursor.getDouble(6);
        String txtString = cursor.getString(7);
        boolean moveable = cursor.getInt(9) > 0;
        boolean visible = cursor.getInt(10) > 0;

        RectF bounds = new RectF(x, y, x + width, y + height);
        BitmapDrawable drawable = new BitmapDrawable(mContext.getResources(), getImage(res_id));

        TextShape shape = new TextShape(view, bounds, drawable, txtString, visible, moveable, name);
        cursor.close();

        return shape;
    }



}
