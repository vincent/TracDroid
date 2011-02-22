package org.vl.trac.tracdroid;

import java.util.HashMap;
import java.util.Vector;

import org.vl.trac.tracdroid.R;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.TextView;


public abstract class ThreadedListActivity extends ListActivity {

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();
	static Thread currentThread;

    // The data store
	protected Vector<HashMap> data = new Vector<HashMap>();

	// The List adapter
	protected EfficientAdapter adapter;

	// The long op
    protected abstract void startLongRunningOperation();

	protected void updateResultsInUi() {
        // Back in the UI thread -- update our UI elements based on the data in mResults
        adapter.notifyDataSetChanged();
	}

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TracDroid.tabHost.getCurrentTabView().setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				startLongRunningOperation();
				return false;
			}
        });
	}
	
	// Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateResultsInUi();
        }
    };

    public abstract class EfficientAdapter extends BaseAdapter implements Filterable {
        private LayoutInflater mInflater;
        private Context context;

        protected abstract String getItemTextLine1(Integer position);
        protected abstract String getItemTextLine2(Integer position);
        
        public EfficientAdapter(Context context) {
          // Cache the LayoutInflate to avoid asking for a new one each time.
          mInflater = LayoutInflater.from(context);
          this.context = context;
        }

        /**
         * Make a view to hold each row.
         * 
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(final int position, View convertView, ViewGroup parent) {
          // A ViewHolder keeps references to children views to avoid
          // unnecessary calls to findViewById() on each row.
          ViewHolder holder;

          // When convertView is not null, we can reuse it directly, there is
          // no need to reinflate it. We only inflate a new View when the convertView
          // supplied by ListView is null.
          if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_complex, null);

            // Creates a ViewHolder and store references to the two children
            // views we want to bind data to.
            holder = new ViewHolder();
            holder.textLine = (TextView) convertView.findViewById(R.id.list_complex_title);
            holder.textLine2 =(TextView) convertView.findViewById(R.id.list_complex_caption);
            
            convertView.setTag(holder);

          } else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder) convertView.getTag();
          }

          holder.textLine.setText( getItemTextLine1(position) );
          holder.textLine2.setText( getItemTextLine2(position) );
          holder.textLine2.setLinksClickable(true);
          
          return convertView;
        }

        class ViewHolder {
          TextView textLine;
          TextView textLine2;
        }
      }
    
}
