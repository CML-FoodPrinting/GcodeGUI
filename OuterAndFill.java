import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class OuterAndFill
{
	//static boolean turn;
  static double travel_speed = 6000.0D;
  static double print_speed;
  static double unit_E_1;
  static double unit_E_2;
  static double side_count;
  static FileWriter out;
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
  static double cook_lift;
  static double cook_temp_standby;
  static double cook_speed_outer;
  static double cook_speed_fill;
  static double retraction;
  static double cook_lap_on_fill;
  static double retract_after_dump = 3.0D;
  
  public OuterAndFill() {}
  
  public static String writeGcode(HashMap<String, String> settings) throws IOException {
    String name = (String)settings.get("output_name");
    if (name.length() == 0)
      name = "no_name";
    if (!name.endsWith(".gcode"))
      name = name + ".gcode";
    File file = new File(name);
    out = new FileWriter(file);
    

    layer_height = Double.parseDouble((String)settings.get("layer_height"));
    twist_angle = Double.parseDouble((String)settings.get("twist_angle"));
    total_num_layers = Integer.parseInt((String)settings.get("num_layers"));
    centre_to_side_length = Double.parseDouble((String)settings.get("base_width"));
    int stop_after = Integer.parseInt((String)settings.get("stop_after"));
    side_count = Integer.parseInt((String)settings.get("side_count"));
    x_center = Integer.parseInt((String)settings.get("x_center"));
    y_center = Integer.parseInt((String)settings.get("y_center"));
    int fill_layers_count = Integer.parseInt((String)settings.get("fill_layers_count"));
    
    top_thickness = Integer.parseInt((String)settings.get("top_thickness"));
    bottom_thickness = Integer.parseInt((String)settings.get("bottom_thickness"));
    bottom_layers = Integer.parseInt((String)settings.get("bottom_layers"));
    
    retraction = Double.parseDouble((String)settings.get("retraction"));
    
    bed_z = Double.parseDouble((String)settings.get("bed_z"));
    
    print_speed = Double.parseDouble((String)settings.get("print_speed"));
    

    double maxLimit = 123.0D;
    double priming_extrusion = 0.0D;
    double syringe_dia = 22.5D;
    double nozzle_dia = 1.8D;
    double extrusion_multiplier_1 = Double.parseDouble((String)settings.get("extrusion_multiplier_1"));
    double extrusion_multiplier_2 = Double.parseDouble((String)settings.get("extrusion_multiplier_2"));
    double extrusion_width = 1.5D * nozzle_dia;
    unit_E_1 = extrusion_multiplier_1 * (
      (extrusion_width - layer_height) * layer_height + 3.141592653589793D * (layer_height / 2.0D) * (layer_height / 2.0D)) / (
      3.141592653589793D * (syringe_dia / 2.0D) * (syringe_dia / 2.0D));
    
    unit_E_2 = extrusion_multiplier_2 * (
      (extrusion_width - layer_height) * layer_height + 3.141592653589793D * (layer_height / 2.0D) * (layer_height / 2.0D)) / (
      3.141592653589793D * (syringe_dia / 2.0D) * (syringe_dia / 2.0D));
    

    cook_y_offset = -62.0D;
    cook_temp = Double.parseDouble((String)settings.get("cook_temp"));
    cook_temp_standby = Double.parseDouble((String)settings.get("cook_temp_standby"));
    cook_speed_outer = Double.parseDouble((String)settings.get("cook_speed_outer"));
    cook_speed_fill = Double.parseDouble((String)settings.get("cook_speed_fill"));
    cook_lift = Double.parseDouble((String)settings.get("cook_lift"));
    cook_lap_on_fill = Double.parseDouble((String)settings.get("cook_lap_on_fill"));
    int cook_fill = Integer.parseInt((String)settings.get("cook_fill"));
    int cook_outer = Integer.parseInt((String)settings.get("cook_outer"));
    double static_cook_height = Double.parseDouble((String)settings.get("static_cook_height"));
    double static_cook_time = Double.parseDouble((String)settings.get("static_cook_time"));
    




    double[] dump = { 10.0D, 150.0D, 5.0D };
    double load_depth_1 = Double.parseDouble((String)settings.get("load_depth_1")) + 26.0D;
    double load_depth_2 = Double.parseDouble((String)settings.get("load_depth_2")) + 26.0D;
    double initial_dump_speed = 200.0D;
    


    spacing = extrusion_width - layer_height * 0.21460183660255172D;
    
    if (stop_after <= 0) {
      stop_after = total_num_layers;
    }
    
    OuterAndFill.Material one = new OuterAndFill.Material();
    OuterAndFill.Material two = new OuterAndFill.Material();
    


    out.write("G21\n");
    out.write("G90\n");
    out.write("M82\n");
    out.write(String.format("G01 F%4.2f\n", new Object[] { Double.valueOf(travel_speed) }));
    out.write("G92 E0\n");
    out.write("G28 X Y Z\n");
    out.write(String.format("G01 E%4.2f\n", new Object[] { Double.valueOf(0.0D) }));
    

    boolean turn = true;
    OuterAndFill.Material curr = OuterAndFill.Material.pickMaterial(one, two);
    OuterAndFill.Material.initialDump(curr, load_depth_1, initial_dump_speed, dump);
    OuterAndFill.Material.dropMaterial(one, two);
    

    turn = true;
    curr = OuterAndFill.Material.pickMaterial(one, two);
    OuterAndFill.Material.initialDump(curr, load_depth_2, initial_dump_speed, dump);
    printFill(curr, 0);
    OuterAndFill.Material.dropMaterial(one, two);
    if (cook_outer == 1) {
      cookFill(0);
    }
    


    for (int layer = 1; layer < fill_layers_count; layer++) {
      turn = true;
      curr = OuterAndFill.Material.pickMaterial(one, two);
      printOuter(curr, layer);
      OuterAndFill.Material.dropMaterial(one, two);
      
      turn = true;
      curr = OuterAndFill.Material.pickMaterial(one, two);
      printFill(curr, layer);
      OuterAndFill.Material.dropMaterial(one, two);
      
      if (cook_outer == 1)
        cookOuter(layer);
      if (cook_fill == 1) {
        cookFill(layer);
      }
    }
    











    turn = true;
    curr = OuterAndFill.Material.pickMaterial(one, two);
    for (int layer = fill_layers_count; layer <= stop_after; layer++) {
      printOuter(curr, layer);
    }
    OuterAndFill.Material.dropMaterial(one, two);
    


    if (cook_outer == 1) {
      cookStatic(stop_after, static_cook_height, static_cook_time);
    }

	if (deep > maxLimit)
      throw new MatException("Material not enough for fill");
    if (deep > maxLimit)
      throw new MatException("Material not enough for outer");
    out.close();
    return file.getAbsolutePath();
  }
  
  private static void cookStatic(int i, double static_cook_height, double static_cook_time)
    throws IOException
  {
    double z = i * layer_height + bed_z;
    

    out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n", new Object[] { Double.valueOf(x_center), Double.valueOf(y_center + cook_y_offset), 
      Double.valueOf(z + cook_lift + static_cook_height), Double.valueOf(travel_speed) }));
    

    out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp) }));
    

    out.write(String.format("G4 P%4.2f\n", new Object[] { Double.valueOf(static_cook_time * 1000.0D) }));
    

    out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp_standby) }));
  }
  
  private static void cookOuter(int i)
    throws IOException
  {
    ArrayList<Double> x = new ArrayList();
    ArrayList<Double> y = new ArrayList();
    
    ArrayList<Double> x_cook = new ArrayList();
    ArrayList<Double> y_cook = new ArrayList();
    
    x.clear();
    y.clear();
    
    x_cook.clear();
    y_cook.clear();
    
    int thickness = i > bottom_layers ? top_thickness : bottom_thickness;
    double curr_base = (total_num_layers - i) / total_num_layers * centre_to_side_length;
    
    ArrayList<Double> t = new ArrayList();
    
    for (int j = 0; j <= thickness - 1; j++) {
      t.clear();
      for (double d = i * twist_angle * 3.141592653589793D / 180.0D; d <= 6.283185307179586D + i * twist_angle * 3.141592653589793D / 180.0D; 
          d = d + 6.283185307179586D / side_count) {
        t.add(Double.valueOf(d));
      }
      
      for (int index = 0; index < t.size(); index++)
      {
        x.add(Double.valueOf(x_center + (curr_base - j * Math.sqrt(3.0D) * spacing) * Math.cos(((Double)t.get(index)).doubleValue())));
        y.add(Double.valueOf(y_center + (curr_base - j * Math.sqrt(3.0D) * spacing) * Math.sin(((Double)t.get(index)).doubleValue())));
        




        if (j == thickness / 2) {
          x_cook.add((Double)x.get(x.size() - 1));
          y_cook.add((Double)y.get(y.size() - 1));
        }
      }
    }
    


    double z = i * layer_height + bed_z;
    
    if (z < z_lift) {
      out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n", new Object[] { x_cook.get(0), Double.valueOf(((Double)y_cook.get(0)).doubleValue() + cook_y_offset), 
        Double.valueOf(z_lift + cook_lift), Double.valueOf(travel_speed) }));
      out.write(String.format("G01 Z%4.2f F%4.2f\n", new Object[] { Double.valueOf(z + cook_lift), Double.valueOf(travel_speed) }));
    } else {
      out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n", new Object[] { x_cook.get(0), Double.valueOf(((Double)y_cook.get(0)).doubleValue() + cook_y_offset), 
        Double.valueOf(z + cook_lift), Double.valueOf(travel_speed) }));
    }
    

    out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp) }));
    

    for (int j = 2; j <= x_cook.size(); j++) {
      out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n", new Object[] { x_cook.get(j - 1), 
        Double.valueOf(((Double)y_cook.get(j - 1)).doubleValue() + cook_y_offset), Double.valueOf(z + cook_lift), Double.valueOf(cook_speed_outer) }));
    }
    

    if (z < z_lift) {
      out.write(String.format("G01 Z%4.2f  F%4.2f\n", new Object[] { Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
    }
    
    out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp_standby) }));
  }
  
  private static void cookFill(int i) throws IOException
  {
    ArrayList<Double> x = new ArrayList();
    ArrayList<Double> y = new ArrayList();
    ArrayList<Double> x_cook = new ArrayList();
    ArrayList<Double> y_cook = new ArrayList();
    
    x.clear();
    y.clear();
    x_cook.clear();
    y_cook.clear();
    
    ArrayList<Double> t = new ArrayList();
    
    int thickness = i > bottom_layers ? top_thickness : bottom_thickness;
    
    if (i == 0) {
      thickness = 0;
    }
    double curr_base = (total_num_layers - i) / total_num_layers * centre_to_side_length;
    double laps = curr_base / spacing / 2.0D;
    


    for (int j = thickness; j <= laps; j++) { boolean cookOn;
      boolean cookOn;
      if (j % cook_lap_on_fill == 0.0D) {
        cookOn = true;
      } else {
        cookOn = false;
      }
      t.clear();
      for (double d = i * twist_angle * 3.141592653589793D / 180.0D; d <= 6.283185307179586D + i * twist_angle * 3.141592653589793D / 180.0D; 
          d = d + 6.283185307179586D / side_count) {
        t.add(Double.valueOf(d));
      }
      
      for (int index = 0; index < t.size(); index++) {
        x.add(Double.valueOf(x_center + (curr_base - j * Math.sqrt(3.0D) * spacing) * Math.cos(((Double)t.get(index)).doubleValue())));
        y.add(Double.valueOf(y_center + (curr_base - j * Math.sqrt(3.0D) * spacing) * Math.sin(((Double)t.get(index)).doubleValue())));
        



        if (cookOn) {
          x_cook.add((Double)x.get(x.size() - 1));
          y_cook.add((Double)y.get(y.size() - 1));
        }
      }
    }
    
    double z = i * layer_height + bed_z;
    
    if ((x_cook.size() > 0) && (y_cook.size() > 0))
    {
      if (z < z_lift) {
        out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n", new Object[] { x_cook.get(0), 
          Double.valueOf(((Double)y_cook.get(0)).doubleValue() + cook_y_offset), Double.valueOf(z_lift + cook_lift), Double.valueOf(travel_speed) }));
        out.write(String.format("G01 Z%4.2f F%4.2f\n", new Object[] { Double.valueOf(z + cook_lift), Double.valueOf(travel_speed) }));
      } else {
        out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n", new Object[] { x_cook.get(0), 
          Double.valueOf(((Double)y_cook.get(0)).doubleValue() + cook_y_offset), Double.valueOf(z + cook_lift), Double.valueOf(travel_speed) }));
      }
    }
    

    out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp) }));
    

    for (int j = 2; j <= x_cook.size(); j++) {
      out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n", new Object[] { x_cook.get(j - 1), 
        Double.valueOf(((Double)y_cook.get(j - 1)).doubleValue() + cook_y_offset), Double.valueOf(z + cook_lift), Double.valueOf(cook_speed_fill) }));
    }
    


    out.write(String.format("M106 S%4.2f\n", new Object[] { Double.valueOf(cook_temp_standby) }));
    
    if (z < z_lift) {
      out.write(String.format("G01 Z%4.2f  F%4.2f\n", new Object[] { Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
    }
  }
  
  private static void printOuter(OuterAndFill.Material mat, int i) throws IOException
  {
    ArrayList<Double> x = new ArrayList();
    ArrayList<Double> y = new ArrayList();
    ArrayList<Double> e = new ArrayList();
    
    x.clear();
    y.clear();
    e.clear();
    
    e.add(Double.valueOf(deep));
    
    int thickness = i > bottom_layers ? top_thickness : bottom_thickness;
    
    double curr_base = (total_num_layers - i) / total_num_layers * centre_to_side_length;
    
    ArrayList<Double> t = new ArrayList();
    
    for (int j = 0; j <= thickness - 1; j++) {
      t.clear();
      for (double d = i * twist_angle * 3.141592653589793D / 180.0D; d <= 6.283185307179586D + i * twist_angle * 3.141592653589793D / 180.0D; 
          d = d + 6.283185307179586D / side_count) {
        t.add(Double.valueOf(d));
      }
      
      for (int index = 0; index < t.size(); index++)
      {
        x.add(Double.valueOf(x_center + (curr_base - j * Math.sqrt(3.0D) * spacing) * Math.cos(((Double)t.get(index)).doubleValue())));
        y.add(Double.valueOf(y_center + (curr_base - j * Math.sqrt(3.0D) * spacing) * Math.sin(((Double)t.get(index)).doubleValue())));
      }
    }
    




    for (int k = 0; k <= thickness - 1; k++) {
      for (int l = 1; l <= t.size() - 1; l++) {
        double distance = Math.sqrt(Math.pow(((Double)x.get(k * t.size() + l)).doubleValue() - ((Double)x.get(k * t.size() + l - 1)).doubleValue(), 2.0D) + 
          Math.pow(((Double)y.get(k * t.size() + l)).doubleValue() - ((Double)y.get(k * t.size() + l - 1)).doubleValue(), 2.0D));
        e.add(Double.valueOf(unit_E_2 * distance));
      }
    }
    

    for (int k = 1; k < e.size(); k++) {
      e.set(k, Double.valueOf(((Double)e.get(k - 1)).doubleValue() + ((Double)e.get(k)).doubleValue()));
    }
    
    for (int k = 1; k <= thickness - 1; k++) {
      ArrayList<Double> e2 = new ArrayList();
      for (int index = 1; index <= k * t.size(); index++) {
        e2.add((Double)e.get(index - 1));
      }
      e2.add((Double)e.get(k * t.size() - 1));
      for (int index = k * t.size() + 1; index <= e.size(); index++) {
        e2.add((Double)e.get(index - 1));
      }
      
      e = e2;
    }
    
    double z = i * layer_height + bed_z;
    

    if (z < z_lift) {
      out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f  F%4.2f\n", new Object[] { x.get(0), y.get(0), Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
      out.write(String.format("G01 Z%4.2f  F%4.2f\n", new Object[] { Double.valueOf(z), Double.valueOf(travel_speed) }));
    } else {
      out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f  F%4.2f\n", new Object[] { x.get(0), y.get(0), Double.valueOf(z), Double.valueOf(travel_speed) }));
    }
    
    double e_last = 0.0D;
    
    for (int j = 1; j <= x.size(); j++)
    {
      out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f  F%4.2f E%4.2f\n", new Object[] { x.get(j - 1), y.get(j - 1), Double.valueOf(z), 
        Double.valueOf(print_speed), e.get(j - 1) }));
      e_last = ((Double)e.get(j - 1)).doubleValue();
    }
    
    if (e_last != 0.0D) {
      out.write(String.format("G01 F%4.2f E%4.2f\n", new Object[] { Double.valueOf(travel_speed), Double.valueOf(e_last - retraction) }));
    }
    if (z < z_lift) {
      out.write(String.format("G01 Z%4.2f  F%4.2f\n", new Object[] { Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
    }
    deep = ((Double)e.get(e.size() - 1)).doubleValue();
  }
  
  private static void printFill(OuterAndFill.Material mat, int i) throws IOException
  {
    ArrayList<Double> x = new ArrayList();
    ArrayList<Double> y = new ArrayList();
    ArrayList<Double> e = new ArrayList();
    
    x.clear();
    y.clear();
    e.clear();
    
    e.add(Double.valueOf(deep));
    
    int thickness = i > bottom_layers ? top_thickness : bottom_thickness;
    
    if (i == 0) {
      thickness = 0;
    }
    ArrayList<Double> t = new ArrayList();
    
    double curr_base = (total_num_layers - i) / total_num_layers * centre_to_side_length;
    double laps = curr_base / spacing / 2.0D;
    

    for (int j = thickness; j <= laps; j++) {
      t.clear();
      for (double d = i * twist_angle * 3.141592653589793D / 180.0D; d <= 6.283185307179586D + i * twist_angle * 3.141592653589793D / 180.0D; 
          d = d + 6.283185307179586D / side_count) {
        t.add(Double.valueOf(d));
      }
      
      for (int index = 0; index < t.size(); index++) {
        x.add(Double.valueOf(x_center + (curr_base - j * Math.sqrt(3.0D) * spacing) * Math.cos(((Double)t.get(index)).doubleValue())));
        y.add(Double.valueOf(y_center + (curr_base - j * Math.sqrt(3.0D) * spacing) * Math.sin(((Double)t.get(index)).doubleValue())));
      }
    }
    





    for (int k = 0; k <= laps - thickness; k++) {
      for (int l = 1; l < t.size(); l++) {
        double dist = Math.sqrt(Math.pow(((Double)x.get(k * t.size() + l)).doubleValue() - ((Double)x.get(k * t.size() + l - 1)).doubleValue(), 2.0D) + 
          Math.pow(((Double)y.get(k * t.size() + l)).doubleValue() - ((Double)y.get(k * t.size() + l - 1)).doubleValue(), 2.0D));
        e.add(Double.valueOf(unit_E_1 * dist));
      }
    }
    

    for (int k = 1; k < e.size(); k++) {
      e.set(k, Double.valueOf(((Double)e.get(k - 1)).doubleValue() + ((Double)e.get(k)).doubleValue()));
    }
    
    for (int k = 1; k <= laps - thickness; k++) {
      ArrayList<Double> e2 = new ArrayList();
      for (int index = 1; index <= k * t.size(); index++) {
        e2.add((Double)e.get(index - 1));
      }
      e2.add((Double)e.get(k * t.size() - 1));
      for (int index = k * t.size() + 1; index <= e.size(); index++) {
        e2.add((Double)e.get(index - 1));
      }
      
      e = e2;
    }
    
    double z = i * layer_height + bed_z;
    
    if ((x.size() > 0) && (y.size() > 0)) {
      if (z < z_lift) {
        out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n", new Object[] { x.get(0), y.get(0), Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
        out.write(String.format("G01 Z%4.2f  F%4.2f\n", new Object[] { Double.valueOf(z), Double.valueOf(travel_speed) }));
      } else {
        out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f F%4.2f\n", new Object[] { x.get(0), y.get(0), Double.valueOf(z), Double.valueOf(travel_speed) }));
      }
    }
    
    double e_last = 0.0D;
    
    for (int j = 1; j <= x.size(); j++)
    {
      out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f  F%4.2f E%4.2f\n", new Object[] { x.get(j - 1), y.get(j - 1), Double.valueOf(z), 
        Double.valueOf(print_speed), e.get(j - 1) }));
      e_last = ((Double)e.get(j - 1)).doubleValue();
    }
    

    if (e_last != 0.0D) {
      out.write(String.format("G01 F%4.2f E%4.2f\n", new Object[] { Double.valueOf(travel_speed), Double.valueOf(e_last - retraction) }));
    }
    if (z < z_lift) {
      out.write(String.format("G01 Z%4.2f  F%4.2f\n", new Object[] { Double.valueOf(z_lift), Double.valueOf(travel_speed) }));
    }
    deep = ((Double)e.get(e.size() - 1)).doubleValue();
  }
  
  private static String pick1() {
    String s = "G01 X2.5 Y30 Z7 F4000 \nG01 Y10 Z7 \nG01 Y10 Z10 \nG01 Y1 Z10 \nG01 Y1 Z14 \nG01 Y6 Z24 \nG01 Y30 Z24 \n";
    





    return s;
  }
  
  private static String drop1() {
    String s = "G01 X2.5 Y30 Z24 E0.0 F4000 \nG01 Y5 Z24 \nG01 Y2 Z14 \nG01 Y2 Z7 \nG01 Y30 Z7 \n";
    



    return s;
  }
  
  private static String pick2() {
    String s = "G01 X87.5 Y30 Z7 F10000 \nG01 Y10 Z7 \nG01 Y10 Z10 \nG01 Y1 Z10 \nG01 Y1 Z14 \nG01 Y6 Z24 \nG01 Y30 Z24 \n";
    





    return s;
  }
  
  private static String drop2() {
    String s = "G01 X87.5 Y30 Z24 E0.0 F4000 \nG01 Y5 Z24 \nG01 Y2 Z14 \nG01 Y2 Z7 \nG01 Y30 Z7 \n";
    



    return s; }
  
  static class Material { private double deep;
    
    Material() {}
    private boolean turn = false;
    
    public static Material pickMaterial(Material one, Material two) throws IOException {
      if (turn) {
        OuterAndFill.out.write(OuterAndFill.access$0());
        return one;
      }
      OuterAndFill.out.write(OuterAndFill.access$1());
      return two;
    }
    
    public static void dropMaterial(Material one, Material two) throws IOException
    {
      if (turn) {
        OuterAndFill.out.write(OuterAndFill.access$2());
        turn = false;
      }
      else {
        OuterAndFill.out.write(OuterAndFill.access$3());
        turn = false;
      }
    }
    
    public static void initialDump(Material mat, double load_depth, double initial_dump_speed, double[] dump) throws IOException
    {
      OuterAndFill.out.write(String.format("G01 X%4.2f Y%4.2f Z%4.2f\n", new Object[] { Double.valueOf(dump[0]), Double.valueOf(dump[1]), Double.valueOf(dump[2] + OuterAndFill.z_lift) }));
      OuterAndFill.out.write(String.format("G01 Z%4.2f\n", new Object[] { Double.valueOf(dump[2]) }));
      OuterAndFill.out.write(String.format("G01 F%4.2f E%4.2f\n", new Object[] { Double.valueOf(initial_dump_speed), Double.valueOf(load_depth) }));
      OuterAndFill.out.write(String.format("G01 F%4.2f E%4.2f\n", new Object[] { Double.valueOf(initial_dump_speed), Double.valueOf(load_depth - OuterAndFill.retract_after_dump) }));
      OuterAndFill.out.write(String.format("G01 Z%4.2f F%4.2f\n", new Object[] { Double.valueOf(dump[2] + OuterAndFill.z_lift), Double.valueOf(OuterAndFill.travel_speed) }));
      deep += load_depth;
      turn = true;
    }
  }
}
