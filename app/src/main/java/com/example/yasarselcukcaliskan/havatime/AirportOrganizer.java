package com.example.yasarselcukcaliskan.havatime;

import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yasarselcukcaliskan on 10.03.2018.
 */

public class AirportOrganizer extends AppCompatActivity {

    private static Airport airport;

    private static BoardingPoint boardingPoint;

    public enum Airport {ATATURK, SABIHA_GOKCEN};

    public enum BoardingPoint {TAKSIM, KADIKOY, YENISAHRA};

    private static final String TAKSIM_COORDINATES = "41.040483,28.986184";

    private static final String KADIKOY_COORDINATES = "40.993207,29.024668";

    private static final String YENISAHRA_COORDINATES = "40.98550,29.08949";

    private static final String SABIHA_GOKCEN_COORDINATES = "40.908511,29.315082";

    private static final String ATATURK_COORDINATES = "40.978758,28.819860";

    private static Map<Airport, String> airportCoordinatesMap = new HashMap<>();

    private static Map<BoardingPoint, String> boardingPointCoordinatesMap = new HashMap<>();

    public static void setAirport(Airport pAirport){

        if (pAirport instanceof Airport){

            airport = pAirport;
        }
        else{

            throw new IllegalArgumentException();
        }
    }

    public static void setBoardingPoint(BoardingPoint pBoardingPoint){

        if (pBoardingPoint instanceof BoardingPoint){

            boardingPoint = pBoardingPoint;
        }

        else{

            throw new IllegalArgumentException();
        }
    }

    public static Airport getAirport(){

        return airport;
    }

    public static BoardingPoint getBoardingPoint(){

        return boardingPoint;
    }

    public static String getAirportCoordinate(){

        airportCoordinatesMap.put(Airport.ATATURK, ATATURK_COORDINATES);
        airportCoordinatesMap.put(Airport.SABIHA_GOKCEN, SABIHA_GOKCEN_COORDINATES);
        return airportCoordinatesMap.get(airport);
    }
    public static String getBoardingPointCoordinate(){

        boardingPointCoordinatesMap.put(BoardingPoint.TAKSIM, TAKSIM_COORDINATES);
        boardingPointCoordinatesMap.put(BoardingPoint.KADIKOY, KADIKOY_COORDINATES);
        boardingPointCoordinatesMap.put(BoardingPoint.YENISAHRA, YENISAHRA_COORDINATES);
        return boardingPointCoordinatesMap.get(boardingPoint);
    }
}

