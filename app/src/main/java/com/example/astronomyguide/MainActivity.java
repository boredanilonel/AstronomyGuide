package com.example.astronomyguide;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private NewsViewModel newsViewModel;
    private Handler mainHandler;
    private ScheduledExecutorService scheduler;
    private int secondsRemaining = 5;
    private TextView timerText;
    private TextView[] newsTitleViews;
    private TextView[] likeCountViews;
    private ImageView[] likeButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsViewModel = new ViewModelProvider(this).get(NewsViewModel.class);
        mainHandler = new Handler(Looper.getMainLooper());

        initializeViews();

        setupObservers();

        setupLikeButtons();

        startNewsRotation();

        Button continueBtn = findViewById(R.id.continue_btn);

        continueBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SolarSystemActivity.class);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        timerText = findViewById(R.id.timer_text);

        newsTitleViews = new TextView[4];
        likeCountViews = new TextView[4];
        likeButtons = new ImageView[4];

        newsTitleViews[0] = findViewById(R.id.news_1_title);
        newsTitleViews[1] = findViewById(R.id.news_2_title);
        newsTitleViews[2] = findViewById(R.id.news_3_title);
        newsTitleViews[3] = findViewById(R.id.news_4_title);

        likeCountViews[0] = findViewById(R.id.like_1_count);
        likeCountViews[1] = findViewById(R.id.like_2_count);
        likeCountViews[2] = findViewById(R.id.like_3_count);
        likeCountViews[3] = findViewById(R.id.like_4_count);

        likeButtons[0] = findViewById(R.id.like_1_btn);
        likeButtons[1] = findViewById(R.id.like_2_btn);
        likeButtons[2] = findViewById(R.id.like_3_btn);
        likeButtons[3] = findViewById(R.id.like_4_btn);
    }

    private void setupObservers() {
        newsViewModel.getNews().observe(this, newsItems -> {
            updateNewsDisplay(newsItems);
        });
    }

    private void setupLikeButtons() {
        for (int i = 0; i < likeButtons.length; i++) {
            final int position = i;
            likeButtons[i].setOnClickListener(v -> {
                newsViewModel.addLike(position);
            });
        }
    }

    private void updateNewsDisplay(List<NewsItem> newsItems) {
        for (int i = 0; i < newsItems.size() && i < 4; i++) {
            NewsItem news = newsItems.get(i);
            newsTitleViews[i].setText(news.getTitle() + "\n\n" + news.getContent());
            likeCountViews[i].setText(String.valueOf(news.getLikes()));
        }
    }

    private void startNewsRotation() {
        scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleWithFixedDelay(() -> {
            secondsRemaining--;

            mainHandler.post(() -> {
                timerText.setText(String.format(Locale.getDefault(),
                        "Следующая новость через: %d сек", secondsRemaining));

                if (secondsRemaining <= 0) {
                    newsViewModel.replaceRandomNews();
                    secondsRemaining = 5;
                }
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}