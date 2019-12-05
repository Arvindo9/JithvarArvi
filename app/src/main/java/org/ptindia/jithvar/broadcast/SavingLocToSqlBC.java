package org.ptindia.jithvar.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.ptindia.jithvar.config.TrackMeConst;
import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.handler.DataBaseHandler;
import org.ptindia.jithvar.service.LocationServerService;
import org.ptindia.jithvar.service.LocationSqlService;

import java.sql.SQLException;

/**
 * Created by Arvindo Mondal on 28/6/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class SavingLocToSqlBC extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String locationKey = LocationManager.KEY_LOCATION_CHANGED;
        String provideEnableKey = LocationManager.KEY_PROVIDER_ENABLED;

        if(intent.hasExtra(provideEnableKey)){
            if(!intent.getBooleanExtra(provideEnableKey, true)){
                Toast.makeText(context, "provider disable", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "provider enable", Toast.LENGTH_SHORT).show();
            }
        }

        if (intent.hasExtra(locationKey)){
            Location location = (Location) intent.getExtras().get(locationKey);
            Toast.makeText(context, "lat " + location.getLatitude() +
                    " \n" + "long " + location.getLongitude(), Toast.LENGTH_SHORT).show();

            Log.e("lat " , String.valueOf(location.getLatitude()));
            Log.e("long " , String.valueOf(location.getLongitude()));

            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if(!isNetworkAvailable(connectivityManager)){
                try {
                    DataBaseApp db = new DataBaseApp(context);
                    db.insertTrackingTb(new DataBaseHandler(db.getTrueUserId(),
                            String .valueOf(location.getLatitude()),
                            String .valueOf(location.getLongitude()), ""));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            else {
                context.stopService(new Intent(context, LocationSqlService.class));
                context.stopService(new Intent(context, LocationServerService.class));
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
                context.startService(new Intent(context, LocationServerService.class));
//                    }
//                }).start();
            }

//            try {
//                DataBaseApp db = new DataBaseApp(context);
//                db.insertTrackingTb(new DataBaseHandler("69", String .valueOf(location.getLatitude()),
//                        String .valueOf(location.getLongitude())));
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
        }


    }

    private boolean isNetworkAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
