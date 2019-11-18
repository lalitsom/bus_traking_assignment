package com.example.busroutes;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;
    public Route selectedRoute;
    public BusStop currentStop;
    public RequestQueue requestQueue;
    public int nextStopTime = 10000; // set to 10000 for 10 secs
    Marker BusMarker;
    public static MediaPlayer alarmPlayer = null;
    public BusStop userStop=null;
    double cLat,cLng;
    Thread getBusLocationThread;
    public boolean threadRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if(savedInstanceState!=null){
            Log.e("helo ",savedInstanceState.getString("userStop"));
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        selectedRoute = MainActivity.getSelectedRoute();
        currentStop = selectedRoute.stopList.get(0);

        requestQueue = Volley.newRequestQueue(this.getApplicationContext());



        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showsetAlarmPrompt();
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(alarmPlayer!=null){
            alarmPlayer.stop();
        }
        this.finish();

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("onpuase"," called");
        threadRunning=false;
        if(alarmPlayer!=null){
            alarmPlayer.stop();
        }
        this.finish();
    }

    public void showsetAlarmPrompt(){
        AlertDialog.Builder pwd_builder = new AlertDialog.Builder(this);
        AlertDialog pwd_dialog;
        LayoutInflater inflater = LayoutInflater.from(this);
        final View prompt_view = inflater.inflate(R.layout.alarm_prompt,null);

        final Spinner spinner = (Spinner) prompt_view.findViewById(R.id.routeSpinner);
       TextView msg = (TextView) prompt_view.findViewById(R.id.alarmMsg);
       if(userStop!=null){
          msg.setText("Alarm set for stop :" + userStop.stopName + "\n" +
                  "Setting new Alarm will  overwrite it.");
       }else{
           msg.setText("");
       }
        pwd_builder.setView(prompt_view)
                .setPositiveButton("Set Alarm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String userStopName = spinner.getSelectedItem().toString();
                        if(userStopName!=null && userStopName.length()>2){
                            String stopID = userStopName.split(":")[0];
                            userStop = selectedRoute.stopFromStopID.get(stopID);
                            Log.e("user selected stop", userStop.stopName);

                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });


        pwd_dialog = pwd_builder.create();

        populateSpinner(spinner);


        pwd_dialog.show();

    }

    public void populateSpinner(Spinner spinner){

        ArrayList<String> routeList = new ArrayList<String>();

        for(BusStop stop : selectedRoute.stopList){
            routeList.add(stop.stopId +":"+ stop.stopName);
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, routeList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }



    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.e("poly clicked",polyline.getColor() + "");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        showRouteOnMap();
    }


    public void showRouteOnMap(){

       ArrayList<BusStop> stops = selectedRoute.stopList;
       BusStop prevstop = null;
        for(BusStop stop : stops){
            LatLng stopLatLng = new LatLng(stop.latitute,stop.longitude);
            MarkerOptions stopMarker = new MarkerOptions().position(stopLatLng).
                    title(stop.stopName);
            mMap.addMarker(stopMarker);


                if(prevstop!=null){

                    Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                            .clickable(true)
                            .add(
                                    new LatLng(stop.latitute,stop.longitude),
                                    new LatLng(prevstop.latitute,prevstop.longitude)
                            ));

                }
            prevstop = stop;
        }

        LatLng busLatLng = new LatLng(stops.get(0).latitute,stops.get(0).longitude);

        MarkerOptions busMarker = new MarkerOptions().position(busLatLng).
                title("Bus Current Position").zIndex(2)
                .icon(bitmapDescriptorFromVector(this,R.drawable.bus_blue_small));
        BusMarker = mMap.addMarker(busMarker);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(stops.get(0).latitute,stops.get(0).longitude), 12));

        startBusSimulation();

    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }




    public void startBusSimulation(){
        getBusLocationThread = new Thread() {
            @Override
            public void run() {
                while (threadRunning) {
                    android.os.SystemClock.sleep(nextStopTime);
                    try{
                        getNextStop();
                    }catch (Exception e){
                        Log.e("getBusLocationThread", e.toString());
                        //break;
                    }

                }
            }
        };
        getBusLocationThread.start();
    }




    public void getNextStop() {
        // Tag used to cancel the request
        String tag_json_arry = "json_array_req";

        String url = "next route"+selectedRoute.routeId+"/stops/"+currentStop.stopId;

        JsonObjectRequest stopReq = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        updateBusLocation(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });
        requestQueue.add(stopReq);
    }



    public void updateBusLocation(JSONObject newLoc) {
        Log.e("new location",newLoc.toString());
        try{
           String newStop = newLoc.getString("nextStopId");
           if(newStop!="-1"){
               moveToNewStop(newStop);
           }else{
               Toast.makeText(this, "No more Stops", Toast.LENGTH_SHORT).show();
           }
        }catch (Exception e){
            Log.e("Error in update ",e.toString());
        }
    }


    public void moveToNewStop(final String newStop){

        final Thread animateBusThread = new Thread() {
            @Override
            public void run() {

                    try{

                        final BusStop nextStop = selectedRoute.stopFromStopID.get(newStop);
                        Log.e(" stops ", currentStop.latitute + " " + currentStop.longitude );
                        Log.e(" stops ", nextStop.latitute + " " + nextStop.longitude );


                        //        generating animation latlng manually
                        cLat = currentStop.latitute;
                        cLng = currentStop.longitude;
                        double numSteps = 200;
                        int delay = 10;
                        double dLat = (nextStop.latitute - currentStop.latitute)/numSteps;
                        double dLng = (nextStop.longitude - currentStop.longitude)/numSteps;
                        while(numSteps-- >0){
                            if(!threadRunning) return;
                            cLat += dLat; cLng += dLng;

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    try {
                                        BusMarker.setPosition(new LatLng(cLat,cLng));
                                    } catch (Exception e) {
                                        Log.e(this.getClass().toString(), e.getMessage());
                                    }
                                }
                            });


                            android.os.SystemClock.sleep(delay);
                        }

                        cLat = nextStop.latitute; cLng = nextStop.longitude;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    BusMarker.setPosition(new LatLng(cLat,cLng));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLng(
                                            new LatLng(cLat,cLng)));
                                    if(userStop!=null){
                                        Log.e(" r" , "alarm " + newStop + " " + userStop.stopId);
                                    }

                                    if( userStop!=null && newStop.equals(userStop.stopId)){
                                        playAlarm();
                                    }

                                } catch (Exception e) {
                                    Log.e(this.getClass().toString(), e.getMessage());
                                }
                            }
                        });

                        currentStop = nextStop;

                    }catch (Exception e){
                        Log.e("animateBusThread", e.toString());
                        //break;
                    }

            }
        };
        animateBusThread.start();


        //animateBus()
    }

    public void playAlarm(){
        alarmPlayer = MediaPlayer.create(this, R.raw.notify);
        alarmPlayer.start(); // no need to call prepare(); create() does that for you

        AlertDialog.Builder pwd_builder = new AlertDialog.Builder(this);
        AlertDialog pwd_dialog;
        LayoutInflater inflater = LayoutInflater.from(this);
        final View prompt_view = inflater.inflate(R.layout.alarm_ringer,null);
        pwd_builder.setView(prompt_view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                       alarmPlayer.stop();
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Log.e("dismiss","");
                alarmPlayer.stop();
            }
        });


        pwd_dialog = pwd_builder.create();
        pwd_dialog.show();

    }




}


