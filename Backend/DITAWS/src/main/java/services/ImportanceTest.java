package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.Mongo;
import io.RESTController;
import model.POI;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;

public class ImportanceTest {



	public static void main(String[] args) throws Exception  {
		ImportanceTest g = new ImportanceTest();
		String city = "Ravenna";
		Mongo dao = new Mongo();
		g.run(city, dao, "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois\\nominatim.json");
		g.run(city, dao, "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois\\ibc.json");
		g.run(city, dao, "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\"+city+"\\pois\\mibact.json");
	}


	private Logger logger = Logger.getLogger(RESTController.class);



	public void run(String city, Mongo dao, String file) {

		System.out.println(new File(file).getName()+" ***************");

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try {

			DescriptiveStatistics ds = new DescriptiveStatistics();
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			JSONArray parsed = new JSONArray(br.readLine());
			for (int j = 0; j < parsed.length(); j++) {
				JSONObject currentJPOI = (JSONObject) parsed.get(j);
				POI currentPOI = null;
				try {
					currentPOI = mapper.readValue(currentJPOI.toString(), POI.class);
					ds.addValue(currentPOI.getImportance());
					//if(currentPOI.getImportance() > 0.3) System.out.println(currentPOI.getDisplay_name());
				} catch(Exception pe) {
					logger.warn("Error parsing " + currentJPOI.toString());
					pe.printStackTrace();
				}
			}
			br.close();

			for(int i=10;i<=100;i=i+10)
				System.out.println(i+"% = "+ds.getPercentile(i));



		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}