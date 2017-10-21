package com.dnaai;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by bkozik on 10/21/17.
 */

public class Splashscreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);


        Thread timerThread = new Thread(){
            @Override
            public void run() {
                try{
                    sleep(3500);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                } finally {
                    Intent intent = new Intent(Splashscreen.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
