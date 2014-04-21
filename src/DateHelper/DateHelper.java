package DateHelper;

public class DateHelper {
	
	public static String calTimeZone(String city, int month)
	{
		String timezone = "";
		if(city.compareTo("San Francisco") == 0)
		{
			if( month >=3 && month<=10)
				timezone = "'PDT'";
			else 
				timezone = "'PST'";
		}
		
		else if(city.compareTo("Pittsburgh") == 0 || city.compareTo("New York") == 0){
			if( month >=3 && month<=10)
				timezone = "'EDT'";
			else 
				timezone = "'EST'";
		}
		
		return timezone;
	}
}
