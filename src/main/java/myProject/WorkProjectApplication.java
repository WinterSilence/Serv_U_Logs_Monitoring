package myProject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class WorkProjectApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("view/WorkProjectApplication.fxml"));

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("WorkProjectApp");
        stage.setScene(scene);
        stage.show();
    }
}

/*
    public static void main(String[] args) {
        MyModel myModel = new MyModel();
        ConsoleView view = new ConsoleView(myModel);
        FXMLController mainController = new FXMLController(myModel);
        view.start();
    }

*/
