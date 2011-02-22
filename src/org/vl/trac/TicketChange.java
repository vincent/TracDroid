package org.vl.trac;

import java.util.Date;
import java.util.Map;

import org.vl.trac.tracdroid.PrettyDate;


public class TicketChange {

	public Date time;
	public String author;
	public String field;
	
	public String old;
	public String newv;
	
	public TicketChange(Date time, String author, String field, String old,
			String newv) {
		super();
		this.time = time;
		this.author = author;
		this.field = field;
		this.old = old;
		this.newv = newv;
	}
	
	public static TicketChange fromXMLRPC(Object[] array) {
		return new TicketChange( (Date) array[0], (String) array[1], 
				(String) array[2], (String) array[3], (String) array[4] );
	}
	
	public String getNiceTitle() {
		String title = "";
		String date = PrettyDate.between(new Date(), time);
		
		if (field.equals("comment") || field.equals("description")) {
			if (field.equals("comment"))
				title = String.format("%s,\n%s comments", date, author);
			else
				title = String.format("%s,\n%s change description", date, author);
		}
		else if (field.equals("attachment")) {
			title = String.format("%s,\n%s attached the file %s", date, author, newv);
		}
		else {
			String newvalue = newv;
			if (newvalue.length() == 0)
				title = String.format("%s,\n%s canceled %s", date, author, field);
			else
				title = String.format("%s,\n%s changed %s to %s", date, author, field, newv);
		}

		return title;
	}
}
