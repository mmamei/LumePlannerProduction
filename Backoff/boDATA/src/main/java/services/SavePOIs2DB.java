package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.Mongo;
import io.RESTController;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;
import model.City;
import model.POI;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class SavePOIs2DB
{
	public static void main(String[] args)
			throws Exception
	{
		SavePOIs2DB g = new SavePOIs2DB();
		List<City> cities = City.getInstance();
		Mongo dao = new Mongo();

		City c = City.getInstance("/", "Regali_A_Palazzo");
		String dir = "G:\\CODE\\IJ-IDEA\\LumePlanner\\Backend\\DITAWS\\src\\main\\webapp\\WEB-INF\\data\\cities\\" + c.getName() + "\\pois";
		run(null, c.getName(), dao, dir);
	}

	private Logger logger = Logger.getLogger(RESTController.class);

	public static void run(Logger logger, String city, Mongo dao, String pois_dir)
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		try
		{
			File dir = new File(pois_dir);
			for (File f : dir.listFiles())
			{
				if (city.equals("Modena")) {
					System.out.println(f);
				}
				String file = f.getAbsolutePath();
				if (file.endsWith("json"))
				{
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
					StringBuffer sb = new StringBuffer();
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line);
					}
					br.close();
					JSONArray parsed = new JSONArray(sb.toString());
					for (int j = 0; j < parsed.length(); j++)
					{
						JSONObject currentJPOI = (JSONObject)parsed.get(j);

						POI currentPOI = null;
						try
						{
							currentPOI = (POI)mapper.readValue(currentJPOI.toString(), POI.class);
						}
						catch (Exception pe)
						{
							logger.warn("Error parsing " + currentJPOI.toString());
							pe.printStackTrace();
						}
						if (currentPOI != null) {
							dao.insertActivity(city, currentPOI);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
