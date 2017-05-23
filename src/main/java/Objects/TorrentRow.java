package Objects;

import com.turn.ttorrent.client.Client;
import javafx.scene.control.ProgressBar;

/**
 * Created by narey on 11.05.2017.
 */
public class TorrentRow {

    private String name;
    private String size;
    private ProgressBar progress;
    private String left;
    private String download;
    private String upload;
    private String timePassed;
    private String downloaded;
    private String directory;
    private Client client;

    public TorrentRow(String name, long size, double progress, long left, double download, double upload, String directory) {
        this.name = name;
        this.size = Double.toString( (double) (size / (1024 * 1024))) + " MB";
        this.progress = new ProgressBar(progress);
        this.left = Double.toString( (double) (left / (1024 * 1024))) + " MB";
        this.download = Double.toString(download) + "kB/s";
        this.upload = Double.toString(upload) + "kB/s";
        this.directory = directory;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public ProgressBar getProgress() {
        return progress;
    }

    public void setProgress(ProgressBar progress) {
        this.progress = progress;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getTimePassed() {
        return timePassed;
    }

    public void setTimePassed(String timePassed) {
        this.timePassed = timePassed;
    }

    public String getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(String downloaded) {
        this.downloaded = downloaded;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
