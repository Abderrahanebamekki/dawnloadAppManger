package com.example.dawnloadappmanger;


import javafx.beans.property.SimpleStringProperty;

public class FileObject {
    private final SimpleStringProperty pathClmn;
    private final SimpleStringProperty progClmn;
    private final SimpleStringProperty idClmn;
    private SimpleStringProperty sizeClmn;

    public FileObject(String pathClmn, String progClmn, String id, String size) {
        this.pathClmn = new SimpleStringProperty(pathClmn);
        this.progClmn = new SimpleStringProperty(progClmn);
        this.idClmn = new SimpleStringProperty(id);
        this.sizeClmn = new SimpleStringProperty(size);

    }
    public SimpleStringProperty pathProperty() {
        return pathClmn;
    }
    public SimpleStringProperty progProperty() {
        return progClmn;
    }
    public SimpleStringProperty idProperty() {
        return idClmn;
    }
    public SimpleStringProperty sizeProperty() {
        return sizeClmn;
    }
    public void setSize(String size) {
        this.sizeClmn = new SimpleStringProperty(size);
    }
}
