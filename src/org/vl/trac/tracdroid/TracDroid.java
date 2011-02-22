package org.vl.trac.tracdroid;

import java.util.AbstractCollection;
import java.util.Iterator;

import org.vl.trac.tracdroid.R;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
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

	static int current_tab;

	static final int DIALOG_COMMIT_COMMENT = 42;

	public static final String PREFS_NAME = "TracDroidPreferences";

	public static TabHost tabHost;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load localized timespans
		String[] timespans = { 	getString(R.string._hours), getString(R.string._days), getString(R.string._weeks), getString(R.string._months), getString(R.string._years),
								getString(R.string._less_than_one_minute_ago), getString(R.string._in_less_than_one_minute),
								getString(R.string._seconds_ago), getString(R.string._minutes_ago), getString(R.string._hours_ago), 
								getString(R.string._days_ago), getString(R.string._weeks_ago), getString(R.string._months_ago), getString(R.string._years_ago),
								getString(R.string._in_seconds), getString(R.string._in_minutes), getString(R.string._in_hours),
								getString(R.string._in_days), getString(R.string._in_weeks), getString(R.string._in_months), getString(R.string._in_years) };
		PrettyDate.setLocalizedTimespan(timespans);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		if (preferences.getString("domain", "").equals("")) {
			// We don"t have any trac server configured
			startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));      	
		}
		else {

			String tracServerDomain = preferences.getString("domain", "");
			String tracServerUsername = preferences.getString("username", "");
			String tracServerPassword = preferences.getString("password", "");

			// Instanciate new Trac server
			TracDroid.server = new TracServer(tracServerDomain, tracServerUsername,  tracServerPassword);

			String tracServerWikiStartPage = preferences.getString("wiki_start_page", "");
			if (! tracServerDomain.equals(""))
				TracDroid.wikiStartPage = tracServerWikiStartPage;
			else
				TracDroid.wikiStartPage = "WikiStart";

			tabHost = getTabHost();  // The activity TabHost
			tabHost.clearAllTabs();
			
			tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
				@Override
				public void onTabChanged(String tabId) {
					TracDroid.current_tab = tabHost.getCurrentTab();
				}
			});

			Resources res = getResources(); // Resource object to get Drawables
			TabHost.TabSpec spec;  // Reusable TabSpec for each tab
			Intent intent;  // Reusable Intent for each tab

			// Create an Intent to launch WikiActivity
			intent = new Intent().setClass(getApplicationContext(), WikiActivity.class);
			spec = tabHost.newTabSpec("wiki").setIndicator(getString(R.string.tab_wiki), res.getDrawable(R.drawable.ic_tab_wiki)).setContent(intent);
			tabHost.addTab(spec);

			// Create an Intent to launch RoadmapsActivity
			intent = new Intent().setClass(getApplicationContext(), RoadmapsActivity.class);
			spec = tabHost.newTabSpec("roadmaps").setIndicator(getString(R.string.tab_roadmaps), res.getDrawable(R.drawable.ic_tab_roadmaps)).setContent(intent);
			tabHost.addTab(spec);

			/*
			 * Unimplemented
			 * 
	        // Create an Intent to launch TimelineActivity
	        intent = new Intent().setClass(getApplicationContext(), TimelineActivity.class);
	        spec = tabHost.newTabSpec("timeline").setIndicator("Timeline", res.getDrawable(R.drawable.ic_tab_timeline)).setContent(intent);
	        tabHost.addTab(spec);
			 */

			// Create an Intent to launch TicketsActivity
			intent = new Intent().setClass(getApplicationContext(), TicketsActivity.class);
			spec = tabHost.newTabSpec("tickets").setIndicator(getString(R.string.tab_tickets), res.getDrawable(R.drawable.ic_tab_tickets)).setContent(intent);
			tabHost.addTab(spec);

			/*
			intent = new Intent().setClass(getApplicationContext(), DiffActivity.class);
			intent.putExtra("leftText", "text1");
			intent.putExtra("rightText", "text2");
			startActivity(intent);
			*/

			/*
			intent = new Intent().setClass(getApplicationContext(), CameraActivity.class);
			startActivity(intent);
			*/

			tabHost.setCurrentTab(TracDroid.current_tab);
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