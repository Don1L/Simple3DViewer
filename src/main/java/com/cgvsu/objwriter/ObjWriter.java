package com.cgvsu.objwriter;

import com.cgvsu.model.Model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Polygon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ObjWriter {

    public static void write(Model model, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Запись вершин
            for (Vector3f vertex : model.vertices) {
                writer.write("v " + vertex.x + " " + vertex.y + " " + vertex.z + "\n");
            }

            // Запись текстурных вершин (если они есть)
            if (!model.textureVertices.isEmpty()) {
                for (Vector2f textureVertex : model.textureVertices) {
                    writer.write("vt " + textureVertex.x + " " + textureVertex.y + "\n");
                }
            }

            // Запись нормалей (если они есть)
            if (!model.normals.isEmpty()) {
                for (Vector3f normal : model.normals) {
                    writer.write("vn " + normal.x + " " + normal.y + " " + normal.z + "\n");
                }
            }

            // Запись полигонов
            for (Polygon polygon : model.polygons) {
                writer.write("f ");

                for (int i = 0; i < polygon.getVertexIndices().size(); i++) {
                    int vertexIndex = polygon.getVertexIndices().get(i) + 1; // Индексы в OBJ начинаются с 1

                    // Если есть текстурные вершины, добавляем их
                    if (!polygon.getTextureVertexIndices().isEmpty()) {
                        int textureIndex = polygon.getTextureVertexIndices().get(i) + 1;

                        // Если есть нормали, добавляем их
                        if (!polygon.getNormalIndices().isEmpty()) {
                            int normalIndex = polygon.getNormalIndices().get(i) + 1;
                            writer.write(vertexIndex + "/" + textureIndex + "/" + normalIndex + " ");
                        } else {
                            writer.write(vertexIndex + "/" + textureIndex + " ");
                        }
                    } else {
                        // Если нет текстурных вершин, но есть нормали
                        if (!polygon.getNormalIndices().isEmpty()) {
                            int normalIndex = polygon.getNormalIndices().get(i) + 1;
                            writer.write(vertexIndex + "//" + normalIndex + " ");
                        } else {
                            // Если нет ни текстурных вершин, ни нормалей
                            writer.write(vertexIndex + " ");
                        }
                    }
                }

                writer.write("\n");
            }
        }
    }
}