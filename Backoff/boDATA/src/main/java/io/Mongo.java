package io;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import java.io.PrintStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import model.Itinerary;
import model.POI;
import model.Visit;
import model.VisitPlan;
import model.VisitPlanAlternatives;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import services.CategoriesDictionary;
import util.PointCodec;

import static io.LoginInfo.MONGO_DB;
import static io.LoginInfo.MONGO_PASSWORD;
import static io.LoginInfo.MONGO_USER;
import static services.CategoriesDictionary.CAT;

public class Mongo
{
  private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Mongo.class);
  private ObjectMapper mapper;
  private MongoClient mongoClient;
  private MongoDatabase db;
  private java.util.logging.Logger mongoLogger;
  
  public static void main(String[] args)
  {
    String user = "a5464575677";
    Mongo dao = new Mongo();
    Map<String, Double> prefs = dao.getPrefs(user);
    System.out.println(prefs);
    dao.updatePrefs(user, "attractions", 1.0D);
    dao.updatePrefs(user, "attractions", 1.0D);
    dao.updatePrefs(user, "attractions", 1.0D);
    prefs = dao.getPrefs(user);
    System.out.println(prefs);
  }
  
  public Mongo()
  {
    CodecRegistry codecRegistry =
            CodecRegistries.fromRegistries(
                    CodecRegistries.fromCodecs(new PointCodec()),
                    MongoClient.getDefaultCodecRegistry());


    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    mongoLogger = java.util.logging.Logger.getLogger( "org.mongodb.driver" );
    mongoLogger.setLevel(Level.SEVERE);
    mongoClient = new MongoClient(new ServerAddress(LoginInfo.MONGO_URL), Arrays.asList(
            MongoCredential.createCredential(
                    MONGO_USER,
                    MONGO_DB,
                    MONGO_PASSWORD.toCharArray())),
            MongoClientOptions.builder().codecRegistry(codecRegistry).build());
    db = mongoClient.getDatabase(MONGO_DB);
    logger.info("loading new db "+MONGO_DB+" .....");
  }

  
  public void testInsertion()
    throws ParseException
  {
    this.db.getCollection("testCollection").insertOne(new Document("id", new Document("id1", "key1")
      .append("id2", "key2")));
    List<Document> docList = new ArrayList();
    docList.add(new Document("key1", "value1"));
    docList.add(new Document("key2", "value2"));
    docList.add(new Document("key3", "value3"));
    this.db.getCollection("testCollection").insertMany(docList);
  }
  
  public void insertActivity(String city, POI poi)
  {
    try
    {
      if (this.db.getCollection(city + "activities").find(new Document("place_id", poi.getPlace_id())).first() == null) {
        this.db.getCollection(city + "activities").insertOne(Document.parse(poi.toJSONString()));
      }
    }
    catch (Exception e)
    {
      this.logger.info(e.getMessage());
    }
  }
  
  public boolean deleteActivities(String city)
  {
    try
    {
      this.db.getCollection(city + "activities").drop();
      return true;
    }
    catch (Exception e)
    {
      this.logger.info(e.getMessage());
    }
    return false;
  }
  
  public void insertItinerary(String city, Itinerary itinerary)
  {
    try
    {
      if (this.db.getCollection(city + "itineraries").find(new Document("itinerary_id", itinerary.getItinerary_id())).first() == null) {
        this.db.getCollection(city + "itineraries").insertOne(Document.parse(itinerary.toJSONString()));
      }
    }
    catch (Exception e)
    {
      this.logger.info(e.getMessage());
    }
  }
  
  public boolean deleteItinerary(String city, String itinerary)
  {
    try
    {
      if (this.db.getCollection(city + "itineraries").find(new Document("itinerary_id", itinerary)).first() != null)
      {
        this.db.getCollection(city + "itineraries").findOneAndDelete(new Document("itinerary_id", itinerary));
        return true;
      }
    }
    catch (Exception e)
    {
      this.logger.info(e.getMessage());
      return false;
    }
    return false;
  }
  
  public void insertFBData(String fbdata)
  {
    this.db.getCollection("fbdata").insertOne(Document.parse(fbdata));
  }
  
  public MongoCollection<Document> retrieveFBData()
  {
    return this.db.getCollection("fbdata");
  }
  
  public void testQuery()
  {
    MongoCollection<Document> collection = this.db.getCollection("testCollection");
    FindIterable<Document> cursor = collection.find();
    for (Iterator<Document> iter = cursor.iterator(); iter.hasNext();) {
      System.out.println(iter.next());
    }
  }
  
  public List<POI> retrieveActivities(String city)
  {
    List<POI> result = new ArrayList();
    Iterator<Document> iter;
    try
    {
      for (iter = this.db.getCollection(city + "activities").find().iterator(); iter.hasNext();)
      {
        POI p = (POI)this.mapper.readValue(((Document)iter.next()).toJson(), POI.class);
        p.setDisplay_name(Normalizer.normalize(p.getDisplay_name(), Normalizer.Form.NFD).replaceAll("[^\\x00-\\x7F]", "").replaceAll("''", "'"));
        result.add(p);
      }
    }
    catch (Exception e)
    {

      e.printStackTrace();
    }
    this.logger.info(result.size()+" activities retrieved for "+city);
    return result;
  }
  
  public boolean checkActivities(String city)
  {
    return this.db.getCollection(city + "activities").count() != 0L;
  }
  
  public POI retrieveActivity(String city, String place_id)
  {
    try
    {
      return (POI)this.mapper.readValue(((Document)this.db.getCollection(city + "activities").find(new Document("place_id", place_id)).iterator().next()).toJson(), POI.class);
    }
    catch (Exception e)
    {
      this.logger.info(e.getMessage());
    }
    return null;
  }
  
  public List<Itinerary> retrieveItineraries(String city)
  {
    List<Itinerary> result = new ArrayList();
    Iterator<Document> iter;
    try
    {
      for (iter = this.db.getCollection(city + "itineraries").find().iterator(); iter.hasNext();)
      {
        Itinerary i = (Itinerary)this.mapper.readValue(((Document)iter.next()).toJson(), Itinerary.class);
        i.setDisplay_name(Normalizer.normalize(i.getDisplay_name(), Normalizer.Form.NFD).replaceAll("[^\\x00-\\x7F]", "").replaceAll("''", "'"));
        result.add(i);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return result;
  }
  
  private Document signup(String user)
  {
    Document userRecord = (Document)this.db.getCollection("users").find(new Document("user", user)).first();
    if (null == userRecord)
    {
      this.logger.info("Creating new user account for " + user);
      userRecord = new Document();
      userRecord.append("user", user);
      
      Map<String, Double> prefs = new HashMap();
      for (String cat : CategoriesDictionary.CAT) {
        prefs.put(cat, Double.valueOf(0.0D));
      }
      userRecord.append("prefs", prefs);
      
      this.db.getCollection("users").insertOne(userRecord);
      return userRecord;
    }
    this.logger.info("User " + user + " already exists");
    return userRecord;
  }
  
  public Map<String, Double> getPrefs(String user)
  {
    Document userRecord = (Document)this.db.getCollection("users").find(new Document("user", user)).first();
    if (userRecord == null) {
      userRecord = signup(user);
    }
    return (Map)userRecord.get("prefs");
  }
  
  public void updatePrefs(String user, Map<String, Double> newprefs)
  {
    Document userRecord = (Document)this.db.getCollection("users").find(new Document("user", user)).first();
    if (userRecord == null) {
      userRecord = signup(user);
    }
    userRecord.put("prefs", newprefs);
    this.db.getCollection("users").replaceOne(new BasicDBObject().append("user", user), userRecord);
  }
  
  public Map<String, Double> updatePrefs(String user, String cat, double delta)
  {
    Document userRecord = (Document)this.db.getCollection("users").find(new Document("user", user)).first();
    if (userRecord == null) {
      userRecord = signup(user);
    }
    Map<String, Double> prefs = (Map)userRecord.get("prefs");
    prefs.put(cat, Double.valueOf(((Double)prefs.get(cat)).doubleValue() + delta));
    this.db.getCollection("users").replaceOne(new BasicDBObject().append("user", user), userRecord);
    return prefs;
  }
  
  public boolean insertPlan(VisitPlanAlternatives plans)
  {
    VisitPlan plan_accepted = plans.get(plans.getSelected());
    try
    {
      Document userPlanRecord = (Document)this.db.getCollection("plans").find(new Document("user", plan_accepted.getUser())).first();
      if (null == userPlanRecord)
      {
        this.logger.info("Creating new visit plan for user " + plan_accepted.getUser());
        this.db.getCollection("plans").insertOne(Document.parse(plans.toJSONString()));
        return true;
      }
      this.logger.info("Updating visit plan for user " + plan_accepted.getUser());
      this.db.getCollection("plans").findOneAndReplace(userPlanRecord, Document.parse(plans.toJSONString()));
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  public VisitPlanAlternatives retrievePlan(String user)
  {
    try
    {
      Document userPlanRecord = (Document)this.db.getCollection("plans").find(new Document("user", user)).first();
      if (null == userPlanRecord) {
        return null;
      }
      this.logger.info("Getting visit plan for user " + user);
      return (VisitPlanAlternatives)this.mapper.readValue(userPlanRecord.toJson(), VisitPlanAlternatives.class);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public VisitPlanAlternatives updatePlan(Visit new_visited)
  {
    try
    {
      this.logger.info("Trying to find the plan for user " + new_visited.getUser());
      Document userPlanRecord = (Document)this.db.getCollection("plans").find(new Document("user", new_visited.getUser())).first();
      if (null != userPlanRecord)
      {
        VisitPlanAlternatives current = (VisitPlanAlternatives)this.mapper.readValue(userPlanRecord.toJson(), VisitPlanAlternatives.class);
        for (VisitPlan p : current.getPlans().values()) {
          p.updatePlan(new_visited);
        }
        this.db.getCollection("plans").findOneAndReplace(userPlanRecord, Document.parse(current.toJSONString()));
        return current;
      }
    }
    catch (Exception e)
    {
      this.logger.info(e.getMessage());
    }
    this.logger.info("user plan not found or exception");
    return null;
  }
  
  public boolean deletePlan(String user_mail)
  {
    try
    {
      DeleteResult userPlanRecord = this.db.getCollection("plans").deleteOne(new Document("crowd.user", user_mail));
      if (0L == userPlanRecord.getDeletedCount())
      {
        this.logger.info("Visiting Plan not found for removal " + user_mail);
        return false;
      }
      this.logger.info("Deleting Visiting Plan for user " + user_mail);
      return true;
    }
    catch (Exception e)
    {
      this.logger.info("Deleting Visiting Plan for user " + user_mail + " thrown an exception: " + e.getMessage());
    }
    return false;
  }
}
