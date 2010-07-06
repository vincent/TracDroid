package vincentlark.trac.tracdroid;

import java.util.Vector;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TracDroid extends Activity {
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView tv = new TextView(this);
        tv.setText("Hello, Android");
        setContentView(tv);
        
        startActivity(new Intent(this, MethodsListActivity.class));
    }
    
    @SuppressWarnings("unchecked")
	public static String[] getMethods() {
    	Vector<String> methods = new Vector<String>();
        try {
        	XMLRPCClient client = new XMLRPCClient("https://trac.jamendo.com/tracjamendo", "vincent", "sanBar44");
        	methods = (Vector<String>) client.call("listMethods");
		} catch (XMLRPCException e) {
			// TODO Auto-generated catch block
			Log.e("error", "error", e);
			e.printStackTrace();
		}
    	
    	return (String[]) methods.toArray();
    }
	
}