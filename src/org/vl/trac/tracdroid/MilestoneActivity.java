package org.vl.trac.tracdroid;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.vl.trac.Milestone;
import org.vl.trac.MilestoneSummary;
import org.vl.trac.Ticket;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MilestoneActivity extends EditUnitActivity implements OnItemClickListener, DatePickerDialog.OnDateSetListener {

	static Milestone milestone;
	static HashMap<String,String> current_changes;

	Vector<Ticket> tickets;
	MilestoneSummary summary;
	
	final static int DIALOG_DATEPICKER = 1;
	
	MilestoneSummaryAdapter summaryAdapter;
	ListView summaryListView;
	
	String comment;
	Calendar cal;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cal = Calendar.getInstance();
        
		Bundle params = this.getIntent().getExtras();
		String milestone_name = params.getString("milestone_name".intern());
		if (milestone_name == null || milestone_name.equals("".intern()))
			loadMilestone(new Milestone());
		else if (!loadMilestone(milestone_name))
			Toast.makeText(getApplicationContext(), this.getString(R.string.oops), Toast.LENGTH_SHORT).show();
	}
	
	public boolean loadMilestone(String name) {
		ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.loading_ticket), true, true);
		Milestone a_milestone = TracDroid.server.getMilestone(name);
		
		tickets = TracDroid.server.ticketQuery("milestone="+name);
		
		summary = TracDroid.server.summarizeTicketQuery(tickets);

		if (a_milestone != null) {
			this.loadMilestone(a_milestone);
			dialog.dismiss();
			return true;
		}
		dialog.dismiss();
		return false;
	}
	
	protected boolean creating() {
		return milestone.name == null;
	}
	
	public boolean loadMilestone(Milestone a_milestone) {
		milestone = a_milestone;
		current_changes = new HashMap<String,String>();
		cal.setTime(milestone.dateDue == null ? new Date() : milestone.dateDue);
        getWindow().setTitle(getString(R.string.app_name) + " - " + (creating() ? getString(R.string.create_milestone) : milestone.name));

		somethingChanged();

		int[] useless = { R.id.link_changelog, R.id.file_take, R.id.photo_take };
		for (int i=0; i<useless.length; i++)
			findViewById(useless[i]).setVisibility(View.GONE);

		findViewById(R.id.title_edit).setVisibility(View.VISIBLE);
		findViewById(R.id.desc_edit).setVisibility(View.VISIBLE);

		collapseHeader(150);
        
		// Commit button
    	ImageButton commitButton = (ImageButton) findViewById(R.id.commit_button);
    	commitButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) { commentAndSaveTicket(); }
    	});

		// Calendar button
    	showDueDate(milestone.dateDue);
    	TextView calendarButton = (TextView) findViewById(R.id.calendar);
    	calendarButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_DATEPICKER);
			}
    	});

		final String new_text_value = this.getString(R.string.click_to_edit);

		// Title, with longClick to edit
		TextView title = (TextView) findViewById(R.id.title); 
		title.setText(milestone.name);
		title.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((TextView) v).setVisibility(View.INVISIBLE);

				EditText title_edit = (EditText) findViewById(R.id.title_edit);
				if (!((TextView) v).getText().equals(getApplicationContext().getString(R.string.click_to_edit)))
					title_edit.setText(((TextView) v).getText());
				title_edit.setVisibility(View.VISIBLE);
				return true;
			}
		});
		EditText titleEdit = (EditText) findViewById(R.id.title_edit);
		titleEdit.setVisibility(View.GONE);
		titleEdit.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((EditText) v).setVisibility(View.GONE);
				TextView title = (TextView) findViewById(R.id.title);
				String new_summary = ((EditText) v).getText().toString();
				
				if (new_summary.trim().length()==0) {
					new_summary = new_text_value;
				}
				if (new_summary.equals(milestone.name)) {
					current_changes.remove("name");
				}
				else {
					current_changes.put("name", new_summary);
				}
				((EditText) v).setText(new_summary);
				title.setText(new_summary);
				
				somethingChanged();
				((EditText) v).setInputType(InputType.TYPE_NULL);
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				
				title.setVisibility(View.VISIBLE);
				return true;
			}
		});

		// Description, with longClick to edit
		TextView desc = (TextView) findViewById(R.id.desc);
		desc.setText(milestone.description);
		desc.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((TextView) v).setVisibility(View.INVISIBLE);

				EditText desc_edit = (EditText) findViewById(R.id.desc_edit);
				if (!((TextView) v).getText().equals(getApplicationContext().getString(R.string.click_to_edit)))
					desc_edit.setText(((TextView) v).getText());
				desc_edit.setVisibility(View.VISIBLE);
				return true;
			}
		});
		EditText descEdit = (EditText) findViewById(R.id.desc_edit);
		descEdit.setVisibility(View.INVISIBLE);
		descEdit.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((EditText) v).setVisibility(View.INVISIBLE);
				TextView desc = (TextView) findViewById(R.id.desc);
				String new_description = ((EditText) v).getText().toString();
				
				if (new_description.trim().length()==0) {
					new_description = new_text_value;
				}
				if (new_description.equals(milestone.description)) {
					current_changes.remove("description");
				}
				else {
					current_changes.put("description", new_description);
				}
				((EditText) v).setText(new_description);
				desc.setText(new_description);
				
				somethingChanged();
				((EditText) v).setInputType(InputType.TYPE_NULL);
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				
				desc.setVisibility(View.VISIBLE);
				return true;
			}
		});

		if (creating()) {
		}
		else {
			
			// Summary
			summaryAdapter = new MilestoneSummaryAdapter(getApplicationContext(), milestone, summary);
			summaryListView = (ListView) findViewById(R.id.actions_view);
			summaryListView.setAdapter(summaryAdapter);
			summaryListView.setOnItemClickListener(this);

		}
				
		return true;
	}

	protected void showDueDate(Date date) {
    	TextView calendarButton = (TextView) findViewById(R.id.calendar);
    	String text;
    	if (date != null) {
    		calendarButton.setTextSize(13);
    		cal.setTime(date);
    		text = /* DateUtils.getDayOfWeekString(cal.get(Calendar.DAY_OF_WEEK), DateUtils.LENGTH_SHORT) + s + */ 
    					cal.get(Calendar.DAY_OF_MONTH) + "  ".intern() + DateUtils.getMonthString(cal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT)
    			 		+ "\n" + cal.get(Calendar.YEAR);
    	}
    	else {
    		calendarButton.setTextSize(18);
    		text = "\n?".intern();
    	}
    	calendarButton.setText(text);
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ticket_menu, menu);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case DIALOG_DATEPICKER:
			return new DatePickerDialog(this, MilestoneActivity.this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
    	}
		return null;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.milestone_menu_save:
        	commentAndSaveTicket();
        	return true;
        case R.id.milestone_menu_settings:
        	startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
	protected boolean commentAndSaveTicket() {
    	if (somethingChanged()) {
    		comment = "";
			final EditText editor = new EditText(getApplicationContext());
			AlertDialog.Builder builder = createDialog(this.getString(R.string.ask_comment), editor, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					MilestoneActivity.this.comment = editor.getText().toString();
					MilestoneActivity.this.save();
				}
			});
			builder.show();
    	}
    	else {
			Toast.makeText(getApplicationContext(), this.getString(R.string.nothing_changed), Toast.LENGTH_SHORT).show();
    	}
		return false;
    }
	
	protected boolean save() {
		ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.updating_ticket), true, true);
		Log.d("ticket.update", current_changes.toString());
		
		TracDroid.server.updateMilestone(milestone.name, current_changes);
		
		somethingChanged();
		dialog.dismiss();
		
		return false;
	}
    
    
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
		final String sectionTitle = summaryAdapter.getSectionOfItem(arg2);
		final String title = ((TextView) view.findViewById(R.id.list_item_value)).getText().toString();
		
		if (sectionTitle.equals(getString(R.string.all_tickets))) {
			showTicketsQuery("milestone="+milestone.name);
		}
		else if (sectionTitle.equals(getString(R.string.by_status))) {
			showTicketsQuery("milestone="+milestone.name+"&status="+title);
		}
		else if (sectionTitle.equals(getString(R.string.by_owner))) {
			showTicketsQuery("milestone="+milestone.name+"&owner="+title);
		}
		else if (sectionTitle.equals(getString(R.string.by_component))) {
			showTicketsQuery("milestone="+milestone.name+"&component="+title);
		}
		else if (sectionTitle.equals(getString(R.string.by_type))) {
			showTicketsQuery("milestone="+milestone.name+"&type="+title);
		}
	}
	
	protected void showTicketsQuery(String query) {
		Intent intent = new Intent().setClass(getApplicationContext(), TicketsActivity.class);
		intent.putExtra("ticket_query", query);
		startActivity(intent);
	}

	@Override
	protected boolean somethingChangedTest() {
		return false;
	}

	@Override
	protected void onSomethingChangedFirst() {
	}

	@Override
	protected void onSomethingChanged() {
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		Date date = new Date(year-1900, monthOfYear, dayOfMonth);
		Log.d("MilestoneActivity", "set date to "+date.toString());
		current_changes.put("dateDue", date.toGMTString());
		showDueDate(date);
	}
    
}
