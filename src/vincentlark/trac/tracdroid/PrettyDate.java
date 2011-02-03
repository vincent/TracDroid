package vincentlark.trac.tracdroid;

import java.math.BigInteger;
import java.util.Date;

public class PrettyDate {

	public static String hours(int hours) {
		String res;
		if (hours < 24) {
	        res =  String.format("%d hours", hours);
	    }
		else if (hours < 168) {
	    	long mdiff = Math.abs(Math.round(hours / 24));
	        res =  String.format("%d days", mdiff);
	    }
		else if (hours < 672) {
	    	long mdiff = Math.abs(Math.round(hours / 168));
	        res =  String.format("%d weeks", mdiff);
	    }
		else if (hours < 8064) {
	    	long mdiff = Math.abs(Math.round(hours / 672));
	        res =  String.format("%d months", mdiff);
	    }
		else {
			// TODO: compute months
	    	long mdiff = Math.abs(Math.round(hours / 8064));
	        res =  String.format("%d years", mdiff);
		}
		return res;
	}

	public static String between(Date date1, Date date2) {
		
		String diff_ms = String.valueOf(date2.getTime() - date1.getTime());
		String res;

		long diff = new BigInteger(diff_ms).divide( new BigInteger("1000")).longValue();
		
		if (diff > 37747200) {
			// TODO: compute months
	    	long mdiff = Math.round(diff / 37747200);
	        res =  String.format("in %d years", mdiff);
	    }
		else if (diff > 3145600) {
	    	long mdiff = Math.round(diff / 3145600);
	        res =  String.format("in %d months", mdiff);
	    }
		else if (diff > 786400) {
	    	long mdiff = Math.round(diff / 786400);
	        res =  String.format("in %d weeks", mdiff);
	    }
		else if (diff > 86400) {
	    	long mdiff = Math.round(diff / 86400);
	        res =  String.format("in %d days", mdiff);
	    }
		else if (diff > 3600) {
	    	long mdiff = Math.round(diff / 3600);
	        res =  String.format("in %d hours", mdiff);
	    }
		else if (diff > 60) {
	        long mdiff = Math.round(diff / 60);
	        res =  String.format("in %d minutes", mdiff);
	    }
		else if (diff > 30 && diff < 60) {
	        res = "in less than a minute";
	    }
		else if (diff > 0) {
	        res =  String.format("in %d seconds", diff);
	    }
		else if (diff > -30) {
	        res =  String.format("%d seconds ago", diff);
	    }
		else if (diff > -60) {
	        res = "less than a minute ago";
	    }
		else if (diff > -3600) {
	        long mdiff = Math.abs(Math.round(diff / 60));
	        res =  String.format("%d minutes ago", mdiff);
	    }
		else if (diff > -86400) {
	    	long mdiff = Math.abs(Math.round(diff / 3600));
	        res =  String.format("%d hours ago", mdiff);
	    }
		else if (diff > -786400) {
	    	long mdiff = Math.abs(Math.round(diff / 86400));
	        res =  String.format("%d days ago", mdiff);
	    }
		else if (diff > -3145600) {
	    	long mdiff = Math.abs(Math.round(diff / 786400));
	        res =  String.format("%d weeks ago", mdiff);
	    }
		else if (diff > -37747200) {
	    	long mdiff = Math.abs(Math.round(diff / 3145600));
	        res =  String.format("%d months ago", mdiff);
	    }
		else {
			// TODO: compute months
	    	long mdiff = Math.abs(Math.round(diff / 37747200));
	        res =  String.format("%d years ago", mdiff);
		}
		
		return res;
	}
	
}
