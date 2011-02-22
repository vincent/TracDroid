package org.vl.trac.tracdroid;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.vl.trac.Ticket;
import org.vl.trac.TicketAction;
import org.vl.trac.TicketAttributes;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.marakana.CameraActivity;

public class TicketActivity extends EditUnitActivity implements OnItemClickListener {

	static Ticket ticket;
	static HashMap<String,String> current_changes;
	static HashMap<String, Object[]> ticketFieldDefinitions;
	static Vector<String> current_attachements;
	
	static int flipView = 0;
	static int FLIP_VIEW_ACTIONS = 0;
	static int FLIP_VIEW_CHANGELOG = 1;
	static int FLIP_VIEW_ATTACHMENTS = 2;
	
	String comment = "";
	
	TicketAttachementsAdapter attachmentsAdapter;

	TicketActionsAdapter actionsAdapter;
	ListView actionsListView;
	
	static final int TICKET_IMAGE = 1;
	static final int TICKET_FILE = 2;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Bundle params = this.getIntent().getExtras();
		int ticket_id = (int) params.getInt("ticket_id".intern());
		if (ticket_id == 0)
			loadTicket(new Ticket(ticket_id, new Date(), new Date(), new TicketAttributes(TracDroid.server.username)));
		else if (!loadTicket(ticket_id))
			Toast.makeText(getApplicationContext(), this.getString(R.string.oops), Toast.LENGTH_SHORT).show();
	}
	
	public boolean loadTicket(int id) {
		ProgressDialog dialog = ProgressDialog.show(this, "", this.getString(R.string.loading_ticket), true, true);
		Ticket a_ticket = TracDroid.server.getTicket(id, true);
		dialog.dismiss();
		if (a_ticket != null) {
			this.loadTicket(a_ticket);
			return true;
		}
		return false;
	}
	
	protected boolean creating() {
		return ticket.id == 0;
	}
	
	public boolean loadTicket(Ticket a_ticket) {
		ticket = a_ticket;
		current_changes = new HashMap<String,String>();
		current_attachements = new Vector<String>();
		ticketFieldDefinitions = new HashMap<String, Object[]>();
        getWindow().setTitle(getString(R.string.app_name) + " - " + (creating() ? getString(R.string.create_ticket) : String.format(getString(R.string.ticket_number), ticket.id)));
        		
		somethingChanged();

		final String new_text_value = this.getString(R.string.click_to_edit);

		findViewById(R.id.calendar).setVisibility(View.GONE);
		
		// Commit button
    	ImageButton commitButton = (ImageButton) findViewById(R.id.commit_button);
    	commitButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) { commentAndSaveTicket(); }
    	});
		
		// Set the ViewFlipper
		switchView(FLIP_VIEW_ACTIONS);
		findViewById(R.id.file_take).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchView(FLIP_VIEW_ATTACHMENTS);
			}
		});
		
		// Title, with longClick to edit
		TextView title = (TextView) findViewById(R.id.title); 
		title.setText(
				  (ticket.id > 0 ? '#' + ticket.id + ':' : "") 
				+ (ticket.attributes.summary.length() > 0 ? ticket.attributes.summary : "")
		);
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
				if (new_summary.equals(ticket.attributes.summary)) {
					current_changes.remove("summary");
				}
				else {
					current_changes.put("summary", new_summary);
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
		desc.setText(ticket.attributes.description);
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
				
				somethingChanged();
				((EditText) v).setInputType(InputType.TYPE_NULL);
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				
				desc.setVisibility(View.VISIBLE);
				return true;
			}
		});
		
		// Attachments
		attachmentsAdapter = new TicketAttachementsAdapter(getApplicationContext(), ticket);
		ListView attachmentsListView = (ListView) findViewById(R.id.attachements);
		attachmentsListView.setAdapter(attachmentsAdapter);
		
		// Screenshot button
		ImageButton button_photo = (ImageButton) findViewById(R.id.photo_take);
		button_photo.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent().setClass(getApplicationContext(), CameraActivity.class);
				intent.putExtra("ticket_id", ticket.id);
				
				String summary = ((TextView) findViewById(R.id.title)).getText().toString();
				if (summary != new_text_value) intent.putExtra("ticket_summary", summary);
				
				String desc = ((TextView) findViewById(R.id.desc)).getText().toString();
				if (desc != new_text_value) intent.putExtra("ticket_desc", desc);
				
				startActivityForResult(intent, TICKET_IMAGE);
			}
		});

		// Hide all useless fields for creation
		if (creating()) {
			int[] useless = { R.id.link_changelog, R.id.title, R.id.desc };
			for (int i=0; i<useless.length; i++)
				findViewById(useless[i]).setVisibility(View.GONE);
			
			findViewById(R.id.title_edit).setVisibility(View.VISIBLE);
			findViewById(R.id.desc_edit).setVisibility(View.VISIBLE);
			
			collapseHeader(300);
		}
		else {
			collapseHeader(150);

			// Ticket actions
			actionsAdapter = new TicketActionsAdapter(getApplicationContext(), ticket);
			actionsListView = (ListView) findViewById(R.id.actions_view);
			actionsListView.setAdapter(actionsAdapter);
			actionsListView.setOnItemClickListener(this);
			
			actionsListView.setOnScrollListener(new OnScrollListener(){
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					// dynamic
					// headerLayoutParams.height = 150 - (80 / totalItemCount * firstVisibleItem);
					
					if (firstVisibleItem < 1)
						collapseHeader(150);
					else if (firstVisibleItem < 5)
						collapseHeader(120);
					else
						collapseHeader(80);
				}
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) { }
			});
			
			// ChangeLog
			final TicketChangelogAdapter changelogAdapter = new TicketChangelogAdapter(getApplicationContext(), ticket);
			final ListView changelogListView = (ListView) findViewById(R.id.changelog);
			changelogListView.setAdapter(changelogAdapter);
			
			// Handle view switcher
			findViewById(R.id.link_changelog).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					switchView(FLIP_VIEW_CHANGELOG);
				}
			});
			findViewById(R.id.link_changelog).setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if (flipView != FLIP_VIEW_CHANGELOG) switchView(FLIP_VIEW_CHANGELOG);
					changelogAdapter.reverse();
					changelogListView.postInvalidate();
					((TextView) findViewById(R.id.changelog_order)).setText(Html.fromHtml(changelogAdapter.isReversed() ? "&darr;":"&uarr;" ).toString());
					return true;
				}
			});
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
        case R.id.ticket_menu_save:
        	commentAndSaveTicket();
        	return true;
        case R.id.tickets_menu_settings:
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
					TicketActivity.this.comment = editor.getText().toString();
					TicketActivity.this.save();
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
		
		int ticket_id = ticket.id;

		if (creating()) {
			String title = ((EditText) findViewById(R.id.title_edit)).getText().toString();
			String desc  = ((EditText) findViewById(R.id.desc_edit)).getText().toString();
			ticket_id = TracDroid.server.createTicket(title, desc, comment, current_changes, true);
			Log.d("ticket.update: ticket has been created ", String.valueOf(ticket_id));
        	if (ticket_id > 0) {
        		dialog.dismiss();
        		current_changes.clear();
    			Toast.makeText(getApplicationContext(), this.getString(R.string.ticket_created), Toast.LENGTH_SHORT).show();
    			finish();
        	}
        	else {
    			Toast.makeText(getApplicationContext(), this.getString(R.string.oops), Toast.LENGTH_SHORT).show();
        		dialog.dismiss();
    			return false;
        	}
    	}
		else {
			Ticket new_ticket = TracDroid.server.updateTicket(ticket.id, comment, current_changes);
        	if (new_ticket != null) {
    			Log.d("ticket.update: ticket has been updated ", new_ticket.toString());
        		dialog.dismiss();
    			Toast.makeText(getApplicationContext(), this.getString(R.string.ticket_updated), Toast.LENGTH_SHORT).show();
        		current_changes.clear();
        	}
        	else {
    			Toast.makeText(getApplicationContext(), this.getString(R.string.oops), Toast.LENGTH_SHORT).show();
        		dialog.dismiss();
    			return false;
        	}
		}

		current_changes.clear();

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
	    			Toast.makeText(getApplicationContext(), this.getString(R.string.oops), Toast.LENGTH_SHORT).show();
				}
			}
		}

		current_attachements.clear();
		somethingChanged();
		dialog.dismiss();
		
		return false;
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
        somethingChanged();
    }
    
    private void addTicketAttachement(String type, String data) {
    	
    	if (type.equals("bitmap".intern())) {
    		File f = new File(data);
	    	if (f.exists()) {
	        	Log.d("", "onActivityResult - load file as attachement: "+data);
	    		Bitmap myBitmap = decodeFile(f, 40);
	    		ImageView myImage = (ImageView) View.inflate(getApplicationContext(), R.layout.image, null);
	    		myImage.setImageBitmap(myBitmap);
	    		myImage.setAdjustViewBounds(true);
	    		attachmentsAdapter.add(f.getName(), myImage);
	    	}
    	}
    	if (type.equals("file".intern())) {
    		File f = new File(data);
	    	if (f.exists()) {
	        	Log.d("", "onActivityResult - load file as attachement: "+data);
	    		ImageView myImage = (ImageView) View.inflate(getApplicationContext(), R.layout.image, null);
	    		myImage.setImageResource(R.drawable.ic_tab_tickets_on);
	    		myImage.setAdjustViewBounds(true);
	    		attachmentsAdapter.add(f.getName(), myImage);
	    	}
    	}
    	current_attachements.add(data);
    	((TextView) findViewById(R.id.photo_take_count)).setText(String.valueOf(current_attachements.size()));
    	
    	findViewById(R.id.unit_body).invalidate();
    	somethingChanged();
	}

    private Bitmap decodeFile(File f, int maxSize){
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            int scale = 1;
            if (o.outHeight > maxSize || o.outWidth > maxSize) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(maxSize / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            b = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return b;
    }
    
	private void switchView(int view) {

		if (flipView == view) {
			((ImageButton) findViewById(R.id.file_take)).setBackgroundResource(R.drawable.ic_menu_attachment);
			((ImageButton) findViewById(R.id.link_changelog)).setBackgroundResource(R.drawable.ic_menu_recent_history);

			if (flipView != FLIP_VIEW_ACTIONS) {
				switchView(FLIP_VIEW_ACTIONS);
			}
			return;
		}

		if (view == FLIP_VIEW_CHANGELOG) {
			((ImageButton) findViewById(R.id.link_changelog)).setBackgroundResource(R.drawable.ic_menu_recent_history_on);
			((ImageButton) findViewById(R.id.file_take)).setBackgroundResource(R.drawable.ic_menu_attachment);

			collapseHeader(80);
		}
		else if (view == FLIP_VIEW_ATTACHMENTS) {
			((ImageButton) findViewById(R.id.file_take)).setBackgroundResource(R.drawable.ic_menu_attachment_on);
			((ImageButton) findViewById(R.id.link_changelog)).setBackgroundResource(R.drawable.ic_menu_recent_history);

			collapseHeader(80);
		}
		else if (view == FLIP_VIEW_ACTIONS) {
			collapseHeader(120);
		}
		
		flipView = view;
		
		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.viewflipper);
		flipper.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_up_in));
		flipper.getChildAt(flipView).scrollTo(0, 0);
		flipper.setDisplayedChild(flipView);
		flipper.scrollTo(0, 0);
    }
    
    protected Object[] getTicketFieldDefinition(String title) {
    	if (!ticketFieldDefinitions.containsKey(title)) {
    		Object[] ticketFieldDefinition = null;
    		
			if (title.equals(getString(R.string.ticket_type))) {
				Vector<String> choices = TracDroid.server.listTicketTypes();
				ticketFieldDefinition = new Object[4];
				ticketFieldDefinition[0] = "type";
				ticketFieldDefinition[1] = this.getString(R.string.ticket_type);
				ticketFieldDefinition[2] = choices.toArray(new CharSequence[choices.size()]);
				ticketFieldDefinition[3] = ticket.attributes.type;
			}
	
	    	else if (title.equals(getString(R.string.ticket_milestone))) {
				Vector<String> choices = TracDroid.server.listMilestones();
				ticketFieldDefinition = new Object[4];
				ticketFieldDefinition[0] = "milestone";
				ticketFieldDefinition[1] = this.getString(R.string.ticket_milestone);
				ticketFieldDefinition[2] = choices.toArray(new CharSequence[choices.size()]);
				ticketFieldDefinition[3] = ticket.attributes.milestone;
			}
	    	
	    	else if (title.equals(getString(R.string.ticket_component))) {
				Vector<String> choices = TracDroid.server.listTicketComponents();
				ticketFieldDefinition = new Object[4];
				ticketFieldDefinition[0] = "component";
				ticketFieldDefinition[1] = this.getString(R.string.ticket_component);
				ticketFieldDefinition[2] = choices.toArray(new CharSequence[choices.size()]);
				ticketFieldDefinition[3] = ticket.attributes.component;
			}
			
	    	else if (title.equals(getString(R.string.ticket_priority))) {
				Vector<String> choices = TracDroid.server.listTicketPriorities();
				ticketFieldDefinition = new Object[4];
				ticketFieldDefinition[0] = "priority";
				ticketFieldDefinition[1] = this.getString(R.string.ticket_priority);
				ticketFieldDefinition[2] = choices.toArray(new CharSequence[choices.size()]);
				ticketFieldDefinition[3] = ticket.attributes.priority;
	    	}
			ticketFieldDefinitions.put(title, ticketFieldDefinition);
			return ticketFieldDefinition;
    	}	
		return ticketFieldDefinitions.get(title);
    }
    
	@Override
	public void onItemClick(AdapterView<?> adapter, final View view, int position, long arg3) {

		final String title = ((TextView) view.findViewById(R.id.list_complex_title)).getText().toString();

		final Object[] def = getTicketFieldDefinition(title);

		// Simple ticket attribute
		if (def != null) {
			final CharSequence[] arChoices = (CharSequence[]) def[2];
			AlertDialog.Builder builder = this.createSelectDialog(title, arChoices, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	String newValue = arChoices[item].toString();
			    	((TextView) view.findViewById(R.id.list_complex_caption)).setText(newValue);
			    	
			    	Log.d("TicketActivity", "changed "+(def[0].toString())+" to "+newValue);

			    	// was null, or already this value
			    	if (def[3] != null && newValue.equals(def[3].toString())) {
						current_changes.remove(def[0].toString());
					}
			    	// new value
					else {
						current_changes.put(def[0].toString(), newValue);
					}
			    	TicketActivity.this.somethingChanged();
			    }
			});
			builder.show();
		}

		// Tickets actions
    	else if (ticket.actions.containsKey(title)) {
			TicketAction action = ticket.actions.get(title);

			if (TicketActivity.this.actionsAdapter.selected != null) {
				TicketActivity.this.actionsAdapter.setSelected(null);
		    	TicketActivity.this.actionsListView.invalidateViews();
		    	TicketActivity.this.somethingChanged();
				TicketActivity.this.actionsListView.smoothScrollToPosition(TicketActivity.this.actionsAdapter.getCount());
				return;
			}
			
			// Dialog with input choices
			if (action.inputFields != null) {

				Iterator<String> it = action.inputFields.keySet().iterator();
				while (it.hasNext()) {
					String inputName = (String) it.next();
					@SuppressWarnings("unchecked")
					final HashMap<String, Object> input = (HashMap<String, Object>) action.inputFields.get(inputName);

					//String title = inputFieldPrettyName((String) input.get("name"));
					
					if (input.get("options") != null) {
						@SuppressWarnings("unchecked")
						Vector<String> options = (Vector<String>) input.get("options");
						final String[] arChoices = options.toArray(new String[options.size()]);

						AlertDialog.Builder builder = this.createSelectDialog(title, arChoices, 
								new DialogInterface.OnClickListener() {
								    public void onClick(DialogInterface dialog, int item) {
								    	String newValue = arChoices[item].toString();
								    	((TextView) view.findViewById(R.id.list_complex_caption)).setText(newValue);
		
								    	Log.d("TicketActivity", "changed to "+newValue);
		
								    	if (newValue.equals(input.get("value"))) {
											current_changes.remove("action");
											current_changes.remove(input.get("name").toString());
									    	TicketActivity.this.actionsAdapter.setSelected(null);
										}
										else {
											String[] action_names = ((String) input.get("name")).split("_");
											String action_name = action_names[1] + "_" + action_names[2];
											
											current_changes.put("action", action_name);
											current_changes.put((String) input.get("name"), newValue);
									    	
											TicketActivity.this.actionsAdapter.setSelected(title);
										}
								    	TicketActivity.this.somethingChanged();
								    	TicketActivity.this.actionsListView.invalidateViews();
								    }
								});
						builder.show();
					}
					else {
						final EditText editor = new EditText(getApplicationContext());
						editor.setText(input.get("value").toString());

						AlertDialog.Builder builder = this.createDialog(title, editor, 
								new DialogInterface.OnClickListener() {
								    public void onClick(DialogInterface dialog, int item) {
								    	String newValue = editor.getText().toString();
								    	((TextView) view.findViewById(R.id.list_complex_caption)).setText(newValue);
		
								    	Log.d("TicketActivity", "changed to "+newValue);
		
								    	if (newValue.equals("")) {
											current_changes.remove("action");
											current_changes.remove(input.get("name").toString());
									    	TicketActivity.this.actionsAdapter.setSelected(null);
										}
										else {
											String[] action_names = ((String) input.get("name")).split("_");
											String action_name = action_names[1] + "_" + action_names[2];
											
											current_changes.put("action", action_name);
											current_changes.put((String) input.get("name"), newValue);

											TicketActivity.this.actionsAdapter.setSelected(title);
										}
								    	TicketActivity.this.somethingChanged();
								    	TicketActivity.this.actionsListView.invalidateViews();
								    }
								});
						builder.show();
					}
					
				}
			}
			// Action with no choices
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(title+" ?")
				       .setCancelable(false)
				       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
								current_changes.put("action", title);
								TicketActivity.this.somethingChanged();
						    	TicketActivity.this.actionsAdapter.setSelected(title);
						    	TicketActivity.this.actionsListView.invalidateViews();
				           }
				       })
				       .setNegativeButton("No", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
								current_changes.remove("action");
								current_changes.remove(title);
								TicketActivity.this.somethingChanged();
						    	TicketActivity.this.actionsAdapter.setSelected(null);
						    	TicketActivity.this.actionsListView.invalidateViews();
				           }
				       });
				builder.show();
			}
    	}
	}

	@Override
	protected boolean somethingChangedTest() {
		return (current_changes.size() > 0 || current_attachements.size() > 0);
	}

	@Override
	protected void onSomethingChangedFirst() {
		Toast.makeText(getApplicationContext(), this.getString(R.string.ticket_modified), Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onSomethingChanged() {
		Log.d("TicketActivity", "current_changes="+current_changes+", current_attachements="+current_attachements);
    	TextView photo_count = (TextView) findViewById(R.id.photo_take_count);
    	photo_count.setText(current_attachements.size() > 0 ? String.valueOf(current_attachements.size()) : "".intern());
	}
    
}
