package com.cleveroad.progressexample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cleveroad.progresscircle.ProgressCircle;
import com.cleveroad.progresscircle.ProgressCircleListener;


public class MainActivity extends AppCompatActivity {

    ProgressCircle progressCircle;
    Thread updateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressCircle = (ProgressCircle) findViewById(R.id.progressView);

        final Button button = (Button) findViewById(R.id.bStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {startAnimationThread();
            }
        });
        progressCircle.setListener(new ProgressCircleListener() {
            @Override
            public void endAnimation() {
                Log.d("EndAnimation","End");
            }
        });
    }

    private void startAnimationThread() {
        if (updateThread != null && updateThread.isAlive())
            updateThread.interrupt();
        progressCircle.stopProgress();
        final Handler handler = new Handler();
        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (progressCircle.getMaxProgress() != progressCircle.getProgress()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressCircle.setProgress(progressCircle.getProgress() + 10);
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        updateThread.start();
    }
}

