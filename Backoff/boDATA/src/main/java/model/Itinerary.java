package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;

public class Itinerary
{
  private String itinerary_id;
  private String display_name;
  private List<String> visits;
  private String approx_time;
  private String img;
  private String description;
  
  public Itinerary() {}
  
  public Itinerary(String itinerary_id, String display_name, List<String> visits, String approx_time, String img, String description)
  {
    this.itinerary_id = itinerary_id;
    this.display_name = display_name;
    this.visits = visits;
    this.approx_time = approx_time;
    this.img = img;
    this.description = description;
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
  
  public String getItinerary_id()
  {
    return this.itinerary_id;
  }
  
  public void setItinerary_id(String itinerary_id)
  {
    this.itinerary_id = itinerary_id;
  }
  
  public String getDisplay_name()
  {
    return this.display_name;
  }
  
  public void setDisplay_name(String display_name)
  {
    this.display_name = display_name;
  }
  
  public List<String> getVisits()
  {
    return this.visits;
  }
  
  public void setVisits(List<String> visits)
  {
    this.visits = visits;
  }
  
  public String getApprox_time()
  {
    return this.approx_time;
  }
  
  public void setApprox_time(String approx_time)
  {
    this.approx_time = approx_time;
  }
  
  public String getImg()
  {
    return this.img;
  }
  
  public void setImg(String img)
  {
    this.img = img;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public void setDescrption(String description)
  {
    this.description = description;
  }
}
