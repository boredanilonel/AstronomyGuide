package com.example.astronomyguide;

import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Cube {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram;

    // Количество координат на вершину
    static final int COORDS_PER_VERTEX = 3;

    // Координаты вершин куба
    private float cubeCoords[] = {
            // Передняя грань
            -0.75f,  0.75f,  0.75f, // верхний левый фронт
            -0.75f, -0.75f,  0.75f, // нижний левый фронт
            0.75f, -0.75f,  0.75f, // нижний правый фронт
            0.75f,  0.75f,  0.75f, // верхний правый фронт

            // Задняя грань
            -0.75f,  0.75f, -0.75f, // верхний левый зад
            -0.75f, -0.75f, -0.75f, // нижний левый зад
            0.75f, -0.75f, -0.75f, // нижний правый зад
            0.75f,  0.75f, -0.75f, // верхний правый зад
    };

    // Индексы вершин для отрисовки треугольников
    private short drawOrder[] = {
            // Передняя грань
            0, 1, 2,
            0, 2, 3,

            // Задняя грань
            4, 5, 6,
            4, 6, 7,

            // Верхняя грань
            0, 4, 7,
            0, 7, 3,

            // Нижняя грань
            1, 5, 6,
            1, 6, 2,

            // Правая грань
            3, 2, 6,
            3, 6, 7,

            // Левая грань
            0, 1, 5,
            0, 5, 4
    };

    // Цвета вершин (RGBA)
    private float colors[] = {
            // Передняя грань - красная
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            // Задняя грань - синяя
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
    };

    // Шейдеры
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 aColor;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  vColor = aColor;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    public Cube() {
        // Инициализация буфера вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        // Инициализация буфера цветов
        ByteBuffer cb = ByteBuffer.allocateDirect(colors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        // Инициализация буфера индексов
        ByteBuffer ib = ByteBuffer.allocateDirect(drawOrder.length * 2);
        ib.order(ByteOrder.nativeOrder());
        indexBuffer = ib.asShortBuffer();
        indexBuffer.put(drawOrder);
        indexBuffer.position(0);

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

    public void draw(float[] mvpMatrix) {
        // Используем программу
        GLES20.glUseProgram(mProgram);

        // Получаем хендлы атрибутов
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int colorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Передаем матрицу преобразования
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        // Включаем массив вершин
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * 4, vertexBuffer);

        // Включаем массив цветов
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, 4,
                GLES20.GL_FLOAT, false, 4 * 4, colorBuffer);

        // Рисуем куб
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Отключаем массивы вершин
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}