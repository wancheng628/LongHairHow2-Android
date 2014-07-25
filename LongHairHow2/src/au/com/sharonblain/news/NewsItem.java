package au.com.sharonblain.news;

public class NewsItem {
	private String bp_id ;
	private String title ;
	private String body ;
	
	public NewsItem(String id, String title, String body) {
		this.bp_id = id ;
		this.title = title ;
		this.body = body ;		
	}
	
	public String getId(){
		return bp_id ;
	}
	
	public String getTitle(){
		return title ;
	}
	
	public String getBody(){
		return body ;
	}
	
	public void setId(String bp_id) {
		this.bp_id = bp_id ;
	}
	
	public void setTitle(String title) {
		this.title = title ;
	}
	
	public void setBody(String body) {
		this.body = body ;
	}
}
