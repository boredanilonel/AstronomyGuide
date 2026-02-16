package com.example.astronomyguide;

import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class TransparentCube {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram;

    static final int COORDS_PER_VERTEX = 3;

    private float cubeCoords[] = {
            -0.45f,  0.45f,  0.45f,
            -0.45f, -0.45f,  0.45f,
            0.45f, -0.45f,  0.45f,
            0.45f,  0.45f,  0.45f,

            -0.45f,  0.45f, -0.45f,
            -0.45f, -0.45f, -0.45f,
            0.45f, -0.45f, -0.45f,
            0.45f,  0.45f, -0.45f,
    };

    private short drawOrder[] = {
            0, 1, 2,
            0, 2, 3,

            4, 5, 6,
            4, 6, 7,

            0, 4, 7,
            0, 7, 3,

            1, 5, 6,
            1, 6, 2,

            3, 2, 6,
            3, 6, 7,

            0, 1, 5,
            0, 5, 4
    };

    // Цвета вершин для прозрачного куба
    private float colors[] = {
            0.5f, 0.8f, 1.0f, 0.3f,
            0.5f, 0.8f, 1.0f, 0.3f,
            0.5f, 0.8f, 1.0f, 0.3f,
            0.5f, 0.8f, 1.0f, 0.3f,

            0.5f, 0.8f, 1.0f, 0.3f,
            0.5f, 0.8f, 1.0f, 0.3f,
            0.5f, 0.8f, 1.0f, 0.3f,
            0.5f, 0.8f, 1.0f, 0.3f,
    };

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

    public TransparentCube() {
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
        int vertexShader = SolarSystemRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = SolarSystemRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void setColor(float r, float g, float b, float a) {
        // Обновляем цвет куба
        for (int i = 0; i < 8; i++) {
            colors[i * 4] = r;
            colors[i * 4 + 1] = g;
            colors[i * 4 + 2] = b;
            colors[i * 4 + 3] = a;
        }

        colorBuffer.position(0);
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }

    public void draw(float[] mvpMatrix) {
        // Включаем смешивание для прозрачности
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Используем программу
        GLES20.glUseProgram(mProgram);

        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int colorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, COORDS_PER_VERTEX * 4, vertexBuffer);

        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, 4,
                GLES20.GL_FLOAT, false, 4 * 4, colorBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);

        GLES20.glDisable(GLES20.GL_BLEND);
    }
}