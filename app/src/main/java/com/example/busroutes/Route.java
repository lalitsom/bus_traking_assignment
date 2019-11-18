package com.example.busroutes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;

public class Route {
    public String  routeId,routeName;
    public BusStop firstStop,lastStop;
    public ArrayList<BusStop> stopList;
    public Hashtable<String , BusStop > stopFromStopID = new Hashtable<String,BusStop>();

    Route(JSONObject route) throws Exception {
        routeId = route.getString("routeId");
        routeName = route.getString("routeName");
        stopList = getAllStops(route.getJSONArray("stopDataList"));
        firstStop = stopList.get(0);
        lastStop = stopList.get(stopList.size()-1);

    }


    public ArrayList<BusStop> getAllStops(JSONArray list) throws Exception{
        ArrayList<BusStop> stops = new ArrayList<BusStop>();
        for(int i=0;i<list.length();i++){
            BusStop newstop = new BusStop(list.getJSONObject(i));
            stops.add(newstop);
            stopFromStopID.put(newstop.stopId,newstop);
        }
        return stops;
    }

}
