package com.example.yasarselcukcaliskan.havatime;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * Created by yasarselcukcaliskan on 9.03.2018.
 */

public class TravelTimeLoader extends AsyncTaskLoader<Integer> {

    private static final String LOG_TAG = TravelTimeLoader.class.getSimpleName();

    private String mUrl;

    public TravelTimeLoader(Context context, String url){

        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {

        Log.e(LOG_TAG, "On Start Loading.");
        forceLoad();
    }

    @Override
    public Integer loadInBackground() {

        Log.e(LOG_TAG, "Load in Background.");
        if(mUrl == null){
            return null;
        }

        Integer travelTime = Utils.fetchTravelTimeData(mUrl);
        return travelTime;
    }


}
