package ProcessData;

public class GeoRectangle {
	
	public GeoPoint southwest;
	public GeoPoint northeast;
	
	public GeoRectangle()
	{
		this.southwest = new GeoPoint(0,0);
		this.northeast = new GeoPoint(0,0);
	}
	
	
	public GeoRectangle(GeoPoint sw, GeoPoint ne)
	{
		this.southwest = sw;
		this.northeast = ne;
	}
	
	public boolean contain(GeoPoint point){
		
		if(point.latitude >= southwest.latitude && point.latitude <= northeast.latitude
				&& point.longitude >= southwest.longitude && point.longitude <= northeast.longitude)
			return true;
		
		return false;
	}

}
