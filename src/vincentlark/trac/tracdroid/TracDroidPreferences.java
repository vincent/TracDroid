package vincentlark.trac.tracdroid;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TracDroidPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private EditTextPreference domainPref;
    private EditTextPreference wikiStartPref;
    private EditTextPreference usernamePref;
    private ListPreference tracServerListPreference;
    
    private Vector<String> userDefinedServers = new Vector<String>();
    
    private SharedPreferences preferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);

	    preferences = TracDroid.preferences;
	    
	    tracServerListPreference = (ListPreference)getPreferenceScreen().findPreference("trac_server");
	    domainPref = (EditTextPreference)getPreferenceScreen().findPreference("domain");
	    usernamePref = (EditTextPreference)getPreferenceScreen().findPreference("username");
	    wikiStartPref = (EditTextPreference)getPreferenceScreen().findPreference("wiki_start_page");

	    // Add trac servers to the list
	    String[] tracServerArray = preferences.getString("trac_servers", "").split(";", -1);

	    // Remember user defined servers
	    for (int i = 0; i < tracServerArray.length; i++) {
	    	if (! tracServerArray[i].equals(""))
	    		userDefinedServers.add(tracServerArray[i].toString());
	    }
	    tracServerArray = userDefinedServers.toArray(new String[userDefinedServers.size()]);
	    
	    CharSequence[] trac_server_values = tracServerListPreference.getEntries();
		CharSequence[] trac_server_values_new = new CharSequence[trac_server_values.length + tracServerArray.length];
    	
		// Copy new values, then old values
		System.arraycopy(tracServerArray, 0, trac_server_values_new, 0, tracServerArray.length);
		System.arraycopy(trac_server_values, 0, trac_server_values_new, tracServerArray.length, trac_server_values.length);
		tracServerListPreference.setEntries(trac_server_values_new);
		tracServerListPreference.setEntryValues(trac_server_values_new);
	    
	}

    @Override
    protected void onResume() {
        super.onResume();

        
        /*
    	String trac_server = preferences.getString("trac_servers", "");

    	HashMap<String,String> tracServerEntries = new HashMap<String,String>();
    	tracServerEntries.put("new", "Create a new one");

    	Vector<String> tracServerEntries_keys = new Vector<String>();
    	
    	for (Object o:tracServerEntries.keySet()){
    		tracServerEntries_keys.add((String) o);
        }
    	*/
    	
        //SharedPreferences preferences__ = getPreferenceScreen().getSharedPreferences();
        
        // Setup the initial values
        domainPref.setSummary(preferences.getString("domain", ""));
        usernamePref.setSummary(preferences.getString("username", ""));
        wikiStartPref.setSummary(preferences.getString("wiki_start_page", ""));
        tracServerListPreference.setSummary(preferences.getString("trac_server", ""));

        // Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        savePreferences();
    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    	// Let's do something when a preference value changes
        if (key.equals("domain")) {
        	domainPref.setSummary(sharedPreferences.getString("domain", ""));
        }
        else if (key.equals("username")) {
        	usernamePref.setSummary(sharedPreferences.getString("username", ""));
        }
        else if (key.equals("trac_server")) {
            // A new server has been selected
            if (tracServerListPreference.getValue().equals("new")) {
        		showDialog(TracDroid.DIALOG_COMMIT_COMMENT);
        		CharSequence[] choices = tracServerListPreference.getEntries();
        		if (choices.length > 1) {
        			tracServerListPreference.setValueIndex(choices.length - 2);
        		}
        	}
            else {
            	tracServerListPreference.setSummary(sharedPreferences.getString("trac_server", ""));
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case TracDroid.DIALOG_COMMIT_COMMENT:
        	LayoutInflater infalter = LayoutInflater.from(getApplicationContext());
        	final View textEntryView = infalter.inflate(R.layout.commit_dialog, null);
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);

        	builder.setMessage("Name your new Trac server")
                   .setCancelable(true)
                   .setView(textEntryView)
                   .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	   EditText text_entry = (EditText) textEntryView.findViewById(R.id.commit_dialog_text);
                    	   String newTracServerName = text_entry.getText().toString();
                    	
                    	   if (newTracServerName.equals("")) {
                    		   
                    	   }
                    	   else if (addTracServer(newTracServerName))
                    		   Toast.makeText(getApplicationContext(), "Trac server \""+newTracServerName+"\" created", Toast.LENGTH_SHORT).show();
                    	   else
                    		   Toast.makeText(getApplicationContext(), "Oops, Trac server \""+newTracServerName+"\" already exists", Toast.LENGTH_SHORT).show();

                    	   // TODO: Ugly but force the list to be redisplayed
                    	   startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));
                       }
                   })
                   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                       }
                   });
            dialog = builder.create();
            break;
        default:
            dialog = null;
        }
        return dialog;
    }
    
    private void savePreferences() {
    	
	    // We need an Editor object to make preference changes.
	    // All objects are from android.context.Context
	    //SharedPreferences settings = TracDroid.preferences;
	    SharedPreferences.Editor editor = preferences.edit();
	      
	    editor.putString("trac_server", preferences.getString("trac_server", ""));
	    editor.putString("trac_servers", TracDroid.join(userDefinedServers, ";"));

	    editor.putString("domain", preferences.getString("domain", ""));
	    editor.putString("username", preferences.getString("username", ""));
	    editor.putString("password", preferences.getString("password", ""));
	    editor.putString("wiki_start_page", preferences.getString("wiki_start_page", "WikiStart"));

	    String hash;
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update( (preferences.getString("domain", "") + preferences.getString("username", "") + preferences.getString("password", "")).getBytes() );
			hash = digest.digest().toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			hash = (preferences.getString("domain", "") + preferences.getString("username", "") + preferences.getString("password", ""));
		}
	    
	    editor.putString("config_hash", hash);

	    // Commit the edits!
	    editor.commit();
    }

    
    private boolean addTracServer(String serverName) {
    	
    	serverName = serverName.replace(";", ",");
    	
    	// Test if name already exists
		if (userDefinedServers.contains(serverName))
			return false;
    	
    	userDefinedServers.add(serverName);
    	
	    //SharedPreferences settings = getSharedPreferences(TracDroid.PREFS_NAME, 0);
	    //SharedPreferences.Editor editor = TracDroid.preferences.edit();
	    //editor.commit();
    	
    	return true;
    }
}
