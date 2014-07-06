package au.com.sharonblain.search;

public class BunImageArray {
    private String name;
    private String description;
    private String image_url ;
    
    public BunImageArray(String name, String desc, String url) 
    {
    	this.name = name ;
    	this.description = desc ;
    	this.image_url = url ;
    	
    }
    
    public String getName()
    {
    	return this.name ;
    }
    
    public String getDescription()
    {
    	return this.description ;
    }
    
    public String getImageUrl()
    {
    	return this.image_url ;
    }
    
}
