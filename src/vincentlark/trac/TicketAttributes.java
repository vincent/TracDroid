package vincentlark.trac;

import java.util.HashMap;

public class TicketAttributes {

	public String summary;
	public String description;
	
	public String reporter;
	public String keywords;
	public String component;
	public String status;
	public String milestone;
	public String priority;
	public String owner;
	
	public TicketAttributes(String summary, String description, String reporter, 
			String owner, String keywords, String component, String status,
			String milestone, String priority) {
		super();
		this.summary = summary;
		this.description = description;
		this.reporter = reporter;
		this.owner = owner;
		this.keywords = keywords;
		this.component = component;
		this.status = status;
		this.milestone = milestone;
		this.priority = priority;
	}

	public static TicketAttributes fromXMLRPC(HashMap<String,Object> map) {
		return new TicketAttributes(
				(String) map.get("summary"), (String) map.get("description"), 
				(String) map.get("reporter"), (String) map.get("owner"),
				(String) map.get("keywords"), (String) map.get("component"),
				(String) map.get("status"), (String) map.get("milestone"),
				(String) map.get("priority"));
	}
}
