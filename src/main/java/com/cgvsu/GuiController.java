package com.cgvsu;

import com.cgvsu.model.Scene;
import com.cgvsu.objreader.ObjReaderException;
import com.cgvsu.objwriter.ObjWriter;
import com.cgvsu.render_engine.RenderEngine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.File;
import javax.vecmath.Vector3f;

import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;

public class GuiController {

    final private float TRANSLATION = 0.5F;

    @FXML
    AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    private Scene scene = new Scene();

    private Camera camera = new Camera(
            new Vector3f(0, 00, 100),
            new Vector3f(0, 0, 0),
            1.0F, 1, 0.01F, 100);

    private Timeline timeline;

    @FXML
    private ListView<String> modelList;

    private ObservableList<String> modelNames = FXCollections.observableArrayList();

    private boolean isDarkTheme = false;

    @FXML
    private void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        // Инициализация ListView
        modelList.setItems(modelNames);
        modelList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            int selectedIndex = modelList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                scene.setActiveModel(scene.getModels().get(selectedIndex));
            }
        });

        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(15), event -> {
            double width = canvas.getWidth();
            double height = canvas.getHeight();

            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
            camera.setAspectRatio((float) (width / height));

            // Отрисовываем все модели в сцене
            for (Model model : scene.getModels()) {
                RenderEngine.render(canvas.getGraphicsContext2D(), camera, model, (int) width, (int) height);
            }
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        // Применяем тему при запуске
        applyTheme();
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Load Model");

        File file = fileChooser.showOpenDialog((Stage) canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        Path fileName = Path.of(file.getAbsolutePath());

        try {
            String fileContent = Files.readString(fileName);
            Model model = ObjReader.read(fileContent);
            scene.addModel(model); // Добавляем модель в сцену
            scene.setActiveModel(model); // Делаем её активной

            // Добавляем имя модели в ListView
            modelNames.add(file.getName());
            modelList.getSelectionModel().select(modelNames.size() - 1); // Выбираем последнюю добавленную модель
        } catch (IOException exception) {
            showErrorDialog("Load Error", "Failed to load the model: " + exception.getMessage());
        } catch (ObjReaderException exception) {
            showErrorDialog("Load Error", "Failed to parse the model: " + exception.getMessage());
        }
    }

    @FXML
    private void onSaveModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model (*.obj)", "*.obj"));
        fileChooser.setTitle("Save Model");
        File file = fileChooser.showSaveDialog((Stage) canvas.getScene().getWindow());

        if (file != null) {
            try {
                ObjWriter.write(scene.getActiveModel(), file); // Сохраняем активную модель
                showSuccessDialog("Save Successful", "Model saved successfully.");
            } catch (IOException e) {
                showErrorDialog("Save Error", "Failed to save the model: " + e.getMessage());
            }
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleTranslateX(ActionEvent actionEvent) {
        float delta = 0.1f; // Шаг перемещения
        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            vertex.x += delta;
        }
    }

    @FXML
    public void handleTranslateY(ActionEvent actionEvent) {
        float delta = 0.1f; // Шаг перемещения
        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            vertex.y += delta;
        }
    }

    @FXML
    public void handleTranslateZ(ActionEvent actionEvent) {
        float delta = 0.1f; // Шаг перемещения
        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            vertex.z += delta;
        }
    }

    @FXML
    public void handleRotateX(ActionEvent actionEvent) {
        float angle = 5.0f; // Угол поворота в градусах
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            float y = vertex.y * cos - vertex.z * sin;
            float z = vertex.y * sin + vertex.z * cos;
            vertex.y = y;
            vertex.z = z;
        }
    }

    @FXML
    public void handleRotateY(ActionEvent actionEvent) {
        float angle = 5.0f; // Угол поворота в градусах
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            float x = vertex.x * cos + vertex.z * sin;
            float z = -vertex.x * sin + vertex.z * cos;
            vertex.x = x;
            vertex.z = z;
        }
    }

    @FXML
    public void handleRotateZ(ActionEvent actionEvent) {
        float angle = 5.0f; // Угол поворота в градусах
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            float x = vertex.x * cos - vertex.y * sin;
            float y = vertex.x * sin + vertex.y * cos;
            vertex.x = x;
            vertex.y = y;
        }
    }

    @FXML
    public void handleScale(ActionEvent actionEvent) {
        float scaleFactor = 1.1f; // Коэффициент масштабирования
        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            vertex.x *= scaleFactor;
            vertex.y *= scaleFactor;
            vertex.z *= scaleFactor;
        }
    }

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
    }

    @FXML
    public void handleTranslateXNegative(ActionEvent actionEvent) {
        float delta = -0.1f; // Шаг перемещения в отрицательном направлении
        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            vertex.x += delta;
        }
    }

    @FXML
    public void handleTranslateYNegative(ActionEvent actionEvent) {
        float delta = -0.1f; // Шаг перемещения в отрицательном направлении
        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            vertex.y += delta;
        }
    }

    @FXML
    public void handleTranslateZNegative(ActionEvent actionEvent) {
        float delta = -0.1f; // Шаг перемещения в отрицательном направлении
        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            vertex.z += delta;
        }
    }

    @FXML
    public void handleRotateXNegative(ActionEvent actionEvent) {
        float angle = -5.0f; // Угол поворота в отрицательном направлении
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            float y = vertex.y * cos - vertex.z * sin;
            float z = vertex.y * sin + vertex.z * cos;
            vertex.y = y;
            vertex.z = z;
        }
    }

    @FXML
    public void handleRotateYNegative(ActionEvent actionEvent) {
        float angle = -5.0f; // Угол поворота в отрицательном направлении
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            float x = vertex.x * cos + vertex.z * sin;
            float z = -vertex.x * sin + vertex.z * cos;
            vertex.x = x;
            vertex.z = z;
        }
    }

    @FXML
    public void handleRotateZNegative(ActionEvent actionEvent) {
        float angle = -5.0f; // Угол поворота в отрицательном направлении
        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            float x = vertex.x * cos - vertex.y * sin;
            float y = vertex.x * sin + vertex.y * cos;
            vertex.x = x;
            vertex.y = y;
        }
    }

    @FXML
    public void handleScaleIncrease(ActionEvent actionEvent) {
        float scaleFactor = 1.1f; // Увеличение масштаба
        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            vertex.x *= scaleFactor;
            vertex.y *= scaleFactor;
            vertex.z *= scaleFactor;
        }
    }

    @FXML
    public void handleScaleDecrease(ActionEvent actionEvent) {
        float scaleFactor = 0.9f; // Уменьшение масштаба
        for (com.cgvsu.math.Vector3f vertex : scene.getActiveModel().vertices) {
            vertex.x *= scaleFactor;
            vertex.y *= scaleFactor;
            vertex.z *= scaleFactor;
        }
    }

    @FXML
    public void handleToggleTheme(ActionEvent actionEvent) {
        isDarkTheme = !isDarkTheme; // Переключаем тему
        applyTheme(); // Применяем тему
    }

    private void applyTheme() {
        if (isDarkTheme) {
            // Устанавливаем тёмную тему
            anchorPane.setStyle("-fx-background-color: #2E2E2E;");
            RenderEngine.setStrokeColor(javafx.scene.paint.Color.WHITE); // Белый цвет для объектов

            // Изменяем цвет текста на белый
            modelList.setStyle("-fx-text-fill: white; -fx-control-inner-background: #2E2E2E;");
            setTextColorForNodes(anchorPane, "white");

            // Устанавливаем тёмный фон для кнопок
            setButtonStyle(anchorPane, "-fx-background-color: #444444; -fx-text-fill: white;");
        } else {
            // Устанавливаем светлую тему
            anchorPane.setStyle("-fx-background-color: #FFFFFF;");
            RenderEngine.setStrokeColor(javafx.scene.paint.Color.BLACK); // Чёрный цвет для объектов

            // Изменяем цвет текста на чёрный
            modelList.setStyle("-fx-text-fill: black; -fx-control-inner-background: white;");
            setTextColorForNodes(anchorPane, "black");

            // Устанавливаем светлый фон для кнопок
            setButtonStyle(anchorPane, "-fx-background-color: #DDDDDD; -fx-text-fill: black;");
        }
    }

    // Вспомогательный метод для изменения цвета текста у всех элементов
    private void setTextColorForNodes(Parent parent, String color) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: " + color + ";");
            } else if (node instanceof Button) {
                // Текст внутри кнопок уже обрабатывается в setButtonStyle
            } else if (node instanceof Parent) {
                // Рекурсивно обрабатываем дочерние элементы контейнеров
                setTextColorForNodes((Parent) node, color);
            }
        }
    }

    // Вспомогательный метод для изменения стиля кнопок
    private void setButtonStyle(Parent parent, String style) {
        for (javafx.scene.Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Button) {
                ((Button) node).setStyle(style);
            } else if (node instanceof Parent) {
                // Рекурсивно обрабатываем дочерние элементы контейнеров
                setButtonStyle((Parent) node, style);
            }
        }
    }
}