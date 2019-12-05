package org.ptindia.jithvar.webservice;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.ptindia.jithvar.config.Config.EMPLOYEE_DETAILS;

/**
 * Author       :   Arvindo Mondal
 * Designation  :   Android Developer
 * E-mail       :   arvindomondal@gmail.com
 * Company      :   Jithvar
 * Purpose      :   Web Service Handle
 */

public class WebServiceHandler extends AsyncTask<String, Void, String> {
    private Context context;
    private ProgressDialog progressDialog;
    private String IPADDRESS;
    public WebServiceListener webServiceListener;

    public WebServiceHandler(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context, AlertDialog.THEME_HOLO_LIGHT);
        progressDialog.setMessage("Loading request...");
        progressDialog.setCancelable(false);
        progressDialog.show();
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
    @Override
    protected void onPostExecute(String response) {
        progressDialog.dismiss();
        webServiceListener.onDataReceived(response);

    }


    public void doGetOtp(String message, String contact) throws ExecutionException, InterruptedException {
        try {
            String URL = "http://msg.ptindia.org/rest/services/sendSMS/sendGroupSms?AUTH_KEY=142d53a576f75148d1f44184c4e9a71" +
                    "&message=" + message + "&senderId=JITHVR" + "&routeId=1" + "&mobileNos=" +
                    contact + "&smsContentType=english";
            execute(URL);
        }
        catch (UnsupportedCharsetException e){}
    }

    public void getCategory()throws ExecutionException, InterruptedException{
        String URL = "http://demo.jithvar.com/sr/image/fetchpost";
        execute(URL);
    }
    public  void inbox()throws ExecutionException, InterruptedException{
        String URL = "http://demo.jithvar.com/sr/image/fetchcity";
        execute(URL);
    }
    public void sendMessage(String id,String subject,String message)throws ExecutionException, InterruptedException{
        String URL = "http://demo.jithvar.com/sr/image/fetchcity";
        execute(URL);
    }
    public void getcityAPI() throws ExecutionException, InterruptedException {
        String URL = "http://demo.jithvar.com/sr/image/fetchcity";
        execute(URL);
    } public void getblockAPI(int id) throws ExecutionException, InterruptedException {
        String URL = "http://demo.jithvar.com/sr/image/fetchblock?CityId="+Integer.toString(id);
        execute(URL);
    }
    public void getpanchayatAPI(int id) throws ExecutionException, InterruptedException {
        String URL = "http://demo.jithvar.com/sr/image/fetchpanchayat?BlockId="+Integer.toString(id);
        execute(URL);
    }
    public void getvillageAPI(int id) throws ExecutionException, InterruptedException {
        String URL = "http://demo.jithvar.com/sr/image/fetchvillage?PanchayatId="+Integer.toString(id);
        execute(URL);
    }
    public void getschoolAPI(int id) throws ExecutionException, InterruptedException {
        String URL = "http://demo.jithvar.com/sr/image/fetchschool?VillageId="+Integer.toString(id);
        execute(URL);
    }


    public void checkout(String id)throws ExecutionException, InterruptedException {
        String URL = "http://"+IPADDRESS+"/PhpProject1/umcs.php?id="+id;
        execute(URL);
    }

    public void getEmpDetails()throws ExecutionException, InterruptedException {
        execute(EMPLOYEE_DETAILS);
    }

}