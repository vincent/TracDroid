package org.vl.trac;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MilestoneSummary extends HashMap<String,HashMap<String,Integer>> {

	public static String ALL = "all";
	public static String BY_TYPE = "types";
	public static String BY_OWNER = "owners";
	public static String BY_STATUS = "statuses";
	public static String BY_COMPONENT = "components";
	
	public String[] countsStringBy(String attr) {
		HashMap<String,Integer> attrs = get(attr);
		String[] strings = new String[attrs.size()];

		String a;
		int i=0;
		Iterator<String> it = attrs.keySet().iterator();
		while (it.hasNext()) {
			a = it.next();
			strings[i++] = String.format("%s %s", attrs.get(a).toString(), a);
		}
		return strings;
	}

	public List<Map<String,String>> listForAll() {
		LinkedList<Map<String, String>> list = new LinkedList<Map<String,String>>();
		HashMap<String,String> count = new HashMap<String,String>();
		count.put("title", String.valueOf(size()));
		count.put("caption", ALL);
		list.add(count);
		return list;
	}
	
	public List<Map<String,String>> countsListBy(String attr) {
		LinkedList<Map<String, String>> list = new LinkedList<Map<String,String>>();
		HashMap<String,Integer> attrs = get(attr);
		HashMap<String,String> count;
		String a;
		
		Iterator<String> it = attrs.keySet().iterator();
		while (it.hasNext()) {
			a = it.next();
			count = new HashMap<String,String>();
			count.put("title", attrs.get(a).toString());
			count.put("caption", a);
			list.add(count);
		}
		return list;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
