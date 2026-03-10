package com.example.astronomyguide.lab3;

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

import com.example.astronomyguide.lab8.BlackHole;
import com.example.astronomyguide.lab4.TransparentCube;

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

    private Ring saturnRing;
    private Ring jupiterRing;
    private Ring uranusRing;

    private BlackHole blackHole;
    private float blackHoleX = 60.0f;
    private float blackHoleZ = -20.0f;
    private float blackHoleSpeed = 0.05f;

    private int[] planetTextures = new int[10];
    private FloatBuffer textVertexBuffer;
    private FloatBuffer textTextureBuffer;
    private ShortBuffer textIndexBuffer;
    private int textProgram;
    private List<Sphere> planets;
    private List<String> planetNames;
    private List<Float> orbitRadii;
    private List<Float> orbitSpeeds;
    private List<Float> rotationSpeeds;
    private TransparentCube selectionCube;
    private int selectedPlanetIndex = 0;
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
    private static final float MOON_DISTANCE = 0.45f;
    private static final float MOON_SIZE = 0.15f;
    private static final float[] MOON_COLOR = {0.8f, 0.8f, 0.8f, 1.0f};

    private static final String[] PLANET_NAMES = {
            "Sun", "Mercury", "Venus", "Earth", "Mars",
            "Jupiter", "Saturn", "Uranus", "Neptune", "Moon"
    };

    public SolarSystemRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.05f, 0.0f, 0.1f, 1.0f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        initSolarSystem();
        initTextRenderer();

        blackHole = new BlackHole();

        Matrix.setIdentityM(modelMatrix, 0);
    }

    private void initSolarSystem() {
        planets = new ArrayList<>();
        planetNames = new ArrayList<>();
        orbitRadii = new ArrayList<>();
        orbitSpeeds = new ArrayList<>();
        rotationSpeeds = new ArrayList<>();

        for (String name : PLANET_NAMES) {
            planetNames.add(name);
        }

        sun = new Sphere(0.9f, 20, 20);
        sun.setColor(1.0f, 0.9f, 0.0f, 1.0f);

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

        saturnRing = new Ring(PLANET_SIZES[5] * 1.3f, PLANET_SIZES[5] * 2.2f, 64);
        saturnRing.setColor(0.9f, 0.8f, 0.6f, 0.6f);

        jupiterRing = new Ring(PLANET_SIZES[4] * 1.2f, PLANET_SIZES[4] * 1.8f, 48);
        jupiterRing.setColor(0.7f, 0.6f, 0.5f, 0.3f);

        uranusRing = new Ring(PLANET_SIZES[6] * 1.25f, PLANET_SIZES[6] * 1.9f, 48);
        uranusRing.setColor(0.6f, 0.7f, 0.8f, 0.4f);

        moon = new Sphere(MOON_SIZE, 24, 24);
        moon.setColor(MOON_COLOR[0], MOON_COLOR[1], MOON_COLOR[2], MOON_COLOR[3]);

        planets.add(mercury);
        planets.add(venus);
        planets.add(earth);
        planets.add(mars);
        planets.add(jupiter);
        planets.add(saturn);
        planets.add(uranus);
        planets.add(neptune);

        for (int i = 0; i < planets.size(); i++) {
            orbitRadii.add(PLANET_DISTANCES[i]);
            orbitSpeeds.add(ORBIT_SPEEDS[i]);
            rotationSpeeds.add(ORBIT_SPEEDS[i] * 2.0f);
            planetAngles[i] = (float)(Math.random() * 360);
        }

        selectionCube = new TransparentCube();
        selectionCube.setColor(0.0f, 1.0f, 1.0f, 0.3f);

        allCelestialBodies = new ArrayList<>();
        allBodyNames = new ArrayList<>();

        allCelestialBodies.add(sun);
        allBodyNames.add("Sun");

        for (Sphere planet : planets) {
            allCelestialBodies.add(planet);
        }

        allBodyNames.add("Mercury");
        allBodyNames.add("Venus");
        allBodyNames.add("Earth");
        allBodyNames.add("Mars");
        allBodyNames.add("Jupiter");
        allBodyNames.add("Saturn");
        allBodyNames.add("Uranus");
        allBodyNames.add("Neptune");

        allCelestialBodies.add(moon);
        allBodyNames.add("Moon");

        for (int i = 0; i < selectionCubePositions.length; i++) {
            selectionCubePositions[i][0] = 0;
            selectionCubePositions[i][1] = 0;
            selectionCubePositions[i][2] = 0;
        }
    }

    private void initTextRenderer() {
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

        for (int i = 0; i < planetTextures.length; i++) {
            planetTextures[i] = createTextTexture(PLANET_NAMES[i]);
        }

        initTextQuad();
    }

    private int createTextTexture(String text) {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        int width = 512;
        int height = 256;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.TRANSPARENT);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(120);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, width / 2, height / 2 + 40, paint);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

        return textureId;
    }

    private void initTextQuad() {
        float[] vertices = {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                -0.5f,  0.5f, 0.0f,
                0.5f,  0.5f, 0.0f
        };

        float[] texCoords = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
        };


        short[] indices = {0, 1, 2, 1, 3, 2};

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        textVertexBuffer = bb.asFloatBuffer();
        textVertexBuffer.put(vertices);
        textVertexBuffer.position(0);

        ByteBuffer tb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tb.order(ByteOrder.nativeOrder());
        textTextureBuffer = tb.asFloatBuffer();
        textTextureBuffer.put(texCoords);
        textTextureBuffer.position(0);

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

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 50);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(viewMatrix, 0,
                0, 4, 12,
                0, 0, 0,
                0, 1, 0);

        updateSelectionCubePositions();

        updateBlackHole();

        drawBlackHole();

        drawSun();

        drawPlanets();

        drawRings();

        drawMoon();

        drawSelectionCubes();

        drawPlanetLabels();

        updateAngles();
    }

    private void updateBlackHole() {
        blackHoleX -= blackHoleSpeed;

        if (blackHoleX < -60.0f) {
            blackHoleX = 60.0f;
        }

        float blackHoleY = (float)Math.sin(blackHoleX * 0.5f) * 2.0f;

        blackHole.updatePosition(blackHoleX, blackHoleY, blackHoleZ);
        blackHole.updateAnimation();
    }

    private void drawBlackHole() {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        blackHole.draw(projectionMatrix, viewMatrix);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    private void drawSun() {
        float[] sunMatrix = new float[16];
        Matrix.multiplyMM(sunMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.rotateM(modelMatrix, 0, sunRotation, 0.0f, 1.0f, 0.0f);

        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, sunMatrix, 0, modelMatrix, 0);

        sun.draw(tempMatrix);
    }

    private void drawPlanets() {
        for (int i = 0; i < planets.size(); i++) {
            float[] planetMatrix = new float[16];
            Matrix.multiplyMM(planetMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            Matrix.setIdentityM(modelMatrix, 0);

            float angle = planetAngles[i];
            float radius = orbitRadii.get(i);

            float x = (float)(radius * Math.cos(Math.toRadians(angle)));
            float z = (float)(radius * Math.sin(Math.toRadians(angle)));

            Matrix.translateM(modelMatrix, 0, x, 0.0f, z);

            Matrix.rotateM(modelMatrix, 0, planetAngles[i] * 2.0f, 0.0f, 1.0f, 0.0f);

            float[] tempMatrix = new float[16];
            Matrix.multiplyMM(tempMatrix, 0, planetMatrix, 0, modelMatrix, 0);

            planets.get(i).draw(tempMatrix);
        }
    }

    private void drawRings() {
        for (int i = 0; i < planets.size(); i++) {
            if (i == 4) {
                drawRingForPlanet(jupiterRing, i, 0.0f);
            } else if (i == 5) {
                drawRingForPlanet(saturnRing, i, 0.0f);
            } else if (i == 6) {
                drawRingForPlanet(uranusRing, i, 90.0f);
            }
        }
    }

    private void drawRingForPlanet(Ring ring, int planetIndex, float tiltAngle) {
        float[] ringMatrix = new float[16];
        Matrix.multiplyMM(ringMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);

        float angle = planetAngles[planetIndex];
        float radius = orbitRadii.get(planetIndex);

        float x = (float)(radius * Math.cos(Math.toRadians(angle)));
        float z = (float)(radius * Math.sin(Math.toRadians(angle)));

        Matrix.translateM(modelMatrix, 0, x, 0.0f, z);

        Matrix.rotateM(modelMatrix, 0, tiltAngle, 1.0f, 0.0f, 0.0f);

        Matrix.rotateM(modelMatrix, 0, planetAngles[planetIndex], 0.0f, 1.0f, 0.0f);

        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, ringMatrix, 0, modelMatrix, 0);

        ring.draw(tempMatrix);
    }

    private void drawMoon() {
        float[] moonMatrix = new float[16];
        Matrix.multiplyMM(moonMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);

        float earthAngle = planetAngles[2];
        float earthRadius = orbitRadii.get(2);

        float earthX = (float)(earthRadius * Math.cos(Math.toRadians(earthAngle)));
        float earthZ = (float)(earthRadius * Math.sin(Math.toRadians(earthAngle)));

        float moonX = earthX + (float)(MOON_DISTANCE * Math.cos(Math.toRadians(moonOrbitAngle)));
        float moonY = (float)(MOON_DISTANCE * Math.sin(Math.toRadians(moonOrbitAngle)));
        float moonZ = earthZ;

        Matrix.translateM(modelMatrix, 0, moonX, moonY, moonZ);

        Matrix.rotateM(modelMatrix, 0, moonAngle, 0.0f, 1.0f, 0.0f);

        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, moonMatrix, 0, modelMatrix, 0);

        moon.draw(tempMatrix);
    }

    private void updateSelectionCubePositions() {
        selectionCubePositions[0][0] = 0;
        selectionCubePositions[0][1] = 0;
        selectionCubePositions[0][2] = 0;

        for (int i = 0; i < planets.size(); i++) {
            float angle = planetAngles[i];
            float radius = orbitRadii.get(i);

            float x = (float)(radius * Math.cos(Math.toRadians(angle)));
            float z = (float)(radius * Math.sin(Math.toRadians(angle)));

            selectionCubePositions[i + 1][0] = x;
            selectionCubePositions[i + 1][1] = 0;
            selectionCubePositions[i + 1][2] = z;
        }

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
        if (selectedPlanetIndex >= 0 && selectedPlanetIndex < selectionCubePositions.length) {
            float[] cubeMatrix = new float[16];
            Matrix.multiplyMM(cubeMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

            Matrix.setIdentityM(modelMatrix, 0);

            float x = selectionCubePositions[selectedPlanetIndex][0];
            float y = selectionCubePositions[selectedPlanetIndex][1];
            float z = selectionCubePositions[selectedPlanetIndex][2];

            Matrix.translateM(modelMatrix, 0, x, y, z);

            Matrix.rotateM(modelMatrix, 0, sunRotation * 0.5f, 0.0f, 1.0f, 0.0f);

            float scale;
            if (selectedPlanetIndex == 0) {
                scale = 1.1f;
            } else if (selectedPlanetIndex == 9) {
                scale = MOON_SIZE * 2.5f;
            } else {
                scale = PLANET_SIZES[selectedPlanetIndex - 1] * 2.2f;
            }
            Matrix.scaleM(modelMatrix, 0, scale, scale, scale);

            float[] tempMatrix = new float[16];
            Matrix.multiplyMM(tempMatrix, 0, cubeMatrix, 0, modelMatrix, 0);

            selectionCube.draw(tempMatrix);
        }
    }

    private void drawPlanetLabels() {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        GLES20.glUseProgram(textProgram);

        int positionHandle = GLES20.glGetAttribLocation(textProgram, "vPosition");
        int texCoordHandle = GLES20.glGetAttribLocation(textProgram, "aTexCoord");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(textProgram, "uMVPMatrix");
        int textureHandle = GLES20.glGetUniformLocation(textProgram, "uTexture");
        int colorHandle = GLES20.glGetUniformLocation(textProgram, "uColor");

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false, 3 * 4, textVertexBuffer);
        GLES20.glVertexAttribPointer(texCoordHandle, 2,
                GLES20.GL_FLOAT, false, 2 * 4, textTextureBuffer);

        drawLabel(0, 0.0f, 1.2f, 0.0f, new float[]{1.0f, 1.0f, 0.0f, 1.0f});

        for (int i = 0; i < planets.size(); i++) {
            float angle = planetAngles[i];
            float radius = orbitRadii.get(i);

            float x = (float)(radius * Math.cos(Math.toRadians(angle)));
            float z = (float)(radius * Math.sin(Math.toRadians(angle)));

            float[] textColor = new float[]{
                    PLANET_COLORS[i][0],
                    PLANET_COLORS[i][1],
                    PLANET_COLORS[i][2],
                    1.0f
            };

            drawLabel(i + 1, x, -0.5f, z, textColor);
        }

        float earthAngle = planetAngles[2];
        float earthRadius = orbitRadii.get(2);
        float earthX = (float)(earthRadius * Math.cos(Math.toRadians(earthAngle)));
        float earthZ = (float)(earthRadius * Math.sin(Math.toRadians(earthAngle)));

        float moonX = earthX + (float)(MOON_DISTANCE * Math.cos(Math.toRadians(moonOrbitAngle)));
        float moonY = (float)(MOON_DISTANCE * Math.sin(Math.toRadians(moonOrbitAngle))) - 0.3f;
        float moonZ = earthZ;

        drawLabel(9, moonX, moonY, moonZ, new float[]{0.8f, 0.8f, 0.8f, 1.0f});

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    private void drawLabel(int textureIndex, float x, float y, float z, float[] color) {
        float[] labelMatrix = new float[16];
        Matrix.multiplyMM(labelMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);

        Matrix.translateM(modelMatrix, 0, x, y, z);

        Matrix.scaleM(modelMatrix, 0, 0.3f, 0.1f, 0.3f);

        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, labelMatrix, 0, modelMatrix, 0);

        GLES20.glUniformMatrix4fv(
                GLES20.glGetUniformLocation(textProgram, "uMVPMatrix"),
                1, false, tempMatrix, 0);

        GLES20.glUniform4fv(
                GLES20.glGetUniformLocation(textProgram, "uColor"),
                1, color, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, planetTextures[textureIndex]);
        GLES20.glUniform1i(
                GLES20.glGetUniformLocation(textProgram, "uTexture"), 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6,
                GLES20.GL_UNSIGNED_SHORT, textIndexBuffer);
    }

    private void updateAngles() {
        sunRotation += 0.2f;
        if (sunRotation >= 360) sunRotation -= 360;

        for (int i = 0; i < planetAngles.length; i++) {
            planetAngles[i] += orbitSpeeds.get(i);
            if (planetAngles[i] >= 360) planetAngles[i] -= 360;
        }

        moonAngle += 2.0f;
        if (moonAngle >= 360) moonAngle -= 360;

        moonOrbitAngle += 5.0f;
        if (moonOrbitAngle >= 360) moonOrbitAngle -= 360;
    }

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
        if (selectedPlanetIndex >= 0 && selectedPlanetIndex < allBodyNames.size()) {
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