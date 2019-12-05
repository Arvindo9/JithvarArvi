package org.ptindia.jithvar.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ptindia.jithvar.broadcast.NetworkBC;
import org.ptindia.jithvar.database.DataBaseApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.ptindia.jithvar.config.Config.ON_OFF_DATA;
import static org.ptindia.jithvar.config.Config.ON_OFF_DATA_MUL;
import static org.ptindia.jithvar.config.Config.TRACK_ME_MUL;

/**
 * Created by Arvindo Mondal on 29/6/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class UploadLocationService extends IntentService {


    private DataBaseApp db;
    private String userId;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public UploadLocationService() {
        super("UploadLocationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.e("datsf....-----------","-------------------------");
        if(isNetworkAvailable()){
            db = new DataBaseApp(this);
            try {
                userId = db.getTrueUserId();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendingOnOffData();
                    }
                }).start();

                while (db.isSqlEmpty()){
                    sendPendingData();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


        Toast.makeText(this, "service destroy upload", Toast.LENGTH_SHORT).show();
    }

    private void sendPendingData() {
        try {
            ArrayList<String[]> data = db.trackingData();
            int i = data.size() -1;
            JSONArray jsonArray = new JSONArray();
            try{
                while (i > -1){
                    String[] d = data.get(i--);
                    JSONObject obj = new JSONObject();
                    obj.put("EmployeeId", userId)
                            .put("Lattitude", d[0])
                            .put("Longitude", d[1])
                            .put("BufferOn", d[2])
                            .put("Location", d[3])
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

            if(jsonText != null && !jsonText.equals("[]")) {
                String s =uploadDataPending(jsonText, TRACK_ME_MUL);
                Log.e("from x----------x", s);
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

    //---------------------------------------
    @SuppressWarnings("deprecation")
    private String uploadDataPending(String data, String urlStr) {
        try {
//            int i = data.size() -1;
            URL url = new URL(urlStr);
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

            Log.e("1-----------", String.valueOf(connection.getResponseCode()));
            Log.e("2----------", String.valueOf(HttpURLConnection.HTTP_OK));

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
        Log.e("pending form server ULS", response);

        return response;
    }

    //------------------------------

    private void sendingOnOffData() {
        try {
            ArrayList<String[]> data = db.onOFFData();
            int i = data.size() -1;
            JSONArray jsonArray = new JSONArray();
            try{
                while (i > -1){
                    String[] d = data.get(i--);
                    JSONObject obj = new JSONObject();
                    obj.put("EmployeeId", userId)
                            .put("TrackStatus", d[0])
                            .put("EnterOn", d[1])
                            .put("Status", "passive");
                    jsonArray.put(obj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String jsonText = jsonArray.toString();

            Log.e("lkjbn", jsonText);

            if(jsonText != null && !jsonText.equals("[]")) {
                String s =uploadDataPending(jsonText, ON_OFF_DATA_MUL);
                Log.e("on Off x----------x", s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            db.deleteONOFF();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
