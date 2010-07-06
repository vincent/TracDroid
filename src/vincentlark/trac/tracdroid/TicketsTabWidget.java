package vincentlark.trac.tracdroid;

import android.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class TicketsTabWidget extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, TicketsActivity.class);

	    intent = new Intent().setClass(this, RoadmapActivity.class);
	    spec = tabHost.newTabSpec("albums").setIndicator("Rodmaps",
	                      res.getDrawable(R.drawable.ic_lock_silent_mode))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    spec = tabHost.newTabSpec("artists").setIndicator("Tickets",
                res.getDrawable(R.drawable.ic_delete))
            .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(1);
	}	
}
