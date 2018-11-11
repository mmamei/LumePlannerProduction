package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.LngLatAlt;
import org.geojson.Point;

public class POI
  implements Comparable<POI>
{
  private String place_id;
  private Point geometry;
  private String display_name;
  private String category;
  private String type;
  private float importance;
  private String icon;
  private double visiting_time;
  private String opening_hours;
  private String opening_days;
  private int rating;
  private String photo_url;
  private String description;
  private String www;
  
  public POI(String place_id, double lat, double lon, String display_name, String category, String type, float importance, String icon, double visiting_time, String opening_hours, String opening_days, int rating, String photo, String description, String url)
  {
    this.place_id = place_id;
    this.geometry = new Point(lon, lat);
    this.display_name = display_name;
    this.category = category;
    this.type = type;
    this.importance = importance;
    this.icon = icon;
    this.visiting_time = visiting_time;
    this.opening_hours = opening_hours;
    this.opening_days = opening_days;
    this.rating = rating;
    this.photo_url = photo;
    this.description = description;
    this.www = url;
  }
  
  public POI(String place_id, Point geometry, String display_name, String category, String type, float importance, String icon, double visiting_time, String opening_hours, String opening_days, int rating, String photo, String description, String url)
  {
    this.place_id = place_id;
    this.geometry = geometry;
    this.display_name = display_name;
    this.category = category;
    this.type = type;
    this.importance = importance;
    this.icon = icon;
    this.visiting_time = visiting_time;
    this.opening_hours = opening_hours;
    this.opening_days = opening_days;
    this.rating = rating;
    this.photo_url = photo;
    this.description = description;
    this.www = url;
  }
  
  public POI()
  {
    this.geometry = new Point(0.0D, 0.0D);
  }
  
  public POI(String place_id, double lat, double lng, String display_name)
  {
    this.place_id = place_id;
    this.geometry = new Point(lng, lat);
    this.display_name = display_name;
  }
  
  public String getPlace_id()
  {
    return this.place_id;
  }
  
  public void setPlace_id(String place_id)
  {
    this.place_id = place_id;
  }
  
  public void setLonLat(double lon, double lat)
  {
    this.geometry = new Point(lon, lat);
  }
  
  public void setLat(double lat)
  {
    this.geometry.getCoordinates().setLatitude(lat);
  }
  
  public void setLon(double lon)
  {
    this.geometry.getCoordinates().setLongitude(lon);
  }
  
  public Point getGeometry()
  {
    return this.geometry;
  }
  
  public void setGeometry(Point geometry)
  {
    this.geometry = geometry;
  }
  
  public String getDisplay_name()
  {
    return this.display_name;
  }
  
  public void setDisplay_name(String display_name)
  {
    this.display_name = display_name;
  }
  
  public String getCategory()
  {
    return this.category;
  }
  
  public void setCategory(String category)
  {
    this.category = category;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public void setType(String type)
  {
    this.type = type;
  }
  
  public float getImportance()
  {
    return this.importance;
  }
  
  public void setImportance(float importance)
  {
    this.importance = importance;
  }
  
  public String getIcon()
  {
    return this.icon;
  }
  
  public void setIcon(String icon)
  {
    this.icon = icon;
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
  
  public String getPhoto_url()
  {
    return this.photo_url;
  }
  
  public void setPhoto_url(String photo_url)
  {
    this.photo_url = photo_url;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public void setDescription(String description)
  {
    this.description = description;
  }
  
  public String getWww()
  {
    return this.www;
  }
  
  public void setWww(String www)
  {
    this.www = www;
  }
  
  public int compareTo(POI arg0)
  {
    return this.place_id.compareTo(arg0.place_id);
  }
  
  public String getOpening_hours()
  {
    return this.opening_hours;
  }
  
  public void setOpening_hours(String opening_hours)
  {
    this.opening_hours = opening_hours;
  }
  
  public String getOpening_days()
  {
    return this.opening_days;
  }
  
  public void setOpening_days(String opening_days)
  {
    this.opening_days = opening_days;
  }
  
  public double getVisiting_time()
  {
    return this.visiting_time;
  }
  
  public void setVisiting_time(double visiting_time)
  {
    this.visiting_time = visiting_time;
  }
  
  public int getRating()
  {
    return this.rating;
  }
  
  public void setRating(int rating)
  {
    this.rating = rating;
  }
  
  public int hashCode()
  {
    int prime = 31;
    int result = 1;
    
    result = 31 * result + (this.geometry == null ? 0 : this.geometry.hashCode());
    
    result = 31 * result + (this.place_id == null ? 0 : this.place_id.hashCode());
    return result;
  }
  
  public boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    POI other = (POI)obj;
    if (this.geometry == null)
    {
      if (other.geometry != null) {
        return false;
      }
    }
    else if (!this.geometry.equals(other.geometry)) {
      return false;
    }
    if (this.place_id == null)
    {
      if (other.place_id != null) {
        return false;
      }
    }
    else if (!this.place_id.equals(other.place_id)) {
      return false;
    }
    return true;
  }
  
  public String toString()
  {
    return this.place_id;
  }
}
