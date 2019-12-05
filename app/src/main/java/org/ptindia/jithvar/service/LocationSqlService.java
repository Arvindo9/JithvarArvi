package org.ptindia.jithvar.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;
import android.os.Process;

import org.ptindia.jithvar.broadcast.SavingLocToSqlBC;
import org.ptindia.jithvar.config.TrackMeConst;

/**
 * Created by Arvindo Mondal on 28/6/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class LocationSqlService extends Service {

    private LocationManager locationManager;
    private PendingIntent pendingIntent;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        Toast.makeText(this, "service started", Toast.LENGTH_SHORT).show();
//
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//        Intent i = new Intent(this, SavingLocToSqlBC.class);
//        pendingIntent = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        //request for location updates gps
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
//                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//        }
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, pendingIntent);
//
//        return START_STICKY;
//    }

    @Override
    public void onDestroy() {
//        super.onDestroy();
        locationManager.removeUpdates(pendingIntent);
        super.onDestroy();

        Toast.makeText(this, "service destroy", Toast.LENGTH_SHORT).show();
        Log.e("service gg", "destroy");
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Intent i = new Intent(LocationSqlService.this, SavingLocToSqlBC.class);
            pendingIntent = PendingIntent.getBroadcast(LocationSqlService.this,
                    0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }

            Log.e("t/f----------", String.valueOf(TrackMeConst.isTrackEnable()));

            //request for location updates gps
            if (ActivityCompat.checkSelfPermission(LocationSqlService.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(LocationSqlService.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, pendingIntent);

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            if(isNetworkAvailable()) {
                stopSelf(msg.arg1);
            }
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
