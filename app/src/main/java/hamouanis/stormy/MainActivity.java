package hamouanis.stormy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    CurrentWeather mCurrentWeather;

    private TextView mTemperatureLabel;
    private TextView mTimeLabel;
    private TextView mHumidityValue;
    private TextView mPrecipValue;
    private TextView mSummaryLabel;
    private ImageView mIconImageView;
    private ImageView mRefreshImageView;
    private ProgressBar mProgressBar;



    public static final String TAG = MainActivity.class.getSimpleName();

    //REST gives us directly what we need without knowing how...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimeLabel = (TextView) findViewById(R.id.timeLabel);
        mTemperatureLabel = (TextView) findViewById(R.id.temperatureLabel);
        mHumidityValue = (TextView) findViewById(R.id.humidityValue);
        mPrecipValue = (TextView) findViewById(R.id.precipValue);
        mSummaryLabel = (TextView) findViewById(R.id.sumarryLabel);
        mIconImageView = (ImageView) findViewById(R.id.iconImageView);
        mRefreshImageView = (ImageView) findViewById(R.id.refreshImageView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.INVISIBLE);

        final double latitude = 35.698;
        final double longitude = -0.634;

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getForeCast(latitude,longitude);
            }
        });

        getForeCast(latitude,longitude);
    }

    private void getForeCast(double latitude, double longitude) {
        String apiKey ="0ad5bd335f148bf67455d21f0bf39a78";

        String forecastUrl ="https://api.darksky.net/forecast/" + apiKey + "/" + latitude + "," + longitude;


        if (isNetworkAvailable()){

            toggleRefresh();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(forecastUrl).build();

        Call call = client.newCall(request);

        //.enqueue works with the Asynchronous Get (uses another thread) (executed in the background)
        //.execute works with the Synchronous Get (uses main thread)

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateDisplay();
                    }
                });
                alertUserAboutError();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG,jsonData);
                    if (response.isSuccessful()){
                        mCurrentWeather = getCurrentDetails(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                            }
                        });

                    }
                    else{
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG,"Exeption caught :",e);
                }
                catch (JSONException e){
                    Log.e(TAG,"Exeption caught :",e);

                }
            }
        });

        Log.d(TAG,"Main UI code is running! ");
    }

    else {
            Toast.makeText(this, getString(R.string.network_unavailable_message), Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }

    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormatedTime() + " it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity() +"");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());

        Drawable drawable = ContextCompat.getDrawable(this,mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }


    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {

        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG,"From JSON " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.optDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));

        currentWeather.setTimeZone(timezone);

        Log.d(TAG,currentWeather.getFormatedTime());


        return currentWeather ;
    }



    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;

        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }




    private void alertUserAboutError() {

        AlertDialogFragment dialog = new AlertDialogFragment();

        dialog.show(getFragmentManager(),"error_dialog");

    }
}
