package org.vl.trac.tracdroid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.vl.trac.Ticket;
import org.vl.trac.TicketChange;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TicketsActivity extends ThreadedListActivity {

	public static int mode;
	public static int TICKETS_LIST_RECENT = 0;
	public static int TICKETS_LIST_QUERY = 1;
	
	static Vector<HashMap> dataRecent = new Vector<HashMap>();
	static Vector<Ticket> dataQuery = new Vector<Ticket>();

	int hours_timeback = 48;
	String ticketQuery = "";

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_list);

		// Fill toolbar
		LinearLayout toolbar = (LinearLayout) findViewById(R.id.list_toolbar);
		Button button_new = (Button) View.inflate(getApplicationContext(), R.layout.button, null);
		button_new.setText(R.string.create_ticket);
		toolbar.addView(button_new);
		button_new.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent().setClass(getApplicationContext(), TicketActivity.class);
				intent.putExtra("ticket_id", new Integer(0));
				startActivity(intent);
			}
		});
		
		Bundle params = this.getIntent().getExtras();
		if (params != null && params.containsKey("ticket_query")) {
			mode = TICKETS_LIST_QUERY;
			ticketQuery = params.getCharSequence("ticket_query").toString();
			adapter = new TicketSearchResultsListAdapter(getApplicationContext());
			setListAdapter(adapter);
		}
		else {
			mode = TICKETS_LIST_RECENT;
			adapter = new TicketChangesListAdapter(getApplicationContext());
			setListAdapter(adapter);
		}
		updateTitle();
		
		ListView list = (ListView) this.findViewById(android.R.id.list);
		list.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if (mode == TICKETS_LIST_RECENT) {
					
					int mySize = dataRecent.size();
					if (mySize <= position) {
						hours_timeback += 24;
						startLongRunningOperation();
						arg0.postInvalidate();
						return;
					}

					@SuppressWarnings("unchecked")
					HashMap<String,Object> ticket = dataRecent.get(position);
					
					if (ticket.get("excerpt") != null && ticket.get("excerpt").equals(true)) {
						// Show a big description
						ticket.put("excerpt", false);
						dataRecent.set(position, ticket);
						adapter.notifyDataSetChanged();
					}
					else {
						// Lauch TicketActivity for that ticket
						Intent intent = new Intent().setClass(getApplicationContext(), TicketActivity.class);
						intent.putExtra("ticket_id", (Integer) ticket.get("id"));
						startActivity(intent);
					}
				}
				else if (mode == TICKETS_LIST_QUERY) {
					Ticket ticket = dataQuery.get(position);

					// Lauch TicketActivity for that ticket
					Intent intent = new Intent().setClass(getApplicationContext(), TicketActivity.class);
					intent.putExtra("ticket_id", (Integer) ticket.id);
					startActivity(intent);
				}
			}
		});
		
		startLongRunningOperation();
	}
	
	protected void updateTitle() {
		if (mode == TICKETS_LIST_QUERY) {
			String title = getString(R.string.ticket_search_results);
			List<NameValuePair> params;
			try {
				params = URLEncodedUtils.parse(new URI("http://localhost/?"+ticketQuery), "utf-8");
				Log.d("TicketsActivity", "params decoded as "+params.toString());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				params = new LinkedList<NameValuePair>();
			}
			Iterator<NameValuePair> it = params.iterator();
			while (it.hasNext()) {
				NameValuePair nvp = it.next();
				title = title.concat("\n\t"+nvp.getName()+" : "+nvp.getValue());
			}
			((TextView) findViewById(R.id.list_title)).setText(title);
	        getWindow().setTitle(getString(R.string.app_name) + " - " + getString(R.string.ticket_search_results));
		}
		else {
			((TextView) findViewById(R.id.list_title)).setText(String.format(this.getString(R.string.tickects_changes_since), PrettyDate.hours(hours_timeback)));
	        getWindow().setTitle(getString(R.string.app_name) + " - " + getString(R.string.ticket_changes_recent));
		}
	}

	protected void updateResultsInUi() {
		super.updateResultsInUi();
        updateTitle();
	}

	protected void startLongRunningOperation() {

		if (currentThread != null) {
			currentThread.interrupt();
	        adapter.notifyDataSetChanged();
		}
		
		if (mode == TICKETS_LIST_RECENT) {
	    	final ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.loading_changes), true, true);
	    	
	        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
	        currentThread = new Thread() {
	            public void run() {
	            	dataRecent.clear();
	    			Calendar cal = Calendar.getInstance();
	    			boolean running = true;
	    			while (dataRecent.size() == 0 && running) {
						cal.add(Calendar.HOUR, -hours_timeback);
						dataRecent.addAll(TracDroid.server.getRecentTicketChanges( cal.getTime() ));
						if (TracDroid.server.isOnError()) {
							dialog.dismiss();
							TicketsActivity.this.finishActivity(Activity.RESULT_CANCELED);
						}
						mHandler.post(mUpdateResults);
						if (!TracDroid.server.isConnected(getApplicationContext())) {
							dialog.setTitle(getApplicationContext().getString(R.string.no_network));
							running = false;
						}
						else if (hours_timeback < 168) {
							hours_timeback += 48;
							dialog.setTitle(String.format(getApplicationContext().getString(R.string.loading_changes_since), PrettyDate.hours(hours_timeback)));
						}
	    			}
					dialog.dismiss();
	            }
	        };
		}
		else if (mode == TICKETS_LIST_QUERY) {
	    	final ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.loading_search_results), true, true);
	    	
	        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
	        currentThread = new Thread() {
	            public void run() {
	            	dataQuery.clear();
					dataQuery = TracDroid.server.ticketQuery(ticketQuery);
					if (TracDroid.server.isOnError()) {
						dialog.dismiss();
						TicketsActivity.this.finishActivity(Activity.RESULT_CANCELED);
					}
					mHandler.post(mUpdateResults);
					dialog.dismiss();
	            }
	        };
		}
        currentThread.start();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tickets_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.tickets_menu_new:
			Intent intent = new Intent().setClass(getApplicationContext(), TicketActivity.class);
			intent.putExtra("ticket_id", new Integer(0));
			startActivity(intent);
        	return true;
        case R.id.tickets_menu_settings:
        	startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public class TicketSearchResultsListAdapter extends EfficientAdapter {

		public TicketSearchResultsListAdapter(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

        @Override
        public long getItemId(int position) {
          // TODO Auto-generated method stub
          return 0;
        }

        @Override
        public int getCount() {
          // TODO Auto-generated method stub
          return dataQuery.size();
        }

        @Override
        public Object getItem(int position) {
          // TODO Auto-generated method stub
          return dataQuery.get(position);
        }

    	@Override
    	public android.widget.Filter getFilter() {
    		// TODO Auto-generated method stub
    		return null;
    	}

		@Override
		protected String getItemTextLine1(Integer position) {
			if (dataQuery.size() > position) {
				Ticket ticket = dataQuery.get(position);
				return ticket.id + " " + ticket.attributes.summary;
			}
			else return "";
		}

		@Override
		protected String getItemTextLine2(Integer position) {
			if (dataQuery.size() > position) {
				Ticket ticket = dataQuery.get(position);
				return ticket.attributes.description;
			}
			else return "";
		}
    }
    
    public class TicketChangesListAdapter extends EfficientAdapter {

		public TicketChangesListAdapter(Context context) {
			super(context);
		}

		@Override
		protected String getItemTextLine1(Integer position) {
			int mySize = dataRecent.size();
			if (mySize == 0) {
				return String.format(getApplicationContext().getString(R.string.no_changes_ask_load_more), PrettyDate.hours(hours_timeback));
			}
			if (mySize > position) {
				@SuppressWarnings("unchecked")
				HashMap<String, Object> ticket_change = dataRecent.get(position);
				TicketChange change = (TicketChange) ticket_change.get("change");
				return String.format(getApplicationContext().getString(R.string._by), ticket_change.get("id"), change.author);
			}
			else return getApplicationContext().getString(R.string.load_one_day_back);
		}

		@Override
		protected String getItemTextLine2(Integer position) {
			if (dataRecent.size() > position) {
	          @SuppressWarnings("unchecked")
			  HashMap<String, Object> ticket_change = dataRecent.get(position);
	          TicketChange change = (TicketChange) ticket_change.get("change");

	          // Show a quick description
	          if (change.newv.length() > 25 && change.field.equals("description")) {
	        	  if (ticket_change.get("excerpt") == null) {
	        		  ticket_change.put("excerpt", true);
					  dataRecent.set(position, ticket_change);
		        	  return change.field + ": " + change.newv.substring(0, 20) + " ...";
	        	  }
	        	  else if (ticket_change.get("excerpt").equals(true)) {
	        		  return change.field + ": " + change.newv.substring(0, 20) + " ...";
	        	  }
	          }
	          return change.field + ": " + change.newv;
			}
			else return "";
		}

        @Override
        public long getItemId(int position) {
          // TODO Auto-generated method stub
          return 0;
        }

        @Override
        public int getCount() {
          // TODO Auto-generated method stub
          return dataRecent.size() + 1;
        }

        @Override
        public Object getItem(int position) {
          // TODO Auto-generated method stub
          return dataRecent.get(position);
        }

    	@Override
    	public android.widget.Filter getFilter() {
    		// TODO Auto-generated method stub
    		return null;
    	}
    }

}
