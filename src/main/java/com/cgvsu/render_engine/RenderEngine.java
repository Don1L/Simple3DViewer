package com.cgvsu.render_engine;

import java.util.ArrayList;

import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import javax.vecmath.*;
import com.cgvsu.model.Model;
import static com.cgvsu.render_engine.GraphicConveyor.*;

public class RenderEngine {

    public static void render(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height) {

        // Матрица модели (пока что единичная, но может быть изменена для трансформаций)
        Matrix4f modelMatrix = rotateScaleTranslate();

        // Матрица вида (из камеры)
        Matrix4f viewMatrix = camera.getViewMatrix();

        // Матрица проекции (из камеры)
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        // Комбинируем матрицы
        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        // Отрисовка полигонов модели
        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                // Получаем вершину
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                // Преобразуем вершину в экранные координаты
                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(vertex.x, vertex.y, vertex.z);
                Point2f resultPoint = vertexToPoint(multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath), width, height);
                resultPoints.add(resultPoint);
            }

            // Отрисовка линий между вершинами полигона
            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).x,
                        resultPoints.get(vertexInPolygonInd - 1).y,
                        resultPoints.get(vertexInPolygonInd).x,
                        resultPoints.get(vertexInPolygonInd).y);
            }

            // Замыкание полигона (линия от последней вершины к первой)
            if (nVerticesInPolygon > 0) {
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).x,
                        resultPoints.get(nVerticesInPolygon - 1).y,
                        resultPoints.get(0).x,
                        resultPoints.get(0).y);
            }
        }
    }

    // Метод для создания единичной матрицы модели
    private static Matrix4f rotateScaleTranslate() {
        float[] matrix = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    // Метод для преобразования вершины с помощью матрицы
    private static javax.vecmath.Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final javax.vecmath.Vector3f vertex) {
        final float x = (vertex.x * matrix.m00) + (vertex.y * matrix.m10) + (vertex.z * matrix.m20) + matrix.m30;
        final float y = (vertex.x * matrix.m01) + (vertex.y * matrix.m11) + (vertex.z * matrix.m21) + matrix.m31;
        final float z = (vertex.x * matrix.m02) + (vertex.y * matrix.m12) + (vertex.z * matrix.m22) + matrix.m32;
        final float w = (vertex.x * matrix.m03) + (vertex.y * matrix.m13) + (vertex.z * matrix.m23) + matrix.m33;
        return new javax.vecmath.Vector3f(x / w, y / w, z / w);
    }

    // Метод для преобразования вершины в экранные координаты
    private static Point2f vertexToPoint(final javax.vecmath.Vector3f vertex, final int width, final int height) {
        return new Point2f(vertex.x * width + width / 2.0F, -vertex.y * height + height / 2.0F);
    }
}