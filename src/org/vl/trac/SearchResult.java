package org.vl.trac;

import java.util.Date;
import java.util.Map;

import org.vl.trac.tracdroid.PrettyDate;


public class SearchResult {

	public String href;
	public String title;
	public Date time;
	public String author;
	public String excerpt;
	
	public SearchResult(String href, String title, Date time, String author, String excerpt) {
		super();
		this.time = time;
		this.author = author;
		this.href = href;
		this.title = title;
		this.excerpt = excerpt;
	}
	
	public static SearchResult fromXMLRPC(Object[] array) {
		return new SearchResult( (String) array[0], (String) array[1],
					(Date) array[2], (String) array[3],(String) array[4] );
	}
	
}
