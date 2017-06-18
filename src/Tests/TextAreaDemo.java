package Tests;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TextAreaDemo extends Application {
    @Override
    public void start(Stage stage) {
        final TextArea textArea = new TextArea("Text Sample");
        textArea.setStyle("-fx-text-fill: black;");
        textArea.setPrefSize(200, 40);

        final ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        colorPicker.valueProperty().addListener((observable, oldColor, newColor) ->
                textArea.setStyle(
                        "-fx-text-fill: " + toRgbString(newColor) + ";"
                )
        );

        stage.setScene(
                new Scene(new VBox(textArea, colorPicker), 300, 250)
        );
        stage.show();
    }

    private String toRgbString(Color c) {
        return "rgb("
                + to255Int(c.getRed())
                + "," + to255Int(c.getGreen())
                + "," + to255Int(c.getBlue())
                + ")";
    }

    private int to255Int(double d) {
        return (int) (d * 255);
    }

    public static void main(String[] args) {
        launch(args);
    }
}