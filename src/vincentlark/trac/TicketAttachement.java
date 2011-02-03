package vincentlark.trac;

import java.util.Date;
import java.util.HashMap;

public class TicketAttachement {
	
	String filename;
	String description;
	int size;
	Date time;
	String author;

	public TicketAttachement(String filename, String description, int size, Date time, String author) {
		super();
		this.filename = filename;
		this.description = description;
		this.size = size;
		this.time = time;
		this.author = author;
	}

	public static TicketAttachement fromXMLRPC(HashMap<String,Object> map) {
		return new TicketAttachement(
				(String) map.get("filename"), (String) map.get("description"), 
				((Integer) map.get("size")).intValue(), (Date) map.get("time"),
				(String) map.get("author"));
	}
}
