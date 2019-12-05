package org.ptindia.jithvar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ptindia.jithvar.config.TrackMeConst;
import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.service.LocationServerService;
import org.ptindia.jithvar.service.LocationSqlService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static org.ptindia.jithvar.config.Config.TRACK_ME;
import static org.ptindia.jithvar.config.Config.TRACK_ME_MUL;

/**
 * Created by Arvindo Mondal on 27/6/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */

public class TrackMeFragment extends Fragment implements View.OnClickListener,
        LocationListener {

    private DataBaseApp db;
    private String userId;

    private LocationManager loctionManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    private double longD, latD;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DataBaseApp(getActivity());
        try {
            userId = db.getTrueUserId();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.trake_me_frag, container, false);
//        Button trak_disable_bt = (Button) rootview.findViewById(R.id.trake_disable);
//        Button trak_enable_bt = (Button) rootview.findViewById(R.id.trake_enable);
//        Button trake_data = (Button) rootview.findViewById(R.id.trake_data);

        rootview.findViewById(R.id.trake_disable).setOnClickListener(this);
        rootview.findViewById(R.id.trake_enable).setOnClickListener(this);
        rootview.findViewById(R.id.trake_data).setOnClickListener(this);

        return rootview;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trake_enable:
//                if(!TrackMeConst.isTrackEnable()) {
//                    TrackMeConst.setIsTrackEnable(true);
//                    checkProvider();
//                }
                break;

            case R.id.trake_disable:
//                TrackMeConst.setIsTrackEnable(false);
//                getActivity().stopService(new Intent(getActivity(), LocationSqlService.class));
//                getActivity().stopService(new Intent(getActivity(), LocationServerService.class));
                break;

            case R.id.trake_data:
                try {
                    ArrayList<String[]> data = db.trackingData();
                    int i = data.size() -1;
                    while (i > -1){
                        String[] d = data.get(i--);
                        Log.e("lat", d[0]);
                        Log.e("lon", d[1]);
                        Log.e("dat", d[2]);
                    }
//                    Toast.makeText(getActivity(), data.size(),
//                            Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public boolean checkProvider() {
        loctionManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        this.isGPSEnabled = loctionManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        this.isNetworkEnabled = loctionManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isNetworkEnabled && !isGPSEnabled) {
            AlertDialog.Builder d = new AlertDialog.Builder(getActivity());
            d.setTitle("GPS Off");
            d.setMessage("GPS is not enabled. Do you want to go to settings menu?");
            d.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                }
            });
            d.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            d.show();
            return false;

        } else {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12);
                //this code will call onRequestPermissionsResult();
            } else {
                try {
                    threadAutomaticUpload();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Log.d("CameraDemo", "Pic saved");

            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12);
                //this code will call onRequestPermissionsResult();
            } else {
                try {
                    threadAutomaticUpload();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 12) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    try {
                        threadAutomaticUpload();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        longD = location.getLongitude();
        latD = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void threadAutomaticUpload() throws Exception {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getActivity(), "permission not granted", Toast.LENGTH_SHORT).show();
        }
        else {
            if(isNetworkAvailable()) {
                getActivity().startService(new Intent(getActivity(),
                        LocationServerService.class));
            }
            else {
                getActivity().startService(new Intent(getActivity(),
                        LocationSqlService.class));
            }
        }
    }

    private void sendPendingData() {
        try {
            ArrayList<String[]> data = db.trackingData();
            int i = data.size() -1;
            JSONArray jsonArray = new JSONArray();
            JSONObject obj = new JSONObject();
            try{
                while (i > -1){
                    String[] d = data.get(i--);
                    obj.put("EmployeeId", userId)
                            .put("Lattitude", d[0])
                            .put("Longitude", d[1])
                            .put("BufferOn", d[2])
                            .put("Status", "passive");
                    jsonArray.put(obj);

                    Log.e("lat", d[0]);
                    Log.e("lon", d[1]);
                    Log.e("dat", d[2]);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String jsonText = jsonArray.toString();

            Log.e("datsf-----------", jsonText);

            if(jsonText != null && !jsonText.equals("")) {
                uploadDataPending(jsonText);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            db.deleteTrakingData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //----------------------------------------------------

    @SuppressWarnings("deprecation")
    private String uploadData() {
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
            entity.addPart("Lattitude",new StringBody(String .valueOf(latD)));
            entity.addPart("Longitude",new StringBody(String .valueOf(longD)));
            entity.addPart("BufferOn",new StringBody(""));
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
//---------------------------------------
    @SuppressWarnings("deprecation")
    private String uploadDataPending(String data) {
        try {
//            int i = data.size() -1;
            URL url = new URL(TRACK_ME_MUL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.connect();

            String reqHead = "Accept:application/json";
            connection.setRequestMethod("POST");
            connection.setRequestProperty("connection","Keep-Alive"+reqHead);
            @SuppressWarnings("deprecation")
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            entity.addPart("JSONdata",new StringBody(data));

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

}
