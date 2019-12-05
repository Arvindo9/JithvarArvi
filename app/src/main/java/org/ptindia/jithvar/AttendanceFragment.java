package org.ptindia.jithvar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.handler.DataBaseHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static android.content.Context.LOCATION_SERVICE;
import static org.ptindia.jithvar.config.Config.ATTENDENCE;

public class AttendanceFragment extends Fragment implements LocationListener {
    private DataBaseApp db;
    private String userId;
    private String phoneContact = "12345";

    private Button onGPS_bt, takePic_bt, submit_bt;
    private ImageView pic_i;
    private int TAKE_PHOTO_CODE = 1;
    private LocationManager loctionManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    private File fileForUpload;
    private String fileExtention;
    private FileBody picBody;
    private long fileSize;
    private ProgressDialog mProgressDialog;

    private String locationAddress = "";
    private String latitude = "";
    private String longitude = "";

    private String mprovider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DataBaseApp(getActivity());
        try {
            userId = db.getTrueUserId();
            phoneContact = db.getPhoneNum(userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        checkProvider();

    }

    public boolean checkProvider() {
        loctionManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        this.isGPSEnabled = loctionManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        this.isNetworkEnabled = loctionManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isNetworkEnabled && !isGPSEnabled) {
            // btnShowLocation.setChecked(false);
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

        }else {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12);
            } else {
                loctionManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }  //  loctionManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);}
        return true;
    }

    private boolean providerEnable() {
        loctionManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        this.isGPSEnabled = loctionManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        this.isNetworkEnabled = loctionManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isNetworkEnabled && isGPSEnabled;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_attendance, container, false);

        onGPS_bt = (Button) rootview.findViewById(R.id.gps);
        submit_bt = (Button) rootview.findViewById(R.id.submit);
        takePic_bt = (Button) rootview.findViewById(R.id.take_pic);
        pic_i = (ImageView) rootview.findViewById(R.id.pic);

        inatialLoading();

        return rootview;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(providerEnable()){
            onGPS_bt.setVisibility(View.GONE);
            submit_bt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(providerEnable()){
            onGPS_bt.setVisibility(View.GONE);
            submit_bt.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        loctionManager.removeUpdates(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        loctionManager.removeUpdates(this);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode==12){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission. ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    loctionManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,500,0,this);
                    submit_bt.setVisibility(View.VISIBLE);
//                    takePicture_bt.setVisibility(View.GONE);
                }
            }

        }
    }

    void inatialLoading() {

        onGPS_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        submit_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isNetworkAvailable()) {
                    if (picBody != null) {
                        loctionManager.removeUpdates(AttendanceFragment.this);
                        new AttendanceFragment.SubmitToServer().execute();
                    } else {
                        Toast.makeText(getActivity(), "take picture", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
                }
//                new AttendanceFragment.SubmitToServer().execute();
            }
        });

        takePic_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TAKE_PHOTO_CODE);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            Log.d("CameraDemo", "Pic saved");

            Bundle extras = data.getExtras();
            final Bitmap bitmap = (Bitmap) extras.get("data");

            BitmapDrawable bmD = new BitmapDrawable(getResources(), bitmap);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Bitmap bmp = (Bitmap) extras.getParcelable("data");
                pic_i.setImageBitmap(bmp);
            }
            else{
                pic_i.setBackgroundDrawable(bmD);
            }

            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission. ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                loctionManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
                submit_bt.setVisibility(View.VISIBLE);
//                    takePicture_bt.setVisibility(View.GONE);
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    saveImgToInternalStorage(bitmap);
                }
            }).start();

        }
    }

    private void saveImgToInternalStorage(Bitmap bitmap){
        ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
        File directory = cw.getDir("temp_folder", Context.MODE_PRIVATE);
        int a = (int)(Math.random() * 100);
        int b = 100 + (int)(Math.random() * 500);
        int c = 700 + (int)(Math.random() * 1000);

        String pathOfPic = "img_" + a + "_" + b + "_" + c + ".png" ;

        File imgPath = new File(directory, pathOfPic);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(imgPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        imgPath = new File(directory, pathOfPic);
        if (imgPath.exists()){
            fileForUpload = imgPath;
            fileExtention = pathOfPic.substring(pathOfPic.lastIndexOf(".") + 1, pathOfPic.length());
            picBody = new FileBody(fileForUpload);
            fileSize = fileForUpload.length();

            Log.e("fils exist", directory.getAbsolutePath() + "/" + pathOfPic);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        longitude = String.valueOf(location.getLongitude());
        latitude = String.valueOf(location.getLatitude());

        Log.e("longitude", longitude);
        Log.e(latitude, latitude);

        /*------- To get city name from coordinates -------- */
        Double longD = location.getLongitude();
        Double latD = location.getLatitude();

        try {
            Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
            List<Address> addresses = null;
            addresses = geocoder.getFromLocation(latD, longD, 1);
            String cityName = addresses.get(0).getAddressLine(0);
            String stateName = addresses.get(0).getAddressLine(1);
            String countryName = addresses.get(0).getAddressLine(2);
            locationAddress = cityName + " " + stateName + " " + countryName;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        loctionManager.removeUpdates(this);
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

    //----------------------------------------------------

    private class SubmitToServer extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity(), R.style.MyTheme);
//            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {
            return uploadFile();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();

            if (result != null) {
                Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Try Again", Toast.LENGTH_SHORT).show();
            }
            Log.d("DownloadTextTask", result);
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            try {
                URL url = new URL(ATTENDENCE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //connection.connect();

                String reqHead = "Accept:application/json";
                connection.setRequestMethod("POST");
                connection.setRequestProperty("connection","Keep-Alive"+reqHead);
                @SuppressWarnings("deprecation")
                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                entity.addPart("EmployeeId",new StringBody(userId));
                entity.addPart("Contact",new StringBody(phoneContact));
                entity.addPart("Lattitude",new StringBody(latitude));
                entity.addPart("Longitude",new StringBody(longitude));
                entity.addPart("Location",new StringBody(locationAddress));
                entity.addPart("fileImage", picBody);

                connection.addRequestProperty("content-length",entity.getContentLength()+"");
                connection.addRequestProperty(entity.getContentType().getName(),
                        entity.getContentType().getValue());

                OutputStream os = connection.getOutputStream();
                entity.writeTo(connection.getOutputStream());
                os.close();
                Log.d("HITTING","hitting url");
                connection.connect();

                Log.e("code1 ", String.valueOf(connection.getResponseCode()));
                Log.e("code2 ", String.valueOf(HttpURLConnection.HTTP_OK));

                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                    return readStream(connection.getInputStream());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.e("method used " + "no", String.valueOf(fileSize));
            return "fails";
        }
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
        Log.e("response form server ", response);

        try {
            response = initializedData(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private String initializedData(String response) throws Exception {
        if(response != null && !response.equals("")){
            /*
            try {
                Log.e("sdf", response);
//                JSONArray jsonArray = new JSONArray(response);

//                for(int i=0;i<jsonArray.length();i++){
//                    JSONObject c = jsonArray.getJSONObject(i);
                JSONObject c = new JSONObject(response);
                final String status = c.getString("status");

                if(status.equals("Success")){
                    response = "Attendance successful";
                }
                else{
                    response = "error";
                }
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            */
            if(response.equals("Success")){
                response = "Attendance successful";
            }
            else{
                response = "error";
            }
        }
        return response;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}