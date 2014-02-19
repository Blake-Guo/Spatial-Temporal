package ProcessData;

public class GeoPoint {
	
	public GeoPoint()
	{
		latitude = 0.0f;
		longitude = 0.0f;
	}
	
	public GeoPoint(double lat, double lon)
	{
		latitude = lat;
		longitude = lon;
	}
	
	
	public double latitude;
	public double longitude;
}
