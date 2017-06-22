package myProject.controller;

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

    public FXMLController(MyModel myModel) {
        this.myModel = myModel;
    }

    public void setView(WindowView view) {
        this.view = view;
    }

    public void establishConnection(FileSource fileSource) {
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Helper.print("Establish connection");
                myModel.init(fileSource);
                view.update();
                while (!myModel.isOffline()) {
                    Date currentDate = new Date();
                    Calendar cal = new GregorianCalendar();
                    cal.set(Calendar.HOUR_OF_DAY, 9);
                    cal.set(Calendar.MINUTE, 10);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 999);
                    Date anotherDate = cal.getTime();
                    if (currentDate.compareTo(anotherDate) > 0) {
                        System.out.println("NEW DAY");
                        myModel.init(fileSource);
                    } else {
                        System.out.println("NOT_NEW_DAY");
                        myModel.update();
                    }
                    view.update();
                    Helper.pause(5);
                }
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

    public void setFullPath(String fullPath) {
        myModel.setFullPath(fullPath);
    }

    public boolean isOffline() {
        return myModel.isOffline();
    }

    public void reset() {
        myModel.setOffline();
    }
}