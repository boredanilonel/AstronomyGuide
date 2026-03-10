package com.example.astronomyguide.lab2;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.astronomyguide.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private final float[] projectionMatrix = new float[16];
    private final float[] viewMatrix = new float[16];
    private final float[] modelMatrix = new float[16];
    private final float[] mvpMatrix = new float[16];
    private Square square;
    private Cube cube;

    private float angleCube = 0;

    public OpenGLRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.1f, 1.0f);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        square = new Square();
        cube = new Cube();

        square.loadTexture(context, R.drawable.galaxy_texture);

        Matrix.setIdentityM(modelMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 100);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(viewMatrix, 0,
                0, 0, 3,
                0, 0, 0,
                0, 1, 0);

        float[] backgroundMatrix = new float[16];
        Matrix.multiplyMM(backgroundMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.translateM(backgroundMatrix, 0, 0, 0, -20f);
        Matrix.scaleM(backgroundMatrix, 0, 15.0f, 15.0f, 1.0f);

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        square.draw(backgroundMatrix);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        float[] cubeMVPMatrix = new float[16];
        Matrix.multiplyMM(cubeMVPMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, 0, 0, -2f); // Немного ближе к камере

        Matrix.rotateM(modelMatrix, 0, angleCube, 1.0f, 1.0f, 0.5f);

        float[] tempMatrix = new float[16];
        Matrix.multiplyMM(tempMatrix, 0, cubeMVPMatrix, 0, modelMatrix, 0);

        cube.draw(tempMatrix);

        angleCube += 0.8f;
        if (angleCube >= 360) {
            angleCube -= 360;
        }
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