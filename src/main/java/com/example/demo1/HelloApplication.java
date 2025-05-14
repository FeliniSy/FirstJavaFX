package com.example.demo1;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {

    private static final String URL = "jdbc:sqlserver://FELINISY;database=Hospital;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "!234";

    // Store execution times for historical comparison
    private final List<Long> query1Times = new ArrayList<>();
    private final List<Long> query2Times = new ArrayList<>();
    private boolean isFirstExecution = true;

    // UI components
    private TextField queryField1;
    private TextField queryField2;
    private Label durationLabel1;
    private Label durationLabel2;
    private TextArea resultArea1;
    private TextArea resultArea2;
    //    private BarChart<String, Number> currentExecutionChart;
    private BarChart<String, Number> historicalExecutionChart;

    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab queryTab = new Tab("Queries");
        Tab chartTab = new Tab("Charts");

        BorderPane queryPane = setupQueryInterface();
        queryTab.setContent(queryPane);

        VBox chartPane = setupChartInterface();
        chartTab.setContent(chartPane);

        tabPane.getTabs().addAll(queryTab, chartTab);

        // Main scene
        Scene scene = new Scene(tabPane, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("SQL Query Performance Analyzer");
        stage.setScene(scene);
        stage.show();
    }

    private BorderPane setupQueryInterface() {
        BorderPane queryPane = new BorderPane();

        // Query input section
        VBox inputBox = new VBox(10);
        inputBox.setPadding(new Insets(15));
        inputBox.getStyleClass().add("rootLayout");

        // Query 1
        VBox queryBox1 = new VBox(5);
        queryBox1.getStyleClass().add("query-box");
        Label queryLabel1 = new Label("Query 1:");
        queryLabel1.getStyleClass().add("query-label");
        queryField1 = new TextField();
        queryField1.setPromptText("Enter first SQL query");
        queryField1.getStyleClass().add("query-field");
        queryBox1.getChildren().addAll(queryLabel1, queryField1);

        // Query 2
        VBox queryBox2 = new VBox(5);
        queryBox2.getStyleClass().add("query-box");
        Label queryLabel2 = new Label("Query 2:");
        queryLabel2.getStyleClass().add("query-label");
        queryField2 = new TextField();
        queryField2.setPromptText("Enter second SQL query");
        queryField2.getStyleClass().add("query-field");
        queryBox2.getChildren().addAll(queryLabel2, queryField2);

        // Duration labels
        HBox durationBox = new HBox(20);
        durationBox.getStyleClass().add("duration-box");
        durationBox.setAlignment(Pos.CENTER);
        durationLabel1 = new Label("Query 1 Time: 0 ms");
        durationLabel1.getStyleClass().add("duration-label");
        durationLabel2 = new Label("Query 2 Time: 0 ms");
        durationLabel2.getStyleClass().add("duration-label");
        durationBox.getChildren().addAll(durationLabel1, durationLabel2);

        // Run button
        Button runButton = new Button("Run Queries");
        runButton.getStyleClass().add("runButton");
        runButton.setDefaultButton(true); // run with enter
        HBox buttonBox = new HBox(runButton);
        buttonBox.setAlignment(Pos.CENTER);

        inputBox.getChildren().addAll(queryBox1, queryBox2, durationBox, buttonBox);

        // Results section - side by side
        GridPane resultsGrid = new GridPane();
        resultsGrid.setPadding(new Insets(10));
        resultsGrid.setHgap(10);


        //Results
        resultArea1 = new TextArea();
        resultArea1.setEditable(false);
        resultArea1.getStyleClass().add("resultArea");
        resultArea1.setWrapText(true);
        GridPane.setHgrow(resultArea1, Priority.ALWAYS);
        GridPane.setVgrow(resultArea1, Priority.ALWAYS);

        resultArea2 = new TextArea();
        resultArea2.setEditable(false);
        resultArea2.getStyleClass().add("resultArea");
        resultArea2.setWrapText(true);
        GridPane.setHgrow(resultArea2, Priority.ALWAYS);
        GridPane.setVgrow(resultArea2, Priority.ALWAYS);

        resultsGrid.add(new Label("Query 1 Results:"), 0, 0);
        resultsGrid.add(new Label("Query 2 Results:"), 1, 0);
        resultsGrid.add(resultArea1, 0, 1);
        resultsGrid.add(resultArea2, 1, 1);

        //with equality
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col2.setPercentWidth(50);
        resultsGrid.getColumnConstraints().addAll(col1, col2);

        runButton.setOnAction(e -> executeQueries());

        // Layout
        queryPane.setTop(inputBox);
        queryPane.setCenter(resultsGrid);

        return queryPane;
    }

    private VBox setupChartInterface() {
        VBox chartPane = new VBox(20);
        chartPane.setPadding(new Insets(15));
        chartPane.getStyleClass().add("rootLayout");

        // Historical execution chart
        historicalExecutionChart = createBarChart("Historical Execution Times", "Execution", "Time (ms)");
        historicalExecutionChart.setPrefHeight(300);

        // 100s-mde
        ((NumberAxis) historicalExecutionChart.getYAxis()).setUpperBound(100);

        chartPane.getChildren().addAll(
                new Label("Query Performance Visualization"),
                historicalExecutionChart
        );

        return chartPane;
    }

    private BarChart<String, Number> createBarChart(String title, String xAxisLabel, String yAxisLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setUpperBound(100); // Set maximum to 100ms

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle(title);
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);
        barChart.setLegendVisible(true);
        barChart.setAnimated(false);

        return barChart;
    }

    private void executeQueries() {
        String query1 = queryField1.getText();
        String query2 = queryField2.getText();

        StringBuilder result1 = new StringBuilder();
        StringBuilder result2 = new StringBuilder();
        long duration1 = 0;
        long duration2 = 0;

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = con.createStatement()) {

            // Run first query
            if (!query1.trim().isEmpty()) {
                long startTime1 = System.currentTimeMillis();

                try (ResultSet rs1 = stmt.executeQuery(query1)) {
                    ResultSetMetaData meta1 = rs1.getMetaData();
                    int colCount1 = meta1.getColumnCount();

                    result1.append("QUERY 1 RESULTS:\n");
                    result1.append("-----------------------------\n");

                    // Column headers
                    for (int i = 1; i <= colCount1; i++) {
                        result1.append(meta1.getColumnName(i));
                        if (i < colCount1) result1.append(" | ");
                    }
                    result1.append("\n");
                    result1.append("-----------------------------\n");

                    // Data rows
                    int rowCount = 0;
                    while (rs1.next()) {
                        rowCount++;
                        for (int i = 1; i <= colCount1; i++) {
                            result1.append(rs1.getString(i));
                            if (i < colCount1) result1.append(" | ");
                        }
                        result1.append("\n");
                    }

                    result1.append("-----------------------------\n");
                    result1.append("Total rows: ").append(rowCount);
                }

                duration1 = System.currentTimeMillis() - startTime1;

                // Only add to tracking if not the first execution
                if (!isFirstExecution) {
                    query1Times.add(duration1);
                }

                durationLabel1.setText("Query 1: " + duration1 + " ms");
            } else {
                result1.append("Query 1: Not executed (empty query)");
            }

            // Run second query
            if (!query2.trim().isEmpty()) {
                long startTime2 = System.currentTimeMillis();

                try (ResultSet rs2 = stmt.executeQuery(query2)) {
                    ResultSetMetaData meta2 = rs2.getMetaData();
                    int colCount2 = meta2.getColumnCount();

                    result2.append("QUERY 2 RESULTS:\n");
                    result2.append("-----------------------------\n");

                    // Column headers
                    for (int i = 1; i <= colCount2; i++) {
                        result2.append(meta2.getColumnName(i));
                        if (i < colCount2) result2.append(" | ");
                    }
                    result2.append("\n");
                    result2.append("-----------------------------\n");

                    // Data rows
                    int rowCount = 0;
                    while (rs2.next()) {
                        rowCount++;
                        for (int i = 1; i <= colCount2; i++) {
                            result2.append(rs2.getString(i));
                            if (i < colCount2) result2.append(" | ");
                        }
                        result2.append("\n");
                    }

                    result2.append("-----------------------------\n");
                    result2.append("Total rows: ").append(rowCount);
                }

                duration2 = System.currentTimeMillis() - startTime2;

                // Only add to tracking if not the first execution
                if (!isFirstExecution) {
                    query2Times.add(duration2);
                }

                durationLabel2.setText("Query 2: " + duration2 + " ms");
            } else {
                result2.append("Query 2: Not executed (empty query)");
            }

            // Update results
            resultArea1.setText(result1.toString());
            resultArea2.setText(result2.toString());


            // Update historical chart only after first execution
            if (isFirstExecution) {
                isFirstExecution = false;
            } else {
                updateHistoricalChart();
            }

        } catch (SQLException ex) {
            resultArea1.setText("SQL Error: " + ex.getMessage());
            resultArea2.setText("SQL Error: " + ex.getMessage());
        }
    }

    private void updateHistoricalChart() {
        ObservableList<XYChart.Series<String, Number>> data = FXCollections.observableArrayList();



        if (!query1Times.isEmpty()) {
            XYChart.Series<String, Number> series1 = new XYChart.Series<>();
            series1.setName(queryField1.getText());

            for (int i = 0; i < query1Times.size(); i++) {
                series1.getData().add(new XYChart.Data<>("Run " + (i + 1), query1Times.get(i)));
            }

            data.add(series1);
        }

        if (!query2Times.isEmpty()) {
            XYChart.Series<String, Number> series2 = new XYChart.Series<>();
            series2.setName(queryField2.getText());

            for (int i = 0; i < query2Times.size(); i++) {
                series2.getData().add(new XYChart.Data<>("Run " + (i + 1), query2Times.get(i)));
            }

            data.add(series2);
        }

        historicalExecutionChart.setData(data);
    }

    public static void main(String[] args) {
        launch();
    }
}