package com.example.astronomyguide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.opengl.GLUtils;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class SolarSystemRenderer implements GLSurfaceView.Renderer {

    private Context context;

    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];

    // Планеты
    private Sphere sun;
    private Sphere mercury;
    private Sphere venus;
    private Sphere earth;
    private Sphere mars;
    private Sphere jupiter;
    private Sphere saturn;
    private Sphere uranus;
    private Sphere neptune;
    private Sphere moon;

    // Кольца
    private Ring saturnRing;
    private Ring jupiterRing;
    private Ring uranusRing;

    // Текстуры для текста
    private int[] planetTextures = new int[10]; // 8 планет + солнце + луна
    private FloatBuffer textVertexBuffer;
    private FloatBuffer textTextureBuffer;
    private ShortBuffer textIndexBuffer;
    private int textProgram;

    // Списки для удобства
    private List<Sphere> planets;
    private List<String> planetNames;
    private List<Float> orbitRadii;
    private List<Float> orbitSpeeds;
    private List<Float> rotationSpeeds;

    // Новые поля для выбора планет
    private TransparentCube selectionCube;
    private int selectedPlanetIndex = 0; // 0 = Солнце, 1-8 = планеты, 9 = Луна
    private List<Sphere> allCelestialBodies;
    private List<String> allBodyNames;
    private float[][] selectionCubePositions = new float[10][3];

    // Углы вращения
    private float sunRotation = 0;
    private float[] planetAngles = new float[8];
    private float moonAngle = 0;
    private float moonOrbitAngle = 0;

    private static final float[] PLANET_DISTANCES = {
            1.5f,   // Меркурий
            2.9f,   // Венера
            3.7f,   // Земля
            4.5f,   // Марс
            5.7f,   // Юпитер
            8.1f,   // Сатурн
            8.9f,   // Уран
            9.9f    // Нептун
    };

    // Размеры планет (относительные)
    private static final float[] PLANET_SIZES = {
            0.25f,  // Меркурий
            0.28f,  // Венера
            0.29f,  // Земля
            0.27f,  // Марс
            0.5f,   // Юпитер
            0.55f,  // Сатурн
            0.4f,   // Уран
            0.39f   // Нептун
    };

    // Цвета планет (RGBA)
    private static final float[][] PLANET_COLORS = {
            {0.7f, 0.6f, 0.5f, 1.0f},   // Меркурий (серо-коричневый)
            {1.0f, 0.8f, 0.2f, 1.0f},   // Венера (желто-оранжевый)
            {0.2f, 0.4f, 1.0f, 1.0f},   // Земля (синий)
            {1.0f, 0.3f, 0.2f, 1.0f},   // Марс (красный)
            {0.9f, 0.7f, 0.5f, 1.0f},   // Юпитер (коричнево-желтый)
            {0.9f, 0.8f, 0.6f, 1.0f},   // Сатурн (песочный)
            {0.6f, 0.8f, 1.0f, 1.0f},   // Уран (голубой)
            {0.2f, 0.3f, 0.9f, 1.0f}    // Нептун (синий)
    };

    // Скорости вращения
    private static final float[] ORBIT_SPEEDS = {
            1.3f,   // Меркурий
            0.9f,   // Венера
            0.7f,   // Земля
            0.5f,   // Марс
            0.1f,   // Юпитер
            0.07f,  // Сатурн
            0.05f,  // Уран
            0.03f   // Нептун
    };

    // Луна
    private static final float MOON_DISTANCE = 0.45f;
    private static final float MOON_SIZE = 0.15f;
    private static final float[] MOON_COLOR = {0.8f, 0.8f, 0.8f, 1.0f};

    // Названия планет
    private static final String[] PLANET_NAMES = {
            "Sun", "Mercury", "Venus", "Earth", "Mars",
            "Jupiter", "Saturn", "Uranus", "Neptune", "Moon"
    };

    public SolarSystemRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Устанавливаем цвет очистки экрана
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Черный фон

        // Включаем тест глубины
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        // Инициализация
        initSolarSystem();
        initTextRenderer();

        Matrix.setIdentityM(modelMatrix, 0);
    }

    private void initSolarSystem() {
        // Инициализируем списки
        planets = new ArrayList<>();
        planetNames = new ArrayList<>();
        orbitRadii = new ArrayList<>();
        orbitSpeeds = new ArrayList<>();
        rotationSpeeds = new ArrayList<>();

        // Добавляем названия планет
        for (String name : PLANET_NAMES) {
            planetNames.add(name);
        }

        // Создаем Солнце (большой желтый шар)
        sun = new Sphere(0.9f, 20, 20);
        sun.setColor(1.0f, 0.9f, 0.0f, 1.0f); // Желтый

        // Создаем планеты
        mercury = new Sphere(PLANET_SIZES[0], 16, 16);
        mercury.setColor(PLANET_COLORS[0][0], PLANET_COLORS[0][1],
                PLANET_COLORS[0][2], PLANET_COLORS[0][3]);

        venus = new Sphere(PLANET_SIZES[1], 16, 16);
        venus.setColor(PLANET_COLORS[1][0], PLANET_COLORS[1][1],
                PLANET_COLORS[1][2], PLANET_COLORS[1][3]);

        earth = new Sphere(PLANET_SIZES[2], 16, 16);
        earth.setColor(PLANET_COLORS[2][0], PLANET_COLORS[2][1],
                PLANET_COLORS[2][2], PLANET_COLORS[2][3]);

        mars = new Sphere(PLANET_SIZES[3], 16, 16);
        mars.setColor(PLANET_COLORS[3][0], PLANET_COLORS[3][1],
                PLANET_COLORS[3][2], PLANET_COLORS[3][3]);

        jupiter = new Sphere(PLANET_SIZES[4], 20, 20);
        jupiter.setColor(PLANET_COLORS[4][0], PLANET_COLORS[4][1],
                PLANET_COLORS[4][2], PLANET_COLORS[4][3]);

        saturn = new Sphere(PLANET_SIZES[5], 20, 20);
        saturn.setColor(PLANET_COLORS[5][0], PLANET_COLORS[5][1],
                PLANET_COLORS[5][2], PLANET_COLORS[5][3]);

        uranus = new Sphere(PLANET_SIZES[6], 18, 18);
        uranus.setColor(PLANET_COLORS[6][0], PLANET_COLORS[6][1],
                PLANET_COLORS[6][2], PLANET_COLORS[6][3]);

        neptune = new Sphere(PLANET_SIZES[7], 18, 18);
        neptune.setColor(PLANET_COLORS[7][0], PLANET_COLORS[7][1],
                PLANET_COLORS[7][2], PLANET_COLORS[7][3]);

        // Создаем кольца
        // Сатурн - самые заметные кольца
        saturnRing = new Ring(PLANET_SIZES[5] * 1.3f, PLANET_SIZES[5] * 2.2f, 64);
        saturnRing.setColor(0.9f, 0.8f, 0.6f, 0.6f); // Золотистый, полупрозрачный

        // Юпитер - слабые кольца
        jupiterRing = new Ring(PLANET_SIZES[4] * 1.2f, PLANET_SIZES[4] * 1.8f, 48);
        jupiterRing.setColor(0.7f, 0.6f, 0.5f, 0.3f); // Темнее, более прозрачный

        // Уран - тонкие вертикальные кольца
        uranusRing = new Ring(PLANET_SIZES[6] * 1.25f, PLANET_SIZES[6] * 1.9f, 48);
        uranusRing.setColor(0.6f, 0.7f, 0.8f, 0.4f); // Голубоватый, прозрачный

        // Создаем Луну
        moon = new Sphere(MOON_SIZE, 24, 24);
        moon.setColor(MOON_COLOR[0], MOON_COLOR[1], MOON_COLOR[2], MOON_COLOR[3]);

        // Добавляем планеты в список
        planets.add(mercury);
        planets.add(venus);
        planets.add(earth);
        planets.add(mars);
        planets.add(jupiter);
        planets.add(saturn);
        planets.add(uranus);
        planets.add(neptune);

        // Инициализируем параметры орбит
        for (int i = 0; i < planets.size(); i++) {
            orbitRadii.add(PLANET_DISTANCES[i]);
            orbitSpeeds.add(ORBIT_SPEEDS[i]);
            rotationSpeeds.add(ORBIT_SPEEDS[i] * 2.0f);
            planetAngles[i] = (float)(Math.random() * 360);
        }

        // Инициализируем прозрачный куб для выделения
        selectionCube = new TransparentCube();
        selectionCube.setColor(0.0f, 1.0f, 1.0f, 0.3f); // Голубой полупрозрачный

        // Создаем список всех небесных тел для выбора
        allCelestialBodies = new ArrayList<>();
        allBodyNames = new ArrayList<>();

        // Добавляем Солнце
        allCelestialBodies.add(sun);
        allBodyNames.add("Sun");

        // Добавляем планеты
        for (Sphere planet : planets) {
            allCelestialBodies.add(planet);
        }

        // Добавляем Луну
        allCelestialBodies.add(moon);
        allBodyNames.add("Moon");

        // Инициализируем массив позиций для кубов выбора
        for (int i = 0; i < selectionCubePositions.length; i++) {
            selectionCubePositions[i][0] = 0;
            selectionCubePositions[i][1] = 0;
            selectionCubePositions[i][2] = 0;
        }
    }

    private void initTextRenderer() {
        // Инициализация шейдеров для текста
        String vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec2 aTexCoord;" +
                        "varying vec2 vTexCoord;" +
                        "void main() {" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "  vTexCoord = aTexCoord;" +
                        "}";

        String fragmentShaderCode =
                "precision mediump float;" +
                        "varying vec2 vTexCoord;" +
                        "uniform sampler2D uTexture;" +
                        "uniform vec4 uColor;" +
                        "void main() {" +
                        "  vec4 texColor = texture2D(uTexture, vTexCoord);" +
                        "  gl_FragColor = vec4(uColor.rgb, texColor.a * uColor.a);" +
                        "}";

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        textProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(textProgram, vertexShader);
        GLES20.glAttachShader(textProgram, fragmentShader);
        GLES20.glLinkProgram(textProgram);

        // Создаем текстуры с текстом для каждой планеты
        for (int i = 0; i < planetTextures.length; i++) {
            planetTextures[i] = createTextTexture(PLANET_NAMES[i]);
        }

        // Инициализируем буферы для квадрата (для отображения текста)
        initTextQuad();
    }

    private int createTextTexture(String text) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        // Параметры текстуры
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // Создаем Bitmap с текстом
        int width = 512;
        int height = 256;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Прозрачный фон
        canvas.drawColor(Color.TRANSPARENT);

        // Настройки текста
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(120);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        // Рисуем текст
        canvas.drawText(text, width / 2, height / 2 + 40, paint);

        // Загружаем текстуру
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        return textureId;
    }

    private void initTextQuad() {
        // Координаты вершин квадрата для текста
        float[] vertices = {
                -0.5f, -0.5f, 0.0f,  // нижний левый
                0.5f, -0.5f, 0.0f,  // нижний правый
                -0.5f,  0.5f, 0.0f,  // верхний левый
                0.5f,  0.5f, 0.0f   // верхний правый
        };

        // Координаты текстуры
        float[] texCoords = {
                0.0f, 1.0f,  // нижний левый
                1.0f, 1.0f,  // нижний правый
                0.0f, 0.0f,  // верхний левый
                1.0f, 0.0f   // верхний правый
        };

        // Индексы
        short[] indices = {0, 1, 2, 1, 3, 2};

        // Буфер вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        textVertexBuffer = bb.asFloatBuffer();
        textVertexBuffer.put(vertices);
        textVertexBuffer.position(0);

        // Буфер текстурных координат
        ByteBuffer tb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tb.order(ByteOrder.nativeOrder());
        textTextureBuffer = tb.asFloatBuffer();
        textTextureBuffer.put(texCoords);
        textTextureBuffer.position(0);

        // Буфер индексов
        ByteBuffer ib = ByteBuffer.allocateDirect(indices.length * 2);
        ib.order(ByteOrder.nativeOrder());
        textIndexBuffer = ib.asShortBuffer();
        textIndexBuffer.put(indices);
        textIndexBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // Устанавливаем матрицу проекции (перспектива)
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 50);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Очищаем экран и буфер глубины
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Устанавливаем позицию камеры
        Matrix.setLookAtM(viewMatrix, 0,
                0, 4, 12,      // Позиция камеры (чуть дальше для лучшего обзора)
                0, 0, 0,       // Точка, на которую смотрим
                0, 1, 0);      // Направление вверх

        // Обновляем позиции для кубов выбора
        updateSelectionCubePositions();

        // 1. Рисуем Солнце
        drawSun();

        // 2. Рисуем планеты
        drawPlanets();

        // 3. Рисуем кольца
        drawRings();

        // 4. Рисуем Луну
        drawMoon();

        // 5. Рисуем кубы выбора (прозрачные кубы вокруг выбранных планет)
        drawSelectionCubes();

        // 6. Рисуем подписи планет
        drawPlanetLabels();

        // 7. Обновляем углы вращения
        updateAngles();
    }

    private void drawSun() {
        float[] sunMatrix = new float[16];
        Matrix.multiplyMM(sunMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);

        // Вращение Солнца вокруг своей оси
        Matrix.rotateM(modelMatrix, 0, sunRotation, 0.0f, 1.0f, 0.0f);

        // Объединяем матрицы
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, sunMatrix, 0, modelMatrix, 0);

        sun.draw(tempMatrix);
    }

    private void drawPlanets() {
        for (int i = 0; i < planets.size(); i++) {
            float[] planetMatrix = new float[16];
            Matrix.multiplyMM(planetMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            Matrix.setIdentityM(modelMatrix, 0);

            // Позиция планеты на орбите
            float angle = planetAngles[i];
            float radius = orbitRadii.get(i);

            // Вычисляем позицию на круговой орбите в плоскости XZ
            float x = (float)(radius * Math.cos(Math.toRadians(angle)));
            float z = (float)(radius * Math.sin(Math.toRadians(angle)));

            // Планеты вращаются в плоскости эклиптики (XZ)
            Matrix.translateM(modelMatrix, 0, x, 0.0f, z);

            // Вращение планеты вокруг своей оси
            Matrix.rotateM(modelMatrix, 0, planetAngles[i] * 2.0f, 0.0f, 1.0f, 0.0f);

            // Объединяем матрицы
            float[] tempMatrix = new float[16];
            Matrix.multiplyMM(tempMatrix, 0, planetMatrix, 0, modelMatrix, 0);

            planets.get(i).draw(tempMatrix);
        }
    }

    private void drawRings() {
        // Рисуем кольца для соответствующих планет
        for (int i = 0; i < planets.size(); i++) {
            if (i == 4) { // Юпитер (индекс 4)
                drawRingForPlanet(jupiterRing, i, 0.0f);
            } else if (i == 5) { // Сатурн (индекс 5)
                drawRingForPlanet(saturnRing, i, 0.0f);
            } else if (i == 6) { // Уран (индекс 6)
                // Уран вращается на боку, его кольца вертикальные
                drawRingForPlanet(uranusRing, i, 90.0f);
            }
        }
    }

    private void drawRingForPlanet(Ring ring, int planetIndex, float tiltAngle) {
        float[] ringMatrix = new float[16];
        Matrix.multiplyMM(ringMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);

        // Позиция кольца совпадает с позицией планеты
        float angle = planetAngles[planetIndex];
        float radius = orbitRadii.get(planetIndex);

        float x = (float)(radius * Math.cos(Math.toRadians(angle)));
        float z = (float)(radius * Math.sin(Math.toRadians(angle)));

        Matrix.translateM(modelMatrix, 0, x, 0.0f, z);

        // Наклон кольца (особенно важно для Урана)
        Matrix.rotateM(modelMatrix, 0, tiltAngle, 1.0f, 0.0f, 0.0f);

        // Вращение кольца вместе с планетой
        Matrix.rotateM(modelMatrix, 0, planetAngles[planetIndex], 0.0f, 1.0f, 0.0f);

        // Объединяем матрицы
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, ringMatrix, 0, modelMatrix, 0);

        ring.draw(tempMatrix);
    }

    private void drawMoon() {
        float[] moonMatrix = new float[16];
        Matrix.multiplyMM(moonMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);

        // Сначала позиционируем относительно Земли
        float earthAngle = planetAngles[2]; // Земля - третья планета
        float earthRadius = orbitRadii.get(2);

        // Позиция Земли
        float earthX = (float)(earthRadius * Math.cos(Math.toRadians(earthAngle)));
        float earthZ = (float)(earthRadius * Math.sin(Math.toRadians(earthAngle)));

        // Луна вращается вокруг Земли
        // Орбита Луны перпендикулярна плоскости эклиптики (используем ось Y)
        float moonX = earthX + (float)(MOON_DISTANCE * Math.cos(Math.toRadians(moonOrbitAngle)));
        float moonY = (float)(MOON_DISTANCE * Math.sin(Math.toRadians(moonOrbitAngle))); // Вращение по Y
        float moonZ = earthZ;

        Matrix.translateM(modelMatrix, 0, moonX, moonY, moonZ);

        // Вращение Луны вокруг своей оси
        Matrix.rotateM(modelMatrix, 0, moonAngle, 0.0f, 1.0f, 0.0f);

        // Объединяем матрицы
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, moonMatrix, 0, modelMatrix, 0);

        moon.draw(tempMatrix);
    }

    private void updateSelectionCubePositions() {
        // Позиция для Солнца (всегда в центре)
        selectionCubePositions[0][0] = 0;
        selectionCubePositions[0][1] = 0;
        selectionCubePositions[0][2] = 0;

        // Позиции для планет
        for (int i = 0; i < planets.size(); i++) {
            float angle = planetAngles[i];
            float radius = orbitRadii.get(i);

            float x = (float)(radius * Math.cos(Math.toRadians(angle)));
            float z = (float)(radius * Math.sin(Math.toRadians(angle)));

            selectionCubePositions[i + 1][0] = x;
            selectionCubePositions[i + 1][1] = 0;
            selectionCubePositions[i + 1][2] = z;
        }

        // Позиция для Луны (индекс 9)
        float earthAngle = planetAngles[2];
        float earthRadius = orbitRadii.get(2);
        float earthX = (float)(earthRadius * Math.cos(Math.toRadians(earthAngle)));
        float earthZ = (float)(earthRadius * Math.sin(Math.toRadians(earthAngle)));

        float moonX = earthX + (float)(MOON_DISTANCE * Math.cos(Math.toRadians(moonOrbitAngle)));
        float moonY = (float)(MOON_DISTANCE * Math.sin(Math.toRadians(moonOrbitAngle)));
        float moonZ = earthZ;

        selectionCubePositions[9][0] = moonX;
        selectionCubePositions[9][1] = moonY;
        selectionCubePositions[9][2] = moonZ;
    }

    private void drawSelectionCubes() {
        // Рисуем прозрачный куб вокруг выбранной планеты
        if (selectedPlanetIndex >= 0 && selectedPlanetIndex < selectionCubePositions.length) {
            float[] cubeMatrix = new float[16];
            Matrix.multiplyMM(cubeMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            Matrix.setIdentityM(modelMatrix, 0);

            // Позиционируем куб
            float x = selectionCubePositions[selectedPlanetIndex][0];
            float y = selectionCubePositions[selectedPlanetIndex][1];
            float z = selectionCubePositions[selectedPlanetIndex][2];

            Matrix.translateM(modelMatrix, 0, x, y, z);

            // Вращаем куб для лучшего эффекта
            Matrix.rotateM(modelMatrix, 0, sunRotation * 0.5f, 0.0f, 1.0f, 0.0f);

            // Масштабируем куб чуть больше планеты
            float scale;
            if (selectedPlanetIndex == 0) {
                scale = 1.1f; // Солнце
            } else if (selectedPlanetIndex == 9) {
                scale = MOON_SIZE * 2.5f; // Луна
            } else {
                scale = PLANET_SIZES[selectedPlanetIndex - 1] * 2.2f;
            }
            Matrix.scaleM(modelMatrix, 0, scale, scale, scale);

            // Объединяем матрицы
            float[] tempMatrix = new float[16];
            Matrix.multiplyMM(tempMatrix, 0, cubeMatrix, 0, modelMatrix, 0);

            selectionCube.draw(tempMatrix);
        }
    }

    private void drawPlanetLabels() {
        // Включаем смешивание для текста
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Отключаем тест глубины для текста (чтобы текст всегда был поверх)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Используем программу для текста
        GLES20.glUseProgram(textProgram);

        // Получаем хендлы атрибутов
        int positionHandle = GLES20.glGetAttribLocation(textProgram, "vPosition");
        int texCoordHandle = GLES20.glGetAttribLocation(textProgram, "aTexCoord");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(textProgram, "uMVPMatrix");
        int textureHandle = GLES20.glGetUniformLocation(textProgram, "uTexture");
        int colorHandle = GLES20.glGetUniformLocation(textProgram, "uColor");

        // Включаем массивы
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        // Устанавливаем указатели на буферы
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false, 3 * 4, textVertexBuffer);
        GLES20.glVertexAttribPointer(texCoordHandle, 2,
                GLES20.GL_FLOAT, false, 2 * 4, textTextureBuffer);

        // Рисуем подпись для Солнца
        drawLabel(0, 0.0f, 1.2f, 0.0f, new float[]{1.0f, 1.0f, 0.0f, 1.0f});

        // Рисуем подписи для планет
        for (int i = 0; i < planets.size(); i++) {
            float angle = planetAngles[i];
            float radius = orbitRadii.get(i);

            float x = (float)(radius * Math.cos(Math.toRadians(angle)));
            float z = (float)(radius * Math.sin(Math.toRadians(angle)));

            // Цвет текста соответствует цвету планеты
            float[] textColor = new float[]{
                    PLANET_COLORS[i][0],
                    PLANET_COLORS[i][1],
                    PLANET_COLORS[i][2],
                    1.0f
            };

            // Подпись немного ниже планеты
            drawLabel(i + 1, x, -0.5f, z, textColor);
        }

        // Рисуем подпись для Луны
        float earthAngle = planetAngles[2];
        float earthRadius = orbitRadii.get(2);
        float earthX = (float)(earthRadius * Math.cos(Math.toRadians(earthAngle)));
        float earthZ = (float)(earthRadius * Math.sin(Math.toRadians(earthAngle)));

        float moonX = earthX + (float)(MOON_DISTANCE * Math.cos(Math.toRadians(moonOrbitAngle)));
        float moonY = (float)(MOON_DISTANCE * Math.sin(Math.toRadians(moonOrbitAngle))) - 0.3f;
        float moonZ = earthZ;

        drawLabel(9, moonX, moonY, moonZ, new float[]{0.8f, 0.8f, 0.8f, 1.0f});

        // Отключаем массивы
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);

        // Включаем тест глубины обратно
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    private void drawLabel(int textureIndex, float x, float y, float z, float[] color) {
        float[] labelMatrix = new float[16];
        Matrix.multiplyMM(labelMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);

        // Позиционируем текст
        Matrix.translateM(modelMatrix, 0, x, y, z);

        // Масштабируем текст (делаем маленьким)
        Matrix.scaleM(modelMatrix, 0, 0.3f, 0.1f, 0.3f);

        // Объединяем матрицы
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, labelMatrix, 0, modelMatrix, 0);

        // Устанавливаем матрицу
        GLES20.glUniformMatrix4fv(
                GLES20.glGetUniformLocation(textProgram, "uMVPMatrix"),
                1, false, tempMatrix, 0);

        // Устанавливаем цвет
        GLES20.glUniform4fv(
                GLES20.glGetUniformLocation(textProgram, "uColor"),
                1, color, 0);

        // Активируем текстуру
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, planetTextures[textureIndex]);
        GLES20.glUniform1i(
                GLES20.glGetUniformLocation(textProgram, "uTexture"), 0);

        // Рисуем текст
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6,
                GLES20.GL_UNSIGNED_SHORT, textIndexBuffer);
    }

    private void updateAngles() {
        // Вращение Солнца
        sunRotation += 0.2f;
        if (sunRotation >= 360) sunRotation -= 360;

        // Обновление углов планет
        for (int i = 0; i < planetAngles.length; i++) {
            planetAngles[i] += orbitSpeeds.get(i);
            if (planetAngles[i] >= 360) planetAngles[i] -= 360;
        }

        // Вращение Луны вокруг своей оси
        moonAngle += 2.0f;
        if (moonAngle >= 360) moonAngle -= 360;

        // Орбита Луны вокруг Земли
        moonOrbitAngle += 5.0f;
        if (moonOrbitAngle >= 360) moonOrbitAngle -= 360;
    }

    // Методы для управления выбором планеты
    public void selectNextPlanet() {
        selectedPlanetIndex = (selectedPlanetIndex + 1) % allCelestialBodies.size();
    }

    public void selectPreviousPlanet() {
        selectedPlanetIndex--;
        if (selectedPlanetIndex < 0) {
            selectedPlanetIndex = allCelestialBodies.size() - 1;
        }
    }

    public String getSelectedPlanetName() {
        if (selectedPlanetIndex < allBodyNames.size()) {
            return allBodyNames.get(selectedPlanetIndex);
        }
        return "Unknown";
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String error = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Ошибка компиляции шейдера: " + error);
        }

        return shader;
    }
}