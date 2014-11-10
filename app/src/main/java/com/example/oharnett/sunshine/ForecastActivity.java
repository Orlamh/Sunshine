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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Orla on 11/1/2014.
 */
public class ForecastActivity {

    public static class ForecastFragment extends Fragment {
        ArrayAdapter<String> _arrayAdapter;

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

            String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q=98136,seattle,us&mode=json&units=metric&cnt=7";
            String forecasts = null;
            new GetForecastTask().execute(url, null, forecasts);
            List<String> forecastList = new ArrayList<String>(Arrays.asList(forecasts));

            _arrayAdapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.list_item_forecast,
                    R.id.list_item_forecast_textview);
            ListView view = (ListView)rootView.findViewById(R.id.listview_forecast);
            view.setAdapter(_arrayAdapter);

            return rootView;
        }


        public class GetForecastTask extends AsyncTask<String, Void, String[]> {
            private String[] forecasts = null;
            @Override
            protected String[] doInBackground(String... urls)
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
                try {
                    return new JsonHelper().getWeatherDataFromJson(forecastJsonStr,7);
                }
                catch (JSONException e) {
                    Log.e("something", e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String[] result) {
                if(result != null) {
                    _arrayAdapter.clear();
                    //for (String forecast : result) {
                        _arrayAdapter.addAll(forecasts);
                    //}

                }
            }
        }

        public static class JsonHelper
        {
            private String getReadableDateString(long time){
                // Because the API returns a unix timestamp (measured in seconds),
                // it must be converted to milliseconds in order to be converted to valid date.
                Date date = new Date(time * 1000);
                SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
                return format.format(date).toString();
            }

            /**
             * Prepare the weather high/lows for presentation.
             */
            private String formatHighLows(double high, double low) {
                // For presentation, assume the user doesn't care about tenths of a degree.
                long roundedHigh = Math.round(high);
                long roundedLow = Math.round(low);

                String highLowStr = roundedHigh + "/" + roundedLow;
                return highLowStr;
            }

            /**
             * Take the String representing the complete forecast in JSON Format and
             * pull out the data we need to construct the Strings needed for the wireframes.
             *
             * Fortunately parsing is easy:  constructor takes the JSON string and converts it
             * into an Object hierarchy for us.
             */
            public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                    throws JSONException {

                // These are the names of the JSON objects that need to be extracted.
                final String OWM_LIST = "list";
                final String OWM_WEATHER = "weather";
                final String OWM_TEMPERATURE = "temp";
                final String OWM_MAX = "max";
                final String OWM_MIN = "min";
                final String OWM_DATETIME = "dt";
                final String OWM_DESCRIPTION = "main";

                JSONObject forecastJson = new JSONObject(forecastJsonStr);
                JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                String[] resultStrs = new String[numDays];
                for(int i = 0; i < weatherArray.length(); i++) {
                    // For now, using the format "Day, description, hi/low"
                    String day;
                    String description;
                    String highAndLow;

                    // Get the JSON object representing the day
                    JSONObject dayForecast = weatherArray.getJSONObject(i);

                    // The date/time is returned as a long.  We need to convert that
                    // into something human-readable, since most people won't read "1400356800" as
                    // "this saturday".
                    long dateTime = dayForecast.getLong(OWM_DATETIME);
                    day = getReadableDateString(dateTime);

                    // description is in a child array called "weather", which is 1 element long.
                    JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                    description = weatherObject.getString(OWM_DESCRIPTION);

                    // Temperatures are in a child object called "temp".  Try not to name variables
                    // "temp" when working with temperature.  It confuses everybody.
                    JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                    double high = temperatureObject.getDouble(OWM_MAX);
                    double low = temperatureObject.getDouble(OWM_MIN);

                    highAndLow = formatHighLows(high, low);
                    resultStrs[i] = day + " - " + description + " - " + highAndLow;
                }

                return resultStrs;
            }
        }

    }
}
