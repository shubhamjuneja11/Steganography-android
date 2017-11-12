package net.simplifiedcoding.volleyupload;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener  {

    private Button buttonChoose;
    private Button buttonUpload;

    private ImageView imageView;

    private EditText editTextName,editpass;

    private Bitmap bitmap;
    String mImageName;
    private int PICK_IMAGE_REQUEST = 1;

    private String UPLOAD_URL =Urls.getURL()+"/upload";

    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    private String IMAGE_TYPE="type",PASS="password";
    String type,imgtype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        editTextName = (EditText) findViewById(R.id.editText);
        editpass=(EditText)findViewById(R.id.password);
        imageView  = (ImageView) findViewById(R.id.imageView);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
    }

    public String getStringImage(Bitmap bmp){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encodedImage;
    }

    private void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        Log.e("abcde",s);
                        try {
                            JSONObject object=new JSONObject(s);
                            byte[] imageBytes=Base64.decode(object.getString("image"),0);
                            mImageName=object.getString("name");
                            saveme(imageBytes,mImageName);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        //byte[] imageBytes=Base64.decode(s,0);
                       Log.e("abc","2");
                           // saveme(imageBytes);



                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        try {
                            Toast.makeText(MainActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);

                //Getting Image Name
                String name = editTextName.getText().toString().trim();
                String password=editpass.getText().toString().trim();
                //Creating parameters
                Map<String,String> params = new Hashtable<>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(PASS,password);
                params.put(KEY_NAME,name);
                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                type=getContentResolver().getType(filePath);
                imageView.setImageBitmap(bitmap);
                imgtype=queryName(filePath);
               // Toast.makeText(this, queryName(filePath), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }



        }
    }
    private String queryName(Uri uri ) {

        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);
    /*
     * Get the column indexes of the data in the Cursor,
     * move to the first row in the Cursor, get the data,
     * and display it.
     */
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();

       String s=returnCursor.getString(nameIndex);
        returnCursor.close();
        try {
            String a[]= s.split("\\.");
            s=a[a.length-1];
           // Toast.makeText(this, imgtype, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return s;
    }

    @Override
    public void onClick(View v) {

        if(v == buttonChoose){
            showFileChooser();
        }

        if(v == buttonUpload){
            uploadImage();
        }
    }

    private void saveme(byte [] bytes,String name){
        File f=getOutputMediaFile();
        if (f == null) {
            Log.d("abcd",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bytes);
            fos.close();
            Toast.makeText(MainActivity.this, "File saved as "+name, Toast.LENGTH_SHORT).show();

        }
        catch (Exception e){

        }
    }
    private  File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + "Steganograhy"
                + "/Files");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.e("abcd","shit");
                return null;
            }
            Log.e("abcd","fgh");
        }
        Log.e("abcd","frfdf");
        // Create a media file name
        //String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
         //mImageName="MI_"+ timeStamp +".jpg";
        //mImageName="output.jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        Log.e("abc",mediaFile.getAbsolutePath());
        return mediaFile;
    }


}
