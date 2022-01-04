package personal.kcho.backend;

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
        System.out.println("\n");
    }

    public void testCreate(){
        System.out.println("\nTest: creating database tables");

        //Connect to the database
        String db_url = System.getenv("DATABASE_URL");
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
        createTables(db);
        dropTables(db);
    }

    public void testInsert(){
        System.out.println("\nTest: inserting rows into tables and selecting those rows");

        //Connect to the database
        String db_url = System.getenv("DATABASE_URL");
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
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
    }
}
