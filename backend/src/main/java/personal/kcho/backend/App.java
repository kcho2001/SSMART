package personal.kcho.backend;

import spark.Spark;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.*;

import com.google.gson.*;

import personal.kcho.backend.Database.*;

public class App 
{
    
    public static void main( String[] args )
    {
        final Gson gson = new Gson();
        //Connect to the database
        String db_url = System.getenv("DATABASE_URL");
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
        Spark.port(4567);

        Spark.get("/", (request, response) -> {
            response.redirect("/index.html");
            return "";
        });

        //Request to query for all users in the database
        Spark.get("/users", (request, response) -> {
            response.status(200);
            response.type("application/json");
            ArrayList<UserRow> data = db.selectUsers();
            return gson.toJson(new StructuredResponse("ok", null, data));
        });

        //Request to query for all Stretch goals in the database
        Spark.get("/stretchs", (request, response) -> {
            response.status(200);
            response.type("application/json");
            ArrayList<StretchGoalRow> data = db.selectStretchs();
            return gson.toJson(new StructuredResponse("ok", null, data));
        });

        //Request to query for all Smart goals in the database
        Spark.get("/smarts", (request, response) -> {
            response.status(200);
            response.type("application/json");
            ArrayList<SmartGoalRow> data = db.selectSmarts();
            return gson.toJson(new StructuredResponse("ok", null, data));
        });
        
    }
}