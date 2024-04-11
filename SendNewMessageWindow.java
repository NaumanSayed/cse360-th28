import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class SendNewMessageWindow {
    private final Stage newMessageStage;
    private final Stage mainStage;
    private final String userRole;
    private final String patientId;
    private final Map<String, SimpleBooleanProperty> recipientSelectionMap;
    private final Runnable refreshMessagesCallback;
    private final int[] patientIds = getCurrentPatientArray(); // Array with list of all patient ids available

    public SendNewMessageWindow(Stage mainStage, String userRole, String patientId, Runnable refreshMessagesCallback) {
        this.mainStage = mainStage;
        this.userRole = userRole;
        this.patientId = patientId;
        this.refreshMessagesCallback = refreshMessagesCallback;
        this.recipientSelectionMap = new HashMap<>();
        newMessageStage = new Stage();

        initializeRecipientSelectionMap();

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(10));

        Label toLabel = new Label("To:");
        gridPane.add(toLabel, 0, 0);

        ListView<String> recipientCheckList = createRecipientCheckList();
        gridPane.add(recipientCheckList, 1, 0);

        Label messageLabel = new Label("Message:");
        gridPane.add(messageLabel, 0, 1);

        TextArea messageTextArea = new TextArea();
        messageTextArea.setPrefRowCount(2);
        gridPane.add(messageTextArea, 1, 1);

        Button sendButton = new Button("Send");
        sendButton.setOnAction(event -> {
            try {
                sendMessage(recipientCheckList, messageTextArea.getText());
                messageTextArea.clear();
                clearRecipientSelections();
                if (refreshMessagesCallback != null) {
                    refreshMessagesCallback.run();
                }
            } catch (IOException e) {
                e.printStackTrace(); // Replace with proper error handling
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(event -> {
            newMessageStage.close();
            mainStage.show();
            if (refreshMessagesCallback != null) {
                refreshMessagesCallback.run();
            }
        });

        HBox buttonBox = new HBox(10, sendButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(10, gridPane, buttonBox);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10));

        Scene scene = new Scene(vBox);
        newMessageStage.setScene(scene);
        newMessageStage.setTitle("Send a New Message");

        Platform.runLater(newMessageStage::sizeToScene);
    }

    private void initializeRecipientSelectionMap() {
        // Doctors and nurses can message each other
        if (!userRole.equals("Patient")) {
            recipientSelectionMap.put("Doctor", new SimpleBooleanProperty(false));
            recipientSelectionMap.put("Nurse", new SimpleBooleanProperty(false));
        }
        // Add patients if the user is not a patient themselves
        if (userRole.equals("Doctor") || userRole.equals("Nurse")) {
            for (int id : patientIds) {
                recipientSelectionMap.put("Patient(" + id + ")", new SimpleBooleanProperty(false));
            }
        }
    }

    private ListView<String> createRecipientCheckList() {
        List<String> roles = new ArrayList<>();
        if (userRole.equals("Patient")) {
            // Patients can message doctors and nurses
            roles.add("Doctor");
            roles.add("Nurse");
        } else {
            // Doctors and nurses can message each other and each patient
            roles.add("Doctor");
            roles.add("Nurse");
            Arrays.stream(patientIds).mapToObj(id -> "Patient(" + id + ")").forEach(roles::add);
        }

        ObservableList<String> items = FXCollections.observableArrayList(roles.stream()
                .filter(role -> !role.equals(userRole)) // Exclude the user's role
                .collect(Collectors.toList()));

        ListView<String> recipientCheckList = new ListView<>();
        recipientCheckList.setItems(items);
        recipientCheckList.setPrefHeight(25 * items.size());
        recipientCheckList.setCellFactory(CheckBoxListCell.forListView(item -> 
            recipientSelectionMap.computeIfAbsent(item, k -> new SimpleBooleanProperty(false)),
            new StringConverter<String>() {
                @Override
                public String toString(String object) {
                    return object;
                }

                @Override
                public String fromString(String string) {
                    return string;
                }
            }
        ));

        return recipientCheckList;
    }

    private void clearRecipientSelections() {
        recipientSelectionMap.values().forEach(property -> property.set(false));
    }

    private void sendMessage(ListView<String> recipientCheckList, String message) throws IOException {
        List<String> selectedRecipients = recipientCheckList.getItems().stream()
            .filter(item -> recipientSelectionMap.get(item).get())
            .collect(Collectors.toList());

        String recipients = selectedRecipients.isEmpty() ? "[]" : String.join(", ", selectedRecipients);
        String sender = userRole.equals("Patient") ? "Patient(" + patientId + ")" : userRole;
        String content = "[" + recipients + "] [" + sender + "]: " + message;
        writeToFile(content);
    }

    private void writeToFile(String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Messages.txt", true))) {
            writer.write(content);
            writer.newLine();
        }
    }

    public void showWindow() {
        mainStage.hide();
        newMessageStage.show();
    }
    
    private int[] getCurrentPatientArray() {
    	String countQuery = "SELECT COUNT(*) FROM Patients";

        try (
            // Establish a connection to the database
            Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/PediatricOffice", "root", "mysql123");
            // Create a statement for executing SQL queries
            Statement statement = connection.createStatement();
            // Execute the count query and get the result set
            ResultSet resultSet = statement.executeQuery(countQuery);
        ) {
            // Get the count of patient IDs from the result set
            resultSet.next(); // Move to the first row
            int count = resultSet.getInt(1);

            // Create an array to hold patient IDs
            int[] patientArray = new int[count];

            // Populate the array with numbers from 1 to count
            for (int i = 0; i < count; i++) {
                patientArray[i] = i + 1;
            }

            return patientArray;
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null if an error occurs
        }
    }
}
