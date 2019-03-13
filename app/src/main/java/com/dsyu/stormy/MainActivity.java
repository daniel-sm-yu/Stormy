package com.dsyu.stormy;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather currentWeather;
    private Locations locations = new Locations();

    private ImageView iconImageView;
    private TextView locationTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        getForecast(locations.getMississaugaLatitude(), locations.getMississaugaLongitude());

        locationTextView = findViewById( R.id.locationValue );
    }

    private void getForecast(double latitude, double longitude) {
        final ActivityMainBinding binding = DataBindingUtil.setContentView( MainActivity.this, R.layout.activity_main );

        TextView darkSky = findViewById( R.id.darkSkyAttribution );
        darkSky.setMovementMethod( LinkMovementMethod.getInstance() );

        iconImageView = findViewById( R.id.iconImageView );

        String apiKEY = "9f1b5bba01b440c97205eb1ec559be48";

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

                            // runs the following code on the main thread, other threads can not change the UI
                            runOnUiThread( new Runnable() {
                                @Override
                                public void run() {
                                    iconImageView.setImageDrawable( getResources().getDrawable( displayWeather.getIconId() ) );
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

    public void mississauga(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                locationTextView.setText( "Mississauga" );
                getForecast( locations.getMississaugaLatitude(), locations.getMississaugaLongitude() );
            }
        } );
    }

    public void waterloo(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                locationTextView.setText( "Waterloo" );
                getForecast(locations.getWaterlooLatitude(), locations.getWaterlooLongitude());
            }
        } );
    }

    public void ottawa(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                locationTextView.setText( "Ottawa" );
                getForecast(locations.getOttawaLatitude(), locations.getOttawaLongitude());
            }
        } );
    }

    public void montreal(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                locationTextView.setText( "Montreal" );
                getForecast( locations.getMontrealLatitude(), locations.getMontrealLongitude() );
            }
        } );
    }

    public void losAngeles(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                locationTextView.setText( "Los Angeles" );
                getForecast(locations.getLosAngelesLatitude(), locations.getLosAngelesLongitude());
            }
        } );
    }

    public void dalian(View view) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                locationTextView.setText( "Dalian" );
                getForecast(locations.getDalianLatitude(), locations.getDalianLongitude());
            }
        } );
    }
}
