package ProcessData;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ParseFrame {
	
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
	
	
	/**
	 * Delete all those /n in the text
	 * @param ifilename
	 * @throws IOException 
	 */
	public static void preprocessFile_char(String ifilename, String ofilename) throws IOException
	{
		BufferedReader breader = new BufferedReader(new FileReader(ifilename));
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(ofilename));
		
		String title = breader.readLine();//the title
		bwriter.write(title+"\n");
		
		int ich;
		while((ich = breader.read()) != -1 )
		{
			int tcount = 0;
			char ch = (char) ich;
			String lineRecord = "" + ch ;
			
			while(true)
			{
				ch = (char) breader.read();
				if(ch == '\t')
				{
					tcount++;
				}
				else if(ch == '\n')
				{
					if(tcount<10)
						continue;//ignore the \n
					else
					{//already the end
						lineRecord += ch;
						break;
					}
				}
				
				lineRecord += ch;
			}
			
			System.out.println(lineRecord);
			bwriter.write(lineRecord);
			
		}
		
		breader.close();
		bwriter.close();
		
	}
	
	public static int countTab(String str)
	{
		int count = 0;
		for(int i=0;i<str.length();i++)
		{
			if(str.charAt(i) == '\t')
				count++;
		}
		
		return count;
	}
	
	
	/**
	 * Delete all those /n in the text
	 * @param ifilename
	 * @throws IOException 
	 */
	public static void preprocessFile_line(String ifilename, String ofilename) throws IOException
	{
		BufferedReader breader = new BufferedReader(new FileReader(ifilename));
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(ofilename));
		
		String title = breader.readLine();//the title
		bwriter.write(title+"\n");
		String line = "";
		while( (line=breader.readLine()) != null )
		{
			int itab = countTab(line);
			
			String appendLine = "";
			while(itab < 10 && (appendLine=breader.readLine())!=null)
			{
				appendLine = " " + appendLine;
				itab += countTab(appendLine);
				line += appendLine;
			}
			
			System.out.println(line);
			bwriter.write(line+"\n");
		}
		
		
		breader.close();
		bwriter.close();
		
	}
	
	/**
	 * Given the data frame(from Ke's Pittsburgh data), parse it and store them in the database.
	 * @param filename
	 * @param conn
	 * @throws IOException 
	 * @throws ParseException 
	 * @throws SQLException 
	 */
	public static void parseFrame(String filename, String seperator, Connection conn, String tableName) throws IOException, ParseException, SQLException
	{
		//userID	placeID	timeString	lat	lng	day_of_week	hour_of_day	tweetID	tweetText	url	expanded_url
		 BufferedReader breader = new BufferedReader(new FileReader(filename));
		 
		 String lineStr = "";
		 breader.readLine();//the header
		 
		 while( (lineStr = breader.readLine()) != null )
		 {			 
			 TwitterRecorder twrec = new TwitterRecorder(); 

			 String[] strs = lineStr.split(seperator);
			 
			 System.out.println(lineStr + ", " + strs.length );
			 
			 //0:userID
			 twrec.SetUserId(strs[0]);
			 
			 //1:placeID, skip it
			 
			 //2:timeString
			String timeStr = strs[2].substring(1, strs[2].length());//delete the double quotation.
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.S");
			java.util.Date date = df.parse(timeStr);
			twrec.SetTime(date);
			
			//3:lat, 4:lng
			GeoPoint geopoint = new GeoPoint();
			geopoint.latitude = Double.parseDouble(strs[3]);
			geopoint.longitude = Double.parseDouble(strs[4]);
			twrec.SetGeoPoint(geopoint);
			
			//5: day_of_week, skip it
			//6: hour_of_day, skip it
			
			//7:tweetID
			twrec.SetTwitterId(strs[7].substring(1,strs[7].length()));//delete the double quotation.
			
			//8:tweetText
			if(strs.length > 8)
				twrec.SetText(strs[8]);
			
			//9: url, skip
			
			
			//10: expanded_url
			if(strs.length > 10 && strs[10]!="null")
				twrec.SetURL(strs[10]);
			
			
			//insert it into the database
			InsertTwitterRecordIntoTable( twrec,  conn,  tableName);
			
		 }
	}
	
	/**
	 * Given an twitter recorder, insert it into the database
	 * @param twrec
	 * @param conn
	 * @param tableName
	 * @throws SQLException 
	 */
	public static void InsertTwitterRecordIntoTable(TwitterRecorder twrec, Connection connection, String tableName) throws SQLException
	{
		Statement statement = connection.createStatement();
		
		DateFormat dfout = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		dfout.setTimeZone(TimeZone.getTimeZone("UTC"));
		String timeformat = dfout.format(twrec.GetTime());
		
		String glonlat = "ST_GeometryFromText('POINT("+ twrec.GetGeoPoint().longitude + " " + twrec.GetGeoPoint().latitude + ")' ,4326)";
		String geompoint = "ST_Transform( ST_GeometryFromText('POINT("+
				twrec.GetGeoPoint().longitude + " " + twrec.GetGeoPoint().latitude + ")' ,4326), 916)";
		
		String val = "'" + twrec.GetTwitterId() + "','"
				+ twrec.GetUserId() + "','" + timeformat + "','"
				+ twrec.GetText() + "','"
				+ twrec.getURL() + "',"
				+ String.valueOf(twrec.GetGeoPoint().latitude) + ","
				+ String.valueOf(twrec.GetGeoPoint().longitude) + ","
				+ glonlat + ","
				+ geompoint;
		String sql = "insert into " + tableName + "(twitterId, userid, time, text, url, latitude, longitude, geolonlat, geompoint)"
				+ " values(" + val + ")";

		try {
			statement.executeUpdate(sql);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, ParseException, SQLException
	{
		Connection conn = ConnectDatabase();
		parseFrame("pittsburgh_twitters", "\t",  conn, "pittgeotwitters");
		System.out.println("Done");
		
		//preprocessFile_line("Original_Pittsburgh_Twitters.txt","pittsburgh_twitters");
		//preprocessFile_char("itest","otest");
		//preprocessFile_line("itest","otest");
	}
}
