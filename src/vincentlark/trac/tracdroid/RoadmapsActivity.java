package vincentlark.trac.tracdroid;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import vincentlark.trac.tracdroid.ThreadedListActivity.EfficientAdapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.TextView;


public class RoadmapsActivity extends ThreadedListActivity {
	
	// Today
	private static Date today = new Date();
	static Vector<HashMap> data = new Vector<HashMap>();
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_list);

		TextView title = (TextView) findViewById(R.id.list_title);
		title.setText("Open milestones");
		
		adapter = new RoadmapsListAdapter(getApplicationContext());
		setListAdapter(adapter);
		
		startLongRunningOperation();
    }

    protected void startLongRunningOperation() {

        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
            public void run() {
            	data = TracDroid.server.listRoadmaps();
                mHandler.post(mUpdateResults);
            }
        };
        t.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.roadmaps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.roadmaps_menu_settings:
        	startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	public class RoadmapsListAdapter extends EfficientAdapter {

		public RoadmapsListAdapter(Context context) {
			super(context);
		}

		@Override
		protected String getItemTextLine1(Integer position) {
			HashMap milestone = data.get(position);
			return (String) milestone.get("name");
		}

		@Override
		protected String getItemTextLine2(Integer position) {
			HashMap milestone = data.get(position);
			String due;
	          
			Log.d("DATE", (String) milestone.get("name"));
			if (milestone.get("due") == null) {
				due = "No date set";
			}
			else {
				due = "due " + PrettyDateDiff.between(today, (Date)milestone.get("due"));
			}
			return due;
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
