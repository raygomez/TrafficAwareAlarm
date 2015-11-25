package com.silex.ragomez.trafficawarealarm;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
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
import com.silex.ragomez.trafficawarealarm.db.Alarm;
import com.silex.ragomez.trafficawarealarm.db.DatabaseHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends SampleActivityBase implements GoogleApiClient.OnConnectionFailedListener {

/**
     * GoogleApiClient wraps our service connection to Google Play Services and provides access
     * to the user's sign in state as well as the Google's APIs.
     */
    protected GoogleApiClient mGoogleApiClient;

    private PlaceAutocompleteAdapter mAdapter;

    private EditText alarmNameView;
    private AutoCompleteTextView originView;
    private AutoCompleteTextView destinationView;

    private static final LatLngBounds BOUNDS_METRO_MANILA = new LatLngBounds(
            new LatLng(14.446976, 120.954027), new LatLng(14.763922, 121.062517));

    private LatLng originCoordinates = null;
    private LatLng destinationCoordinates = null;

    private int prepTimeInSeconds;
    private AlarmUpdaterBroadcastReceiver alarm;

    private EditText default_date;
    private EditText default_time;
    private EditText target_date;
    private EditText target_time;
    private EditText prep;

    private Integer _id;

    private final String[] hours = new String[] { "0 hour", "1 hour", "2 hours"};
    private final String[] minutes = new String[] { "0 minute", "15 minutes", "30 minutes", "45 minutes"};

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

        alarmNameView = (EditText) findViewById(R.id.alarm_name);
        default_date = (EditText) findViewById(R.id.default_date);
        default_time = (EditText) findViewById(R.id.default_time);
        target_date = (EditText) findViewById(R.id.target_date);
        target_time = (EditText) findViewById(R.id.target_time);
        prep = (EditText) findViewById(R.id.prep_time);

        alarm = new AlarmUpdaterBroadcastReceiver();

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if(b != null) {
            if(b.containsKey(DatabaseHandler.KEY_ID)) {
                _id = b.getInt(DatabaseHandler.KEY_ID);
                String name = b.getString(DatabaseHandler.KEY_NAME);
                String origin = b.getString(DatabaseHandler.KEY_ORIGIN);
                double originLat = b.getDouble(DatabaseHandler.KEY_ORIGIN_LAT);
                double originLong = b.getDouble(DatabaseHandler.KEY_ORIGIN_LONG);
                String dest = b.getString(DatabaseHandler.KEY_DEST);
                double destLat = b.getDouble(DatabaseHandler.KEY_DEST_LAT);
                double destLong = b.getDouble(DatabaseHandler.KEY_DEST_LONG);
                prepTimeInSeconds = b.getInt(DatabaseHandler.KEY_PREP_TIME);
                long defaultAlarm = b.getLong(DatabaseHandler.KEY_DEFAULT_ALARM);
                long eta = b.getLong(DatabaseHandler.KEY_ETA);

                alarmNameView.setText(name);
                originView.setText(origin);
                destinationView.setText(dest);

                originCoordinates = new LatLng(originLat, originLong);
                destinationCoordinates = new LatLng(destLat, destLong);

                int prepInHours = prepTimeInSeconds / 3600;
                int prepInQuarters = (prepTimeInSeconds - prepInHours * 3600)/(60 * 15);
                String input = hours[prepInHours] + ", " + minutes[prepInQuarters];
                prep.setText(input);
            }
        } else {
            Button deleteButton = (Button) findViewById(R.id.button_delete_alarm);
            deleteButton.setVisibility(View.GONE);
        }

    }

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

            //Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
            //        Toast.LENGTH_SHORT).show();
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

            //Toast.makeText(getApplicationContext(), "Clicked: " + primaryText,
            //        Toast.LENGTH_SHORT).show();
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

            originCoordinates = place.getLatLng();

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

            destinationCoordinates = place.getLatLng();

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

        try {
            Date defaultAlarmTime = createDate(((EditText) view).getText().toString());
            args.putLong("date", defaultAlarmTime.getTime());
        } catch (ParseException e) {
            args.putLong("date", System.currentTimeMillis());
        } finally {
            newFragment.setArguments(args);
            newFragment.show(getSupportFragmentManager(), "datePicker");
        }
    }

    public void openTimePickerDialog(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt("source", view.getId());

        try {
            Date defaultAlarmTime = createTime(((EditText) view).getText().toString());
            args.putLong("time", defaultAlarmTime.getTime());
        } catch (ParseException e) {
            args.putLong("time", System.currentTimeMillis());
        } finally {
            newFragment.setArguments(args);
            newFragment.show(getSupportFragmentManager(), "timePicker");
        }
    }

    public void repeatingTimer(View view) {
        Context context = getApplicationContext();

        if(originCoordinates == null){
            showToast(context, "Please enter an origin.");
            return;
        }
        if(destinationCoordinates == null){
            showToast(context, "Please enter a destination.");
            return;
        }

        Date defaultAlarmTime, targetArrivalTime;
        try{
            defaultAlarmTime = createDate(default_date, default_time);
        }
        catch (ParseException e){
            showToast(context, "Please enter an alarm time.");
            return;
        }

        try{
            targetArrivalTime = createDate(target_date, target_time);
        }
        catch(ParseException e){
            showToast(context, "Please enter a target arrival time.");
            return;
        }


        if(timeHasElapsed(defaultAlarmTime)){
            showToast(context, "Given alarm time has passed. Please input a later alarm time.");
            return;
        }
        if(timeHasElapsed(targetArrivalTime)){
            showToast(context, "Given arrival time has passed. Please input a later arrival time.");
            return;
        }
        if(defaultAlarmTime.getTime() > targetArrivalTime.getTime()){
            showToast(context, "Target arrival time must be later than alarm time. Please input an alarm time earlier than your target arrival time.");
            return;
        }

        String input = prep.getText().toString();
        int hours = getHoursFromInput(input);
        int minutes = getMinutesFromInput(input);
        int milliseconds = 1000 * (hours * 60 * 60 + minutes * 60);
        alarm.createRepeatingAlarmTimer(context, originCoordinates.latitude, originCoordinates.longitude, destinationCoordinates.latitude,
                destinationCoordinates.longitude, defaultAlarmTime.getTime(),
                targetArrivalTime.getTime(), milliseconds);
        Toast.makeText(context, "Alarm Created!", Toast.LENGTH_LONG).show();
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private boolean timeHasElapsed(Date defaultAlarmTime) {
        return System.currentTimeMillis() > defaultAlarmTime.getTime();
    }

    private Date createDate(String dateString) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        return format.parse(dateString);
    }

    private Date createTime(String timeString) throws ParseException {
        DateFormat format = new SimpleDateFormat("hh : mm a");
        return format.parse(timeString);
    }

    private Date createDate(EditText dateText, EditText timeText) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd hh : mm a");
        return format.parse(dateText.getText().toString() + ' ' + timeText.getText().toString());
    }


    public void stopTimer(View view) {
        Context context = getApplicationContext();
        alarm.cancel(context);
        Toast.makeText(context, "Alarm Cancelled", Toast.LENGTH_LONG).show();
    }

    public void openNumberPickerDialog(View view) {
        final Dialog d = new Dialog(MainActivity.this);
        d.setContentView(R.layout.number_picker_dialog);
        Button setButton = (Button) d.findViewById(R.id.set_button);
        Button cancelButton = (Button) d.findViewById(R.id.cancel_button);

        int prepInHours = prepTimeInSeconds / 3600;
        int prepInQuarters = (prepTimeInSeconds - prepInHours * 3600)/(60 * 15);

        final NumberPicker hourPicker = (NumberPicker) d.findViewById(R.id.prep_hours);
        hourPicker.setMaxValue(2);
        hourPicker.setMinValue(0);

        hourPicker.setDisplayedValues(hours);
        hourPicker.setValue(prepInHours);


        final NumberPicker minPicker = (NumberPicker) d.findViewById(R.id.prep_minutes);
        minPicker.setMaxValue(3);
        minPicker.setMinValue(0);

        minPicker.setDisplayedValues(minutes);
        minPicker.setValue(prepInQuarters);


        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = hours[hourPicker.getValue()] + ", " + minutes[minPicker.getValue()];
                prep.setText(input);
                prepTimeInSeconds = hourPicker.getValue() * 3600 + minPicker.getValue() * 60 * 15;
                d.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private int getHoursFromInput(String input) {

        Pattern p = Pattern.compile("(\\d+) hour[s]?, (\\d+) minute[s]?");
        Matcher m = p.matcher(input);

        if(m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            return 0;
        }
    }
    private int getMinutesFromInput(String input) {

        Pattern p = Pattern.compile("(\\d+) hour[s]?, (\\d+) minute[s]+");
        Matcher m = p.matcher(input);

        if(m.find()) {
            return Integer.parseInt(m.group(2));
        } else {
            return 0;
        }
    }

    public void saveAlarm(View view) {

        DatabaseHandler handler = DatabaseHandler.getInstance(this);

        Alarm newAlarm = new Alarm();

        newAlarm.setName(alarmNameView.getText().toString());
        newAlarm.setOrigin(originView.getText().toString());
        newAlarm.setOriginLatitude(originCoordinates.latitude);
        newAlarm.setOriginLongitude(originCoordinates.longitude);
        newAlarm.setDestination(destinationView.getText().toString());
        newAlarm.setDestLatitude(destinationCoordinates.latitude);
        newAlarm.setDestLongitude(destinationCoordinates.longitude);
        newAlarm.setPrepTime(prepTimeInSeconds);
        newAlarm.setDefaultAlarm(6);
        newAlarm.setEta(7);

        handler.addAlarm(newAlarm);
        showToast(getApplicationContext(), "Alarm saved");
        finish();
    }

    public void deleteAlarm(View view) {

        DatabaseHandler db = DatabaseHandler.getInstance(this);
        db.deleteAlarmById(_id);
        showToast(getApplicationContext(), "Alarm deleted");
        finish();
    }
}