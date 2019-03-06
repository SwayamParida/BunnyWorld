package edu.stanford.cs108.bunnyworld;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.CountDownLatch;

import javax.net.ssl.HttpsURLConnection;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.azure.cognitiveservices.search.imagesearch.BingImageSearchAPI;
import com.microsoft.azure.cognitiveservices.search.imagesearch.BingImageSearchManager;
import com.microsoft.azure.cognitiveservices.search.imagesearch.models.ImageObject;
import com.microsoft.azure.cognitiveservices.search.imagesearch.models.ImagesModel;
import com.microsoft.azure.cognitiveservices.search.imagesearch.models.SearchResultsAnswer;


public class BingSearch extends Thread {

    private static Context context = null;
    private static String searchTerm;
    private static ArrayList<Bitmap> imageList = new ArrayList<>();
    private static final int NUM_WORKERS = 4;
    public BingSearch() {
        super();
    }
    private CountDownLatch cdLatch;

    public void runQuery(BingImageSearchAPI client) {
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


    public void run() {
        try {
            final String subscriptionKey = "ad61c48c2e2d46a7930e3975ff4a4a2e";

            BingImageSearchAPI client = BingImageSearchManager.authenticate(subscriptionKey);
            runQuery(client);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setContext(Context con) {
        context = con;
    }

    public static void setSearchTerm(String term) {
        searchTerm = term;
    }

    public static ArrayList<Bitmap> getImages() {
        return imageList;
    }

    private class imgWorker implements Runnable {

        private int startIndex;
        private int sectionLen;
        private List<ImageObject> values;

        public imgWorker(int startIndex, int sectionLen, List<ImageObject> values) {
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
