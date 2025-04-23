package com.example.demo1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.concurrent.atomic.AtomicLong;

public class HelloApplication extends Application {


    private static final String URL = "jdbc:sqlserver://localhost:1433;database=Hospital;encrypt=true;trustServerCertificate=true";
    private static final String USER = "*******";
    private static final String PASSWORD = "*******";
    @Override
    public void start(Stage stage) {
        TextField queryField1 = new TextField();
        queryField1.setPromptText("Enter query: ");
        TextField queryField2 = new TextField();
        queryField2.setPromptText("Enter query: ");

        Label durationLabel1 = new Label("Query 1 Time: 0 ms");
        Label durationLabel2 = new Label("Query 2 Time: 0 ms");


        Button runButton = new Button("Run query");
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);



        runButton.setOnAction(e -> {
            String query1 = queryField1.getText();
            String query2 = queryField2.getText();

            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
                 Statement stmt = con.createStatement()) {

                StringBuilder result = new StringBuilder();

                // Run first query
                long startTime1 = System.currentTimeMillis();
                if (!query1.trim().isEmpty()) {
                    try (ResultSet rs1 = stmt.executeQuery(query1)) {
                        ResultSetMetaData meta1 = rs1.getMetaData();
                        int colCount1 = meta1.getColumnCount();
                        result.append("Result of Query 1:\n");
                        while (rs1.next()) {
                            for (int i = 1; i <= colCount1; i++) {
                                result.append(meta1.getColumnName(i)).append(": ")
                                        .append(rs1.getString(i)).append(" ");
                            }
                            result.append("\n");
                        }
                    }
                }
                long duration1 = System.currentTimeMillis() - startTime1;
                durationLabel1.setText("Query 1 time " + duration1 + "ms");


                // Run second query
                long startTime2 = System.currentTimeMillis();
                if (!query2.trim().isEmpty()) {
                    try (ResultSet rs2 = stmt.executeQuery(query2)) {
                        ResultSetMetaData meta2 = rs2.getMetaData();
                        int colCount2 = meta2.getColumnCount();
                        result.append("\nResult of Query 2:\n");
                        while (rs2.next()) {
                            for (int i = 1; i <= colCount2; i++) {
                                result.append(meta2.getColumnName(i)).append(": ")
                                        .append(rs2.getString(i)).append(" ");
                            }
                            result.append("\n");
                        }
                    }
                }
                long duration2 = System.currentTimeMillis() - startTime2;
                durationLabel2.setText("Query 2 time " + duration2 + "ms");

                resultArea.setText(result.toString());

            } catch (SQLException ex) {
                resultArea.setText("SQL Error: " + ex.getMessage());
            }
        });

        // Layouts
        HBox queryFieldsRow = new HBox(10, queryField1, queryField2);
        HBox durationsRow = new HBox(20, durationLabel1, durationLabel2);
        VBox rootLayout = new VBox(10, queryFieldsRow, runButton, durationsRow, resultArea);

        Scene scene = new Scene(rootLayout, 800, 450);
        stage.setTitle("SQL Query Runner");
        stage.setScene(scene);
        stage.show();
        }

    public static void main(String[] args) {
        launch();
    }
}