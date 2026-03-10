package com.example.astronomyguide.lab7;

import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Sphere {

    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;
    private ShortBuffer indexBuffer;

    private int numIndices;

    public Sphere(float radius, int stacks, int slices) {
        generateSphere(radius, stacks, slices);
    }

    private void generateSphere(float radius, int stacks, int slices) {
        int verticesPerStack = slices + 1;
        int numVertices = (stacks + 1) * verticesPerStack;
        numIndices = stacks * slices * 6;

        float[] vertices = new float[numVertices * 3];
        float[] texCoords = new float[numVertices * 2];
        short[] indices = new short[numIndices];

        for (int i = 0; i <= stacks; i++) {
            float phi = (float)(Math.PI * i / stacks);
            float sinPhi = (float)Math.sin(phi);
            float cosPhi = (float)Math.cos(phi);

            for (int j = 0; j <= slices; j++) {
                float theta = (float)(2.0 * Math.PI * j / slices);
                float sinTheta = (float)Math.sin(theta);
                float cosTheta = (float)Math.cos(theta);

                int index = i * verticesPerStack + j;

                vertices[index * 3] = radius * cosTheta * sinPhi;
                vertices[index * 3 + 1] = radius * cosPhi;
                vertices[index * 3 + 2] = radius * sinTheta * sinPhi;

                texCoords[index * 2] = (float)j / slices * 4.0f;
                texCoords[index * 2 + 1] = (float)i / stacks * 4.0f;
            }
        }

        int idx = 0;
        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int a = i * verticesPerStack + j;
                int b = i * verticesPerStack + j + 1;
                int c = (i + 1) * verticesPerStack + j;
                int d = (i + 1) * verticesPerStack + j + 1;

                indices[idx++] = (short)a;
                indices[idx++] = (short)c;
                indices[idx++] = (short)b;

                indices[idx++] = (short)b;
                indices[idx++] = (short)c;
                indices[idx++] = (short)d;
            }
        }

        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer tb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tb.order(ByteOrder.nativeOrder());
        texCoordBuffer = tb.asFloatBuffer();
        texCoordBuffer.put(texCoords);
        texCoordBuffer.position(0);

        ByteBuffer ib = ByteBuffer.allocateDirect(indices.length * 2);
        ib.order(ByteOrder.nativeOrder());
        indexBuffer = ib.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public void draw(int positionHandle, int texCoordHandle) {
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false, 3 * 4, vertexBuffer);
        GLES20.glVertexAttribPointer(texCoordHandle, 2,
                GLES20.GL_FLOAT, false, 2 * 4, texCoordBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }
}