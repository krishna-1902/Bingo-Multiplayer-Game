package com.example.bingoonline;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

class MyThread extends Thread{
    ImageView imageView;
    boolean tillThen=true;
    float alpha=0f;

    public MyThread(){}

    public MyThread(ImageView img)
    {
        imageView=img;
    }

    @Override
    public void run() {
        super.run();
        while(alpha!=1f)
        {
            try{
                Thread.currentThread().sleep(100);
                alpha+=0.05;
                imageView.setAlpha(alpha);
            }catch (Exception ex){}
        }
    }
}

public class Mode extends AppCompatActivity {

    private MyThread myThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode);
        getSupportActionBar().hide();

        ImageView imageView=(ImageView)findViewById(R.id.imageView);
        myThread=new MyThread(imageView);
        myThread.start();
    }

    public void offLine(View view)
    {
        Intent intent=new Intent(this,OfflineMode.class);
        startActivity(intent);
        try {
            myThread.stop();
        }catch (Exception e){}
    }

    public void onLine(View view)
    {
        Intent intent=new Intent(this,createRoom.class);
        startActivity(intent);
        try {
            myThread.stop();
        }catch (Exception e){}
    }
}