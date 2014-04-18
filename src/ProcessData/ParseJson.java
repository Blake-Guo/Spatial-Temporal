package ProcessData;

import java.text.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class ParseJson {

	/**
	 * Get the connection the database.
	 * 
	 * @return the Connection to the database
	 */
	public static Connection ConnectDatabase() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("PostgreSQL JDBC Driver Registered!");

		Connection connection = null;

		try {
			connection = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/twitterdb", "postgres",
					"postgres");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("Successfully connected to the data base");
		return connection;
	}

	/**
	 * Given a point, this function return the city(actually the table name of
	 * the city) that contains the point. If there is no such city, return null.
	 * 
	 * @param mapCityMBR
	 *            : a map, the key is the city's table name in the database, the
	 *            value is the city's minimal bounding rectangle.
	 * @param point
	 * @return
	 */
	public static String CityPointBelongTo(
			Map<String, GeoRectangle> mapCityMBR, GeoPoint point) {
		for (Map.Entry<String, GeoRectangle> entry : mapCityMBR.entrySet()) {

			GeoRectangle cityMBR = entry.getValue();
			if (cityMBR.contain(point))
				return entry.getKey();
		}

		return null;
	}

	/**
	 * Get the connection the database.
	 * 
	 * @param host
	 *            , the IP address of the server.
	 * @param port
	 *            ,the port of the database service hosted by the server.
	 * @param database
	 *            , the name of the database.
	 * @param user
	 *            , the user name of the database.
	 * @param passwd
	 *            , the password of the user.
	 * @return
	 */
	public static Connection ConnectDatabase(String host, String port,
			String database, String user, String passwd) {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("PostgreSQL JDBC Driver Registered!");

		Connection connection = null;

		try {
			connection = DriverManager.getConnection("jdbc:postgresql://"
					+ host + ":" + port + "/" + database, user, passwd);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("Successfully connected to the data base");
		return connection;
	}

	/**
	 * Get all files contained in the given folder.
	 * 
	 * @param directory
	 *            , the given folder.
	 * @return
	 */
	public static ArrayList<File> ScanFiles(String directory) {
		ArrayList<File> files = new ArrayList<File>();

		File rootFile = new File(directory);

		File[] fileinfos = rootFile.listFiles();

		for (File file : fileinfos) {
			if (file.isHidden() || file.isDirectory()) {
				continue;
			}
			files.add(file);
		}

		return files;
	}

	public static void AddOneColumn_URL(String filename, Connection connection)
			throws Exception {
		Statement statement = connection.createStatement();

		JsonFactory f = new MappingJsonFactory();

		JsonParser jp = f.createParser(new File(filename));

		while (jp.nextToken() == JsonToken.START_OBJECT) {

			jp.nextToken();

			JsonNode jNode = null;

			try {

				jNode = jp.readValueAsTree();

			} catch (JsonProcessingException e) {

				// TODO Auto-generated catch block

				e.printStackTrace();

				break;

			} catch (IOException e) {

				// TODO Auto-generated catch block

				e.printStackTrace();

				break;
			}

			Iterator<JsonNode> iter = jNode.path("entities").path("urls")
					.iterator();
			String urlStr = "";
			if (iter.hasNext()) {
				urlStr = iter.next().path("expanded_url").asText();
			}

			String idStr = jNode.path("id").asText();

			GeoPoint geoPoint = new GeoPoint(0, 0);

			if (jNode.path("coordinates").path("type").asText().equals("Point")) {

				ArrayList<Double> lonlat = new ArrayList<Double>();

				for (JsonNode tmpNode : jNode.path("coordinates").path(
						"coordinates")) {

					lonlat.add(Double.parseDouble(tmpNode.asText()));
				}

				geoPoint.longitude = lonlat.get(0);
				geoPoint.latitude = lonlat.get(1);

			}

			if (!(geoPoint.longitude == 0 && geoPoint.latitude == 0)) {

				String sql = "update sfgeotwitters set url='" + urlStr
						+ "' where twitterid='" + idStr + "'";

				try {
					statement.executeUpdate(sql);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
	}

	public static void AddColumns_TimeAndGeoAndGeom(String tableName,
			String filePath, Connection connection) throws Exception {
		Statement statement = connection.createStatement();

		JsonFactory f = new MappingJsonFactory();

		JsonParser jp = f.createParser(new File(filePath));

		while (jp.nextToken() == JsonToken.START_OBJECT) {
			jp.nextToken();

			JsonNode jNode = null;

			try {
				jNode = jp.readValueAsTree();
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}

			// Get twitter id;
			String idStr = jNode.path("id").asText();

			// Get the timestamp
			String timeStr = jNode.path("created_at").asText();
			DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");
			java.util.Date date = df.parse(timeStr);

			DateFormat dfout = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
			dfout.setTimeZone(TimeZone.getTimeZone("UTC"));
			String timeformat = dfout.format(date);
			// get the location;
			GeoPoint gPoint = null;
			if (jNode.path("coordinates").path("type").asText().equals("Point")) {

				ArrayList<Double> lonlat = new ArrayList<Double>();
				gPoint = new GeoPoint(0, 0);

				for (JsonNode tmpNode : jNode.path("coordinates").path(
						"coordinates")) {
					lonlat.add(Double.parseDouble(tmpNode.asText()));
				}
				gPoint.longitude = lonlat.get(0);
				gPoint.latitude = lonlat.get(1);
			}

			if (gPoint != null) {
				String glonlatVal = "ST_GeometryFromText('POINT("
						+ gPoint.longitude + " " + gPoint.latitude
						+ ")' ,4326)";
				String geompointVal = "ST_Transform( ST_GeometryFromText('POINT("
						+ gPoint.longitude
						+ " "
						+ gPoint.latitude
						+ ")' ,4326), 916)";

				String sql = "update " + tableName + " set geolonlat="
						+ glonlatVal + ", geompoint=" + geompointVal
						+ ", time='" + timeformat + "'" + " where twitterid='"
						+ idStr + "'";

				try {
					statement.executeUpdate(sql);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Parse the twitter check-in json record and insert them into the
	 * corresponding database table.
	 * 
	 * @param tableName
	 *            : the name of table that we want to insert the record into.
	 * @param jsonDirectory
	 *            : the path of the json file folder.
	 * @param connection
	 *            : the connection to the database
	 * @throws Exception
	 */
	public static void ParseTwitRecorders(String tableName,

	String jsonDirectory, Connection connection) throws Exception {

		Statement statement = connection.createStatement();
		// connection.setAutoCommit(false);
		JsonFactory f = new MappingJsonFactory();

		JsonParser jp = f.createParser(new File(jsonDirectory));

		while (jp.nextToken() == JsonToken.START_OBJECT) {

			TwitterRecorder twrec = new TwitterRecorder();

			jp.nextToken();

			JsonNode jNode = null;

			try {

				jNode = jp.readValueAsTree();

			} catch (JsonProcessingException e) {

				// TODO Auto-generated catch block

				e.printStackTrace();

				break;

			} catch (IOException e) {

				// TODO Auto-generated catch block

				e.printStackTrace();

				break;

			}

			// for time
			String timeStr = jNode.path("created_at").asText();

			DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");

			java.util.Date date = df.parse(timeStr);

			twrec.SetTime(date);

			// for twitter id
			String idStr = jNode.path("id").asText();

			twrec.SetTwitterId(idStr);

			// for text
			String textStr = jNode.path("text").asText();

			textStr = textStr.replace('\'', ' ');

			twrec.SetText(textStr);

			// for userid
			twrec.SetUserId(jNode.path("user").path("id").asText());

			// for url
			Iterator<JsonNode> iter = jNode.path("entities").path("urls")
					.iterator();
			String urlStr = "";
			if (iter.hasNext()) {
				urlStr = iter.next().path("expanded_url").asText();
			}
			twrec.SetURL(urlStr);

			// for location
			if (jNode.path("coordinates").path("type").asText().equals("Point")) {

				ArrayList<Double> lonlat = new ArrayList<Double>();

				for (JsonNode tmpNode : jNode.path("coordinates").path(
						"coordinates")) {

					lonlat.add(Double.parseDouble(tmpNode.asText()));
				}
				twrec.SetGeoPoint(lonlat.get(0), lonlat.get(1));
			}

			// Write the twitter record into database.
			if (!(twrec.GetGeoPoint().latitude == 0 && twrec.GetGeoPoint().longitude == 0)) {
				DateFormat dfout = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
				dfout.setTimeZone(TimeZone.getTimeZone("UTC"));
				String timeformat = dfout.format(twrec.GetTime());

				String glonlatVal = "ST_GeometryFromText('POINT("
						+ twrec.GetGeoPoint().longitude + " "
						+ twrec.GetGeoPoint().latitude + ")' ,4326)";
				String geompointVal = "ST_Transform( ST_GeometryFromText('POINT("
						+ twrec.GetGeoPoint().longitude
						+ " "
						+ twrec.GetGeoPoint().latitude + ")' ,4326), 916)";

				String val = "'" + twrec.GetTwitterId() + "','"
						+ twrec.GetUserId() + "','" + timeformat + "','"
						+ twrec.GetText() + "','" + twrec.getURL() + "',"
						+ String.valueOf(twrec.GetGeoPoint().latitude) + ","
						+ String.valueOf(twrec.GetGeoPoint().longitude) + ","
						+ glonlatVal + "," + geompointVal;
				String sql = "insert into "
						+ tableName
						+ "(twitterId, userid, time, text, url, latitude, longitude, geolonlat, geompoint)"
						+ " values(" + val + ")";

				try {
					statement.executeUpdate(sql);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}// if !=0

		}// while

		System.out.println(jsonDirectory + "Parse Successfully");
	}

	/**
	 * Parse the twitter check-in json record and insert them into the
	 * corresponding database table.
	 * 
	 * @param tableName
	 *            : the name of table that we want to insert the record into.
	 * @param jsonDirectory
	 *            : the path of the json file folder.
	 * @param connection
	 *            : the connection to the database.
	 * @param boundary
	 *            : the boundary that we are interested in.
	 * @throws Exception
	 */
	public static void ParseTwitRecorders(Map<String, GeoRectangle> mapCityMBR,
			String jsonDirectory, Connection connection) throws Exception {

		// the statement for database
		Statement statement = connection.createStatement();

		// start to parse the json
		JsonFactory f = new MappingJsonFactory();

		JsonParser jp = f.createParser(new File(jsonDirectory));

		while (jp.nextToken() == JsonToken.START_OBJECT) {

			TwitterRecorder twrec = new TwitterRecorder();

			jp.nextToken();

			JsonNode jNode = null;

			try {
				jNode = jp.readValueAsTree();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				break;

			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			// 1 for time
			String timeStr = jNode.path("created_at").asText();

			DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss Z yyyy");

			java.util.Date date = df.parse(timeStr);

			twrec.SetTime(date);

			// 2 for twitter id
			String idStr = jNode.path("id").asText();

			twrec.SetTwitterId(idStr);

			// 3 for text
			String textStr = jNode.path("text").asText();

			textStr = textStr.replace('\'', ' ');

			twrec.SetText(textStr);

			// 4 for userid
			twrec.SetUserId(jNode.path("user").path("id").asText());

			// 5 for url
			Iterator<JsonNode> iter = jNode.path("entities").path("urls")
					.iterator();
			String urlStr = "";
			if (iter.hasNext()) {
				urlStr = iter.next().path("expanded_url").asText();
			}
			twrec.SetURL(urlStr);

			// 6 for location
			if (jNode.path("coordinates").path("type").asText().equals("Point")) {

				ArrayList<Double> lonlat = new ArrayList<Double>();

				for (JsonNode tmpNode : jNode.path("coordinates").path(
						"coordinates")) {

					lonlat.add(Double.parseDouble(tmpNode.asText()));
				}
				twrec.SetGeoPoint(lonlat.get(0), lonlat.get(1));
			}

			// Write the twitter record into database.
			if (!(twrec.GetGeoPoint().latitude == 0 && twrec.GetGeoPoint().longitude == 0)) {

				// 1: See if the record is within the given cities' MBR.
				String tableName = CityPointBelongTo(mapCityMBR,
						twrec.GetGeoPoint());
				if (tableName == null) {
					// the check-in point is not within any given city's MBR
					// that we are interested.
					continue;
				}

				// 2: Insert the record into the database.
				DateFormat dfout = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
				dfout.setTimeZone(TimeZone.getTimeZone("UTC"));
				String timeformat = dfout.format(twrec.GetTime());

				String glonlatVal = "ST_GeometryFromText('POINT("
						+ twrec.GetGeoPoint().longitude + " "
						+ twrec.GetGeoPoint().latitude + ")' ,4326)";
				String geompointVal = "ST_Transform( ST_GeometryFromText('POINT("
						+ twrec.GetGeoPoint().longitude
						+ " "
						+ twrec.GetGeoPoint().latitude + ")' ,4326), 916)";

				String val = "'" + twrec.GetTwitterId() + "','"
						+ twrec.GetUserId() + "','" + timeformat + "','"
						+ twrec.GetText() + "','" + twrec.getURL() + "',"
						+ String.valueOf(twrec.GetGeoPoint().latitude) + ","
						+ String.valueOf(twrec.GetGeoPoint().longitude) + ","
						+ glonlatVal + "," + geompointVal;
				String sql = "insert into "
						+ tableName
						+ "(twitterId, userid, time, text, url, latitude, longitude, geolonlat, geompoint)"
						+ " values(" + val + ")";
				
				//System.out.println(sql);

				try {
					statement.executeUpdate(sql);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}// if !=0

		}// while

		System.out.println(jsonDirectory + "Parse Successfully");
	}

	public static Map<String, GeoRectangle> InitializeMapOfCityMBR() {
		Map<String, GeoRectangle> mapCityMBR = new HashMap<String, GeoRectangle>();

		String sftableName = "sfgeotwitters";
		GeoRectangle sfRectangle = new GeoRectangle(new GeoPoint(37.609,
				-122.544), new GeoPoint(37.875, -122.239));
		mapCityMBR.put(sftableName, sfRectangle);

		String pitttableName = "pittgeotwitters";
		GeoRectangle pittRectangle = new GeoRectangle(new GeoPoint(40.26,
				-80.26), new GeoPoint(40.62, -79.75));
		mapCityMBR.put(pitttableName, pittRectangle);

		String nytableName = "nygeotwitters";
		GeoRectangle nyRectangle = new GeoRectangle(new GeoPoint(40.559,
				-74.257), new GeoPoint(40.849, -73.762));
		mapCityMBR.put(nytableName, nyRectangle);

		return mapCityMBR;
	}
	
	public static boolean CheckFileBeenScanned(String pathName, String toBeScannedJsonFile) throws IOException
	{
		BufferedReader bReader = new BufferedReader(new FileReader(pathName));
		
		String str = "";
		
		while((str = bReader.readLine()) != null){
			if(str.compareTo(toBeScannedJsonFile) == 0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	

	public static void main(String[] args) throws Exception {
		
		Connection connection = ConnectDatabase("107.170.82.229", "5432",
				"twitterdb", "postg", "abcdefg");

		Map<String, GeoRectangle> mapCityMBR = InitializeMapOfCityMBR();
		
		String scannedFile = "scannedJsonFiles.txt";

		ArrayList<File> files = ScanFiles("/media/Elements/tweetRelated/NYAndSF_7-9/NY");
		for (File f : files) {
			System.out.println("New File:");
			System.out.println(f.getName());
			
			if(CheckFileBeenScanned(scannedFile, f.getName())){
				System.out.println(f.getName() + " has been scanned, ignore it");
				continue;
			}
				
			ParseTwitRecorders(mapCityMBR, f.getAbsolutePath(), connection);
			
			
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(new File(scannedFile), true));
			bWriter.write(f.getName() + "\n");
			bWriter.close();
		}
	

		System.out.println("All done");
	}
}
