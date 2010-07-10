package vincentlark.trac.tracdroid;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;


public class TracDroid extends TabActivity {
	
	static TracServer server;

	static final int DIALOG_COMMIT_COMMENT = 0;
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TracDroid.server = new TracServer("https://trac.jamendo.com/tracjamendo/login/xmlrpc", "vincent",  "sanBar44");
        
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch WikiActivity
        intent = new Intent().setClass(getApplicationContext(), WikiActivity.class);
        spec = tabHost.newTabSpec("wiki").setIndicator("Wiki", res.getDrawable(R.drawable.ic_tab_wiki)).setContent(intent);
        tabHost.addTab(spec);

        // Create an Intent to launch RoadmapsActivity
        intent = new Intent().setClass(getApplicationContext(), RoadmapsActivity.class);
        spec = tabHost.newTabSpec("roadmaps").setIndicator("Roadmaps", res.getDrawable(R.drawable.ic_tab_roadmaps)).setContent(intent);
        tabHost.addTab(spec);

        // Create an Intent to launch TimelineActivity
        intent = new Intent().setClass(getApplicationContext(), TimelineActivity.class);
        spec = tabHost.newTabSpec("timeline").setIndicator("Timeline", res.getDrawable(R.drawable.ic_tab_timeline)).setContent(intent);
        tabHost.addTab(spec);

        // Create an Intent to launch TicketsActivity
        intent = new Intent().setClass(getApplicationContext(), TicketsActivity.class);
        spec = tabHost.newTabSpec("tickets").setIndicator("Tickets", res.getDrawable(R.drawable.ic_tab_tickets)).setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);

    }
    	
}