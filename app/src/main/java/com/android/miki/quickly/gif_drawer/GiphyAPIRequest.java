package com.android.miki.quickly.gif_drawer;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mpokr on 5/28/2017.
 */

public class GiphyAPIRequest extends AsyncTask<String, Void, String> {

    private HttpURLConnection urlConnection;
    private static final String TAG = "GiphyAPIRequest";
    private GiphyAPIResponse giphyAPIResponse;
    private String queryTerm;

    public GiphyAPIRequest(GiphyAPIResponse giphyAPIResponse) {
        this.giphyAPIResponse = giphyAPIResponse;
    }

    @Override
    protected String doInBackground(String... strings) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(strings[0]);
            queryTerm = strings[1];
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return result.toString();
    }


    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String jsonString) {
        Log.d(TAG, jsonString);
        try {
            Log.d(TAG+"r", "Retrieved gifs for this query: " + queryTerm);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            List<String> urls = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject gifInfo = jsonArray.getJSONObject(i);
                String url = gifInfo.getJSONObject("images").getJSONObject("fixed_height").getString("url");

               // Log.d(TAG, url);
                urls.add(url);
            }
            giphyAPIResponse.gifURLsRetrieved(urls);


        } catch (JSONException e) {
            Log.d(TAG, "jsonException");
            e.printStackTrace();
        }
    }




}
