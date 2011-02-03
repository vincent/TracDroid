package vincentlark.trac.tracdroid;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import vincentlark.trac.TicketChange;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TicketsActivity extends ThreadedListActivity {

	static Vector<HashMap> data = new Vector<HashMap>();
	int hours_timeback = 48;

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
		
		
		updateTitle();
		
		ListView list = (ListView) this.findViewById(android.R.id.list);
		list.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				int mySize = data.size();
				if (mySize <= position) {
					hours_timeback += 24;
					startLongRunningOperation();
					return;
				}
				
				HashMap<String,Object> ticket = data.get(position);
				
				if (ticket.get("excerpt") != null && ticket.get("excerpt").equals(true)) {
					// Show a big description
					ticket.put("excerpt", false);
					data.set(position, ticket);
					adapter.notifyDataSetChanged();
				}
				else {
					// Lauch TicketActivity for that ticket
					Intent intent = new Intent().setClass(getApplicationContext(), TicketActivity.class);
					intent.putExtra("ticket_id", (Integer) ticket.get("id"));
					startActivity(intent);
				}
			}
		});
		
		adapter = new TicketListAdapter(getApplicationContext());
		setListAdapter(adapter);

		startLongRunningOperation();
	}
	
	protected void updateTitle() {
		((TextView) findViewById(R.id.list_title)).setText(String.format(this.getString(R.string.tickects_changes_since), PrettyDate.hours(hours_timeback)));
	}

	protected void updateResultsInUi() {
        // Back in the UI thread -- update our UI elements based on the data in mResults
        adapter.notifyDataSetChanged();
        updateTitle();
	}

	protected void startLongRunningOperation() {

    	final ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.loading_changes), true, true);
    	
        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
            public void run() {
            	data.clear();
    			Calendar cal = Calendar.getInstance();
    			boolean running = true;
    			while (data.size() == 0 && running) {
					cal.add(Calendar.HOUR, -hours_timeback);
					data = TracDroid.server.getRecentTicketChanges( cal.getTime() );
					if (data.size() > 0) {
						mHandler.post(mUpdateResults);
					}
					else if (!TracDroid.server.isConnected(getApplicationContext())) {
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
        t.start();
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

    public class TicketListAdapter extends EfficientAdapter {

		public TicketListAdapter(Context context) {
			super(context);
		}

		@Override
		protected String getItemTextLine1(Integer position) {
			int mySize = data.size();
			if (mySize == 0) {
				return String.format(getApplicationContext().getString(R.string.no_changes_ask_load_more), PrettyDate.hours(hours_timeback));
			}
			if (mySize > position) {
	          HashMap<String, Object> ticket_change = data.get(position);
	          TicketChange change = (TicketChange) ticket_change.get("change");
	          return String.format(getApplicationContext().getString(R.string._by), ticket_change.get("id"), change.author);
			}
			else return getApplicationContext().getString(R.string.load_one_day_back);
		}

		@Override
		protected String getItemTextLine2(Integer position) {
			if (data.size() > position) {
	          HashMap<String, Object> ticket_change = data.get(position);
	          TicketChange change = (TicketChange) ticket_change.get("change");

	          // Show a quick description
	          if (change.newv.length() > 25 && change.field.equals("description")) {
	        	  if (ticket_change.get("excerpt") == null) {
	        		  ticket_change.put("excerpt", true);
					  data.set(position, ticket_change);
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
          return data.size() + 1;
        }

        @Override
        public Object getItem(int position) {
          // TODO Auto-generated method stub
          return data.get(position);
        }

    	@Override
    	public android.widget.Filter getFilter() {
    		// TODO Auto-generated method stub
    		return null;
    	}
    }

}
