package net.simplifiedcoding.volleyupload;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private EditText editTextName;

    private Bitmap bitmap;

    private int PICK_IMAGE_REQUEST = 1;

    private String UPLOAD_URL =" http://192.168.0.105:5000/upload";

    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        editTextName = (EditText) findViewById(R.id.editText);

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
                        Log.e("abc","1");
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        byte[] imageBytes=Base64.decode(s,0);
                       Log.e("abc","2");


                            saveme(imageBytes);



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

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, name);

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
            } catch (IOException e) {
                e.printStackTrace();
            }



        }
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
    private void SaveImage(byte[] bytes) {
Log.e("fgf","1");
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "outputz-"+ n +".jpg";
        File file = new File (myDir, fname);
        Log.e("fgf",file.getAbsolutePath());
        if (file.exists ())
            file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(bytes);
            out.close();
            File f=new File(fname);
            Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "Image saved as output.jpg", Toast.LENGTH_SHORT).show();
            finish();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void save(byte [] bytes){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");
        Log.e("abc",mypath.getAbsolutePath());
        //File file=new File(mypath);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mypath);
            out.write(bytes);
            out.close();
            File f=new File(mypath.getAbsolutePath());
            Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "Image saved as output.jpg", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    private void saveme(byte [] bytes){
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
            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

        }
        catch (Exception e){

        }
    }
    private  File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");
        Log.e("abcd",mediaStorageDir.getAbsolutePath());

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.e("abcd","shit");
                return null;
            }
            Log.e("abcd","fgh");
        }
        Log.e("abcd","frfdf");
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        Log.e("abc",mediaFile.getAbsolutePath());
        return mediaFile;
    }
    private void tryme(byte [] bytes){
        File mediaFile = new File(getExternalCacheDir(), "NewDirectory");
        File file = new File(mediaFile, "xyz.jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
