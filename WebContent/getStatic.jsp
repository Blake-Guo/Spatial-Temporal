
<%@ page import="com.fasterxml.jackson.core.*, com.fasterxml.jackson.databind.*,com.fasterxml.jackson.databind.node.* ,java.sql.* , java.util.*, java.io.*" %>

 
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
		try{
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/twitterdb","postgres","postgres");
		}
		
		catch(SQLException e)
		{
			e.printStackTrace();
			return;
		}
		
		if(connection != null)
		{
			System.out.println("You made it");
		}
		else {
			System.out.println("You failed to connect the database");
		}
				
		try {
			String point1 = request.getParameter("point1");
			String point2 = request.getParameter("point2");
			int len1 = point1.length();
			int len2 = point2.length();
			point1 = point1.substring(1,len1-1);
			point2 = point2.substring(1,len2-1);
			String[] latlon1 = point1.split(", ");
			String[] latlon2 = point2.split(", ");
			String[] bboxpoints = new String[5];
			//In the postgresql, the longitude comes first and latitude.
			bboxpoints[0] = latlon1[1] + " " + latlon1[0];
			bboxpoints[1] = latlon2[1] + " " + latlon1[0];
			bboxpoints[2] = latlon2[1] + " " + latlon2[0];
			bboxpoints[3] = latlon1[1] + " " + latlon2[0];
			bboxpoints[4] = latlon1[1] + " " + latlon1[0];
			
			String coords =bboxpoints[0];
			for(int i=1;i<5;i++)
				coords += ", "+bboxpoints[i];
			
			String preSql = "select extract(dow from time at time zone 'PDT') as dow, extract(hour from time at time zone 'PDT') as hour, count(*) as number" +
					" from sfgeotwitters" +
					" where extract(month from time at time zone 'PDT')=9" +
					" and extract(day from time at time zone 'PDT')>=9 and extract(day from time at time zone 'PDT')<=15 and" +
					" ST_Contains( ST_GeomFromText('POLYGON((";
					
			String postSql = "))',4326), geolonlat)" +
			" group by extract(dow from time at time zone 'PDT'), extract(hour from time at time zone 'PDT')" +
			" order by extract(dow from time at time zone 'PDT'), extract(hour from time at time zone 'PDT')";
			
			Statement st = connection.createStatement();
			String sql = preSql + coords + postSql;
			//String sql = "select extract(dow from time at time zone 'PDT') as dow, extract(hour from time at time zone 'PDT') as hour, count(*) as number" +
			//		" from sfgeotwitters" +
			//		" where extract(month from time at time zone 'PDT')=8" +
			//		" and extract(day from time at time zone 'PDT')>=24 and extract(day from time at time zone 'PDT')<=30 and" +
			//		" ST_Contains( ST_GeomFromText('POLYGON(( -125.83740234375 31.57853542647338,  -114.71923828125 31.57853542647338,-114.71923828125 40.027614437486655,-125.83740234375 40.027614437486655, -125.83740234375 31.57853542647338))',4326), geolonlat)" +
			//		" group by extract(dow from time at time zone 'PDT'), extract(hour from time at time zone 'PDT')" +
			//		" order by extract(dow from time at time zone 'PDT'), extract(hour from time at time zone 'PDT')";

			ResultSet dbrs = st.executeQuery(sql);
			
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode rootNode = mapper.createObjectNode();
			
			while(dbrs.next())
			{
				String date = dbrs.getString(1);
				String hour = dbrs.getString(2);
				int num = dbrs.getInt(3);
	
				rootNode.with(date).put(hour, num);
			}
			
			String jsonVal = mapper.writeValueAsString(rootNode);
			
			out.print(jsonVal);
			//out.print(sql);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	%>