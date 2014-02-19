package ProcessData;

import java.text.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import java.sql.Date;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

public class parsemain {

	public static Connection ConnectDatabase() {
		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
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

	public static ArrayList<File> ScanFiles(String filename) {
		ArrayList<File> files = new ArrayList<File>();

		File rootFile = new File(filename);

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

			Iterator<JsonNode> iter = jNode.path("entities").path("urls").iterator();
			String urlStr="";
			if(iter.hasNext())
			{
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
	
	public static void AddColumns_TimeAndGeoAndGeom(String filePath, Connection connection) throws Exception
	{
		Statement statement = connection.createStatement();
		
		JsonFactory f = new MappingJsonFactory();
		
		JsonParser jp = f.createParser(new File(filePath));
		
		while(jp.nextToken() == JsonToken.START_OBJECT){
			jp.nextToken();
			
			JsonNode jNode = null;
			
			try{
				jNode = jp.readValueAsTree();
			}catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			
			//Get twitter id;
			String idStr = jNode.path("id").asText();
			
			
			//Get the timestamp
			String timeStr = jNode.path("created_at").asText();
			DateFormat df = new SimpleDateFormat(
			"EEE MMM d HH:mm:ss Z yyyy");
			java.util.Date date = df.parse(timeStr);
			
			DateFormat dfout = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
			dfout.setTimeZone(TimeZone.getTimeZone("UTC"));
			String timeformat = dfout.format(date);
			//get the location;
			GeoPoint gPoint = null;
			if (jNode.path("coordinates").path("type").asText().equals("Point")) {

				ArrayList<Double> lonlat = new ArrayList<Double>();
				gPoint = new GeoPoint(0,0);

				for (JsonNode tmpNode : jNode.path("coordinates").path(
						"coordinates")) {
					lonlat.add(Double.parseDouble(tmpNode.asText()));
				}
				gPoint.longitude=lonlat.get(0);
				gPoint.latitude=lonlat.get(1);
			}
			
			if( gPoint != null )
			{
				String glonlatVal = "ST_GeometryFromText('POINT("+ gPoint.longitude + " " + gPoint.latitude + ")' ,4326)";
				String geompointVal = "ST_Transform( ST_GeometryFromText('POINT("+
				gPoint.longitude + " " + gPoint.latitude + ")' ,4326), 916)";
				
				String sql="update sfgeotwitters set geolonlat=" + glonlatVal + 
						", geompoint=" + geompointVal + 
						", time='" + timeformat + "'" + 
						" where twitterid='" + idStr+"'";
				
				try{
					statement.executeUpdate(sql);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public static void ParseTwitRecorders(String filename, Connection connection)
			throws Exception {

		Statement statement = connection.createStatement();
		// connection.setAutoCommit(false);
		JsonFactory f = new MappingJsonFactory();

		JsonParser jp = f.createParser(new File(filename));

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

			DateFormat df = new SimpleDateFormat(

			"EEE MMM d HH:mm:ss Z yyyy");

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
				String val = "'" + twrec.GetTwitterId() + "','"
						+ twrec.GetUserId() + "','" + timeformat + "','"
						+ twrec.GetText() + "',"
						+ String.valueOf(twrec.GetGeoPoint().latitude) + ","
						+ String.valueOf(twrec.GetGeoPoint().longitude);
				String sql = "insert into sfgeotwitters(twitterId, userid, time, text, latitude, longitude)"
						+ " values(" + val + ")";

				try {
					statement.executeUpdate(sql);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}// if !=0

		}// while

		// connection.commit();

		System.out.println(filename + "Parse Successfully");
	}

	public static void main(String[] args) throws Exception {

		Connection connection = ConnectDatabase();

		// ArrayList<File> files = ScanFiles("SF");
		// for (File f : files) {
		// System.out.println(f.getName());
		// // System.out.println(f.getName());
		// //ParseTwitRecorders("SF/" + f.getName(), connection);
		// AddOneColumn_URL("SF/" + f.getName(), connection);
		// }
		
//		 ArrayList<File> files = ScanFiles("SF");
//		 for (File f : files) {
//		   System.out.println(f.getName());
//		   AddColumns_TimeAndGeoAndGeom("SF/" + f.getName(), connection);
//		 }
		
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream0.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream1.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream2.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream3.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream4.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream5.json", connection);
		
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream6.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream7.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream8.json", connection);
		
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream9.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream10.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream11.json", connection);
		
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream12.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream13.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream14.json", connection);
		
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream15.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream16.json", connection);
//		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream17.json", connection);

		AddColumns_TimeAndGeoAndGeom("SF/" + "SF_TweetStream18.json", connection);

		System.out.println("All done");
	}
}
