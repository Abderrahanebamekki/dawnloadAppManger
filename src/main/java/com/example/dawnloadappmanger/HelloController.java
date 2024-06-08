package com.example.dawnloadappmanger;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.dawnloadappmanger.DownloadHandler;
import com.example.dawnloadappmanger.FileObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class HelloController implements ProgressCallback{

    private ArrayList<FileObject> urlList = new ArrayList<>();
    private HashMap<String, DownloadHandler> handlerList = new HashMap<>();

    @FXML
    private TextField textField;

    @FXML
    private TableView<FileObject> tableView;

    @FXML
    private TableColumn<FileObject, String> pathClmn;

    @FXML
    private TableColumn<FileObject, String> progClmn;

    @FXML
    private TableColumn<FileObject, String> pauseClmn;

    @FXML
    private TableColumn<FileObject, String> resumeClmn;

    @FXML
    private TableColumn<FileObject, String> cancelClmn;

    @FXML
    private TableColumn<FileObject, String> sizeClmn;


    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public void initialize() {
        pathClmn.setCellValueFactory(cellData -> cellData.getValue().pathProperty());
        progClmn.setCellValueFactory(cellData -> cellData.getValue().progProperty());
        sizeClmn.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());

        // For Pause column: set cell factory to display a button
        pauseClmn.setCellFactory(column -> new TableCell<FileObject, String>() {
            private final Button pauseButton = new Button("Pause");

            {
                pauseButton.setOnAction(event -> {
                    FileObject fileObject = getTableView().getItems().get(getIndex());
                    handlePause(fileObject); // Call a method to handle pause action
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pauseButton);
                }
            }
        });
        // for resume column
        resumeClmn.setCellFactory(column -> new TableCell<FileObject, String>() {
            private final Button resumeButton = new Button("Resume");

            {
                resumeButton.setOnAction(event -> {
                    FileObject fileObject = getTableView().getItems().get(getIndex());
                    handleResume(fileObject); // Call a method to handle resume action
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(resumeButton);
                }
            }
        });

        // For Cancel column: set cell factory to display a button
        cancelClmn.setCellFactory(column -> new TableCell<FileObject, String>() {
            private final Button cancelButton = new Button("Cancel");

            {
                cancelButton.setOnAction(event -> {
                    FileObject fileObject = getTableView().getItems().get(getIndex());
                    handleCancel(fileObject); // Call a method to handle cancel action
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(cancelButton);
                }
            }
        });
    }

    // Method to handle pause action
    private void handlePause(FileObject fileObject) {
        System.out.println("Paused: " + fileObject.idProperty());
        DownloadHandler handler = handlerList.get(fileObject.idProperty().get());
        handler.pause();
        // Add your pause logic here
    }

    // Method to handle cancel action
    private void handleResume(FileObject fileObject) {
        System.out.println("Resume: " + fileObject.idProperty());
        DownloadHandler handler = handlerList.get(fileObject.idProperty().get());
        handler.resume();
    }
    private void handleCancel(FileObject fileObject) {
        System.out.println("Cancel: " + fileObject.idProperty());
        tableView.getItems().remove(fileObject);
        DownloadHandler handler = handlerList.remove(fileObject.idProperty().get());
        //lkn mzal mtfasach tfasih
        if (handler != null) {
            handler.resume();
        }
    }

    @FXML
    private void handelDownloadAll() throws IOException {
        for (FileObject url: urlList) {
            DownloadHandler handler = new DownloadHandler(url , this);
            handlerList.put(url.idProperty().get(), handler);
            handler.setFileName();
            executorService.execute(handler);
        }
    }
    public void handelAdd() throws IOException {
        // Create a new File instance for the added path
        String url = textField.getText().trim();
        //if bloc hadi testi lakan valeur ta3 input vide wla lala
        if (url.isEmpty()){
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText(null);
            alert.setContentText("Please enter a value in the text field.");
            alert.showAndWait();
        }
        //had bloc test ila url valide ou pas
        else {
            FileObject file = new FileObject(url, "0%", UUID.randomUUID().toString(), "");
            DownloadHandler handler = new DownloadHandler(file, this);
            int status = handler.CheckUrl();
            //ilakan valide yjib la taille
            if(status == HttpURLConnection.HTTP_OK){
                System.out.println("Accepted");
                textField.clear();
                double x = (double) handler.getContentLength();
                String size;
                if(x >= 1024 && x < 1024 * 1024){
                    x = Math.round( x / 1024);
                    size = x + "Kb";
                }
                else if(x >= 1024 * 1024){
                    x = Math.round(x / (1024 * 1024));
                    size = x + "Mb";
                }
                else{
                    size = Math.round(x) + "bytes";
                }
                file.setSize(size);
                tableView.getItems().add(file);
                urlList.add(file);
            }
            //laakn machi valide ykhrjlk alerte
            else{
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("The provided URL is not valid, please enter a valid URL.");
                alert.showAndWait();
            }
        }
    }

    @Override
    public void updateProgress(String id, double progress) {
        Platform.runLater(() -> {
            // Find the corresponding FileObject in the TableView and update its progress
            for (FileObject fileObject : tableView.getItems()) {
                if (fileObject.idProperty().get().equals(id)) {
                    fileObject.progProperty().set(String.format("%.2f%%", progress));
                    break;
                }
            }
        });
    }
}


