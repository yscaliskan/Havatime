package com.example.yasarselcukcaliskan.havatime;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final Calendar selectedDate = Calendar.getInstance();
    private static final Calendar currentDate =  Calendar.getInstance();
    private static boolean isInternational;
    private static boolean haveLuggage;
    private Switch luggageSwitch;
    private LinearLayout datePicker;
    private TextView datePickerTextView;
    private LinearLayout timePicker;
    private TextView timePickerTextView;
    private Switch internationalFlightSwitch;
    private boolean isConnected;
    private Spinner boarding_point_spinner;
    private ArrayAdapter<CharSequence> adapter1;
    private List<AirportOrganizer.Airport> airports;
    private List<AirportOrganizer.BoardingPoint> sabihaBoardingPoints;
    private List<AirportOrganizer.BoardingPoint> ataturkBoardingPoints;
    private List<Integer> boardingPointArrays;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        isConnected = activeNetwork.isConnectedOrConnecting() && activeNetwork != null;

        datePicker = findViewById(R.id.date_picker);
        datePickerTextView = findViewById(R.id.date_picker_text_view);
        datePickerTextView.setText(new SimpleDateFormat("dd.MM.yyyy").format(currentDate.getTime()));

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                selectedDate.set(year, monthOfYear, dayOfMonth);
                                datePickerTextView.setText(new SimpleDateFormat("dd.MM.yyyy").format(selectedDate.getTime()));
                            }
                        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis() - currentDate.get(Calendar.HOUR_OF_DAY) * 3600000
                        - currentDate.get(Calendar.MINUTE) * 60000);
                datePickerDialog.show();
            }
        });

        timePicker = findViewById(R.id.time_picker);
        timePickerTextView = findViewById(R.id.time_picker_text_view);
        selectedDate.set(Calendar.HOUR_OF_DAY, currentDate.get(Calendar.HOUR_OF_DAY) + 1);
        timePickerTextView.setText(new SimpleDateFormat("HH:mm").format(selectedDate.getTimeInMillis()));

        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                selectedDate.set(Calendar.MINUTE, minute);

                                if(selectedDate.getTimeInMillis() - currentDate.getTimeInMillis() > 3600000){

                                    timePickerTextView.setText(new SimpleDateFormat("HH:mm").format(selectedDate.getTime()));

                                }
                                else{

                                    Toast.makeText(MainActivity.this, "You should pick a time at least one hour after now.", Toast.LENGTH_SHORT).show();
                                    selectedDate.set(Calendar.HOUR_OF_DAY, currentDate.get(Calendar.HOUR_OF_DAY));
                                    selectedDate.set(Calendar.MINUTE, currentDate.get(Calendar.MINUTE));
                                }
                            }
                        }, selectedDate.get(Calendar.HOUR_OF_DAY) , selectedDate.get(Calendar.MINUTE), true);
                timePickerDialog.show();
            }
        });

        Spinner airport_spinner = findViewById(R.id.airport_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.airport_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        airport_spinner.setAdapter(adapter);

        boarding_point_spinner = findViewById(R.id.boarding_point_spinner);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getBaseContext(), R.array.sabiha_gokcen_boarding_point, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        boarding_point_spinner.setAdapter(adapter1);

        boardingPointArrays = new ArrayList<>();
        boardingPointArrays.add(R.array.ataturk_boarding_point);
        boardingPointArrays.add(R.array.sabiha_gokcen_boarding_point);
        airports = new ArrayList<>();
        airports.add(AirportOrganizer.Airport.ATATURK);
        airports.add(AirportOrganizer.Airport.SABIHA_GOKCEN);
        sabihaBoardingPoints = new ArrayList<>();
        sabihaBoardingPoints.add(AirportOrganizer.BoardingPoint.TAKSIM);
        sabihaBoardingPoints.add(AirportOrganizer.BoardingPoint.KADIKOY);
        sabihaBoardingPoints.add(AirportOrganizer.BoardingPoint.YENISAHRA);
        ataturkBoardingPoints = new ArrayList<>();
        ataturkBoardingPoints.add(AirportOrganizer.BoardingPoint.TAKSIM);
        final Map<AirportOrganizer.Airport, List<AirportOrganizer.BoardingPoint>> ruleMap = new HashMap<>();
        ruleMap.put(AirportOrganizer.Airport.ATATURK, ataturkBoardingPoints);
        ruleMap.put(AirportOrganizer.Airport.SABIHA_GOKCEN, sabihaBoardingPoints);

        airport_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                final AirportOrganizer.Airport selectedAirport = airports.get(i);
                AirportOrganizer.setAirport(airports.get(i));

                Spinner boarding_point_spinner = findViewById(R.id.boarding_point_spinner);
                ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getBaseContext(), boardingPointArrays.get(i), android.R.layout.simple_spinner_item);
                adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                boarding_point_spinner.setAdapter(adapter1);

                boarding_point_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int k, long l) {
                        AirportOrganizer.setBoardingPoint(ruleMap.get(selectedAirport).get(k));
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        AirportOrganizer.setBoardingPoint(ruleMap.get(selectedAirport).get(0));
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

                AirportOrganizer.setAirport(AirportOrganizer.Airport.ATATURK);
            }
        });




        Button sendButton = findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(selectedDate.getTimeInMillis() - currentDate.getTimeInMillis() > 3600000){

                    if (!isConnected) {
                        Toast.makeText(MainActivity.this, "You don't have an internet connection.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent activityChangeIntent = new Intent(view.getContext(), ResultActivity.class);
                    startActivity(activityChangeIntent);
                }
                else{

                    Toast.makeText(MainActivity.this, "Your flight time must be at least one hour from now.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        internationalFlightSwitch = findViewById(R.id.international_flight_switch);
        internationalFlightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                isInternational = b;
            }
        });

        luggageSwitch = findViewById(R.id.luggage_switch);
        luggageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                haveLuggage = b;
            }
        });

    }

    public static boolean isInternational(){

        return isInternational;
    }

    public static boolean haveLuggage(){

        return haveLuggage;
    }

    public static Calendar getSelectedDate(){

        return selectedDate;
    }
}
