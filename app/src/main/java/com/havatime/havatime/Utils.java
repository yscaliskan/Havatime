package com.havatime.havatime;

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

/**
 * Created by yasarselcukcaliskan on 7.03.2018.
 * Utility functions for the app.
 */


public final class Utils {

    private static final String LOG_TAG = Utils.class.getSimpleName();

    /**
     *
     * @param stringUrl string form of the Url for the Google Matrix Distance API
     * @return URL object for the Google Matrix Distance API
     */
    private static URL createUrl(String stringUrl){
        URL url = null;

        try{
            url = new URL(stringUrl);
        }
        catch (MalformedURLException e){
        }
        return url;
    }

    /**
     * Makes an Http request to the given URL, and returns an JSON Response.
     * @param url URL object for the Google Distance Matrix API
     * @return JSON object returned from Google Matrix Distance API
     * @throws IOException
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
            }
        }
        catch(IOException e){
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

    /**
     *
     * @param inputStream inputStream object
     * @return stringBuilder object from the stream
     * @throws IOException
     */
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

    /**
     * Parses the JSON data to extract the travel time.
     * @param jsonResponse raw JSONResponse
     * @return extracted travel time
     */
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
        }

        return travelTime;
    }

    /**
     * Organizator function that makes the Http request, gets the JSON and parses it to extract travel time.
     * @param requestUrl string Url to make the request.
     * @return travel time.
     */
    public static int fetchTravelTimeData(String requestUrl){

        URL url = createUrl(requestUrl);
        String jsonResponse = null;

        try{
            jsonResponse = makeHttpRequest(url);
        }
        catch (IOException ioException){
        }

        int travelTime = extractTravelTime(jsonResponse);
        return travelTime;
    }
}
