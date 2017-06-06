package Tests;

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import javafx.beans.binding.*;
import javafx.beans.value.*;

public class BindingJavaFX extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World");
        Group root = new Group();
        final Scene scene = new Scene(root, 300, 250);
        final Button btn = new Button();
        btn.setLayoutX(100);
        btn.setLayoutY(80);
        btn.setText("Hello World");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                System.out.println("Hello World");
            }
        });
        final DoubleBinding db = scene.widthProperty().subtract(150);
        db.addListener(new javafx.beans.value.ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                btn.setLayoutX(db.getValue());
            }
        });
        root.getChildren().add(btn);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
