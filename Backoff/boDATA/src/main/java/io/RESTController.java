package io;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.SerializationFeature;
import model.City;
import model.Itinerary;
import model.POI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import services.Config;
import services.ItineraryGenerator;
import services.NominatimPOIsDownload;
import services.SavePOIs2DB;

import org.json.JSONObject;
import services.*;
import model.*;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import util.StringUtils;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping({"/"})
public class RESTController
{
  private Logger logger = Logger.getLogger(RESTController.class);
  static final Logger tracelog = Logger.getLogger("dataMgmtLogger");
  private Mongo dao;
  private List<City> destinazioni;
  private Config properties;
  
  public RESTController()
  {
    this.logger.info("Data Mgmt service initialization started");
    this.dao = new Mongo();
    this.properties = new Config();
    this.destinazioni = City.getInstance(this.properties.getProperty("cities"));
    
    //updateCity(new City("prova", "prova", "prova", new double[] { 12.0D, 45.0D, 11.0D, 44.0D }));
  }
  
  @RequestMapping(value={"signin"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody public boolean signin(@RequestBody String hasheduser)
  {
    try
    {
      //this.logger.info("INFO:" + hasheduser);
      byte[] bytesOfMessage = (this.properties.getProperty("user") + this.properties.getProperty("pass")).getBytes(StandardCharsets.UTF_8);
      MessageDigest md = MessageDigest.getInstance("MD5");
      byte[] thedigest = md.digest(bytesOfMessage);
      StringBuilder sb = new StringBuilder();
      for (byte b : thedigest) {
        sb.append(String.format("%02x", new Object[] { Byte.valueOf(b) }));
      }
      //this.logger.info("COMPARE:" + sb.toString());
      return sb.toString().equals(hasheduser);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }
  
  @RequestMapping(value={"cities"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody public List<City> sendCities()
  {
    return this.destinazioni;
  }
  
  @RequestMapping(value="updatecity", headers="Accept=application/json", method=RequestMethod.POST)
  public @ResponseBody boolean updateCity(@RequestBody City city)
  {
    ObjectMapper mapper = new ObjectMapper();
    try
    {
      for (City d : this.destinazioni) {
        if (d.getName().equals(city.getName()))
        {
          d.setPretty_name(city.getPretty_name());
          d.setImgFile(city.getImgFile());
          d.setLonLatBBox(city.getLonLatBBox());
          mapper.writeValue(new File(this.properties.getProperty("cities")), this.destinazioni);
          tracelog.info("City " + city.getName() + " has been updated");
          return true;
        }
      }
      this.destinazioni.add(city);
      mapper.writeValue(new File(this.properties.getProperty("cities")), this.destinazioni);
      tracelog.info("New city " + city.getName() + " has been added");
      boolean success = new File(this.properties.getProperty("citiesPath") + city.getName() + "/pois").mkdirs();
      if (!success) {
        return false;
      }
      success = new File(this.properties.getProperty("citiesPath") + city.getName() + "/itineraries").mkdirs();
      if (!success) {
        return false;
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      tracelog.info(city.toJSONString());
      return false;
    }
    return true;
  }
  
  @RequestMapping(value={"removecity"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public boolean removeCity(@RequestBody String cityName)
  {
    ObjectMapper mapper = new ObjectMapper();
    try
    {
      for (City d : this.destinazioni) {
        if (d.getName().equals(cityName))
        {
          this.destinazioni.remove(d);
          mapper.writeValue(new File(this.properties.getProperty("cities")), this.destinazioni);
          tracelog.info("City " + cityName + " has been removed");
          return true;
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }
    return false;
  }
  
  @RequestMapping(value={"uploadpic"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public boolean uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("name") String fileName, @RequestParam("type") String type)
  {
    tracelog.info("type:" + type);
    try
    {
      if (type.equals("city"))
      {
        FileUtils.writeByteArrayToFile(new File(this.properties.getProperty("cityImagePath") + fileName), file.getBytes());
        tracelog.info("Picture " + fileName + " has been uploaded: " + this.properties.getProperty("cityImagePath") + fileName);
      }
      else if (type.equals("poi"))
      {
        FileUtils.writeByteArrayToFile(new File(this.properties.getProperty("poiImagePath") + fileName), file.getBytes());
        tracelog.info("Picture " + fileName + " has been uploaded: " + this.properties.getProperty("poiImagePath") + fileName);
      }
      else
      {
        FileUtils.writeByteArrayToFile(new File(this.properties.getProperty("itinerariesImagePath") + fileName), file.getBytes());
        tracelog.info("Picture " + fileName + " has been uploaded: " + this.properties.getProperty("itinerariesImagePath") + fileName);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  @RequestMapping(value={"activities"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public List<POI> sendActivities(@RequestParam(value="city", defaultValue="unknown") String city)
  {
    logger.info("Requested activities for city "+city);
    return this.dao.retrieveActivities(city);
  }
  
  @RequestMapping(value={"nominatim"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  public boolean downloadNominatim(@RequestParam("city") String city)
  {
    double[] coords = null;
    for (City d : this.destinazioni) {
      if (d.getName().equals(city)) {
        coords = d.getLonLatBBox();
      }
    }
    if (coords != null)
    {
      NominatimPOIsDownload.download(city, coords, this.properties.getProperty("citiesPath") + city + "/pois/nominatim.json");
      SavePOIs2DB.run(this.logger, city, this.dao, this.properties.getProperty("citiesPath") + city + "/pois/");
    }
    else
    {
      return false;
    }
    return true;
  }
  
  @RequestMapping(value={"addPoi"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public boolean insertPoi(@RequestBody String poicity)
  {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    try
    {
      tracelog.info("poicity:"+poicity);
      JSONObject j_poi = new JSONObject(poicity).getJSONObject("poi");
      String city = new JSONObject(poicity).getString("city");
      POI poi = mapper.readValue(j_poi.toString(), POI.class);
      List<POI> poi_list = new ArrayList<>();
      poi_list.add(poi);
      tracelog.info("name:"+j_poi.get("display_name"));
      JSONArray array = new JSONArray();
      array.put(j_poi);
      tracelog.info("Write POI(obj):"+j_poi);
      tracelog.info("Write POI(string):"+j_poi.toString());
      tracelog.info("Write POI(array-string):"+array.toString());
      tracelog.info("Write POI(array):"+array);
      mapper.writeValue(new File(this.properties.getProperty("citiesPath") + city + "/pois/" + j_poi.getString("place_id") + ".json"), poi_list);
      SavePOIs2DB.run(this.logger, city, this.dao, this.properties.getProperty("citiesPath") + city + "/pois/");
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @RequestMapping(value={"removeactivity"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public boolean removeActivity(@RequestParam String activityName, @RequestParam String cityName)
  {
    return this.dao.deleteActivity(cityName, activityName);
  }


  @RequestMapping(value={"itineraries"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public List<Itinerary> sendItineraries(@RequestParam(value="city", defaultValue="unknown") String city)
  {
    return this.dao.retrieveItineraries(city);
  }
  
  @RequestMapping(value={"removeitinerary"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public boolean removeItinerary(@RequestParam String itineraryName, @RequestParam String cityName)
  {
    return this.dao.deleteItinerary(cityName, itineraryName);
  }
  
  @RequestMapping(value={"additinerary"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.POST})
  @ResponseBody
  public boolean addItinerary(@RequestParam Itinerary itinerary, @RequestParam String cityName)
  {
    this.dao.insertItinerary(cityName, itinerary);
    return true;
  }
  
  @RequestMapping(value={"createitineraries"}, headers={"Accept=application/json"}, method={org.springframework.web.bind.annotation.RequestMethod.GET})
  @ResponseBody
  public boolean createItineraries(@RequestParam(value="city", defaultValue="unknown") String city)
  {
    List<Itinerary> itinerari = new ArrayList();
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    double[] coords = null;
    ItineraryGenerator iti_gen = null;
    City d;
    try
    {
      itinerari.addAll((List)mapper.readValue(new File(this.properties.getProperty("citiesPath") + city + "/itineraries/itineraries.json"), new TypeReference() {}));
      for (Iterator localIterator = this.destinazioni.iterator(); localIterator.hasNext();)
      {
        d = (City)localIterator.next();
        if (d.getName().equals(city)) {
          coords = d.getLonLatBBox();
        }
      }

      iti_gen = new ItineraryGenerator(this.dao, city, "default", coords);
      
      List<Itinerary> itis = iti_gen.generate();
      for (Itinerary i : itis)
      {
        itinerari.add(i);
        this.dao.insertItinerary(city, i);
      }
      mapper.writeValue(new File(this.properties.getProperty("citiesPath") + city + "/itineraries/itineraries.json"), itinerari);
      
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }
}
