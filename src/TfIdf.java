import java.util.ArrayList;
import java.util.Hashtable;


public class TfIdf {
	private ArrayList<Document> DOCLIST= new ArrayList<Document>();
	private  Hashtable<String, Double > QUERY= new  Hashtable<String, Double >();
    private Hashtable<String, Integer > TERMLIST = new Hashtable<String, Integer >();


    public TfIdf(){
    	this.DOCLIST = ReadWriteData.getDOCLIST();
    	this.QUERY = ReadWriteData.getQUERY();
    	this.TERMLIST = ReadWriteData.getTERMLIST();
    }
    
	// calculate the TF score and change it in matrix
	public void calculateTF(){
		System.out.println("calculateTF");
		double tf;
		for (int i=0; i< DOCLIST.size();i++){			
			for (String term : DOCLIST.get(i).getLIST().keySet()){
					tf = Math.log10(1 + DOCLIST.get(i).getLIST().get(term));
					DOCLIST.get(i).setLIST(term, tf);				
			}
		}
		for (String term : QUERY.keySet()){
				tf = 1 + Math.log10(QUERY.get(term));
				QUERY.put(term, tf);
			
		}
	}

	// calculate the IDF score and multiply it in matrix
	public void multiplyIDF(){
		System.out.println("multiplyIDF");
		double tf, inversedf, IDF ;
		for (String term: TERMLIST.keySet()){
			inversedf = (DOCLIST.size())/TERMLIST.get(term);
			IDF = Math.log10(inversedf);
			if(QUERY.containsKey(term)){
				for (int j=0; j< DOCLIST.size();j++){
					if (DOCLIST.get(j).getLIST().containsKey(term)){
						tf = DOCLIST.get(j).getLIST().get(term);
						DOCLIST.get(j).setLIST(term, IDF * tf);
					}
				}		
			
				tf = QUERY.get(term);
				QUERY.put(term, IDF *tf);
			}
		}		
	}
	
}
