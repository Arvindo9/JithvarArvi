package org.ptindia.jithvar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ptindia.jithvar.dialog.DatePickerFragment;
import org.ptindia.jithvar.handler.MapItem;
import org.ptindia.jithvar.handler.TrackData;
import org.ptindia.jithvar.interfaceClass.DateInterface;
import org.ptindia.jithvar.service.FetchAddressIntentService;
import org.ptindia.jithvar.service.LocationAddress;
import org.ptindia.jithvar.webservice.WebServiceHandler;
import org.ptindia.jithvar.webservice.WebServiceListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static org.ptindia.jithvar.config.Config.TRACK_MOBILE_EMP;

/**
 * Created by Arvindo Mondal on 10/8/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class TrackActivity extends AppCompatActivity implements
        View.OnClickListener, ClusterManager.OnClusterClickListener<MapItem>,
        ClusterManager.OnClusterInfoWindowClickListener<MapItem>,
        ClusterManager.OnClusterItemClickListener<MapItem>,
        ClusterManager.OnClusterItemInfoWindowClickListener<MapItem>,
        DateInterface{

    private SupportMapFragment mapFragment;
    private ArrayList<TrackData> empDataList;
    private ArrayList<TrackData> trackDataList;
    private SearchableSpinner employName;
    private int pos;
    private static String dateToTrack = "";
    private String empIdToTrack = "";
    private String empNameToTrack = "";
    private Button dateBtn;

    // Declare a variable for the cluster manager.
    private ClusterManager<MapItem> mClusterManager;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_frag);

        dateBtn = (Button) findViewById(R.id.select_date);

        empDataList = new ArrayList<>();
        trackDataList = new ArrayList<>();

        employName=(SearchableSpinner) findViewById(R.id.emp_name_sp);
        employName.setTitle("Name");
        if(isNetworkAvailable()) {
            getEmployeeDetails(employName);
        }
        else{
            Toast.makeText(this, getResources().getText(R.string.no_network),
                    Toast.LENGTH_SHORT).show();
        }
        loadData();

        findViewById(R.id.select_date).setOnClickListener(this);
        findViewById(R.id.search).setOnClickListener(this);


//        MapFragment mapFragment = (MapFragment) getFragmentManager()
//                .findFragmentById(R.id.map);

        this.mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

//        mapFragment.getMapAsync(new GoogleMapArvi());
//        mapFragment.getMapAsync(new GoogleMapArvi());
//        new GoogleMapArvi().loadMap(mapFragment);
    }

    private void loadData(){
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        month++;
        String monthStr = String.valueOf(month);
        String dayStr = String.valueOf(day);
        if(monthStr.length() < 2){
            monthStr = "0" + String.valueOf(month);
        }
        if(dayStr.length() < 2){
            dayStr = "0" + String.valueOf(day);
        }

        dateToTrack = year + "-" + monthStr + "-" + dayStr;

//        ((TextView)findViewById(R.id.select_date)).setText(dateToTrack);
        dateBtn.setText(dateToTrack);

        employName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                //  pos = arg2;
                pos = employName.getSelectedItemPosition();
                pos--;
                Log.e("position",Integer.toString(pos));
                if (pos >= 0) {
                    empIdToTrack = empDataList.get(pos).getEmployeeId();
                    empNameToTrack = empDataList.get(pos).getEmployeeName();
                    Log.e("empidname",empNameToTrack);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }});
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.select_date:
                DialogFragment newFragment = new DatePickerFragment(this);
                newFragment.show(getSupportFragmentManager(), "datePicker");
//                dateBtn.setText(dateToTrack);
                break;

            case R.id.search:
                if(isNetworkAvailable()) {
                    if (!empIdToTrack.equals("") && !dateToTrack.equals("")) {
//                        Toast.makeText(TrackActivity.this, dateToTrack, Toast.LENGTH_SHORT).show();
                        new ConnectingToServer().execute("");
                    } else if (empIdToTrack.equals("")) {
                        Toast.makeText(TrackActivity.this, "select name", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TrackActivity.this, "select date", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(this, getResources().getText(R.string.no_network),
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void getEmployeeDetails(final Spinner spinner) {
        final ArrayList<String> list = new ArrayList<>();
        list.add("select name");

        WebServiceHandler serviceHandler = new WebServiceHandler(this);
        serviceHandler.webServiceListener = new WebServiceListener() {
            @Override
            public void onDataReceived(String response) {
                Log.e("Web Responce", response);
                if (response != null) {
                    try {
                        JSONArray jsnarray= new JSONArray(response);
                        for(int i=0;i<jsnarray.length();i++){
                            try{
                                JSONObject jsnobj = jsnarray.getJSONObject(i);

                                String empName =  jsnobj.getString("EmployeeName");
                                String empID =  jsnobj.getString("EmployeeId");
                                String status =  jsnobj.getString("status");

                                empDataList.add(new TrackData(empID, empName, status));
                                list.add(empName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        spinner.setAdapter(new ArrayAdapter<String>(TrackActivity.this,
                                android.R.layout.simple_dropdown_item_1line, list));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        try {
            serviceHandler.getEmpDetails();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //-----------------------map cluster-------------------------------------------

    @Override
    public boolean onClusterClick(Cluster<MapItem> cluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MapItem> cluster) {

    }

    @Override
    public boolean onClusterItemClick(MapItem mapItem) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(MapItem mapItem) {

    }

    //----------map cluster------------------
    private void setUpClusterer(final GoogleMap getMap) {
        // Position the map.
        int i = trackDataList.size()-1;
        if(i > -1) {
            final LatLng latLng = new LatLng(
                    Double.parseDouble(trackDataList.get(i).getLattitude()),
                    Double.parseDouble(trackDataList.get(i).getLongitude()));

            getMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));

            mClusterManager = new ClusterManager<MapItem>(this, getMap);
            mClusterManager.setRenderer(new LocationRenderer(this, getMap));
            getMap.setOnCameraIdleListener(mClusterManager);
            getMap.setOnMarkerClickListener(mClusterManager);
            getMap.setOnInfoWindowClickListener(mClusterManager);
            mClusterManager.setOnClusterClickListener(this);
            mClusterManager.setOnClusterInfoWindowClickListener(this);
            mClusterManager.setOnClusterItemClickListener(this);
            mClusterManager.setOnClusterItemInfoWindowClickListener(this);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    addItems();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mClusterManager.cluster();
                        }
                    });
                }
            }).start();
//            addItems(getMap);

//            mClusterManager.cluster();
            Log.e("map------------", "-------------");
        }
    }

    private void setUpClusterer1(final GoogleMap getMap) {
        // Position the map.
        getMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 10));

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<MapItem>(this, getMap);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mClusterManager.setRenderer(new LocationRenderer(this, getMap));
        getMap.setOnCameraIdleListener(mClusterManager);
        getMap.setOnMarkerClickListener(mClusterManager);
        getMap.setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        mClusterManager.cluster();
        // Add cluster items (markers) to the cluster manager.
        addItems1();
    }

    private void addItems1() {

        // Set some lat/lng coordinates to start with.
        double lat = 51.5145160;
        double lng = -0.1270060;

        int j =0;
        int k = 1;
        // Add ten cluster items in close proximity, for purposes of this example.
        for (int i = 0; i < 100; i++) {
            double offset = i / 60d;
            lat = lat + offset;
            lng = lng + offset;
            MapItem offsetItem = new MapItem(lat, lng, "kjfkdjfk",
                "df sdfsd fsdf dfds f",
                    k);
            mClusterManager.addItem(offsetItem);
            if(j > 10){
                j = 0;
                k++;
            }
            j++;
        }
    }

    private void addItems() {
        for (int i = 0; i < trackDataList.size(); i++) {
            int image = Integer.parseInt(trackDataList.get(i).getTime().substring(0, 2));
            switch (image) {
                case 9:
                    image = 2;
                    break;
                case 10:
                    image = 3;
                    break;
                case 11:
                    image = 8;
                    break;
                case 12:
                    image = 5;
                    break;
                case 13:
                    image = 6;
                    break;
                case 15:
                    image = 9;
                    break;
                case 16:
                    image = 10;
                    break;
                case 17:
                    image = 1;
                    break;
                case 18:
                    image = 4;
                    break;
                case 19:
                    image = 7;
                    break;
                default:
                    image = 50;
                    break;
            }
            //for location address
            if(trackDataList.get(i).getLocation().equals("") ||
                trackDataList.get(i).getLocation().equals("null") ||
                    trackDataList.get(i).getLocation() == null){
                Geocoder geocoder;
                List<Address> addresses = null;
                String address = "address loading...";
                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(
                            Double.parseDouble(trackDataList.get(i).getLattitude()),
                            Double.parseDouble(trackDataList.get(i).getLongitude()),
                            1);

                    String addressLine = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();

                    address = addressLine + ", " + city + ", " + state +
                            " " + country + " \n" +
                            postalCode + ", " + knownName;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MapItem offsetItem = new MapItem(
                        Double.parseDouble(trackDataList.get(i).getLattitude()),
                        Double.parseDouble(trackDataList.get(i).getLongitude()),
                        address,
                        trackDataList.get(i).getDate() + " " +
                                trackDataList.get(i).getTime() + " " +
                                trackDataList.get(i).getStatus(),
                                image);
                mClusterManager.addItem(offsetItem);
            }
            else {
                MapItem offsetItem = new MapItem(
                        Double.parseDouble(trackDataList.get(i).getLattitude()),
                        Double.parseDouble(trackDataList.get(i).getLongitude()),
                        trackDataList.get(i).getLocation(),
                        trackDataList.get(i).getDate() + " " +
                                trackDataList.get(i).getTime() + " " +
                                trackDataList.get(i).getStatus(),
                                image);
                mClusterManager.addItem(offsetItem);
            }

            Log.e("image ", String.valueOf(image) + "  " +
                    trackDataList.get(i).getTime());
        }
    }

    @Override
    public void DateSet(String date) {
        dateBtn.setText(date);
    }

    //--------------designing cluster-------------------------

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class LocationRenderer1 extends DefaultClusterRenderer<MapItem> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        LocationRenderer1(GoogleMap googleMap) {
            super(getApplicationContext(), googleMap, mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);
//
            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(MapItem person, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            mImageView.setImageResource(person.iconImage);
            Bitmap icon = mIconGenerator.makeIcon();

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .title(person.getTitle())
                    .snippet(person.getSnippet());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MapItem> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (MapItem p : cluster.getItems()) {
//                 Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = getResources().getDrawable(p.iconImage);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
//            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
//            multiDrawable.setBounds(0, 0, width, height);
//
//            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }


        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    private class LocationRenderer extends DefaultClusterRenderer<MapItem> {
        private final Context mContext;

        LocationRenderer(Context context, GoogleMap googleMap) {
            super(getApplicationContext(), googleMap, mClusterManager);
            mContext = context;

        }

        @Override
        protected void onBeforeClusterItemRendered(MapItem item, MarkerOptions markerOptions) {
            final BitmapDescriptor markerDescriptor;
//                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);

            switch (item.iconImage){
                case 1:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
                    break;
                case 2:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                    break;
                case 3:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN);
                    break;
                case 4:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                    break;
                case 5:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                    break;
                case 6:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
                    break;
                case 7:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
                    break;
                case 8:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                    break;
                case 9:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA);
                    break;
                case 10:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE);
                    break;
                default:
                    markerDescriptor =
                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

            }
            markerOptions.icon(markerDescriptor)
                    .title(item.getTitle())
                    .snippet(item.getSnippet());
        }
    }

    //--------------map class---------------------------

    private class GoogleMapArvi implements OnMapReadyCallback{

        @Override
        public void onMapReady(final GoogleMap googleMap) {
            setUpClusterer(googleMap);
//            mClusterManager.setRenderer(new OwnRendring(getApplicationContext()
//                    ,googleMap,mClusterManager));
        }
    }

    //---------------date picker-----------------
    @SuppressLint("ValidFragment")
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private DateInterface dateInterface;

        public DatePickerFragment(){
            super();
        }

        public DatePickerFragment(TrackActivity activity){
            this();
            dateInterface = activity;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

//            Log.e("date select-----", year + "-" + month + "-" + day);
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            month++;
            String monthStr = String.valueOf(month);
            String dayStr = String.valueOf(day);
            if(monthStr.length() < 2){
                monthStr = "0" + String.valueOf(month);
            }
            if(dayStr.length() < 2){
                dayStr = "0" + String.valueOf(day);
            }

            dateToTrack = year + "-" + monthStr + "-" + dayStr;
//            DateInterface dateInterface1 = new DateInterface() {
//                @Override
//                public void DateSet(String date) {
//
//                }
//            };

//            dateInterface = (DateInterface) this ;
            dateInterface.DateSet(dateToTrack);
//            DateSet(dateToTrack);
//            Log.e("month", String.valueOf(month));
            Log.e("date select", dateToTrack);
        }

        public void initialised(TrackActivity activity){
            dateInterface = activity;
            dateInterface = new TrackActivity();
            d();
        }

        void d(){

        }
    }

    //-----------------------------------------------------

    private class ConnectingToServer extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(TrackActivity.this, R.style.MyTheme);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {
            return requestServerForDataString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressDialog.dismiss();

            try {
                if (result != null && result.equals("true")) {
                    Toast.makeText(TrackActivity.this, result, Toast.LENGTH_SHORT).show();

                    //loading map
                    mapFragment.getMapAsync(new GoogleMapArvi());
                } else {
                    Toast.makeText(TrackActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception ignored){}
        }
    }

    private String requestServerForDataString() {
        try {
            URL url = new URL(TRACK_MOBILE_EMP);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.connect();

            String reqHead = "Accept:application/json";
            connection.setRequestMethod("POST");
            connection.setRequestProperty("connection","Keep-Alive"+reqHead);
            //Header header = new Header();

            @SuppressWarnings("deprecation")
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//            entity.addPart("register_type",new StringBody(id_s));

            entity.addPart("EmployeeId", new StringBody(empIdToTrack));
            entity.addPart("Date",new StringBody(dateToTrack));

            connection.addRequestProperty("content-length",entity.getContentLength()+"");
            connection.addRequestProperty(entity.getContentType().getName(),
                    entity.getContentType().getValue());

            OutputStream os = connection.getOutputStream();
            entity.writeTo(connection.getOutputStream());
            os.close();
            Log.d("HITTING","hitting url");
            connection.connect();
            Log.e("Related post", String.valueOf(connection.getResponseCode()));
            Log.e("Related", String.valueOf(HttpURLConnection.HTTP_OK));

            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK){
                return readStream(connection.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "false";
    }

    private String readStream(InputStream inputStream) {

        String response = "false";
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
        Log.e("respond form server ", response);

        try {
            initializedData(response);
            response = "true";
        } catch (Exception e) {
            e.printStackTrace();
            response = "false";
        }

        return response;
    }

    private void initializedData(String response) throws Exception {
        if(response != null && !response.equals("")){
            try {
                Log.e("releted post----", response);
                JSONArray jsonArray = new JSONArray(response);

                for(int i=0;i<jsonArray.length();i++){
                    try {
                        JSONObject c = jsonArray.getJSONObject(i);
                        String EmployeeId = c.getString("EmployeeId");             //primary key, who posted
                        String dateTime = c.getString("Edate");      // tab catagory
                        String Latitude = c.getString("Lattitude");                 //msg sub
                        String Longitude = c.getString("Longitude"); //image path
                        String Location = c.getString("Location");         //
                        String Status = c.getString("Status");      //views

                        String date = dateTime.substring(0, 10);
                        String time = dateTime.substring(11, 16);

                        trackDataList.add(new TrackData(EmployeeId, empNameToTrack, Latitude,
                                Longitude, Location, Status, date, time));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
