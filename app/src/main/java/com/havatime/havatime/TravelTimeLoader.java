package com.havatime.havatime;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * Created by yasarselcukcaliskan on 9.03.2018.
 * AsyncTaskLoader class.
 */

/**
 *
 */
public class TravelTimeLoader extends AsyncTaskLoader<Integer> {

    private String mUrl;

    /**
     *
     * @param context context
     * @param url url
     */
    public TravelTimeLoader(Context context, String url){

        super(context);
        mUrl = url;
    }


    @Override
    protected void onStartLoading() {

        forceLoad();
    }

    /**
     *
     * @return the travel time
     */
    @Override
    public Integer loadInBackground() {

        if(mUrl == null){
            return null;
        }

        Integer travelTime = Utils.fetchTravelTimeData(mUrl);
        return travelTime;
    }


}
