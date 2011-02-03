package vincentlark.trac.tracdroid;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import vincentlark.trac.Ticket;
import vincentlark.trac.TicketAction;
import vincentlark.trac.TicketAttributes;
import vincentlark.trac.TicketChange;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.marakana.CameraActivity;

public class TicketActivity extends Activity {

	static Ticket ticket;
	static HashMap<String,String> current_changes;
	static Vector<String> current_attachements;
	
	static LinearLayout imagesAttachementsContainer;
	static LinearLayout filesAttachementsContainer;
	
	static final int TICKET_IMAGE = 1;
	static final int TICKET_FILE = 2;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.ticket);

		Bundle params = this.getIntent().getExtras();
		int ticket_id = (int) params.getInt("ticket_id".intern());
		if (ticket_id == 0)
			loadTicket(new Ticket(ticket_id, new Date(), new Date(), new TicketAttributes("", "", TracDroid.server.username, TracDroid.server.username, "", "", "", "", "")));
		else if (!loadTicket(ticket_id))
			Toast.makeText(getApplicationContext(), this.getString(R.string.unfetcheable_ticket), Toast.LENGTH_SHORT).show();
	}

	public boolean loadTicket(int id) {
		ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.loading_ticket), true, true);
		Ticket a_ticket = TracDroid.server.getTicket(id);
		dialog.dismiss();
		if (a_ticket != null) {
			this.loadTicket(a_ticket);
			return true;
		}
		return false;
	}
	
	private boolean creatingTicket() {
		return ticket.id == 0;
	}
	
	public boolean loadTicket(Ticket a_ticket) {
		ticket = a_ticket;
		current_changes = new HashMap<String,String>();
		current_attachements = new Vector<String>();

		final String new_text_value = this.getString(R.string.click_to_edit);

		// Set the ViewFlipper
		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.ticket_viewflipper);
		flipper.setDisplayedChild(1);
		flipper.getChildAt(1).scrollTo(0, 0);
		
		// Title, with longClick to edit
		TextView title = (TextView) findViewById(R.id.ticket_title); 
		title.setText(
				  (ticket.id > 0 ? '#' + ticket.id + ':' : "") 
				+ (ticket.attributes.summary.length() > 0 ? ticket.attributes.summary : "")
		);
		title.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((TextView) v).setVisibility(View.GONE);

				EditText title_edit = (EditText) findViewById(R.id.ticket_title_edit);
				if (!((TextView) v).getText().equals(getApplicationContext().getString(R.string.click_to_edit)))
					title_edit.setText(((TextView) v).getText());
				title_edit.setVisibility(View.VISIBLE);
				return true;
			}
		});
		EditText titleEdit = (EditText) findViewById(R.id.ticket_title_edit);
		titleEdit.setVisibility(View.GONE);
		titleEdit.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((EditText) v).setVisibility(View.GONE);
				TextView title = (TextView) findViewById(R.id.ticket_title);
				String new_summary = ((EditText) v).getText().toString();
				
				if (new_summary.trim().length()==0) {
					new_summary = new_text_value;
				}
				if (new_summary.equals(ticket.attributes.summary)) {
					current_changes.remove("summary");
				}
				else {
					current_changes.put("summary", new_summary);
				}
				((EditText) v).setText(new_summary);
				title.setText(new_summary);
				
				title.setVisibility(View.VISIBLE);
				return true;
			}
		});

		// Description, with longClick to edit
		TextView desc = (TextView) findViewById(R.id.ticket_desc);
		desc.setText(ticket.attributes.description);
		desc.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((TextView) v).setVisibility(View.GONE);

				EditText desc_edit = (EditText) findViewById(R.id.ticket_desc_edit);
				if (!((TextView) v).getText().equals(getApplicationContext().getString(R.string.click_to_edit)))
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
				String new_description = ((EditText) v).getText().toString();
				
				if (new_description.trim().length()==0) {
					new_description = "Click to edit";
				}
				if (new_description.equals(ticket.attributes.description)) {
					current_changes.remove("description");
				}
				else {
					current_changes.put("description", new_description);
				}
				((EditText) v).setText(new_description);
				desc.setText(new_description);
				
				desc.setVisibility(View.VISIBLE);
				return true;
			}
		});
		
		if (!creatingTicket()) {
			// Reported by ..
			String created = PrettyDate.between(new Date(), ticket.dateCreated);
			((TextView) findViewById(R.id.ticket_report)).setText(String.format(this.getString(R.string.reported_by_on), ticket.attributes.reporter, created));
			
			// Owned by ..
			if (ticket.attributes.owner != null)
				((TextView) findViewById(R.id.ticket_owner)).setText(String.format(this.getString(R.string.owned_by), ticket.attributes.owner));
			else
				((TextView) findViewById(R.id.ticket_owner)).setText(this.getString(R.string.not_owned));
			
			// Current status
			((TextView) findViewById(R.id.ticket_status)).setText(String.format(this.getString(R.string.status_is), ticket.attributes.status));
			
			// ChangeLog
			Vector<TicketChange> changelog = (Vector<TicketChange>) ticket.changeLog;
			LinearLayout changelogLayout = (LinearLayout) findViewById(R.id.ticket_changelog);
			Iterator<TicketChange> itc = changelog.iterator();
			while (itc.hasNext()) {
				TicketChange change = (TicketChange) itc.next();
				String field = (String) change.field;
				String niceTitle = change.getNiceTitle();
	
				// Inflate a new layout for that change
				LinearLayout changelogItem = (LinearLayout) View.inflate(this, R.layout.ticket_changelog_item, null);
				
				//TextView title = (TextView) changelogItem.findViewById(R.id.changelog_item_title);
				TextView titlev = new TextView(getApplicationContext());
				changelogLayout.addView(titlev);
				
				if (field.equals("comment") || field.equals("description")) {
				
					titlev.setText(niceTitle);
	
					//TextView text = (TextView) changelogItem.findViewById(R.id.changelog_item_text);
					TextView text = new TextView(getApplicationContext());
					text.setText(change.newv);
					changelogLayout.addView(text);
	
					// Show text when title is clicked
					titlev.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View v) { /* text.setVisibility(View.VISIBLE); */ }
					});
				}
				else {
					titlev.setText(niceTitle);
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
			Iterator<String> ita = actions.keySet().iterator();
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
					Iterator<String> it = inputFields.keySet().iterator();
					while (it.hasNext()) {
						String inputName = (String) it.next();
						@SuppressWarnings("unchecked")
						final HashMap<String, Object> input = (HashMap<String, Object>) inputFields.get(inputName);
		
						// Inflate a layout to align fields
						LinearLayout actionInputPanel = (LinearLayout) View.inflate(this, R.layout.ticket_action_input, (ViewGroup) actionPanel);
					    
						// Field name
					    TextView inputLabel = (TextView) actionInputPanel.findViewById(R.id.ticket_action_input_name);
					    inputLabel.setText(inputFieldPrettyName((String) input.get("name")));
						
					    final View inputField;
						// Input has options, use a spinner
						if (input.get("options") != null) {
							
							@SuppressWarnings("unchecked")
							Vector<String> options = (Vector<String>) input.get("options");
							String[] arUsers = options.toArray(new String[options.size()]);
							
							inputField = actionInputPanel.findViewById(R.id.ticket_action_input_options);
							((Spinner) inputField).setAdapter(new ArrayAdapter<Object>(getApplicationContext(), R.layout.list_item_black, arUsers));
							((Spinner) inputField).setSelection(options.indexOf( input.get("value") ));
							((Spinner) inputField).setOnItemSelectedListener(new OnItemSelectedListener() {
							    @Override
							    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
							        String new_input_value = (String) ((Spinner) inputField).getAdapter().getItem(position);
									if (new_input_value.equals(input.get("value"))) {
										current_changes.remove("action");
										current_changes.remove((String) input.get("name"));
									}
									else {
										String[] action_names = ((String) input.get("name")).split("_");
										String action_name = action_names[1] + "_" + action_names[2];
										
										current_changes.put("action", action_name);
										current_changes.put((String) input.get("name"), new_input_value);
									}
							    }
	
							    @Override
							    public void onNothingSelected(AdapterView<?> parentView) {
									current_changes.remove("action");
									current_changes.remove((String) input.get("name"));
							    }
							});
							
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
		}

		// Type
		Vector<String> types = TracDroid.server.listTicketTypes();
		String[] arTypes = types.toArray(new String[types.size()]);
		final Spinner typeField = (Spinner) findViewById(R.id.ticket_spin_type);
		typeField.setAdapter(new ArrayAdapter<Object>(getApplicationContext(), R.layout.list_item_black, arTypes));
		typeField.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	String new_type = (String) typeField.getAdapter().getItem(position);
		    	/* TODO
				if (new_type.equals(ticket.attributes.)) {
					current_changes.remove("type");
				}
				else {
					current_changes.put("type", new_description);
				}
		        */
		        current_changes.put("type", new_type);
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
				current_changes.remove("type");
		    }
		});
		
		// Milestone
		Vector<String> milestones = TracDroid.server.listMilestones();
		String[] arMilestones = milestones.toArray(new String[milestones.size()]);
		final Spinner milestoneField = (Spinner) findViewById(R.id.ticket_spin_milestone);
		milestoneField.setAdapter(new ArrayAdapter<Object>(getApplicationContext(), R.layout.list_item_black, arMilestones));
		milestoneField.setSelection(milestones.indexOf( ticket.attributes.milestone ));
		milestoneField.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		        String new_milestone = (String) milestoneField.getAdapter().getItem(position);
				if (new_milestone.equals(ticket.attributes.milestone)) {
					current_changes.remove("milestone");
				}
				else {
					current_changes.put("milestone", new_milestone);
				}
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
				current_changes.remove("milestone");
		    }
		});
		
		// Images Attachements
		imagesAttachementsContainer = (LinearLayout) findViewById(R.id.ticket_images_attachements);
		// Screenshot button
		Button button_photo = (Button) findViewById(R.id.ticket_photo_take);
		button_photo.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent().setClass(getApplicationContext(), CameraActivity.class);
				intent.putExtra("ticket_id", ticket.id);
				
				String summary = ((TextView) findViewById(R.id.ticket_title)).getText().toString();
				if (summary != new_text_value) intent.putExtra("ticket_summary", summary);
				
				String desc = ((TextView) findViewById(R.id.ticket_desc)).getText().toString();
				if (desc != new_text_value) intent.putExtra("ticket_desc", desc);
				
				startActivityForResult(intent, TICKET_IMAGE);
			}
		});
				
		// Files Attachements
		filesAttachementsContainer = (LinearLayout) findViewById(R.id.ticket_files_attachements);
		// Screenshot button
		Button button_file = (Button) findViewById(R.id.ticket_file_take);
		button_file.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				/*
				Intent intent = new Intent().setClass(getApplicationContext(), CameraActivity.class);
				intent.putExtra("ticket_id", ticket.id);
				startActivityForResult(intent, TICKET_FILE);
				*/
			}
		});
		
		// Handle view switcher
		findViewById(R.id.ticket_link_changelog).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchView(0);
			}
		});

		// Hide all useless fields for creation
		if (creatingTicket()) {
			int[] useless = { R.id.ticket_link_changelog, R.id.ticket_owner, R.id.ticket_report,
							  R.id.ticket_title, R.id.ticket_desc, R.id.ticket_actions_title };
			for (int i=0; i<useless.length; i++)
				findViewById(useless[i]).setVisibility(View.GONE);
			
			findViewById(R.id.ticket_title_edit).setVisibility(View.VISIBLE);
			findViewById(R.id.ticket_desc_edit).setVisibility(View.VISIBLE);
			
		}
		
		return true;
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
        case R.id.ticket_menu_new:
			Intent intent = new Intent().setClass(getApplicationContext(), TicketActivity.class);
			intent.putExtra("ticket_id", new Integer(0));
			startActivity(intent);
        	return true;
        case R.id.ticket_menu_save:
        	if (current_changes.size() > 0 || current_attachements.size() > 0) {
        		int ticket_id = ticket.id;
        		
	    		ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.updating_ticket), true, true);
	    		Log.d("ticket.update", current_changes.toString());
	    		
	    		/* FIXME */
	    		if (creatingTicket()) {
	    			String title = ((EditText) findViewById(R.id.ticket_title_edit)).getText().toString();
	    			String desc  = ((EditText) findViewById(R.id.ticket_desc_edit)).getText().toString();
	    			ticket_id = TracDroid.server.createTicket(title, desc, current_changes, true);
	    			Log.d("ticket.update: ticket has been created ", String.valueOf(ticket_id));
		        	if (ticket_id > 0) {
		        		dialog.dismiss();
		    			Toast.makeText(getApplicationContext(), this.getString(R.string.ticket_created), Toast.LENGTH_SHORT).show();
		        	}
	        	}
	    		else {
	    			Ticket new_ticket = TracDroid.server.updateTicket(ticket.id, current_changes);
	    			Log.d("ticket.update: ticket has been updated ", new_ticket.toString());
		        	if (new_ticket != null) {
		        		dialog.dismiss();
		    			Toast.makeText(getApplicationContext(), this.getString(R.string.ticket_updated), Toast.LENGTH_SHORT).show();
		        		current_changes.clear();
		        	}
	    		}

	    		if (ticket_id > 0) {
	    			Log.d("ticket.update", "There are "+String.valueOf(current_attachements.size())+" attachements to upload");

	    			Iterator<String> it = current_attachements.iterator();
	    			while (it.hasNext()) {
	    				String fileName = (String) it.next();
	    				try {
	    					File f = new File(fileName);
	    					DataInputStream file = new DataInputStream(new FileInputStream(fileName));
	    					// FIXME
	    					byte[] buffer = new byte[(int) f.length()];
	    					file.read(buffer);
	    					TracDroid.server.putAttachment(ticket_id, f.getName(), "", buffer);
	    				}
	    				catch (Exception e) {
	    					
	    				}
	    			}
	    		}

	    		current_changes.clear();
        		current_attachements.clear();
        		dialog.dismiss();
        	}
        	else {
        		Toast.makeText(getApplicationContext(), this.getString(R.string.nothing_changed), Toast.LENGTH_SHORT).show();
        	}
        	return true;
        case R.id.tickets_menu_settings:
        	startActivity(new Intent().setClass(getApplicationContext(), TracDroidPreferences.class));
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	Log.d("", "onActivityResult - requestCode="+String.valueOf(requestCode)+", resultCode="+String.valueOf(resultCode));
        // See which child activity is calling us back.
        switch (requestCode) {
        case TICKET_IMAGE:
            // This is the standard resultCode that is sent back if the
            // activity crashed or didn't doesn't supply an explicit result.
            if (resultCode == RESULT_CANCELED){
            } 
            else {
            	Log.d("", "onActivityResult - add attachement: "+data.getCharSequenceExtra("image_filename".intern()).toString());
            	addTicketAttachement("bitmap", data.getCharSequenceExtra("image_filename".intern()).toString());
            }
            break;
            
        case TICKET_FILE:
            // This is the standard resultCode that is sent back if the
            // activity crashed or didn't doesn't supply an explicit result.
            if (resultCode == RESULT_CANCELED){
            } 
            else {
            	Log.d("", "onActivityResult - add attachement: "+data.getCharSequenceExtra("file_filename".intern()).toString());
            	addTicketAttachement("file", data.getCharSequenceExtra("file_filename".intern()).toString());
            }
            break;
            
            default:
                break;
        }
    }
    
    private void addTicketAttachement(String type, String data) {
    	
    	if (type.equals("bitmap".intern())) {
	    	if (new File(data).exists()) {
	        	Log.d("", "onActivityResult - load file as attachement: "+data);
	    		Bitmap myBitmap = BitmapFactory.decodeFile(data);
	    		ImageView myImage = (ImageView) View.inflate(getApplicationContext(), R.layout.image, null);
	    		myImage.setImageBitmap(myBitmap);
	    		myImage.setAdjustViewBounds(true);
	    		imagesAttachementsContainer.addView(myImage);
	    	}
    	}
    	if (type.equals("file".intern())) {
	    	if (new File(data).exists()) {
	        	Log.d("", "onActivityResult - load file as attachement: "+data);
	    		ImageView myImage = (ImageView) View.inflate(getApplicationContext(), R.layout.image, null);
	    		myImage.setImageResource(R.drawable.ic_tab_tickets_on);
	    		myImage.setAdjustViewBounds(true);
	    		filesAttachementsContainer.addView(myImage);
	    	}
    	}
    	current_attachements.add(data);
	}

	private void switchView(int view) {
		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.ticket_viewflipper);
		flipper.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_up_in));
		flipper.setDisplayedChild(view);
		flipper.scrollTo(0, 0);
		flipper.getChildAt(view).scrollTo(0, 0);
    }
    
    private String inputFieldPrettyName(String name) {
    	if (name.equals("action_assigned_assigned_reassign_owner")) {
    		name = this.getString(R.string.to)+" ";
    	}
    	else {
    		name = "> ".intern();
    	}
    	return name;
    }

}
