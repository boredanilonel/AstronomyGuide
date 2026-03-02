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
import java.util.ArrayList;
import java.util.List;

public class MoonRenderer implements GLSurfaceView.Renderer {

    private Context context;

    // Матрицы
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];
    private final float[] mvMatrix = new float[16];
    private final float[] normalMatrix = new float[16];

    // Объекты
    private MoonSphere moon;

    // Параметры освещения
    private float[] lightPosition = {5.0f, 5.0f, 5.0f, 1.0f}; // Позиция источника света
    private float[] lightAmbient = {0.2f, 0.2f, 0.2f, 1.0f};   // Фоновое освещение
    private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};   // Рассеянный свет
    private float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};  // Зеркальный свет

    // Материал Луны
    private float[] materialAmbient = {0.3f, 0.3f, 0.3f, 1.0f};
    private float[] materialDiffuse = {0.8f, 0.8f, 0.8f, 1.0f};
    private float[] materialSpecular = {0.5f, 0.5f, 0.5f, 1.0f};
    private float materialShininess = 32.0f;

    // Углы вращения
    private float rotationAngle = 0;

    public MoonRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Устанавливаем цвет очистки экрана (черный)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Включаем тест глубины
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        // Создаем Луну с высоким разрешением для красивого освещения
        moon = new MoonSphere(1.0f, 64, 64);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // Устанавливаем матрицу проекции (перспектива)
        Matrix.perspectiveM(projectionMatrix, 0, 45.0f, ratio, 1.0f, 100.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Очищаем экран и буфер глубины
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Устанавливаем позицию камеры
        Matrix.setLookAtM(viewMatrix, 0,
                0, 0, 5.0f,      // Позиция камеры
                0, 0, 0,         // Точка, на которую смотрим
                0, 1, 0);        // Направление вверх

        // Рисуем Луну
        drawMoon();

        // Обновляем угол вращения
        rotationAngle += 0.5f;
        if (rotationAngle >= 360) rotationAngle -= 360;
    }

    private void drawMoon() {
        // Вычисляем модельно-видовую матрицу
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, rotationAngle, 0.0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, modelMatrix, 0);

        // Вычисляем матрицу нормалей (транспонированная обратная от mvMatrix)
        Matrix.invertM(normalMatrix, 0, mvMatrix, 0);
        Matrix.transposeM(normalMatrix, 0, normalMatrix, 0);

        // Вычисляем MVP матрицу
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvMatrix, 0);

        // Рисуем Луну с освещением
        moon.draw(mvpMatrix, mvMatrix, normalMatrix,
                lightPosition, lightAmbient, lightDiffuse, lightSpecular,
                materialAmbient, materialDiffuse, materialSpecular, materialShininess);
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

    // Внутренний класс для сферы с поддержкой освещения Фонга
    private static class MoonSphere {

        private FloatBuffer vertexBuffer;
        private FloatBuffer normalBuffer;
        private ShortBuffer indexBuffer;
        private int mProgram;

        private int numIndices;

        // Шейдеры с моделью освещения Фонга
        private final String vertexShaderCode =
                "uniform mat4 uMVPMatrix;" +
                        "uniform mat4 uMVMatrix;" +
                        "uniform mat4 uNormalMatrix;" +
                        "uniform vec3 uLightPosition;" +
                        "uniform vec4 uLightAmbient;" +
                        "uniform vec4 uLightDiffuse;" +
                        "uniform vec4 uLightSpecular;" +
                        "uniform vec4 uMaterialAmbient;" +
                        "uniform vec4 uMaterialDiffuse;" +
                        "uniform vec4 uMaterialSpecular;" +
                        "uniform float uMaterialShininess;" +
                        "attribute vec4 vPosition;" +
                        "attribute vec3 vNormal;" +
                        "varying vec4 vColor;" +
                        "void main() {" +
                        "  vec3 position = vec3(uMVMatrix * vPosition);" +
                        "  vec3 normal = normalize(vec3(uNormalMatrix * vec4(vNormal, 0.0)));" +
                        "  vec3 lightDir = normalize(uLightPosition - position);" +
                        "  vec3 viewDir = normalize(-position);" +
                        "  vec3 reflectDir = reflect(-lightDir, normal);" +
                        "  float lambertian = max(dot(lightDir, normal), 0.0);" +
                        "  float specular = 0.0;" +
                        "  if (lambertian > 0.0) {" +
                        "    float specAngle = max(dot(reflectDir, viewDir), 0.0);" +
                        "    specular = pow(specAngle, uMaterialShininess);" +
                        "  }" +
                        "  vec4 ambient = uLightAmbient * uMaterialAmbient;" +
                        "  vec4 diffuse = uLightDiffuse * uMaterialDiffuse * lambertian;" +
                        "  vec4 spec = uLightSpecular * uMaterialSpecular * specular;" +
                        "  vColor = ambient + diffuse + spec;" +
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}";

        private final String fragmentShaderCode =
                "precision mediump float;" +
                        "varying vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";

        public MoonSphere(float radius, int stacks, int slices) {
            generateSphere(radius, stacks, slices);
            initShader();
        }

        private void generateSphere(float radius, int stacks, int slices) {
            List<Float> vertices = new ArrayList<>();
            List<Float> normals = new ArrayList<>();
            List<Short> indices = new ArrayList<>();

            // Генерируем вершины и нормали
            for (int i = 0; i <= stacks; i++) {
                float phi = (float)(Math.PI * i / stacks);
                float sinPhi = (float)Math.sin(phi);
                float cosPhi = (float)Math.cos(phi);

                for (int j = 0; j <= slices; j++) {
                    float theta = (float)(2.0 * Math.PI * j / slices);
                    float sinTheta = (float)Math.sin(theta);
                    float cosTheta = (float)Math.cos(theta);

                    // Координаты вершины
                    float x = radius * cosTheta * sinPhi;
                    float y = radius * cosPhi;
                    float z = radius * sinTheta * sinPhi;

                    vertices.add(x);
                    vertices.add(y);
                    vertices.add(z);

                    // Нормаль (нормализованный вектор от центра к точке)
                    float length = (float)Math.sqrt(x*x + y*y + z*z);
                    normals.add(x / length);
                    normals.add(y / length);
                    normals.add(z / length);
                }
            }

            // Генерируем индексы
            for (int i = 0; i < stacks; i++) {
                for (int j = 0; j < slices; j++) {
                    int first = (i * (slices + 1)) + j;
                    int second = first + slices + 1;

                    // Первый треугольник
                    indices.add((short)first);
                    indices.add((short)second);
                    indices.add((short)(first + 1));

                    // Второй треугольник
                    indices.add((short)(first + 1));
                    indices.add((short)second);
                    indices.add((short)(second + 1));
                }
            }

            // Конвертируем списки в массивы
            numIndices = indices.size();

            float[] vertexArray = new float[vertices.size()];
            for (int i = 0; i < vertices.size(); i++) {
                vertexArray[i] = vertices.get(i);
            }

            float[] normalArray = new float[normals.size()];
            for (int i = 0; i < normals.size(); i++) {
                normalArray[i] = normals.get(i);
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

            // Создаем буфер нормалей
            ByteBuffer nb = ByteBuffer.allocateDirect(normalArray.length * 4);
            nb.order(ByteOrder.nativeOrder());
            normalBuffer = nb.asFloatBuffer();
            normalBuffer.put(normalArray);
            normalBuffer.position(0);

            // Создаем буфер индексов
            ByteBuffer ib = ByteBuffer.allocateDirect(indexArray.length * 2);
            ib.order(ByteOrder.nativeOrder());
            indexBuffer = ib.asShortBuffer();
            indexBuffer.put(indexArray);
            indexBuffer.position(0);
        }

        private void initShader() {
            int vertexShader = MoonRenderer.loadShader(
                    GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = MoonRenderer.loadShader(
                    GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

            mProgram = GLES20.glCreateProgram();
            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);
            GLES20.glLinkProgram(mProgram);
        }

        public void draw(float[] mvpMatrix, float[] mvMatrix, float[] normalMatrix,
                         float[] lightPosition, float[] lightAmbient, float[] lightDiffuse, float[] lightSpecular,
                         float[] materialAmbient, float[] materialDiffuse, float[] materialSpecular, float materialShininess) {

            GLES20.glUseProgram(mProgram);

            // Получаем хендлы атрибутов и uniform-переменных
            int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            int normalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");
            int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            int mvMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");
            int normalMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uNormalMatrix");
            int lightPosHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition");
            int lightAmbientHandle = GLES20.glGetUniformLocation(mProgram, "uLightAmbient");
            int lightDiffuseHandle = GLES20.glGetUniformLocation(mProgram, "uLightDiffuse");
            int lightSpecularHandle = GLES20.glGetUniformLocation(mProgram, "uLightSpecular");
            int materialAmbientHandle = GLES20.glGetUniformLocation(mProgram, "uMaterialAmbient");
            int materialDiffuseHandle = GLES20.glGetUniformLocation(mProgram, "uMaterialDiffuse");
            int materialSpecularHandle = GLES20.glGetUniformLocation(mProgram, "uMaterialSpecular");
            int materialShininessHandle = GLES20.glGetUniformLocation(mProgram, "uMaterialShininess");

            // Передаем матрицы
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
            GLES20.glUniformMatrix4fv(mvMatrixHandle, 1, false, mvMatrix, 0);
            GLES20.glUniformMatrix4fv(normalMatrixHandle, 1, false, normalMatrix, 0);

            // Передаем параметры освещения
            GLES20.glUniform3f(lightPosHandle, lightPosition[0], lightPosition[1], lightPosition[2]);
            GLES20.glUniform4fv(lightAmbientHandle, 1, lightAmbient, 0);
            GLES20.glUniform4fv(lightDiffuseHandle, 1, lightDiffuse, 0);
            GLES20.glUniform4fv(lightSpecularHandle, 1, lightSpecular, 0);
            GLES20.glUniform4fv(materialAmbientHandle, 1, materialAmbient, 0);
            GLES20.glUniform4fv(materialDiffuseHandle, 1, materialDiffuse, 0);
            GLES20.glUniform4fv(materialSpecularHandle, 1, materialSpecular, 0);
            GLES20.glUniform1f(materialShininessHandle, materialShininess);

            // Включаем массивы
            GLES20.glEnableVertexAttribArray(positionHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);

            // Устанавливаем указатели на буферы
            GLES20.glVertexAttribPointer(positionHandle, 3,
                    GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
            GLES20.glVertexAttribPointer(normalHandle, 3,
                    GLES20.GL_FLOAT, false, 3 * 4, normalBuffer);

            // Рисуем сферу
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices,
                    GLES20.GL_UNSIGNED_SHORT, indexBuffer);

            // Отключаем массивы
            GLES20.glDisableVertexAttribArray(positionHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
        }
    }
}