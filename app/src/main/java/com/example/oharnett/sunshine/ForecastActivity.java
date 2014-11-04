package com.example.oharnett.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.oharnett.sunshine.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Orla on 11/1/2014.
 */
public class ForecastActivity {
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ForecastFragment extends Fragment {

        public ForecastFragment() {
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.forecastfragment, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == R.id.action_refresh) {
                String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q=98136,seattle,us&mode=json&units=metric&cnt=7";
                new GetForecastTask().execute(url);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {


            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            String[] forecasts = {
                    "Today - Sunny - 88/63",
                    "Tomorrow - Foggy - 70/46",
                    "Weds - Cloudy - 72/63",
                    "Thurs - Rain - 56/50",
                    "Fri - Sunny - 65/60",
                    "Sat - Sunny - 65/60"};
            List<String> forecastList = new ArrayList<String>(Arrays.asList(forecasts));

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.list_item_forecast,
                    R.id.list_item_forecast_textview,
                    forecastList);
            ListView view = (ListView)rootView.findViewById(R.id.listview_forecast);
            view.setAdapter(adapter);
            String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q=98136,seattle,us&mode=json&units=metric&cnt=7";
            new GetForecastTask().execute(url);
            return rootView;
        }

        public class GetForecastTask extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... urls)
            {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                String forecastJsonStr = null;
                try {
                    URL url = new URL(urls[0]);
                    connection = (HttpURLConnection ) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    StringBuilder builder = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line + "\n");
                    }
                    forecastJsonStr = builder.toString();
                } catch (IOException e) {
                    Log.e("PlaceholderFragment", "Error ", e);
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e("PlaceholderFragment", "Error closing stream", e);
                        }
                    }
                }
                return forecastJsonStr;
            }

        }
    }
}
