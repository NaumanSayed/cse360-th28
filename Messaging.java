import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messaging {
    private String userRole;
    private String patientId;
    private TextArea messageArea;

    public Messaging(Stage stage, String userRole, String patientId) {
        this.userRole = userRole;
        this.patientId = patientId;

        stage.setTitle("Messaging");

        BorderPane borderPane = new BorderPane();
        messageArea = new TextArea();
        messageArea.setEditable(false);
        messageArea.setPadding(new Insets(10));
        borderPane.setCenter(messageArea);

        Button sendNewMessageButton = new Button("Send a New Message");
        sendNewMessageButton.setOnAction(event -> {
            SendNewMessageWindow sendNewMessageWindow = new SendNewMessageWindow(stage, userRole, patientId, this::refreshMessages);
            sendNewMessageWindow.showWindow();
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> stage.close());

        VBox buttonContainer = new VBox(10, sendNewMessageButton, backButton);
        buttonContainer.setPadding(new Insets(10));
        buttonContainer.setAlignment(Pos.CENTER);

        borderPane.setBottom(buttonContainer);

        Scene scene = new Scene(borderPane, 500, 500);
        stage.setScene(scene);

        refreshMessages();

        stage.show();
    }

    public void refreshMessages() {
        messageArea.clear();
        File file = new File("Messages.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processLine(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processLine(String line) {
        Pattern pattern = Pattern.compile("\\[(.*?)\\]\\s\\[(.*?)\\]:\\s(.*)");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String recipients = matcher.group(1);
            String senderRole = matcher.group(2);
            String message = matcher.group(3);

            // If the user is a patient, only show messages where they are the sender or recipient
            if (userRole.equals("Patient")) {
                if (senderRole.contains("Patient(" + patientId + ")") || recipients.contains("Patient(" + patientId + ")")) {
                    if (senderRole.contains("Patient(" + patientId + ")")) {
                        messageArea.appendText("Me (to " + recipients + "): " + message + "\n");
                    } else {
                        messageArea.appendText(senderRole + ": " + message + "\n");
                    }
                }
            } else {
                // If the user is not a patient, show all messages that involve them
                if (senderRole.contains(userRole) || recipients.contains(userRole)) {
                    if (senderRole.contains(userRole)) {
                    	messageArea.appendText("Me (to " + recipients + "): " + message + "\n");
                    } else {
                        messageArea.appendText(senderRole + ": " + message + "\n");
                    }
                }
            }
        }
    }
}
