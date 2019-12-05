package org.ptindia.jithvar.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.ptindia.jithvar.other.NetworkUtil;
import org.ptindia.jithvar.service.UploadLocationService;

import java.net.HttpURLConnection;

/**
 * Created by Arvindo Mondal on 29/6/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class NetworkBC extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String status = NetworkUtil.getConnectivityStatusString(context);

        Toast.makeText(context, status, Toast.LENGTH_LONG).show();
        Log.e("pending--------e--", "bc");

//        if(status != null && (status.equals("1") || status.equals("2"))){
        if(status != null && status.equals("1")){
            Log.e("pending----------", "bc");
            Intent uplIntent = new Intent(context, UploadLocationService.class);
            uplIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(uplIntent);
        }

    }
}
