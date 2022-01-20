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
        //String db_url = System.getenv("DATABASE_URL");
        String db_url = "postgres://bsicetporpfarr:09e2fd2095f5cb2a28dfd27f8d5460e709a684eed4c24b2da9dd9615b9155c37@ec2-18-233-104-114.compute-1.amazonaws.com:5432/da33ktj2nj43fj";
        Database db = Database.getDatabase(db_url);

        if(db == null) //Ensure that we are connected to the database
            return;
        
        //Set default port variable
        String port = System.getenv("PORT");
        if(port == null){
            Spark.port(4567);
        }
        else{
            Spark.port(Integer.parseInt(port));
        }
        //Tell Spark where the static html files are located: Spark initially assumes -> /src/main/resources/
        Spark.staticFileLocation("/web");

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