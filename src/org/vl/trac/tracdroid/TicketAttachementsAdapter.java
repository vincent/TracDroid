package org.vl.trac.tracdroid;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import org.vl.trac.Ticket;
import org.vl.trac.TicketAttachement;

import org.vl.trac.tracdroid.R;
import android.content.Context;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class TicketAttachementsAdapter extends SeparatedListAdapter {

	static LinkedList<Map<String,?>> ticketAttachmentsList;
	static Ticket ticket;
	static String onlyShown;
	
	Context context; 
	
	public TicketAttachementsAdapter(Context context, Ticket t) {
		super(context);

		ticket = t;
		this.context = context;
		
		ticketAttachmentsList = new LinkedList<Map<String,?>>();

		feedticketAttachments();
		
		addSection("Attachments", new SimpleAdapter(context, ticketAttachmentsList, R.layout.list_complex_img,
				new String[] { SeparatedListAdapter.ITEM_TITLE, SeparatedListAdapter.ITEM_CAPTION, SeparatedListAdapter.ITEM_ACTION }, 
				new int[] { R.id.list_complex_title, R.id.list_complex_caption, R.id.list_complex_img }));

	}

	private void feedticketAttachments() {
		Vector<TicketAttachement> attachments = TracDroid.server.listAttachments(ticket.id);
		if (TracDroid.server.isOnError()) {
			Toast.makeText(context, context.getString(R.string.oops), Toast.LENGTH_SHORT).show();
			return;
		}
		
		ticketAttachmentsList.clear();
		
		if (attachments.size() > 0) {
			Iterator<TicketAttachement> it = attachments.iterator();
			while (it.hasNext()) {
				TicketAttachement att = it.next();
				// FIXME: use preg
				int nameLength = att.filename.length();
				ticketAttachmentsList.add(SeparatedListAdapter.createItem(att.filename, att.description,
											att.filename.substring(nameLength-3, nameLength).toUpperCase()));
			}
		}
		else {
			ticketAttachmentsList.add(SeparatedListAdapter.createItem(context.getString(R.string.no_attachment), "", ""));
		}
		notifyDataSetChanged();
	}

	public void add(String name, ImageView image) {
		int nameLength = name.length();
		ticketAttachmentsList.add(SeparatedListAdapter.createItem(name, "", name.substring(nameLength-3, nameLength).toUpperCase()));
		notifyDataSetChanged();
	}
	
	public boolean isEnabled(int position) {
		return false;
	}
}
