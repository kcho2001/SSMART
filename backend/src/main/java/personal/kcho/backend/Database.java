package personal.kcho.backend;

import java.util.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.postgresql.util.PSQLException;

public class Database {
    /**
     * The connection to the database.  When there is no connection, it should
     * be null.  Otherwise, there is a valid open connection
     */
    private Connection mConnection;

    //Initialization of preparedStatements
    //Create Tables
    private PreparedStatement createUser;
    private PreparedStatement createStretch;
    private PreparedStatement createSmart;

    //Drop Tables
    private PreparedStatement dropUser;
    private PreparedStatement dropStretch;
    private PreparedStatement dropSmart;

    //Select All
    private PreparedStatement selectUsers;
    private PreparedStatement selectStretchs;
    private PreparedStatement selectSmarts;

    //Delete row
    private PreparedStatement deleteUser;
    private PreparedStatement deleteSmart;
    private PreparedStatement deleteStretch;

    //Inserts
    private PreparedStatement insertUser;
    private PreparedStatement insertStretch;
    private PreparedStatement insertSmart;

    //Miscellaneous
    private PreparedStatement selectStretchByUser;
    private PreparedStatement selectSmartByStretch;

    //Count integer that will return information regarding operations with the database
    private int count = 0;

    /**
     * The Database constructor is private: we only create Database objects 
     * through the getDatabase() method.
     */
    private Database() {
    }

    class StretchGoalRow{
        String goal;
        
        public StretchGoalRow(String goal){
            this.goal = goal;
        }
    }

    class SmartGoalRow{
        String specific;
        String measureable;
        String attainable;
        String relevant;
        String time;

        public SmartGoalRow(String specific, String measureable, String attainable, String relevant, String time){
            this.specific = specific;
            this.measureable = measureable;
            this.attainable = attainable;
            this.relevant = relevant;
            this.time = time;
        }
    }

    /**
     * Get a fully-configured connection to the database
     * @param db_url
     */
    static Database getDatabase(String db_url) {
        // Create an un-configured Database object
        Database db = new Database();

        // Give the Database object a connection, fail if we cannot get one
        try {
            Class.forName("org.postgresql.Driver");
            URI dbUri = new URI(db_url);
            String username = dbUri.getUserInfo().split(":")[0];
            String password = dbUri.getUserInfo().split(":")[1];
            String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() + "?sslmode=require";
            Connection conn = DriverManager.getConnection(dbUrl, username, password);
            if (conn == null) {
                System.err.println("Error: DriverManager.getConnection() returned a null object");
                return null;
            }
            db.mConnection = conn;
        } catch (SQLException e) {
            System.err.println("Error: DriverManager.getConnection() threw a SQLException");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Unable to find postgresql driver");
            return null;
        } catch (URISyntaxException s) {
            System.out.println("URI Syntax Error");
            return null;
        }

        // Attempt to create all of our prepared statements.  If any of these 
        // fail, the whole getDatabase() call should fail
        try {
            // NB: we can easily get ourselves in trouble here by typing the
            //     SQL incorrectly.  We really should have things like "tblData"
            //     as constants, and then build the strings for the statements
            //     from those constants.

            // Note: no "IF NOT EXISTS" checks on table 
            // so multiple executions of creation will cause an exception
            // For explanation of how the database is setup, check README.md
            db.createUser = db.mConnection.prepareStatement("CREATE TABLE User (userID SERIAL PRIMARY KEY)");
                    
            db.createStretch = db.mConnection.prepareStatement("CREATE TABLE Stretch (stretchID SERIAL PRIMARY KEY, goal VARCHAR(300), authorID INT, "
            + "FOREIGN KEY (authorID) references User (userID))");

            db.createSmart = db.mConnection.prepareStatement("CREATE TABLE Smart (smartID SERIAL, stretchID INT, specific VARCHAR(200), "
            + "measureable VARCHAR(200), attainable VARCHAR(200), relevant VARCHAR(200), time VARCHAR(200), PRIMARY KEY (smartID, stretchID), "
            + "FOREIGN KEY (stretchID) references Stretch (stretchID))");

            db.dropUser = db.mConnection.prepareStatement("DROP TABLE IF EXISTS User");

            db.dropStretch = db.mConnection.prepareStatement("DROP TABLE IF EXISTS Stretch");

            db.dropSmart = db.mConnection.prepareStatement("DROP TABLE IF EXISTS Smart");

            //Select alls
            db.selectUsers = db.mConnection.prepareStatement("SELECT * FROM User");

            db.selectStretchs = db.mConnection.prepareStatement("SELECT * FROM Stretch");

            db.selectSmarts = db.mConnection.prepareStatement("SELECT * FROM Smart");

            //Insert row
            db.insertUser = db.mConnection.prepareStatement("INSERT INTO User VALUES (default)");

            db.insertStretch = db.mConnection.prepareStatement("INSERT INTO Stretch VALUES (default, ?, ?)");

            db.insertSmart = db.mConnection.prepareStatement("INSERT INTO User VALUES (default, ?, ?, ?, ?, ?, ?)");

            //Delete one specific row from a table
            db.deleteUser = db.mConnection.prepareStatement("DELETE FROM User WHERE userID = ?");

            db.deleteStretch = db.mConnection.prepareStatement("DELETE FROM Stretch WHERE stretchID = ?");

            db.deleteSmart = db.mConnection.prepareStatement("DELETE FROM Smart WHERE smartID = ? and stretchID = ?");

            //Miscellaneous
            db.selectStretchByUser = db.mConnection.prepareStatement("SELECT * FROM Stretch WHERE authorID = ?");
            db.selectSmartByStretch = db.mConnection.prepareStatement("SELECT * FROM Smart WHERE stretchID = ?");

        } catch (SQLException e) {
            System.err.println("Error creating prepared statement");
            e.printStackTrace();
            db.disconnect();
            return null;
        }
        return db;
    }

    /**
     * Close the current connection to the database, if one exists.
     * 
     * NB: The connection will always be null after this call, even if an 
     *     error occurred during the closing operation.
     * 
     * @return True if the connection was cleanly closed, false otherwise
     */
    boolean disconnect() {
        if (mConnection == null) {
            System.err.println("Unable to close connection: Connection was null");
            return false;
        }
        try {
            mConnection.close();
        } catch (SQLException e) {
            System.err.println("Error: Connection.close() threw a SQLException");
            e.printStackTrace();
            mConnection = null;
            return false;
        }
        mConnection = null;
        return true;
    }

    /**
     * Create User. If tables are created out of order (due to dependencies), there will be an error printed
     */
    void createUser() {
        try{
            createUser.execute();
            System.out.println("created User");
        }
        catch (PSQLException e) {
            String error = e.getLocalizedMessage();
            System.out.println(error);
            if(error.equals("ERROR: relation \"User\" does not exist")){
                System.out.println("Foreign key issue will be resolved when the Users table is created first!");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create Stretch table.
     * If tables are created out of order (due to dependencies), there will be an error printed
     */
    void createStretch() {
        try{
            createStretch.execute();
            System.out.println("created Stretch");
        }
        catch (PSQLException e) {
            String error = e.getLocalizedMessage();
            System.out.println(error);
            if(error.equals("ERROR: relation \"User\" does not exist")){
                System.out.println("Foreign key issue will be resolved when the Users table is created first!");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create Smart table.
     * If tables are created out of order (due to dependencies), there will be an error printed
     */
    void createSmart() {
        try{
            createSmart.execute();
            System.out.println("created Smart");
        }
        catch (PSQLException e) {
            String error = e.getLocalizedMessage();
            System.out.println(error);
            if(error.equals("ERROR: relation \"User\" does not exist")){
                System.out.println("Foreign key issue will be resolved when the Users table is created first!");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Insert a User into the database
     * @return The number of rows that were inserted
     */
    int insertUser() {
        count = 0;
        try {
            count += insertUser.executeUpdate();
            System.out.println("inserted " + count + " row(s) in User");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Insert a Stretch goal into the database
     * @param goal The actual content of the goal being inserted into the database
     * @return The number of rows that were inserted
     */
    int insertStretch(String goal, int authorID) {
        count = 0;
        try {
            insertStretch.setString(1, goal);
            insertStretch.setInt(2, authorID);
            count += insertStretch.executeUpdate();
            System.out.println("inserted " + count + " row(s) in Stretch");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Insert a SMART goal into the database
     * @param stretchID The ID of the stretch goal that the SMART goal is associated with
     * @param specific The specific part of the SMART goal
     * @param measureable The measureable part of the SMART goal
     * @param attainable The attainable part of the SMART goal
     * @param relevant The relevant part of the SMART goal
     * @param time The time part of the SMART goal
     * @return The number of rows that were inserted
     */
    int insertdb(int stretchID, String specific, String measureable, String attainable, String relevant, String time) {
        count = 0;
        try {
            insertSmart.setInt(1, stretchID);
            insertSmart.setString(2, specific);
            insertSmart.setString(3, measureable);
            insertSmart.setString(4, attainable);
            insertSmart.setString(5, relevant);
            insertSmart.setString(6, time);
            count += insertSmart.executeUpdate();
            System.out.println("inserted " + count + " row(s) in Smart");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Delete a User
     * @param userID The primary key of the user
     * @return The number of rows that were deleted.
     */
    int deleteUser(int userID) {
        count = 0;
        try {
            deleteUser.setInt(1, userID);
            count += deleteUser.executeUpdate();
            System.out.println("deleted " + count + " row(s) from User");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Delete a Stretch goal
     * @param stretchID The primary key of the stretch goal
     * @return The number of rows that were deleted.
     */
    int deleteStretch(int stretchID) {
        count = 0;
        try {
            deleteStretch.setInt(1, stretchID);
            count += deleteStretch.executeUpdate();
            System.out.println("deleted " + count + " row(s) from Stretch");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Delete a Smart goal
     * @param smartID The id of the smart goal
     * @param stretchID The id of the stretch goal that it is associated with
     * @return The number of rows that were deleted.
     */
    int deleteSmart(int smartID, int stretchID) {
        count = 0;
        try {
            deleteSmart.setInt(1, smartID);
            deleteSmart.setInt(1, stretchID);
            count += deleteSmart.executeUpdate();
            System.out.println("deleted " + count + " row(s) from Smart");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Return all of the Smart goals associated with a stretch goal
     * @param stretchID the id of the stretch goal that we want the SMART goals for
     * @return The list of SMART goals
     */
    ArrayList<SmartGoalRow> selectSmarts() {
        ArrayList<SmartGoalRow> smartGoals = new ArrayList<SmartGoalRow>();
        try {
            ResultSet rs = selectSmarts.executeQuery();
            while (rs.next()) {
                smartGoals.add(new SmartGoalRow(rs.getString("specific"), rs.getString("measureable"), rs.getString("attainable"), rs.getString("relevant"), rs.getString("time")));
            }
            if(smartGoals.size() == 0){
                System.out.println("selected 0 rows from Smart");
                return null;
            }
            System.out.println("selected all SMART goals");
            return smartGoals;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return all of the Stretch goals written by a particular user
     * @param authorID the one who made the stretch goals
     * @return The list of stretch goals
     */
    ArrayList<StretchGoalRow> selectStretchs(int authorID) {
        ArrayList<StretchGoalRow> stretchGoals = new ArrayList<StretchGoalRow>();
        try {
            ResultSet rs = selectStretchs.executeQuery();
            while (rs.next()) {
                stretchGoals.add(new StretchGoalRow(rs.getString("goal")));
            }
            if(stretchGoals.size() == 0){
                System.out.println("selected 0 rows from Stretch");
                return null;
            }
            System.out.println("selected all Stretch goals");
            return stretchGoals;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return all of the Stretch goals written by a particular user
     * @param authorID the one who made the stretch goals
     * @return The list of stretch goals
     */
    ArrayList<StretchGoalRow> selectStretchByUser(int authorID) {
        ArrayList<StretchGoalRow> stretchGoals = new ArrayList<StretchGoalRow>();
        try {
            selectStretchByUser.setInt(1, authorID);
            ResultSet rs = selectStretchByUser.executeQuery();
            while (rs.next()) {
                stretchGoals.add(new StretchGoalRow(rs.getString("goal")));
            }
            if(stretchGoals.size() == 0){
                System.out.println("selected 0 rows from Stretch");
                return null;
            }
            System.out.println("selected stretch goals made by user " + authorID);
            return stretchGoals;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return all of the Smart goals associated with a stretch goal
     * @param stretchID the id of the stretch goal that we want the SMART goals for
     * @return The list of SMART goals
     */
    ArrayList<SmartGoalRow> selectSmartByStretch(int stretchID) {
        ArrayList<SmartGoalRow> smartGoals = new ArrayList<SmartGoalRow>();
        try {
            selectSmartByStretch.setInt(1, stretchID);
            ResultSet rs = selectSmartByStretch.executeQuery();
            while (rs.next()) {
                smartGoals.add(new SmartGoalRow(rs.getString("specific"), rs.getString("measureable"), rs.getString("attainable"), rs.getString("relevant"), rs.getString("time")));
            }
            if(smartGoals.size() == 0){
                System.out.println("selected 0 rows from Smart");
                return null;
            }
            System.out.println("selected SMART goals for stretch goal " + stretchID);
            return smartGoals;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Remove User table from the database.
     * If tables are dropped in the wrong order (due to dependencies), an error is printed
     */
    void dropUser() {
        try{
            dropUser.execute();
            System.out.println("dropped User");
        } catch (SQLException e) {
            int errNo = e.getErrorCode();
            if (errNo == 0){
                System.out.println("Error: Drop tables in the correct order to prevent violations of foreign keys!");
                System.out.println("Drop Smart, Streth, and then User");
            }
            else{
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove Stretch table from the database.
     * If tables are dropped in the wrong order (due to dependencies), an error is printed
     */
    void dropStretch() {
        try{
            dropStretch.execute();
            System.out.println("dropped Stretch");
        } catch (SQLException e) {
            int errNo = e.getErrorCode();
            if (errNo == 0){
                System.out.println("Error: Drop tables in the correct order to prevent violations of foreign keys!");
                System.out.println("Drop Smart, Streth, and then User");
            }
            else{
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove Smart table from the database.
     * If tables are dropped in the wrong order (due to dependencies), an error is printed
     */
    void dropSmart() {
        try{
            dropSmart.execute();
            System.out.println("dropped Smart");
        } catch (SQLException e) {
            int errNo = e.getErrorCode();
            if (errNo == 0){
                System.out.println("Error: Drop tables in the correct order to prevent violations of foreign keys!");
                System.out.println("Drop Smart, Streth, and then User");
            }
            else{
                e.printStackTrace();
            }
        }
    }
}