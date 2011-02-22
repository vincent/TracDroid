package org.vl.trac.tracdroid;
import java.util.Date;
import java.util.Vector;

import org.vl.trac.Milestone;

import org.vl.trac.tracdroid.R;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class RoadmapsActivity extends ThreadedListActivity {
	
	// Today
	private static Date today = new Date();

	static Vector<Milestone> data = new Vector<Milestone>();
	static String[] listFilters = {};
	static String[] defaultFilters = {"not-passed"};
	static String[] noFilters = {"not-passed"};
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_list);

		TextView title = (TextView) findViewById(R.id.list_title);
		title.setText("Open milestones");
		
		// Fill toolbar
		LinearLayout toolbar = (LinearLayout) findViewById(R.id.list_toolbar);
		Button button_new = (Button) View.inflate(getApplicationContext(), R.layout.button, null);
		button_new.setText(R.string.create_milestone);
		toolbar.addView(button_new);
		button_new.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent().setClass(getApplicationContext(), MilestoneActivity.class);
				intent.putExtra("milestone_id", new Integer(0));
				startActivity(intent);
			}
		});
		
		adapter = new RoadmapsListAdapter(getApplicationContext(), new int[] {});
		setListAdapter(adapter);

		ListView list = (ListView) this.findViewById(android.R.id.list);
		list.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Milestone milestone = data.get(position);
				Intent intent = new Intent().setClass(getApplicationContext(), MilestoneActivity.class);
				intent.putExtra("milestone_name", milestone.name);
				startActivity(intent);
			}
		});
		
		startLongRunningOperation();
    }
	
	protected void updateResultsInUi() {
		super.updateResultsInUi();
        getWindow().setTitle(getString(R.string.app_name) + " - " + getString(R.string.milestones));
	}

    protected void startLongRunningOperation() {

    	final ProgressDialog dialog = ProgressDialog.show(this, "", "Loading roadmaps", true, true);
    	
        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
            @SuppressWarnings("unchecked")
			public void run() {
            	data = TracDroid.server.listRoadmaps();
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
        case R.id.roadmaps_menu_all:
        	return true;
        case R.id.roadmaps_menu_settings:
        	startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

	public class RoadmapsListAdapter extends EfficientAdapter {

		public RoadmapsListAdapter(Context context, int[] filters) {
			super(context);
		}

		@Override
		protected String getItemTextLine1(Integer position) {
			Milestone milestone = data.get(position);
			return (String) milestone.name;
		}

		@Override
		protected String getItemTextLine2(Integer position) {
			Milestone milestone = data.get(position);
			String due;
	          
			
			if (milestone.dateDue == null) {
				due = "No date set";
			}
			else {
				due = "due " + PrettyDate.between(today, (Date)milestone.dateDue);
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

    	public void setFilters(int[] filters) {
            mHandler.post(mUpdateResults);
    	}
    	
	}
}
