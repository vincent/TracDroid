package vincentlark.trac.tracdroid;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class RoadmapsActivity extends ThreadedListActivity {
	
	// Today
	private static Date today = new Date();

	static Vector<HashMap> cached_data = new Vector<HashMap>();
	static Vector<HashMap> data = new Vector<HashMap>();
	static String[] listFilters = {};
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_list);

		TextView title = (TextView) findViewById(R.id.list_title);
		title.setText("Open milestones");
		
		adapter = new RoadmapsListAdapter(getApplicationContext());
		setListAdapter(adapter);
		
		String[] defaultFilters = {"not-passed"};
		setFilters(defaultFilters);

		// Fill toolbar
		LinearLayout toolbar = (LinearLayout) findViewById(R.id.list_toolbar);
		Button button_new = (Button) View.inflate(getApplicationContext(), R.layout.button, null);
		button_new.setText(R.string.create_milestone);
		toolbar.addView(button_new);
		button_new.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent().setClass(getApplicationContext(), TicketActivity.class);
				intent.putExtra("milestone_id", new Integer(0));
				startActivity(intent);
			}
		});
		
		startLongRunningOperation();
    }

	protected void updateResultsInUi() {
        // Back in the UI thread -- update our UI elements based on the data in mResults
		data = new Vector<HashMap>();

		Iterator it = cached_data.iterator ();
		while (it.hasNext()) {
			HashMap ms = (HashMap) it.next();
			boolean add = true;
			for (int f=0; f < listFilters.length; f++) {
			
			
				if (listFilters[f].equals("not-passed"))
					add &= (ms.get("due") != null &&  ((Date) ms.get("due")).getTime() > today.getTime());


			}
			if (add) data.add(ms);
		}
        adapter.notifyDataSetChanged();
	}
	
	protected void setFilters(String[] filters) {
		listFilters = filters;
        mHandler.post(mUpdateResults);
	}
	
    protected void startLongRunningOperation() {

    	final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading roadmaps", true, true);
    	
        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
            @SuppressWarnings("unchecked")
			public void run() {
            	cached_data = data = TracDroid.server.listRoadmaps();
                mHandler.post(mUpdateResults);
                dialog.dismiss();
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
        case R.id.roadmaps_menu_home:
        	return true;
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
	          
			
			if (milestone.get("due") == null) {
				due = "No date set";
			}
			else {
				due = "due " + PrettyDate.between(today, (Date)milestone.get("due"));
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
