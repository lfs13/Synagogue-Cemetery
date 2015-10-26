import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class mainWindow extends JPanel
{
  private static DisplayPanel dp;
  private JPanel titlePanel;
  private JLabel mainHeading = new JLabel("Beth Shalom Synagogue");
  private JLabel subHeading = new JLabel("Cemetery Management System");
  private JPanel createPanel;
  private JButton createButton;
  private JPanel namePanel;
  private JLabel nameLabel;
  private JTextField nameField;
  private JButton nameButton;
  private JPanel plotPanel;
  private JLabel plotLabel;
  private JTextField plotField;
  private JButton plotButton;

  public mainWindow(DisplayPanel display)
  {
    dp = display;
    //Setup SearchPanel
    setLayout(new GridLayout(4, 1));

    //Deal with titlePanel
    titlePanel = new JPanel(new FlowLayout());
    Color panelColor = new Color(153, 204, 255); //initialize main color
    titlePanel.setBackground(panelColor); //set color of title panel
    Font mainHeadingFont = new Font("Serif", Font.PLAIN, 24); //font for the main heading
    mainHeading.setFont(mainHeadingFont); // set the font of the main heading
    titlePanel.add(mainHeading);
    Font subHeadingFont = new Font("Serif", Font.BOLD, 32); //font for the sub heading
    subHeading.setFont(subHeadingFont); // set the font of the sub heading
    titlePanel.add(subHeading);
    add(titlePanel);

    //Initialize createPanel
    createPanel = new JPanel(new GridLayout(1, 1));
    createPanel.setBackground(panelColor);// set create panel color
    Font mainFont = new Font("Serif", Font.PLAIN, 20); //create main font for buttons, etc
    createButton = new JButton("Create New Entry");
    createButton.addActionListener(new createListener());
    createButton.setFont(mainFont); //set create button font
    createPanel.add(createButton);
    add(createPanel);
    createPanel.setVisible(true);

    //Initialize namePanel
    namePanel = new JPanel(new GridLayout(1, 3));
    namePanel.setBackground(panelColor); // set name panel color
    nameLabel = new JLabel("Search By Name:");
    nameLabel.setFont(mainFont);//set name label font
    nameLabel.setHorizontalAlignment(SwingConstants.CENTER); //center name label
    nameField = new JTextField();
    nameButton = new JButton("Search!");
    nameButton.setFont(mainFont); // set name button font
    nameButton.addActionListener(new nameListener());
    namePanel.add(nameLabel);
    namePanel.add(nameField);
    namePanel.add(nameButton);
    add(namePanel);
    namePanel.setVisible(true);

    //Initialize plotPanel
    plotPanel = new JPanel(new GridLayout(1, 3));
    plotPanel.setBackground(panelColor);//set plot panel color
    plotLabel = new JLabel("Search By Plot:");
    plotLabel.setFont(mainFont);//set plot label font
    plotLabel.setHorizontalAlignment(SwingConstants.CENTER); //center plot label
    plotField = new JTextField();
    plotButton = new JButton("Search!");
    plotButton.setFont(mainFont); // set plot button font
    plotButton.addActionListener(new plotListener());
    plotPanel.add(plotLabel);
    plotPanel.add(plotField);
    plotPanel.add(plotButton);
    add(plotPanel);
    plotPanel.setVisible(true);
  }

  class createListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
      newEntry entry = new newEntry();
      entry.setVisible(true);
    }
  }

  /*
  Searching by name
  */
  class nameListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
      String fullName = nameField.getText();
      fullName = fullName.toLowerCase();

      if(fullName.contains(" "))
      {
        // Split name if contains spaces - has a first and last name
        String[] splitStr = fullName.split("\\s");

        //set separate strings for first and last name
        String firstName = splitStr[0];
        String lastName = splitStr[1];

        //clean up input to make first char of string uppercase
        firstName = cleanUp(firstName);
        lastName = cleanUp(lastName);

        try // Both first and last name match entry in database
        {
          queryDb("SELECT * FROM PLOTS WHERE DECEASED_FNAME like \'" + firstName + "\' AND DECEASED_LNAME like \'" + lastName + "\'");
        }
        catch (SQLException e)
        {
          e.printStackTrace();
        }
      }
      else // Is only a first or only a last name
      {
        //clean up input to make first char of string uppercase
        fullName = cleanUp(fullName);

        try // Matches entry's first or last name field in database
        {
          queryDb("SELECT * FROM PLOTS WHERE DECEASED_FNAME like \'" + fullName + "\' OR DECEASED_LNAME like \'" + fullName + "\'");
        }
        catch (SQLException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  /*
  Searching by plot number
  */
  class plotListener implements ActionListener
  {
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
      String plot = plotField.getText();
      int plotInt = Integer.parseInt(plot); // Read in integer plot number
      try // Search for plot number
      {
        queryDb("SELECT * FROM PLOTS WHERE PLOT_NUMBER = " + plotInt + "");
      }
      catch (SQLException e)
      {
        e.printStackTrace();
      }
    }
  }

  /*
  A ResultSet is table of data representing a database.
  Generated by executing a statement that queries the database.

  isEmpty called on an ResultSet will return true if the query is unsuccessful
  will return true otherwise
   */
  public boolean isEmpty(ResultSet rs) throws java.sql.SQLException
  {
    return !rs.first();
  }

  /*
  hasMoreThanOneRow called on a ResultSet will return true if the result set
  contains multiple rows
  */
  public boolean hasMoreThanOneRow(ResultSet rs) throws java.sql.SQLException
  {
    return rs.first() && rs.next();
  }

  //accepts a string and returns a string that is formatted with the first character
  //caputalized and the rest lowercase
  public String cleanUp(String s)
  {
    char[] sc = s.toCharArray();
    sc[0] = Character.toUpperCase(sc[0]);
    s = new String(sc);
    return s;
  }

  /*
  helper method to query the DB
  takes a string that should be a properly formatted SQL statement
  */
  public static int queryDb(String query) throws java.sql.SQLException
  {
    dp.clear();
    int numEntries = 0; // Number of entries - used primarily for JUnit tests

    try
    {
      //establishes connection to our DB
      Class.forName("org.h2.Driver");
      //totally should not have password in plain text...
      Connection con = DriverManager.getConnection("jdbc:h2:./h2/cemetery;IFEXISTS=TRUE", "laboon", "bethshalom");
      Statement stmt = con.createStatement();

      //sql statement to collect all the data in a certain row where the first name
      //matches whatever entered into s
      ResultSet rs = stmt.executeQuery(query);

      //returns false if the query doesnt return anything
      //if (isEmpty(rs)) return false;

      //i is a counter for number of results in resultset
      int i = 0;

      while (rs.next())
      {
        numEntries++; // Increment number of entries

        //tokenizes the results of select statement into individual strings corresponding
        //to their columns
        String fname = rs.getString("DECEASED_FNAME");
        String lname = rs.getString("DECEASED_LNAME");
        String plotNum = rs.getString("PLOT_NUMBER");
        String date = rs.getString("DATE_DECEASED");

        dp.add(fname, lname, plotNum, date, i); //add the current result to the table data
        i++; //increment the row in the table so if multiple results returned, each is displayed in a new row
      }
      stmt.close();
      con.close();

      return numEntries;
    }
    catch (Exception er)
    {
      System.out.println(er.getMessage());
      return numEntries;
    }
  }

}