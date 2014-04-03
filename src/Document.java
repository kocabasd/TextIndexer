import java.util.Hashtable;


public class Document {
	// String -> Term & Double for frequency
	private String location;
	private Hashtable<String, Double > LIST = new Hashtable<String, Double >();

	public Document(String location){
		this.location = location;
	}

	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public Hashtable<String, Double> getLIST() {
		return LIST;
	}
	public void setLIST(String term, double frequency) {
		this.LIST.put(term, frequency);
	}
	
	public void setLIST(Hashtable<String, Double> LIST) {
		this.LIST = LIST;
	}
}
