package com.example.astronomyguide.lab8;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.example.astronomyguide.lab3.SolarSystemRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class BlackHole {

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer indexBuffer;
    private int mProgram;

    private int numIndices;

    private float[] position = new float[3];
    private float rotationAngle = 0;
    private float pulseScale = 1.0f;
    private boolean pulseIncreasing = true;
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 aColor;" +
                    "varying vec4 vColor;" +
                    "varying float vDistance;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  vColor = aColor;" +
                    "  vDistance = length(vPosition.xyz);" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 vColor;" +
                    "varying float vDistance;" +
                    "void main() {" +
                    "  float glow = 1.0 - vDistance * 0.3;" +
                    "  if (glow < 0.0) glow = 0.0;" +
                    "  gl_FragColor = vec4(vColor.rgb * (0.5 + glow * 0.5), vColor.a);" +
                    "}";

    public BlackHole() {
        generateBlackHole();
        initShader();
    }

    private void generateBlackHole() {

        List<Float> vertices = new ArrayList<>();
        List<Float> colors = new ArrayList<>();
        List<Short> indices = new ArrayList<>();

        generateSphere(vertices, colors, indices, 0.8f, 0, 0, 0, 16, 16,
                new float[]{0.0f, 0.0f, 0.0f, 1.0f});

        generateRing(vertices, colors, indices, 1.0f, 1.5f, 32,
                new float[]{1.0f, 0.5f, 0.0f, 0.3f});

        generateRing(vertices, colors, indices, 1.6f, 2.0f, 32,
                new float[]{0.8f, 0.3f, 0.0f, 0.2f});

        generateRays(vertices, colors, indices, 2.5f, 16,
                new float[]{1.0f, 0.8f, 0.5f, 0.1f});

        numIndices = indices.size();

        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }

        float[] colorArray = new float[colors.size()];
        for (int i = 0; i < colors.size(); i++) {
            colorArray[i] = colors.get(i);
        }

        short[] indexArray = new short[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indexArray[i] = indices.get(i);
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexArray.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexArray);
        vertexBuffer.position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(colorArray.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colorArray);
        colorBuffer.position(0);

        ByteBuffer ib = ByteBuffer.allocateDirect(indexArray.length * 2);
        ib.order(ByteOrder.nativeOrder());
        indexBuffer = ib.asShortBuffer();
        indexBuffer.put(indexArray);
        indexBuffer.position(0);
    }

    private void generateSphere(List<Float> vertices, List<Float> colors, List<Short> indices,
                                float radius, float cx, float cy, float cz,
                                int stacks, int slices, float[] color) {
        int startIndex = vertices.size() / 3;

        for (int i = 0; i <= stacks; i++) {
            float phi = (float)(Math.PI * i / stacks);
            float sinPhi = (float)Math.sin(phi);
            float cosPhi = (float)Math.cos(phi);

            for (int j = 0; j <= slices; j++) {
                float theta = (float)(2.0 * Math.PI * j / slices);
                float sinTheta = (float)Math.sin(theta);
                float cosTheta = (float)Math.cos(theta);

                float x = cx + radius * cosTheta * sinPhi;
                float y = cy + radius * cosPhi;
                float z = cz + radius * sinTheta * sinPhi;

                vertices.add(x);
                vertices.add(y);
                vertices.add(z);

                colors.add(color[0]);
                colors.add(color[1]);
                colors.add(color[2]);
                colors.add(color[3]);
            }
        }

        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int first = startIndex + (i * (slices + 1)) + j;
                int second = startIndex + (i + 1) * (slices + 1) + j;

                indices.add((short)first);
                indices.add((short)second);
                indices.add((short)(first + 1));

                indices.add((short)(first + 1));
                indices.add((short)second);
                indices.add((short)(second + 1));
            }
        }
    }

    private void generateRing(List<Float> vertices, List<Float> colors, List<Short> indices,
                              float innerRadius, float outerRadius, int segments, float[] color) {
        int startIndex = vertices.size() / 3;

        for (int i = 0; i <= segments; i++) {
            float angle = (float)(2.0 * Math.PI * i / segments);
            float cos = (float)Math.cos(angle);
            float sin = (float)Math.sin(angle);

            vertices.add(cos * outerRadius);
            vertices.add(0.0f);
            vertices.add(sin * outerRadius);

            colors.add(color[0]);
            colors.add(color[1]);
            colors.add(color[2]);
            colors.add(color[3]);

            vertices.add(cos * innerRadius);
            vertices.add(0.0f);
            vertices.add(sin * innerRadius);

            colors.add(color[0]);
            colors.add(color[1]);
            colors.add(color[2]);
            colors.add(color[3]);
        }

        for (int i = 0; i < segments; i++) {
            int outer1 = startIndex + i * 2;
            int inner1 = startIndex + i * 2 + 1;
            int outer2 = startIndex + (i + 1) * 2;
            int inner2 = startIndex + (i + 1) * 2 + 1;

            indices.add((short)outer1);
            indices.add((short)inner1);
            indices.add((short)outer2);

            indices.add((short)outer2);
            indices.add((short)inner1);
            indices.add((short)inner2);
        }
    }

    private void generateRays(List<Float> vertices, List<Float> colors, List<Short> indices,
                              float radius, int numRays, float[] color) {
        int startIndex = vertices.size() / 3;

        for (int i = 0; i < numRays; i++) {
            float angle1 = (float)(2.0 * Math.PI * i / numRays);
            float angle2 = (float)(2.0 * Math.PI * (i + 1) / numRays);

            float cos1 = (float)Math.cos(angle1);
            float sin1 = (float)Math.sin(angle1);
            float cos2 = (float)Math.cos(angle2);
            float sin2 = (float)Math.sin(angle2);

            vertices.add(cos1 * radius);
            vertices.add(0.0f);
            vertices.add(sin1 * radius);

            colors.add(color[0]);
            colors.add(color[1]);
            colors.add(color[2]);
            colors.add(color[3]);

            vertices.add(cos2 * radius);
            vertices.add(0.0f);
            vertices.add(sin2 * radius);

            colors.add(color[0]);
            colors.add(color[1]);
            colors.add(color[2]);
            colors.add(color[3]);

            vertices.add(0.0f);
            vertices.add(0.0f);
            vertices.add(0.0f);

            colors.add(color[0] * 0.5f);
            colors.add(color[1] * 0.5f);
            colors.add(color[2] * 0.5f);
            colors.add(color[3] * 0.5f);
        }

        for (int i = 0; i < numRays; i++) {
            int baseIndex = startIndex + i * 3;
            indices.add((short)baseIndex);
            indices.add((short)(baseIndex + 1));
            indices.add((short)(baseIndex + 2));
        }
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

    public void updatePosition(float x, float y, float z) {
        position[0] = x;
        position[1] = y;
        position[2] = z;
    }

    public void updateAnimation() {
        rotationAngle += 0.5f;
        if (rotationAngle >= 360) rotationAngle -= 360;

        if (pulseIncreasing) {
            pulseScale += 0.01f;
            if (pulseScale >= 1.2f) pulseIncreasing = false;
        } else {
            pulseScale -= 0.01f;
            if (pulseScale <= 0.8f) pulseIncreasing = true;
        }
    }

    public void draw(float[] projMatrix, float[] viewMatrix) {
        GLES20.glUseProgram(mProgram);

        float[] modelMatrix = new float[16];
        float[] mvpMatrix = new float[16];

        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, position[0], position[1], position[2]);
        Matrix.scaleM(modelMatrix, 0, pulseScale, pulseScale, pulseScale);
        Matrix.rotateM(modelMatrix, 0, rotationAngle, 0.0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0);

        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int colorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        int mvpMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(colorHandle);

        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
        GLES20.glVertexAttribPointer(colorHandle, 4,
                GLES20.GL_FLOAT, false, 4 * 4, colorBuffer);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisable(GLES20.GL_BLEND);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(colorHandle);
    }
}