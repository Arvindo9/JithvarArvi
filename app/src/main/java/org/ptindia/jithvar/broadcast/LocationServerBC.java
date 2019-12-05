package org.ptindia.jithvar.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.ptindia.jithvar.config.TrackMeConst;
import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.handler.DataBaseHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import static org.ptindia.jithvar.config.Config.TRACK_ME;

/**
 * Created by Arvindo Mondal on 28/6/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class LocationServerBC extends BroadcastReceiver {

//    String locationAddress = "";
    String userId = "";

    @Override
    public void onReceive(final Context context, Intent intent) {
        String locationKey = LocationManager.KEY_LOCATION_CHANGED;
        String provideEnableKey = LocationManager.KEY_PROVIDER_ENABLED;

        try {
            userId = new DataBaseApp(context).getTrueUserId();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(intent.hasExtra(provideEnableKey)){
            if(!intent.getBooleanExtra(provideEnableKey, true)){
                Toast.makeText(context, "provider disable", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "provider enable", Toast.LENGTH_SHORT).show();
            }
        }

        if (intent.hasExtra(locationKey)){
            final Location location = (Location) intent.getExtras().get(locationKey);
//            Toast.makeText(context, "lat t" + location.getLatitude() +
//                    " \n" + "long t" + location.getLongitude(), Toast.LENGTH_SHORT).show();

//            Log.e("lat t" , String.valueOf(location.getLatitude()));
//            Log.e("long t" , String.valueOf(location.getLongitude()));


            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if(isNetworkAvailable(connectivityManager)){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String locationAddress = "";
//                            Log.e("from server--start--" , "wait");
                            try {
                                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                                List<Address> addresses = null;
                                addresses = geocoder.getFromLocation(location.getLatitude(),
                                        location.getLongitude(), 1);
                                String cityName = addresses.get(0).getAddressLine(0);
                                String stateName = addresses.get(0).getAddressLine(1);
                                String countryName = addresses.get(0).getAddressLine(2);
                                locationAddress = cityName + " " + stateName + " " + countryName;
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            String result = uploadData(location.getLatitude(), location.getLongitude(),
                                    locationAddress);
                            if((!result.equals("success") || (!result.equals("Success")))){
                                try {
                                    DataBaseApp db = new DataBaseApp(context);
                                    db.insertTrackingTb(new DataBaseHandler(db.getTrueUserId(),
                                            String .valueOf(location.getLatitude()),
                                            String .valueOf(location.getLongitude()), locationAddress));
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }


//                            Log.e("from server----------" , result);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String locationAddress = "";
                        Log.e("enter bd" , "true");
                        try {
                            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                            List<Address> addresses = null;
                            addresses = geocoder.getFromLocation(location.getLatitude(),
                                    location.getLongitude(), 1);
                            String cityName = addresses.get(0).getAddressLine(0);
                            String stateName = addresses.get(0).getAddressLine(1);
                            String countryName = addresses.get(0).getAddressLine(2);
                            locationAddress = cityName + " " + stateName + " " + countryName;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        try {
                            DataBaseApp db = new DataBaseApp(context);
                            db.insertTrackingTb(new DataBaseHandler(db.getTrueUserId(),
                                    String .valueOf(location.getLatitude()),
                                    String .valueOf(location.getLongitude()), locationAddress));
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Log.e("enter bd" , "false");
                        }
                    }
                }).start();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private String uploadData(double latitude, double longitude, String locationAddress)
            throws Exception {
        try {
            URL url = new URL(TRACK_ME);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.connect();

            String reqHead = "Accept:application/json";
            connection.setRequestMethod("POST");
            connection.setRequestProperty("connection","Keep-Alive"+reqHead);
            @SuppressWarnings("deprecation")
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            entity.addPart("EmployeeId",new StringBody(userId));
            entity.addPart("Lattitude",new StringBody(String .valueOf(latitude)));
            entity.addPart("Longitude",new StringBody(String .valueOf(longitude)));
//            entity.addPart("BufferOn",new StringBody(""));
            entity.addPart("Location",new StringBody(locationAddress));
            entity.addPart("Status",new StringBody("active"));

            connection.addRequestProperty("content-length",entity.getContentLength()+"");
            connection.addRequestProperty(entity.getContentType().getName(),
                    entity.getContentType().getValue());

            OutputStream os = connection.getOutputStream();
            entity.writeTo(connection.getOutputStream());
            os.close();
            Log.d("HITTING","hitting url");
            connection.connect();

            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                return readStream(connection.getInputStream());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "false";
    }

    private String readStream(InputStream inputStream) {

        String response = "";
        BufferedReader reader;
        StringBuilder builder = new StringBuilder();

        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine())!=null){
                builder.append(line);
                Log.e("\n", builder.toString());
            }
            response = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e("responset form server ", response);

        return response;
    }

    private boolean isNetworkAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
