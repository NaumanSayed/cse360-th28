import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.lang.System;
import java.sql.*;
import java.time.LocalDate;

public class PatientPortal extends GridPane {
	PatientPortal(Stage stage) {
        Label firstNameLabel = new Label("First Name:");
        Label lastNameLabel = new Label("Last Name:");
        Label birthdayLabel = new Label("Birthday:");
        Label msg = new Label("Don't have an account?");

        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();

        DatePicker birthdayPicker = new DatePicker();


        Button signInButton = new Button("Sign In");
        signInButton.setOnAction(event -> {
            // Check if all input fields are not empty
            if (!firstNameField.getText().isEmpty() && !lastNameField.getText().isEmpty() && birthdayPicker.getValue() != null) {
                // Call signInHandler only if all fields are not empty
                signInHandler(stage, firstNameField, lastNameField, birthdayPicker);
                // Clear all input fields after processing
                firstNameField.clear();
                lastNameField.clear();
                birthdayPicker.setValue(null);
            } else {
                // Show alert if any of the fields are empty
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("One or more fields are empty. Please fill in all fields.");
                alert.showAndWait();
            }
        });
        Button createAccountButton = new Button("Sign Up");
        createAccountButton.setOnAction(event -> signUpHandler(stage));

        this.setPadding(new Insets(20));
        this.setVgap(10);
        this.setHgap(10);

        this.add(firstNameLabel, 0, 0);
        this.add(firstNameField, 1, 0);
        this.add(lastNameLabel, 0, 1);
        this.add(lastNameField, 1, 1);
        this.add(birthdayLabel, 0, 2);
        this.add(birthdayPicker, 1, 2);
        this.add(signInButton, 1, 3);
        this.add(createAccountButton, 1, 4);
        this.add(msg, 0, 4);
        
        Scene scene = new Scene(this, 400, 200); 
        stage.setScene(scene);
        stage.show();
		
	}
	
	public void signInHandler(Stage stage, TextField firstNameField, TextField lastNameField, DatePicker birthdayPicker) {
		
		String fn = firstNameField.getText().trim();
		String ln = lastNameField.getText().trim();
		LocalDate dob = birthdayPicker.getValue();
		
		
	    try {
	        // Establish the database connection
	        Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/PediatricOffice", "root", "mysql123");

	        // Prepare the SQL query
	        String sql = "SELECT * FROM Patients WHERE first_name = ? AND last_name = ? AND birthday = ?";
	        PreparedStatement statement = connection.prepareStatement(sql);
	        statement.setString(1, fn);
	        statement.setString(2, ln);
	        statement.setDate(3, java.sql.Date.valueOf(dob));

	        // Execute the query
	        ResultSet resultSet = statement.executeQuery();

	        // Check if the result set has any rows
	        if (resultSet.next()) {
	            // If the result set is not empty, it means the patient exists in the database
	            // You can perform further actions here, such as opening the patient's portal
//	            new PatientPortal(stage);
//	        	System.out.print("patient found");
	        	openPortal(stage, fn, ln);
	            
	        } else {
	            // If the result set is empty, display an alert indicating that the patient does not exist
	            Alert alert = new Alert(AlertType.ERROR);
	            alert.setTitle("Error");
	            alert.setHeaderText(null);
	            alert.setContentText("Patient not found. Please check your information.");
	            alert.showAndWait();
	        }

	        // Close the connections and statement
	        resultSet.close();
	        statement.close();
	        connection.close();
	    } catch (SQLException e) {
	        // Handle any SQL exceptions
	        e.printStackTrace();
	    }
	}
	
	public void openPortal(Stage stage, String fn, String ln) {
        Label titleLabel = new Label("Patient Portal");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button messageButton = new Button("Open Chat");
        messageButton.setOnAction(e -> {
        	Stage messagingStage = new Stage();
        	new Messaging(messagingStage, "Patient", getPatientID(fn, ln)); 
        });

        // VBox to hold visit summaries
        Label visitsContainer = new Label(getPatientHistory(getPatientID(fn, ln)));

        // ScrollPane to make the summaries scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(visitsContainer);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setFitToWidth(true);

        // Email TextField and Change button at the bottom right
        HBox emailChangeBox = new HBox(10);
        emailChangeBox.setAlignment(Pos.CENTER_RIGHT); // Align to the right
        TextField emailTextField = new TextField(getPatientEmail(fn, ln)); 
        Button changeEmailButton = new Button("Change");
        changeEmailButton.setOnAction(event -> updatePatientEmail(emailTextField.getText(), getPatientEmail(fn, ln)));
        
        emailChangeBox.getChildren().addAll(emailTextField, changeEmailButton);

        // Adding an HBox at the bottom to contain both the message button and email change section
        HBox bottomBox = new HBox();
        bottomBox.setPadding(new Insets(10));
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setSpacing(10);
        bottomBox.getChildren().addAll(messageButton, emailChangeBox);

        // Main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(titleLabel);
        BorderPane.setMargin(titleLabel, new Insets(10, 0, 10, 0));
        mainLayout.setCenter(scrollPane);
        mainLayout.setBottom(bottomBox);
        
        // Scene setup
        Scene scene = new Scene(mainLayout, 400, 300);
        stage.setScene(scene);
        stage.show();
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
	
	private String getPatientID(String fn, String ln) {
	    String patientId = null;

	    // SQL query to retrieve the patient ID based on first name and last name
	    String sql = "SELECT patient_id FROM Patients WHERE first_name = ? AND last_name = ?";

	    try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/PediatricOffice", "root", "mysql123");
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, fn);
	        stmt.setString(2, ln);

	        ResultSet rs = stmt.executeQuery();

	        // Check if the result set contains a row
	        if (rs.next()) {
	            // Extract the patient ID from the result set
	            patientId = rs.getString("patient_id");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return patientId;
	}
	
	public void updatePatientEmail(String newEmail, String oldEmail) {
		if (!newEmail.equals(oldEmail)) {
            // Connection details
            String url = "jdbc:mysql://127.0.0.1:3306/PediatricOffice";
            String user = "root";
            String password = "mysql123";

            // SQL command to update the email
            String sql = "UPDATE Patients SET email = ? WHERE email = ?";

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                
                // Set the parameters for the query
                statement.setString(1, newEmail);
                statement.setString(2, oldEmail);
                
                // Execute the update
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Email updated successfully.");
                } else {
                    System.out.println("Email update failed. No match found for the old email.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } 
	}
	
	public String getPatientEmail(String fn, String ln) {
		String email = "";
	    // Connection string
        String url = "jdbc:mysql://127.0.0.1:3306/PediatricOffice";
        String user = "root";
        String password = "mysql123";
        
        // SQL query to retrieve the email
        String sql = "SELECT email FROM Patients WHERE first_name = ? AND last_name = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Set the parameters
            statement.setString(1, fn);
            statement.setString(2, ln);
            
            // Execute the query
            try (ResultSet resultSet = statement.executeQuery()) {
                // Check if an email was found
                if (resultSet.next()) {
                    email = resultSet.getString("email");
                } else {
                    System.out.println("No email found for the provided name.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		
		return email;
	}
	
	public void signUpHandler(Stage stage) {
        VBox mainLayout = new VBox();
        mainLayout.setPadding(new Insets(15));
        mainLayout.setSpacing(10);

        // Section titles
        Label contactInfoLabel = new Label("Contact Information");
        Label insuranceInfoLabel = new Label("Insurance Information");
        Label pharmacyInfoLabel = new Label("Pharmacy Information");
        Label personalInfoLabel = new Label("Personal Information");

        // Contact information section (simplified)
        GridPane contactInfoGrid = createGridPane();
        TextField emailField = addTextFieldWithLabel(contactInfoGrid, "Email:", 0);

        // Insurance information section
        GridPane insuranceInfoGrid = createGridPane();
        TextField insuranceProviderField = addTextFieldWithLabel(insuranceInfoGrid, "Provider:", 0);
        TextField policyNumberField = addTextFieldWithLabel(insuranceInfoGrid, "Policy Number:", 1);

        // Pharmacy information section
        GridPane pharmacyInfoGrid = createGridPane();
        TextField pharmacyNameField = addTextFieldWithLabel(pharmacyInfoGrid, "Name:", 0);
        TextField pharmacyPhoneField = addTextFieldWithLabel(pharmacyInfoGrid, "Phone:", 1);

        // Personal information section
        GridPane personalInfoGrid = createGridPane();
        TextField firstNameField = addTextFieldWithLabel(personalInfoGrid, "First Name:", 0);
        TextField lastNameField = addTextFieldWithLabel(personalInfoGrid, "Last Name:", 1);
        DatePicker birthdayPicker = new DatePicker();
        personalInfoGrid.add(new Label("Birthday:"), 0, 2);
        personalInfoGrid.add(birthdayPicker, 1, 2);

        // Buttons
        Button createAccountButton = new Button("Create Account");
        Button signInButton = new Button("Sign In");

        // Event handlers for buttons
        createAccountButton.setOnAction(event -> {
            // Check if all fields are not empty
            if (!firstNameField.getText().isEmpty() && !lastNameField.getText().isEmpty() && birthdayPicker.getValue() != null &&
                    !emailField.getText().isEmpty() && !insuranceProviderField.getText().isEmpty() && !policyNumberField.getText().isEmpty() &&
                    !pharmacyNameField.getText().isEmpty() && !pharmacyPhoneField.getText().isEmpty()) {
                // Call newPatient and showPatientIdAlert if all fields are not empty
                newPatient(firstNameField, lastNameField, birthdayPicker, emailField, insuranceProviderField,
                        policyNumberField, pharmacyNameField, pharmacyPhoneField);
                showPatientIdAlert(getPatientID(firstNameField.getText(), lastNameField.getText()));

                // Clear all fields after processing
                firstNameField.clear();
                lastNameField.clear();
                birthdayPicker.setValue(null);
                emailField.clear();
                insuranceProviderField.clear();
                policyNumberField.clear();
                pharmacyNameField.clear();
                pharmacyPhoneField.clear();
            } else {
                // Show alert if any field is empty
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("One or more fields are empty. Please fill in all fields.");
                alert.showAndWait();
            }
        });
        signInButton.setOnAction(event -> new PatientPortal(stage));
        

        // Adding sections and buttons to the main layout
        mainLayout.getChildren().addAll(
                personalInfoLabel, personalInfoGrid,
                contactInfoLabel, contactInfoGrid,
                insuranceInfoLabel, insuranceInfoGrid,
                pharmacyInfoLabel, pharmacyInfoGrid,
                createAccountButton, signInButton // Added signInButton here
        );

        // Scene setup
        Scene scene = new Scene(mainLayout, 400, 650); // Adjusted height to accommodate new button
        stage.setScene(scene);
        stage.show();
    }
	
	public void newPatient(TextField firstNameField, TextField lastNameField,DatePicker birthdayPicker, TextField emailField, TextField insuranceProviderField, TextField policyNumberField, TextField pharmacyNameField, TextField pharmacyPhoneField) {
		String fn = firstNameField.getText().trim();
		String ln = lastNameField.getText().trim();
		LocalDate dob = birthdayPicker.getValue();
		java.sql.Date sqlDate = java.sql.Date.valueOf(dob);
		String em = emailField.getText();
		String ip = insuranceProviderField.getText();
		String pn = policyNumberField.getText();
		String pname = pharmacyNameField.getText();
		String pp = pharmacyPhoneField.getText();
		
				
		try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/PediatricOffice", "root", "mysql123");
            String sql = "INSERT INTO Patients (first_name, last_name, birthday, email, provider, policy_number, pharmacy_name, pharmacy_phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, fn);
            statement.setString(2, ln);
            statement.setDate(3, sqlDate);
            statement.setString(4, em);
            statement.setString(5, ip);
            statement.setString(6, pn);
            statement.setString(7, pname);
            statement.setString(8, pp);

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new row has been inserted successfully!");
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
		
	}

    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(10, 0, 10, 0));
        return grid;
    }

    private TextField addTextFieldWithLabel(GridPane grid, String labelText, int rowIndex) {
        Label label = new Label(labelText);
        TextField textField = new TextField();
        grid.add(label, 0, rowIndex);
        grid.add(textField, 1, rowIndex);
        return textField;
    }
    
    private static void showPatientIdAlert(String patientId) {
        Stage alertWindow = new Stage();
        alertWindow.setTitle("Patient ID");

        Label messageLabel = new Label("Your Patient ID is: " + patientId);
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> alertWindow.close());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(messageLabel, closeButton);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 300, 150);
        alertWindow.setScene(scene);
        alertWindow.showAndWait();
    }
	
}
