package vincentlark.trac.tracdroid;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import vincentlark.trac.Ticket;
import vincentlark.trac.TicketAction;
import vincentlark.trac.TicketChange;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TicketActivity extends Activity {

	public int ticket_id;
	static Ticket ticket;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.ticket);

		Bundle params = this.getIntent().getExtras();
		
		if (! loadTicket((int) params.getInt("ticket_id")))
			Toast.makeText(getApplicationContext(), "Sorry, cannot fetch this ticket", Toast.LENGTH_SHORT).show();
	}

	public boolean loadTicket(int id) {
		ProgressDialog dialog = ProgressDialog.show(this, "", "Loading wiki page", true, true);
		ticket = TracDroid.server.getTicket(id);
		
		if (ticket.id == 0) {
			dialog.dismiss();
			return false;
		}

		// Set the ViewFlipper
		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.ticket_viewflipper);
		flipper.setDisplayedChild(1);
		
		// Title
		((TextView) findViewById(R.id.ticket_title)).setText( "#" + ticket.id + ": " + ticket.attributes.summary);

		// Description, with longClick to edit
		TextView desc = (TextView) findViewById(R.id.ticket_desc);
		desc.setText(ticket.attributes.description);
		desc.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((TextView) v).setVisibility(View.GONE);

				EditText desc_edit = (EditText) findViewById(R.id.ticket_desc_edit);
				desc_edit.setText(((TextView) v).getText());
				desc_edit.setVisibility(View.VISIBLE);
				return true;
			}
		});
		EditText descEdit = (EditText) findViewById(R.id.ticket_desc_edit);
		descEdit.setVisibility(View.GONE);
		descEdit.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((EditText) v).setVisibility(View.GONE);

				TextView desc = (TextView) findViewById(R.id.ticket_desc);
				desc.setText(((EditText) v).getText());
				desc.setVisibility(View.VISIBLE);
				return true;
			}
		});
		
		// Reported by ..
		String created = PrettyDateDiff.between(new Date(), ticket.dateCreated);
		((TextView) findViewById(R.id.ticket_report)).setText(String.format("reported by %s %s", ticket.attributes.reporter, created));
		
		// Owned by ..
		((TextView) findViewById(R.id.ticket_owner)).setText(String.format("owned by %s", ticket.attributes.owner));
		
		// Current status
		((TextView) findViewById(R.id.ticket_status)).setText(String.format("status : %s", ticket.attributes.status));
		
		// ChangeLog
		Vector<TicketChange> changelog = (Vector<TicketChange>) ticket.changeLog;
		LinearLayout changelogLayout = (LinearLayout) findViewById(R.id.ticket_changelog);
		Iterator itc = changelog.iterator();
		while (itc.hasNext()) {
			TicketChange change = (TicketChange) itc.next();
			String field = (String) change.field;
			String niceTitle = change.getNiceTitle();

			// Inflate a new layout for that change
			LinearLayout changelogItem = (LinearLayout) View.inflate(this, R.layout.ticket_changelog_item, null);
			
			//TextView title = (TextView) changelogItem.findViewById(R.id.changelog_item_title);
			TextView title = new TextView(getApplicationContext());
			changelogLayout.addView(title);
			
			if (field.equals("comment") || field.equals("description")) {
			
				title.setText(niceTitle);

				//TextView text = (TextView) changelogItem.findViewById(R.id.changelog_item_text);
				TextView text = new TextView(getApplicationContext());
				text.setText(change.newv);
				changelogLayout.addView(text);

				// Show text when title is clicked
				title.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) { /* text.setVisibility(View.VISIBLE); */ }
				});
			}
			else {
				title.setText(niceTitle);
			}
			
			changelogLayout.addView(changelogItem);
		}
		((Button) findViewById(R.id.ticket_changelog_back)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) { switchView(1); }
		});
		
		
		// Tickets actions
		RadioGroup actions_radios = (RadioGroup) findViewById(R.id.ticket_actions);
		HashMap<String,TicketAction> actions = ticket.actions;
		Iterator ita = actions.keySet().iterator();
		while (ita.hasNext()) {
			String actionName = (String) ita.next();
			TicketAction action = actions.get(actionName);

			// Create a RadioButton for that choice
		    RadioButton radio = new RadioButton(getApplicationContext());
		    actions_radios.addView(radio);
		    radio.setText(action.label);

			// Handle inputFields for this action
			if (action.inputFields != null) {
				
				// Create a new layout for this choice's input_fields
				final LinearLayout actionPanel = new LinearLayout(getApplicationContext());
				
				// Make the inputFields (dis)appear when its radio is toggled
				radio.setOnCheckedChangeListener(new OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						actionPanel.setVisibility( isChecked ? View.VISIBLE : View.GONE );
					}});
				
				HashMap<String, Object> inputFields = (HashMap<String, Object>) action.inputFields;
				Iterator it = inputFields.keySet().iterator();
				while (it.hasNext()) {
					String inputName = (String) it.next();
					HashMap<String, Object> input = (HashMap<String, Object>) inputFields.get(inputName);
	
					// Inflate a layout to align fields
					LinearLayout actionInputPanel = (LinearLayout) View.inflate(this, R.layout.ticket_action_input, (ViewGroup) actionPanel);
				    
					// Field name
				    TextView inputLabel = (TextView) actionInputPanel.findViewById(R.id.ticket_action_input_name);
				    inputLabel.setText(inputFieldPrettyName((String) input.get("name")));
					
				    View inputField;
					// Input has options, use a spinner
					if (input.get("options") != null) {
						
						Vector<String> options = (Vector<String>) input.get("options");
						String[] arUsers = options.toArray(new String[options.size()]);
						
						inputField = actionInputPanel.findViewById(R.id.ticket_action_input_options);
						((Spinner) inputField).setAdapter(new ArrayAdapter(getApplicationContext(), R.layout.list_item_black, arUsers));
						((Spinner) inputField).setSelection(options.indexOf( input.get("value") ));
					}
					// Input hasn't options, use an EditText
					else {
						inputField = actionInputPanel.findViewById(R.id.ticket_action_input_text);
						((EditText) inputField).setText( (CharSequence) input.get("value") );
						
					}
					// Show the correct input
					inputField.setVisibility(View.VISIBLE);
				}
				// This inputFileds panel will be show if its radio is toggled on
				actionPanel.setVisibility(View.GONE);
				actions_radios.addView(actionPanel);
			}
		}

		// Type
		Vector<String> types = TracDroid.server.listTicketTypes();
		String[] arTypes = types.toArray(new String[types.size()]);
		Spinner typeField = (Spinner) findViewById(R.id.ticket_spin_type);
		typeField.setAdapter(new ArrayAdapter(getApplicationContext(), R.layout.list_item_black, arTypes));
		//typeField.setSelection(types.indexOf( ticket.type ));
		
		// Milestone
		Vector<String> milestones = TracDroid.server.listMilestones();
		String[] arMilestones = milestones.toArray(new String[milestones.size()]);
		Spinner milestoneField = (Spinner) findViewById(R.id.ticket_spin_milestone);
		milestoneField.setAdapter(new ArrayAdapter(getApplicationContext(), R.layout.list_item_black, arMilestones));
		milestoneField.setSelection(milestones.indexOf( ticket.attributes.milestone ));
		
		// Handle view switchers
		findViewById(R.id.ticket_link_changelog).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchView(0);
			}
		});
		/*
		findViewById(R.id.ticket_link_actions).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchView(2);
			}
		});
		*/
		
		dialog.dismiss();
		return true;
	}
	
	protected void saveCurrentTicket() {
		if (ticket == null || ticket.id == 0) return;
		
		
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ticket_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.ticket_menu_save:
        	return true;
        case R.id.tickets_menu_settings:
        	startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void switchView(int view) {
		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.ticket_viewflipper);
		flipper.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_up_in));
		flipper.setDisplayedChild(view);
    }
    
    private String inputFieldPrettyName(String name) {
    	if (name.equals("action_assigned_assigned_reassign_owner")) {
    		name = "to ";
    	}
    	else {
    		name = "> ";
    	}
    	return name;
    }

}
