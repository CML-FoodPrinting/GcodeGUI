import java.io.File;
import javax.swing.filechooser.FileFilter;

public class FoodFilter extends FileFilter
{
  public FoodFilter() {}
  
  public boolean accept(File f)
  {
    if (f.getName().endsWith(".food")) return true;
    return false;
  }
  
  public String getDescription()
  {
    return "Food parameter files (.food)";
  }
}
