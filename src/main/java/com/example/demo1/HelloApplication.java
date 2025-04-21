package com.example.demo1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class HelloApplication extends Application {


    private static final String URL = "jdbc:sqlserver://localhost:1433;database=Hospital;encrypt=true;trustServerCertificate=true";
    private static final String USER = "******";
    private static final String PASSWORD = "********";
    @Override
    public void start(Stage stage) {
        TextField queryField = new TextField();
        queryField.setPromptText("Enter query: ");

        Button runButton = new Button("Run query");
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);

        runButton.setOnAction(e -> {
            String query = queryField.getText();

            try(Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                StringBuilder result = new StringBuilder();

                while(rs.next()){
                    for(int i = 1; i <= columnCount; i++){
                        result.append(meta.getColumnName(i)).append(": ")
                                .append(rs.getString(i)).append(" ");
                    }
                    result.append("\n");
                }
                resultArea.setText(result.toString());
            }catch (SQLException ex){
                resultArea.setText("SQL Error: " + ex.getMessage());
            }
        });

        VBox layout = new VBox(10, queryField, runButton, resultArea);
        Scene scene = new Scene(layout,600,400);

        stage.setTitle("SQL Query Runner");
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}