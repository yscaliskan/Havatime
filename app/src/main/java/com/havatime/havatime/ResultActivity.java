package com.havatime.havatime;

import android.app.LoaderManager;
import android.content.Loader;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.havatime.havatime.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by yasarselcukcaliskan on 10.03.2018.
 * Result Activity Page.
 */

public class ResultActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Integer>{

    private static final String DISTANCE_MATRIX_API_REQUEST_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?";
    private static final String API_KEY = "AIzaSyC7QKDUunHZWYafcuTi_75XvXddrgogbmI";
    private static final int LOADER_ID = 0;
    private static final int MAX_DOMESTIC_AIRPORT_TIME = 6300;
    private static final int MIN_DOMESTIC_AIRPORT_TIME = 2700;
    private static final int MAX_INTERNATIONAL_AIRPORT_TIME = 9000;
    private static final int MIN_INTERNATIONAL_AIRPORT_TIME = 5400;
    private static final int SHUTTLE_INTERVAL_IN_MILLIS = 1800000;
    private static final int TAKSIM_TO_SABIHA_MIN_BOARDING_TIME_IN_MILLIS = 5400000;
    private static final int KADIKOY_TO_SABIHA_MIN_BOARDING_TIME_IN_MILLIS = 3000000;
    private static final int YENISAHRA_TO_SABIHA_MIN_BOARDING_TIME_IN_MILLIS = 2400000;
    private static final int TAKSIM_TO_ATATURK_MIN_BOARDING_TIME_IN_MILLIS = 3600000;
    private static final int SABIHA_LUGGAGE_TIME = 1800;
    private static final int ATATURK_LUGGAGE_TIME = 1200;
    private static final int INTERNATIONAL_RISK_CONSTANT = 36;
    private static final int DOMESTIC_RISK_CONSTANT = 36;
    private static Calendar currentDate = Calendar.getInstance();

    private TravelTimeLoader travelTimeLoader;
    private LoaderManager loaderManager;
    private TextView destinationTextView;
    private LinearLayout linearLayout1;
    private LinearLayout linearLayout2;
    private LinearLayout linearLayout3;
    private TextView emptyStateTextView;
    public Calendar firstShuttleTime;
    private TextView shuttleTime1;
    private TextView shuttleTime2;
    private TextView shuttleTime3;
    private TextView shuttleRisk1;
    private TextView shuttleRisk2;
    private TextView shuttleRisk3;
    
    private int count = 0;

    public ArrayList<Calendar> shuttleTimes = new ArrayList<Calendar>();

    public ArrayList<Integer> shuttleRisks = new ArrayList<Integer>();

    Bundle bundle = new Bundle();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.result_activity);

        emptyStateTextView = findViewById(R.id.empty_state_text_view);
        emptyStateTextView.setText(R.string.no_shuttles);
        emptyStateTextView.setVisibility(View.INVISIBLE);

        destinationTextView = findViewById(R.id.destination_header_text_view);

        linearLayout1 = findViewById(R.id.linear_layout_1);
        linearLayout1.setVisibility(View.INVISIBLE);
        linearLayout2 = findViewById(R.id.linear_layout_2);
        linearLayout2.setVisibility(View.INVISIBLE);
        linearLayout3 = findViewById(R.id.linear_layout_3);
        linearLayout3.setVisibility(View.INVISIBLE);

        shuttleTime1 = findViewById(R.id.shuttle_time_1);
        shuttleTime2 = findViewById(R.id.shuttle_time_2);
        shuttleTime3 = findViewById(R.id.shuttle_time_3);
        shuttleRisk1 = findViewById(R.id.shuttle_risk_1);
        shuttleRisk2 = findViewById(R.id.shuttle_risk_2);
        shuttleRisk3 = findViewById(R.id.shuttle_risk_3);

        firstShuttleTime = calculateFirstShuttle();

        calculateTravelTime(firstShuttleTime);
    }

    private Calendar calculateFirstShuttle(){

        Calendar selectedDate = MainActivity.getSelectedDate();

        Calendar firstShuttleTime = Calendar.getInstance();

        switch (AirportOrganizer.getAirport()){
            case ATATURK:
                destinationTextView.setText(ResultActivity.this.getString(R.string.TAKSIM) + " -> " + ResultActivity.this.getString(R.string.IST));
                firstShuttleTime.setTimeInMillis(selectedDate.getTimeInMillis() - TAKSIM_TO_ATATURK_MIN_BOARDING_TIME_IN_MILLIS);
                break;
            case SABIHA_GOKCEN:
                switch (AirportOrganizer.getBoardingPoint()){
                    case TAKSIM:
                        destinationTextView.setText(ResultActivity.this.getString(R.string.TAKSIM) + " -> " + ResultActivity.this.getString(R.string.SAW));
                        firstShuttleTime.setTimeInMillis(selectedDate.getTimeInMillis() - TAKSIM_TO_SABIHA_MIN_BOARDING_TIME_IN_MILLIS);
                        break;
                    case KADIKOY:
                        destinationTextView.setText(ResultActivity.this.getString(R.string.KADIKOY) + " -> " + ResultActivity.this.getString(R.string.SAW));
                        firstShuttleTime.setTimeInMillis(selectedDate.getTimeInMillis() - KADIKOY_TO_SABIHA_MIN_BOARDING_TIME_IN_MILLIS);
                        break;
                    case YENISAHRA:
                        destinationTextView.setText(ResultActivity.this.getString(R.string.YENISAHRA) + " -> " + ResultActivity.this.getString(R.string.SAW));
                        firstShuttleTime.setTimeInMillis(selectedDate.getTimeInMillis() - YENISAHRA_TO_SABIHA_MIN_BOARDING_TIME_IN_MILLIS);
                        break;
            }
        }
        if(MainActivity.isInternational()) firstShuttleTime.setTimeInMillis(firstShuttleTime.getTimeInMillis() - 4500000);
        if(MainActivity.haveLuggage()) firstShuttleTime.setTimeInMillis(firstShuttleTime.getTimeInMillis() - 1800000);
        switch (AirportOrganizer.getBoardingPoint()){
            case KADIKOY:
                if(firstShuttleTime.get(Calendar.MINUTE) >= 30) firstShuttleTime.set(Calendar.MINUTE, 45);
                else firstShuttleTime.set(Calendar.MINUTE, 15);
                break;
            default:
                if(firstShuttleTime.get(Calendar.MINUTE) >= 30) firstShuttleTime.set(Calendar.MINUTE, 30);
                else firstShuttleTime.set(Calendar.MINUTE, 00);
                break;
        }
        return firstShuttleTime;
    }

    private int calculateRisk(int travelTime, long shuttleTimeInMillis){

        int risk;
        Calendar selectedDate = MainActivity.getSelectedDate();
        Long flightTimeInSeconds = selectedDate.getTimeInMillis() /1000;
        Long shuttleTimeInSeconds = shuttleTimeInMillis / 1000;
        int timeAtAirport;

        if(MainActivity.isInternational()){

            if(MainActivity.haveLuggage()){

                switch (AirportOrganizer.getAirport()){
                    case ATATURK:
                        timeAtAirport = (int) (flightTimeInSeconds - shuttleTimeInSeconds - travelTime - ATATURK_LUGGAGE_TIME);
                        if (timeAtAirport >= MAX_INTERNATIONAL_AIRPORT_TIME) return 3;
                        risk =  (int) Math.floor(100 - (timeAtAirport - MIN_INTERNATIONAL_AIRPORT_TIME) /
                                INTERNATIONAL_RISK_CONSTANT);
                        break;
                    case SABIHA_GOKCEN:
                        timeAtAirport = (int) (flightTimeInSeconds - shuttleTimeInSeconds - travelTime - SABIHA_LUGGAGE_TIME);
                        if (timeAtAirport >= MAX_INTERNATIONAL_AIRPORT_TIME) return 3;
                        risk =  (int) Math.floor(100 - (timeAtAirport - MIN_INTERNATIONAL_AIRPORT_TIME) /
                                INTERNATIONAL_RISK_CONSTANT);
                        break;
                    default:
                        timeAtAirport = (int) (flightTimeInSeconds - shuttleTimeInSeconds - travelTime - ATATURK_LUGGAGE_TIME);
                        if (timeAtAirport >= MAX_INTERNATIONAL_AIRPORT_TIME) return 3;
                        risk =  (int) Math.floor(100 - (timeAtAirport - MIN_INTERNATIONAL_AIRPORT_TIME) /
                                INTERNATIONAL_RISK_CONSTANT);
                        break;
                }
            }
            else{

                timeAtAirport = (int) (flightTimeInSeconds - shuttleTimeInSeconds - travelTime);
                if (timeAtAirport >= MAX_INTERNATIONAL_AIRPORT_TIME) return 3;
                risk = (int) Math.floor(100 - (timeAtAirport - MIN_INTERNATIONAL_AIRPORT_TIME) /
                        INTERNATIONAL_RISK_CONSTANT);
            }
        }
        else {

            if(MainActivity.haveLuggage()){

                switch (AirportOrganizer.getAirport()){

                    case ATATURK:
                        timeAtAirport = (int) (flightTimeInSeconds - shuttleTimeInSeconds - travelTime - ATATURK_LUGGAGE_TIME);
                        if (timeAtAirport >= MAX_DOMESTIC_AIRPORT_TIME) return 3;
                        risk = (int) Math.floor(100 - (timeAtAirport - MIN_DOMESTIC_AIRPORT_TIME) /
                                DOMESTIC_RISK_CONSTANT);
                        break;
                    case SABIHA_GOKCEN:
                        timeAtAirport = (int) (flightTimeInSeconds - shuttleTimeInSeconds - travelTime - SABIHA_LUGGAGE_TIME);
                        if (timeAtAirport >= MAX_DOMESTIC_AIRPORT_TIME) return 3;
                        risk = (int) Math.floor(100 - (timeAtAirport - MIN_DOMESTIC_AIRPORT_TIME) /
                                DOMESTIC_RISK_CONSTANT);
                        break;
                    default:
                        timeAtAirport = (int) (flightTimeInSeconds - shuttleTimeInSeconds - travelTime - ATATURK_LUGGAGE_TIME);
                        if (timeAtAirport >= MAX_DOMESTIC_AIRPORT_TIME) return 3;
                        risk = (int) Math.floor(100 - (timeAtAirport - MIN_DOMESTIC_AIRPORT_TIME) /
                                DOMESTIC_RISK_CONSTANT);
                        break;
                }
            }
            else{

                timeAtAirport = (int) (flightTimeInSeconds - shuttleTimeInSeconds - travelTime);
                if (timeAtAirport >= MAX_DOMESTIC_AIRPORT_TIME) return 3;
                risk = (int) Math.floor(100 - (timeAtAirport - MIN_DOMESTIC_AIRPORT_TIME) /
                        DOMESTIC_RISK_CONSTANT);
            }
        }
        return risk;
    }

    private void calculateTravelTime(Calendar firstShuttleTime){

        fixNightInterval(firstShuttleTime.getTimeInMillis() / 1000);
        if(firstShuttleTime.getTimeInMillis() - count * 1800000 < currentDate.getTimeInMillis()){
            findViewById(R.id.loading_spinner).setVisibility(View.INVISIBLE);
            emptyStateTextView.setVisibility(View.VISIBLE);
        }
        else{
            bundle.putString("departureTime", Long.toString(firstShuttleTime.getTimeInMillis() / 1000));
            loaderManager = getLoaderManager();
            loaderManager.initLoader(LOADER_ID, bundle, this);
        }
    }

    public Loader<Integer> onCreateLoader(int i, Bundle bundle){

        Uri baseUri = Uri.parse(DISTANCE_MATRIX_API_REQUEST_URL);
        Uri.Builder builder = baseUri.buildUpon();
        builder.appendQueryParameter("origins", AirportOrganizer.getAirportCoordinate());
        builder.appendQueryParameter("destinations", AirportOrganizer.getBoardingPointCoordinate());
        if(firstShuttleTime.get(Calendar.HOUR_OF_DAY) >= 16 && firstShuttleTime.get(Calendar.HOUR_OF_DAY) <= 20) {
            builder.appendQueryParameter("traffic_model", "pessimistic");
        }
        builder.appendQueryParameter("departure_time", bundle.getString("departureTime"));
        builder.appendQueryParameter("key", API_KEY);

        travelTimeLoader = new TravelTimeLoader(this, builder.toString());
        return travelTimeLoader;
    }


    @Override
    public void onLoadFinished(Loader<Integer> loader, Integer integer) {

        if(integer != null && integer != 0){

            if (calculateRisk(integer, firstShuttleTime.getTimeInMillis() - count * SHUTTLE_INTERVAL_IN_MILLIS) < 100) {

                Calendar shuttleTime = Calendar.getInstance();
                shuttleTime.setTimeInMillis(firstShuttleTime.getTimeInMillis() - count * SHUTTLE_INTERVAL_IN_MILLIS);
                shuttleTimes.add(shuttleTime);
                shuttleRisks.add(calculateRisk(integer, shuttleTime.getTimeInMillis()));
            }
            
            count++;

            if (shuttleRisks.size() == 3){

                findViewById(R.id.loading_spinner).setVisibility(View.GONE);

                if(shuttleRisks.get(0) == -1 && shuttleRisks.get(1) == -1 && shuttleRisks.get(2) == -1){
                    emptyStateTextView = findViewById(R.id.empty_state_text_view);
                    emptyStateTextView.setText(R.string.no_shuttles);
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    destinationTextView.setVisibility(View.INVISIBLE);
                }

                if(shuttleTimes.get(0) != null) {

                    linearLayout1.setVisibility(View.VISIBLE);

                    shuttleTime1.setText(new SimpleDateFormat("HH:mm").format(shuttleTimes.get(0).getTime()));
                    shuttleRisk1.setText(Integer.toString(shuttleRisks.get(0)));

                    GradientDrawable magnitudeCircle = (GradientDrawable) shuttleRisk1.getBackground();
                    magnitudeCircle.setColor(getRiskColor(shuttleRisks.get(0)));
                }

                if(shuttleTimes.get(1) != null) {

                    linearLayout2.setVisibility(View.VISIBLE);

                    shuttleTime2.setText(new SimpleDateFormat("HH:mm").format(shuttleTimes.get(1).getTime()));
                    shuttleRisk2.setText(Integer.toString(shuttleRisks.get(1)));

                    GradientDrawable magnitudeCircle = (GradientDrawable) shuttleRisk2.getBackground();
                    magnitudeCircle.setColor(getRiskColor(shuttleRisks.get(1)));
                }

                if(shuttleTimes.get(2) != null) {

                    linearLayout3.setVisibility(View.VISIBLE);

                    shuttleTime3.setText(new SimpleDateFormat("HH:mm").format(shuttleTimes.get(2).getTime()));
                    shuttleRisk3.setText(Integer.toString(shuttleRisks.get(2)));

                    GradientDrawable magnitudeCircle = (GradientDrawable) shuttleRisk3.getBackground();
                    magnitudeCircle.setColor(getRiskColor(shuttleRisks.get(2)));
                }
            }

            else {

                if((firstShuttleTime.getTimeInMillis() - count * SHUTTLE_INTERVAL_IN_MILLIS) < currentDate.getTimeInMillis()){

                    for (int i=shuttleRisks.size(); i<3; i++){

                        shuttleTimes.add(null);
                        shuttleRisks.add(-1);
                    }

                    if (shuttleRisks.size() == 3){

                        findViewById(R.id.loading_spinner).setVisibility(View.GONE);

                        if(shuttleRisks.get(0) == -1 && shuttleRisks.get(1) == -1 && shuttleRisks.get(2) == -1){
                            emptyStateTextView = findViewById(R.id.empty_state_text_view);
                            emptyStateTextView.setText(R.string.no_shuttles);
                            emptyStateTextView.setVisibility(View.VISIBLE);
                            destinationTextView.setVisibility(View.INVISIBLE);
                        }

                        if(shuttleTimes.get(0) != null) {

                            linearLayout1.setVisibility(View.VISIBLE);

                            shuttleTime1.setText(new SimpleDateFormat("HH:mm").format(shuttleTimes.get(0).getTime()));
                            shuttleRisk1.setText(Integer.toString(shuttleRisks.get(0)));

                            GradientDrawable magnitudeCircle = (GradientDrawable) shuttleRisk1.getBackground();
                            magnitudeCircle.setColor(getRiskColor(shuttleRisks.get(0)));
                        }

                        if(shuttleTimes.get(1) != null) {

                            linearLayout2.setVisibility(View.VISIBLE);

                            shuttleTime2.setText(new SimpleDateFormat("HH:mm").format(shuttleTimes.get(1).getTime()));
                            shuttleRisk2.setText(Integer.toString(shuttleRisks.get(1)));

                            GradientDrawable magnitudeCircle = (GradientDrawable) shuttleRisk2.getBackground();
                            magnitudeCircle.setColor(getRiskColor(shuttleRisks.get(1)));
                        }

                        if(shuttleTimes.get(2) != null) {

                            linearLayout3.setVisibility(View.VISIBLE);

                            shuttleTime3.setText(new SimpleDateFormat("HH:mm").format(shuttleTimes.get(2).getTime()));
                            shuttleRisk3.setText(Integer.toString(shuttleRisks.get(2)));

                            GradientDrawable magnitudeCircle = (GradientDrawable) shuttleRisk3.getBackground();
                            magnitudeCircle.setColor(getRiskColor(shuttleRisks.get(2)));
                        }
                    }
                }
                else{

                    bundle.remove("departureTime");
                    fixNightInterval(firstShuttleTime.getTimeInMillis() / 1000 - count * SHUTTLE_INTERVAL_IN_MILLIS / 1000);
                    bundle.putString("departureTime", Long.toString(firstShuttleTime.getTimeInMillis() / 1000 - count * SHUTTLE_INTERVAL_IN_MILLIS / 1000));
                    loaderManager.restartLoader(LOADER_ID, bundle, this);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Integer> loader) {

    }

    private void fixNightInterval(long shuttleTimeInMillis){

        Calendar shuttleTime = Calendar.getInstance();
        shuttleTime.setTimeInMillis(shuttleTimeInMillis * 1000);
        switch (AirportOrganizer.getAirport()){
            case SABIHA_GOKCEN:
                switch (AirportOrganizer.getBoardingPoint()){
                    case TAKSIM:
                        if(shuttleTime.get(Calendar.HOUR_OF_DAY) == 03 && shuttleTime.get(Calendar.MINUTE) == 30) return;
                        while(shuttleTime.get(Calendar.HOUR_OF_DAY) <= 03){
                            if(shuttleTime.get(Calendar.HOUR_OF_DAY) ==  01 && shuttleTime.get(Calendar.MINUTE) == 00) break;
                            shuttleTime.setTimeInMillis(shuttleTime.getTimeInMillis() - 1800000);
                            count += 1;
                        }
                        break;
                    case KADIKOY:
                        while(shuttleTime.get(Calendar.HOUR_OF_DAY) <= 03 && shuttleTime.get(Calendar.HOUR_OF_DAY) >= 01){
                            shuttleTime.setTimeInMillis(shuttleTime.getTimeInMillis() - 1800000);
                            count += 1;
                        }
                        break;
                    case YENISAHRA:
                        if(shuttleTime.get(Calendar.HOUR_OF_DAY) == 04 && shuttleTime.get(Calendar.MINUTE) == 30) return;
                        while(shuttleTime.get(Calendar.HOUR_OF_DAY) <= 04){
                            if(shuttleTime.get(Calendar.HOUR_OF_DAY) ==  01 && shuttleTime.get(Calendar.MINUTE) == 00) break;
                            shuttleTime.setTimeInMillis(shuttleTime.getTimeInMillis() - 1800000);
                            count += 1;
                        }
                        break;
                }
                break;
            case ATATURK:
                while((shuttleTime.get(Calendar.HOUR_OF_DAY) < 04)
                        && (shuttleTime.get(Calendar.HOUR_OF_DAY) >= 01 )){
                    if(shuttleTime.get(Calendar.HOUR_OF_DAY) ==  01 && shuttleTime.get(Calendar.MINUTE) == 00) break;
                    shuttleTime.setTimeInMillis(shuttleTime.getTimeInMillis() - 1800000);
                    count += 1;
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    private int getRiskColor(int risk){

        int riskFloor = risk / 10;

        switch (riskFloor){

            case 0 :
                return ContextCompat.getColor(ResultActivity.this, R.color.risk10);

            case 1:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk20);

            case 2:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk30);

            case 3:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk40);

            case 4:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk50);

            case 5:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk60);

            case 6:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk70);

            case 7:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk80);

            case 8:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk90);

            case 9:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk100);

            default:
                return ContextCompat.getColor(ResultActivity.this, R.color.risk10);
        }
    }
}