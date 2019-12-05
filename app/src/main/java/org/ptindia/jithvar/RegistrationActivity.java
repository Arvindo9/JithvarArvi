package org.ptindia.jithvar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ptindia.jithvar.config.Config;
import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.dialog.VerifyLogin;
import org.ptindia.jithvar.handler.DataBaseHandler;
import org.ptindia.jithvar.spinner.SpinnerBinding;
import org.ptindia.jithvar.webservice.WebServiceHandler;
import org.ptindia.jithvar.webservice.WebServiceListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.ptindia.jithvar.config.Config.REGISTRATION;

public class RegistrationActivity extends AppCompatActivity implements VerifyLogin.OtpVerifyDialog{

    private ProgressDialog mProgressDialog;
    private String CITY="",BLOCK="",PANCHAYAT="",VILLAGE="",SCHOOL="";
    private RadioGroup radioSexGroup;
    private int pos;
    private String EMail;
    private SearchableSpinner city,block,villg,nyaypanchayat,school;
    private ArrayList<String>Idcity=new ArrayList<>();
    private ArrayList<String>Idblock=new ArrayList<>();
    private ArrayList<String>Idpanchayat=new ArrayList<>();
    private ArrayList<String>Idvillage=new ArrayList<>();
    private ArrayList<String>Idschool=new ArrayList<>();
    private ArrayList<String> City =new ArrayList();
    private ArrayList<String> Villg=new ArrayList();
    private ArrayList<String> Block=new ArrayList();
    private ArrayList<String> School =new ArrayList();
    private ArrayList<String> Panchayat=new ArrayList();
    private DatePickerDialog datePickerDialog;
    private EditText fname,lname,email,contact,desig,address1,address2,middlename;
    private String firstName,lastName,Contact,add1;
    private String add2;
    private String day;
    private String middlname="";
    private TextView dob;
    private DataBaseApp db;
    private String otpServer = "123456";
    private  String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        otpServer = String.valueOf(100000 + (int)(Math.random() * 800000));

        loadingData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        db = new DataBaseApp(RegistrationActivity.this);
    }

    private void loadingData(){
        SpinnerBinding spincity= new SpinnerBinding();
        spincity.setId("0");
        radioSexGroup = (RadioGroup) findViewById(R.id.radioSex);
        Idcity.add(0,"1");
        Idblock.add(0,"1");
        Idpanchayat.add(0,"1");
        Idvillage.add(0,"1");
        Idschool.add(0,"1");
        city=(SearchableSpinner)findViewById(R.id.city);
        city.setTitle("");
        block=(SearchableSpinner)findViewById(R.id.block);
        block.setTitle("");
        villg=(SearchableSpinner)findViewById(R.id.village);
        villg.setTitle("");
        school=(SearchableSpinner)findViewById(R.id.school) ;
        school.setTitle("");
        nyaypanchayat=(SearchableSpinner)findViewById(R.id.nyaypanchayat);
        nyaypanchayat.setTitle("");
        city.setTitle("Select City");
        City.add("Select City");
        Block.add("Select Block");
        Villg.add("Select Village");
        Panchayat.add("Select Panchayat");
        School.add("Select School");
        middlename=(EditText)findViewById(R.id.middlename);
        getLocationFromServer(1,city,City,0,Idcity);

        contact =(EditText)findViewById(R.id.contact);
        Contact = contact.getText().toString().trim();
        dob = (TextView) findViewById(R.id.dob);
        // perform click event on edit text
        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(RegistrationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                dob.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()-568036800000l);
                datePickerDialog.show();
            }
        });

        address1=(EditText)findViewById(R.id.address1);
        address2=(EditText)findViewById(R.id.address2);
        fname=(EditText)findViewById(R.id.firstname);
        lname =(EditText)findViewById(R.id.lastname);
        email =(EditText)findViewById(R.id.email);
        day=dob.getText().toString().trim();
        desig =(EditText)findViewById(R.id.designation);

        city.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                //  pos = arg2;
                pos=   city.getSelectedItemPosition();
                Log.e("position",Integer.toString(pos));
                if (pos > 0) {
                    //  HashMap<String, String> resultp = new HashMap<String, String>();
                    //  resultp=bind.get(pos);
                    CITY=Idcity.get(pos);
                    // CITY=bind.get(pos).getId();
                    Log.e("cityid",CITY);
                    //   SpinnerBinding spinblock= new SpinnerBinding();
                    // spinblock.setId("0");
                    getLocationFromServer(2,block,Block,Integer.parseInt(CITY),Idblock);
                    chooceblock();
                }
            }
            public void chooceblock(){
                block.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        pos = position;
                        if (pos > 0) {
                            BLOCK=Idblock.get(pos);
                            //  SpinnerBinding spinpanchayat= new SpinnerBinding();
                            //   spinpanchayat.setId("0");
                            getLocationFromServer(3,nyaypanchayat,Panchayat,Integer.parseInt(BLOCK),
                                    Idpanchayat);
                            choocePanchayat();
                        }}
                    public void choocePanchayat(){
                        nyaypanchayat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position,
                                                       long id) {
                                pos = position;
                                if (pos > 0) {
                                    PANCHAYAT=Idpanchayat.get(pos);
                                    //  PANCHAYAT=bind.get(position).getId();
                                    //   SpinnerBinding spinvill= new SpinnerBinding();
                                    //    spinvill.setId("0");
                                    getLocationFromServer(4,villg,Villg,Integer.parseInt(PANCHAYAT),
                                            Idvillage);
                                    choocevillage();
                                }
                            }
                            public void choocevillage(){
                                villg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view,
                                                               int position, long id) {
                                        pos = position;
                                        if (pos > 0) {
                                            VILLAGE=Idvillage.get(pos);
                                            //  VILLAGE=bind.get(position).getId();
                                            //    SpinnerBinding spinschool= new SpinnerBinding();
                                            //      spinschool.setId("0");
                                            getLocationFromServer(5,school,School,
                                                    Integer.parseInt(VILLAGE),Idschool);
                                            chooceschool();
                                        }
                                    }
                                    public void chooceschool(){
                                        school.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent,
                                                                       View view, int position, long id) {
                                                pos = position;
                                                if (pos > 0) {
                                                    SCHOOL=Idschool.get(pos);
                                                    // SCHOOL=bind.get(position).getId();
                                                    Log.e("schoolid",SCHOOL);
                                                    //  getLocationFromServer(5,school,School,Integer.parseInt(VILLAGE));
                                                    // chooceschool();
                                                }
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        });

                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }});
    }

    private void getLocationFromServer(int flag, final Spinner spinner, ArrayList<String> list,
                                       int id, ArrayList<String>ID) {
        final ArrayList<String> strings = list;
        final ArrayList<String>IDlist = ID;
        // final ArrayList<HashMap<String, String>> strings=list;
        strings.subList(1,strings.size()).clear();
        IDlist.subList(1,IDlist.size()).clear();
        //  final SpinnerBinding spinbind=spinbnd;
        //if(bind!=null){ bind.subList(1,bind.size()).clear();}
        //   strings.clear();

        WebServiceHandler serviceHandler = new WebServiceHandler(RegistrationActivity.this);
        serviceHandler.webServiceListener = new WebServiceListener() {
            @Override
            public void onDataReceived(String response) {
                Log.e("Web Responce", response);
                if (response != null) {
                    try {
                        // bind=new ArrayList<SpinnerBinding>();
                        JSONArray jsnarray= new JSONArray(response);
                        //  SpinnerBinding spinbind=new SpinnerBinding();
                        //       spinbind.setId(Integer.toString(0));
                        //     bind.add(spinbind);
                        for(int i=0;i<jsnarray.length();i++){
                            JSONObject jsnobj=jsnarray.getJSONObject(i);
                            Iterator<String> keys=jsnobj.keys();
                            //  SpinnerBinding spinbind=new SpinnerBinding();
                            //  spinbind.setId(Integer.toString(0));

                            //  bind.add(spinbind);

                            while (keys.hasNext()) {
                                String name=keys.next();
                                IDlist.add(name);
                                strings.add(jsnobj.getString(name));
                            }
                        }
                        spinner.setAdapter(new ArrayAdapter<String>(RegistrationActivity.this,
                                android.R.layout.simple_dropdown_item_1line, strings));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        try {
            if (flag==1) {
                serviceHandler.getcityAPI();
            }if(flag==2) {
                serviceHandler.getblockAPI(id);
            }if(flag==3) {
                serviceHandler.getpanchayatAPI(id);
            }if(flag==4) {
                serviceHandler.getvillageAPI(id);
            }if(flag==5) {
                serviceHandler.getschoolAPI(id);
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.register:
                register();
            break;
        }
    }

    public void register() {
        middlname=middlename.getText().toString().trim();
        firstName = fname.getText().toString().trim();
        add1=address1.getText().toString().trim();
        add2=address2.getText().toString().trim();
        lastName = lname.getText().toString().trim();
        EMail = email.getText().toString().trim();
        Contact = contact.getText().toString().trim();
        day=dob.getText().toString().trim();
        String designation = desig.getText().toString().trim();
        if (CITY.equals("")||BLOCK.equals("")||add1.equals("")||PANCHAYAT.equals("")||
                VILLAGE.equals("")||firstName.equals("")|| Contact.length()!=10) {
            if(firstName.equals("")){
                TextView ferror=(TextView)findViewById(R.id.fname_error);
                ferror.setVisibility(View.VISIBLE);
            }

            if(add1.equals("")){
                TextView aderror=(TextView)findViewById(R.id.address_error);
                aderror.setVisibility(View.VISIBLE);
            }

            if(Contact.length()!=10){
                TextView ferror=(TextView)findViewById(R.id.contact_error);
                ferror.setVisibility(View.VISIBLE);
            }

            if(day.equals("")){
                TextView ferror=(TextView)findViewById(R.id.dob_error);
                ferror.setVisibility(View.VISIBLE);
            }
        }
        else {
            int selectedId = radioSexGroup.getCheckedRadioButtonId();
            // find the radiobutton by returned id
            RadioButton radioSexButton = (RadioButton) findViewById(selectedId);
            new ConnectingToServer().execute("");
        }

    }

    private class ConnectingToServer extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(RegistrationActivity.this,R.style.MyTheme);
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
                    Toast.makeText( RegistrationActivity.this, "success", Toast.LENGTH_SHORT).show();
                    sendingOTP();
//                    Intent i=new Intent(RegistrationActivity.this, HomeActivity.class);
//                    startActivity(i);
//                    finish();
                } else {
                    Toast.makeText( RegistrationActivity.this, "Try Again", Toast.LENGTH_SHORT).show();
                }

                Log.e("DownloadTextTask", result);
            }
            catch (Exception ignored){}
        }
    }

    private String requestServerForDataString() {
        try {
            URL url = new URL(REGISTRATION);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.connect();

            String reqHead = "Accept:application/json";
            connection.setRequestMethod("POST");
            connection.setRequestProperty("connection","Keep-Alive"+reqHead);
            @SuppressWarnings("deprecation")
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            entity.addPart("FirstName",new StringBody(firstName));
            entity.addPart("MiddleName",new StringBody(middlname));
            entity.addPart("LastName",new StringBody(lastName));
            entity.addPart("Address",new StringBody(add1));
            entity.addPart("Mobile",new StringBody(Contact));
            entity.addPart("Email",new StringBody(EMail));
            entity.addPart("CityId",new StringBody(CITY));
            entity.addPart("BlockId",new StringBody(BLOCK));
            entity.addPart("PanchayatId",new StringBody(PANCHAYAT));
            entity.addPart("VillageId",new StringBody(VILLAGE));
            entity.addPart("SchoolId",new StringBody(SCHOOL));
            entity.addPart("DOB",new StringBody(day));

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
//                JSONArray jsonArray = new JSONArray(response);

//                for(int i=0;i<jsonArray.length();i++){
//                    JSONObject c = jsonArray.getJSONObject(i);
                JSONObject c = new JSONObject(response);
                final String status = c.getString("status");

                if(status.equals("Success")){
                    response = "true";
                     userID = c.getString("data");
//                    try {
//                        db.insertRegistrationTB(new DataBaseHandler(userId, "true"));
//                        db.insertContactTB(new DataBaseHandler(userId, Contact, 1));
//                        response = "true";
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                        response = "false";
//                    }
                }
                else{
                    response = "false";
                }
//                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    //---------------otp
    private void sendingOTP(){
        new ConnectingOTP().execute(Config.sendOpt(otpServer, Contact));
    }

    private void showOptDialog(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        VerifyLogin verifyLogin = new VerifyLogin();
        verifyLogin.setCancelable(false);
        verifyLogin.setDialogTitle("Enter OTP");
        verifyLogin.show(fragmentManager, "opt dialog");
    }

    @Override
    public void onFinishOtpDialog(String inputText) {
        if(inputText.equals(otpServer)) {
            Toast.makeText(this, "login successful", Toast.LENGTH_SHORT).show();
            try {
                db.insertRegistrationTB(new DataBaseHandler(userID, "true"));
                db.insertContactTB(new DataBaseHandler(userID, Contact, 1));
            } catch (SQLException e) {
                e.printStackTrace();
            }

            Intent i=new Intent(RegistrationActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }
        else{
            Toast.makeText(this, "incorrect otp", Toast.LENGTH_SHORT).show();
        }
    }

    private class ConnectingOTP extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            showOptDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try {
                Log.e("hit Url",params[0]);
                response=run(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        private String run(String url) throws IOException {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }
    }

}
