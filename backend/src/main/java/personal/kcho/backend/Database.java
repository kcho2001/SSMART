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


    /**
     * The Database constructor is private: we only create Database objects 
     * through the getDatabase() method.
     */
    private Database() {
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

    int count = 0;

    /**
     * Insert a db into the database
     * @param userID The ID of the user who is doing the dbed
     * @param dbID The ID of the user to be dbed
     * @return The number of rows that were inserted
     */
    int insertdb(int userID, int dbID) {
        count = 0;
        try {
            insertdb.setInt(1, userID);
            insertdb.setInt(2, dbID);
            count += insertdb.executeUpdate();
            System.out.println("inserted " + count + " row(s) in db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Query the database to see all of the dbed users for a given user
     * @param userID the one who dbed the users
     * @return The list of dbed users
     */
    ArrayList<Integer> selectAlldbed(int userID) {
        ArrayList<Integer> dbedUsers = new ArrayList<Integer>();
        try {
            selectAlldbed.setInt(1, userID);
            ResultSet rs = selectAlldbed.executeQuery();
            if (rs.next()) {
                dbedUsers.add(rs.getInt("dbedID"));
            }
            if(dbedUsers.size() == 0){
                System.out.println("selected 0 rows from db");
                return null;
            }
            System.out.println("selected ID's of users dbed by " + userID);
            return dbedUsers;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete a row by primary key
     * @param userID The one undbing a user
     * @param dbID The id of the user to be undbed
     * @return The number of rows that were deleted.
     */
    int deletedb(int userID, int dbID) {
        int res = 0;
        try {
            deletedb.setInt(1, userID);
            deletedb.setInt(2, dbID);
            res = deletedb.executeUpdate();
            System.out.println("deleted " + res + " row(s) from db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Create tblData.  If it already exists, this will print an error
     * If tables are created out of order (due to dependencies), there will be an error printed
     */
    void createTable() {
        try{
            createdb.execute();
            System.out.println("created db");
        }
        catch (PSQLException e) {
            String error = e.getLocalizedMessage();
            System.out.println(error);
            if(error.equals("ERROR: relation \"users\" does not exist")){
                System.out.println("Foreign key issue will be resolved when the Users table is created first!");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove tblData from the database.  If it does not exist, this will print an error.
     * If tables are dropped in the wrong order (due to dependencies), an error is printed
     */
    void dropTable() {
        try{
            dropdb.execute();
            System.out.println("dropped db");
        } catch (SQLException e) {
            int errNo = e.getErrorCode();
            if (errNo == 0){
                System.out.println("Error: Drop tables in the correct order to prevent violations of foreign keys!");
                System.out.println("Drop Flags, db, Votes, then Comments, then Messages, and lastly Users");
            }
            else{
                e.printStackTrace();
            }
        }
    }   
}