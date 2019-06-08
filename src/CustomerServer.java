import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class CustomerServer extends Thread {
    private ServerSocket listenSocket;

    public static void main(String args[]) {
        new CustomerServer();
    }

    private CustomerServer() {
        // Replace 97xx with your port number
        int port = 9705;
        		//44444;
        try {
            listenSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Exception creating server socket: " + e);
            System.exit(1);
        }

        System.out.println("LOG: Server listening on port " + port);
        this.start();
    }

    /**
     * run()
     * The body of the server thread. Loops forever, listening for and
     * accepting connections from clients. For each connection, create a
     * new Conversation object to handle the communication through the
     * new Socket.
     */

    public void run() {
        try {
            while (true) {
                Socket clientSocket = listenSocket.accept();

                System.out.println("LOG: Client connected");

                // Create a Conversation object to handle this client and pass
                // it the Socket to use.  If needed, we could save the Conversation
                // object reference in an ArrayList. In this way we could later iterate
                // through this list looking for "dead" connections and reclaim
                // any resources.
                new Conversation(clientSocket);
            }
        } catch (IOException e) {
            System.err.println("Exception listening for connections: " + e);
        }
    }
}

/**
 * The Conversation class handles all communication with a client.
 */
class Conversation extends Thread {

    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // Where JavaCustXX is your database name
    private static final String URL = "jdbc:mysql://courses:3306/JavaCust05";

    String flag; 
    private Statement getAllStatement = null;
    private PreparedStatement addStatement = null;
    private PreparedStatement deleteStatement = null;
    private PreparedStatement updateStatement = null;
    Connection connection = null;
    private ResultSet rs = null;
    

    /**
     * Constructor
     *
     * Initialize the streams and start the thread.
     */
    Conversation(Socket socket) {
        clientSocket = socket;

        try {
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            System.out.println("LOG: Streams opened-server");
        } catch (IOException e) {
            try {
                clientSocket.close();
            } catch (IOException e2) {
                System.err.println("Exception closing client socket: " + e2);
            }

            System.err.println("Exception getting socket streams: " + e);
            return;
        }
            System.out.println("LOG: Connected to database");
/*
        } catch (SQLException e) {
            System.err.println("Exception connecting to database manager: " + e);
            return;}
       // } catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}*/

        // Start the run loop.
        System.out.println("LOG: Connection achieved, starting run loop");
        this.start();
    }

    /**
     * run()
     *
     * Reads and processes input from the client until the client disconnects.
     */
    public void run() {
        System.out.println("LOG: Thread running");

        try {
            while (true) {
                // Read and process input from the client.

                	Customer cust = (Customer) in.readObject();
                	System.out.println("Client says: "+cust.toString());
                	flag = cust.getFlag();
                	System.out.println("Flag: "+flag);
                    System.out.println("LOG: Trying to create database connection");
                    connection = DriverManager.getConnection(URL);

                    // Create your Statements and PreparedStatements here
                    switch(flag) {
                    case "getAll":
                    	handleGetAll();
                        break;
                    case "add":
                    	handleAdd(cust);
                    	break;
                    case "update":
                    	handleUpdate(cust);
                    	break;
                    case "delete":
                    	handleDelete(cust);
                    	break;
                    }
            }
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Exception connecting to database manager: " + e);
            return;
		}

        finally {
            try {
                clientSocket.close();   
                //connection.close();
                out.close();
                in.close();
                
            } catch (IOException e) {
                System.err.println("Exception closing client socket: " + e);
            }
        }
    }

    private void handleGetAll() {
    	ArrayList<Customer> custList = new ArrayList<Customer>();
    	try {
    		getAllStatement=connection.createStatement();
            rs=getAllStatement.executeQuery("SELECT * from customer");
            System.out.println(rs.getFetchSize());
            while(rs.next()) {
            	custList.add(new Customer(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)));
            }

			out.writeObject(custList);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
		}
    	
    }

    private void handleAdd(Customer cust) {
    	
    	System.out.println("Insert from switch");
    	try {
			addStatement=connection.prepareStatement("INSERT into customer values(?,?,?,?)");
			addStatement.setString(1, cust.getName());
	    	addStatement.setString(2, cust.getSsn());
	    	addStatement.setString(3, cust.getAddress());
	    	addStatement.setString(4, cust.getZipCode());
	    	addStatement.executeUpdate();
	    	
	    	out.writeObject("Record added");
	    	out.flush();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			try {
				out.writeObject("Duplicate Record for SSN");
				out.flush();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

    private void handleDelete(Customer cust) {
    
    	try {
			deleteStatement=connection.prepareStatement("DELETE from customer where ssn = ?");
			deleteStatement.setString(1, cust.getSsn());
	    	int result = deleteStatement.executeUpdate();
	    	System.out.println((result));
	    	if(result!=0)
	    	{
		    	out.writeObject("Record deleted");
		    	out.flush();
	    	}
	    	else
	    	{
	    		out.writeObject("Unable to delete record");
				out.flush();
	    	}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    }

    private void handleUpdate(Customer cust) {
    			try {
					updateStatement=connection.prepareStatement("UPDATE customer SET address = ? where ssn=?");
					updateStatement.setString(1, cust.getAddress());
                	updateStatement.setString(2, cust.getSsn());
                	int result = updateStatement.executeUpdate();
                	if(result!=0)
                	{
	                   	out.writeObject("Record updated");
	        	    	out.flush();
                	}
                	else
                	{
                		out.writeObject("Unable to update address");
						out.flush();
                	}
        	
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                    	
    }
}