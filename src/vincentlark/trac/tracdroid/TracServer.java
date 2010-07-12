package vincentlark.trac.tracdroid;

import java.security.Security;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import android.util.Log;

import com.byarger.exchangeit.EasySSLSocketFactory;

public class TracServer {
	
	XMLRPCClient client;
	
	public String domain;
	public String username;
	public String password;
	public String wikiStartPage;

	public TracServer(String url, String username, String password) {
		Security.setProperty("ssl.SocketFactory.provider",EasySSLSocketFactory.class.getName());

		this.domain = url;
		this.username = username;
		this.password = password;
		
		Log.d("XML-RPC", "Contacting " + this.domain+"/login/xmlrpc");

		client = new XMLRPCClient(this.domain+"/login/xmlrpc");
    	client.setBasicAuthentication(this.username, this.password);
	}

	public String[] listMethods() {
    	Vector<String> methods = new Vector<String>();
        try {
        	Object[] methods_obj = (Object[]) client.call("system.listMethods");
        	for (int i = 0;  i < methods_obj.length;  i++) {
                if (methods_obj[i] != null) {
                	methods.add(methods_obj[i].toString());
                }
            }
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
		}
    	
    	return (String[]) methods.toArray(new String[methods.size()]);
    }
	
	public Vector listRoadmaps() {
    	Vector roadmaps = new Vector();
        try {
        	Vector<HashMap> method_signs = new Vector<HashMap>();

        	Object[] methods_obj = (Object[]) client.call("ticket.milestone.getAll");
        	for (int i = methods_obj.length-1;  i >= 0 ;  i--) {
                if (methods_obj[i] != null) {
                	//roadmaps.add(methods_obj[i].toString());

                	String[] params = new String[1];
                	params[0] = methods_obj[i].toString();

                	HashMap signature = new HashMap();
                	signature.put("methodName", "ticket.milestone.get");
                	signature.put("params", params);
                	method_signs.add(signature);
                }
            }

			Log.d("PROFILING", "multicall start");
			Object[] methods_results = (Object[]) client.call("system.multicall", method_signs.toArray());
			Log.d("PROFILING", "multicall end");

        	for (int i = 0;  i < methods_results.length;  i++) {
        		Object[] ticket_change_obj = (Object[]) methods_results[i];
        		
        		HashMap milestone = (HashMap) ticket_change_obj[0];
        		if (!(milestone.get("due") instanceof Date)) {
        			milestone.put("due", null);
        		}
        		roadmaps.add(milestone);
        	}
        	
        	//milestone['tickets'] = self.server.ticket.query("col=status&milestone=" + name)
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
		}
    	
		return roadmaps;
    }
	
	public String[] listTimelineItems() {
    	Vector<String> methods = new Vector<String>();
    	/*
        try {
        	Object[] methods_obj = (Object[]) client.call("ticket.milestone.getAll");
        	for (int i = 0;  i < methods_obj.length;  i++) {
                if (methods_obj[i] != null) {
                	methods.add(methods_obj[i].toString());
                }
            }
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
		}
		*/
    	
    	return (String[]) methods.toArray(new String[methods.size()]);
    }

	public Vector getRecentTicketChanges(Date since) {
		Log.d("PROFILING", "get tickets");
		Vector recent_changes = new Vector();
        try {

        	Vector<HashMap> method_signs = new Vector<HashMap>();
        	Object[] methods_obj = (Object[]) client.call("ticket.getRecentChanges", since);
        	Integer[] ticket_ids = new Integer[methods_obj.length];

        	for (int i = 0;  i < methods_obj.length;  i++) {
                if (methods_obj[i] != null) {

                	Integer[] params = new Integer[1];
                	params[0] = ticket_ids[i] = Integer.decode(methods_obj[i].toString());

                	HashMap signature = new HashMap();
                	signature.put("methodName", "ticket.changeLog");
                	signature.put("params", params);
                	method_signs.add(signature);
                }
            }

			Log.d("PROFILING", "multicall start");
			@SuppressWarnings("unused")
			Object[] methods_results = (Object[]) client.call("system.multicall", method_signs.toArray());
			Log.d("PROFILING", "multicall end");

        	for (int i = 0;  i < methods_results.length;  i++) {
        		HashMap ticket_change = new HashMap();
        		Object[] ticket_change_obj = (Object[]) methods_results[i];
        		ticket_change_obj = (Object[]) ticket_change_obj[0];
        		ticket_change_obj = (Object[]) ticket_change_obj[ ticket_change_obj.length - 1 ];
        		
        		// (time, author, field, oldvalue, newvalue, permanent)[]
        		ticket_change.put("id", ticket_ids[i]);
        		ticket_change.put("author", (String) ticket_change_obj[1]);
        		ticket_change.put("oldvalue", (String) ticket_change_obj[2]);
        		ticket_change.put("newvalue", (String) ticket_change_obj[3]);
        		
        		recent_changes.add(ticket_change);
        	}
			
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
		}
    	
    	//return (Integer[]) ticket_ids.toArray(new Integer[ticket_ids.size()]);
		return recent_changes;
    }
	
	public String getPageHTML(String pagename) {
		return getPageHTML(pagename, null);
	}
	public String getPageHTML(String pagename, Integer version) {
		Log.d("PROFILING", "get wiki.getPageHTML");
        try {
        	return (String) client.call("wiki.getPageHTML", pagename);
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
		}
		return null;
    }
	
	public HashMap<String,String> getPageComplete(String pagename) {
		return getPageComplete(pagename, null);
	}
	public HashMap<String,String> getPageComplete(String pagename, Integer version) {
		HashMap<String,String> result = new HashMap();
		Vector<HashMap> method_signs = new Vector<HashMap>();

		HashMap method = new HashMap();
		String[] params = new String[1];
		
		method.put("methodName", "wiki.getPage");
		params[0] = pagename;
		//params[1] = version;
		method.put("params", params);
		method_signs.add(method);
		
		method = new HashMap();
		params = new String[1];

		method.put("methodName", "wiki.getPageHTML");
		params[0] = pagename;
		//params[1] = version;
		method.put("params", params);
		method_signs.add(method);
		
		Log.d("PROFILING", "get wiki.getPageComplete");
        try {
    		Object[] methods_results = (Object[]) client.call("system.multicall", method_signs.toArray());
    		
    		Object[] method_result_wiki = (Object[]) methods_results[0];
    		Object[] method_result_html = (Object[]) methods_results[1];
    		
    		result.put("wiki", (String) method_result_wiki[0]);
    		result.put("html", (String) method_result_html[0]);
    		
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
		}

		return result;
	}
	
	public boolean putPage(String pagename, String content, HashMap attributes) {
		boolean success = false;
        try {
        	Object res = client.call("wiki.putPage", pagename, content, attributes);
        	success = (res != null);
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
		}
		return success;
	}
	
}
