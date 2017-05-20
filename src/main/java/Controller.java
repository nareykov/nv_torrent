import Objects.TorrentRow;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.client.peer.SharingPeer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class Controller {

    public Label labelPassed;
    public Label labelDowloaded;
    public Label labelLeft;
    public Label labelDirectory;
    public Label labelDowload;
    public Label labelName;
    private FileChooser fileChooser = new FileChooser();
    private DirectoryChooser directoryChooser = new DirectoryChooser();
    private ObservableList<TorrentRow> list;
    private int selectedIndex = -1;

    @FXML
    TableView table;

    @FXML
    Button addButton;

    @FXML
    Button closeButton;

    @FXML
    TableColumn<TorrentRow, String> columnName;

    @FXML
    TableColumn<TorrentRow, String> columnSize;

    @FXML
    TableColumn<TorrentRow, ProgressBar> columnProgress;

    @FXML
    TableColumn<TorrentRow, String> columnLeft;

    @FXML
    TableColumn<TorrentRow, String> columnDownload;

    @FXML
    TableColumn<TorrentRow, String> columnUpload;

    @FXML
    GridPane gridPane;

    @FXML
    private void initialize() {
        list = FXCollections.observableArrayList();

        columnName.setCellValueFactory(new PropertyValueFactory<TorrentRow, String>("name"));
        columnSize.setCellValueFactory(new PropertyValueFactory<TorrentRow, String>("size"));
        columnProgress.setCellValueFactory(new PropertyValueFactory<TorrentRow, ProgressBar>("progress"));
        columnLeft.setCellValueFactory(new PropertyValueFactory<TorrentRow, String>("left"));
        columnDownload.setCellValueFactory(new PropertyValueFactory<TorrentRow, String>("download"));
        columnUpload.setCellValueFactory(new PropertyValueFactory<TorrentRow, String>("upload"));

        table.setItems(list);
    }

    @FXML private void rowSelected(MouseEvent e) {
        selectedIndex = table.getSelectionModel().getSelectedIndex();
    }

    @FXML protected void close(ActionEvent e){
        System.out.println("Closing GUI");
        System.exit(0);
    }

    @FXML protected void openTorrent(ActionEvent e) {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("torrent Files", "*.torrent"),
                new FileChooser.ExtensionFilter("All Files", "*")
        );
        fileChooser.setTitle("Open .torrent File");
        File torrentFile = fileChooser.showOpenDialog(gridPane.getScene().getWindow());

        directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Output Folder");
        File outDirectory = directoryChooser.showDialog(gridPane.getScene().getWindow());

        ThreadManager tm = new ThreadManager(torrentFile, outDirectory);
        new Thread(tm).start();

    }

     private void refreshInfo(TorrentRow torrentRow) {
         Platform.runLater(
                 () -> {
                     labelDowload.setText(torrentRow.getDownload());
                     labelDirectory.setText(torrentRow.getDirectory());
                     labelDowloaded.setText(torrentRow.getDownloaded());
                     labelLeft.setText(torrentRow.getLeft());
                     labelName.setText(torrentRow.getName());
                     labelPassed.setText(torrentRow.getTimePassed());
                 }
         );
    }

    private class ThreadManager implements Runnable {
        File torrentFile;
        File outDirectory;

        ThreadManager(File torrentFile, File outDirectory){
            this.torrentFile = torrentFile;
            this.outDirectory = outDirectory;
        }

        public void run() {
            try {
                SharedTorrent torrent = SharedTorrent.fromFile(torrentFile, outDirectory);
                Client client = new Client(InetAddress.getLocalHost(), torrent);

                TorrentRow torrentRow = new TorrentRow(torrent.getName(), torrent.getSize(), (double) torrent.getCompletion(),
                        torrent.getLeft(), 0.0, 0.0, outDirectory.getPath());
                list.add(torrentRow);

                client.share();
                Date beginTime = new Date();

                int i = 0;
                while (!torrent.isFinished()) {
                    torrentRow.getProgress().setProgress(torrent.getCompletion() / 100);
                    torrentRow.setLeft(Double.toString( (double) (torrent.getLeft() / (1024 * 1024))) + " MB");
                    torrentRow.setDownloaded(Double.toString((double) ((torrent.getSize() - torrent.getLeft()) / (1024 * 1024))) + " MB");

                    float dl = 0;
                    float ul = 0;
                    for (SharingPeer peer : client.getPeers()) {
                        if (peer.isConnected()) {
                            dl += peer.getDLRate().get();
                            ul += peer.getULRate().get();
                        }
                    }
                    torrentRow.setDownload(String.format("%.2f", dl/1024.0) + " kB/s");
                    torrentRow.setUpload(String.format("%.2f", ul/1024.0) + " kB/s");

                    i += 3;
                    table.refresh();

                    Date endTime = new Date();
                    SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                    torrentRow.setTimePassed(format.format(new Date(endTime.getTime() - beginTime.getTime())));

                    if (selectedIndex >= 0 && list.get(selectedIndex).equals(torrentRow)) {
                        refreshInfo(torrentRow);
                    }

                    Thread.sleep(3000);
                }
                torrentRow.setLeft("finished");
                torrentRow.setDownload(String.format("%.2f", 0.0) + " kB/s");
                torrentRow.setUpload(String.format("%.2f", 0.0) + " kB/s");

                client.waitForCompletion();
                System.out.print("Waiting for completion");
            } catch (UnknownHostException uhE) {
                System.err.println("Cannot find host.");
                //uhE.printStackTrace();
            } catch (IOException ioE) {
                System.err.println("Cannot find file.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
