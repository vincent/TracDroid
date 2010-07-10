package vincentlark.trac.tracdroid;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.TextView;

public class TicketsActivity extends ThreadedListActivity {

	static Vector<HashMap> data = new Vector<HashMap>();

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_list);

		TextView title = (TextView) findViewById(R.id.list_title);
		title.setText("Tickets changes today");
		
		adapter = new TicketListAdapter(getApplicationContext());
		setListAdapter(adapter);

		startLongRunningOperation();
	}

    protected void startLongRunningOperation() {

        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
            public void run() {
            	Log.d("PROFILING", "start thread");
            	
            	Calendar cal = Calendar.getInstance();
				cal.add(Calendar.HOUR, -48);
				data = TracDroid.server.getRecentTicketChanges( cal.getTime() );
				mHandler.post(mUpdateResults);
            }
        };
        t.start();
    }
    
    public class TicketListAdapter extends EfficientAdapter {

		public TicketListAdapter(Context context) {
			super(context);
		}

		@Override
		protected String getItemTextLine1(Integer position) {
	          HashMap ticket_change = data.get(position);
	          return "#"+ticket_change.get("id")+" by "+ticket_change.get("author");
		}

		@Override
		protected String getItemTextLine2(Integer position) {
	          HashMap ticket_change = data.get(position);
	          return ticket_change.get("oldvalue") + " => " + ticket_change.get("newvalue");
		}

        @Override
        public long getItemId(int position) {
          // TODO Auto-generated method stub
          return 0;
        }

        @Override
        public int getCount() {
          // TODO Auto-generated method stub
          return data.size();
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
