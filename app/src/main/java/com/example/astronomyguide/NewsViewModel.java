package com.example.astronomyguide;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NewsViewModel extends ViewModel {
    private MutableLiveData<List<NewsItem>> newsLiveData;
    private List<NewsItem> allNews;
    private Random random;
    private List<Integer> displayedIndices;

    public NewsViewModel() {
        newsLiveData = new MutableLiveData<>();
        random = new Random();
        displayedIndices = new ArrayList<>();
        initializeNews();
    }

    private void initializeNews() {
        allNews = new ArrayList<>();

        allNews.add(new NewsItem("Открытие новой экзопланеты",
                "Астрономы обнаружили планету в зоне обитаемости звезды Kepler-452"));
        allNews.add(new NewsItem("Запуск нового телескопа",
                "NASA успешно запустило космический телескоп James Webb"));
        allNews.add(new NewsItem("Марсианская миссия",
                "Perseverance нашел следы древней воды на Марсе"));
        allNews.add(new NewsItem("Лунная программа",
                "Россия и Китай объявили о совместной лунной станции"));
        allNews.add(new NewsItem("Астероидная угроза",
                "Ученые отслеживают астероид, который приблизится к Земле в 2029 году"));
        allNews.add(new NewsItem("Черные дыры",
                "Обнаружена самая массивная черная дыра в соседней галактике"));
        allNews.add(new NewsItem("Солнечная активность",
                "Зафиксирована мощная солнечная вспышка класса X"));
        allNews.add(new NewsItem("Темная материя",
                "Новые данные проливают свет на природу темной материи"));
        allNews.add(new NewsItem("Венера",
                "В атмосфере Венеры обнаружены возможные признаки жизни"));
        allNews.add(new NewsItem("Млечный Путь",
                "Составлена новая 3D карта нашей галактики"));

        displayedIndices.clear();
        while (displayedIndices.size() < 4) {
            int index = random.nextInt(allNews.size());
            if (!displayedIndices.contains(index)) {
                displayedIndices.add(index);
            }
        }
        updateDisplayedNews();
    }

    public LiveData<List<NewsItem>> getNews() {
        return newsLiveData;
    }

    public List<NewsItem> getDisplayedNews() {
        List<NewsItem> displayed = new ArrayList<>();
        for (int index : displayedIndices) {
            displayed.add(allNews.get(index));
        }
        return displayed;
    }

    public void addLike(int position) {
        if (position >= 0 && position < displayedIndices.size()) {
            int newsIndex = displayedIndices.get(position);
            allNews.get(newsIndex).addLike();
            updateDisplayedNews();
        }
    }

    public void replaceRandomNews() {
        if (displayedIndices.isEmpty()) return;

        int positionToReplace = random.nextInt(displayedIndices.size());

        int newNewsIndex;
        do {
            newNewsIndex = random.nextInt(allNews.size());
        } while (displayedIndices.contains(newNewsIndex));

        displayedIndices.set(positionToReplace, newNewsIndex);
        updateDisplayedNews();
    }

    private void updateDisplayedNews() {
        List<NewsItem> displayed = getDisplayedNews();
        newsLiveData.setValue(displayed);
    }
}