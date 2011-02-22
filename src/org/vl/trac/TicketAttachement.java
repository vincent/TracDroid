package org.vl.trac;

import java.util.Date;
import java.util.HashMap;

public class TicketAttachement {
	
	public String filename;
	public String description;
	public int size;
	public Date time;
	public String author;

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

	public static TicketAttachement fromXMLRPC(Object[] obj) {
		return new TicketAttachement(
				(String) obj[0], (String) obj[1], (Integer) obj[2],
				(Date) obj[3], (String) obj[4]);
	}

}
