package com.example.astronomyguide;

import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class Ring {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram;

    private int numVertices;
    private int numIndices;

    private float innerRadius;
    private float outerRadius;
    private float[] color;

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

    public Ring(float innerRadius, float outerRadius, int segments) {
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.color = new float[]{0.9f, 0.8f, 0.6f, 0.7f}; // Цвет по умолчанию (полупрозрачный)

        generateRing(segments);
        initShader();
    }

    private void generateRing(int segments) {
        List<Float> vertices = new ArrayList<>();
        List<Short> indices = new ArrayList<>();

        // Генерируем вершины для кольца
        for (int i = 0; i <= segments; i++) {
            float angle = (float)(2.0 * Math.PI * i / segments);
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);

            // Внешняя точка кольца
            vertices.add(cos * outerRadius);
            vertices.add(0.0f);
            vertices.add(sin * outerRadius);

            // Внутренняя точка кольца
            vertices.add(cos * innerRadius);
            vertices.add(0.0f);
            vertices.add(sin * innerRadius);
        }

        // Генерируем индексы для треугольников
        for (int i = 0; i < segments; i++) {
            int outer1 = i * 2;
            int inner1 = i * 2 + 1;
            int outer2 = (i + 1) * 2;
            int inner2 = (i + 1) * 2 + 1;

            // Первый треугольник
            indices.add((short)outer1);
            indices.add((short)inner1);
            indices.add((short)outer2);

            // Второй треугольник
            indices.add((short)outer2);
            indices.add((short)inner1);
            indices.add((short)inner2);
        }

        // Конвертируем списки в массивы
        numVertices = vertices.size() / 3;
        numIndices = indices.size();

        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }

        short[] indexArray = new short[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indexArray[i] = indices.get(i);
        }

        // Создаем буфер вершин
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexArray.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexArray);
        vertexBuffer.position(0);

        // Создаем буфер цветов
        float[] colorArray = new float[numVertices * 4];
        for (int i = 0; i < numVertices; i++) {
            colorArray[i * 4] = color[0];
            colorArray[i * 4 + 1] = color[1];
            colorArray[i * 4 + 2] = color[2];
            colorArray[i * 4 + 3] = color[3];
        }

        ByteBuffer cb = ByteBuffer.allocateDirect(colorArray.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colorArray);
        colorBuffer.position(0);

        // Создаем буфер индексов
        ByteBuffer ib = ByteBuffer.allocateDirect(indexArray.length * 2);
        ib.order(ByteOrder.nativeOrder());
        indexBuffer = ib.asShortBuffer();
        indexBuffer.put(indexArray);
        indexBuffer.position(0);
    }

    private void initShader() {
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
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;

        // Обновляем буфер цветов
        float[] colorArray = new float[numVertices * 4];
        for (int i = 0; i < numVertices; i++) {
            colorArray[i * 4] = color[0];
            colorArray[i * 4 + 1] = color[1];
            colorArray[i * 4 + 2] = color[2];
            colorArray[i * 4 + 3] = color[3];
        }

        colorBuffer.position(0);
        colorBuffer.put(colorArray);
        colorBuffer.position(0);
    }

    public void draw(float[] mvpMatrix) {
        // Включаем смешивание для полупрозрачности
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

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
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);

        // Включаем массив цветов
        GLES20.glEnableVertexAttribArray(colorHandle);
        GLES20.glVertexAttribPointer(colorHandle, 4,
                GLES20.GL_FLOAT, false, 4 * 4, colorBuffer);

        // Рисуем кольцо
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Отключаем массивы вершин
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);

        GLES20.glDisable(GLES20.GL_BLEND);
    }
}