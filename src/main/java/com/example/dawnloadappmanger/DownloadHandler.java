package com.example.dawnloadappmanger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class DownloadHandler implements Runnable {

    private ProgressCallback progressCallback;
    private volatile boolean isPaused = false;
    private CountDownLatch pauseLatch = new CountDownLatch(1);
    private static final int BUFFER_SIZE = 1024;

    private String fileUrl;
    private String saveDir ;
    private String fileName = "";
    private HttpURLConnection httpConn;
    public double progress;
    public FileObject fileObj;

    public DownloadHandler(FileObject urls, ProgressCallback progressCallback) throws IOException{
        this.fileObj = urls;
        this.fileUrl = urls.pathProperty().get();
        this.saveDir = "/home/abdoubmk/Desktop/";
        this.progress = 0;
        this.progressCallback = progressCallback;
        URL url = new URL(urls.pathProperty().get());
        this.httpConn = (HttpURLConnection) url.openConnection();
    }
    // always check HTTP response code first
    public int CheckUrl() throws IOException {
        //hadi traj3 etat ta conexion li kriyinah esq 200 400...mlih  nn
        int responseCode = httpConn.getResponseCode();
        return responseCode;
    }

    public void downloadFile() throws IOException, InterruptedException  {


        // opens input stream from the HTTP connection
        InputStream inputStream = httpConn.getInputStream();
        String saveFilePath = saveDir + File.separator + fileName;

        // opens an output stream to save into file
        FileOutputStream outputStream = new FileOutputStream(saveFilePath);
        int bytesRead = -1;
        byte[] buffer = new byte[BUFFER_SIZE];
        double m = 0;
        double fileSize = this.getContentLength();
        while ((bytesRead = inputStream.read(buffer)) != -1){

            /*System.out.println(inputStream.read(buffer));*/
            checkPause();
            outputStream.write(buffer, 0, bytesRead);

            m += bytesRead;
            this.progress = (m / fileSize) * 100;
            progressCallback.updateProgress(this.fileObj.idProperty().get(), this.progress);
            //System.out.println("size: "+ fileSize + " // x: "+ m + " // progress: " + Math.round(this.progress) + "%");
        }
    }

    // get file name from header
    public void setFileName() {
        String disposition = httpConn.getHeaderField("Content-Disposition");

        if (disposition != null) {
            // extracts file name from header field
            int index = disposition.indexOf("filename=");
            System.out.println("index "+index);
            if (index > 0) {
                fileName = disposition.substring(index + 10, disposition.length() - 1);
            }
        } else {
            fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        }
        this.fileName= fileName;
    }
    // get the lenght of file
    public int getContentLength() {
        return httpConn.getContentLength();
    }
    // this method for disconnected from http
    public void disconnect(){
        httpConn.disconnect();
    }

    public void pause() {
        isPaused = true;
        pauseLatch = new CountDownLatch(1);
    }

    public void resume() {
        isPaused = false;
        pauseLatch.countDown();
    }

    public boolean isPaused() {
        return isPaused;
    }

    private void checkPause() throws InterruptedException {
        while (isPaused) {
            pauseLatch.await();
        }
    }

    @Override
    public void run() {
        try {
            downloadFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
