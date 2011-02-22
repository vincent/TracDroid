package org.vl.trac.tracdroid;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vl.trac.Milestone;
import org.vl.trac.MilestoneSummary;

import android.content.Context;
import android.widget.SimpleAdapter;

public class MilestoneSummaryAdapter extends SeparatedListAdapter {

	static int MINIMUM_ENABLED_POSITION = 2;
	static LinkedList<Map<String,?>> summaryList;
	
	static final String propertiesHeaderString = "Properties";
	
	MilestoneSummary summary;
	Milestone milestone;
	Context context;
	
	public MilestoneSummaryAdapter(final Context context, Milestone milestone, MilestoneSummary milestoneSummary) {
		super(context);

		this.milestone = milestone;
		this.summary = milestoneSummary;
		this.context = context;
		
		// Feed lists
		feedProperties();
	}

	public void feedProperties() {

		String[] keys = new String[] { SeparatedListAdapter.ITEM_TITLE, SeparatedListAdapter.ITEM_CAPTION };
		int[] ids = new int[] { R.id.list_item_text, R.id.list_item_value };

		List<Map<String,String>> by;
		
		// All
		addSection(context.getString(R.string.all_tickets), new SimpleAdapter(context, summary.listForAll(), R.layout.list_item_paired, keys, ids));
		
		// Priority
		by = summary.countsListBy(MilestoneSummary.BY_STATUS);
		if (by.size() > 0)
			addSection(context.getString(R.string.by_status), new SimpleAdapter(context, by, R.layout.list_item_paired, keys, ids));
		
		// Types
		by = summary.countsListBy(MilestoneSummary.BY_TYPE);
		if (by.size() > 0)
			addSection(context.getString(R.string.by_type), new SimpleAdapter(context, by, R.layout.list_item_paired, keys, ids));
		
		// Component
		by = summary.countsListBy(MilestoneSummary.BY_COMPONENT);
		if (by.size() > 0)
			addSection(context.getString(R.string.by_component), new SimpleAdapter(context, by, R.layout.list_item_paired, keys, ids));
		
		// Owners
		by = summary.countsListBy(MilestoneSummary.BY_OWNER);
		if (by.size() > 0)
			addSection(context.getString(R.string.by_owner), new SimpleAdapter(context, by, R.layout.list_item_paired, keys, ids));
	}

}
