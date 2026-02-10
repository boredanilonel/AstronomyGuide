package com.example.astronomyguide;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private Context context;

    // Матрицы проекции и вида
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];

    // Квадрат (фоновая текстура)
    private Square square;

    // Куб
    private Cube cube;

    // Углы вращения
    private float angleCube = 0;

    public OpenGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Устанавливаем цвет очистки экрана (темно-синий как запасной фон)
        GLES20.glClearColor(0.0f, 0.0f, 0.1f, 1.0f);

        // Включаем тест глубины
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        // Включаем смешивание цветов
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Создаем квадрат и куб
        square = new Square();
        cube = new Cube();

        // Инициализация текстур
        square.loadTexture(context, R.drawable.galaxy_texture);

        // Инициализируем матрицу модели
        Matrix.setIdentityM(modelMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // Устанавливаем матрицу проекции (перспектива)
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 100);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Очищаем экран и буфер глубины
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Устанавливаем позицию камеры
        Matrix.setLookAtM(viewMatrix, 0,
                0, 0, 3,      // Позиция камеры (x, y, z) - ближе к сцене
                0, 0, 0,      // Точка, на которую смотрим (x, y, z)
                0, 1, 0);     // Направление вверх (вектор)

        // 1. Сначала рисуем фон (квадрат с текстурой)
        // Фон должен быть далеко позади всего
        float[] backgroundMatrix = new float[16];
        Matrix.multiplyMM(backgroundMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Отодвигаем и увеличиваем фон
        Matrix.translateM(backgroundMatrix, 0, 0, 0, -20f); // Очень далеко
        Matrix.scaleM(backgroundMatrix, 0, 15.0f, 15.0f, 1.0f); // Увеличиваем

        // Отключаем тест глубины для фона, чтобы он всегда был сзади
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        square.draw(backgroundMatrix);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // 2. Затем рисуем куб
        float[] cubeMVPMatrix = new float[16];
        Matrix.multiplyMM(cubeMVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // Преобразования для куба
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0, 0, -2f); // Немного ближе к камере

        // Вращаем куб
        Matrix.rotateM(modelMatrix, 0, angleCube, 1.0f, 1.0f, 0.5f);

        // Объединяем матрицу модели с MVP
        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, cubeMVPMatrix, 0, modelMatrix, 0);

        cube.draw(tempMatrix);

        // Обновляем углы вращения
        angleCube += 0.8f;
        if (angleCube >= 360) {
            angleCube -= 360;
        }
    }

    // Утилитарный метод для загрузки шейдера
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        // Проверяем компиляцию
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