import javafx.application.Application;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.text.Font;

public class System extends Application {
	@Override
	public void start(Stage stage) {
		StackPane root = new StackPane();
		MainView mainview = new MainView(stage);
        root.getChildren().add(mainview);

        Scene scene = new Scene(root, 500, 300);
        stage.setScene(scene);
        stage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}

class MainView extends VBox {
	public MainView(Stage stage) {
		Label label = new Label("Office Automation System for Pediatric Doctor’s Office");
		label.setPadding(new Insets(10, 0, 25, 0));
		
		
		Button bt1 = new Button("Nurse View");
		bt1.setPrefWidth(200);
        bt1.setPrefHeight(50);
        bt1.setOnAction(event -> new NurseView(stage));
        
        VBox.setMargin(bt1, new Insets(0, 0, 25, 0));
		Button bt2 = new Button("Doctor View");
		bt2.setPrefWidth(200);
        bt2.setPrefHeight(50);
        bt2.setOnAction(event -> new DoctorView(stage));
        
        VBox.setMargin(bt2, new Insets(0, 0, 25, 0));
		Button bt3 = new Button("Patient Portal");
		bt3.setPrefWidth(200);
        bt3.setPrefHeight(50);
        bt3.setOnAction(event -> new PatientPortal(stage));
        
        bt1.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(Color.rgb(79, 113, 191), null, null)));
        bt2.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(Color.rgb(79, 113, 191), null, null)));
        bt3.setBackground(new javafx.scene.layout.Background(new javafx.scene.layout.BackgroundFill(Color.rgb(79, 113, 191), null, null)));
        bt1.setFont(Font.font("System", FontWeight.BOLD, 12));
        bt2.setFont(Font.font("System", FontWeight.BOLD, 12));
        bt3.setFont(Font.font("System", FontWeight.BOLD, 12));
		
		this.setAlignment(Pos.TOP_CENTER);
		this.getChildren().addAll(label, bt1, bt2, bt3);
		
	}
}