package vincentlark.trac.tracdroid;

import java.util.AbstractCollection;
import java.util.Iterator;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TabHost;


public class TracDroid extends TabActivity {
	
	static TracServer server;

	static String username;
	static String password;
	static String wikiStartPage;
	
	static SharedPreferences preferences;

	static final int DIALOG_COMMIT_COMMENT = 42;
	
	public static final String PREFS_NAME = "TracDroidPreferences";

	
	private TabHost tabHost;
	private String lastUsedConfigHash;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//getSharedPreferences(TracDroid.PREFS_NAME, 0);
        
        Log.d("TRAC", "Current trac domain is "+preferences.getString("domain", ""));
    }
    
    public void onResume() {
    	super.onResume();
    	
        if (preferences.getString("domain", "").equals("")) {
        	// We don"t have any trac server configured
        	startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));      	
        }
        else if (! preferences.getString("domain", "").equals(lastUsedConfigHash)) {
           lastUsedConfigHash = preferences.getString("config_hash", "");
        	   
 	       String tracServerDomain = preferences.getString("domain", "");
	       String tracServerUsername = preferences.getString("username", "");
	       String tracServerPassword = preferences.getString("password", "");
	       if (! tracServerDomain.equals("")) {
	    	   TracDroid.server = new TracServer(tracServerDomain, tracServerUsername,  tracServerPassword);
	       }

	       String tracServerWikiStartPage = preferences.getString("wiki_start_page", "");
	       if (! tracServerDomain.equals(""))
	    	   TracDroid.wikiStartPage = tracServerWikiStartPage;
	       else
	    	   TracDroid.wikiStartPage = "WikiStart";

	       if (tabHost == null)
	    	   tabHost = getTabHost();  // The activity TabHost
	       else
	    	   tabHost.clearAllTabs();
	       
	        Resources res = getResources(); // Resource object to get Drawables
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
    
    public static String join(AbstractCollection<String> s, String delimiter) {
        if (s.isEmpty()) return "";
        Iterator<String> iter = s.iterator();
        StringBuffer buffer = new StringBuffer(iter.next());
        while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
        return buffer.toString();
    }
    
}