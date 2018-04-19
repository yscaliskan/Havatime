package com.havatime.havatime;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

/**
 * Created by yasarselcukcaliskan on 9.03.2018.
 */

public class TravelTimeLoader extends AsyncTaskLoader<Integer> {

    private String mUrl;

    public TravelTimeLoader(Context context, String url){

        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {

        forceLoad();
    }

    @Override
    public Integer loadInBackground() {

        if(mUrl == null){
            return null;
        }

        Integer travelTime = Utils.fetchTravelTimeData(mUrl);
        return travelTime;
    }


}
