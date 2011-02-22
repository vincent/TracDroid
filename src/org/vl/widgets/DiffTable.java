package org.vl.widgets;

import org.vl.trac.tracdroid.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class DiffTable extends TableLayout {

	String[] leftText;
	String[] rightText;
	
	public DiffTable(Context context) {
		super(context);
	}
	
	public void setLeftPaneText(String text) {
		clear();
		String[] lines = text.split("\n");
		leftText = lines;

		LayoutInflater mInflater = LayoutInflater.from(getContext());
		
		int paneWidth = -1;
		
		for (int i=0; i<lines.length; i++) {
	        TableRow row = (TableRow) mInflater.inflate(R.layout.diffview_row, null);
	        TextView lineNb = (TextView) row.findViewById(R.id.diff_row_linenb);
	        lineNb.setText(String.valueOf(i+1));

	        TextView leftTextView = (TextView) row.findViewById(R.id.diff_row_left_text);
	        TextView rightTextView = (TextView) row.findViewById(R.id.diff_row_right_text);

	        if (paneWidth < 0) paneWidth = (row.getWidth() - lineNb.getWidth()) / 2;
	        
	        leftTextView.setText(lines[i]);
	        leftTextView.setMinimumWidth(paneWidth);
	        
	        rightTextView.setText("");
	        rightTextView.setMinimumWidth(paneWidth);
	        
			this.addView(row);
		}
	}

	public void setRightPaneText(String text) {
		String[] lines = text.split("\n");
		rightText = lines;

		LayoutInflater mInflater = LayoutInflater.from(getContext());
		int rowsCount = this.getChildCount();
		int rowsDiff = 0;

		for (int i=0; i<lines.length; i++) {
			TableRow row;
			boolean isNewRow = false;
			
			if (rowsCount <= i) {
				isNewRow = true;
				rowsCount++;

				row = (TableRow) mInflater.inflate(R.layout.diffview_row, null);
		        ((TextView) row.findViewById(R.id.diff_row_linenb)).setText(String.valueOf(i+1));

		        this.addView(row, i);
			}
			else {
				row = (TableRow) this.getChildAt(i);
			}
	        ((TextView) row.findViewById(R.id.diff_row_right_text)).setText(lines[i]);
			
			if (isNewRow || !leftText[i].equals(lines[i-rowsDiff])) {
				((TextView) row.findViewById(R.id.diff_row_left_text)).setBackgroundColor(Color.RED);
				((TextView) row.findViewById(R.id.diff_row_right_text)).setBackgroundColor(Color.GREEN);
				rowsDiff++;
			}
		}
	}

	private void clear() {
		this.removeAllViews();
		leftText = null;
		rightText = null;
	}
	
}
