
<%@ page
	import=" DateHelper.*, com.fasterxml.jackson.core.*, com.fasterxml.jackson.databind.*,com.fasterxml.jackson.databind.node.* ,java.sql.* , java.util.*, java.io.*"%>


<%
	try {
		Class.forName("org.postgresql.Driver");
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	}

	System.out.println("PostgreSQL JDBC Driver Registered!");

	Connection connection = null;
	try {
		connection = DriverManager.getConnection(
				"jdbc:postgresql://107.170.82.229:5432/twitterdb",
				"postg", "abcdefg");
	}

	catch (SQLException e) {
		e.printStackTrace();
		return;
	}

	if (connection != null) {
		System.out.println("You made it");
	} else {
		System.out.println("You failed to connect the database");
	}

	String point1 = request.getParameter("point1");
	String point2 = request.getParameter("point2");

	//1: select the table
	String selectedCity = request.getParameter("city");
	System.out.println("selectedCity:" + selectedCity);

	String tableName = "sfgeotwitters";
	if (selectedCity.compareTo("Pittsburgh") == 0) {
		tableName = "pittgeotwitters";
	}

	else if (selectedCity.compareTo("San Francisco") == 0) {
		tableName = "sfgeotwitters";
	}

	else if (selectedCity.compareTo("New York") == 0) {
		tableName = "nygeotwitters";
	}

	//2: select the date:
	int month = 9;
	int day = 2;//Currently only consider the days within the same month.
	int week = 2;

	//3: select the time zone
	String timezone = DateHelper.calTimeZone(selectedCity, month);
	System.out.println("time zone:" + timezone);

	// Query the database for 3 weeks' data. Currenlty only consider the data within the same month.
	try {
		int len1 = point1.length();
		int len2 = point2.length();
		point1 = point1.substring(1, len1 - 1);
		point2 = point2.substring(1, len2 - 1);
		String[] latlon1 = point1.split(", ");
		String[] latlon2 = point2.split(", ");
		String[] bboxpoints = new String[5];
		//In the postgresql, the longitude comes first and latitude.
		bboxpoints[0] = latlon1[1] + " " + latlon1[0];
		bboxpoints[1] = latlon2[1] + " " + latlon1[0];
		bboxpoints[2] = latlon2[1] + " " + latlon2[0];
		bboxpoints[3] = latlon1[1] + " " + latlon2[0];
		bboxpoints[4] = latlon1[1] + " " + latlon1[0];

		String coords = bboxpoints[0];
		for (int i = 1; i < 5; i++)
			coords += ", " + bboxpoints[i];

		//Query each week's check-in pattern.
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		for (int i = day,k = 0; k < week ; i+=7,k+=1) {

			int monday = i;
			int sunday = i + 6;

			String preSql = "select extract(isodow from time at time zone "
					+ timezone
					+ " ) as isodow, extract(hour from time at time zone  "
					+ timezone
					+ " ) as hour, count(*) as number"
					+ " from "
					+ tableName
					+ "  where extract(month from time at time zone  "
					+ timezone
					+ " )="
					+ month
					+ " and extract(day from time at time zone  "
					+ timezone
					+ " )>="
					+ monday
					+ " and extract(day from time at time zone  "
					+ timezone
					+ " )<="
					+ sunday
					+ " and"
					+ " ST_Contains( ST_GeomFromText('POLYGON((";

			String postSql = "))',4326), geolonlat)"
					+ " group by extract(isodow from time at time zone  "
					+ timezone
					+ " ), extract(hour from time at time zone  "
					+ timezone
					+ " )"
					+ " order by extract(isodow from time at time zone  "
					+ timezone
					+ " ), extract(hour from time at time zone  "
					+ timezone + " )";

			//		String preSql = "select extract(dow from time at time zone 'PDT') as dow, extract(hour from time at time zone 'PDT') as hour, count(*) as number" +
			//				" from " + tableName + 
			//				"  where extract(month from time at time zone 'PDT')=9" +
			//				" and extract(day from time at time zone 'PDT')>=9 and extract(day from time at time zone 'PDT')<=15 and" +
			//				" ST_Contains( ST_GeomFromText('POLYGON((";

			//		String postSql = "))',4326), geolonlat)" +
			//		" group by extract(dow from time at time zone 'PDT'), extract(hour from time at time zone 'PDT')" +
			//		" order by extract(dow from time at time zone 'PDT'), extract(hour from time at time zone 'PDT')";

			Statement st = connection.createStatement();
			String sql = preSql + coords + postSql;

			ResultSet dbrs = st.executeQuery(sql);

			while (dbrs.next()) {
				int isodow = dbrs.getInt(1);
				String hour = dbrs.getString(2);
				int num = dbrs.getInt(3);
				
				isodow = k * 7 + isodow;
								
				rootNode.with(Integer.toString(isodow)).put(hour, num);
			}
		}

		String jsonVal = mapper.writeValueAsString(rootNode);
		
		out.print(jsonVal);

	} catch (SQLException e) {
		e.printStackTrace();
	}
%>