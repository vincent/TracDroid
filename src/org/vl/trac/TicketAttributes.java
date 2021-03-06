package org.vl.trac;

import java.util.HashMap;

public class TicketAttributes {

	public String summary;
	public String description;
	
	public String reporter;
	public String keywords;
	public String component;
	public String status;
	public String type;
	public String milestone;
	public String priority;
	public String owner;
	public String resolution;
	public String cc;
	public String version;
	
	public TicketAttributes(String summary, String description, String reporter, 
			String owner, String keywords, String component, String status,
			String type, String milestone, String priority, String resolution,
			String cc, String version) {
		super();
		this.summary = summary;
		this.description = description;
		this.reporter = reporter;
		this.owner = owner;
		this.keywords = keywords;
		this.component = component;
		this.status = status;
		this.type = type;
		this.milestone = milestone;
		this.priority = priority;
		this.resolution = resolution;
		this.cc = cc;
		this.version = version;
	}

	public TicketAttributes(String reporter) {
		super();
		this.reporter = reporter.trim();
		this.owner = reporter.trim();
		this.summary = "";
		this.description = "";
		this.keywords = "";
		this.component = "";
		this.status = "";
		this.type = "";
		this.milestone = "";
		this.priority = "";
		this.resolution = "";
		this.version = "";
		this.cc = "";
	}

	public static TicketAttributes fromXMLRPC(HashMap<String,Object> map) {
		return new TicketAttributes(
				(String) map.get("summary"), (String) map.get("description"), 
				(String) map.get("reporter"), (String) map.get("owner"),
				(String) map.get("keywords"), (String) map.get("component"),
				(String) map.get("status"), (String) map.get("type"), 
				(String) map.get("milestone"), (String) map.get("priority"),
				(String) map.get("resolution"), (String) map.get("cc"),
				(String) map.get("version")
			);
	}
}
