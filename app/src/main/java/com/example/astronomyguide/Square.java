package com.example.astronomyguide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private ShortBuffer drawListBuffer;
    private int mProgram;
    private int textureID = -1;
    private boolean textureLoaded = false;

    // Количество координат на вершину
    static final int COORDS_PER_VERTEX = 3;

    // Координаты вершин квадрата (увеличен для фона)
    static float squareCoords[] = {
            -3.85f,  3.85f, -3.85f,   // верхний левый - ОЧЕНЬ ДАЛЕКО
            -3.85f, -3.85f, -3.85f,   // нижний левый
            3.85f, -3.85f, -3.85f,   // нижний правый
            3.85f,  3.85f, -3.85f    // верхний правый
    };

    // Порядок отрисовки вершин
    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };

    // Координаты текстуры (перевернуты по Y для OpenGL)
    private float textureCoords[] = {
            0.0f, 0.0f,  // нижний левый
            0.0f, 1.0f,  // верхний левый
            1.0f, 1.0f,  // верхний правый
            1.0f, 0.0f   // нижний правый
    };

    // Шейдеры
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 aTexCoord;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  vTexCoord = aTexCoord;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec2 vTexCoord;" +
                    "uniform sampler2D uTexture;" +
                    "void main() {" +
                    "  vec4 texColor = texture2D(uTexture, vTexCoord);" +
                    "  gl_FragColor = texColor;" +
                    "}";

    public Square() {
        // Инициализация буферов вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // Инициализация буферов текстуры
        ByteBuffer tb = ByteBuffer.allocateDirect(textureCoords.length * 4);
        tb.order(ByteOrder.nativeOrder());
        textureBuffer = tb.asFloatBuffer();
        textureBuffer.put(textureCoords);
        textureBuffer.position(0);

        // Инициализация буфера порядка отрисовки
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        // Загрузка шейдеров
        int vertexShader = OpenGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = OpenGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Создание программы OpenGL
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void loadTexture(Context context, int resourceId) {
        try {
            // Генерируем текстуру
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            textureID = textures[0];

            // Связываем текстуру
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

            // Устанавливаем параметры фильтрации текстуры
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            // Устанавливаем параметры наложения текстуры
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            // Загружаем bitmap из ресурсов
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false; // Важно! Отключаем автоматическое масштабирование

            Bitmap bitmap = BitmapFactory.decodeResource(
                    context.getResources(), resourceId, options);

            if (bitmap == null) {
                // Создаем простую текстуру программно если файл не найден
                bitmap = createFallbackTexture();
            }

            // Загружаем bitmap в OpenGL
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Освобождаем bitmap
            bitmap.recycle();

            textureLoaded = true;

        } catch (Exception e) {
            e.printStackTrace();
            textureLoaded = false;
        }
    }

    private Bitmap createFallbackTexture() {
        // Создаем простую текстуру звездного неба программно
        int width = 256;
        int height = 256;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color;
                if ((x + y) % 50 < 2) {
                    // Белые точки - звезды
                    color = 0xFFFFFFFF;
                } else {
                    // Темно-синий фон
                    int r = 10 + (x % 20);
                    int g = 10 + (y % 20);
                    int b = 40 + ((x + y) % 40);
                    color = 0xFF000000 | (r << 16) | (g << 8) | b;
                }
                bitmap.setPixel(x, y, color);
            }
        }
        return bitmap;
    }

    public void draw(float[] mvpMatrix) {
        if (!textureLoaded) {
            return; // Не рисуем если текстура не загружена
        }

        // Используем программу
        GLES20.glUseProgram(mProgram);

        // Получаем хендлы атрибутов
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int textureCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        int textureHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Передаем матрицу преобразования
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Включаем массив вершин
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * 4, vertexBuffer);

        // Включаем массив координат текстуры
        GLES20.glEnableVertexAttribArray(textureCoordHandle);
        GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                GLES20.GL_FLOAT, false, 2 * 4, textureBuffer);

        // Связываем текстуру
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        GLES20.glUniform1i(textureHandle, 0);

        // Включаем смешивание для прозрачности
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Рисуем квадрат БЕЗ теста глубины для фона
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_BLEND);

        // Отключаем массивы вершин
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
    }
}