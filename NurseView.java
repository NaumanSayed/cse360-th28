import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NurseView extends HBox {
	
	NurseView(Stage stage) {
		stage.setTitle("Nurse's Portal");

        // Main layout is now a BorderPane
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15, 12, 15, 12));

        // Top bar for 'Open Chat' button
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        Button openChatButton = new Button("Open Chat");
        openChatButton.setOnAction(e -> {
            Stage messagingStage = new Stage();
            new Messaging(messagingStage, "Nurse", "1"); // "1" is an example patientId
        });
        topBar.getChildren().add(openChatButton);
        topBar.setPadding(new Insets(10, 10, 10, 0));

        // Set the top bar to the top region of BorderPane
        root.setTop(topBar);

        // Title for Nurse's Portal
        Label portalLabel = new Label("Nurse's Portal");
        portalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Patient ID input
        TextField patientIdTextField = new TextField();
        HBox patientIdBox = new HBox(10, new Label("Patient ID:"), patientIdTextField);
        patientIdBox.setPadding(new Insets(0, 12, 15, 12));

        // Space between title and Patient ID
        VBox titleSpaceBox = new VBox(20, portalLabel, patientIdBox);

        // Grid for form inputs
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        TextField heightTextField = new TextField();
        grid.add(new Label("Enter Height:"), 0, 0);
        grid.add(heightTextField, 1, 0);

        TextField weightTextField = new TextField();
        grid.add(new Label("Enter Weight:"), 0, 1);
        grid.add(weightTextField, 1, 1);

        TextField tempTextField = new TextField();
        grid.add(new Label("Body Temperature:"), 0, 2);
        grid.add(tempTextField, 1, 2);

        TextField bpTextField = new TextField();
        grid.add(new Label("Blood Pressure:"), 0, 3);
        grid.add(bpTextField, 1, 3);

        TextArea allergyTextArea = new TextArea();
        grid.add(new Label("Enter any known allergies:"), 0, 4);
        grid.add(allergyTextArea, 1, 4);

        TextArea concernsTextArea = new TextArea();
        grid.add(new Label("Enter any health concerns:"), 0, 5);
        grid.add(concernsTextArea, 1, 5);

        // Buttons for actions
        Button historyButton = new Button("Check Patient History");
        historyButton.setOnAction(e -> showPatientHistory());

        Button saveButton = new Button("Save Visit Details");
        saveButton.setOnAction(e -> {
            // Check if all fields are not empty
            if (!patientIdTextField.getText().isEmpty() && !heightTextField.getText().isEmpty() &&
                    !weightTextField.getText().isEmpty() && !tempTextField.getText().isEmpty() &&
                    !bpTextField.getText().isEmpty() && !allergyTextArea.getText().isEmpty() &&
                    !concernsTextArea.getText().isEmpty()) {
                // Call SavePatientVitals if all fields are not empty
                SavePatientVitals(patientIdTextField.getText(), heightTextField.getText(), weightTextField.getText(),
                        tempTextField.getText(), bpTextField.getText(), allergyTextArea.getText(), concernsTextArea.getText());

                // Clear all fields after processing
                patientIdTextField.setText("");
                heightTextField.setText("");
                weightTextField.setText("");
                tempTextField.setText("");
                bpTextField.setText("");
                allergyTextArea.setText("");
                concernsTextArea.setText("");
            } else {
                // Show alert if any field is empty
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("One or more fields are empty. Please fill in all fields.");
                alert.showAndWait();
            }
        });

        HBox buttonLayout = new HBox(10, historyButton, saveButton);
        buttonLayout.setAlignment(Pos.CENTER);

        // Compose the main layout
        VBox mainContent = new VBox(10, titleSpaceBox, grid, buttonLayout);
        root.setCenter(mainContent); // Add mainContent to the center region of BorderPane

        // Scene and stage setup
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
 }
	
	public void SavePatientVitals(String ID, String height, String weight, String temp, String bp, String allergy, String concerns) {
		int patientId = Integer.parseInt(ID);
		int visitId = insertPatientVisit(patientId);
        if (visitId != -1) {
            // Step 2: Insert a new record into the VisitDetails table
            insertPatientVitals(visitId, height, weight, temp, bp, allergy, concerns);
//            System.out.println("Test results and prescription saved successfully.");
        } else {
//            System.out.println("Failed to save test results and prescription.");
        }
	}
	
	private int insertPatientVisit(int patientId) {
        String sql = "INSERT INTO PatientVisits (patient_id) VALUES (?)";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/PediatricOffice", "root", "mysql123");
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, patientId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the generated visit_id
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if insertion failed
    }
	
	private void insertPatientVitals(int visitId, String height, String weight, String temp, String bp, String allergy, String concerns) {
   	 String sql = "INSERT INTO VisitDetails (visit_id, test_results, prescription, Height, Weight, BodyTemperature, BloodPressure, Allergies, HealthConcerns) VALUES (?, NULL, NULL, ?, ?, ?, ?, ?, ?)";
   	    try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/PediatricOffice", "root", "mysql123");
   	         PreparedStatement stmt = conn.prepareStatement(sql)) {
   	        stmt.setInt(1, visitId);
   	        stmt.setString(2, height);
   	        stmt.setString(3, weight);
   	        stmt.setString(4, temp);
   	        stmt.setString(5, bp);
   	        stmt.setString(6, allergy);
   	        stmt.setString(7, concerns);
   	        // No need to set NULLs explicitly for the other columns, as they are defaulted to NULL
   	        stmt.executeUpdate();
   	    } catch (SQLException e) {
   	        e.printStackTrace();
   	    }	   
   }
	
	private void showPatientHistory() {
        // New window (Stage) for patient history
        Stage historyStage = new Stage();
        historyStage.initModality(Modality.APPLICATION_MODAL);
        historyStage.setTitle("Patient History");

     // Patient History layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        // Patient ID input
        Label patientIdLabel = new Label("Enter Patient ID:");
        TextField patientIdInput = new TextField();
        patientIdInput.setPromptText("Patient ID");

        // Label to display patient history
        Label historyLabel = new Label();

        // Button to show history
        Button showHistoryButton = new Button("Show History");
        showHistoryButton.setOnAction(e -> {
            String patientId = patientIdInput.getText();
            if (!patientId.isEmpty()) {
                // Call getPatientHistory only if patientId is not empty
                historyLabel.setText("History for Patient ID: " + patientId + "\n" + getPatientHistory(patientId));
                patientIdInput.clear(); // Clear the patientIdInput field
            } else {
                // Show alert if patientId is empty
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("Patient ID is empty. Please enter a valid Patient ID.");
                alert.showAndWait();
            }
        });

        // Layout for Patient ID and button
        HBox patientIdBox = new HBox(5, patientIdLabel, patientIdInput, showHistoryButton);
        patientIdBox.setAlignment(Pos.CENTER);

        // ScrollPane for displaying history
        ScrollPane scrollPane = new ScrollPane(historyLabel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);

        // Close button for the history window
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> historyStage.close());

        // Assembling the history layout
        layout.getChildren().addAll(patientIdBox, scrollPane, closeButton);
        layout.setAlignment(Pos.CENTER);

        // Scene and stage setup for history window
        Scene scene = new Scene(layout, 400, 300);
        historyStage.setScene(scene);
        historyStage.showAndWait();
    }
	
	private String getPatientHistory(String patientId) {
	    StringBuilder history = new StringBuilder();

	    // Query to fetch visit details for the given patient ID
	    String sql = "SELECT v.visit_id, v.test_results, v.prescription, v.Height, v.Weight, v.BodyTemperature, v.BloodPressure, v.Allergies, v.HealthConcerns " +
	                 "FROM VisitDetails v " +
	                 "JOIN PatientVisits pv ON v.visit_id = pv.visit_id " +
	                 "WHERE pv.patient_id = ?";

	    try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/PediatricOffice", "root", "mysql123");
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, patientId);
	        ResultSet rs = stmt.executeQuery();

	        // Iterate over the result set and format the visit details
	        while (rs.next()) {
	            int visitId = rs.getInt("visit_id");
	            String testResults = rs.getString("test_results");
	            String prescription = rs.getString("prescription");
	            String height = rs.getString("Height");
	            String weight = rs.getString("Weight");
	            String bodyTemperature = rs.getString("BodyTemperature");
	            String bloodPressure = rs.getString("BloodPressure");
	            String allergies = rs.getString("Allergies");
	            String healthConcerns = rs.getString("HealthConcerns");

	            // Append the formatted visit details to the StringBuilder
	            history.append("Visit: ").append(visitId).append("\n");
	            history.append("Test Results: ").append(testResults).append("\n");
	            history.append("Prescription: ").append(prescription).append("\n");
	            history.append("Height: ").append(height).append("\n");
	            history.append("Weight: ").append(weight).append("\n");
	            history.append("Body Temperature: ").append(bodyTemperature).append("\n");
	            history.append("Blood Pressure: ").append(bloodPressure).append("\n");
	            history.append("Allergies: ").append(allergies).append("\n");
	            history.append("Health Concerns: ").append(healthConcerns).append("\n\n");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return history.toString();	
	}
	
}


