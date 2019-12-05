package org.ptindia.jithvar;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.handler.DataBaseHandler;
import org.ptindia.jithvar.image.ImageLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.ptindia.jithvar.config.Config.PROFILEPIC;
import static org.ptindia.jithvar.config.Config.PROFILE_SEND_DATA;
import static org.ptindia.jithvar.config.Config.REGISTRATION;


public class ProfileFragment extends Fragment {
    private ProgressDialog mProgressDialog;
    private JSONObject jsonObject;
    private String imagepath="",result="",firstname,lastname,email,
            contact, village,address,city,school,block,panchayat,
            designation,dob,middlnam;
    private InputStream is=null;
    private TextView txtname,txtcontact,txtemail,txtadd,txtadd1,txtcity,txtblock,txtdesg,
            txtpanch,txtvill,txtsch,txtdob;
    private ImageView image;
//    private ImageLoader imageloader;
//    private SharedPreferences preferences;
    private DataBaseApp db;
    private String userId;
    private ImageLoader imageloader;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview=inflater.inflate(R.layout.fragment_profile, container, false);
//        imageloader=new ImageLoader(getActivity());
//        preferences= getActivity().getSharedPreferences("mypref",0);
        txtname=(TextView)rootview. findViewById(R.id.name);
      //  txtfatnm=(TextView)rootview.findViewById(R.id.fatnm_det) ;
        txtdob=(TextView)rootview.findViewById(R.id.dob_det);
        txtcontact=(TextView) rootview.findViewById(R.id.contact_det);
        txtemail=(TextView)rootview.findViewById(R.id.email_det);
        txtadd=(TextView)rootview.findViewById(R.id.address);
        txtadd1=(TextView)rootview.findViewById(R.id.address1);
        txtcity=(TextView)rootview.findViewById(R.id.city);
        txtblock=(TextView)rootview.findViewById(R.id.block);
        txtdesg=(TextView)rootview.findViewById(R.id.designation);
        txtpanch=(TextView)rootview.findViewById(R.id.panchayat);
        txtvill=(TextView)rootview.findViewById(R.id.village);
        txtsch=(TextView)rootview.findViewById(R.id.school);
        image=(ImageView)rootview.findViewById(R.id.image_det);
        imageloader=new ImageLoader(getActivity());
        // if(contact_search!=null){

        new downloadJSON().execute();

//        new ConnectingToServer().execute("");

        return rootview;
        // }
    }

    private class ConnectingToServer extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity(), R.style.MyTheme);
            mProgressDialog.setIndeterminate(false);
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
                    Toast.makeText( getActivity(), "success", Toast.LENGTH_SHORT).show();
                    Intent i=new Intent(getActivity(),HomeActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText( getActivity(), "Try Again", Toast.LENGTH_SHORT).show();
                }

                Log.e("DownloadTextTask", result);
            }
            catch (Exception ignored){}
        }
    }

    private String requestServerForDataString() {
        try {
            URL url = new URL(PROFILE_SEND_DATA + userId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.connect();

            String reqHead = "Accept:application/json";
            connection.setRequestMethod("POST");
            connection.setRequestProperty("connection","Keep-Alive"+reqHead);
            @SuppressWarnings("deprecation")
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            entity.addPart("FirstName",new StringBody(userId));

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
        return "fails";
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

        try {
            response = initializedData(response);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private String initializedData(String response) throws Exception {
        if(response != null && !response.equals("")){
            try {
                Log.e("sdf", response);
                JSONArray jsonArray = new JSONArray(response);

                for(int i=0;i<jsonArray.length();i++){
                    JSONObject c = jsonArray.getJSONObject(i);
                    final String status = c.getString("status");
                    final String userId = c.getString("data");

                    if(status.equals("success")){
                        try {
                            db.insertRegistrationTB(new DataBaseHandler(userId, "true"));
                            response = "true";
                        } catch (SQLException e) {
                            e.printStackTrace();
                            response = "false";
                        }
                    }
                    else{
                        response = "false";
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    private class downloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(getActivity(),R.style.MyTheme);
            // Set progressdialog title
            // Set progressdialog messag
            // mProgressDialog.setIndeterminate(false);
            // Show progressdialog
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Create an array
            // Retrieve JSON Objects from the given URL address
            //http://www.androidbegin.com/tutorial/jsonparsetutorial.txt
            try {
//                String url="http://demo.jithvar.com/sr/image/display-employee?TeacherId="+
//                        preferences.getString("EmployeeId","");

                String url = PROFILE_SEND_DATA + userId;

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                // return response.body().string();
                result=response.body().string();
                Log.e("json",result);
            }   catch (Exception e) {
                Log.e("log_tag", "Error in http connection " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            try {
                jsonObject = new JSONObject(result);

//                firstname =jsonObject.getString("TeacherId");
//                firstname =jsonObject.getString("status");

                firstname =jsonObject.getString("FirstName");
                lastname =jsonObject.getString("LastName");
                middlnam=jsonObject.getString("MiddleName");
                email =jsonObject.getString("Email");
                contact =jsonObject.getString("Mobile");
                dob=jsonObject.getString("DOB");
                village =jsonObject.getString("VillageName");
                address =jsonObject.getString("Address");
                city =jsonObject.getString("CityName");
                block=jsonObject.getString("BlockName");
                school =jsonObject.getString("SchoolName");
                panchayat =jsonObject.getString("PanchayatName");

                if(jsonObject.getString("EmployeePic")!=null) {
                    imagepath = PROFILEPIC + jsonObject.getString("EmployeePic");
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            Log.e("imgpath",imagepath);
//            imageloader.DisplayImage(imagepath,image);

            try {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//
//            try {
//                Picasso.with(getActivity())
//                        .load(imagepath)
//                        .placeholder(R.drawable.no_image) //
////                    .error(R.drawable.face) //
//                        .fit() //
////                    .tag(holder) //
//                        .into(image);


            imageloader.DisplayImage(imagepath,image);

            if(middlnam.equals("")){
            txtname.setText(firstname+" "+lastname);}
            else {
                txtname.setText(firstname+" "+middlnam+" "+lastname);
            }
          //txtfatnm.setText("Father's Name : "+fathnam);
            txtcontact.setText(contact);
            txtdob.setText(dob);
            txtemail.setText(email);
            txtadd.setText(address);
            txtsch.setText(school);
            txtcity.setText(city);
            txtdesg.setText(designation);
            txtblock.setText(block);
            txtpanch.setText(panchayat);
            txtvill.setText(village);
            }
            catch (Exception e){

            }
        }
    }



}




