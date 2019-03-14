package com.dsyu.stormy;

import android.annotation.TargetApi;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
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
    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView precipitationTextView;
    private Button mississaugaButton;
    private Button waterlooButton;
    private Button ottawaButton;
    private Button montrealButton;
    private Button losAngelesButton;
    private Button dalianButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        getForecast(locations.getMississaugaLatitude(), locations.getMississaugaLongitude(), "Mississauga");
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
        temperatureTextView = findViewById( R.id.temperatureValue );
        humidityTextView = findViewById( R.id.humidityValue );
        precipitationTextView = findViewById( R.id.precipValue);
        mississaugaButton = findViewById( R.id.mississaugaButton );
        waterlooButton = findViewById( R.id.waterlooButton );
        ottawaButton = findViewById( R.id.ottawaButton );
        montrealButton = findViewById( R.id.montrealButton );
        losAngelesButton = findViewById( R.id.losAngelesButton );
        dalianButton = findViewById( R.id.dalianButton );

        // Avoids default text from being displayed during the API call
        degreeImageView.setVisibility( View.INVISIBLE );
        timeTextView.setVisibility( View.INVISIBLE );
        temperatureTextView.setVisibility( View.INVISIBLE );
        humidityTextView.setVisibility( View.INVISIBLE );
        precipitationTextView.setVisibility( View.INVISIBLE );
        getWindow().setStatusBarColor( getResources().getColor( R.color.black ) );

        String apiKEY = "f5826972faf15ce3b0d0fb1101ac945c";

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
                                    currentWeather.getTemperature(),
                                    currentWeather.getHumidity(),
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
                                    temperatureTextView.setVisibility( View.VISIBLE );
                                    humidityTextView.setVisibility( View.VISIBLE );
                                    precipitationTextView.setVisibility( View.VISIBLE );
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

        CurrentWeather currentWeather = new CurrentWeather();

        currentWeather.setTimeZone(forecast.getString( "timezone" ));
        currentWeather.setHumidity( currently.getDouble( "humidity" ) );
        currentWeather.setTime( currently.getLong( "time" ) );
        currentWeather.setIcon( currently.getString( "icon" ) );
        currentWeather.setPrecipChance( currently.getDouble( "precipProbability" ) );
        currentWeather.setSummary( currently.getString( "summary" ) );
        currentWeather.setTemperature( currently.getDouble( "temperature" ) );

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
        if (currentWeather.getHour() < 7 || currentWeather.getHour() > 18) {
            changeColors( getResources().getColor( R.color.night ) );
        }
        else if (currentWeather.getIcon().equals("clear-day") || currentWeather.getIcon().equals("clear-night") || currentWeather.getIcon().equals("wind")) {
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
        montrealButton.setTextColor( color );
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

    public void montreal(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                getForecast( locations.getMontrealLatitude(), locations.getMontrealLongitude(), "Montreal");
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