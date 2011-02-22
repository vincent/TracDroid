package org.vl.widgets;

import java.util.Date;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DateView extends LinearLayout {

	public DateView(Context context, Date date) {
		super(context);
		setOrientation(LinearLayout.VERTICAL);
		
		TextView dayName = new TextView(context);
		
		TextView dayString = new TextView(context);
		addView(dayName);
		addView(dayString);
	}

	
	
	@Override
	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
		// TODO Auto-generated method stub

	}

}
