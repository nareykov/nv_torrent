package Objects;

/**
 * Created by narey on 20.05.2017.
 */
public class PeerRow {
    private String id;
    private String download;
    private String upload;

    public PeerRow(String id, String download, String upload) {
        this.id = id;
        this.download = download;
        this.upload = upload;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
