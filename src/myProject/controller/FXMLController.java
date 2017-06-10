package myProject.controller;

import myProject.Helper;
import myProject.model.MyModel;
import myProject.model.infoFromFile.FileSource;
import myProject.view.WindowView;

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
                    myModel.update();                    // Observe
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