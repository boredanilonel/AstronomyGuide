package com.example.astronomyguide.lab6;

import android.util.SparseArray;

import com.example.astronomyguide.R;

import java.util.HashMap;
import java.util.Map;

public class PlanetDataProvider {

    private static PlanetDataProvider instance;
    private Map<String, PlanetInfo> planetInfoMap;
    private SparseArray<PlanetInfo> planetInfoById;

    private PlanetDataProvider() {
        planetInfoMap = new HashMap<>();
        planetInfoById = new SparseArray<>();
        initPlanetData();
    }

    public static synchronized PlanetDataProvider getInstance() {
        if (instance == null) {
            instance = new PlanetDataProvider();
        }
        return instance;
    }

    private void initPlanetData() {
        String[] sunChars = {
                "Тип: Желтый карлик",
                "Диаметр: 1.39 млн км",
                "Масса: 1.989 × 10^30 кг",
                "Температура: 5500°C (поверхность)",
                "Возраст: 4.6 млрд лет"
        };
        planetInfoMap.put("Sun", new PlanetInfo("Sun", "Солнце",
                "Солнце — единственная звезда Солнечной системы. Вокруг Солнца обращаются другие объекты этой системы: планеты и их спутники, карликовые планеты и их спутники, астероиды, метеороиды, кометы и космическая пыль.",
                R.drawable.sun, sunChars));
        planetInfoById.put(0, planetInfoMap.get("Sun"));

        String[] mercuryChars = {
                "Тип: Планета земной группы",
                "Диаметр: 4,879 км",
                "Масса: 3.30 × 10^23 кг",
                "Температура: -173°C до 427°C",
                "День: 59 земных суток",
                "Год: 88 земных суток"
        };
        planetInfoMap.put("Mercury", new PlanetInfo("Mercury", "Меркурий",
                "Меркурий — самая близкая к Солнцу планета. Поверхность Меркурия покрыта кратерами, как у Луны. У планеты нет спутников и практически нет атмосферы.",
                R.drawable.mercury, mercuryChars));
        planetInfoById.put(1, planetInfoMap.get("Mercury"));

        String[] venusChars = {
                "Тип: Планета земной группы",
                "Диаметр: 12,104 км",
                "Масса: 4.87 × 10^24 кг",
                "Температура: 462°C",
                "День: 243 земных суток",
                "Год: 225 земных суток"
        };
        planetInfoMap.put("Venus", new PlanetInfo("Venus", "Венера",
                "Венера — вторая планета от Солнца. Это самая горячая планета Солнечной системы из-за плотной атмосферы, состоящей в основном из углекислого газа. Венера вращается в направлении, противоположном большинству планет.",
                R.drawable.venus, venusChars));
        planetInfoById.put(2, planetInfoMap.get("Venus"));

        String[] earthChars = {
                "Тип: Планета земной группы",
                "Диаметр: 12,742 км",
                "Масса: 5.97 × 10^24 кг",
                "Температура: -88°C до 58°C",
                "День: 24 часа",
                "Год: 365.25 дней",
                "Спутник: 1 (Луна)"
        };
        planetInfoMap.put("Earth", new PlanetInfo("Earth", "Земля",
                "Земля — третья планета от Солнца и единственная известная планета, на которой есть жизнь. Около 71% поверхности Земли покрыто водой. Атмосфера Земли состоит в основном из азота и кислорода.",
                R.drawable.earth, earthChars));
        planetInfoById.put(3, planetInfoMap.get("Earth"));

        String[] marsChars = {
                "Тип: Планета земной группы",
                "Диаметр: 6,779 км",
                "Масса: 6.42 × 10^23 кг",
                "Температура: -153°C до 20°C",
                "День: 24.6 часа",
                "Год: 687 земных суток",
                "Спутники: 2 (Фобос и Деймос)"
        };
        planetInfoMap.put("Mars", new PlanetInfo("Mars", "Марс",
                "Марс — четвертая планета от Солнца. Из-за красного цвета поверхности, вызванного оксидом железа, Марс называют Красной планетой. На Марсе находится самый высокий вулкан в Солнечной системе — Олимп.",
                R.drawable.mars, marsChars));
        planetInfoById.put(4, planetInfoMap.get("Mars"));

        String[] jupiterChars = {
                "Тип: Газовый гигант",
                "Диаметр: 139,820 км",
                "Масса: 1.90 × 10^27 кг",
                "Температура: -145°C",
                "День: 9.9 часа",
                "Год: 11.9 земных лет",
                "Спутники: 79 (включая Ганимед, Каллисто, Ио, Европу)"
        };
        planetInfoMap.put("Jupiter", new PlanetInfo("Jupiter", "Юпитер",
                "Юпитер — пятая планета от Солнца и крупнейшая в Солнечной системе. Это газовый гигант, состоящий в основном из водорода и гелия. У Юпитера мощная магнитосфера и Большое красное пятно — гигантский шторм.",
                R.drawable.jupiter, jupiterChars));
        planetInfoById.put(5, planetInfoMap.get("Jupiter"));

        String[] saturnChars = {
                "Тип: Газовый гигант",
                "Диаметр: 116,460 км",
                "Масса: 5.68 × 10^26 кг",
                "Температура: -178°C",
                "День: 10.7 часа",
                "Год: 29.5 земных лет",
                "Спутники: 82 (включая Титан, Рею, Диону)"
        };
        planetInfoMap.put("Saturn", new PlanetInfo("Saturn", "Сатурн",
                "Сатурн — шестая планета от Солнца. Знаменит своей системой колец, состоящих из льда и пыли. Сатурн — самая менее плотная планета Солнечной системы (его плотность меньше плотности воды).",
                R.drawable.saturn, saturnChars));
        planetInfoById.put(6, planetInfoMap.get("Saturn"));

        String[] uranusChars = {
                "Тип: Ледяной гигант",
                "Диаметр: 50,724 км",
                "Масса: 8.68 × 10^25 кг",
                "Температура: -224°C",
                "День: 17.2 часа",
                "Год: 84 земных года",
                "Спутники: 27"
        };
        planetInfoMap.put("Uranus", new PlanetInfo("Uranus", "Уран",
                "Уран — седьмая планета от Солнца. Это ледяной гигант с очень холодной атмосферой. Уран вращается «на боку» — ось вращения наклонена почти на 98 градусов.",
                R.drawable.uranus, uranusChars));
        planetInfoById.put(7, planetInfoMap.get("Uranus"));

        String[] neptuneChars = {
                "Тип: Ледяной гигант",
                "Диаметр: 49,244 км",
                "Масса: 1.02 × 10^26 кг",
                "Температура: -218°C",
                "День: 16.1 часа",
                "Год: 164.8 земных лет",
                "Спутники: 14 (включая Тритон)"
        };
        planetInfoMap.put("Neptune", new PlanetInfo("Neptune", "Нептун",
                "Нептун — восьмая и самая дальняя планета от Солнца. Это ледяной гигант, известный своими сильными ветрами — самыми быстрыми в Солнечной системе (до 2100 км/ч).",
                R.drawable.neptune, neptuneChars));
        planetInfoById.put(8, planetInfoMap.get("Neptune"));

        String[] moonChars = {
                "Тип: Естественный спутник",
                "Диаметр: 3,474 км",
                "Масса: 7.35 × 10^22 кг",
                "Температура: -173°C до 127°C",
                "Период вращения: 27.3 дней",
                "Расстояние от Земли: 384,400 км"
        };

        planetInfoMap.put("Moon", new PlanetInfo("Moon", "Луна",
                "Луна — единственный естественный спутник Земли. Это пятый по величине спутник в Солнечной системе. Лунная поверхность покрыта кратерами от ударов метеоритов. Луна всегда повернута к Земле одной стороной.",
                0, moonChars));
        planetInfoById.put(9, planetInfoMap.get("Moon"));
    }

    public PlanetInfo getPlanetInfo(String planetName) {
        return planetInfoMap.get(planetName);
    }

    public PlanetInfo getPlanetInfo(int index) {
        return planetInfoById.get(index);
    }

    public int getPlanetImageResource(String planetName) {
        PlanetInfo info = planetInfoMap.get(planetName);
        if (info != null && info.getImageResourceId() != 0) {
            return info.getImageResourceId();
        }
        return R.drawable.unknown;
    }
}