package net.simplifiedcoding.volleyupload;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

public class DecodeActivity extends AppCompatActivity implements View.OnClickListener{
    private Button buttonChoose;
    private Button buttonUpload;

    private ImageView imageView;

    private TextView text;

    private Bitmap bitmap;

    private int PICK_IMAGE_REQUEST = 1;

    private String UPLOAD_URL =Urls.getURL()+"/upload";

    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    String type;
    public String getStringImage(Bitmap bmp){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return encodedImage;
    }
    public void decodemessage(){
        //Showing the progress dialog

        final ProgressDialog loading = ProgressDialog.show(this,"Decoding...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Urls.getURL()+"/decode",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {

                        loading.dismiss();
                        text.setText(s);
                       // Toast.makeText(DecodeActivity.this,s, Toast.LENGTH_SHORT).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        try {
                            Toast.makeText(DecodeActivity.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
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


                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                //params.put(KEY_NAME, name);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);

        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        text = (TextView) findViewById(R.id.text);

        imageView  = (ImageView) findViewById(R.id.imageView);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
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
            decodemessage();
        }
    }
}
