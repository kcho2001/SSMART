package personal.kcho.backend;

import java.util.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import personal.kcho.backend.Database.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    String db_url = "postgres://bsicetporpfarr:09e2fd2095f5cb2a28dfd27f8d5460e709a684eed4c24b2da9dd9615b9155c37@ec2-18-233-104-114.compute-1.amazonaws.com:5432/da33ktj2nj43fj";
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Method to simplify table creation
     */
    public void createTables(Database db)
    {
        db.createUsers();
        db.createStretch();
        db.createSmart();
    }

    /**
     * Method to simplify table deletion
     */
    public void dropTables(Database db)
    {
        db.dropSmart();
        db.dropStretch();
        db.dropUsers();
    }

    public void testCreate(){
        System.out.println("\nTest: creating database tables");
        //Connect to the database
        //String db_url = System.getenv("DATABASE_URL");
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
        dropTables(db);
        createTables(db);
        dropTables(db);
        System.out.println("\n");
    }

    public void testInsert(){
        System.out.println("\nTest: inserting rows into tables and selecting specific rows");

        //Connect to the database
        //String db_url = System.getenv("DATABASE_URL");
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
        dropTables(db);
        createTables(db);
        db.insertUser("Kenny");
        db.insertStretch("Stretch Goal 1", 1);
        db.insertStretch("Stretch Goal 2", 1);
        db.insertSmart(1, "specific", "measureable", "attainable", "relevant", "time");
        db.insertSmart(2, "s", "m", "a", "r", "t");
        db.insertSmart(1, "spec", "meas", "atta", "rele", "time");
        UserRow user = db.selectUser(1);
        StretchGoalRow stretch1 = db.selectStretch(1);
        StretchGoalRow stretch2 = db.selectStretch(2);
        SmartGoalRow smart1 = db.selectSmart(1, 1);
        SmartGoalRow smart2 = db.selectSmart(2, 2);
        SmartGoalRow smart3 = db.selectSmart(3, 1);
        assertEquals(user.name, "Kenny");
        assertEquals(stretch1.goal, "Stretch Goal 1");
        assertEquals(stretch2.goal, "Stretch Goal 2");
        assertEquals(smart1.attainable, "attainable");
        assertEquals(smart2.measureable, "m");
        assertEquals(smart3.relevant, "rele");
        dropTables(db);
        System.out.println("\n");
    }

    public void testSelectAll(){
        System.out.println("\nTest: inserting rows into tables and selecting all rows");

        //Connect to the database
        //String db_url = System.getenv("DATABASE_URL");
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
        dropTables(db);
        createTables(db);
        db.insertUser("Kenny");
        db.insertUser("Foo");
        db.insertUser("Bar");
        db.insertStretch("Stretch Goal 1", 1);
        db.insertStretch("Stretch Goal 2", 3);
        db.insertSmart(1, "specific", "measureable", "attainable", "relevant", "time");
        db.insertSmart(2, "s", "m", "a", "r", "t");
        db.insertSmart(1, "spec", "meas", "atta", "rele", "time");
        ArrayList<UserRow> users = db.selectUsers();
        ArrayList<StretchGoalRow> stretchGoals = db.selectStretchs();
        ArrayList<SmartGoalRow> smartGoals = db.selectSmarts();
        assertEquals(users.get(0).name, "Kenny");
        assertEquals(users.get(1).name, "Foo");
        assertEquals(users.get(2).name, "Bar");
        assertEquals(stretchGoals.get(0).goal, "Stretch Goal 1");
        assertEquals(stretchGoals.get(1).goal, "Stretch Goal 2");
        assertEquals(smartGoals.get(0).measureable, "measureable");
        assertEquals(smartGoals.get(1).specific, "s");
        assertEquals(smartGoals.get(1).attainable, "a");
        assertEquals(smartGoals.get(2).time, "time");
        assertEquals(smartGoals.get(2).relevant, "rele");
        dropTables(db);
        System.out.println("\n");
    }

    public void testDelete(){
        System.out.println("\nTest: insert rows and then delete them (also test delete cascades)");

        //Connect to the database
        //String db_url = System.getenv("DATABASE_URL");
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
        dropTables(db);
        createTables(db);
        db.insertUser("Kenny");
        db.insertStretch("Stretch Goal 1", 1);
        db.insertSmart(1, "specific", "measureable", "attainable", "relevant", "time");
        UserRow user = db.selectUser(1);
        StretchGoalRow stretch = db.selectStretch(1);
        SmartGoalRow smart = db.selectSmart(1, 1);
        assertEquals(user.name, "Kenny");
        assertEquals(stretch.goal, "Stretch Goal 1");
        assertEquals(smart.specific, "specific");
        db.deleteSmart(1, 1);
        db.deleteStretch(1);
        stretch = db.selectStretch(1);
        smart = db.selectSmart(1,1);
        assertEquals(stretch, null);
        assertEquals(smart, null);
        //Testing if delete cascade works for deleting a stretch goal, and not the associated smart goal first
        db.insertStretch("Stretch Goal 1", 1);
        db.insertSmart(2, "specific", "measureable", "attainable", "relevant", "time");
        db.deleteStretch(2);
        stretch = db.selectStretch(2);
        smart = db.selectSmart(2,2);
        assertEquals(stretch, null);
        assertEquals(smart, null);

        //Testing if delete cascade works for deleting user (when user is deleted, any database reference of stretch goals to this user would also be deleted)
        //Therefore, also deleting the SMART goals associated to the stretch goal that was referencing the user
        db.insertStretch("Stretch Goal 1", 1);
        db.insertSmart(3, "specific", "measureable", "attainable", "relevant", "time");
        db.deleteUser(1);
        user = db.selectUser(1);
        stretch = db.selectStretch(3);
        smart = db.selectSmart(3,3);
        assertEquals(user, null);
        assertEquals(stretch, null);
        assertEquals(smart, null);
        dropTables(db);
        System.out.println("\n");
    }

    public void testUpdate(){
        System.out.println("\nTest: inserting rows into tables and updating rows");

        //Connect to the database
        //String db_url = System.getenv("DATABASE_URL");
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
        dropTables(db);    
        createTables(db);
        db.insertUser("Kenny");
        db.insertUser("Foo");
        db.insertStretch("Stretch Goal 1", 1);
        db.insertStretch("Stretch Goal 2", 2);
        db.insertSmart(1, "specific", "measureable", "attainable", "relevant", "time");
        db.insertSmart(2, "s", "m", "a", "r", "t");
        assertEquals(db.updateUser(1, "Kenneth"), 1);
        assertEquals(db.updateUser(2, "Bar"), 1);
        assertEquals(db.updateStretch(1, "Updated Stretch Goal 1"), 1);
        assertEquals(db.updateStretch(2, "Updated Stretch Goal 2"), 1);
        assertEquals(db.updateSmart(1, 1, "Updated specific", "updated measureable", "updated attainable", "updated relevant", "updated time"), 1);
        assertEquals(db.updateSmart(2, 2, "Updated s", "updated m", "updated a", "updated r", "updated t"), 1);
        //Trying to update rows that don't exist
        assertEquals(db.updateUser(3, "fake"), 0);
        assertEquals(db.updateStretch(4, "fake"), 0);
        assertEquals(db.updateSmart(3, 2, "Updated s", "updated m", "updated a", "updated r", "updated t"), 0);
        assertEquals(db.updateSmart(2, 4, "Updated s", "updated m", "updated a", "updated r", "updated t"), 0);

        //Select all of the rows and check that the update went through
        ArrayList<UserRow> users = db.selectUsers();
        ArrayList<StretchGoalRow> stretchGoals = db.selectStretchs();
        ArrayList<SmartGoalRow> smartGoals = db.selectSmarts();
        assertEquals(users.get(0).name, "Kenneth");
        assertEquals(users.get(1).name, "Bar");
        assertEquals(stretchGoals.get(0).goal, "Updated Stretch Goal 1");
        assertEquals(stretchGoals.get(1).goal, "Updated Stretch Goal 2");
        assertEquals(smartGoals.get(0).specific, "Updated specific");
        assertEquals(smartGoals.get(0).measureable, "updated measureable");
        assertEquals(smartGoals.get(1).attainable, "updated a");
        assertEquals(smartGoals.get(1).relevant, "updated r");
        assertEquals(smartGoals.get(1).time, "updated t");
        dropTables(db);
        System.out.println("\n");
    }

    public void testMiscellaneous(){
        System.out.println("\nTest: miscellaneous selects");

        //Connect to the database
        //String db_url = System.getenv("DATABASE_URL");
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
        dropTables(db);
        createTables(db);
        db.insertUser("Kenny");
        db.insertUser("Foo");
        db.insertStretch("Stretch Goal by Kenny 1", 1);
        db.insertStretch("Stretch Goal by Kenny 2", 1);
        db.insertStretch("Stretch Goal 1 by Foo", 2);
        db.insertStretch("Stretch Goal 2 by Foo", 2);
        db.insertSmart(1, "specific", "measureable", "attainable", "relevant", "time");
        db.insertSmart(2, "s", "m", "a", "r", "t");
        db.insertSmart(1, "spec", "meas", "atta", "rele", "time");
        db.insertSmart(2, "Foo", "Bar", "X", "Y", "Z");
        db.insertSmart(3, "Fao", "Bbr", "X", "Y", "Z");
        db.insertSmart(3, "Fbo", "Bcr", "X", "Y", "Z");
        db.insertSmart(4, "Fco", "Bdr", "X", "Y", "Z");
        db.insertSmart(4, "Fdo", "Ber", "X", "Y", "Z");

        //Checking stretch goals made by Kenny
        ArrayList<StretchGoalRow> stretchGoals = db.selectStretchByUser(1);
        assertEquals(stretchGoals.get(0).goal, "Stretch Goal by Kenny 1");
        assertEquals(stretchGoals.get(1).goal, "Stretch Goal by Kenny 2");
        //Checking stretch goals made by Foo
        stretchGoals = db.selectStretchByUser(2);
        assertEquals(stretchGoals.get(0).goal, "Stretch Goal 1 by Foo");
        assertEquals(stretchGoals.get(1).goal, "Stretch Goal 2 by Foo");

        //Looking at SMART Goals for stretch goal 1
        ArrayList<SmartGoalRow> smartGoals = db.selectSmartByStretch(1);
        assertEquals(smartGoals.get(0).specific, "specific");
        assertEquals(smartGoals.get(1).specific, "spec");
        //Looking at SMART Goals for stretch goal 2
        smartGoals = db.selectSmartByStretch(2);
        assertEquals(smartGoals.get(0).measureable, "m");
        assertEquals(smartGoals.get(1).measureable, "Bar");
        //Looking at SMART Goals for stretch goal 3
        smartGoals = db.selectSmartByStretch(3);
        assertEquals(smartGoals.get(0).attainable, "X");
        assertEquals(smartGoals.get(1).relevant, "Y");
        //Looking at SMART Goals for stretch goal 4
        smartGoals = db.selectSmartByStretch(4);
        assertEquals(smartGoals.get(0).specific, "Fco");
        assertEquals(smartGoals.get(1).time, "Z");
        dropTables(db);
        System.out.println("\n");
    }
}
