import java.io.Serializable;

public class Customer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	//data members of the class
	String name, ssn, address, zipCode,flag;
	
	//constructor
	public Customer(String name, String ssn, String address, String zipCode) {
		
		this.name=name;
		this.ssn=ssn;
		this.address=address;
		this.zipCode=zipCode;		
	}

	public Customer() {
		// TODO Auto-generated constructor stub
	}

	//getter and setter methods for data members of the Customer
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSsn() {
		return ssn;
	}

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String toString() {
		
		return name+":"+ssn+":"+address+":"+zipCode;
	}
	

}
