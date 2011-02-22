package org.vl.trac.tracdroid;

import java.math.BigInteger;
import java.util.Date;

public class PrettyDate {

	private static int HOURS = 0;
	private static int DAYS = 1;
	private static int WEEKS = 2;
	private static int MONTHS = 3;
	private static int YEARS = 4;

	private static int LT1MA = 5;
	private static int ILT1M = 6;
	
	private static int S_AGO = 7;
	private static int MIN_AGO = 9;
	private static int H_AGO = 10;
	private static int D_AGO = 11;
	private static int W_AGO = 12;
	private static int M_AGO = 13;
	private static int Y_AGO = 14;
	
	private static int S_IN = 15;
	private static int MIN_IN = 16;
	private static int H_IN = 17;
	private static int D_IN = 18;
	private static int W_IN = 19;
	private static int M_IN = 20;
	private static int Y_IN = 21;
	
	
	private static String[] lT;
	public static void setLocalizedTimespan(String[] ts) {
		lT = ts;
	}
	
	public static String hours(int hours) {
		String res;
		if (hours < 24) {
	        res =  String.format(lT[HOURS], hours);
	    }
		else if (hours < 168) {
	    	long mdiff = Math.abs(Math.round(hours / 24));
	        res =  String.format(lT[DAYS], mdiff);
	    }
		else if (hours < 672) {
	    	long mdiff = Math.abs(Math.round(hours / 168));
	        res =  String.format(lT[WEEKS], mdiff);
	    }
		else if (hours < 8064) {
	    	long mdiff = Math.abs(Math.round(hours / 672));
	        res =  String.format(lT[MONTHS], mdiff);
	    }
		else {
			// TODO: compute months
	    	long mdiff = Math.abs(Math.round(hours / 8064));
	        res =  String.format(lT[YEARS], mdiff);
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
	        res =  String.format(lT[Y_IN], mdiff);
	    }
		else if (diff > 3145600) {
	    	long mdiff = Math.round(diff / 3145600);
	        res =  String.format(lT[M_IN], mdiff);
	    }
		else if (diff > 786400) {
	    	long mdiff = Math.round(diff / 786400);
	        res =  String.format(lT[W_IN], mdiff);
	    }
		else if (diff > 86400) {
	    	long mdiff = Math.round(diff / 86400);
	        res =  String.format(lT[D_IN], mdiff);
	    }
		else if (diff > 3600) {
	    	long mdiff = Math.round(diff / 3600);
	        res =  String.format(lT[H_IN], mdiff);
	    }
		else if (diff > 60) {
	        long mdiff = Math.round(diff / 60);
	        res =  String.format(lT[MIN_IN], mdiff);
	    }
		else if (diff > 30 && diff < 60) {
	        res = lT[ILT1M];
	    }
		else if (diff > 0) {
	        res =  String.format(lT[S_IN], diff);
	    }
		else if (diff > -30) {
	        res =  String.format(lT[S_AGO], diff);
	    }
		else if (diff > -60) {
	        res = lT[LT1MA];
	    }
		else if (diff > -3600) {
	        long mdiff = Math.abs(Math.round(diff / 60));
	        res =  String.format(lT[MIN_AGO], mdiff);
	    }
		else if (diff > -86400) {
	    	long mdiff = Math.abs(Math.round(diff / 3600));
	        res =  String.format(lT[H_AGO], mdiff);
	    }
		else if (diff > -786400) {
	    	long mdiff = Math.abs(Math.round(diff / 86400));
	        res =  String.format(lT[D_AGO], mdiff);
	    }
		else if (diff > -3145600) {
	    	long mdiff = Math.abs(Math.round(diff / 786400));
	        res =  String.format(lT[W_AGO], mdiff);
	    }
		else if (diff > -37747200) {
	    	long mdiff = Math.abs(Math.round(diff / 3145600));
	        res =  String.format(lT[M_AGO], mdiff);
	    }
		else {
			// TODO: compute months
	    	long mdiff = Math.abs(Math.round(diff / 37747200));
	        res =  String.format(lT[Y_AGO], mdiff);
		}
		
		return res;
	}
	
}
