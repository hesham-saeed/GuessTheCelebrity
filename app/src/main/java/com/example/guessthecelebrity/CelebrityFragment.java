package com.example.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CelebrityFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "CelebrityFragment";
    private static final String CELEBRITIES_DATA_URL = "http://www.posh24.se/kandisar";
    private static final int BYTE_SIZE = 1024;
    private static final int CONNECTION_TIMEOUT_MS = 500;
    private static final int CONNECTION_ATTEMPTS = 5;
    private static final int ANSWERS_COUNT = 4;

    //UI Elements
    private Button firstButton;
    private Button secondButton;
    private Button thirdButton;
    private Button fourthButton;
    private ImageView celebrityImageView;
    private Toast answerToast;

    private LruCache<String, Bitmap> memoryCache;
    private final ArrayList<Pair<String, String>> celebritiesData = new ArrayList<>();
    private final ArrayList<String> answers = new ArrayList<>();
    private int currentQuestionIndex;
    private final Random randomIndexGenerator = new Random(); //random generator to pick 3 wrong answers
    private String correctAnswer = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_celebrity, container, false);

        setRetainInstance(true); // to retain the app data across screen orientation change

        firstButton = view.findViewById(R.id.btn_first);
        secondButton = view.findViewById(R.id.btn_second);
        thirdButton = view.findViewById(R.id.btn_third);
        fourthButton = view.findViewById(R.id.btn_fourth);
        celebrityImageView = view.findViewById(R.id.iv_celebrity_poster);

        // if it's not the first run, then just change the UI
        if (savedInstanceState != null) {
            showNextQuestion(celebritiesData.get(currentQuestionIndex).first, celebritiesData.get(currentQuestionIndex).second);
        } else {
            // make an http request to retrieve the celebrities' images and their names
            WebDownloader task = new WebDownloader();
            task.execute(CELEBRITIES_DATA_URL);

            // Instantiating an Lru Cache to retain bitmaps
            if (memoryCache == null) {
                final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / (BYTE_SIZE * BYTE_SIZE));
                Log.d(TAG, "max VM memory: " + maxMemory + "MB");
                final int cacheSize = maxMemory / 8;
                memoryCache = new LruCache<String, Bitmap>(cacheSize) {
                    @Override
                    protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                        return bitmap.getByteCount() / BYTE_SIZE;
                    }
                };
            }
        }

        firstButton.setOnClickListener(this);
        secondButton.setOnClickListener(this);
        thirdButton.setOnClickListener(this);
        fourthButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        String celebrityPressed = ((Button) v).getText().toString();
        String correctCelebrity = correctAnswer;
        if (answerToast != null)
            answerToast.cancel();
        if (celebrityPressed.equals(correctCelebrity))
            answerToast = Toast.makeText(getActivity(), "Correct!", Toast.LENGTH_SHORT);
        else
            answerToast = Toast.makeText(getActivity(), "Wrong, It was " + correctCelebrity, Toast.LENGTH_SHORT);
        answerToast.show();
        currentQuestionIndex++;
        showNextQuestion(celebritiesData.get(currentQuestionIndex).first, celebritiesData.get(currentQuestionIndex).second);
        answers.clear();
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (memoryCache.get(key) == null)
            memoryCache.put(key, bitmap);
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    //an Async task to download images and add them to the cache
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        private final String celebrityName;

        public ImageDownloader(String name) {
            this.celebrityName = name;
        }

        int connectionTimesCount = 1;

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                Log.d(TAG, "ImageDownloader URL: " + urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(CONNECTION_TIMEOUT_MS);

                InputStream in = connection.getInputStream();
                Log.d(TAG, "ImageDownloader Response " + connection.getResponseCode() + " " + connection.getResponseMessage());

                Bitmap bitmap = BitmapFactory.decodeStream(in);
                if (bitmap != null)
                    addBitmapToMemoryCache(urls[0], bitmap); //adding bitmap to the lru cache
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (SocketTimeoutException e) {
                Log.w(TAG, "SocketTimeOutException called");
                if (connectionTimesCount < CONNECTION_ATTEMPTS) { //retry downloading an image for 5 times
                    connectionTimesCount++;
                    doInBackground(urls[0]);
                } else {
                    //try retrieving the next question if the current one fails or no longer exists
                    if (currentQuestionIndex < celebritiesData.size()) {
                        currentQuestionIndex++;
                        showNextQuestion(celebritiesData.get(currentQuestionIndex).first, celebritiesData.get(currentQuestionIndex).second);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null)
                return;
            correctAnswer = celebrityName;
            celebrityImageView.setImageBitmap(bitmap);
            generateAnswers(celebrityName);
            updateButtonsWithAnswers();
        }
    }

    private void generateAnswers(String correctAnswer) {
        answers.add(correctAnswer); //adding the correct answer first
        while (answers.size() != ANSWERS_COUNT) {
            int randomIndex = randomIndexGenerator.nextInt(celebritiesData.size());
            String randomCelebrity = celebritiesData.get(randomIndex).second;
            if (!answers.contains(randomCelebrity))
                answers.add(randomCelebrity);
        }
        Collections.shuffle(answers); //shuffling the correct answer
    }

    private void updateButtonsWithAnswers() {
        firstButton.setText(answers.get(0));
        secondButton.setText(answers.get(1));
        thirdButton.setText(answers.get(2));
        fourthButton.setText(answers.get(3));
    }

    private class WebDownloader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            String result = "";
            try {
                long before = System.currentTimeMillis();
                result = NetworkUtils.getUrlString(args[0]);
                //Log.d(TAG, "result from Web Downloader" + result);
                long after = System.currentTimeMillis();
                long diff = after - before;
                Log.d(TAG, "Total time Web Downloader : " + diff + "ms");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            ArrayList<String> celebritiesImages = extractImages(s);
            ArrayList<String> celebritiesList = extractCelebrities(s);

            for (int i = 0; i < celebritiesImages.size(); i++) {
                celebritiesData.add(new Pair<>(celebritiesImages.get(i), celebritiesList.get(i)));
                //Log.d(TAG, celebritiesList.get(i) + "\t " + celebritiesImages.get(i));
            }
            Log.d(TAG, celebritiesData.toString());
            Collections.shuffle(celebritiesData);
            showNextQuestion(celebritiesData.get(currentQuestionIndex).first, celebritiesData.get(currentQuestionIndex).second);
        }
    }

    private void showNextQuestion(String imageUrl, String celebrityName) {

        Bitmap bitmap = getBitmapFromMemCache(imageUrl);
        if (bitmap != null) {
            celebrityImageView.setImageBitmap(bitmap);
            updateButtonsWithAnswers();
        } else {
            new ImageDownloader(celebrityName).execute(imageUrl);
        }
    }

    //extracting data from a webpage
    private ArrayList<String> extractImages(String html) {
        ArrayList<String> imageUrls = new ArrayList<>();
        Pattern pattern = Pattern.compile("<img src=\"(.*?)\" alt");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String url = matcher.group(1);
            imageUrls.add(url);
        }
        Log.d(TAG, "found " + imageUrls.size() + " images");
        return imageUrls;
    }

    private ArrayList<String> extractCelebrities(String html) {
        ArrayList<String> celebrities = new ArrayList<>();
        Pattern pattern = Pattern.compile(" alt=\"(.*?)\"/>");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String celebrity = matcher.group(1);
            celebrities.add(celebrity);
        }
        Log.d(TAG, "found " + celebrities.size() + " celebrities");
        return celebrities;
    }
}
