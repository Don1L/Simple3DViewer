package com.cgvsu.objreader;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ObjReader {

	// Константы для токенов, используемых в формате OBJ
	private static final String OBJ_VERTEX_TOKEN = "v";
	private static final String OBJ_TEXTURE_TOKEN = "vt";
	private static final String OBJ_NORMAL_TOKEN = "vn";
	private static final String OBJ_FACE_TOKEN = "f";

	// Основной метод для чтения и парсинга файла OBJ
	public static Model read(String fileContent) {
		Model result = new Model();

		int lineInd = 0;
		Scanner scanner = new Scanner(fileContent);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			ArrayList<String> wordsInLine = new ArrayList<>(Arrays.asList(line.split("\\s+")));
			if (wordsInLine.isEmpty()) {
				continue;
			}

			final String token = wordsInLine.get(0);
			wordsInLine.remove(0);

			++lineInd;
			switch (token) {
				// Парсинг вершин
				case OBJ_VERTEX_TOKEN -> result.vertices.add(parseVertex(wordsInLine, lineInd));
				// Парсинг текстурных вершин
				case OBJ_TEXTURE_TOKEN -> result.textureVertices.add(parseTextureVertex(wordsInLine, lineInd));
				// Парсинг нормалей
				case OBJ_NORMAL_TOKEN -> result.normals.add(parseNormal(wordsInLine, lineInd));
				// Парсинг полигонов
				case OBJ_FACE_TOKEN -> {
					result.polygons.add(parseFace(wordsInLine, lineInd));
					// Проверка на соответствие текстурных вершин в полигонах
					if (result.polygons.size() > 1 &&
							(result.polygons.get(result.polygons.size() - 2).getTextureVertexIndices().size() == 0) !=
									(result.polygons.get(result.polygons.size() - 1).getTextureVertexIndices().size() == 0)) {
						throw new ObjReaderException("Polygon has no texture vertices.", lineInd);
					}
				}
				default -> {}
			}
		}

		// Проверка на наличие хотя бы одного полигона
		if (result.polygons.isEmpty()) {
			throw new ObjReaderException("OBJ file has no polygons.", lineInd);
		}

		// Проверка на наличие хотя бы одной вершины
		if (result.vertices.isEmpty()) {
			throw new ObjReaderException("OBJ file has no vertices.", lineInd);
		}

		// Проверка индексов вершин, нормалей и текстурных вершин в полигонах
		for (Polygon polygon : result.polygons) {
			validateIndices(polygon.getVertexIndices(), result.vertices.size(), OBJ_VERTEX_TOKEN, lineInd);
			validateIndices(polygon.getNormalIndices(), result.normals.size(), OBJ_NORMAL_TOKEN, lineInd);
			validateIndices(polygon.getTextureVertexIndices(), result.textureVertices.size(), OBJ_TEXTURE_TOKEN, lineInd);
		}

		return result;
	}

	// Метод для парсинга вершин
	protected static Vector3f parseVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			if (wordsInLineWithoutToken.size() > 3) {
				throw new ObjReaderException("Too many vertex arguments.", lineInd);
			}

			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few vertex arguments.", lineInd);
		}
	}

	// Метод для парсинга текстурных вершин
	protected static Vector2f parseTextureVertex(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			if (wordsInLineWithoutToken.size() > 2) {
				throw new ObjReaderException("Too many texture vertex arguments.", lineInd);
			}
			return new Vector2f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)));

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few texture vertex arguments.", lineInd);
		}
	}

	// Метод для парсинга нормалей
	protected static Vector3f parseNormal(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		try {
			if (wordsInLineWithoutToken.size() > 3) {
				throw new ObjReaderException("Too many normal arguments.", lineInd);
			}
			return new Vector3f(
					Float.parseFloat(wordsInLineWithoutToken.get(0)),
					Float.parseFloat(wordsInLineWithoutToken.get(1)),
					Float.parseFloat(wordsInLineWithoutToken.get(2)));

		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse float value.", lineInd);
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few normal arguments.", lineInd);
		}
	}

	// Метод для парсинга полигонов
	protected static Polygon parseFace(final ArrayList<String> wordsInLineWithoutToken, int lineInd) {
		if (wordsInLineWithoutToken.size() < 3) {
			throw new ObjReaderException("Polygon has too few vertices.", lineInd);
		}

		ArrayList<Integer> vertexIndices = new ArrayList<>();
		ArrayList<Integer> textureVertexIndices = new ArrayList<>();
		ArrayList<Integer> normalIndices = new ArrayList<>();

		for (String s : wordsInLineWithoutToken) {
			parseFaceWord(s, vertexIndices, textureVertexIndices, normalIndices, lineInd);
		}

		if (hasDuplicates(vertexIndices)) {
			throw new ObjReaderException("The polygon contains duplicate vertex indices.", lineInd);
		}

		Polygon polygon = new Polygon();
		polygon.setVertexIndices(vertexIndices);
		polygon.setTextureVertexIndices(textureVertexIndices);
		polygon.setNormalIndices(normalIndices);
		return polygon;
	}

	// Метод для парсинга отдельных слов в строках полигонов
	protected static void parseFaceWord(
			String wordInLine,
			ArrayList<Integer> vertexIndices,
			ArrayList<Integer> textureVertexIndices,
			ArrayList<Integer> normalIndices,
			int lineInd) {
		try {
			String[] wordIndices = wordInLine.split("/");
			switch (wordIndices.length) {
				case 1 -> vertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
				case 2 -> {
					vertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
					textureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
				}
				case 3 -> {
					vertexIndices.add(Integer.parseInt(wordIndices[0]) - 1);
					normalIndices.add(Integer.parseInt(wordIndices[2]) - 1);
					if (!wordIndices[1].isEmpty()) {
						textureVertexIndices.add(Integer.parseInt(wordIndices[1]) - 1);
					}
				}
				default -> throw new ObjReaderException("Invalid element size.", lineInd);
			}
		} catch (NumberFormatException e) {
			throw new ObjReaderException("Failed to parse int value.", lineInd);
		} catch (IndexOutOfBoundsException e) {
			throw new ObjReaderException("Too few arguments.", lineInd);
		}
	}

	// Метод для проверки индексов
	private static void validateIndices(ArrayList<Integer> indices, int maxIndex, String token, int lineInd) {
		for (int i = 0; i < indices.size(); i++) {
			int index = indices.get(i);
			if (index < 0) {
				index = maxIndex + 1 + index;
				indices.set(i, index);
			}
			if (index >= maxIndex || index < 0) {
				throw new ObjReaderException("The polygon is specified incorrectly: " + token + " index out of bounds.", lineInd);
			}
		}
	}

	// Метод для проверки на дублирование индексов вершин в полигоне
	private static boolean hasDuplicates(ArrayList<Integer> vertexIndices) {
		for (int i = 0; i < vertexIndices.size() - 1; i++) {
			for (int j = i + 1; j < vertexIndices.size(); j++) {
				if (vertexIndices.get(i).equals(vertexIndices.get(j))) {
					return true;
				}
			}
		}
		return false;
	}
}