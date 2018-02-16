
// Instances of PyrWindow are created by PyramidWindowUI.actionPerform()
// I believe this class to specifically used for the pyramid test print

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

//import PyrWindow.Entry;

public class PyrWindow extends JFrame implements java.awt.event.ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<PyrWindow.Entry> entries; // An arraylist of instances of
												// the Entry subclass
	HashMap<String, JPanel> panels;
	JButton generate;
	JButton load;
	JTabbedPane tabbedPane;
	public int fileNumber;

	public PyrWindow(String title, int fileNumber) {
		super(title);
		init(fileNumber);
		pack();
		setVisible(true);
		this.fileNumber = fileNumber;
	}

	// a subclass for each test entry
	private class Entry {
		String name;
		String value;
		JTextField field;
		JLabel label;
		// String labelText; //unused variable
		String tab;

		Entry(String n, String v, String text, String t) {
			name = n;
			value = v;
			field = new JTextField(10);
			field.setText(v);
			label = new JLabel(text);
			tab = t;
		}
	}
	// sub class Entry ends here

	// initialize all entries to be displayed in selection window
	public void init(int fileNumber) {
		entries = new ArrayList<Entry>();
		entries.add(new PyrWindow.Entry("output_name", "", "Output name", "Basic Settings"));
		entries.add(
				new PyrWindow.Entry("x_center", "140", "x co-ordinate of the centre of the print", "Basic Settings"));
		entries.add(
				new PyrWindow.Entry("y_center", "140", "y co-ordinate of the centre of the print", "Basic Settings"));
		entries.add(new PyrWindow.Entry("cook_outer", "1", "Cook outer yes (1) / no (0)", "Cooking Settings"));
		entries.add(new PyrWindow.Entry("cook_speed_outer", "200", "Cook speed of outer material (mm/sec)",
				"Cooking Settings"));
		entries.add(new PyrWindow.Entry("side_count", "3", "Number of sides in the shape", "Basic Settings"));
		entries.add(new PyrWindow.Entry("print_speed", "1200", "Print Speed (mm/sec)", "Basic Settings"));
		entries.add(new PyrWindow.Entry("num_layers", "25", "Number of Layers", "Size Settings"));
		entries.add(new PyrWindow.Entry("base_width", "25", "Base Width (mm)", "Size Settings"));
		entries.add(new PyrWindow.Entry("layer_height", "1.2", "Layer Height (mm)", "Size Settings"));
		entries.add(new PyrWindow.Entry("twist_angle", "4", "Twist Angle per Layer (degrees)", "Basic Settings"));
		entries.add(new PyrWindow.Entry("top_thickness", "1", "Thickness of Top Layers (layers)", "Size Settings"));
		entries.add(
				new PyrWindow.Entry("bottom_thickness", "2", "Thickness of Bottom Layers (layers)", "Size Settings"));
		entries.add(new PyrWindow.Entry("bottom_layers", "15", "Number of Bottom Layers", "Size Settings"));
		entries.add(new PyrWindow.Entry("stop_after", "0", "Number of Layers to Print (0 = all)", "Size Settings"));
		entries.add(new PyrWindow.Entry("bed_z", "6", "Z position of print start [6 (slab) / 9 (plate)]",
				"Basic Settings"));
		entries.add(new PyrWindow.Entry("cook_temp", "255", "Spotlight power (0-255)", "Cooking Settings"));
		entries.add(new PyrWindow.Entry("cook_temp_standby", "0", "Spotlight power when not cooking (0-255)",
				"Cooking Settings"));
		entries.add(
				new PyrWindow.Entry("cook_lift", "0", "Height change on cook for every layer(mm)", "Cooking Settings"));
		entries.add(new PyrWindow.Entry("static_cook_height", "0", "Height change on static cook at the end",
				"Cooking Settings"));
		entries.add(
				new PyrWindow.Entry("static_cook_time", "5", "Time of static cook at the end (s)", "Cooking Settings"));
		entries.add(
				new PyrWindow.Entry("retraction", "3", "Retraction amount after print / cook moves", "Size Settings"));
		entries.add(new PyrWindow.Entry("cook_extra_extrude", "0", "Extra extrusion on cook moves (mm)",
				"Cooking Settings"));

		if (fileNumber == 1) {
			entries.add(new PyrWindow.Entry("load_depth", "0", "level of outer material (slot 1)",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("extrusion_multiplier", "1.0", "Extrusion Multiplier of outer material",
					"Multimaterial Settings"));
		} else if (fileNumber == 2) {
			entries.add(new PyrWindow.Entry("fill_layers_count", "20", "number of layers to fill",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("load_depth_2", "0", "level of outer material (slot 3)",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("load_depth_1", "0", "level of fill material (slot 1)",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("extrusion_multiplier_2", "1.0", "Extrusion Multiplier of outer material",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("extrusion_multiplier_1", "1.0", "Extrusion Multiplier of fill material",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("cook_fill", "1", "Cook fill yes (1) / no (0)", "Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("cook_speed_fill", "200", "Cook speed of fill material",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("cook_lap_on_fill", "2", "n : Cook every nth layer on fill",
					"Multimaterial Settings"));
		} else if (fileNumber == 3) {
			entries.add(new PyrWindow.Entry("fill_layers_count", "20", "number of layers to fill (<20)",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("load_depth_2", "0", "level of outer material (slot 3)",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("load_depth_1", "0", "level of fill material 1 (slot 1)",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("load_depth_3", "0", "level of fill material 2 (slot 2)",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("extrusion_multiplier_2", "1.0", "Extrusion Multiplier of outer material",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("extrusion_multiplier_1", "1.0", "Extrusion Multiplier of fill material 1",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("extrusion_multiplier_3", "1.0", "Extrusion Multiplier of fill material 2",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("cook_fill", "1", "Cook fill yes (1) / no (0)", "Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("cook_speed_fill", "200", "Cook speed of fill material",
					"Multimaterial Settings"));
			entries.add(new PyrWindow.Entry("cook_lap_on_fill", "2", "n : Cook every nth layer on fill",
					"Multimaterial Settings"));
		}

		///// Initial Error occured here because variables like tab, label, and
		///// field are not declared
		///// Tutch modified arugments passed to panels.containsKey():
		///// for example, "tab" to "entry.tab". "label" to "entry.label"
		panels = new HashMap<String, JPanel>();
		tabbedPane = new JTabbedPane();
		for (PyrWindow.Entry entry : entries) {
			if (panels.containsKey(entry.tab)) { // this tab belongs to the
													// Entry subclass
				//Sarah removed cast to JPanel
				( panels.get(entry.tab)).add(entry.label); 			// this label
																	// now
																	// belongs
																	// to the
																	// Entry
																	// subclass
				( panels.get(entry.tab)).add(entry.field); 			// this field
																	// belongs
																	// to the
																	// Entry
																	// subclass
			} else {
				JPanel p = new JPanel(new java.awt.GridLayout(0, 2));
				panels.put(entry.tab, p);
				tabbedPane.addTab(entry.tab, p);
				p.add(entry.label);
				p.add(entry.field);
			}
		}

		generate = new JButton("Generate");
		generate.addActionListener(this);

		load = new JButton("Load");
		load.addActionListener(this);

		for (JPanel p : panels.values()) {
			p.add(generate);
			p.add(load);
		}

		//Sarah removed casts to Jpanel here
		(panels.get("Multimaterial Settings")).add(generate);
		(panels.get("Multimaterial Settings")).add(load);

		add(tabbedPane);
	} // inti() method ends here

	// This method gets invoked whenever the user performs any action
	// (clicking a button, enter text in a field, or selecting from menu)
	public void actionPerformed(ActionEvent e) {
		String path = null; // path = path to G-code file

		/// ******** Generate option begins here ******////
		///// Generate here means to generate G-code ////////
		if (e.getSource() == generate) {
			try {
				HashMap<String, String> settings = new HashMap<String, String>();
				for (PyrWindow.Entry entry : entries) {
					entry.value = entry.field.getText(); // changed value to
															// entry.value //
															// changed field to
															// entry.field
					settings.put(entry.name, entry.value);
				}

				if (fileNumber == 1) {
					// Pyramid.writeHcode() takes in a Hashmap<name, value>
					// as argument
					path = Pyramid.writeGcode(settings);
				} else if (fileNumber == 2) {
					path = OuterAndFill.writeGcode(settings);
				} else if (fileNumber == 3) {
					path = MultiMaterial.writeGcode(settings);
				}
				saveConfig(settings);
				JOptionPane.showMessageDialog(this, "Gcode Generated at " + path);
			} catch (NumberFormatException e1) {
				JOptionPane.showMessageDialog(this, "Invalid input to one or more fields.");
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, "Error writing to file. " + e1.getMessage());
			}
		}
		/// ******* Generate option ends here ********////

		//// Load option begins here (Tutch assummes that this is for loading
		//// g-code files?) ///////
		else if (e.getSource() == load) {
			// open a file chooser
			JFileChooser fc = new JFileChooser(new File(".").getAbsolutePath());
			// chooser only accepts files ending in .food
			fc.addChoosableFileFilter(new FoodFilter());
			fc.setAcceptAllFileFilterUsed(false);

			int returnVal = fc.showOpenDialog(this);
			if (returnVal == 0) {
				File file = fc.getSelectedFile();
				// if loaded file is valid, load its configuration
				loadConfig(file);
			} else {
				JOptionPane.showMessageDialog(this, "No file was loaded.");
			}
		}
	}
	/// ****actionPerformed() ends here *****////////

	// saveCongif() is called by the Generate option portion of
	// actionPerformed()
	public void saveConfig(HashMap<String, String> settings) throws IOException {
		String name = settings.get("output_name");
		if (name.length() == 0) {
			name = "no_name";
		}
		if (name.endsWith(".gcode")) {
			name = name.substring(0, name.length() - 6);
		}
		File file = new File(name + ".food");
		FileWriter out = new FileWriter(file);
		for (PyrWindow.Entry entry : entries) {
			out.write(name + " " + entry.value + "\n"); /// Tutch changed value
														/// to entry.value
		}
		out.close();
	}

	// loadConfig() is called by the load option portion of actionPerformed()
	// loadConfig() scans File f
	public void loadConfig(File f) {
		try {
			Scanner in = new Scanner(f);
			Iterator<Entry> localIterator; // fixed raw Iterator to
											// Itarator<Entry>

			// for all lines in the loaded file f
			for (; in.hasNextLine(); localIterator.hasNext()) {
				// split the first line of the loaded file f
				String[] line = in.nextLine().split(" ");
				// store the first line in String[] line

				// Next, we want to iterate over all entries in the entries
				// ArrayList
				localIterator = entries.iterator(); // continue;
				//Sarah removed cast to (PyrWindow.Entry) 
				PyrWindow.Entry e = localIterator.next();

				// if the name of an entry matches with the first line of the
				// load file,
				// fetch and write the entry's value to line[1] and display that
				// value on the text field(in Window)
				if (e.name.equals(line[0])) {
					e.value = line[1];
					e.field.setText(e.value);
				}
			}

			in.close(); // close loaded file f
			JOptionPane.showMessageDialog(this, "Loaded " + f.getName());
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(this, "Error loading file");
		}
	}
}
