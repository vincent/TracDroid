package vincentlark.trac;

import java.util.HashMap;
import java.util.Vector;

public class TicketAction {

	public String action;
	public String label;
	public String hints;

	public HashMap<String,Object> inputFields;
	
	public TicketAction(String action, String label, String hints,
			HashMap<String, Object> inputFields) {
		super();
		this.action = action;
		this.label = label;
		this.hints = hints;
		this.inputFields = inputFields;
	}
	
	public static TicketAction fromXMLRPC(Object[] array) {

		HashMap<String,Object> ticket_action = new HashMap<String,Object>();
		HashMap<String,Object> inputFields = null;
		Object[] inputFields_obj = (Object[]) array[3];
		if (inputFields_obj.length > 0) {
			inputFields = new HashMap<String,Object>();
			HashMap<String,Object> inputField = new HashMap<String,Object>();

			for (int j=0; j < inputFields_obj.length; j++) {
				inputField.clear();
				Object[] inputField_obj = (Object[]) inputFields_obj[j];
				
				inputField.put("name", (String) inputField_obj[0]);
				inputField.put("value", (String) inputField_obj[1]);
				
				Vector<String> options = null;
				Object[] options_obj = (Object[]) inputField_obj[2];
				
				if (options_obj.length > 0) {
					options = new Vector<String>();
					for (int k=0; k < options_obj.length; k++)
						options.add((String) options_obj[k]);
				}
				inputField.put("options", options);

				inputFields.put((String) inputField.get("name"), inputField);
			}
			ticket_action.put("input_fields", inputFields);
		}
		
		return new TicketAction( (String) array[0], (String) array[1], (String) array[2], inputFields);
	}

}
