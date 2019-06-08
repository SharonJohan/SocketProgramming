/************************************************************************************
 CSCI 470 - Assignment 6 - Spring 2019

Progammers:  Sharon Thomas (Z1833666)
						Sudheeshna Devarapalli (Z1840147)
						Priyanjani Chandra (Z1864520)
Section: 1
Date Due: April 11, 2019
Purpose: implement both the client of a cooperating client-server database
system.
**************************************************************************************/
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;

public class CustomerClient extends JFrame implements ActionListener {

    // GUI components
    private JButton connectButton = new JButton("Connect");
    private JButton getAllButton = new JButton("Get All");
    private JButton addButton = new JButton("Add");
    private JButton deleteButton = new JButton("Delete");
    private JButton updateButton = new JButton("Update Address");
    private JLabel nameLabel, ssnLabel,addLabel, zipLabel,outputLabel;
    private JTextField nameTxtField, ssnTxtField, addTxtField, zipTxtField;
    private JTextArea custArea = new JTextArea(10,60);

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private static final long serialVersionUID = 1L;

    /***
    Function: initComponents 
    Use: initializes all the GUI components with the required size and texts
    Parameters: Nothing. 
    Returns: Nothing.
    ***/
	public void initComponents()
	{
		JPanel info    = new JPanel(new GridLayout(2,4,2,2));   // panel for the labels and textboxes
	    info.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	    Panel buttons = new Panel();   // panel for the buttons
	    Panel txtarea = new Panel();
	    outputLabel = new JLabel("Client started");
	    nameLabel = new JLabel("Name: ");
		nameTxtField = new JTextField(5);
		nameTxtField.setHorizontalAlignment(JTextField.RIGHT);
		nameTxtField.setFont(new Font("Bookman Old Style", Font.PLAIN, 13));
		ssnLabel = new JLabel("SSN: ");
		ssnTxtField = new JTextField();
		ssnTxtField.setHorizontalAlignment(JTextField.RIGHT);
		ssnTxtField.setFont(new Font("Bookman Old Style", Font.PLAIN, 13));
		addLabel = new JLabel("Address: ");
		addTxtField = new JTextField();
		addTxtField.setHorizontalAlignment(JTextField.RIGHT);
		addTxtField.setFont(new Font("Bookman Old Style", Font.PLAIN, 13));
		zipLabel = new JLabel("Zip Code: ");
		zipTxtField = new JTextField();
		zipTxtField.setHorizontalAlignment(JTextField.RIGHT);
		zipTxtField.setFont(new Font("Bookman Old Style", Font.PLAIN, 13));
		JScrollPane scrollPane = new JScrollPane(custArea, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    //info.setLayout(new GridLayout(2,4,2,2));  // methods of the info panel

	    info.add(nameLabel); 
	    info.add(nameTxtField);
	    info.add(ssnLabel); 
	    info.add(ssnTxtField); 
	    info.add(addLabel); 
	    info.add(addTxtField);
	    info.add(zipLabel); 
	    info.add(zipTxtField);

	    buttons.setLayout(new FlowLayout());  // methods of the buttons panel
	    buttons.add(connectButton); buttons.add(getAllButton);
	    buttons.add(addButton); buttons.add(deleteButton); buttons.add(updateButton);
	    getAllButton.setEnabled(false);
	    addButton.setEnabled(false);
	    deleteButton.setEnabled(false);
	    updateButton.setEnabled(false);

	    txtarea.setLayout(new BorderLayout());
	    txtarea.setEnabled(false);
	    txtarea.add(BorderLayout.PAGE_START,outputLabel);
	    txtarea.add(BorderLayout.PAGE_END,scrollPane);
	    setLayout(new BorderLayout());       // methods of the frame
	    add(BorderLayout.CENTER,buttons);       
	    add(BorderLayout.PAGE_START,info);
	    add(BorderLayout.PAGE_END,txtarea);
	}
	
    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            CustomerClient client = new CustomerClient();
            client.createAndShowGUI();
        });
    }

    private CustomerClient() {
        super("Customer Database");
    }

    private void createAndShowGUI() {
    	 		
    			initComponents();
    	 		
    	 		//adding listeners to the buttons
    	 		connectButton.addActionListener(this);
    	 		getAllButton.addActionListener(this);
    	 		addButton.addActionListener(this);
    	 		deleteButton.addActionListener(this);
    	 		updateButton.addActionListener(this);

    			// Display the window.
    			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    			pack();
    			setVisible(true);		
    }

    /***************************************************************
    Event: actionPerformed 
    Use: An action event occurs, whenever an action is performed by the user. 
    	The result is that an actionPerformed message is sent to all action listeners 
    	that are registered on the buttons in GUI
    Parameters: ActionEvent. 
    Returns: Nothing.
    ***************************************************************/
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Connect")) {
            connect();
        } else if (e.getActionCommand().equals("Disconnect")) {
            disconnect();
        } else if (e.getSource() == getAllButton) {
            handleGetAll();
        } else if (e.getSource() == addButton) {
            handleAdd();
        } else if (e.getSource() == updateButton) {
            handleUpdate();
        } else if (e.getSource() == deleteButton) {
            handleDelete();
        }
    }

    /*******************************************************
    Function: connect 
    Use: Called when connect button is clicked and 
    			Opens a socket to connect to the server socket. 
    Parameters: Nothing. 
    Returns: Nothing.
    *********************************************************/
    private void connect() {
        try {
            // Replace 97xx with your port number	//"localhost", 44444
            socket = new Socket("turing.cs.niu.edu", 9705);

            System.out.println("LOG: Socket opened");

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("LOG: Streams opened-client");

            connectButton.setText("Disconnect");
            
            // Enable buttons
            getAllButton.setEnabled(true);
    	    addButton.setEnabled(true);
    	    deleteButton.setEnabled(true);
    	    updateButton.setEnabled(true);

        } catch (UnknownHostException e) {
            System.err.println("Exception resolving host name: " + e);
        } catch (IOException e) {
            System.err.println("Exception establishing socket connection: " + e);
        }
    }

    /*******************************************************
    Function: disconnect 
    Use: Called when disconnect button is clicked.
    		Closes the socket and as well as input,output object streams that were opened
    Parameters: Nothing. 
    Returns: Nothing.
    *********************************************************/
    private void disconnect() {
        connectButton.setText("Connect");
        
        // Disable buttons
        getAllButton.setEnabled(false);
	    addButton.setEnabled(false);
	    deleteButton.setEnabled(false);
	    updateButton.setEnabled(false);


        try {
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            System.err.println("Exception closing socket: " + e);
        }
    }

    /*******************************************************
    Function: handleGetAll 
    Use: handles all the rows returned from the server by adding them to an arraylist.
    Parameters: Nothing. 
    Returns: Nothing.
    *********************************************************/
    @SuppressWarnings("unchecked")
	private void handleGetAll() {
    	
    	System.out.println("Inside handleDelete in client");

    	Customer cust = new Customer();
    	cust.setFlag("getAll");
    	custArea.setText("");
    	ArrayList<Customer> custList = new ArrayList<Customer>();  //arraylist to store all records returned from server
    	String records;
    	
    	try {			//sending an empty customer object.
			out.writeObject(cust);
			out.flush();
			
			custList=(ArrayList<Customer>) in.readObject();
			records = String.valueOf(custList.size());
			outputLabel.setText("Total Customer Records: " + records);
			System.out.println("Records: "+records);
			for(Customer customer:custList) {			//iterating the array list and appending each to the textarea on the GUI.
				custArea.append(customer.toString()+"\n");
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /*******************************************************
    Function: handleAdd
    Use: Passes the customer object with details of the customer that has to 
    		be added to the table, to the server. 
    Parameters: Nothing. 
    Returns: Nothing.
    *********************************************************/
    private void handleAdd() {
    	
    	String name,ssn,address,zipCode;
    	System.out.println("Inside handleAdd in client");
    	name= nameTxtField.getText();
    	ssn= ssnTxtField.getText();
    	address=addTxtField.getText();
    	zipCode=zipTxtField.getText();
    	custArea.setText("");
    	outputLabel.setText("");
    	String ssnExpression = "^\\d{3}- ?\\d{2}- ?\\d{4}$";
    	Pattern ssnPattern = Pattern.compile(ssnExpression);
    	Matcher ssnMatch = ssnPattern.matcher(ssn);
    	
    	String zipExpression = "^\\d{5}$";
    	Pattern zipPattern = Pattern.compile(zipExpression);
    	Matcher zipMatch = zipPattern.matcher(zipCode);
    	
    	//assert the validity of all 4 fields
    	if(name.equals("") || name.length()>20) {    		
			outputLabel.setText("Name should not be empty or no greater than 20 characters");
    	}
    	else if(ssn.equals("") || !(ssn.length()==11) || ssnMatch.matches()==false) {
    		outputLabel.setText("SSN should not be empty and should be equal to 11 characters with xxx-xx-xxxx format");
    	}
    	else if(address.equals("") || address.length()>40) {
    		outputLabel.setText("Address should not be empty or no greater than 40 characters");
    	}
    	else if(zipCode.equals("") || !(zipCode.length()==5)||zipMatch.matches()==false) {
    		outputLabel.setText("ZipCode should not be empty and should contain 5 digits"); 
    	}
    	else {
    		
    		Customer cust = new Customer(name,ssn,address,zipCode);
    		System.out.println("in handle add "+cust.getName());
    		try {
    			cust.setFlag("add");
				out.writeObject(cust);				
				out.flush();
				
				String output = (String)in.readObject();
				outputLabel.setText(output );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}	  	
    }

    /*******************************************************
    Function: handleDelete
    Use: Passes the customer object with details of the customer that has to 
    		be deleted from the table, to the server. 
    Parameters: Nothing. 
    Returns: Nothing.
    *********************************************************/
    private void handleDelete() {
    	
     	String ssn;
    	System.out.println("Inside handleDelete in client");
    	ssn= ssnTxtField.getText();
    	custArea.setText("");
    	outputLabel.setText("");
    	String ssnExpression = "^\\d{3}- ?\\d{2}- ?\\d{4}$";
    	Pattern ssnPattern = Pattern.compile(ssnExpression);
    	Matcher ssnMatch = ssnPattern.matcher(ssn);
    	
    	//check the format of SSN
    	if(ssn.equals("") || !(ssn.length()==11) || ssnMatch.matches()==false) {
			outputLabel.setText("SSN should not be empty and should be equal to 11 characters with xxx-xx-xxxx format");
    	}
    	
    	else {
    					//pass the object to the server, along with the delete flag
    		Customer cust = new Customer();
    		cust.setSsn(ssn);
    		try {
    			cust.setFlag("delete");
				out.writeObject(cust);
				
				out.flush();
				String output = (String)in.readObject();
				outputLabel.setText(output );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
    	}	
    	
    }

    /*******************************************************
    Function: handleUpdate
    Use: Pass the customer object with the details of the customer whose address
     		has to be updated in the customer table. 
    Parameters: Nothing. 
    Returns: Nothing.
    *********************************************************/
    private void handleUpdate() {
    	
    	String ssn,address;
    	System.out.println("Inside handleUpdate in client");
    	ssn= ssnTxtField.getText();
    	address=addTxtField.getText();
    	custArea.setText("");
    	outputLabel.setText("");
    	String ssnExpression = "^\\d{3}- ?\\d{2}- ?\\d{4}$";
    	Pattern ssnPattern = Pattern.compile(ssnExpression);
    	Matcher ssnMatch = ssnPattern.matcher(ssn);
    	
    	//check the validity of ssn
    	if(ssn.equals("") || !(ssn.length()==11) || ssnMatch.matches()==false) {
    		outputLabel.setText("SSN should not be empty and should be equal to 11 characters with xxx-xx-xxxx format"); 
    	}
    	//address field should not be empty and also length of address should not exceed 40 characters
    	else if(address.equals("") || address.length()>40) {
    		outputLabel.setText("Address should not be empty or greater than 40 characters"); 
    	}
    	
    	else {
    		//flush the customer object to the server
    		Customer cust = new Customer();
    		cust.setSsn(ssn);
    		cust.setAddress(address);
    		try {
    			cust.setFlag("update");
				out.writeObject(cust);
				
				out.flush();
				String output = (String)in.readObject();
				outputLabel.setText(output );
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	
    	}	
    }
}