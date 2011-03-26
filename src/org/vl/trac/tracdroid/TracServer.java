package org.vl.trac.tracdroid;

import java.security.Security;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.vl.trac.Milestone;
import org.vl.trac.MilestoneSummary;
import org.vl.trac.SearchResult;
import org.vl.trac.Ticket;
import org.vl.trac.TicketAction;
import org.vl.trac.TicketAttachement;
import org.vl.trac.TicketChange;
import org.vl.xmlrpc.CachedXMLRPCClient;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import com.byarger.exchangeit.EasySSLSocketFactory;

public class TracServer {
	
	XMLRPCClient client;
	
	public String domain;
	public String username;
	public String password;
	public String wikiStartPage;
	
	Exception error;

	/**
	 * TracServer constructor. Creates new instance based on server URI and credentials
	 * 
	 * @param XMLRPC server URI
	 * @param username to use
	 * @param password to use
	 */
	public TracServer(String url, String username, String password) {
		Security.setProperty("ssl.SocketFactory.provider",EasySSLSocketFactory.class.getName());
		//Security.setProperty("ssl.SocketFactory.provider",PlainSocketFactory.class.getName());

		this.domain = url;
		this.username = username;
		this.password = password;
		
		Log.d("XML-RPC", "Contacting " + this.domain+"/login/xmlrpc");

		client = new CachedXMLRPCClient(this.domain+"/login/xmlrpc");
    	client.setBasicAuthentication(this.username, this.password);
	}

	/*
	protected Object cachedCall(String methodName, Object o1) {
		String cacheid = methodName + o1.hashCode();
		if (!cache.have(cacheid) && isConnected()) {
			Object data = client.call(method);
			cache.put(cacheid, data);
			return data;
		}
		else {
			return cache.get(cacheid);
		}
	}
	*/

	/**
	 * Does the last call produce an error ?
	 */
	public boolean isOnError() {
		return (error != null);
	}
	public Exception lastError() {
		return error;
	}
	
	/**
	 * Convenience function to know if the device is connected or not
	 * 
	 * @param application context
	 * @return true if connected
	 */
	public boolean isConnected(Context context) {
		ConnectivityManager service = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = service.getActiveNetworkInfo();
		return (activeNetwork==null ? false : activeNetwork.isConnected());
	}
	
	/**
	 * Convenience function to know if the device is roaming or not
	 * 
	 * @param application context
	 * @return true if roaming
	 */
	public boolean isRoaming(Context context) {
		ConnectivityManager service = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = service.getActiveNetworkInfo(); 
		return (activeNetwork==null ? false : activeNetwork.isRoaming());
	}
	
	/**
	 * Convenience function to get a string list result
	 * 
	 * @param methodName name of method to call
	 * @return strings vector
	 */
	public Vector<String> simpleListCall(String methodName) {
		Vector<String> results = new Vector<String>();
    	Object[] methods_obj;
		try {
			error = null;
			methods_obj = (Object[]) client.call(methodName);

			for (int i = methods_obj.length-1;  i >= 0 ;  i--) {
	            if (methods_obj[i] != null) {
	            	results.add(methods_obj[i].toString());
	            }
	    	}
		} catch (XMLRPCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = e;
		}
		return results;
	}
	
	/**
	 * Convenience function to get a list of available methods
	 * 
	 * @return strings vector
	 */
	public Vector<String> listMethods() {
		return simpleListCall("system.listMethods");
    }

	/**
	 * List all ticket types
	 * 
	 * @return ticket types as a string vector
	 */
	public Vector<String> listTicketTypes() {
		return simpleListCall("ticket.type.getAll");
	}
	
	/**
	 * List all components
	 * 
	 * @return ticket types as a string vector
	 */
	public Vector<String> listTicketComponents() {
		return simpleListCall("ticket.component.getAll");
	}
	
	/**
	 * List all priorities
	 * 
	 * @return ticket types as a string vector
	 */
	public Vector<String> listTicketPriorities() {
		return simpleListCall("ticket.priority.getAll");
	}
	
	/**
	 * List all milestones names
	 * 
	 * @return milestones names as a string vector
	 */
	public Vector<String> listMilestones() {
		return simpleListCall("ticket.milestone.getAll");
	}
	
	/**
	 * List all milestones, with attributes
	 * 
	 * @return milestones names as a HashMap vector
	 */
	public Vector<Milestone> listRoadmaps() {
    	Vector<Milestone> roadmaps = new Vector<Milestone>();
        try {
			error = null;
        	Vector<HashMap> method_signs = new Vector<HashMap>();

        	Object[] methods_obj = (Object[]) client.call("ticket.milestone.getAll");
        	for (int i = methods_obj.length-1;  i >= 0 ;  i--) {
                if (methods_obj[i] != null) {
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
        		Object[] milestone_obj = (Object[]) methods_results[i];
        		roadmaps.add(Milestone.fromXMLRPC((HashMap) milestone_obj[0]));
        	}
        	
        	//milestone['tickets'] = self.server.ticket.query("col=status&milestone=" + name)
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
						e.printStackTrace();
			error = e;
		}
    	
		return roadmaps;
    }
	
	
	public Milestone getMilestone(String name) {
		Milestone milestone = null;
		try {
			error = null;
			HashMap milestone_obj = (HashMap) client.call("ticket.milestone.get", name);

			if (milestone_obj.size() > 0) {
				milestone = Milestone.fromXMLRPC((HashMap) milestone_obj);
			}
			
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return milestone;
	}
	
	public int updateMilestone(String name, HashMap<String,String> changes) {
		int res = -1;
		try {
			error = null;
			res = ((Integer) client.call("ticket.milestone.update", name, changes)).intValue();

		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return res;
	}
	
	public int createMilestone(String name, HashMap<String,String> changes) {
		int res = -1;
		try {
			error = null;
			res = ((Integer) client.call("ticket.milestone.create", name, changes)).intValue();

		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return res;
	}
	
	public String[] listTimelineItems() {
    	Vector<String> methods = new Vector<String>();
    	/*
        try {
			error = null;
        	Object[] methods_obj = (Object[]) client.call("ticket.milestone.getAll");
        	for (int i = 0;  i < methods_obj.length;  i++) {
                if (methods_obj[i] != null) {
                	methods.add(methods_obj[i].toString());
                }
            }
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		*/
    	
    	return (String[]) methods.toArray(new String[methods.size()]);
    }

	/**
	 * List recent tickets changes, since a specified date
	 * 
	 * @param since the date since when you want changes
	 * @return ticket changes as a HashMap (int ticket_id, TicketChange change) vector
	 */
	public Vector<HashMap> getRecentTicketChanges(Date since) {
		Log.d("PROFILING", "get tickets");
		Vector<HashMap> recent_changes = new Vector<HashMap>();
        try {
			error = null;

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
			Object[] methods_results = (Object[]) client.call("system.multicall", method_signs.toArray());
			Log.d("PROFILING", "multicall end");

        	for (int i = 0;  i < methods_results.length;  i++) {
        		HashMap<String,Object> ticket_change = new HashMap<String,Object>();
        		Object[] ticket_change_obj = (Object[]) methods_results[i];
        		ticket_change_obj = (Object[]) ticket_change_obj[0];
        		
        		if (ticket_change_obj.length == 0) continue;
        		
        		// Get the last change
        		ticket_change_obj = (Object[]) ticket_change_obj[ ticket_change_obj.length - 1 ];
        		
        		ticket_change.put("id", ticket_ids[i]);
        		ticket_change.put("change", TicketChange.fromXMLRPC(ticket_change_obj));
        		
        		recent_changes.add(ticket_change);
        	}
			
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
    	
		return recent_changes;
    }

	/**
	 * Get a ticket
	 * 
	 * @param ticket_id id of the desired ticket
	 * @return ticket data
	 */
	public Ticket getTicket(int ticket_id) {
		return getTicket(ticket_id, false);
	}
	
	public Ticket getTicket(int ticket_id, boolean withData) {
		Ticket ticket = null;
		HashMap<String, TicketAction> ticket_actions;
	
    	try {
			error = null;
    		
        	// Fetch a ticket. Returns [id, time_created, time_changed, attributes]
			Object[] ticket_obj = (Object[]) client.call("ticket.get", ticket_id);

			if (ticket_obj.length > 0) {
				ticket = Ticket.fromXMLRPC_ticket_get(ticket_obj);
			
				if (withData) {
					Object[] changlelog_objs = (Object[]) client.call("ticket.changeLog", ticket_id);
					Vector<TicketChange> changelog = new Vector<TicketChange>();
		        	for (int i = 0;  i < changlelog_objs.length;  i++) {
		        		changelog.add(TicketChange.fromXMLRPC((Object[]) changlelog_objs[i]));
		        	}
		        	ticket.setChangeLog(changelog);
		
					Object[] actions = (Object[]) client.call("ticket.getActions", ticket_id);
					if (actions.length > 0) {
						ticket_actions = new HashMap<String,TicketAction>();
						
						for (int i=0; i < actions.length; i++) {
							TicketAction ticket_action = TicketAction.fromXMLRPC((Object[]) actions[i]);
							ticket_actions.put((String) ticket_action.action, ticket_action);
						}
						ticket.setActions(ticket_actions);
					}
				}
			}

    	} catch (XMLRPCException e) {
			e.printStackTrace();
			error = e;
		}
		
    	return ticket;
	}

	
	/**
	 * Get a ticket, and all infos, in one call
	 * 
	 * @param ticket_id id of the desired ticket
	 * @return ticket data
	 */
	public Ticket getTicketOneShot(int ticket_id) {
		Ticket ticket = null;
		HashMap<String, TicketAction> ticket_actions;
    	Vector<HashMap> method_signs = new Vector<HashMap>();
    	HashMap<String,Object> signature = new HashMap<String,Object>();
    	
    	signature = new HashMap<String,Object>();
    	Object[] params1 = new Object[1];
    	params1[0] = ticket_id;
    	signature.put("methodName", "ticket.get");
    	signature.put("params", params1);
    	method_signs.add(signature);
    	
    	signature = new HashMap<String,Object>();
    	Object[] params2 = new Object[1];
    	params2[0] = ticket_id;
    	signature.put("methodName", "ticket.changeLog");
    	signature.put("params", params2);
    	method_signs.add(signature);
    	
    	signature = new HashMap<String,Object>();
    	Object[] params3 = new Object[1];
    	params3[0] = ticket_id;
    	signature.put("methodName", "ticket.getActions");
    	signature.put("params", params3);
    	method_signs.add(signature);
    	
    	
    	try {
			error = null;
    		
			Object[] methods_results = (Object[]) client.call("system.multicall", method_signs.toArray());
			
			Object[] _methodResult = (Object[]) methods_results[0];
			Object[] methodResult = (Object[]) _methodResult[0];
			if (methodResult.length > 0) {
				ticket = Ticket.fromXMLRPC_ticket_get(methodResult);
			
				_methodResult = (Object[]) methods_results[1];
				methodResult = (Object[]) _methodResult[0];
				Vector<TicketChange> changelog = new Vector<TicketChange>();
	        	for (int i = 0;  i < methodResult.length;  i++) {
	        		changelog.add(TicketChange.fromXMLRPC((Object[]) methodResult[i]));
	        	}
	        	ticket.setChangeLog(changelog);
	
				_methodResult = (Object[]) methods_results[2];
				methodResult = (Object[]) _methodResult[0];
				if (methodResult.length > 0) {
					ticket_actions = new HashMap<String,TicketAction>();
					
					for (int i=0; i < methodResult.length; i++) {
						TicketAction ticket_action = TicketAction.fromXMLRPC((Object[]) methodResult[i]);
						ticket_actions.put((String) ticket_action.action, ticket_action);
					}
					ticket.setActions(ticket_actions);
				}
			}

    	} catch (XMLRPCException e) {
			e.printStackTrace();
			error = e;
		}
		
    	return ticket;
	}

	
	/**
	 * Update a ticket with specified attributes
	 * 
	 * @param ticket_id id of the ticket to update
	 * @param attributes HashMap of attributes to update
	 * @return modified ticket
	 */
	public Ticket updateTicket(int ticket_id, String comment, HashMap<String,String> attributes) {
        try {
			error = null;
        	Object[] ticket_obj = (Object[]) client.call("ticket.update", ticket_id, comment, attributes);
        	return Ticket.fromXMLRPC_ticket_get(ticket_obj);
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		
		return null;
	}
	
	/**
	 * Create a ticket with specified attributes
	 * 
	 * @param summary ticket title
	 * @param description ticket description
	 * @param attributes HashMap of attributes
	 * @param notify notify
	 * @return id of the created ticket
	 */
	public int createTicket(String summary, String description, String comment, HashMap<String,String> attributes, boolean notify) {
        try {
			error = null;
        	Object ticket_id_obj = client.call("ticket.create", summary, comment, attributes, notify);
        	return Integer.parseInt(String.valueOf(ticket_id_obj));
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		
		return 0;
	}

	/**
	 * Get HTML code from a wiki text
	 * 
	 * @param text wiki text
	 * @return generated HTML code
	 */
	public String wikiToHtml(String text) {
        try {
			error = null;
        	return (String) client.call("wiki.wikiToHtml", text);
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return text;
	}
	
	/**
	 * Get wiki page text
	 * 
	 * @param pagename name of the wiki page
	 * @return page text
	 */
	public String getPageHTML(String pagename) {
		return getPageHTML(pagename, null);
	}

	/**
	 * Get wiki page text, for a particular version
	 * 
	 * @param pagename name of the wiki page
	 * @param version desired version
	 * @return page text
	 */
	public String getPageHTML(String pagename, Integer version) {
		Log.d("PROFILING", "get wiki.getPageHTML");
        try {
			error = null;
        	return (String) client.call("wiki.getPageHTML", pagename);
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return null;
    }
	
	/**
	 * Get complete wiki page
	 * 
	 * @param pagename name of the wiki page
	 * @return page attributes
	 */
	public HashMap<String,String> getPageComplete(String pagename) {
		return getPageComplete(pagename, null);
	}

	/**
	 * Get complete wiki page
	 * 
	 * @param pagename name of the wiki page
	 * @param version desired version
	 * @return page attributes
	 */
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
			error = null;
    		Object[] methods_results = (Object[]) client.call("system.multicall", method_signs.toArray());
    		
    		Object[] method_result_wiki = (Object[]) methods_results[0];
    		Object[] method_result_html = (Object[]) methods_results[1];
    		
    		result.put("wiki", (String) method_result_wiki[0]);
    		result.put("html", (String) method_result_html[0]);
    		
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}

		return result;
	}
	
	/**
	 * Store a new version of a wiki page 
	 * 
	 * @param pagename name of the wiki page
	 * @param content content of the wiki page
	 * @param attrs attributes of the wiki page
	 * @return true when succeed
	 */
	public boolean putPage(String pagename, String content, HashMap<String, String> attrs) {
		boolean success = false;
        try {
			error = null;
        	Object res = client.call("wiki.putPage", pagename, content, attrs);
        	success = (res != null);
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return success;
	}

	/**
	 * List attachements of a ticket 
	 * 
	 * @param ticket_id id of the ticket
	 * @return vector of TicketAttachement
	 */
	public Vector<TicketAttachement> listAttachments(int ticket_id) {
		Vector<TicketAttachement> attachements = new Vector<TicketAttachement>();
        try {
			error = null;
        	Object[] res = (Object[]) client.call("ticket.listAttachments", ticket_id);

			if (res.length > 0) {
				for (int i = 0; i < res.length; i++) {
					attachements.add(TicketAttachement.fromXMLRPC((Object[]) res[i]));
				}
			}

		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return attachements;
	}

	/**
	 * Get an attachement data 
	 * 
	 * @param ticket_id id of the ticket
	 * @param filename name of the attachement
	 * @return attachement data as a byte[]
	 */
	public byte[] getAttachment(int ticket_id, String filename) {
        try {
			error = null;
        	return Base64.decode((byte[]) client.call("ticket.getAttachment", ticket_id, filename), Base64.DEFAULT);
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return null;
	}

	/**
	 * Attach a file to a ticket 
	 * 
	 * @param ticket_id id of the ticket
	 * @param filename name of the attachement
	 * @param description description of the attachement
	 * @param base64Data attachement data as a byte[]
	 * @return created attachement filename
	 */
	public String putAttachment(int ticket_id, String filename, String description, byte[] base64Data) {
		return putAttachment(ticket_id, filename, description, base64Data, true);
	}
	
	/**
	 * Attach a file to a ticket 
	 * 
	 * @param ticket_id id of the ticket
	 * @param filename name of the attachement
	 * @param description description of the attachement
	 * @param base64Data attachement data as a byte[]
	 * @param replace replace existing attachement or not
	 * @return created attachement filename
	 */
	public String putAttachment(int ticket_id, String filename, String description, byte[] base64Data, boolean replace) {
        try {
			error = null;
        	return (String) client.call("ticket.putAttachment", ticket_id, filename, description, base64Data, replace);
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return null;
	}
	
	/**
	 * Summarize ticket query
	 * 
	 * Returns counts of status, owners, components, types
	 * 
	 * @param Vector<Ticket> collection A ticket search result
	 * @return HashMap<String,Integer> result
	 */
	public MilestoneSummary summarizeTicketQuery(Vector<Ticket> collection) {
		MilestoneSummary result = new MilestoneSummary();

		HashMap<String,Integer> components = new HashMap<String,Integer>();
		HashMap<String,Integer> statuses = new HashMap<String,Integer>();
		HashMap<String,Integer> owners = new HashMap<String,Integer>();
		HashMap<String,Integer> types = new HashMap<String,Integer>();

		Ticket t;
		for (int i=0; i < collection.size(); i++) {
			t = collection.get(i);
			if (t.attributes.status != null)
				statuses.put(t.attributes.status, statuses.containsKey(t.attributes.status) ? statuses.get(t.attributes.status) + 1 : 1);
			if (t.attributes.owner != null)
				owners.put(t.attributes.owner, owners.containsKey(t.attributes.owner) ? owners.get(t.attributes.owner) + 1 : 1);
			if (t.attributes.component != null)
				components.put(t.attributes.component, components.containsKey(t.attributes.component) ? components.get(t.attributes.component) + 1 : 1);
			if (t.attributes.type != null)
				types.put(t.attributes.type, types.containsKey(t.attributes.type) ? types.get(t.attributes.type) + 1 : 1);
		}
			
		result.put("components", components);
		result.put("statuses", statuses);
		result.put("owners", owners);
		result.put("types", types);

		return result;
	}
	
	/**
	 * Perform ticket search
	 * 
	 * ticket.query(string qstr="status!=closed")
	 */
	public Vector<Ticket> ticketQuery(String query) {
		Vector<Ticket> results = new Vector<Ticket>();
        try {
			error = null;
        	Object[] ticket_ids = (Object[]) client.call("ticket.query", query);

			for (int i=0; i < ticket_ids.length; i++) {
				results.add(getTicket(((Integer) ticket_ids[i]).intValue()));
			}
			return results;
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return null;
	}
	
	/**
	 * Perform global search 
	 * (href, title, date, author, excerpt)
	 * 
	 * @param query Search query
	 * @return vector of results
	 */
	public Vector<SearchResult> performSearch(String query, String[] filters) {
		Vector<SearchResult> results = new Vector<SearchResult>();
        try {
			error = null;
        	Object[] res = (Object[]) client.call("search.performSearch", query, new String[] { "ticket", "changeset", "milestone", "wiki", "" });

			for (int i=0; i < res.length; i++) {
				results.add(SearchResult.fromXMLRPC((Object[]) res[i]));
			}
			return results;
        	
		} catch (XMLRPCException e) {
			Log.e("error", "error", e);
			e.printStackTrace();
			error = e;
		}
		return null;
	}
	
	/*
	array search.getSearchFilters()
	array search.performSearch(string query, array filters=None)
	*/
}
