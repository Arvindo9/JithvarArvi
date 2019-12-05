package org.ptindia.jithvar;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.ptindia.jithvar.config.Config;
import org.ptindia.jithvar.config.TrackMeConst;
import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.handler.DataBaseHandler;
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

/**
 * Created by Arvindo Mondal on 20/6/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */

public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private Toolbar toolbar;
    private DataBaseApp db;// = new DataBaseApp(HomeActivity.this);
    private Button on;
    private Button off;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_activity);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        db = new DataBaseApp(this);
        try {
            userId = db.getTrueUserId();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        on = (Button) findViewById(R.id.on);
        off = (Button) findViewById(R.id.off);

        on.setOnClickListener(this);
        off.setOnClickListener(this);

        getFragmentManager().beginTransaction().replace(R.id.container,new ProfileFragment()).commit();

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        DataBaseApp db = new DataBaseApp(this);
        try {
//            String userId = db.getTrueUserId();
            TrackMeConst.setUserId(db.getTrueUserId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {
            if(db.isTrackingEnable()){
                on.setBackgroundColor(getResources().getColor(R.color.track_color));
                off.setBackgroundColor(Color.TRANSPARENT);
            }
            else{
                off.setBackgroundColor(getResources().getColor(R.color.track_color));
                on.setBackgroundColor(Color.TRANSPARENT);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setCheckable(true);
        int id = item.getItemId();
        switch (id){
            case R.id.profile:
                getFragmentManager().beginTransaction().replace(R.id.container,
                        new ProfileFragment()).addToBackStack(null).commit();
                break;

            case R.id.attendance:
                getFragmentManager().beginTransaction().replace(R.id.container,
                        new AttendanceFragment()).addToBackStack(null).commit();
                break;


            case R.id.attendance_other:
                getFragmentManager().beginTransaction().replace(R.id.container,
                        new AttendanceFragmentOther()).addToBackStack(null).commit();
                break;

            case R.id.update:
                getFragmentManager().beginTransaction().replace(R.id.container,
                        new UpdateFragment()).addToBackStack(null).commit();
                break;

            case R.id.track:
//                getFragmentManager().beginTransaction().replace(R.id.container,
//                        new TrackFragment()).addToBackStack(null).commit();

//                getSupportFragmentManager().beginTransaction().replace(R.id.container,
//                        new TrackFragment()).commit();//now replace the argument fragment

                startActivity(new Intent(HomeActivity.this, TrackActivity.class));
                break;

            case R.id.inbox:
//                getFragmentManager().beginTransaction().replace(R.id.container,
//                        new InboxFragment()).addToBackStack(null).commit();
                break;


//            case R.id.track_me:
//                getFragmentManager().beginTransaction().replace(R.id.container,
//                        new TrackMeFragment()).addToBackStack(null).commit();
//                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.on:
                Toast.makeText(this, "on", Toast.LENGTH_SHORT).show();
                if(!isNetworkAvailable()) {
                    try {
//                    Log.e("button", String.valueOf(db.isTrackingEnable()));
                        db.insertONffTb(new DataBaseHandler("on", true));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadDataPending(userId, "on");
                        }
                    }).start();
                }

                try {
                    if(!db.isTrackingEnable()) {
                        on.setBackgroundColor(getResources().getColor(R.color.track_color));
                        off.setBackgroundColor(Color.TRANSPARENT);

                        db.insertTrackingED_TB(new DataBaseHandler("true"));
                        checkProvider();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.off:
                Toast.makeText(this, "off", Toast.LENGTH_SHORT).show();
                if(!isNetworkAvailable()) {
                    try {
//                    Log.e("button", String.valueOf(db.isTrackingEnable()));
                        db.insertONffTb(new DataBaseHandler("off", false));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            uploadDataPending(userId, "off");
                        }
                    }).start();
                }
                try {
                    off.setBackgroundColor(getResources().getColor(R.color.track_color));
                    on.setBackgroundColor(Color.TRANSPARENT);

                    db.insertTrackingED_TB(new DataBaseHandler("false"));
//                    stopService(new Intent(this, LocationSqlService.class));
                    stopService(new Intent(this, LocationServerService.class));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public boolean checkProvider() {

        LocationManager loctionManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean isGPSEnabled = loctionManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = loctionManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isNetworkEnabled && !isGPSEnabled) {
            AlertDialog.Builder d = new AlertDialog.Builder(this);
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
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
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

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
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

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 12);
            return false;
        } else {
            return true;
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 12) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
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
    private void threadAutomaticUpload() throws Exception {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show();
        }
        else {
            startService(new Intent(this,
                    LocationServerService.class));
//            if(isNetworkAvailable()) {
//                startService(new Intent(this,
//                        LocationServerService.class));
//            }
//            else {
//                startService(new Intent(this,
//                        LocationSqlService.class));
//            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //--------------------------

    //---------------------------------------
    @SuppressWarnings("deprecation")
    private String uploadDataPending(String userId, String status) {
        try {
//            int i = data.size() -1;
            URL url = new URL(Config.ON_OFF_DATA);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.connect();

            String reqHead = "Accept:application/json";
            connection.setRequestMethod("POST");
            connection.setRequestProperty("connection","Keep-Alive"+reqHead);
            @SuppressWarnings("deprecation")
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            entity.addPart("EmployeeId",new StringBody(userId));
            entity.addPart("TrackStatus",new StringBody(status));
            entity.addPart("EnterOn",new StringBody(""));
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

        return response;
    }

}
