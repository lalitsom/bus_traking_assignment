package com.example.busroutes;

import org.json.JSONObject;

public class BusStop {
    public String stopId,stopName,sequence;
    double latitute,longitude;

    BusStop(JSONObject stop) throws Exception{
        stopId = stop.getString("stopId");
        stopName = stop.getString("stopName");
        sequence = stop.getString("sequence");
        latitute = stop.getDouble("latitute");
        longitude = stop.getDouble("longitude");
    }
}
