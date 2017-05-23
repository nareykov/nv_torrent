import Objects.PeerRow;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;

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
    public TableColumn columnPeerIP;
    public TableColumn columnPeerDownload;
    public TableColumn columnPeerUpload;
    private FileChooser fileChooser = new FileChooser();
    private DirectoryChooser directoryChooser = new DirectoryChooser();
    private ObservableList<TorrentRow> listTorrents;
    private ObservableList<PeerRow> listPeers;
    private int selectedIndex = -1;
    private static final Logger log = Logger.getLogger(Controller.class);

    @FXML
    TableView table;
    @FXML
    public TableView infoTable;

    @FXML
    Button addButton;
    @FXML
    Button closeButton;
    @FXML
    Button deleteButton;

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
        listTorrents = FXCollections.observableArrayList();
        listPeers = FXCollections.observableArrayList();

        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        columnProgress.setCellValueFactory(new PropertyValueFactory<>("progress"));
        columnLeft.setCellValueFactory(new PropertyValueFactory<>("left"));
        columnDownload.setCellValueFactory(new PropertyValueFactory<>("download"));
        columnUpload.setCellValueFactory(new PropertyValueFactory<TorrentRow, String>("upload"));

        columnPeerIP.setCellValueFactory(new PropertyValueFactory<PeerRow, String>("id"));
        columnPeerDownload.setCellValueFactory(new PropertyValueFactory<PeerRow, String>("download"));
        columnPeerUpload.setCellValueFactory(new PropertyValueFactory<PeerRow, String>("upload"));

        table.setItems(listTorrents);
        infoTable.setItems(listPeers);

        addButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("Плюс Filled-50.png"))));
        deleteButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("Отмена Filled-50.png"))));
        closeButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("Выключение системы Filled-50.png"))));
    }

    @FXML private void rowSelected(MouseEvent e) {
        selectedIndex = table.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            refreshInfo(listTorrents.get(selectedIndex));
            log.info("Selected " + selectedIndex + " row");
        }
    }

    @FXML private void delete(ActionEvent e) {
        if (selectedIndex >= 0) {
            TorrentRow torrentRow = listTorrents.get(selectedIndex);
            torrentRow.getClient().stop();
            deleteFile(torrentRow.getDirectory() + "\\" + torrentRow.getName());
            refreshInfo(new TorrentRow("", 0 , 0, 0, 0, 0, ""));
            listTorrents.remove(selectedIndex);
            listPeers.clear();
        }
    }

    /**
     * Функция удаления файла/папки
     * @param path путь
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        if (deleteDirectory(file)) {
            System.out.println("File " + path + " deleted successfully");
            log.info("File " + path + " deleted successfully");
        } else {
            System.out.println("File " + path + " delete error");
            log.error("File " + path + " delete error");
        }
    }

    /**
     * Рекурсивная функция удаления
     * @param directory Объект удаляемого файла/папки
     * @return true - в случае успеха
     */
    public static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return directory.delete();
    }

    @FXML protected void close(MouseEvent e){
        System.out.println("Closing GUI");
        log.info("Closing GUI");
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
                listTorrents.add(torrentRow);
                torrentRow.setClient(client);

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

                    if (selectedIndex >= 0 && listTorrents.get(selectedIndex).equals(torrentRow)) {
                        refreshInfo(torrentRow);
                        listPeers.clear();
                        for (SharingPeer peer : client.getPeers()) {
                            if (peer.isConnected()) {
                                listPeers.add(new PeerRow(peer.getIp(), Float.toString(dl), Float.toString(ul)));
                            }
                        }
                    }

                    Thread.sleep(1000);
                }
                torrentRow.setLeft("finished");
                torrentRow.setDownload(String.format("%.2f", 0.0) + " kB/s");
                torrentRow.setUpload(String.format("%.2f", 0.0) + " kB/s");

                client.waitForCompletion();
                System.out.print("Waiting for completion");
            } catch (UnknownHostException uhE) {
                System.err.println("Cannot find host.");
            } catch (IOException ioE) {
                System.err.println("Cannot find file.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
