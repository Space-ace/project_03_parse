import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MessageController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label infolabel;

    @FXML
    private Button openFileButton;

    @FXML
    private Button continueButton;

    @FXML
    void initialize() {
        infolabel.setText(infolabel.getText() + " " + ProgressTask.timeSpent + " мс");
        openFileButton.setOnAction(event -> {
            File file = new File(ProgressTask.filePath);
            try {
                Desktop.getDesktop().edit(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        continueButton.setOnAction(event -> {
            Stage stage = (Stage) continueButton.getScene().getWindow();
            stage.close();
        });
    }
}
