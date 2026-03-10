package com.example.astronomyguide.lab7;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class NeptuneWaterActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private NeptuneRenderer renderer;
    private boolean rendererSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!supportsOpenGLES20()) {
            Toast.makeText(this, "Это устройство не поддерживает OpenGL ES 2.0", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new NeptuneRenderer(this);
        glSurfaceView.setRenderer(renderer);
        rendererSet = true;

        mainLayout.addView(glSurfaceView);

        LinearLayout topPanel = new LinearLayout(this);
        topPanel.setOrientation(LinearLayout.VERTICAL);
        topPanel.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        RelativeLayout.LayoutParams panelParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        panelParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        panelParams.setMargins(20, 40, 20, 0);
        topPanel.setLayoutParams(panelParams);

        TextView titleText = new TextView(this);
        titleText.setText("НЕПТУН - ВОДНАЯ ПОВЕРХНОСТЬ");
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setTextSize(24);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setBackgroundColor(0x80000000);
        titleText.setPadding(20, 10, 20, 10);
        titleText.setGravity(android.view.Gravity.CENTER);
        topPanel.addView(titleText);

        TextView infoText = new TextView(this);
        infoText.setText("Сфера с анимированной водной текстурой");
        infoText.setTextColor(0xFFFFFFFF);
        infoText.setTextSize(14);
        infoText.setBackgroundColor(0x80000000);
        infoText.setPadding(20, 10, 20, 10);
        infoText.setGravity(android.view.Gravity.CENTER);
        topPanel.addView(infoText);

        mainLayout.addView(topPanel);

        Button backButton = new Button(this);
        backButton.setText("◄ НАЗАД К ПЛАНЕТАМ");
        backButton.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        backButton.setBackgroundColor(0x80000000);
        backButton.setTextColor(0xFFFFFFFF);
        backButton.setTextSize(16);
        backButton.setPadding(20, 20, 20, 20);

        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        buttonParams.setMargins(20, 0, 20, 40);
        backButton.setLayoutParams(buttonParams);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mainLayout.addView(backButton);

        setContentView(mainLayout);
    }

    private boolean supportsOpenGLES20() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo =
                activityManager.getDeviceConfigurationInfo();
        return configurationInfo.reqGlEsVersion >= 0x20000;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rendererSet) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rendererSet) {
            glSurfaceView.onResume();
        }
    }
}