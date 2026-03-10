package com.example.astronomyguide.lab7;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NeptuneRenderer implements GLSurfaceView.Renderer {

    private Context context;

    private Sphere neptuneSphere;
    private int waterTextureId;

    private final float[] modelMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] projectionMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];

    private int program;
    private int positionHandle;
    private int texCoordHandle;
    private int mvpMatrixHandle;
    private int timeHandle;
    private int textureUniformHandle;

    private long startTime = System.currentTimeMillis();

    public NeptuneRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, getVertexShader());
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, getFragmentShader());

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        timeHandle = GLES20.glGetUniformLocation(program, "uTime");
        textureUniformHandle = GLES20.glGetUniformLocation(program, "uTexture");

        neptuneSphere = new Sphere(0.7f, 180, 180);

        waterTextureId = createDetailedWaterTexture();
    }

    private int createDetailedWaterTexture() {
        int size = 1024;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                float u = (float) x / size;
                float v = (float) y / size;

                float baseWave1 = (float)(Math.sin(u * 10.0f * Math.PI) * Math.cos(v * 8.0f * Math.PI));
                float baseWave2 = (float)(Math.sin(u * 15.0f * Math.PI + 2.0f) * Math.cos(v * 12.0f * Math.PI + 1.0f));

                float ripple1 = (float)(Math.sin(u * 40.0f * Math.PI) * Math.sin(v * 35.0f * Math.PI));
                float ripple2 = (float)(Math.cos(u * 50.0f * Math.PI + 3.0f) * Math.cos(v * 45.0f * Math.PI + 2.0f));

                float microRipple1 = (float)(Math.sin(u * 120.0f * Math.PI + v * 80.0f * Math.PI));
                float microRipple2 = (float)(Math.cos(u * 150.0f * Math.PI - v * 130.0f * Math.PI));

                float centerX1 = 0.3f;
                float centerY1 = 0.7f;
                float dist1 = (float)Math.sqrt(Math.pow(u - centerX1, 2) + Math.pow(v - centerY1, 2));
                float circular1 = (float)(Math.sin(dist1 * 30.0f * Math.PI) * Math.exp(-dist1 * 5.0f));

                float centerX2 = 0.7f;
                float centerY2 = 0.3f;
                float dist2 = (float)Math.sqrt(Math.pow(u - centerX2, 2) + Math.pow(v - centerY2, 2));
                float circular2 = (float)(Math.cos(dist2 * 25.0f * Math.PI) * Math.exp(-dist2 * 4.0f));

                float turbulence = (float)(Math.sin(u * 200.0f * Math.PI + v * 150.0f * Math.PI) *
                        Math.cos(v * 180.0f * Math.PI - u * 160.0f * Math.PI));

                float combinedWave = (
                        baseWave1 * 0.4f +
                                baseWave2 * 0.3f +
                                ripple1 * 0.2f +
                                ripple2 * 0.15f +
                                microRipple1 * 0.1f +
                                microRipple2 * 0.1f +
                                circular1 * 0.25f +
                                circular2 * 0.2f +
                                turbulence * 0.08f
                ) * 0.5f + 0.5f;

                float depthFactor = 1.0f - (float)Math.sqrt(Math.pow(u - 0.5f, 2) + Math.pow(v - 0.5f, 2)) * 1.2f;
                float finalHeight = Math.min(Math.max(combinedWave * 0.7f + depthFactor * 0.3f, 0.0f), 1.0f);

                float red = Math.min(Math.max(0.05f + finalHeight * 0.15f + (float)Math.sin(finalHeight * 10.0f) * 0.03f, 0.0f), 1.0f);
                float green = Math.min(Math.max(0.2f + finalHeight * 0.4f + (float)Math.cos(finalHeight * 8.0f) * 0.05f, 0.0f), 1.0f);
                float blue = Math.min(Math.max(0.6f + finalHeight * 0.4f + (float)Math.sin(finalHeight * 12.0f) * 0.07f, 0.0f), 1.0f);

                float finalRed = red;
                float finalGreen = green;
                float finalBlue = blue;

                if (finalHeight > 0.8f) {
                    float foam = (finalHeight - 0.8f) * 5.0f;
                    finalRed = Math.min(red + foam * 0.5f, 1.0f);
                    finalGreen = Math.min(green + foam * 0.5f, 1.0f);
                    finalBlue = Math.min(blue + foam, 1.0f);
                }

                int color = Color.rgb(
                        (int)(finalRed * 255),
                        (int)(finalGreen * 255),
                        (int)(finalBlue * 255)
                );
                bitmap.setPixel(x, y, color);
            }
        }

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        bitmap.recycle();

        return textures[0];
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float aspectRatio = (height > 0) ? (float) width / height : 1.0f;
        Matrix.perspectiveM(projectionMatrix, 0, 45.0f, aspectRatio, 1.0f, 100.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(program);

        float time = (float)(System.currentTimeMillis() - startTime) / 1000.0f;

        Matrix.setLookAtM(viewMatrix, 0,
                1.2f, 0.8f, 2.8f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, time * 8.0f, 0.0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0);

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniform1f(timeHandle, time);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, waterTextureId);
        GLES20.glUniform1i(textureUniformHandle, 0);

        neptuneSphere.draw(positionHandle, texCoordHandle);
    }

    private int loadShader(int type, String shaderCode) {
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

    private String getVertexShader() {
        return
                "attribute vec3 aPosition;\n" +
                        "attribute vec2 aTexCoord;\n" +
                        "uniform mat4 uMVPMatrix;\n" +
                        "varying vec2 vTexCoord;\n" +
                        "varying vec3 vPosition;\n" +
                        "void main() {\n" +
                        "    gl_Position = uMVPMatrix * vec4(aPosition, 1.0);\n" +
                        "    vTexCoord = aTexCoord;\n" +
                        "    vPosition = aPosition;\n" +
                        "}";
    }

    private String getFragmentShader() {
        return
                "precision highp float;\n" +
                        "varying vec2 vTexCoord;\n" +
                        "varying vec3 vPosition;\n" +
                        "uniform sampler2D uTexture;\n" +
                        "uniform float uTime;\n" +
                        "\n" +
                        "void main() {\n" +
                        "    // Более сложное искажение текстурных координат для эффекта движущихся волн\n" +
                        "    vec2 coord = vTexCoord;\n" +
                        "    \n" +
                        "    // Множество слоев искажения с разными частотами\n" +
                        "    float distortion1 = sin(coord.y * 25.0 + uTime * 3.0) * 0.03;\n" +
                        "    float distortion2 = cos(coord.x * 30.0 - uTime * 2.5) * 0.02;\n" +
                        "    float distortion3 = sin(coord.x * 60.0 + coord.y * 40.0 + uTime * 5.0) * 0.015;\n" +
                        "    float distortion4 = cos(coord.x * 100.0 - coord.y * 80.0 + uTime * 4.0) * 0.01;\n" +
                        "    \n" +
                        "    coord.x += distortion1 + distortion2 + distortion3;\n" +
                        "    coord.y += distortion2 + distortion3 + distortion4;\n" +
                        "    \n" +
                        "    // Получаем цвет из текстуры с искаженными координатами\n" +
                        "    vec4 color = texture2D(uTexture, coord);\n" +
                        "    \n" +
                        "    // Улучшенное освещение (учет позиции источника света)\n" +
                        "    vec3 normal = normalize(vPosition);\n" +
                        "    vec3 lightDir = normalize(vec3(0.5, 1.0, 0.5));  // Направление света\n" +
                        "    float diff = max(dot(normal, lightDir), 0.2);\n" +
                        "    \n" +
                        "    // Добавляем блеск на освещенной стороне\n" +
                        "    float specular = pow(max(dot(normal, lightDir), 0.0), 32.0) * 0.3;\n" +
                        "    \n" +
                        "    color.rgb *= (diff + specular);\n" +
                        "    \n" +
                        "    gl_FragColor = color;\n" +
                        "}";
    }
}