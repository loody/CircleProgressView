package me.loody.circleprogressview;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class MainActivity extends Activity {
    CircleProgressView circleProgressView;
    float progress = 0.1f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        circleProgressView = findViewById(R.id.cpv);
        circleProgressView.setDuration(500);
        circleProgressView.setProgress(progress);
        circleProgressView.execute();
        circleProgressView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress += 0.2f;
                if (progress > 1) {
                    progress = 0.1f;
                }
                circleProgressView.setProgress(progress);
                circleProgressView.execute();
            }
        });
    }
}
