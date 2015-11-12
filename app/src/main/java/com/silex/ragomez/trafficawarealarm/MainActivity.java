package com.silex.ragomez.trafficawarealarm;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.google.playservices.placecomplete.PlaceAutocompleteAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends SampleActivityBase implements GoogleApiClient.OnConnectionFailedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Construct a GoogleApiClient for the {@link Places#GEO_DATA_API} using AutoManage
        // functionality, which automatically sets up the API client to handle Activity lifecycle
        // events. If your activity does not extend FragmentActivity, make sure to call connect()
        // and disconnect() explicitly.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        // Retrieve the AutoCompleteTextView that will display Place suggestions.
        originView = (AutoCompleteTextView) findViewById(R.id.origin);
        destinationView = (AutoCompleteTextView) findViewById(R.id.destination);

        // Register a listener that receives callbacks when a suggestion has been selected
        originView.setOnItemClickListener(originViewClickListener);
        destinationView.setOnItemClickListener(destinationViewClickListener);

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_METRO_MANILA,
                null);
        originView.setAdapter(mAdapter);
        destinationView.setAdapter(mAdapter);

        default_date = (EditText) findViewById(R.id.default_date);
        default_time = (EditText) findViewById(R.id.default_time);
        target_date = (EditText) findViewById(R.id.target_date);
        target_time = (EditText) findViewById(R.id.target_time);

        // Set up the 'clear text' button that clears the text in the autocomplete view
        Button clearButton = (Button) findViewById(R.id.button_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                originView.setText("");
                destinationView.setText("");
                default_date.setText("");
                default_time.setText("");
                target_date.setText("");
                target_time.setText("");
            }
        });

        alarm = new AlarmManagerBroadcastReceiver();
        Button createAlarm = (Button) findViewById(R.id.button_create);
        createAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check details
                // send post with input details
                String path = String.format("http://silex-archnat.rhcloud.com/rest/api/v1/compute_travel_time?to=%f,%f&from=%f,%f",
                        destination.latitude, destination.longitude, origin.latitude, origin.longitude);
                Log.i(TAG, "path:" + path);
                new HttpAsyncTask().execute(path);

            }
        });

    }

    /**
     * GoogleApiClient wraps our service connection to Google Play Services and provides access
     * to the user's sign in state as well as the Google's APIs.
     */
    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private AutoCompleteTextView originView;
    private AutoCompleteTextView destinationView;

    private static final LatLngBounds BOUNDS_METRO_MANILA = new LatLngBounds(
            new LatLng(14.446976, 120.954027), new LatLng(14.763922, 121.062517));

    private LatLng origin = null;
    private LatLng destination = null;

    private AlarmManagerBroadcastReceiver alarm;

    EditText default_date;
    EditText default_time;
    EditText target_date;
    EditText target_time;

    /**
     * Listener that handles selections from suggestions from the AutoCompleteTextView that
     * displays Place suggestions.
     * Gets the place id of the selected item and issues a request to the Places Geo Data API
     * to retrieve more details about the place.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
     * String...)
     */
    private AdapterView.OnItemClickListener originViewClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(originUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };

    private AdapterView.OnItemClickListener destinationViewClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            /*
             Retrieve the place ID of the selected item from the Adapter.
             The adapter stores each Place suggestion in a AutocompletePrediction from which we
             read the place ID and title.
              */
            final AutocompletePrediction item = mAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            final CharSequence primaryText = item.getPrimaryText(null);

            Log.i(TAG, "Autocomplete item selected: " + primaryText);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
             details about the place.
              */
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(destinationUpdatePlaceDetailsCallback);

            Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Called getPlaceById to get Place details for " + placeId);
        }
    };
    /**
     * Callback for results from a Places Geo Data API query that shows the first place result in
     * the details view on screen.
     */
    private ResultCallback<PlaceBuffer> originUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            origin = place.getLatLng();

            Log.i(TAG, "Place details received: " + place.getName());
            Log.i(TAG, "Location received: " + place.getLatLng().toString());

            places.release();
        }
    };

    private ResultCallback<PlaceBuffer> destinationUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                // Request did not complete successfully
                Log.e(TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                places.release();
                return;
            }
            // Get the Place object from the buffer.
            final Place place = places.get(0);

            destination = place.getLatLng();

            Log.i(TAG, "Place details received: " + place.getName());
            Log.i( TAG, "Location received: " + place.getLatLng().toString());

            places.release();
        }
    };
    /**
     * Called when the Activity could not connect to Google Play services and the auto manager
     * could resolve the error automatically.
     * In this case the API is not available and notify the user.
     *
     * @param connectionResult can be inspected to determine the cause of the failure
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    public void openDatePickerDialog(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("source", view.getId());
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void openTimePickerDialog(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt("source", view.getId());
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "timePicker");

    }

    public void oneTimeTimer(View view) {
        Context context = getApplicationContext();

        DateFormat format = new SimpleDateFormat("yyyy/MM/dd hh : mm a");
        try {

            Date date = format.parse(default_date.getText().toString() + ' ' +
                    default_time.getText().toString());
            Log.i(TAG, "Date: " + date.toString());

            if(alarm != null){
                Log.i("Timer", "timer started:" + new Date().toString());
                alarm.setOnetimeTimer(context, date.getTime());
            } else {
                Toast.makeText(context, "Alarm is null", Toast.LENGTH_SHORT).show();
            }


        } catch (ParseException e) {
            Toast.makeText(context, "Parsing the date is not successful", Toast.LENGTH_SHORT).show();
        }

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        String message;
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Estimated time: " + message + "seconds", Toast.LENGTH_LONG).show();
        }

        private String GET(String urlStr) {
            android.util.Log.i("urlConnection", " gonna connect to " + urlStr);
            InputStream inputStream;
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urlStr);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                if (!url.getHost().equals(urlConnection.getURL().getHost())) {
                    android.util.Log.i("urlConnection", "we were redirected! Kick the user out to the browser to sign on?");
                }
                int responseCode = urlConnection.getResponseCode();
                System.out.println("urlConnection.responseCode: " + responseCode);
                System.out.println("urlConnection.connection.getResponseMessage(): " + urlConnection.getResponseMessage());
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    System.out.println("nice! urlConnection.responseCode: " + responseCode);
                    result = convertInputStreamToString(inputStream);
                    JSONObject json = new JSONObject(result);
                    message = json.get("duration_in_seconds").toString();

                } else {
                    android.util.Log.v("CatalogClient", "Response code:" + responseCode);
                    result = "oh noes";
                }
                Log.i("alarm", result);

            } catch (Exception e) {
                android.util.Log.i("urlConnection", "urlConnection exception occurred");
                e.printStackTrace();
            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            return result;
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            String result = "";
            while ((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }
    }
}