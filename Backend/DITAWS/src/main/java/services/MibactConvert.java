package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.City;
import model.POI;
import util.StringUtils;

import java.io.*;
import java.util.*;

import static services.CategoriesDictionary.DEFAULT_CAT;
import static services.CategoriesDictionary.MIBACT_TO_CAT;

/**
 * Created by marco on 21/04/2017.
 */

/*
This program takes as input a csv file from:
http://www.patrimonioculturale-er.it/webgis/
And split it according to the cities we have

0 WKT
1 gid
2 codice
3 nome
4 denominazioni
5 proprietario_pubblico
6 provincia
7 comune
8 diocesi
9 frazione
10 indirizzo
11 tipo_tutela
12 tipo_proprieta
13 tipologie_cronologie
14 categoria
15 eta
16 eta_attestazione
17 provvedimenti_date
18 stato+
19 data_cambio_stato
20 link_vir
21 data_upd
22 lon
23 lat
24 x
25 y
26 guidarossa
27 link_fai
*/

public class MibactConvert {

    public static  boolean GUIDA_ROSSA_ONLY = true;

    public static void main(String[] args) throws Exception {




        // POI Categories
        // attractions
        // monuments
        // museums
        // parks
        // historical
        // religious




        List<City> cities = City.getInstance();

        Map<String,List<POI>> hm = new HashMap<>();
        for(City city : cities)
            hm.put(city.getName(),new ArrayList<POI>());

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\patrimonioculturale-er.csv"), "UTF8"));
        String line;

        String[] header = br.readLine().split("\t"); // skip header
        //for(int i=0; i<header.length;i++)
        //System.out.println(i+" "+header[i]);


        // POI Constructor:
        // String place_id, double lat, double lon, String display_name, String category, String type,
        // float importance, String icon, double visiting_time, String opening_hours, String opening_days, int rating, String photo_url, String description, String www



        Map<String,Integer> allCategories = new HashMap<>();

        int tot = 0;
        while((line = br.readLine())!=null) {
            String[] e = line.split("\t");

            String cat = e[13].split(" |/")[0];

            Integer c = allCategories.get(cat);
            if(c == null) allCategories.put(cat,1);
            else allCategories.put(cat,c+1);


            double[] lonlat = convert(e[0]);
            for(City cp: cities) {
                if(cp.contains(lonlat[1],lonlat[0])) {

                    if(!GUIDA_ROSSA_ONLY || (GUIDA_ROSSA_ONLY && e.length > 26 && !e[26].isEmpty())) {

                        String guida_rossa_cat = e[26];
                        float importance = 0;
                        // Corsivo, Presente, Fuori guida, Grassetto+asterisco, Corsivo+asterisco, Assente, Grassetto
                        if(guida_rossa_cat.equals("Grassetto+asterisco"))  importance = 1;
                        if(guida_rossa_cat.equals("Grassetto"))  importance = 0.9f;
                        if(guida_rossa_cat.equals("Corsivo+asterisco"))  importance = 0.8f;
                        if(guida_rossa_cat.equals("Corsivo"))  importance = 0.7f;
                        if(guida_rossa_cat.equals("Presente"))  importance = 0.4f;
                        if(guida_rossa_cat.equals("Fuori guida"))  importance = 0.3f;

                        String www = e.length > 27 && !e[27].isEmpty() ? e[27] : e.length > 20 && !e[20].isEmpty() ? e[20] : null;

                        String category = MIBACT_TO_CAT.get(cat);
                        if(category == null) category = DEFAULT_CAT;

                        hm.get(cp.getName()).add(new POI(e[1], lonlat[1], lonlat[0], e[3]+",from:MIBACT", category, cat, importance, "", 0, "ok", "ok", 0, null,null,www));
                        tot ++;
                    }
                }
            }
        }


        //System.exit(0);

        //for(String k: allCategories.keySet())
        //    System.out.println(k+" ==> "+allCategories.get(k));

        br.close();
        System.out.println("tot = "+tot);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        for(String city: hm.keySet()) {
            System.out.println(city+" ==> "+hm.get(city).size());

            File dir = new File("G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\"+ StringUtils.removeAccent(city)+"\\pois");
            dir.mkdirs();
            mapper.writeValue(new File(dir+"/mibact.json"), hm.get(city));
        }
    }
    //"POINT (11.586493673133132 44.806869144609884)"
    private static double[] convert(String wtk) {
        String[] e = wtk.substring("\"POINT (".length(),wtk.length()-2).split(" ");
        return new double[]{Double.parseDouble(e[0]),Double.parseDouble(e[1])};
    }
}
