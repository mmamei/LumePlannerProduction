package services;

import io.Mongo;
import model.Itinerary;
import model.POI;
import model.UserLog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marco on 13/09/2017.
 */
public class CheckUser {
    public static UserLog checkUser(Mongo dao, String userId) {
        UserLog ul = new UserLog();
        ul.setUserid(userId);
        try {

            BufferedReader br = new BufferedReader(new FileReader("/var/log/tomcat8/USERS.log"));
            String line;
            while((line=br.readLine())!=null) {
                if(line.contains(userId)) {
                    if(line.contains("localized at")) {
                        String[] latLon = line.substring(line.lastIndexOf(" ")+1).split(",");
                        ul.getLatLon().add(new double[]{Double.parseDouble(latLon[0]),Double.parseDouble(latLon[1])});
                    }
                    if(line.contains("request newplan plan")) {
                        //System.out.println(line);
                        String[] e = line.split(" ");
                        String city = e[12];
                        String places = line.substring(line.lastIndexOf("to visit:")+"to visit:".length()).trim();
                        String name = getItineraryName(dao,city,places);

                        if(!ul.getItineraries().containsKey(name)) {

                            List<POI> list = new ArrayList<>();
                            String[] pois = places.replaceAll("\\[|\\]","").split(",");
                            for(String poi: pois) {
                               list.add(dao.retrieveActivity(city,poi.trim()));
                            }
                            ul.getItineraries().put(name,list);
                        }
                    }
                    if(line.contains("prize!"))
                        ul.setGotPrize(true);

                }
            }
            br.close();
        }catch(Exception e) {
            e.printStackTrace();
        }

        return ul;
    }

    public static String getItineraryName(Mongo dao, String city, String places) {
        List<Itinerary> iti = dao.retrieveItineraries(city);
        for(Itinerary i: iti)
            if(i.getVisits().toString().equals(places))
                return i.getDisplay_name();
        return "Custom Itinerary";
    }

    public static void main(String[] args) {
        Mongo dao = new Mongo();
        UserLog ul = checkUser(dao, "3662677592101309");
        ul.print();
    }
}
