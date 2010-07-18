package vincentlark.trac.tracdroid;

import java.security.Security;
import java.util.ArrayList;
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
	
	public Vector<HashMap> listRoadmaps() {
    	Vector<HashMap> roadmaps = new Vector<HashMap>();
        try {
        	Vector<HashMap> method_signs = new Vector<HashMap>();

        	Object[] methods_obj = (Object[]) client.call("ticket.milestone.getAll");
        	for (int i = methods_obj.length-1;  i >= 0 ;  i--) {
                if (methods_obj[i] != null) {
                	//roadmaps.add(methods_obj[i].toString());

                	String[] params = new String[1];
                	params[0] = methods_obj[i].toString();

                	HashMap<String,Object> signature = new HashMap<String,Object>();
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
        		
        		HashMap<String,Object> milestone = (HashMap<String,Object>) ticket_change_obj[0];
        		if (!(milestone.get("due") instanceof Date)) {
        			milestone.put("due", null);
        		}
        		//else Log.d("MILESTONE DUE", milestone.get("name").toString() + " => " + milestone.get("due").toString());
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

	public Vector<HashMap> getRecentTicketChanges(Date since) {
		Log.d("PROFILING", "get tickets");
		Vector<HashMap> recent_changes = new Vector<HashMap>();
        try {

        	Vector<HashMap> method_signs = new Vector<HashMap>();
        	Object[] methods_obj = (Object[]) client.call("ticket.getRecentChanges", since);
        	Integer[] ticket_ids = new Integer[methods_obj.length];

        	for (int i = 0;  i < methods_obj.length;  i++) {
                if (methods_obj[i] != null) {

                	Integer[] params = new Integer[1];
                	params[0] = ticket_ids[i] = Integer.decode(methods_obj[i].toString());

                	HashMap<String,Object> signature = new HashMap<String,Object>();
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
        		HashMap<String,Object> ticket_change = new HashMap();
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
    	
		return recent_changes;
    }

	public HashMap<String,Object> getTicket(int ticket_id) {
		HashMap<String,Object> ticket = null;
		HashMap<String,Object> ticket_actions;
		
    	try {
    		// Fetch a ticket. Returns [id, time_created, time_changed, attributes]
			Object[] ticket_obj = (Object[]) client.call("ticket.get", ticket_id);

			if (ticket_obj.length > 0) {
				ticket = new HashMap<String,Object>();
				ticket.put("id", (Integer) ticket_obj[0]);
				ticket.put("time_created", (Date) ticket_obj[1]);
				ticket.put("time_changed", (Date) ticket_obj[2]);
				ticket.put("attributes", (HashMap<String,Object>) ticket_obj[3]);
			}
			
			Object[] actions = (Object[]) client.call("ticket.getActions", ticket_id);
			if (actions.length > 0) {
				ticket_actions = new HashMap<String,Object>();
				HashMap<String,Object> ticket_action = new HashMap<String,Object>();
				
				for (int i=0; i < actions.length; i++) {
					ticket_action.clear();
					
					Object[] action = (Object[]) actions[i];
					
					ticket_action.put("action", (String) action[0]);
					ticket_action.put("label", (String) action[1]);
					ticket_action.put("hints", (String) action[2]);
					
					HashMap<String,Object> inputFields = null;
					Object[] inputFields_obj = (Object[]) action[3];
					if (inputFields_obj.length > 0) {
						inputFields = new HashMap<String,Object>();
						HashMap<String,Object> inputField = new HashMap<String,Object>();

						for (int j=0; j < inputFields_obj.length; j++) {
							inputField.clear();
							Object[] inputField_obj = (Object[]) inputFields_obj[j];
							
							inputField.put("name", (String) inputField_obj[0]);
							inputField.put("value", (String) inputField_obj[1]);
							
							Vector<String> options = null;
							Object[] options_obj = (Object[]) inputField_obj[2];
							
							if (options_obj.length > 0) {
								options = new Vector<String>();
								for (int k=0; k < options_obj.length; k++)
									options.add((String) options_obj[k]);
							}
							inputField.put("options", options);

							inputFields.put((String) inputField.get("name"), inputField);
						}
						ticket_action.put("input_fields", inputFields);
					}
					ticket_actions.put((String) ticket_action.get("action"), ticket_action.clone());
				}
				ticket.put("actions", ticket_actions.clone());
			}

    	} catch (XMLRPCException e) {
			e.printStackTrace();
		}
		
    	return ticket;
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
		HashMap<String,String> result = new HashMap<String,String>();
		Vector<HashMap> method_signs = new Vector<HashMap>();

		HashMap<String,Object> method = new HashMap<String,Object>();
		String[] params = new String[1];
		
		method.put("methodName", "wiki.getPage");
		params[0] = pagename;
		//params[1] = version;
		method.put("params", params);
		method_signs.add(method);
		
		method = new HashMap<String,Object>();
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
	
	public boolean putPage(String pagename, String content, HashMap<String, String> attrs) {
		boolean success = false;
        try {
        	Object res = client.call("wiki.putPage", pagename, content, attrs);
        	success = (res != null);
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
		}
		return success;
	}
	
}
