package vincentlark.trac.tracdroid;

import vincentlark.widgets.DiffTable;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;

public class DiffActivity extends Activity {

	static DiffTable diffTable;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		this.setContentView(R.layout.diffview);

		diffTable = new DiffTable(getApplicationContext());
		((ScrollView) findViewById(R.id.diffview_content)).addView(diffTable);

		Bundle params = this.getIntent().getExtras();
		diffTable.setLeftPaneText((String) params.getString("leftText"));
		diffTable.setRightPaneText((String) params.getString("rightText"));
	}

}
