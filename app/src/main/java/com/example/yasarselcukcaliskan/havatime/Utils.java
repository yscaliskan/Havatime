package com.example.yasarselcukcaliskan.havatime;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by yasarselcukcaliskan on 7.03.2018.
 */

public final class Utils {

    private static final String LOG_TAG = Utils.class.getSimpleName();

    /*
    * Creates an URL object with the given string.
    */
    private static URL createUrl(String stringUrl){
        URL url = null;

        try{
            url = new URL(stringUrl);
        }
        catch (MalformedURLException e){
            Log.e(LOG_TAG, "Error with creating the URL.");
        }
        return url;
    }

    /*
    * Makes an Http request to the given URL, and returns an JSON Response.
    */
    private static String makeHttpRequest(URL url) throws IOException{

        String jsonResponse = "";
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;

        try{
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == 200){

                inputStream = httpURLConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
            else {
                Log.e(LOG_TAG, "Response Code: " + httpURLConnection.getResponseCode());
            }
        }
        catch(IOException e){
            Log.e(LOG_TAG, "Problem retrieving the JSON Results.", e);
        }

        finally{

            if (httpURLConnection != null){
                httpURLConnection.disconnect();
            }
            if (inputStream != null){
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException{

        StringBuilder stringBuilder = new StringBuilder();

        if(inputStream != null){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            while(line != null){
                stringBuilder.append(line);
                line = bufferedReader.readLine();
            }
        }

        return stringBuilder.toString();
    }

    private static int extractTravelTime(String jsonResponse){

        int travelTime = 0;

        try{
            JSONObject rootJSONObject = new JSONObject(jsonResponse);
            JSONArray rowsArray = rootJSONObject.getJSONArray("rows");
            for (int i=0; i<rowsArray.length(); i++){
                JSONObject currentDistance = rowsArray.getJSONObject(i);
                JSONArray currentElements = currentDistance.getJSONArray("elements");
                JSONObject currentTravel = currentElements.getJSONObject(i);
                JSONObject current_duration_in_traffic = currentTravel.getJSONObject("duration_in_traffic");
                travelTime += current_duration_in_traffic.getInt("value");
            }
        }
        catch (JSONException e){
            Log.e(LOG_TAG, "Problem parsing the JSON results.", e);
        }

        return travelTime;
    }

    public static int fetchTravelTimeData(String requestUrl){

        URL url = createUrl(requestUrl);
        String jsonResponse = null;

        try{
            jsonResponse = makeHttpRequest(url);
        }
        catch (IOException ioException){
            Log.e(LOG_TAG, "Error closing input stream.", ioException);
        }

        int travelTime = extractTravelTime(jsonResponse);
        return travelTime;
    }
}
