package org.vl.trac.tracdroid;

import java.util.HashMap;

import org.vl.trac.tracdroid.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class WikiActivity extends Activity {

	int currentView = 0;
	private int switchsource_anim = R.anim.push_up_in;

	private TextView wikisource;
	private WebView webkit;
	private MenuItem wiki_menu_edit;
	private MenuItem wiki_menu_save;
	
	private String current_page;;
	private boolean wikisource_text_has_changed = false;
	
	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();

	// Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            updateResultsInUi();
        }
    };
	protected void updateResultsInUi() {
        // Back in the UI thread -- update our UI elements based on the data in mResults
		if (data.get("wiki") != null && data.get("html") != null) {
			
			wikisource.setText(data.get("wiki"));
			webkit.loadData(data.get("html"), "text/html", "UTF-8");
	
			wikisource_text_has_changed = false;
		}
	}

	HashMap<String,String> data;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wikiview);
        
	    Intent intent = getIntent();

	    wikisource = (TextView) findViewById(R.id.webview_text);
        webkit = (WebView) findViewById(R.id.webview_html);
        webkit.getSettings().setJavaScriptEnabled(true);
        
        wikisource.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				wikisource_text_has_changed = true;
				Log.d("WIKI", "Text has changed");
				return false;
			}
        });
        
        webkit.setWebViewClient(new WebViewClient() {
    	
        	public boolean shouldOverrideUrlLoading(WebView view, String url) {
        		this.redirectToWiki(url);
                return true;
            }
        	
			public void redirectToWiki(String url) {
				if (url.startsWith(TracDroid.server.domain)) {
					// The user switched to a page of the current trac
					String pagename = url.replaceFirst(TracDroid.server.domain+"/wiki/", "");
					changePage(pagename);
				}
	        }
	    });

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
	    	changePage(intent.getStringExtra("page_name"));
	    }
		else { 
			changePage(TracDroid.wikiStartPage);
		}
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case TracDroid.DIALOG_COMMIT_COMMENT:
        	LayoutInflater infalter = LayoutInflater.from(getApplicationContext());  
        	final View textEntryView = infalter.inflate(R.layout.commit_dialog, null);
        	AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

        	builder.setMessage("Comment your change")
                   .setCancelable(true)
                   .setView(textEntryView)
                   .setPositiveButton("Commit", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                    	  
                    	   EditText text_entry = (EditText) textEntryView.findViewById(R.id.commit_dialog_text);
                    	
                    	   HashMap<String,String> attrs = new HashMap<String,String>();
                    	   attrs.put("comment", text_entry.getText().toString());
                    	   boolean success = TracDroid.server.putPage(current_page, wikisource.getText().toString(), attrs);
                    	   if (success)
                    		   Toast.makeText(getApplicationContext(), String.format(getApplicationContext().getString(R.string.wiki_page_edited), current_page), Toast.LENGTH_SHORT).show();
                    	   else
                    		   Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.oops), Toast.LENGTH_SHORT).show();
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
    
    private void savePage() {
    	showDialog(TracDroid.DIALOG_COMMIT_COMMENT);
    }
    
    protected void changePage(final String pagename) {

    	final ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.loading_wiki_page), true, true);
    	
        // Fire off a thread to do some work that we shouldn't do directly in the UI thread
        Thread t = new Thread() {

            public void run() {
            	Log.d("PROFILING", "start thread");
            	
        		data = TracDroid.server.getPageComplete(pagename);
        		mHandler.post(mUpdateResults);
        		current_page = pagename;
        		dialog.dismiss();
            }
        };
        t.start();
    }
    
    private void switchSource() {
        ViewFlipper vf = (ViewFlipper) findViewById(R.id.details);
        vf.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), switchsource_anim));
        
    	if (currentView == 0) {
        	vf.showNext();
        	currentView = 1;
        	wiki_menu_edit.setTitle("View");
        }
        else {
        	vf.showPrevious();
        	currentView = 0;
        	wiki_menu_edit.setTitle("Edit");
        }
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        wiki_menu_save = menu.findItem(R.id.wiki_menu_save);
        
        if (wikisource_text_has_changed) Log.d("WIKI", "Text has changed, show save button");
        wiki_menu_save.setVisible(wikisource_text_has_changed);
        return true;
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wiki_menu, menu);
        wiki_menu_edit = menu.findItem(R.id.wiki_menu_edit);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.wiki_menu_edit:
            switchSource();
        	return true;
        case R.id.wiki_menu_save:
        	savePage();
            return true;
        case R.id.wiki_menu_settings:
        	startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
