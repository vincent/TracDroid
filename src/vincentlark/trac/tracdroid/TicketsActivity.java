package vincentlark.trac.tracdroid;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class TicketsActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("This is the Tickets tab");
        setContentView(textview);
    }	
}
