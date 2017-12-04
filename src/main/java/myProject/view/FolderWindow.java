package myProject.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class FolderWindow {
    private Parent folderWindowNode;

    private TableView<File> folderTableView;

    private String folderPath;

    public FolderWindow() throws IOException {
        folderPath = "\\\\ftpres\\upload\\upload_inet\\";
        init();
    }

    public Parent getFolderWindowNode() {
        return folderWindowNode;
    }

    private void init() throws IOException {
        this.folderWindowNode = FXMLLoader.load(getClass().getClassLoader().getResource("FolderWindow.fxml"));
        folderTableView = (TableView<File>) folderWindowNode.lookup("#folderTableView");
        initFolderTable();
        setFolderTable();
    }

    private void initFolderTable() {
        TableColumn<File, String> recentlyEndColumn =
                (TableColumn<File, String>) folderTableView.getColumns().get(1);
        TableColumn<File, String> recentlyFilenameColumn =
                (TableColumn<File, String>) folderTableView.getColumns().get(2);
        TableColumn<File, String> recentlySizeColumn =
                (TableColumn<File, String>) folderTableView.getColumns().get(3);

        Label recentlyCheckBoxColumnLabel = new Label("□");
        TableColumn<File, Boolean> recentlyCheckBoxColumn = new TableColumn<>();
        recentlyCheckBoxColumn.setGraphic(recentlyCheckBoxColumnLabel);
        folderTableView.getColumns().set(0, recentlyCheckBoxColumn);

        recentlyCheckBoxColumn.setSortable(false);
        recentlyCheckBoxColumn.setResizable(false);
        recentlyCheckBoxColumn.setMinWidth(30);
        recentlyCheckBoxColumn.setPrefWidth(30);
        recentlyCheckBoxColumn.setMaxWidth(30);
        recentlyCheckBoxColumn.setStyle("-fx-alignment: CENTER;");

        recentlyCheckBoxColumn.setCellValueFactory(new PropertyValueFactory<>("checkbox"));

        recentlyCheckBoxColumn.setCellFactory(new Callback<TableColumn<File, Boolean>, TableCell<File, Boolean>>() {
            @Override
            public TableCell<File, Boolean> call(TableColumn<File, Boolean> param) {
                return new CheckBoxTableCell<>();
            }
        });

        recentlyCheckBoxColumnLabel.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isPrimaryButtonDown()) {
//                    for (Task task : selectedTasksSorted) {
// todo clear checkbox!!!
//                        task.setCheckbox(false);
//                    }
                }
            }
        });

        folderTableView.setEditable(true);

//        folderTableTaskChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                if (newValue != null) {
//                    searchText = "";
//                    searchTextField.setText(searchText);
//                    selectedLogin = newValue;
//                    clearCheckboxColumn();
//                    update();
//                }
//            }
//        });

        folderTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

/*
        folderTableView.setRowFactory(new Callback<TableView<Task>, TableRow<Task>>() {
            @Override
            public TableRow<Task> call(TableView<Task> tableView) {
                final TableRow<Task> row = new TableRow<Task>() {
                    {
                        TableRow<Task> row1 = this;
                        setOnDragDetected(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                startFullDrag();
                                getTableView().getSelectionModel().select(getIndex());
//                                contextMenuRecentlyUploadedFiles(row1);
                            }
                        });
                        setOnMouseDragEntered(new EventHandler<MouseDragEvent>() {
                            @Override
                            public void handle(MouseDragEvent event) {
                                getTableView().getSelectionModel().select(getIndex());
//                                contextMenuRecentlyUploadedFiles(row1);
//                                clearCheckboxColumn();
//                                selectedTasksSorted.clear();
//                                selectedTasksSorted.addAll(getTableView().getSelectionModel().getSelectedItems());
//                                setSelectedCheckboxColumn();
                            }
                        });
                        setOnMousePressed(new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent event) {
                                getTableView().getSelectionModel().select(getIndex());
                                if (event.isPrimaryButtonDown() || (row1.getItem() != null && !row1.getItem().checkboxProperty().get())) {
//                                    clearCheckboxColumn();
                                }
//                                selectedTasksSorted.clear();
//                                selectedTasksSorted.addAll(getTableView().getSelectionModel().getSelectedItems());
//                                contextMenuRecentlyUploadedFiles(row1);
//                                setSelectedCheckboxColumn();
                            }
                        });
                        setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                int focusedIndex = folderTableView.getSelectionModel().getFocusedIndex();
                                Task focusedTask = null;
                                if (focusedIndex >= 0) {
//                                    focusedTask = folderTableView.getItems().get(focusedIndex);
                                    try {
                                        Desktop.getDesktop().open(new File(Helper.renameFolder(focusedTask.getUnitFile().getFile().toString())));
                                    } catch (IOException ex) {
                                        Helper.log(ex);
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void updateItem(Task task, boolean empty) {
                        super.updateItem(task, empty);
                        if (!empty) {
                            if (task.getTimeEndToString().equals("")) {
//                                setTextRowStyle(getChildren(), Color.GREY);
                                setStyle("");
                            } else if (task.getTimeEnd().before(Helper.yesterday())) {
//                                setTextRowStyle(getChildren(), MyCustomColors.getYesterdayTextRowColor());
                                setStyle("");
                            } else {
//                                setTextRowStyle(getChildren(), MyCustomColors.getTodayTextRowColor());
                                setStyle("-fx-font-weight: bold;");
                            }
                        } else {
                            setStyle("");
                        }
                    }
                };
                return row;
            }
        });
*/

        recentlyEndColumn.setCellValueFactory(value ->
                new SimpleStringProperty(
                        new SimpleDateFormat("HH:mm dd.MM.yyyy").format(new Date(value.getValue().lastModified()))
                )
        );
        recentlyEndColumn.setStyle("-fx-alignment: CENTER;");

        recentlyFilenameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        recentlyFilenameColumn.setStyle("-fx-alignment: CENTER;");

        recentlySizeColumn.setCellValueFactory(value -> new SimpleStringProperty(value.getValue().length() / (8 * 8 * 8 * 8 * 8 * 8 * 4) + " Мб"));
        recentlySizeColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void setFolderTable() {

        folderTableView.setItems(FXCollections.observableArrayList(getContentOfFolder()));
        folderTableView.sortPolicyProperty().set(new Callback<TableView<File>, Boolean>() {
            @Override
            public Boolean call(TableView<File> param) {
                Comparator<File> comparator = new Comparator<File>() {
                    @Override
                    public int compare(File task1, File task2) {
                        return task1.lastModified() < task2.lastModified() ? 1 : -1;
                    }
                };
                FXCollections.sort(folderTableView.getItems(), comparator);
                return true;
            }
        });
    }

    private File[] getContentOfFolder() {
        File folder = new File(folderPath);
        if (folder.exists())
            return folder.listFiles();
        return new File[0];
    }
}