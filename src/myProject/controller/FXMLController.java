package myProject.controller;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import myProject.Helper;
import myProject.model.MyModel;
import myProject.model.infoFromFile.FileSource;
import myProject.view.WindowView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FXMLController implements Controller {

    private MyModel myModel;
    private WindowView view;

    private BooleanProperty offline = new SimpleBooleanProperty(true);

    public FXMLController(MyModel myModel) {
        this.myModel = myModel;
    }

    public void setView(WindowView view) {
        this.view = view;
    }

    public BooleanProperty offlineProperty() {
        return offline;
    }

    public void setOffline() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                offline.set(myModel.isOffline());
            }
        });

    }

    public void establishConnection(FileSource fileSource) {
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Helper.print("Establish connection");
                myModel.initDefault(fileSource);
                view.update();
                Calendar today = new GregorianCalendar();
                setOffline();
                while (!myModel.isOffline()) {
                    Calendar checkDate = new GregorianCalendar();
                    if (Helper.comparingDays(today, checkDate) != 0) {
                        myModel.initDefault(fileSource);
                        today = new GregorianCalendar();
                    } else {
                        myModel.update();
                    }
                    view.update();
                    Helper.pause(5);
                }
                setOffline();
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
                myModel.initToday(fileSource);
                view.update();
                Calendar today = new GregorianCalendar();
                while (!myModel.isOffline()) {
                    Calendar checkDate = new GregorianCalendar();
                    if (Helper.comparingDays(today, checkDate) != 0) {
                        myModel.initToday(fileSource);
                        today = new GregorianCalendar();
                    } else {
                        myModel.update();
                    }
                    view.update();
                    Helper.pause(5);
                }
                Helper.print("Disconnected today only");
            }
        });

        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void establishConnection(FileSource fileSource, Date ... dates) {
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Helper.print("Establish connection");
                myModel.init(fileSource, dates);
                view.update();
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