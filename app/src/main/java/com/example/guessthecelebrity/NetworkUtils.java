package com.example.guessthecelebrity;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

    public static byte[] getUrlBytes(String stringUrl) throws IOException {
        URL url = new URL(stringUrl);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {

            connection = (HttpURLConnection) url.openConnection();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": " + stringUrl + " Error code" + connection.getResponseCode());
            }
            Log.d("CelebrityActivity", "Content Length: " + String.valueOf(connection.getContentLength()));

            int bytesRead;
            byte buffer[] = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public static String getUrlString(String stringUrl) throws IOException {
        return new String(getUrlBytes(stringUrl));
    }
}
