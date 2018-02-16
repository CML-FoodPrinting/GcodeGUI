
//The Pyramid class is responsible for generating g-code
//and controlling the cooking/printing process

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//@SuppressWarnings("unchecked")
public class Pyramid {
	static double travel_speed = 6000.0D;
	static double print_speed;
	static double unit_E;
	static double side_count;
	static FileWriter out; // a filewriter writes streams of chars to a file
	static double spacing;
	static double x_center;
	static double y_center;
	static int top_thickness;
	static int bottom_thickness;
	static int bottom_layers;
	static double twist_angle;
	static int total_num_layers;
	static double centre_to_side_length;
	static double layer_height;
	static double z_lift = 20.0D;
	static double bed_z;
	static double cook_y_offset;
	static double cook_temp;
	static double retraction;
	static double cook_lift;
	static double cook_temp_standby;
	static double cook_speed_outer;
	static double cook_lap_on_fill;
	static double retract_after_dump = 3.0D;

	public Pyramid() {
	}

	// writeGcode() is called by the generate portion of
	// PyrWindow.actionPerformed()
	// writeGcode() takes in A hashmap<name, value> as arugument where name and
	// value are members of an entry. eg name: "layer_height", value: "1.2"
	public static String writeGcode(HashMap<String, String> settings) throws IOException {
		String name = settings.get("output_name"); // "output_name" is
															// a member of
															// Entry, which has
															// been hashed into
															// hashmap
		if (name.length() == 0)
			name = "no_name";
		if (!name.endsWith(".gcode"))
			name = name + ".gcode";
		File file = new File(name); // create a new .gcode file
		out = new FileWriter(file); // create a new writer to the .gcode file

		////// ****Begin initializing all member variables with values stored in
		////// ******///////
		// PyrWindow.entries that have been inserted by users. Obtaining these
		////// values by string parsing
		layer_height = Double.parseDouble(settings.get("layer_height"));
		twist_angle = Double.parseDouble(settings.get("twist_angle"));
		total_num_layers = Integer.parseInt(settings.get("num_layers"));
		centre_to_side_length = Double.parseDouble(settings.get("base_width"));
		// stop_after is the number of layer to print
		int stop_after = Integer.parseInt(settings.get("stop_after")); 
		side_count = Integer.parseInt(settings.get("side_count"));
		x_center = Integer.parseInt(settings.get("x_center"));
		y_center = Integer.parseInt(settings.get("y_center"));

		top_thickness = Integer.parseInt(settings.get("top_thickness"));
		bottom_thickness = Integer.parseInt(settings.get("bottom_thickness"));
		bottom_layers = Integer.parseInt(settings.get("bottom_layers"));

		retraction = Double.parseDouble(settings.get("retraction"));
		bed_z = Double.parseDouble(settings.get("bed_z"));
		print_speed = Double.parseDouble(settings.get("print_speed"));

		double maxLimit = 123.0D;
		double priming_extrusion = 0.0D;
		double syringe_dia = 22.5D;
		double nozzle_dia = 1.8D;
		double extrusion_multiplier = Double.parseDouble(settings.get("extrusion_multiplier"));

		double extrusion_width = 1.5D * nozzle_dia;
		unit_E = extrusion_multiplier
				* ((extrusion_width - layer_height) * layer_height
						+ 3.141592653589793D * (layer_height / 2.0D) * (layer_height / 2.0D))
				/ (3.141592653589793D * (syringe_dia / 2.0D) * (syringe_dia / 2.0D));

		cook_y_offset = -62.0D;
		cook_temp = Double.parseDouble(settings.get("cook_temp"));
		cook_temp_standby = Double.parseDouble(settings.get("cook_temp_standby"));
		cook_speed_outer = Double.parseDouble(settings.get("cook_speed_outer"));
		cook_lift = Double.parseDouble(settings.get("cook_lift"));
		int cook_outer = Integer.parseInt(settings.get("cook_outer"));
		double static_cook_height = Double.parseDouble(settings.get("static_cook_height"));
		double static_cook_time = Double.parseDouble(settings.get("static_cook_time"));

		double[] dump = { 10.0D, 150.0D, 5.0D };
		double load_depth = Double.parseDouble(settings.get("load_depth")) + 26.0D;
		double initial_dump_speed = 200.0D;

		spacing = extrusion_width - layer_height * 0.21460183660255172D;

		/////// ****** Initializing all member variables ends here****///

		// If the number of layer to print is <= 0, set it to total_num_layer
		if (stop_after <= 0) {
			stop_after = total_num_layers;
		}
		// create a new material
		Pyramid.Material curr = new Pyramid.Material(); // Material is Pyramid's
														// subclass

		/// write these chars to .gcode file
		/// figure out what these chars mean
		out.write("G21\n");
		out.write("G90\n");
		out.write("M82\n");
		out.write(String.format("G01 F%4.2f\n", new Object[] { Double.valueOf(travel_speed) }));
		out.write("G92 E0\n");
		out.write("G28 X Y Z\n");
		out.write(String.format("G01 E%4.2f\n", new Object[] { Double.valueOf(0.0D) }));

		//Modifying material's property based on the cook_outer variable 
		//and writing print-instructions to .gcode file 
		// If user chooses to print only the outer shell, call printOuter()
		if (cook_outer == 1) {
			Pyramid.Material.pickMaterial();
			Pyramid.Material.initialDump(curr, load_depth, initial_dump_speed, dump);
			printFill(curr, 0);
			Pyramid.Material.dropMaterial();
			cookFill(0);

			for (int layer = 1; layer <= stop_after; layer++) {
				Pyramid.Material.pickMaterial();//pickMaterials() contains undefined method
				printOuter(curr, layer);
				Pyramid.Material.dropMaterial();// dropMaterial() contains underfined method
				cookOuter(layer);
			}
		}
		//Else, if the user chooses to print the entire solid, call printFill()
		else {
			Pyramid.Material.pickMaterial(); //pickMaterials() contains undefined method
			Pyramid.Material.initialDump(curr, load_depth, initial_dump_speed, dump);
			printFill(curr, 0);
			for (int layer = 1; layer <= stop_after; layer++) {
				printOuter(curr, layer);
			}
			Pyramid.Material.dropMaterial();// dropMaterial() contains underfined method
		}

		if (cook_outer == 1) {
			cookStatic(stop_after, static_cook_height, static_cook_time);
		}
		//Tutch modified deep to curr.deep
		//maxLimit defined to be 123.00
		if (curr.deep > maxLimit) {
			throw new MatException("Material not enough for print");
		}
		out.close();
		return file.getAbsolutePath();
	}
	///// wrtieGcode() ends here

	
	//called by writeGcode(). parameters: i number of layers, cook height, cooktime
	//cookStatic() generates G-code that cooks one spot of solid
	private static void cookStatic(int i, double static_cook_height, double static_cook_time) throws IOException {
		double z = i * layer_height + bed_z; // z is the total measured height

		// write chars to .gcode file
		// look up what these 4 chars and their format mean
		// Tutch assumes that G01, M106, G4 are gcode instructions
		
		//G01 = move head to x y z with controlled speed. Format: G01 x y z speed
		out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n",
				new Object[] { Double.valueOf(x_center), Double.valueOf(y_center + cook_y_offset),
						Double.valueOf(z + cook_lift + static_cook_height), Double.valueOf(travel_speed) }));

		out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp) }));

		// G4 = dwell at current postion for p amount of seconds/millisecs. Format: G4 p 
		out.write(String.format("G4 P%4.2f\n", new Object[] { Double.valueOf(static_cook_time * 1000.0D) }));

		out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp_standby) }));
	}
	

	//private method called by writeGcode()
	//cookOuter() generates g-code that cooks printed shell layers
	private static void cookOuter(int i) throws IOException {
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();

		ArrayList<Double> x_cook = new ArrayList<Double>();
		ArrayList<Double> y_cook = new ArrayList<Double>();

		x.clear();
		y.clear();

		x_cook.clear();
		y_cook.clear();

		int thickness = i > bottom_layers ? top_thickness : bottom_thickness;
		double curr_base = (total_num_layers - i) / total_num_layers * centre_to_side_length;

		ArrayList<Double> t = new ArrayList<Double>();

		//// making lists of x-y coordinates when cooking outer shell //////
		for (int j = 0; j <= thickness - 1; j++) {
			t.clear();
			// d_i = layer thickness x pyramid's twist_anle x pi
			// d_(i+1) = d_i + 2pi/side_count
			//To do: Tutch needs to figure out what this formula means
			for (double d = i * twist_angle * 3.141592653589793D / 180.0D; d <= 6.283185307179586D 
					+ i * twist_angle * 3.141592653589793D / 180.0D; d = d + 6.283185307179586D / side_count) {
				t.add(Double.valueOf(d));
			}

			//For each layer of shell, we add x-cooridinate to list x, we also add to y-coordnate to list y 
			for (int index = 0; index < t.size(); index++) {
				x.add(Double.valueOf(x_center + (curr_base - j * Math.sqrt(3.0D) * spacing)
						* Math.cos(( t.get(index)).doubleValue())));
				y.add(Double.valueOf(y_center + (curr_base - j * Math.sqrt(3.0D) * spacing)
						* Math.sin((t.get(index)).doubleValue())));

				if (j == thickness / 2) {
					x_cook.add( x.get(x.size() - 1));
					y_cook.add( y.get(y.size() - 1));
				}
			}
		}

		///////generating path in the x-y direction with respect to the z direction///////
		double z = i * layer_height + bed_z;

		if (z < z_lift) {
			out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n",
					new Object[] { x_cook.get(0),
							Double.valueOf(( y_cook.get(0)).doubleValue() + cook_y_offset),
							Double.valueOf(z_lift + cook_lift), Double.valueOf(travel_speed) }));
			out.write(String.format("G01 Z%4.2f F%4.2f\n",
					new Object[] { Double.valueOf(z + cook_lift), Double.valueOf(travel_speed) }));
		} else {
			out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n",
					new Object[] { x_cook.get(0),
							Double.valueOf(( y_cook.get(0)).doubleValue() + cook_y_offset),
							Double.valueOf(z + cook_lift), Double.valueOf(travel_speed) }));
		}

		out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp) }));

		//generating the x-y-z paths for shell cooking
		for (int j = 2; j <= x_cook.size(); j++) {
			out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n",
					new Object[] { x_cook.get(j - 1),
							Double.valueOf(( y_cook.get(j - 1)).doubleValue() + cook_y_offset),
							Double.valueOf(z + cook_lift), Double.valueOf(cook_speed_outer) }));
		}

		if (z < z_lift) {
			out.write(String.format("G01 Z%4.2f  F%4.2f\n",
					new Object[] { Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
		}

		out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp_standby) }));
	}
	//////*******cookOuter() ends here********/////

	
	
	//////cookFill()
	private static void cookFill(int i) throws IOException {
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		ArrayList<Double> x_cook = new ArrayList<Double>();
		ArrayList<Double> y_cook = new ArrayList<Double>();

		x.clear();
		y.clear();
		x_cook.clear();
		y_cook.clear();

		ArrayList<Double> t = new ArrayList<Double>();

		int thickness = i > bottom_layers ? top_thickness : bottom_thickness;

		if (i == 0) {
			thickness = 0;
		}
		double curr_base = (total_num_layers - i) / total_num_layers * centre_to_side_length;
		double laps = curr_base / spacing / 2.0D;

		// for-loop of 2 other for-loops: Making lists of x-y coordinates
		for (int j = thickness; j <= laps; j++) {
			boolean cookOn;
			// duplicate variable // boolean cookOn;
			if (j % cook_lap_on_fill == 0.0D) {
				cookOn = true;
			} else {
				cookOn = false;
			}
			t.clear();
			
			//the following 2 for-loops are similar to those in cookOuter()
			for (double d = i * twist_angle * 3.141592653589793D / 180.0D; d <= 6.283185307179586D
					+ i * twist_angle * 3.141592653589793D / 180.0D; d = d + 6.283185307179586D / side_count) {
				t.add(Double.valueOf(d));
			}

			for (int index = 0; index < t.size(); index++) {
				x.add(Double.valueOf(x_center + (curr_base - j * Math.sqrt(3.0D) * spacing)
						* Math.cos((t.get(index)).doubleValue())));
				y.add(Double.valueOf(y_center + (curr_base - j * Math.sqrt(3.0D) * spacing)
						* Math.sin(( t.get(index)).doubleValue())));

				if (cookOn) {
					x_cook.add( x.get(x.size() - 1));
					y_cook.add( y.get(y.size() - 1));
				}
			}
		}

		double z = i * layer_height + bed_z;

		if ((x_cook.size() > 0) && (y_cook.size() > 0)) {
			if (z < z_lift) {
				out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n",
						new Object[] { x_cook.get(0),
								Double.valueOf(( y_cook.get(0)).doubleValue() + cook_y_offset),
								Double.valueOf(z_lift + cook_lift), Double.valueOf(travel_speed) }));
				out.write(String.format("G01 Z%4.2f F%4.2f\n",
						new Object[] { Double.valueOf(z + cook_lift), Double.valueOf(travel_speed) }));
			} else {
				out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n",
						new Object[] { x_cook.get(0),
								Double.valueOf((y_cook.get(0)).doubleValue() + cook_y_offset),
								Double.valueOf(z + cook_lift), Double.valueOf(travel_speed) }));
			}
		}

		out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp) }));

		for (int j = 2; j <= x_cook.size(); j++) {
			out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n",
					new Object[] { x_cook.get(j - 1),
							Double.valueOf(( y_cook.get(j - 1)).doubleValue() + cook_y_offset),
							Double.valueOf(z + cook_lift), Double.valueOf(cook_speed_outer) }));
		}

		out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp_standby) }));

		out.write(String.format("G01 Z%4.2f  F%4.2f\n",
				new Object[] { Double.valueOf(z + z_lift), Double.valueOf(travel_speed) }));
	}
	/////////******cookFill() ends here *****//////////////

	
	//printOuter is called by writeGcode() when the user wants to print just shell
	//printOuter takes in 2 parameters: Object of the Material subclass, i layer number
	//Hence printOuter is responsible for generating g-code of one shell's layer
	private static void printOuter(Pyramid.Material mat, int i) throws IOException {
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		ArrayList<Double> e = new ArrayList<Double>();

		x.clear();
		y.clear();
		e.clear();

		e.add(Double.valueOf(mat.deep)); // Tutch modified deep to mat.deep

		int thickness = i > bottom_layers ? top_thickness : bottom_thickness;

		double curr_base = (total_num_layers - i) / total_num_layers * centre_to_side_length;

		ArrayList<Double> t = new ArrayList<Double>();

		for (int j = 0; j <= thickness - 1; j++) {
			t.clear();
			for (double d = i * twist_angle * 3.141592653589793D / 180.0D; d <= 6.283185307179586D
					+ i * twist_angle * 3.141592653589793D / 180.0D; d = d + 6.283185307179586D / side_count) {
				t.add(Double.valueOf(d));
			}

			for (int index = 0; index < t.size(); index++) {
				x.add(Double.valueOf(x_center + (curr_base - j * Math.sqrt(3.0D) * spacing)
						* Math.cos(( t.get(index)).doubleValue())));
				y.add(Double.valueOf(y_center + (curr_base - j * Math.sqrt(3.0D) * spacing)
						* Math.sin(( t.get(index)).doubleValue())));
			}
		}

		for (int k = 0; k <= thickness - 1; k++) {
			for (int l = 1; l <= t.size() - 1; l++) {
				double distance = Math.sqrt(Math
						.pow((x.get(k * t.size() + l)).doubleValue()
								- ( x.get(k * t.size() + l - 1)).doubleValue(), 2.0D)
						+ Math.pow((y.get(k * t.size() + l)).doubleValue()
								- (y.get(k * t.size() + l - 1)).doubleValue(), 2.0D));
				e.add(Double.valueOf(unit_E * distance));
			}
		}

		for (int k = 1; k < e.size(); k++) {
			e.set(k, Double.valueOf(( e.get(k - 1)).doubleValue() + ( e.get(k)).doubleValue()));
		}

		for (int k = 1; k <= thickness - 1; k++) {
			ArrayList<Double> e2 = new ArrayList<Double>();
			for (int index = 1; index <= k * t.size(); index++) {
				e2.add( e.get(index - 1));
			}
			e2.add( e.get(k * t.size() - 1));
			for (int index = k * t.size() + 1; index <= e.size(); index++) {
				e2.add(e.get(index - 1));
			}

			e = e2;
		}

		double z = i * layer_height + bed_z;

		if (z < z_lift) {
			out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f  F%4.2f\n",
					new Object[] { x.get(0), y.get(0), Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
			out.write(String.format("G01 Z%4.2f  F%4.2f\n",
					new Object[] { Double.valueOf(z), Double.valueOf(travel_speed) }));
		} else {
			out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f  F%4.2f\n",
					new Object[] { x.get(0), y.get(0), Double.valueOf(z), Double.valueOf(travel_speed) }));
		}

		double e_last = 0.0D;

		for (int j = 1; j <= x.size(); j++) {
			out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f  F%4.2f E%4.2f\n", new Object[] { x.get(j - 1),
					y.get(j - 1), Double.valueOf(z), Double.valueOf(print_speed), e.get(j - 1) }));
			e_last = ( e.get(j - 1)).doubleValue();
		}

		if (e_last != 0.0D) {
			out.write(String.format("G01 F%4.2f E%4.2f\n",
					new Object[] { Double.valueOf(travel_speed), Double.valueOf(e_last - retraction) }));
		}
		if (z < z_lift) {
			out.write(String.format("G01 Z%4.2f  F%4.2f\n",
					new Object[] { Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
		}
		mat.deep = (e.get(e.size() - 1)).doubleValue();//deep was initially not a member of any class object. Tutch assumed that deep belong to the argument mat
		// Tutch modified deep to mat.deep
	}

	
	//printFill() is called by writeGcode() when the user wants to print the entire solid instead of shell 
	//Like printOuter(), printFill() is responsible for generating g-code of one solid layer
	//printFill() takes in an object of the Material subclass, i layer number
	private static void printFill(Pyramid.Material mat, int i) throws IOException {
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		ArrayList<Double> e = new ArrayList<Double>();

		x.clear();
		y.clear();
		e.clear();

		e.add(Double.valueOf(mat.deep));//deep was initially not a member of any class object. Tutch assumed that deep belong to the argument mat
		// Tutch modified deep to mat.deep

		int thickness = i > bottom_layers ? top_thickness : bottom_thickness;

		if (i == 0) {
			thickness = 0;
		}
		ArrayList<Double> t = new ArrayList<Double>();

		double curr_base = (total_num_layers - i) / total_num_layers * centre_to_side_length;
		double laps = curr_base / spacing / 2.0D;

		for (int j = thickness; j <= laps; j++) {
			t.clear();
			for (double d = i * twist_angle * 3.141592653589793D / 180.0D; d <= 6.283185307179586D
					+ i * twist_angle * 3.141592653589793D / 180.0D; d = d + 6.283185307179586D / side_count) {
				t.add(Double.valueOf(d));
			}

			for (int index = 0; index < t.size(); index++) {
				x.add(Double.valueOf(x_center + (curr_base - j * Math.sqrt(3.0D) * spacing)
						* Math.cos((t.get(index)).doubleValue())));
				y.add(Double.valueOf(y_center + (curr_base - j * Math.sqrt(3.0D) * spacing)
						* Math.sin(( t.get(index)).doubleValue())));
			}
		}

		for (int k = 0; k <= laps - thickness; k++) {
			for (int l = 1; l < t.size(); l++) {
				double dist = Math.sqrt(Math
						.pow((x.get(k * t.size() + l)).doubleValue()
								- ( x.get(k * t.size() + l - 1)).doubleValue(), 2.0D)
						+ Math.pow(( y.get(k * t.size() + l)).doubleValue()
								- (y.get(k * t.size() + l - 1)).doubleValue(), 2.0D));
				e.add(Double.valueOf(unit_E * dist));
			}
		}

		for (int k = 1; k < e.size(); k++) {
			e.set(k, Double.valueOf(( e.get(k - 1)).doubleValue() + ( e.get(k)).doubleValue()));
		}

		for (int k = 1; k <= laps - thickness; k++) {
			ArrayList<Double> e2 = new ArrayList<Double>();
			for (int index = 1; index <= k * t.size(); index++) {
				e2.add( e.get(index - 1));
			}
			e2.add( e.get(k * t.size() - 1));
			for (int index = k * t.size() + 1; index <= e.size(); index++) {
				e2.add( e.get(index - 1));
			}

			e = e2;
		}

		double z = i * layer_height + bed_z;

		if ((x.size() > 0) && (y.size() > 0)) {
			if (z < z_lift) {
				out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n",
						new Object[] { x.get(0), y.get(0), Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
				out.write(String.format("G01 Z%4.2f  F%4.2f\n",
						new Object[] { Double.valueOf(z), Double.valueOf(travel_speed) }));
			} else {
				out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n",
						new Object[] { x.get(0), y.get(0), Double.valueOf(z), Double.valueOf(travel_speed) }));
			}
		}

		double e_last = 0.0D;

		for (int j = 1; j <= x.size(); j++) {
			out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f  F%4.2f E%4.2f\n", new Object[] { x.get(j - 1),
					y.get(j - 1), Double.valueOf(z), Double.valueOf(print_speed), e.get(j - 1) }));
			e_last = ( e.get(j - 1)).doubleValue();
		}

		if (e_last != 0.0D) {
			out.write(String.format("G01 F%4.2f E%4.2f\n",
					new Object[] { Double.valueOf(travel_speed), Double.valueOf(e_last - retraction) }));
		}
		if (z < z_lift) {
			out.write(String.format("G01 Z%4.2f  F%4.2f\n",
					new Object[] { Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
		}
		mat.deep = (e.get(e.size() - 1)).doubleValue();//deep was initially not a member of any class object. Tutch assumed that deep belong to the argument mat
		// Tutch modified deep to mat.deep
	}

	private static String pick1() {
		String s = "G01 X2.5 Y30 Z7 F4000 \nG01 Y10 Z7 \nG01 Y10 Z10 \nG01 Y1 Z10 \nG01 Y1 Z14 \nG01 Y6 Z24 \nG01 Y30 Z24 \n";

		return s;
	}

	private static String drop1() {
		String s = "G01 X2.5 Y30 Z24 E0.0 F4000 \nG01 Y5 Z24 \nG01 Y2 Z14 \nG01 Y2 Z7 \nG01 Y30 Z7 \n";

		return s;
	}

	static class Material {
		private double deep;

		Material() {
		}

		public static void pickMaterial() throws IOException {
			//Pyramid.out.write(Pyramid.access$0()); //undefined method access()?
		}

		public static void dropMaterial() throws IOException {
			//Pyramid.out.write(Pyramid.access$1()); //undefined method access()?
		}

		public static void initialDump(Material mat, double load_depth, double initial_dump_speed, double[] dump)
				throws IOException {
			Pyramid.out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f\n", new Object[] { Double.valueOf(dump[0]),
					Double.valueOf(dump[1]), Double.valueOf(dump[2] + Pyramid.z_lift) }));
			Pyramid.out.write(String.format("G01 Z%4.2f\n", new Object[] { Double.valueOf(dump[2]) }));
			Pyramid.out.write(String.format("G01 F%4.2f E%4.2f\n",
					new Object[] { Double.valueOf(initial_dump_speed), Double.valueOf(load_depth) }));
			Pyramid.out.write(String.format("G01 F%4.2f E%4.2f\n", new Object[] { Double.valueOf(initial_dump_speed),
					Double.valueOf(load_depth - Pyramid.retract_after_dump) }));
			Pyramid.out.write(String.format("G01 Z%4.2f F%4.2f\n",
					new Object[] { Double.valueOf(dump[2] + Pyramid.z_lift), Double.valueOf(Pyramid.travel_speed) }));
			mat.deep += load_depth;//deep was initially not a member of any class object. Tutch assumed that deep belong to the argument mat
			// Tutch modified deep to mat.deep
		}
	}
}
