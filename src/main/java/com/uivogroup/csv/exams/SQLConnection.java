package com.uivogroup.csv.exams;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/*
 * This is a helper class for this simple task that provides simplified DB connection & utilities
 */
public class SQLConnection {

    /*
     * Table columns
     */
    public enum Columns {
        ID, LOAN_TYPE, LOAN_ORIGINATOR, SCORE_CLASS, GUARANTEED_PRINCIPAL, GUARANTEED_PRINCIPAL2, TERM, INSTALMENT_TYPE,
        STATUS, INTEREST_RATE, AVAILABLE_FOR_INVESTMENT, DISCOUNT_PREMIUM, PRICE;
    }

    /*
     * SQLConnection instance
     * */
    private static SQLConnection instance;

    /*
     * Original CSF file path
     * */
    private Path csvFilePath;

    /*
     * column separator
     */
    private String separator;

    /*
     * Composed JDBC Url for CsvData JDBC Driver
     */
    private String url;

    // Singleton pattern - protect default constructor
    private SQLConnection() {
        super();
    }

    /*
     * Initializing utility
     */
    static synchronized SQLConnection init(Path csvFilePath, String separator) {
        if (instance == null) {
            instance = new SQLConnection();
            instance.setCsvFilePath(csvFilePath);
            instance.setSeparator(separator);
        }
        return instance;
    }

    /*
     * Provided getInstace for SQLConnectoion singleton
     */
    static SQLConnection getInstance() {
        return init(null, null);
    }

    /*
     * returns Csv Original File
     */
    public Path getCsvFilePath() {
        return csvFilePath;
    }

    /*
     * Sets Csv Original File
     */
    public void setCsvFilePath(Path csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    /*
     * Generates operable temp file
     */
    public String getTempTableName() {
        String fullFilename = csvFilePath.toFile().getName();
        return fullFilename.lastIndexOf(".") != -1 ? fullFilename.substring(0, fullFilename.lastIndexOf("."))
                : fullFilename;
    }

    /*
     * returns Csv separator char as a string
     */
    public String getSeparator() {
        return separator;
    }

    /*
     * Sets separator char
     */
    public void setSeparator(String separator) {
        this.separator = separator;
    }

    /*
     * Composes and returns JDBC
     */
    public String getJdbcUrl() {
        if (url == null) {
            Path folder = csvFilePath.getParent();
            String fullFilename = csvFilePath.toFile().getName();
            String fileExtention = fullFilename.lastIndexOf(".") != -1
                    ? fullFilename.substring(fullFilename.lastIndexOf("."))
                    : "";
            url = "jdbc:relique:csv:"
//            + System.getProperty("user.dir") + "/"
                    + (folder != null ? folder.toString() : "") + "?" + "fileExtension=" + fileExtention;
        }
        return url;
    }

    /*
     * Provided connection for ST validation purposes.
     */
    public static Connection getConnection() throws SQLException {
        String separator = getInstance().getSeparator();
        Properties props = new Properties();
        props.put("separator", separator);
        props.put("suppressHeaders", "true");
        props.put("headerline",
                Columns.ID.toString() + separator + Columns.LOAN_TYPE.toString() + separator
                        + Columns.LOAN_ORIGINATOR.toString() + separator + Columns.SCORE_CLASS.toString() + separator
                        + Columns.GUARANTEED_PRINCIPAL.toString() + separator
                        + Columns.GUARANTEED_PRINCIPAL2.toString() + separator + Columns.TERM.toString() + separator
                        + Columns.INSTALMENT_TYPE.toString() + separator + Columns.STATUS.toString() + separator
                        + Columns.INTEREST_RATE.toString() + separator + Columns.AVAILABLE_FOR_INVESTMENT.toString() + separator
                        + Columns.DISCOUNT_PREMIUM.toString() + separator + Columns.PRICE.toString());
        props.put("columnTypes", "int,string,string,string,int,int,int,string,string,float,float,float,float");
        return DriverManager.getConnection(getInstance().getJdbcUrl(), props);
    }
}
