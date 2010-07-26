package vincentlark.trac.tracdroid;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import vincentlark.trac.TicketChange;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

public class TicketsActivity extends ThreadedListActivity {

	static Vector<HashMap> data = new Vector<HashMap>();

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_list);

		TextView title = (TextView) findViewById(R.id.list_title);
		title.setText("Tickets changes today");
		
		ListView list = (ListView) this.findViewById(android.R.id.list);
		list.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// Lauch TicketActivity for that ticket
				if (data.size() < position) return;
				
				HashMap<String,Object> ticket = data.get(position);
				
				Intent intent = new Intent().setClass(getApplicationContext(), TicketActivity.class);
				intent.putExtra("ticket_id", (Integer) ticket.get("id"));
				startActivity(intent);
			}
		});
		
		adapter = new TicketListAdapter(getApplicationContext());
		setListAdapter(adapter);

		startLongRunningOperation();
	}

    protected void startLongRunningOperation() {

    	final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading recent ticket changes", true, true);
    	
        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
            public void run() {
            	Log.d("PROFILING", "start thread");
            	
            	Calendar cal = Calendar.getInstance();
				cal.add(Calendar.HOUR, -48);
				data = TracDroid.server.getRecentTicketChanges( cal.getTime() );
				mHandler.post(mUpdateResults);
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
			if (data.size() > 0) {
	          HashMap ticket_change = data.get(position);
	          TicketChange change = (TicketChange) ticket_change.get("change");
	          return "#"+ticket_change.get("id")+" by "+ change.author;
			}
			else return "No recent ticket changes";
		}

		@Override
		protected String getItemTextLine2(Integer position) {
			if (data.size() > 0) {
	          HashMap ticket_change = data.get(position);
	          TicketChange change = (TicketChange) ticket_change.get("change");
	          return ticket_change.get("oldvalue") + " => " + change.newv;
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
          return Math.max(data.size(), 1);
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
