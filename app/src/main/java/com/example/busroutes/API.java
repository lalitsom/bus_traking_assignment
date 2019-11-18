package com.example.busroutes;

import android.app.ProgressDialog;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

public class API {


//
//    RequestQueue requestQueue;
//
//    // Instantiate the cache
//    Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap
//
//    // Set up the network to use HttpURLConnection as the HTTP client.
//    Network network = new BasicNetwork(new HurlStack());
//
//// Instantiate the RequestQueue with the cache and network.
//    requestQueue = new RequestQueue(cache, network);
//
//// Start the queue
//requestQueue.start();
//
//    String url ="http://www.example.com";
//
//    // Formulate the request and handle the response.
//    JsonArrayRequest req = new JsonArrayRequest(url,
//            new Response.Listener<JSONArray>() {
//                @Override
//                public void onResponse(JSONArray response) {
//                    Log.e("Respone volley", response.toString());
//                    pDialog.hide();
//                }
//            }, new Response.ErrorListener() {
//        @Override
//        public void onErrorResponse(VolleyError error) {
//            VolleyLog.d("Error Respone", "Error: " + error.getMessage());
//            pDialog.hide();
//        }
//    });

    public static void updateRoutesData(){

    }


}
