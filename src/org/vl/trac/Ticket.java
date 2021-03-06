package org.vl.trac;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class Ticket {

	public int id;
	public Date dateCreated;
	public Date dateChanged;
	public TicketAttributes attributes;
	
	public Vector<TicketChange> changeLog;
	public HashMap<String,TicketAction> actions;
	
	public Ticket(int id, Date dateCreated, Date dateChanged,
			TicketAttributes attributes) {
		super();
		this.id = id;
		this.dateCreated = dateCreated;
		this.dateChanged = dateChanged;
		this.attributes = attributes;

		this.actions = new HashMap<String,TicketAction>();
		this.changeLog = new Vector<TicketChange>();
	}

	public static Ticket fromXMLRPC_ticket_get(Object[] array) {
		return new Ticket(
					((Integer) array[0]).intValue(),
					(Date) array[1], (Date) array[2],
					TicketAttributes.fromXMLRPC((HashMap<String, Object>) array[3]));
	}

	public void setActions(HashMap<String,TicketAction> actions) {
		this.actions = actions;
	}
	
	public void setChangeLog(Vector<TicketChange> changeLog) {
		this.changeLog = changeLog;
	}
	

}
