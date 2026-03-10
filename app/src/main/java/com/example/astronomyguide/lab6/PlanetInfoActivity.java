package com.example.astronomyguide.lab6;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.astronomyguide.lab7.NeptuneWaterActivity;
import com.example.astronomyguide.R;

public class PlanetInfoActivity extends AppCompatActivity {

    private PlanetInfo planetInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String planetName = getIntent().getStringExtra("planet_name");
        if (planetName == null) {
            finish();
            return;
        }

        PlanetDataProvider dataProvider = PlanetDataProvider.getInstance();
        planetInfo = dataProvider.getPlanetInfo(planetName);

        if (planetInfo == null) {
            finish();
            return;
        }

        if (planetName.equals("Neptune")) {
            Intent intent = new Intent(this, NeptuneWaterActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        scrollView.setBackgroundColor(0xFF0A0A2A);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mainLayout.setPadding(40, 40, 40, 40);

        TextView titleText = new TextView(this);
        titleText.setText(planetInfo.getRussianName() + " (" + planetInfo.getName() + ")");
        titleText.setTextColor(0xFFFFFFFF);
        titleText.setTextSize(32);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 30);
        mainLayout.addView(titleText);

        if (planetInfo.getImageResourceId() != 0) {
            ImageView planetImage = new ImageView(this);
            planetImage.setLayoutParams(new LinearLayout.LayoutParams(
                    600, 600));
            planetImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            planetImage.setImageResource(planetInfo.getImageResourceId());
            planetImage.setPadding(0, 20, 0, 40);

            LinearLayout imageContainer = new LinearLayout(this);
            imageContainer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            imageContainer.setGravity(android.view.Gravity.CENTER);
            imageContainer.addView(planetImage);
            mainLayout.addView(imageContainer);
        } else {
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    40));
            mainLayout.addView(spacer);
        }

        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2));
        divider.setBackgroundColor(0xFF3F51B5);
        divider.setPadding(0, 20, 0, 20);
        mainLayout.addView(divider);

        TextView descTitle = new TextView(this);
        descTitle.setText("Описание");
        descTitle.setTextColor(0xFF3F51B5);
        descTitle.setTextSize(24);
        descTitle.setTypeface(null, Typeface.BOLD);
        descTitle.setPadding(0, 30, 0, 20);
        mainLayout.addView(descTitle);

        TextView descriptionText = new TextView(this);
        descriptionText.setText(planetInfo.getDescription());
        descriptionText.setTextColor(0xFFCCCCCC);
        descriptionText.setTextSize(16);
        descriptionText.setLineSpacing(1.5f, 1.2f);
        descriptionText.setPadding(0, 0, 0, 30);
        mainLayout.addView(descriptionText);

        TextView charsTitle = new TextView(this);
        charsTitle.setText("Характеристики");
        charsTitle.setTextColor(0xFF3F51B5);
        charsTitle.setTextSize(24);
        charsTitle.setTypeface(null, Typeface.BOLD);
        charsTitle.setPadding(0, 20, 0, 20);
        mainLayout.addView(charsTitle);

        LinearLayout charsLayout = new LinearLayout(this);
        charsLayout.setOrientation(LinearLayout.VERTICAL);
        charsLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        charsLayout.setBackgroundColor(0x1A1A1A4A);
        charsLayout.setPadding(30, 30, 30, 30);

        String[] characteristics = planetInfo.getCharacteristics();
        for (String characteristic : characteristics) {
            LinearLayout charItem = new LinearLayout(this);
            charItem.setOrientation(LinearLayout.HORIZONTAL);
            charItem.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            charItem.setPadding(0, 10, 0, 10);

            TextView marker = new TextView(this);
            marker.setText("• ");
            marker.setTextColor(0xFF3F51B5);
            marker.setTextSize(18);
            marker.setTypeface(null, Typeface.BOLD);
            charItem.addView(marker);

            TextView charText = new TextView(this);
            charText.setText(characteristic);
            charText.setTextColor(0xFFFFFFFF);
            charText.setTextSize(16);
            charItem.addView(charText);

            charsLayout.addView(charItem);
        }

        mainLayout.addView(charsLayout);

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                40));
        mainLayout.addView(spacer);

        Button backButton = new Button(this);
        backButton.setText("◄ НАЗАД К ПЛАНЕТАМ");
        backButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        backButton.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background));
        backButton.setTextColor(0xFFFFFFFF);
        backButton.setTextSize(18);
        backButton.setPadding(30, 20, 30, 20);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mainLayout.addView(backButton);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }
}