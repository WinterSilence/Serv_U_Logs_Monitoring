package myProject;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import myProject.controller.FXMLController;
import myProject.model.MyModel;
import myProject.view.WindowView;

import java.io.IOException;

public class Start extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon/WorkProject.png")));
        MyModel myModel = new MyModel();
        WindowView windowView = new WindowView(myModel);
        FXMLController fxmlController = new FXMLController(myModel);
        fxmlController.setView(windowView);
        windowView.setFxmlController(fxmlController);
        fxmlController.start(stage);
    }
}