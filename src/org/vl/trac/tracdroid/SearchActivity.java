package org.vl.trac.tracdroid;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.vl.trac.SearchResult;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SearchActivity extends ThreadedListActivity implements OnItemClickListener {

	protected Vector<SearchResult> data;
	String queryString;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.simple_list);

	    Intent intent = getIntent();
	    data = new Vector<SearchResult>();

		adapter = new SearchListAdapter(getApplicationContext(), new int[] {});
		setListAdapter(adapter);
		this.getListView().setOnItemClickListener(this);
		
		((TextView) findViewById(R.id.list_title)).setVisibility(View.GONE);
		//setText(getString(R.string.search_trac));

		getWindow().setTitle(getString(R.string.app_name) + " - " + getString(R.string.search_trac)); 
				
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      doSearch(query);
	    }
	}
	
	protected void doSearch(String query) {
		queryString = query;
		startLongRunningOperation();
	}

	@Override
	protected void startLongRunningOperation() {
    	final ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.loading_search_results), "", true, true);

    	// Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {
			public void run() {
            	data = TracDroid.server.performSearch(queryString, null);
                mHandler.post(mUpdateResults);
                dialog.dismiss();
            }
        };
        t.start();
	}

	public class SearchListAdapter extends EfficientAdapter {

		public SearchListAdapter(Context context, int[] filters) {
			super(context);
		}

		@Override
		protected String getItemTextLine1(Integer position) {
			SearchResult res = data.get(position);
			return res.excerpt.length() < 30 ? res.excerpt : res.excerpt.substring(0, 28) + "...";
		}

		@Override
		protected String getItemTextLine2(Integer position) {
			SearchResult res = data.get(position);
			return res.author + "  " + PrettyDate.between(new Date(), res.time) + "\n" + res.href; 
		}

        @Override
        public long getItemId(int position) {
          // TODO Auto-generated method stub
          return 0;
        }
        
        @Override
        public int getCount() {
          if (data == null)
        	  return 0;
          else
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Uri url = Uri.parse(data.get(arg2).href);
		List<String> segments = url.getPathSegments();

		if (segments.size() > 1) {
			String lastSegment = segments.get(segments.size()-2);
			
			// Url links to a ticket
			if (lastSegment.equals("ticket")) {
				Intent intent = new Intent().setClass(getApplicationContext(), TicketActivity.class);
				intent.putExtra("ticket_id", Integer.parseInt(url.getLastPathSegment()));
				startActivity(intent);
				return;
			}

			// Url links to a wiki page
			else if (lastSegment.equals("wiki")) {
				Intent intent = new Intent(Intent.ACTION_VIEW).setClass(getApplicationContext(), WikiActivity.class);
				intent.putExtra("page_name", url.getLastPathSegment());
				startActivity(intent);
				return;
			}
		}
		
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(url);
		startActivity(i);
	}

}
