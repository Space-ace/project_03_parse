import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

public class MainController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField pageField;

    @FXML
    private TextField goodsField;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button parseAndWriteButton;

    @FXML
    private Hyperlink hyperLink;

    private ProgressTask progressTask;

    @FXML
    void initialize() {
        hyperLink.setOnAction(event -> {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(URI.create(hyperLink.getText()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        pageField.setOnMouseClicked(event -> {
            pageField.setPromptText("");
            pageField.setPrefWidth(150);
        });
        goodsField.setOnMouseClicked(event -> {
            goodsField.setPromptText("");
            goodsField.setPrefWidth(150);
        });
        parseAndWriteButton.setOnAction(event -> {
            if (!pageField.getText().isEmpty()) {
                if (!goodsField.getText().isEmpty()) {
                    if (checkTextField(pageField) * 125 >= checkTextField(goodsField)) {
                        ProgressTask.goodCount = checkTextField(pageField) * 125;
                        goodsField.setText("");
                    } else {
                        ProgressTask.goodCount = checkTextField(goodsField);
                        pageField.setText("");
                    }
                } else {
                    ProgressTask.goodCount = checkTextField(pageField) * 125;
                }
            }
            else {
                ProgressTask.goodCount = checkTextField(goodsField);
            }
            if (ProgressTask.goodCount != 0) {
                progressTask = new ProgressTask();
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(progressTask.progressProperty());
                new Thread(progressTask).start();
            }
        });
    }

    //проверка на праввильность введенных символов в textField
    private int checkTextField(TextField textField) {
        if (textField.getText().isEmpty()) {
            textField.setPrefWidth(230);
            textField.setPromptText("Введите целое положительное число");
            textField.setText("");
            textField.setStyle("-fx-prompt-text-fill: red;");
            return 0;
        } else if (!textField.getText().matches("\\d+")) {
            textField.setPrefWidth(500);
            textField.setPromptText("Введено некорректное число: " + textField.getText() + ". Используйте целое положительное число");
            textField.setText("");
            textField.setStyle("-fx-prompt-text-fill: red;");
            return 0;
        }
        return Integer.parseInt(textField.getText());
    }
}
