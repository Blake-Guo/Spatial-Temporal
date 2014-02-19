package ProcessData;

import java.util.*;

//@JsonIgnoreProperties({"id_str","source","truncated","in_reply_to_status_id","in_reply_to_status_id_str","in_reply_to_user_id",
//	"in_reply_to_user_id_str","in_reply_to_screen_name","user","geo","coordinates","place","contributors","retweet_count",
//	"favorite_count","entities","favorited","retweeted","filter_level","lang"})
public class TwitterRecorder {
	
	private String twitterId;
	private String userId;
	private GeoPoint gPoint;
	private String ttext;
	private Date time;
	private String url;
	
	public TwitterRecorder()
	{
		gPoint = new GeoPoint();
	}
	
	
	public void SetTwitterId(String id)
	{
		this.twitterId = id;
	}
	
	public String GetTwitterId()
	{
		return this.twitterId;
	}
	
	public void SetUserId(String id)
	{
		this.userId = id;
	}
	public String GetUserId()
	{
		return this.userId;
	}
	
	public void SetGeoPoint(GeoPoint point)
	{
		this.gPoint = point;
	}
	
	public void SetGeoPoint(double lon, double lat)
	{
		this.gPoint.longitude = lon;
		this.gPoint.latitude = lat;
	}
	
	public GeoPoint GetGeoPoint()
	{
		return this.gPoint;
	}
	
	public void SetText(String str)
	{
		this.ttext = str;
	}
	
	public String GetText()
	{
		return this.ttext;
	}
	
	public void SetTime(Date t)
	{
		this.time = t;
	}
	
	public Date GetTime()
	{
		return this.time;
	}
	
	public void SetURL(String url)
	{
		this.url = url;
	}
	
	public String getURL()
	{
		return url;
	}
	
}
