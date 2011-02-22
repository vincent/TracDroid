package org.vl.trac;

import java.util.Date;
import java.util.HashMap;

public class Milestone {

	public String name;
	public String description;
	public Date dateCreated;
	public Date dateDue;
	
	public Milestone() {
		super();
	}

	public Milestone(String name, String description, Date dateDue, boolean completed) {
		super();
		this.name = name;
		this.description = description;
		this.dateCreated = dateCreated;
		this.dateDue = dateDue;
	}

	public static Milestone fromXMLRPC(HashMap<String,Object> obj) {
		return new Milestone(obj.get("name").toString(), obj.get("description").toString(),
							(obj.get("due") instanceof Date ? (Date) obj.get("due") : null), ((Integer) obj.get("completed") != 0));
	}

}
