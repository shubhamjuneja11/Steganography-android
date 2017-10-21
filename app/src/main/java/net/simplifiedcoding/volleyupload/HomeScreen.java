package net.simplifiedcoding.volleyupload;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
    }
    public void encode(View view){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    public void decode(View view){
        Intent intent=new Intent(this,DecodeActivity.class);
        startActivity(intent);
    }
}
