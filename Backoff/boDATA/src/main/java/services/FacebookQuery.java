package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import io.Mongo;
import io.RESTController;
import model.POI;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

public class FacebookQuery {



	public static void main(String[] args) throws Exception  {
		FacebookQuery fbq = new FacebookQuery();
		Mongo dao = new Mongo();
		fbq.run(dao);
	}


	private Logger logger = Logger.getLogger(RESTController.class);



	public void run(Mongo dao) {
		MongoCollection<Document> collection = dao.retrieveFBData();
		FindIterable<Document> cursor = collection.find();


		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);



		for (Iterator<Document> iter = cursor.iterator(); iter.hasNext();) {
			Document doc = iter.next();
			JSONObject obj = new JSONObject(doc.toJson());
			String id = obj.getString("id");
			String first_name = obj.getJSONObject("generalInfo").getString("first_name");
			String last_name = obj.getJSONObject("generalInfo").getString("last_name");
			String birthday = obj.getJSONObject("generalInfo").getString("birthday");
			String gender = obj.getJSONObject("generalInfo").getString("gender");
			System.out.println("ID: "+id+" => "+first_name+" "+last_name+" ("+gender+") nato il "+birthday);
			System.out.println("LIKES: ");

			JSONArray likes = obj.getJSONObject("generalInfo").getJSONObject("likes").getJSONArray("data");
			for (int i = 0; i < likes.length(); i++) {
				String like = likes.getJSONObject(i).getString("name");
				System.out.println("\t"+(i+1)+". "+like);
			}
			System.out.println("EVENTS: ");
			JSONArray events = obj.getJSONArray("eventsInfo");
			for (int i = 0; i < events.length(); i++) {
				String event = events.getJSONObject(i).getString("name");
				System.out.println("\t"+(i+1)+". "+event);
			}


		}

	}
}