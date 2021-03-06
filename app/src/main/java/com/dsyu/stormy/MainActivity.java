package com.dsyu.stormy;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dsyu.stormy.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.dsyu.stormy.R.layout.activity_main;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather currentWeather;
    private Locations locations = new Locations();

    private ConstraintLayout layout;
    private ImageView iconImageView;
    private ImageView degreeImageView;
    private TextView locationTextView;
    private TextView timeTextView;
    private TextView temperatureValue;
    private TextView uvIndexLabel;
    private TextView uvIndexValue;
    private TextView precipitationLabel;
    private TextView precipitationValue;
    private Button mississaugaButton;
    private Button waterlooButton;
    private Button ottawaButton;
    private Button tokyoButton;
    private Button losAngelesButton;
    private Button dalianButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        getForecast(locations.getWaterlooLatitude(), locations.getWaterlooLongitude(), "Waterloo");
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void getForecast(double latitude, double longitude, final String city) {
        final ActivityMainBinding binding = DataBindingUtil.setContentView( MainActivity.this, activity_main );

        TextView darkSky = findViewById( R.id.darkSkyAttribution );
        darkSky.setMovementMethod( LinkMovementMethod.getInstance() );

        layout= findViewById( R.id.layout );
        locationTextView = findViewById( R.id.locationTextView );
        iconImageView = findViewById( R.id.iconImageView );
        degreeImageView = findViewById( R.id.degreeImageView );
        timeTextView = findViewById( R.id.timeValue );
        temperatureValue = findViewById( R.id.temperatureValue );
        uvIndexLabel = findViewById( R.id.uvIndexLabel );
        uvIndexValue = findViewById( R.id.uvIndexValue );
        precipitationLabel = findViewById( R.id.precipLabel );
        precipitationValue = findViewById( R.id.precipValue);
        mississaugaButton = findViewById( R.id.mississaugaButton );
        waterlooButton = findViewById( R.id.waterlooButton );
        ottawaButton = findViewById( R.id.ottawaButton );
        tokyoButton = findViewById( R.id.tokyoButton );
        losAngelesButton = findViewById( R.id.losAngelesButton );
        dalianButton = findViewById( R.id.dalianButton );

        // Avoids default text from being displayed during the API call
        degreeImageView.setVisibility( View.INVISIBLE );
        timeTextView.setVisibility( View.INVISIBLE );
        temperatureValue.setVisibility( View.INVISIBLE );
        uvIndexLabel.setVisibility( View.INVISIBLE );
        uvIndexValue.setVisibility( View.INVISIBLE );
        precipitationLabel.setVisibility( View.INVISIBLE );
        precipitationValue.setVisibility( View.INVISIBLE );
        getWindow().setStatusBarColor( getResources().getColor( R.color.black ) );

        String apiKEY = BuildConfig.stormy_key;

        String forecastURL = "https://api.darksky.net/forecast/" + apiKEY + "/" + latitude + "," + longitude;

        if (isNetworkAvailable()) {

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder().url( forecastURL ).build();

            Call call = client.newCall( request );
            call.enqueue( new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText( MainActivity.this, "Something went wrong, try again.", Toast.LENGTH_SHORT ).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {

                            currentWeather = getCurrentDetails(jsonData);

                            final CurrentWeather displayWeather = new CurrentWeather(
                                    currentWeather.getIcon(),
                                    currentWeather.getTime(),
                                    currentWeather.getSunRiseTime(),
                                    currentWeather.getSunSetTime(),
                                    currentWeather.getTemperature(),
                                    currentWeather.getUvIndex(),
                                    currentWeather.getPrecipChance(),
                                    currentWeather.getSummary(),
                                    currentWeather.getTimeZone()
                            );

                            binding.setWeather( displayWeather );

                            // Runs the following code on the main thread, other threads can not change the UI
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    locationTextView.setText( city );
                                    iconImageView.setImageDrawable( getResources().getDrawable( displayWeather.getIconId() ) );
                                    degreeImageView.setVisibility( View.VISIBLE );
                                    timeTextView.setVisibility( View.VISIBLE );
                                    temperatureValue.setVisibility( View.VISIBLE );
                                    uvIndexLabel.setVisibility( View.VISIBLE );
                                    uvIndexValue.setVisibility( View.VISIBLE );
                                    precipitationLabel.setVisibility( View.VISIBLE );
                                    precipitationValue.setVisibility( View.VISIBLE );
                                    setWeatherColor();
                                }
                            } );

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e( TAG, "IO Exception caught", e );
                    } catch (JSONException e) {
                        Log.e( TAG, "JSON Exception caught", e );
                    }

                }
            } );
        }
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject( jsonData );

        JSONObject currently = forecast.getJSONObject( "currently" );
        JSONObject dailyData = forecast.getJSONObject( "daily" ).getJSONArray( "data" ).getJSONObject( 0 );

        CurrentWeather currentWeather = new CurrentWeather();

        currentWeather.setTimeZone(forecast.getString( "timezone" ));
        currentWeather.setUvIndex( currently.getInt( "uvIndex" ) );
        currentWeather.setTime( currently.getLong( "time" ) );
        currentWeather.setSunRiseTime( dailyData.getLong( "sunriseTime" ) );
        currentWeather.setSunSetTime( dailyData.getLong( "sunsetTime" ) );
        currentWeather.setIcon( currently.getString( "icon" ) );
        currentWeather.setPrecipChance( currently.getDouble( "precipProbability" ) );
        currentWeather.setTemperature( currently.getDouble( "apparentTemperature" ) );

        // Gets minutely summary for Canadian cities and hourly summary for other cities
        if (forecast.has( "minutely" ) && forecast.getDouble( "latitude" ) != 34.0522) {
            JSONObject minutely = forecast.getJSONObject( "minutely" );
            currentWeather.setSummary( minutely.getString( "summary" ) );
        }
        else {
            JSONObject hourlyData = forecast.getJSONObject( "hourly" );
            currentWeather.setSummary( hourlyData.getString( "summary" ) );
        }
        return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        else {
            Toast.makeText( this, getString( R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getSupportFragmentManager(), "error_dialog");
    }

    private void setWeatherColor() {
        if (currentWeather.getTime() < currentWeather.getSunRiseTime() || currentWeather.getTime() > currentWeather.getSunSetTime() || currentWeather.getIcon().equals( "fog" )) {
            changeColors( getResources().getColor( R.color.night ) );
        }
        else if (currentWeather.getIcon().equals("clear-day") || currentWeather.getIcon().equals("wind")) {
            changeColors( getResources().getColor( R.color.clear ) );
        }
        else {
            changeColors( getResources().getColor( R.color.stormy ) );
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void changeColors(int color) {
        layout.setBackgroundColor(color);
        mississaugaButton.setTextColor( color );
        waterlooButton.setTextColor( color );
        ottawaButton.setTextColor( color );
        tokyoButton.setTextColor( color );
        losAngelesButton.setTextColor( color );
        dalianButton.setTextColor( color );
        getWindow().setStatusBarColor( color );
    }

    public void mississauga(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                getForecast( locations.getMississaugaLatitude(), locations.getMississaugaLongitude(), "Mississauga");
            }
        } );
    }

    public void waterloo(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                getForecast(locations.getWaterlooLatitude(), locations.getWaterlooLongitude(), "Waterloo");
            }
        } );
    }

    public void ottawa(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                getForecast(locations.getOttawaLatitude(), locations.getOttawaLongitude(), "Ottawa");
            }
        } );
    }

    public void tokyo(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                getForecast( locations.getTokyoLatitude(), locations.getTokyoLongitude(), "Tokyo");
            }
        } );
    }

    public void losAngeles(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                getForecast(locations.getLosAngelesLatitude(), locations.getLosAngelesLongitude(), "Los Angeles");
            }
        } );
    }

    public void dalian(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                getForecast(locations.getDalianLatitude(), locations.getDalianLongitude(), "Dalian");
            }
        } );
    }
}