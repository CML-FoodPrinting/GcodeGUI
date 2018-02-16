import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

public class PyramidWindowUI extends JFrame implements java.awt.event.ActionListener
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
HashMap<String, JPanel> panels;
  JRadioButton one;
  JRadioButton two;
  JRadioButton three;
  JTabbedPane tabbedPane;
  
  public PyramidWindowUI(String title)
  {
    super(title); //create a frame with title
    initialize(); 
    pack(); // adjusting frame size 
    setVisible(true); //make fram visible
  }
  
  public void initialize()
  {
    panels = new HashMap<String, JPanel>(); //create a new Hashmap of Panels
    tabbedPane = new JTabbedPane(); //create a new panel of tabs
    
    //create label for number of materials
    JLabel num_materials = new JLabel("Number of materials", 2);
    

    JPanel radioPanel = new JPanel();// create a new panel for radio?
    
    //create 3 new buttons and add them to radioPanel
    one = new JRadioButton("1");
    two = new JRadioButton("2");
    three = new JRadioButton("3");
    radioPanel.setLayout(new GridLayout(1, 3)); //gridlayout(low, columns)
    radioPanel.add(one); 
    radioPanel.add(two);
    radioPanel.add(three);
    
    //The 3 buttons on radioPnel listen for inputs(user selection)
    // If the user clicks on one of these 3 buttons, the actionPerformed() method gets invoked
    one.addActionListener(this);
    two.addActionListener(this);
    three.addActionListener(this);
    
    //create a new panel, p,  with 2-column gridlayout
    //this panel contains the "number of materials" label 
    //and also 3 select buttons
    JPanel p = new JPanel(new GridLayout(0, 2)); 
    panels.put("Materials", p); //hash the p panel
    tabbedPane.addTab("Materials", p); //add panel p to our panel of tabs 
    ((JPanel)panels.get("Materials")).add(num_materials);
    ((JPanel)panels.get("Materials")).add(radioPanel);
    
    add(tabbedPane); //add tabs
  }
  

  //opens a new window based on user's material selection. 
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource() == one)
    {
      PyrWindow wd = new PyrWindow("Pyramid Gcode Generator for single material", 1);
      wd.setDefaultCloseOperation(3);
    }
    else if (e.getSource() == two)
    {
      PyrWindow wd = new PyrWindow("Pyramid Gcode Generator for single material", 2);
      wd.setDefaultCloseOperation(3);
    }
    else if (e.getSource() == three)
    {
      PyrWindow wd = new PyrWindow("Pyramid Gcode Generator for single material", 3);
      wd.setDefaultCloseOperation(3);
    }
  }
}
