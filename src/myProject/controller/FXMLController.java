package myProject.controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;
import myProject.Helper;
import myProject.model.MyModel;
import myProject.model.infoFromFile.FileSource;
import myProject.view.WindowView;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FXMLController implements Controller {

    private MyModel myModel;
    private WindowView view;

    private BooleanProperty connected = new SimpleBooleanProperty(false);

    private BooleanProperty startConnection = new SimpleBooleanProperty(false);

    public FXMLController(MyModel myModel) {
        this.myModel = myModel;
    }

    public void start(Stage stage) throws IOException {
        view.startView(stage);
    }

    public void setView(WindowView view) {
        this.view = view;
    }

    public BooleanProperty connectedProperty() {
        return connected;
    }

    public BooleanProperty startConnectionProperty() {
        return startConnection;
    }

/*
    private void setOfflineProperty() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                offline.set(myModel.isOffline());
            }
        });
    }
*/

    public void establishConnection(FileSource fileSource) {
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Helper.print("Establish connection");
                startConnection.set(true);
                myModel.setFileSource(fileSource);
                myModel.initDefault();
                view.update();
                Calendar today = new GregorianCalendar();

                while (!myModel.isOffline()) {
                connected.set(true);
                    Calendar checkDate = new GregorianCalendar();
                    if (Helper.comparingDays(today, checkDate) != 0) {
                        myModel.initDefault();
                        today = new GregorianCalendar();
                    } else {
                        myModel.update();
                    }
                    view.update();
                    Helper.pause(5);
                }
//                setOffline();
                connected.set(false);
                closeConnection();
                startConnection.set(false);
                Helper.print("Disconnected");
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void establishConnectionTodayOnly(FileSource fileSource) {
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Helper.print("Establish connection");
                myModel.setFileSource(fileSource);
                myModel.initToday();
                view.update();
                Calendar today = new GregorianCalendar();
//                setOfflineProperty();
                while (!myModel.isOffline()) {
                    Calendar checkDate = new GregorianCalendar();
                    if (Helper.comparingDays(today, checkDate) != 0) {
                        myModel.initToday();
                        today = new GregorianCalendar();
                    } else {
                        myModel.update();
                    }
                    view.update();
                    Helper.pause(5);
                }
//                setOfflineProperty();
                Helper.print("Disconnected today only");
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void establishConnection(FileSource fileSource, Date... dates) {
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Helper.print("Establish connection");
                myModel.setFileSource(fileSource);
//                offline.set(false);
                myModel.init(dates);
                view.update();
//                setOfflineProperty();
                Helper.print("Disconnected");
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void closeConnection() {
        myModel.setOffline();
        Helper.print("Connection stopped");
    }

    public void setTodayFullPath(String fullPath) {
        myModel.setTodayFullPath(fullPath);
    }

    public boolean isOffline() {
        return myModel.isOffline();
    }
}