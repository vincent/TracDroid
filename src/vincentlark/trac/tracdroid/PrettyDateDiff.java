package vincentlark.trac.tracdroid;

import java.text.DateFormat;
import java.util.Date;

import android.util.Log;

public class PrettyDateDiff {

	public static String between(Date date1, Date date2) {
		Log.d("DATE 1", DateFormat.getDateInstance().format(date1));
		Log.d("DATE 2", DateFormat.getDateInstance().format(date2));
		
        long diff = new Integer((int) (date2.getTime() - date1.getTime() / 1000)).longValue();
		String res = diff + " seconds";
		
		if (diff > 2629743) {
	    	long mdiff = Math.round(diff / 60 / 60 / 24);
	        res =  String.format("in %d days", mdiff);
	    }
		else if (diff > 86400) {
	    	long mdiff = Math.round(diff / 60 / 60);
	        res =  String.format("in %d hours", mdiff);
	    }
		else if (diff > 3600) {
	        long mdiff = Math.round(diff / 60);
	        res =  String.format("in %d minutes", mdiff);
	    }
		else if (diff > 60) {
	        res = "in less than a minute";
	    }
		else if (diff > 0 && diff < 30) {
	        res = "now";
	    }
		else if (diff > -60) {
	        res = "less than a minute ago";
	    }
		else if (diff > -3600) {
	        long mdiff = Math.round(diff / 60);
	        res =  String.format("%d minutes ago", mdiff);
	    }
		else if (diff > -86400) {
	    	long mdiff = Math.round(diff / 60 / 60);
	        res =  String.format("%d hours ago", mdiff);
	    }
		else if (diff > -2629743) {
	    	long mdiff = Math.round(diff / 60 / 60 / 24);
	        res =  String.format("%d days ago", mdiff);
	    }
		else {
	        res = "never";
	    }   		
		return res;
	}
	
}
