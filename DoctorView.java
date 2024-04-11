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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DoctorView {
	DoctorView(Stage stage) {
        stage.setTitle("Doctor's Portal");

        // Create layout
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setVgap(10);
        grid.setHgap(10);

        // Add components
        Label lblPatientID = new Label("Enter Patient ID:");
        GridPane.setConstraints(lblPatientID, 0, 0);

        TextField txtPatientID = new TextField();
        GridPane.setConstraints(txtPatientID, 1, 0);

        Button btnViewHistory = new Button("View Patient History");
        GridPane.setConstraints(btnViewHistory, 1, 7);

        Label lblPhysicalTest = new Label("Enter Physical Test Results:");
        GridPane.setConstraints(lblPhysicalTest, 0, 3);

        TextArea txtPhysicalTest = new TextArea();
        txtPhysicalTest.setPrefRowCount(5);
        txtPhysicalTest.setPrefColumnCount(30);
        txtPhysicalTest.setWrapText(true);
        GridPane.setConstraints(txtPhysicalTest, 0, 4);

        Button btnEnterTestResults = new Button("Save Visit Info");
//        btnEnterTestResults.setPrefHeight(150);
        GridPane.setConstraints(btnEnterTestResults, 1, 5);

        Label lblPrescription = new Label("Send Prescription:");
        GridPane.setConstraints(lblPrescription, 0, 5);

        TextArea txtPrescription = new TextArea();
        txtPrescription.setPrefRowCount(3);
        txtPrescription.setPrefColumnCount(30);
        txtPrescription.setWrapText(true);
        GridPane.setConstraints(txtPrescription, 0, 6);

//        Button btnSendPrescription = new Button("Send Prescription");
//        GridPane.setConstraints(btnSendPrescription, 1, 6);

        // Add button for messaging system
        Button btnMessagingSystem = new Button("Open Chat");
        GridPane.setConstraints(btnMessagingSystem, 0, 7);

        // Add components to the grid
        grid.getChildren().addAll(lblPatientID, txtPatientID,
                lblPhysicalTest, txtPhysicalTest, btnEnterTestResults,
                lblPrescription, txtPrescription, btnMessagingSystem, btnViewHistory);

        // Set action events
        btnViewHistory.setOnAction(e -> showPatientHistory());
        
        btnEnterTestResults.setOnAction(e -> {
            // Check if all text fields are not empty
            if (!txtPhysicalTest.getText().isEmpty() && !txtPrescription.getText().isEmpty() && !txtPatientID.getText().isEmpty()) {
                // Call SaveTestResults if all text fields are not empty
                SaveTestResults(txtPhysicalTest, txtPrescription, txtPatientID);
                
                // Clear the text fields after calling SaveTestResults
                txtPhysicalTest.clear();
                txtPrescription.clear();
                txtPatientID.clear();
            } else {
                // Show alert if any of the fields are empty
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("One or more fields are empty. Please fill in all fields.");
                alert.showAndWait();
            }
        });
        
        btnMessagingSystem.setOnAction(e -> {
            Stage messagingStage = new Stage();
            new Messaging(messagingStage, "Doctor", "1"); // "1" is an example patientId
        });

        // Set scene
        Scene scene = new Scene(grid, 600, 400);
        stage.setScene(scene);
        stage.show();
	}
	
//	 private void viewPatientHistory(TextArea txtPatientHistory, String patientID) {
//	        // Dummy method to simulate viewing patient history
//	        txtPatientHistory.setText("Patient's medical history for ID " + patientID + " goes here...");
//	    }

	    private void SaveTestResults(TextArea txtPhysicalTest, TextArea txtPrescription, TextField patientID) {
	    	String physicalTest = txtPhysicalTest.getText();
	        String prescription = txtPrescription.getText();
	        int patientId = Integer.parseInt(patientID.getText());
	        
	        Integer visitId = findExistingVisit(patientId);

	        // If there's no existing visit with null fields, do nothing
	        if (visitId == null) {
	            return;
	        }

	        // An existing visit is found; update it
	        updateExistingVisitDetails(visitId, physicalTest, prescription);
	    }
	    
	    private Integer findExistingVisit(int patientId) {
	        String sql = "SELECT v.visit_id FROM VisitDetails v " +
	                     "JOIN PatientVisits pv ON v.visit_id = pv.visit_id " +
	                     "WHERE pv.patient_id = ? AND (v.test_results IS NULL OR v.prescription IS NULL) LIMIT 1";

	        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/PediatricOffice", "root", "mysql123");
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setInt(1, patientId);
	            ResultSet rs = stmt.executeQuery();

	            if (rs.next()) {
	                return rs.getInt("visit_id");
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return null; // No existing visit found
	    }
	    
	    private void updateExistingVisitDetails(int visitId, String physicalTest, String prescription) {
	        String sql = "UPDATE VisitDetails SET test_results = ?, prescription = ? WHERE visit_id = ?";

	        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/PediatricOffice", "root", "mysql123");
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setString(1, physicalTest);
	            stmt.setString(2, prescription);
	            stmt.setInt(3, visitId);

	            int rowsAffected = stmt.executeUpdate();
	            if (rowsAffected > 0) {
//	                System.out.println("Visit details updated successfully.");
	            } else {
//	                System.out.println("Failed to update visit details.");
	            }
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
