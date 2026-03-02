package com.example.astronomyguide;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
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

public class SolarSystemActivity extends AppCompatActivity {

    private GLSurfaceView glSurfaceView;
    private SolarSystemRenderer renderer;
    private boolean rendererSet = false;
    private TextView infoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!supportsOpenGLES20()) {
            Toast.makeText(this, "Это устройство не поддерживает OpenGL ES 2.0", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Создаем главный контейнер
        RelativeLayout mainLayout = new RelativeLayout(this);
        mainLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        // Создаем GLSurfaceView
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        renderer = new SolarSystemRenderer(this);
        glSurfaceView.setRenderer(renderer);
        rendererSet = true;

        // Добавляем GLSurfaceView в главный контейнер
        mainLayout.addView(glSurfaceView);

        // Создаем контейнер для кнопок (внизу экрана)
        LinearLayout buttonContainer = new LinearLayout(this);
        buttonContainer.setOrientation(LinearLayout.HORIZONTAL);
        buttonContainer.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        // Располагаем кнопки внизу
        RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        buttonParams.setMargins(20, 0, 20, 40);
        buttonContainer.setLayoutParams(buttonParams);
        buttonContainer.setWeightSum(3);

        // Создаем кнопку "ВЛЕВО"
        Button leftButton = new Button(this);
        leftButton.setText("◄ ВЛЕВО");
        leftButton.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        leftButton.setBackgroundColor(0x80000000);
        leftButton.setTextColor(0xFFFFFFFF);
        leftButton.setTextSize(16);
        leftButton.setPadding(10, 20, 10, 20);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.selectPreviousPlanet();
                updateInfoText();
            }
        });

        // Создаем кнопку "ИНФО"
        Button infoButton = new Button(this);
        infoButton.setText("ℹ ИНФО");
        infoButton.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        infoButton.setBackgroundColor(0x80000000);
        infoButton.setTextColor(0xFFFFFFFF);
        infoButton.setTextSize(16);
        infoButton.setPadding(10, 20, 10, 20);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String planetName = renderer.getSelectedPlanetName();

                // Проверяем, выбрана ли Луна
                if (planetName.equals("Moon") || planetName.equals("Луна")) {
                    // Переходим к активности с Луной и освещением Фонга
                    Intent intent = new Intent(SolarSystemActivity.this, MoonActivity.class);
                    startActivity(intent);
                } else {
                    // Показываем Toast с названием планеты
                    Toast.makeText(SolarSystemActivity.this,
                            "Выбрана планета: " + planetName + "\nНажмите ИНФО на Луне для детального просмотра",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // Создаем кнопку "ВПРАВО"
        Button rightButton = new Button(this);
        rightButton.setText("ВПРАВО ►");
        rightButton.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1));
        rightButton.setBackgroundColor(0x80000000);
        rightButton.setTextColor(0xFFFFFFFF);
        rightButton.setTextSize(16);
        rightButton.setPadding(10, 20, 10, 20);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.selectNextPlanet();
                updateInfoText();
            }
        });

        // Добавляем кнопки в контейнер
        buttonContainer.addView(leftButton);
        buttonContainer.addView(infoButton);
        buttonContainer.addView(rightButton);

        // Добавляем контейнер кнопок в главный контейнер
        mainLayout.addView(buttonContainer);

        // Создаем информационное поле (сверху экрана)
        infoText = new TextView(this);
        infoText.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        infoText.setText("Выбрано: Солнце");
        infoText.setTextColor(0xFFFFFFFF);
        infoText.setTextSize(18);
        infoText.setTypeface(null, Typeface.BOLD);
        infoText.setBackgroundColor(0x80000000);
        infoText.setPadding(20, 10, 20, 10);

        // Располагаем текст вверху
        RelativeLayout.LayoutParams textParams = (RelativeLayout.LayoutParams) infoText.getLayoutParams();
        textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        textParams.setMargins(0, 40, 0, 0);
        infoText.setLayoutParams(textParams);

        // Добавляем информационное поле в главный контейнер
        mainLayout.addView(infoText);

        // Устанавливаем главный контейнер как content view
        setContentView(mainLayout);
    }

    private void updateInfoText() {
        String planetName = renderer.getSelectedPlanetName();
        infoText.setText("Выбрано: " + planetName);
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