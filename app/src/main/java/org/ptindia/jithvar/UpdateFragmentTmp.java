package org.ptindia.jithvar;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.json.JSONException;
import org.json.JSONObject;
import org.ptindia.jithvar.database.DataBaseApp;
import org.ptindia.jithvar.spinner.SpinnerBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static org.ptindia.jithvar.config.Config.PROFILE_UPDATE_FINAL;
import static org.ptindia.jithvar.config.Config.PROFILE_UPDATE_INITIAL;


/**
 *
 */
public class UpdateFragmentTmp extends Fragment {

    private ProgressDialog mProgressDialog;
    private SharedPreferences prefrences;
    private JSONObject jsonobject;
    private ImageView imageView;
    private Uri file;
    private String CITY="",BLOCK="",PANCHAYAT="",VILLAGE="",SCHOOL="",imagepath;
    private RadioGroup radioSexGroup;
    private RadioButton radioSexButton;
    private int pos;

    private String result = "",status,EMail,path;
    private SearchableSpinner city,block,villg,nyaypanchayat,school;
    private ArrayList<SpinnerBinding> bind;

    private ArrayList<String> City =new ArrayList();
    private ArrayList<String> Villg=new ArrayList();
    private ArrayList<String> Block=new ArrayList();
    private ArrayList<String> School =new ArrayList();
    private ArrayList<String> Panchayat=new ArrayList();
    private JSONObject jArray = null;
    private Button selectImage,reg;
    private InputStream is=null;
    private DatePickerDialog datePickerDialog;
    private EditText fname,lname,email,contact,desig,address1,address2,middlename,pin;
    private String firstName,lastName="",Contact,Street,add1,add2,pincode,Village,
            District,State,Pin,Qualification,Profession,Designation,Experience,
            Organization,Orgplace,day,middlname="";
    private TextView dob;
    //Image request code
    private int PICK_IMAGE_REQUEST = 1;
    //storage permission code
    private static final int STORAGE_PERMISSION_CODE = 123;
    //Bitmap to get image from gallery
    private Bitmap bitmap,btmp;
    //Uri to store the image uri
    private Uri filePath=null;
    private View rootview;
    private SharedPreferences preferences;

    private DataBaseApp db;
    private String userId;

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
         rootview=inflater.inflate(R.layout.fragment_update, container, false);
        preferences=getActivity().getSharedPreferences("mypref",0);
        contact =(EditText) rootview.findViewById(R.id.contact);
        imageView=(ImageView)rootview.findViewById(R.id.img);
        selectImage=(Button)rootview.findViewById(R.id.buttn_image);
        middlename=(EditText)rootview.findViewById(R.id.middlename);
        Contact = contact.getText().toString().trim();
        address1=(EditText) rootview.findViewById(R.id.address);
        fname=(EditText) rootview.findViewById(R.id.firstname);
        lname =(EditText) rootview.findViewById(R.id.lastname);
        pin=(EditText)rootview.findViewById(R.id.pincode);
        email =(EditText) rootview.findViewById(R.id.email);
        reg=(Button) rootview.findViewById(R.id.register);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });
        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        new fillJSON().execute();

        return rootview;
    }
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            filePath = data.getData();
           try {
                bitmap =MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                btmp=modifyOrientation(bitmap,getPath(filePath));
                ByteArrayOutputStream stream= new ByteArrayOutputStream();
                btmp.compress(Bitmap.CompressFormat.JPEG,40,stream);
                try {
                    File imageFile = getOutputMediaFile();
                    imageFile.createNewFile();
                    FileOutputStream fout= new FileOutputStream(imageFile);
                    fout.write(stream.toByteArray());
                    fout.close();
                  file=Uri.fromFile(imageFile);
                   // imageView.setImageURI(file);
                }catch (IOException e){}


               imageView.setImageBitmap(btmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Jithvar");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }
    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path)
            throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    public String getPath(Uri uri) {
        String path = "", document_id = "";
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();
        }

        cursor = getActivity().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        if (cursor != null) {
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        }

        return path;
    }

    public void register() {
        middlname=middlename.getText().toString().trim();
        firstName = fname.getText().toString().trim();
        add1=address1.getText().toString().trim();
        lastName = lname.getText().toString().trim();
        pincode=pin.getText().toString().trim();
        EMail = email.getText().toString().trim();
        Contact = contact.getText().toString().trim();
        if (firstName.equals("") ||  Contact.equals("")) {
            if(firstName.equals("")){

                fname.setError("* First Name required");
            }

            if(add1.equals("")){
               // TextView aderror=(TextView)rootview.findViewById(R.id.address_error);
                //aderror.setVisibility(View.VISIBLE);
                address1.setError("* Address required");
            }
            //if(EMail.equals("")){
               // TextView ferror=(TextView)rootview.findViewById(R.id.email_error);
                //ferror.setVisibility(View.VISIBLE);
              //  email.setError("* Email required");
            //}
            if(Contact.equals("")){
               // TextView ferror=(TextView)rootview.findViewById(R.id.contact_error);
                //ferror.setVisibility(View.VISIBLE);
                contact.setError("* Contact required");
            }

            if(pincode.equals("")||pincode.length()!=6){
               // TextView ferror=(TextView)rootview.findViewById(R.id.dob_error);
                //ferror.setVisibility(View.VISIBLE);
                pin.setError("* Pincode required");

            }

        }

        else {

          //  new DownloadJSON().execute();
            if(filePath!=null){
                uploadMultipart();
            }
            else{
                new DownloadJSON().execute();
            }
        }

    }

    public void uploadMultipart() {

        try {
            String uploadId = UUID.randomUUID().toString();
            Log.e("upload","http://demo.jithvar.com/sr/image/update-employee");
            //?TeacherId="+preferences.getString("EmployeeId",""));
       //    String path=getPath(file);
            final String success=  new MultipartUploadRequest(getActivity(),
                    uploadId, PROFILE_UPDATE_FINAL)
                    .addFileToUpload(file.getPath(), "fileImage")
                    .addParameter("TeacherId", userId)
                    .addParameter("FirstName",firstName)
                    .addParameter("MiddleName", middlname)
                    .addParameter("LastName",lastName)
                    .addParameter("Address",add1)
                    .addParameter("Mobile",Contact)
                    .addParameter("Email",EMail)
                    .addParameter("PinCode",pincode)
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2).
                            setDelegate(new UploadStatusDelegate() {
                                @Override
                                public void onProgress(Context context, UploadInfo uploadInfo) {
                                    //       Log.e("url",Constants.UPLOAD_URL+
                                    // "LAT"+Double.toString(lat)+"LONG"+Double.toString(longt)
                                    // +"contact"+preferences.getString("contact","")+
                                    //    "EmployeeId"+"1");
                                }

                                @Override
                                public void onError(Context context, UploadInfo uploadInfo,
                                                    Exception exception) {
                                    // your code here
                                    Log.e("onError",exception.getMessage());
                                    Log.e("uploadInfo",uploadInfo.toString());
                                }

                                @Override
                                public void onCompleted(Context context, UploadInfo uploadInfo,
                                                        ServerResponse serverResponse) {
                                    // your code here
                                    // if you have mapped your server response to a POJO, you can easily get it:
                                    // YourClass obj = new Gson().fromJson(serverResponse.getBodyAsString(), YourClass.class);
                                    getActivity().getFragmentManager().beginTransaction().
                                    replace(R.id.container,new ProfileFragment()).commit();
                                }
                                @Override
                                public void onCancelled(Context context, UploadInfo uploadInfo) {
                                    // your code here
                                }
                            }).startUpload(); //Starting the upload


        } catch (Exception exc) {
            Toast.makeText(getActivity(), exc.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("error",exc.getMessage());
        }
    }
    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a progressdialog
            mProgressDialog = new ProgressDialog(getActivity(),R.style.MyTheme);
            // Set progressdialog title
            // Set progressdialog messag
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            // Show progressdialog
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Create an array
            // Retrieve JSON Objects from the given URL address
            //http://www.androidbegin.com/tutorial/jsonparsetutorial.txt
            try {
                String url = PROFILE_UPDATE_FINAL;
                OkHttpClient client = new OkHttpClient();
                RequestBody formBody = new FormBody.Builder()
                        .add("TeacherId", userId)
                        .add("FirstName",firstName)
                        .add("MiddleName", middlname)
                        .add("LastName",lastName)
                        .add("Address",add1)
                        .add("Mobile",Contact)
                        .add("Email",EMail)
                        .add("PinCode",pincode)
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .post(formBody)
                        .build();
                Response response = client.newCall(request).execute();

                result=response.body().string();

            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            mProgressDialog.dismiss();
            Log.e("response",result);
            try{
                JSONObject jsonObject= new JSONObject(result);
                if(jsonObject.getString("status").equals("Success")){
                    getActivity().getFragmentManager().beginTransaction().replace(R.id.container,new ProfileFragment()).addToBackStack("").commit();
                }


            }catch (JSONException e){}
    }}
    private class fillJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity(),R.style.MyTheme);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String url= PROFILE_UPDATE_INITIAL + userId;

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
              JSONObject  jsonObject = new JSONObject(result);
                fname.setText(jsonObject.getString("FirstName"));
                if(!jsonObject.getString("MiddleName").equals("")){
                    middlename.setText(jsonObject.getString("MiddleName"));}
                if(!jsonObject.getString("LastName").equals(""))
                { lname.setText(jsonObject.getString("LastName"));}
                email.setText(jsonObject.getString("Email"));
                contact.setText(jsonObject.getString("Mobile"));
                address1.setText(jsonObject.getString("Address"));
                if(!jsonObject.getString("PinCode").equals(""))
                { pin.setText(jsonObject.getString("PinCode"));}


                StrictMode.ThreadPolicy policy = new
                        StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                Picasso.with(getActivity())
                        .load(imagepath + userId)
                        .placeholder(R.drawable.no_image) //
//                    .error(R.drawable.face) //
                        .fit() //
//                    .tag(holder) //
                        .into(imageView);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //-----------------------------------------------




}
