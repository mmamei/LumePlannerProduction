package model;

import java.io.Serializable;

/**
 * Created by andrea on 19/11/18.
 */
public class POICity {

    POI poi;
    String city;

    public POICity(POI poi, String city) {
        this.poi = poi;
        this.city = city;
    }

    public POICity() {}

    public POI getPoi() {
        return poi;
    }

    public void setPoi(POI poi) {
        this.poi = poi;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "POICity{" +
                "poi=" + poi +
                ", city='" + city + '\'' +
                '}';
    }
}
