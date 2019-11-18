package com.example.busroutes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public ArrayList<Route> routeList = new ArrayList<Route>();
    public ArrayList<Route> adapterRouteList = new ArrayList<Route>();
    public RequestQueue requestQueue;
    ArrayAdapter<Route> adapter;

    public static Route selectedRoute;

    public static Route getSelectedRoute(){
        return selectedRoute;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        attachRouteListAdapter();

        requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        updateRoutesData();
        addOnSearchListener();


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void addOnSearchListener() {
        EditText searchET = findViewById(R.id.SearchEditText);
        searchET.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() == 0) {
                    filterRoutes("");
                } else {
                    filterRoutes(s.toString());
                }

            }
        });
    }


    public void filterRoutes(String keyword) {
        Log.e("filter ", keyword);

        adapterRouteList.clear();
        for (Route r : routeList) {
            if (r.firstStop.stopName.toLowerCase().contains(keyword) ||
                    r.lastStop.stopName.toLowerCase().contains(keyword) ||
                    r.routeName.toLowerCase().contains(keyword)) {

                adapterRouteList.add(r);
            }
        }

        adapter.notifyDataSetChanged();
    }


    public void updateRoutesData() {
        // Tag used to cancel the request
        String tag_json_arry = "json_array_req";

        String url = "put url to get json data.com";

        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Preparing Routes for you...");
        pDialog.show();
        pDialog.setCancelable(false);
        JsonArrayRequest routeReq = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("volley response", response.toString());
                        showRoutesfromJsonArray(response);
                        pDialog.hide();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("volley error", "Error: " + error.getMessage());
                noRouteError(true);
                pDialog.hide();
            }
        });
        requestQueue.add(routeReq);
    }


    public void showRoutesfromJsonArray(JSONArray json) {
        routeList.clear();
        for (int i = 0; i < json.length(); i++) {
            try {
                Log.e("object " + i, json.getJSONObject(i).getString("routeId"));
                routeList.add(new Route(json.getJSONObject(i)));
            } catch (Exception e) {
                Log.e("exception route ", e.toString());
            }
        }

//        copying route list to adapterlist
        for (Route r : routeList) {
            adapterRouteList.add(r);
        }

        adapter.notifyDataSetChanged();
    }


    public void noRouteError(boolean visible) {
        Toast.makeText(this, "Check Your Internet Connection", Toast.LENGTH_SHORT).show();
    }

    public void attachRouteListAdapter() {
        adapter = new CustomAdapter(this, adapterRouteList);
        final ListView routesListView = (ListView) findViewById(R.id.RoutesListView);
        routesListView.setAdapter(adapter);
        routesListView.setFocusable(true);

        routesListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Route r = adapterRouteList.get(i);
                        selectedRoute =r;
                        Log.e("route ", r.routeName);
                        openMapsActivity();
                    }
                }
        );
    }


    public void openMapsActivity(){
        Intent mapIntent = new Intent(this, MapsActivity.class);
        this.startActivity(mapIntent);
    }


    public static class CustomAdapter extends ArrayAdapter<Route> {
        public CustomAdapter(Context context, ArrayList<Route> routes) {
            super(context, R.layout.route_item_layout, routes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Route route = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.route_item_layout, parent, false);
            }

            TextView routeName = (TextView) convertView.findViewById(R.id.routeNameTextView);
            TextView routeStart = (TextView) convertView.findViewById(R.id.routeStartTextView);
            TextView routeStop = (TextView) convertView.findViewById(R.id.routeStopTextView);
            routeName.setText(route.routeName);
            routeStart.setText(route.firstStop.stopName + " - ");
            routeStop.setText(route.lastStop.stopName);

            return convertView;
        }
    }


}
