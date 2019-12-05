package org.ptindia.jithvar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.ptindia.jithvar.config.Config;
import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.dialog.VerifyLogin;
import org.ptindia.jithvar.handler.DataBaseHandler;

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

import static org.ptindia.jithvar.config.Config.LOGIN_URL;

/**
 * Created by Arvindo Mondal on 3/7/17.
 * Company name Jithvar
 * Email arvindo@jithvar.com
 */
public class LoginActivity extends FragmentActivity implements VerifyLogin.OtpVerifyDialog{

    private ProgressDialog mProgressDialog;
    private String otpServer = "123456";
//    private String optUser = "";
    private String phoneN = "";
    private String userId = "";
    private EditText name;
    private EditText phone;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        name =(EditText) findViewById(R.id.user_id);
        phone = (EditText) findViewById(R.id.phone_no);
        otpServer = String.valueOf(100000 + (int)(Math.random() * 800000));
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login:
                phoneN = phone.getText().toString();
                userId = name.getText().toString();
                if(!userId.equals("") && !phoneN.equals("")) {
                    new ConnectingToServer().execute();
//                    sendingOTP();
                }
                else if(!userId.equals("")){
                    Toast.makeText(this, "enter user id", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "enter phone number", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void sendingOTP(){
        new ConnectingOTP().execute(Config.sendOpt(otpServer, phoneN));
    }

    private void showOptDialog(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        VerifyLogin verifyLogin = new VerifyLogin();
        verifyLogin.setCancelable(false);
//        verifyLogin.setDialogTitle("Enter OTP");
        verifyLogin.show(fragmentManager, "opt dialog");
    }

    @Override
    public void onFinishOtpDialog(String inputText) {
        if(inputText.equals(otpServer)) {
            Toast.makeText(this, "login successful", Toast.LENGTH_SHORT).show();
            try {
                DataBaseApp db = new DataBaseApp(LoginActivity.this);
                db.insertRegistrationTB(new DataBaseHandler(userId, "true"));
                db.insertContactTB(new DataBaseHandler(userId, phoneN, 1));
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Intent i=new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }
        else{
            Toast.makeText(this, "incorrect otp", Toast.LENGTH_SHORT).show();
        }
    }

    //-------------------------

    private class ConnectingToServer extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(LoginActivity.this,R.style.MyTheme);
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
                if (result != null && result.equals("Success")) {
//                    Toast.makeText( LoginActivity.this, "success", Toast.LENGTH_SHORT).show();
//                    showOptDialog();

                    sendingOTP();
//                    Intent i=new Intent(LoginActivity.this, LoginActivity.class);
//                    startActivity(i);
//                    finish();
                } else {
                    Toast.makeText( LoginActivity.this, "not valid user", Toast.LENGTH_SHORT).show();
                }

                Log.e("DownloadTextTask", result);
            }
            catch (Exception ignored){}
        }
    }

    private String requestServerForDataString() {
        try {
            URL url = new URL(LOGIN_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.connect();

            String reqHead = "Accept:application/json";
            connection.setRequestMethod("POST");
            connection.setRequestProperty("connection","Keep-Alive"+reqHead);
            @SuppressWarnings("deprecation")
            MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

            entity.addPart("EmployeeId",new StringBody(userId));
            entity.addPart("Mobile",new StringBody(phoneN));

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

//        try {
//            response = initializedData(response);
//        } catch (Exception e) {
//            e.printStackTrace();
//            response = "false";
//        }

        return response;
    }

    private String initializedData(String response) throws Exception {
        if(response != null && !response.equals("")){
            try {
                JSONObject c = new JSONObject(response);
                final String status = c.getString("status");

                if(status.equals("Success")){
                    final String userId = c.getString("data");
                    response = "true";
                }
                else{
                    response = "false";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                response = "false";
            }
        }
        return response;
    }

    private class ConnectingOTP extends AsyncTask<String, Integer, String>{

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(LoginActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            progressDialog.setMessage("Loading request...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
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
