package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class City
{
  private String name;
  private String pretty_name;
  private String imgFile;
  private double[] lonLatBBox;
  
  public City() {}
  
  public City(String name, String pretty_name, String imgFile, double[] lonLatBBox)
  {
    this.name = name;
    this.pretty_name = pretty_name;
    this.imgFile = imgFile;
    this.lonLatBBox = lonLatBBox;
  }
  
  public boolean contains(double lat, double lon)
  {
    return (this.lonLatBBox[0] < lon) && (lon < this.lonLatBBox[2]) && (this.lonLatBBox[1] < lat) && (lat < this.lonLatBBox[3]);
  }
  
  public static List<City> getInstance()
  {
    List<City> cities = new ArrayList();
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try
    {
      cities.addAll((List<City>)mapper.readValue(new File("/"), new TypeReference<List<City>>() {}));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return cities;
  }
  
  public static List<City> getInstance(String absPath)
  {
    List<City> cities = new ArrayList();
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try
    {
      cities.addAll((List<City>)mapper.readValue(new File(absPath), new TypeReference<List<City>>() {}));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return cities;
  }
  
  public static City getInstance(String path, String name)
  {
    for (City city : getInstance(path)) {
      if (city.getName().equals(name)) {
        return city;
      }
    }
    return null;
  }
  
  public String toString()
  {
    return this.name;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
  
  public String getPretty_name()
  {
    return this.pretty_name;
  }
  
  public void setPretty_name(String pretty_name)
  {
    this.pretty_name = pretty_name;
  }
  
  public String getImgFile()
  {
    return this.imgFile;
  }
  
  public void setImgFile(String imgFile)
  {
    this.imgFile = imgFile;
  }
  
  public double[] getLonLatBBox()
  {
    return this.lonLatBBox;
  }
  
  public void setLonLatBBox(double[] lonLatBBox)
  {
    this.lonLatBBox = lonLatBBox;
  }
  
  public String toJSONString()
  {
    ObjectMapper mapper = new ObjectMapper();
    try
    {
      return mapper.writeValueAsString(this);
    }
    catch (JsonProcessingException e)
    {
      e.printStackTrace();
    }
    return "";
  }
  
  public boolean equals(Object o)
  {
    if ((o instanceof City)) {
      return this.name.equals(((City)o).name);
    }
    return false;
  }
  
  public static void main(String[] args)
  {
    List<City> cities = getInstance();
    System.out.println(cities.size());
  }
}
