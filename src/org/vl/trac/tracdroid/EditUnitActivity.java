package org.vl.trac.tracdroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public abstract class EditUnitActivity extends Activity {

	boolean somethinghasChanged;
	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editable_unit);
	}

    public AlertDialog.Builder createSelectDialog(String title, CharSequence[] options, DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setItems(options, listener);
		return builder;
    }
    
    public AlertDialog.Builder createDialog(String title, View content, DialogInterface.OnClickListener positiveButtonListener) {
		return createDialog(title, content, positiveButtonListener, null);
    }
    
    public AlertDialog.Builder createDialog(String title, View content, DialogInterface.OnClickListener positiveButtonListener, DialogInterface.OnClickListener negativeButtonListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setView(content);
		builder.setPositiveButton(this.getString(R.string.ok), positiveButtonListener);
		if (negativeButtonListener != null)
			builder.setNegativeButton(this.getString(R.string.cancel), negativeButtonListener);
		return builder;
    }

    boolean somethingChanged() {
    	ImageButton commitButton = (ImageButton) findViewById(R.id.commit_button);
    	if (somethingChangedTest()) {

    		if (!somethinghasChanged)
    			onSomethingChangedFirst();

    		somethinghasChanged = true;
    		
    		onSomethingChanged();

    		commitButton.setBackgroundResource(R.drawable.ic_menu_save);
    		return true;
    	}
		commitButton.setBackgroundResource(R.drawable.btn_check_buttonless_on);
    	return false;
    }
    
    protected abstract boolean somethingChangedTest();
    
    protected abstract void onSomethingChangedFirst();
    
    protected abstract void onSomethingChanged();
    
	protected abstract boolean creating();

	protected abstract boolean save();
	
	protected void collapseHeader(int height) {
		LinearLayout body = (LinearLayout) findViewById(R.id.unit_body);
		RelativeLayout header = (RelativeLayout) findViewById(R.id.header);
		LayoutParams headerLayoutParams = header.getLayoutParams();
		headerLayoutParams.height = height;
		body.updateViewLayout(header, headerLayoutParams);
	}
    
}
