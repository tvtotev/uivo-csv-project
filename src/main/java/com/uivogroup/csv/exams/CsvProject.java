package com.uivogroup.csv.exams;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.relique.jdbc.csv.CsvDriver;

import com.uivogroup.csv.exams.SQLConnection.Columns;

/**
 * CsvProject
 */
public class CsvProject {

    /*
     * Static logger class
     */
    static final Logger LOG = Logger.getLogger(CsvProject.class);

    /*
     * Main method
     */
    public static void main(String[] args) throws Exception {
        
        // Configure basic/console logger
        BasicConfigurator.configure();
        
        // Refactors the raw CSV file to a consistent temporary file 
        String rawCsvFile = args.length > 0 ? args[0] : Paths.get("src/main/resources/csv", "Loans.csv").toString();
        String separator = args.length > 1 ? args[1] : "\t";
        if (!Paths.get(rawCsvFile).toFile().exists()) {
            LOG.error("File " + rawCsvFile + " does not exists.");
            System.exit(-1);
        }

        // Processing
        Path allignedCsvFile = refactorRawCsvFile(rawCsvFile);
        try {
            SQLConnection.init(allignedCsvFile, separator);
            // Всички записи
            listAllRecords();
            LOG.info("");
            // Препоръчва инвестиция в кредита с най-висока доходност, при който минималната сума за инвестиция е 10 лв
            // или евро и не е в закъснение
            highestIncome();
            LOG.info("");
            // Сортира списъка по матуритет на кредитите спрямо оставащите брой вноски и типа им
            orderByMatureted();
            LOG.info("");
            // Показва средната доходност на кредитите за всеки от оригинаторите
            avarageIncome();
        } finally {
            Files.delete(allignedCsvFile);
        }
    }

    /*
     * Normalizing CSV raw format to a consistent table - transforming it to a temporary table with reformatted content:
     * - splitting merged columns
     * - removes extra data like currency signs, %, brackets, etc 
     */
    private static Path refactorRawCsvFile(String file) throws IOException {
        Path tmpFile = Files.createTempFile("uivoexam", ".csv");
        try (FileReader inputFile = new FileReader(file);
                FileWriter outputFile = new FileWriter(tmpFile.toString());
                BufferedReader inputStream = new BufferedReader(inputFile);
                PrintWriter outputStream = new PrintWriter(outputFile)) {
            String l;
            int i = 0;
            while ((l = inputStream.readLine()) != null) {
                if (i++ == 0) {
                    l = l.replaceAll("principal \\(%\\)", "principal\t%");
                } else {
                    l = l.replaceAll(" %", "");
                    l = l.replaceAll(" EUR", "");
                    l = l.replaceAll(" \\(", "\t");
                    l = l.replaceAll("\\)", "");
                }
                l = l.replaceAll("\t$", "");
                l = l.replaceAll("\t$", "");
                outputStream.println(l);
            }
        }
        return tmpFile;
    }

    // Принтиране на всички заеми
    private static void listAllRecords() throws SQLException, IOException {
        LOG.info("------- Start listing all records");
        String sql = "SELECT * FROM " + SQLConnection.getInstance().getTempTableName();
        try (Connection conn = SQLConnection.getConnection();

                // Create a Statement object to execute the query with.
                PreparedStatement stmt = conn.prepareStatement(sql);

                // Select all loan records
                ResultSet results = stmt.executeQuery()) {
            ;
            // Print date
            CsvDriver.writeToCsv(results, System.out, true);
        }
        LOG.info("------- End listing all records");

    }

    // Препоръчва инвестиция в кредита с най-висока доходност, при който минималната сума за инвестиция е 10 лв или евро
    // и не е в закъснение
    private static void highestIncome() throws SQLException, IOException {
        LOG.info(
                "------- Start: Препоръчва инвестиция в кредита с най-висока доходност, при който минималната сума за инвестиция е 10 лв или евро и не е в закъснение");
        String sql = "SELECT * from " + SQLConnection.getInstance().getTempTableName() + " where Upper("
                + Columns.STATUS + ")=? and " + Columns.AVAILABLE_FOR_INVESTMENT + "=? and " + Columns.INTEREST_RATE
                + "=(select max(" + Columns.INTEREST_RATE + ") from " + SQLConnection.getInstance().getTempTableName()
                + " where Upper(" + Columns.STATUS + ")=? and " + Columns.AVAILABLE_FOR_INVESTMENT + "=? group by "
                + Columns.INTEREST_RATE + ")";

        LOG.info("SQL: " + sql);
        try (Connection conn = SQLConnection.getConnection();
                // Create a Statement object to execute the query with.
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            ;
            stmt.setString(1, "Current".toUpperCase());
            stmt.setFloat(2, (float) 10);
            stmt.setString(3, "Current".toUpperCase());
            stmt.setFloat(4, (float) 10);
            // Select all loan records
            try (ResultSet results = stmt.executeQuery()) {
                CsvDriver.writeToCsv(results, System.out, true);
            }
        }
        LOG.info(
                "------- End: Препоръчва инвестиция в кредита с най-висока доходност, при който минималната сума за инвестиция е 10 лв или евро и не е в закъснение");
    }

    // Сортира списъка по матуритет на кредитите спрямо оставащите брой вноски и типа им
    private static void orderByMatureted() throws SQLException, IOException {
        LOG.info("------- Start: Сортира списъка по матуритет на кредитите спрямо оставащите брой вноски и типа им");
        String sql = "SELECT " + Columns.TERM + "," + Columns.INSTALMENT_TYPE + ", * from "
                + SQLConnection.getInstance().getTempTableName()
                // + " where AVAILABLE_FOR_INVESTMENT=?"
                + " order by " + Columns.TERM + "," + Columns.INSTALMENT_TYPE;

        LOG.info("SQL: " + sql);
        try (Connection conn = SQLConnection.getConnection();
                // Create a Statement object to execute the query with.
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            ;
            try (ResultSet results = stmt.executeQuery()) {
                CsvDriver.writeToCsv(results, System.out, true);
            }
        }
        LOG.info("------- End: Сортира списъка по матуритет на кредитите спрямо оставащите брой вноски и типа им");
    }

    // Показва средната доходност на кредитите за всеки от оригинаторите
    private static void avarageIncome() throws SQLException, IOException {
        LOG.info("------- Start: Показва средната доходност на кредитите за всеки от оригинаторите");
        String sql = "SELECT " + Columns.LOAN_ORIGINATOR + ", AVG(" + Columns.INTEREST_RATE
                + ") \"Average_Interest\" from " + SQLConnection.getInstance().getTempTableName() + " group by "
                + Columns.LOAN_ORIGINATOR + ", " + Columns.INTEREST_RATE;

        LOG.info("SQL: " + sql);
        try (Connection conn = SQLConnection.getConnection();
                // Create a Statement object to execute the query with.
                PreparedStatement stmt = conn.prepareStatement(sql);) {
            ;
            // Select all loan records
            try (ResultSet results = stmt.executeQuery()) {
                CsvDriver.writeToCsv(results, System.out, true);
            }
        }
        LOG.info("------- End: Показва средната доходност на кредитите за всеки от оригинаторите");
    }

}
