package org.vl.trac.tracdroid;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.vl.trac.Ticket;
import org.vl.trac.TicketChange;

import org.vl.trac.tracdroid.R;
import android.content.Context;
import android.widget.SimpleAdapter;

public class TicketChangelogAdapter extends SeparatedListAdapter {

	static LinkedList<Map<String,?>> ticketCommentList;
	static Ticket ticket;
	
	Context context; 
	
	private boolean isReversed = false;
	
	public TicketChangelogAdapter(Context context, Ticket t) {
		super(context);

		ticket = t;
		this.context = context;
		
		ticketCommentList = new LinkedList<Map<String,?>>();

		feedChangelog();
		
		addSection("Changelog", new SimpleAdapter(context, ticketCommentList, R.layout.list_complex,
				new String[] { SeparatedListAdapter.ITEM_TITLE, SeparatedListAdapter.ITEM_CAPTION, SeparatedListAdapter.ITEM_ACTION }, 
				new int[] { R.id.list_complex_title, R.id.list_complex_caption, R.id.list_complex_action }));

	}

	private void feedChangelog() {
		ticketCommentList.clear();
		Vector<TicketChange> changelog = (Vector<TicketChange>) ticket.changeLog;
		Iterator<TicketChange> itc = changelog.iterator();
		while (itc.hasNext()) {
			TicketChange change = (TicketChange) itc.next();
			
			String field = (String) change.field;
			String niceTitle = change.getNiceTitle();
			
			if (field.equals("comment") || field.equals("description")) {
				if (change.newv.length()>0)
					if (isReversed)
						ticketCommentList.addFirst(SeparatedListAdapter.createItem(niceTitle, change.newv));
					else
						ticketCommentList.add(SeparatedListAdapter.createItem(niceTitle, change.newv));
			}
			else {
				if (isReversed)
					ticketCommentList.addFirst(SeparatedListAdapter.createItem(niceTitle));
				else
					ticketCommentList.add(SeparatedListAdapter.createItem(niceTitle));
			}
		}
		notifyDataSetChanged();
	}

	public boolean isEnabled(int position) {
		return false;
	}

	public boolean reverse() {
		isReversed = !isReversed;
		feedChangelog();
		return isReversed;
	}

	public boolean isReversed() {
		return isReversed;
	}

}
