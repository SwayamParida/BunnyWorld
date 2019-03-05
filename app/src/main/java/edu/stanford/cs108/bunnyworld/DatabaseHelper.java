package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
     * Given a gameName string, returns boolean regarding whether game name is contained
     * in games table.
     * @return returns true if there exists a game named gameName.
     */
    public boolean gameExists(String gameName) {
        Cursor cursor = db.rawQuery("SELECT * FROM games WHERE gameName = '" + gameName + "';", null);
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
     * @param gameName String name of the game.
     * @return long id of corresponding game. Returns -1L if game does not exist.
     */
    public long getGameId(String gameName) {
        if(!gameExists(gameName)) return -1L;
        Cursor cursor = db.rawQuery("SELECT * FROM games WHERE gameName = '" + gameName + "';", null);
        cursor.moveToLast();
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
        String cmd = "CREATE TABLE games (gameName Text, _id INTEGER PRIMARY KEY AUTOINCREMENT);"; //Create games table
        db.execSQL(cmd);
        cmd = "CREATE TABLE pages (pageName Text, parent_id INTEGER, _id INTEGER PRIMARY KEY AUTOINCREMENT);"; //Create pages table
        db.execSQL(cmd);
        cmd = "CREATE TABLE shapes (shapeName Text, parent_id INTEGER, res_id INTEGER, x REAL, y REAL, width REAL, height REAL, script Text, selectable BOOLEAN, movable BOOLEAN, _id INTEGER PRIMARY KEY AUTOINCREMENT);"; //Create shapes table
        db.execSQL(cmd);
        cmd = "CREATE TABLE resources (resourceName Text, resType INTEGER, file BLOB NOT NULL, _id INTEGER PRIMARY KEY AUTOINCREMENT);";
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
                cv.put("resourceName", name);
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



}
