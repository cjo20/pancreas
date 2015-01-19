import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ParallelScanOptions;

import org.bson.BSONObject;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.io.FileWriter;
import java.text.SimpleDateFormat;

import java.net.UnknownHostException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Pancreas {
    
    public static void main(String[] args)
    {
    	MongoClient mongoClient = null;

    	try
    	{
    		//Hidden.uri is a mongoDB URI, put in a seperate file to try and stop me from commiting my login details to git
    		mongoClient = new MongoClient(new MongoClientURI(Hidden.uri));
    	}
    	catch (UnknownHostException uhe)
    	{
    		System.out.println("Unable to connect to host " + uhe.getMessage());
    	}

    	DB db = mongoClient.getDB("cjo20db");
    	DBCollection collection = db.getCollection("pancreas");
    	DBCursor results = collection.find();

    	int i = 0;

    	List<DBObject> resultsList = results.toArray();
    	ArrayList<Treatment> treatments = new ArrayList<Treatment>();

    	for (DBObject obj : resultsList)
    	{
    		BasicDBObject treatment = (BasicDBObject) obj.get("Treatment");
    		double amount = (double) treatment.get("amount");
    		int time = (int)(double) treatment.get("time");
    		treatments.add(new Treatment(null, amount, time));
    	}

		int currentTime = (int)(System.currentTimeMillis() / 1000);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

		double startingBG = 180;
		try
		{
			FileWriter writer = new FileWriter("test.csv");
			for (i = -3*60; i < 120; ++i)
			{
				java.util.Date time = new java.util.Date((long)(currentTime + i * 60) * 1000);
				writer.append(dateFormat.format(time));
				writer.append(",");
				InsulinModel m = InsulinModel.calcContrib(treatments, currentTime + (i * 60));
				startingBG-= m.activityContrib;
				writer.append(m.insulinContrib + "," + m.activityContrib);
				writer.append("," + startingBG);
				writer.append("\n");
			}
			writer.flush();
			writer.close();
		}
		catch (Exception e)
		{
			System.out.println("Error");
		}		
		System.out.println("Exiting...");
    }
}
