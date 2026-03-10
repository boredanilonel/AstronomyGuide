package com.example.astronomyguide.lab2;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OpenGLActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private boolean rendererSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!supportsOpenGLES20()) {
            Toast.makeText(this, "Это устройство не поддерживает OpenGL ES 2.0", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        glSurfaceView = new GLSurfaceView(this);

        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setRenderer(new OpenGLRenderer(this));
        rendererSet = true;

        setContentView(glSurfaceView);
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