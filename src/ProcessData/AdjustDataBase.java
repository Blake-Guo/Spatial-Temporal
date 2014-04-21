package ProcessData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class AdjustDataBase {

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
	 * The function that add the two columns of the table: geompoint and
	 * geolonlat
	 * 
	 * @param connection
	 * @throws SQLException
	 */
	public static void AddGeomAndGeo(Connection connection) throws SQLException {
		String tablename = "pittgeotwitters";

		// the statement for database
		Statement statement = null;
		try {
			statement = connection.createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 1: Get all the records from the database.
		ResultSet dbrs = null;

		for (int i = 7; i < 12; i++) {

			try {
				String sql = "select twitterId, latitude, longitude from pittgeotwitters where extract(month from time at time zone 'UTC')="
						+ i;
				dbrs = statement.executeQuery(sql);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}

			ArrayList<String> twitterIdList = new ArrayList<>();
			ArrayList<Double> latitudeList = new ArrayList<>();
			ArrayList<Double> longitudeList = new ArrayList<>();

			while (dbrs.next()) {
				twitterIdList.add(dbrs.getString(1));
				latitudeList.add(dbrs.getDouble(2));
				longitudeList.add(dbrs.getDouble(3));
			}

			// 2: Go over the records and insert them back to the database as
			// the
			// PostGIS format.
			int k = 0;
			while (k < twitterIdList.size()) {

				String twitterid = "'" + twitterIdList.get(k) + "'";
				double latitude = latitudeList.get(k);
				double longitude = longitudeList.get(k);

				System.out
						.println(twitterid + " " + longitude + " " + latitude);

				String geompoint = "ST_Transform( ST_GeometryFromText('POINT("
						+ longitude + " " + latitude + ")' ,4326), 916)";
				String geolonlat = "ST_GeometryFromText('POINT(" + longitude
						+ " " + latitude + ")' ,4326)";

				String sql = "Update pittgeotwitters set geompoint = "
						+ geompoint + " , " + "geolonlat = " + geolonlat
						+ " where twitterid = " + twitterid;
				try {
					statement.execute(sql);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				k++;
			}
		}

		System.out.println("Adding Successfully");
	}

	public static void main(String[] args) throws SQLException {
		Connection conn = ConnectDatabase("107.170.82.229", "5432",
				"twitterdb", "postg", "abcdefg");
		AddGeomAndGeo(conn);
	}
}
