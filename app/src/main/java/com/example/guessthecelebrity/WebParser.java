package com.example.guessthecelebrity;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebParser {
    private static final String TAG = "WebParser";

    //extracting data from a webpage
    public static ArrayList<String> extractImages(String html) {
        ArrayList<String> imageUrls = new ArrayList<>();
        Pattern pattern = Pattern.compile("<img src=\"(.*?)\" alt"); //Extracting the celebrity image through the <img> html tag
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String url = matcher.group(1);
            imageUrls.add(url);
        }
        Log.d(TAG, "found " + imageUrls.size() + " images");
        return imageUrls;
    }

    public static ArrayList<String> extractCelebrities(String html) {
        ArrayList<String> celebrities = new ArrayList<>();
        Pattern pattern = Pattern.compile(" alt=\"(.*?)\"/>"); //Extracting the celebrity name through the html alt attribute
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String celebrity = matcher.group(1);
            celebrities.add(celebrity);
        }
        Log.d(TAG, "found " + celebrities.size() + " celebrities");
        return celebrities;
    }
}
