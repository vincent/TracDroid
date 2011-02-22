package org.vl.trac.tracdroid;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.vl.trac.Ticket;
import org.vl.trac.TicketAction;

import org.vl.trac.tracdroid.R;
import android.content.Context;
import android.widget.SimpleAdapter;

public class TicketActionsAdapter extends SeparatedListAdapter {

	static int MINIMUM_ENABLED_POSITION = 2;
	static LinkedList<Map<String,?>> ticketPropertiesList;
	static LinkedList<Map<String,?>> ticketActionsList;
	static LinkedList<Map<String,?>> ticketSelectedActionsList;
	
	static final String propertiesHeaderString = "Properties";
	static final String actionsHeaderString = "Actions";
	static final String selectedActionsHeaderString = "Selected action";
	
	SimpleAdapter selectedActionsAdapter;
	SimpleAdapter actionsAdapter;

	Ticket ticket;
	String selected;
	
	Context context;
	
	public TicketActionsAdapter(final Context context, Ticket t) {
		super(context);

		ticket = t;
		this.context = context;
		
		ticketPropertiesList = new LinkedList<Map<String,?>>();
		ticketActionsList = new LinkedList<Map<String,?>>();
		ticketSelectedActionsList = new LinkedList<Map<String,?>>();

		String[] keys = new String[] { SeparatedListAdapter.ITEM_TITLE, SeparatedListAdapter.ITEM_CAPTION, SeparatedListAdapter.ITEM_ACTION };
		int[] ids = new int[] { R.id.list_complex_title, R.id.list_complex_caption, R.id.list_complex_action };
		
		// Add non hideable list: properties
		addSection(propertiesHeaderString, new SimpleAdapter(context, ticketPropertiesList, R.layout.list_complex, keys, ids));
		
		// Add hideable lists: actions
		actionsAdapter = new SimpleAdapter(context, ticketActionsList, R.layout.list_complex, keys, ids);
		selectedActionsAdapter = new SimpleAdapter(context, ticketSelectedActionsList, R.layout.list_complex_selected, keys, ids);

		// Feed lists
		feedProperties();
		feedActions();

		addSection(actionsHeaderString, actionsAdapter);
		addSection(selectedActionsHeaderString, selectedActionsAdapter);
		
		// Show all
		setSelected(null);
	}

	public void feedProperties() {
		ticketPropertiesList.clear();

		// Reported by ..
		String created = PrettyDate.between(new Date(), ticket.dateCreated);
		ticketPropertiesList.add(SeparatedListAdapter.createItem(
					String.format(context.getString(R.string.reported_by_on), ticket.attributes.reporter, created), 
					""));

		// Owned by ..
		if (ticket.attributes.owner != null)
			ticketPropertiesList.add(SeparatedListAdapter.createItem(
					String.format(context.getString(R.string.owned_by), ticket.attributes.owner),""));
		else
			ticketPropertiesList.add(SeparatedListAdapter.createItem(
					context.getString(R.string.not_owned),""));

		// Current status
		ticketPropertiesList.add(SeparatedListAdapter.createItem(
					String.format(context.getString(R.string.status_is), ticket.attributes.status),
					""));

		// Type
		ticketPropertiesList.add(SeparatedListAdapter.createItem(context.getString(R.string.ticket_type), ticket.attributes.type, ">"));
		
		// Component
		ticketPropertiesList.add(SeparatedListAdapter.createItem(context.getString(R.string.ticket_component), ticket.attributes.component, ">"));
		
		// Milestone
		ticketPropertiesList.add(SeparatedListAdapter.createItem(context.getString(R.string.ticket_milestone), ticket.attributes.milestone, ">"));
		
		// Priority
		ticketPropertiesList.add(SeparatedListAdapter.createItem(context.getString(R.string.ticket_priority), ticket.attributes.priority, ">"));
		
	}

	public void feedActions() {
		ticketActionsList.clear();

		if (selected == null) {
			HashMap<String,TicketAction> actions = ticket.actions;
			Iterator<TicketAction> ita = actions.values().iterator();
			while (ita.hasNext()) {
				TicketAction action = ita.next();
				if (action.label.equals("leave")) continue;
			    ticketActionsList.add(SeparatedListAdapter.createItem(action.label, (action.hints.length() > 1 ? action.hints : ""), ">"));
			}
		}
	}
	
	public void feedSelectedActions() {
		ticketSelectedActionsList.clear();

		if (selected != null && ticket.actions.containsKey(selected)) {
			TicketAction action = ticket.actions.get(selected);
			ticketSelectedActionsList.add(SeparatedListAdapter.createItem(action.label, (action.hints.length() > 1 ? action.hints : "o")));
		}
	}
	
	public void setSelected(String title) {
		selected = title;
		
		// FIXME: can't hide a section :/
		
		feedActions();
		feedSelectedActions();
		notifyDataSetInvalidated();
	}
}
