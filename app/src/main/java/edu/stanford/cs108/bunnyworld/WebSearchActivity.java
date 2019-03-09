package edu.stanford.cs108.bunnyworld;


import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.azure.cognitiveservices.search.imagesearch.BingImageSearchAPI;
import com.microsoft.azure.cognitiveservices.search.imagesearch.BingImageSearchManager;
import com.microsoft.azure.cognitiveservices.search.imagesearch.models.ImageObject;
import com.microsoft.azure.cognitiveservices.search.imagesearch.models.ImagesModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WebSearchActivity extends AppCompatActivity {
    private EditText searchBar = null;
    private LinearLayout linearLayout;
    DatabaseHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_search);
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE|
                SYSTEM_UI_FLAG_FULLSCREEN|SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        linearLayout = findViewById(R.id.webLinLayout);
        Toast.makeText(this, "Started", Toast.LENGTH_LONG);
        searchBar = findViewById(R.id.imgSearchBar);
        dbHelper = DatabaseHelper.getInstance(WebSearchActivity.this);
        searchSetup();
    }

    private void searchSetup() {
        searchBar.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_NULL) && event.getAction() == KeyEvent.ACTION_DOWN) {
                    String query = searchBar.getText().toString();
                    searchBar.setText("");
                    processAction(query);
                }
                return true;
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before == 0 && count == 1 && s.charAt(start) == '\n') {
                    String query = s.subSequence(0, start).toString();
                    searchBar.setText("");
                    processAction(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void processAction(String query) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        if (!query.isEmpty()) performSearch(query);
    }

    private void performSearch(String query) {
        CountDownLatch cdLatch = new CountDownLatch(1);
        BingSearch searchThread = new BingSearch(WebSearchActivity.this, query, cdLatch);
        searchThread.execute("");

        try {
            cdLatch.await();
            updateView(searchThread, query);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void updateView(BingSearch searchThread, String query) {
        int padding = 15;
        ArrayList<Bitmap> imageList = searchThread.getImages();
        LinearLayout layout = findViewById(R.id.imgLinLayout);
        View.OnClickListener imgClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView imageView = (ImageView)v;
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bitmapdata = stream.toByteArray();
                dbHelper.addResource(query, BunnyWorldConstants.IMAGE, bitmapdata);
            }
        };
        for (int i = 0; i < imageList.size(); i++) {
            ImageView imageView = new ImageView(this);
            imageView.setId(i);
            imageView.setPadding(padding, padding, padding, padding);
            imageView.setImageBitmap(imageList.get(i));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setForegroundGravity(Gravity.CENTER_VERTICAL);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            imageView.setOnClickListener(imgClickListener);
            layout.addView(imageView);
        }

    }

    /*BingSearch INNER CLASS
    *-------------------------------------------------------------------
    */

    private class BingSearch extends AsyncTask<String, Void, Boolean> {

        private String searchTerm;
        private ArrayList<Bitmap> imageList = new ArrayList<>();
        private static final int NUM_WORKERS = 4;
        private ProgressDialog dialog;
        private CountDownLatch cdLatch;
        private CountDownLatch outerCdLatch;
        private Context context = null;

        public BingSearch (Context context, String searchTerm, CountDownLatch outerCdLatch) {
            this.context = context;
            this.searchTerm = searchTerm;
            this.outerCdLatch = outerCdLatch;
        }

        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Loading images. Please wait...");
            dialog.setTitle("Loading Images");
            dialog.show();
        }

        protected Boolean doInBackground(final String... args) {
            long startTime = System.nanoTime();
            try {
                final String subscriptionKey = "ad61c48c2e2d46a7930e3975ff4a4a2e";

                BingImageSearchAPI client = BingImageSearchManager.authenticate(subscriptionKey);
                runQuery(client);
                System.out.println("Time taken: " + (System.nanoTime() - startTime));
                return true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            dialog.dismiss();
            if (!success) {
                Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
            }
        }

        private void runQuery(BingImageSearchAPI client) {
            try {
                ImagesModel imageResults = client.bingImages().search()
                        .withQuery(searchTerm)
                        .withMarket("en-us")
                        .execute();

                if (imageResults != null && imageResults.value().size() > 0) {
                    imageList.clear();
                    cdLatch = new CountDownLatch(NUM_WORKERS);
                    initThreads(imageResults.value());
                    cdLatch.await();

                    System.out.println("Added images: " + imageList.size());
                } else {
                    System.out.println("Couldn't find any image results!");
                }
            } catch (Exception f) {
                System.out.println(f.getMessage());
                f.printStackTrace();
            }
            outerCdLatch.countDown();
        }

        private void initThreads(List<ImageObject> values) {
            int sectionSize = values.size()/NUM_WORKERS;
            int overlap = values.size() % NUM_WORKERS;

            int startIndex = 0;
            for (int i = 0; i < NUM_WORKERS; i++) {
                int currSize = sectionSize;
                if (i < overlap) currSize +=  1;
                Thread currWorker = new Thread(new imgWorker(startIndex, currSize, values));
                currWorker.start();
                startIndex += currSize;
            }
        }

        public ArrayList<Bitmap> getImages() {
            return imageList;
        }

        private class imgWorker implements Runnable {

            private int startIndex;
            private int sectionLen;
            private List<ImageObject> values;

            private imgWorker(int startIndex, int sectionLen, List<ImageObject> values) {
                this.startIndex = startIndex;
                this.sectionLen = sectionLen;
                this.values = values;
            }

            @Override
            public void run() {
                for (int i = startIndex; i < startIndex + sectionLen; i++) {
                    ImageObject curr = values.get(i);
                    URL currUrl = null;
                    try {
                        currUrl = new URL(curr.contentUrl());
                        Bitmap currImg = BitmapFactory.decodeStream(currUrl.openConnection().getInputStream());
                        if (currImg == null) continue;
                        synchronized (imageList) {
                            imageList.add(currImg);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                cdLatch.countDown();
            }

        }
    }

}
