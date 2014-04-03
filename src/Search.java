import java.util.ArrayList;
import java.util.Hashtable;


public class Search {
	private ArrayList<Document> DOCLIST= new ArrayList<Document>();
	private  Hashtable<String, Double > QUERY= new  Hashtable<String, Double >();
	private static String [] TopK =new String[100];
	private static double [] TopKScore = new double[100];


    public Search(){
    	this.DOCLIST = ReadWriteData.getDOCLIST();
    	this.QUERY = ReadWriteData.getQUERY();
    }

	public void fastCosineScore(){
		System.out.println("fastCosineScore");
		double [] Scores = new double[DOCLIST.size()];
		
		for (int i=0; i< DOCLIST.size(); i++){
			//Scores[N] = 0
			Scores[i] = 0 ;
			// Add all tf-idf of terms in query to the scores of the documents.
			for (String term : QUERY.keySet()) 
			{ 
				if (DOCLIST.get(i).getLIST().containsKey(term))
					Scores[i] += DOCLIST.get(i).getLIST().get(term) * QUERY.get(term) ;
			}
		// Divide the Score to the length of document			
			Scores[i]=Scores[i]/DOCLIST.get(i).getLIST().size();
		}
		double max=0;
		int elementnr=0;		
		// Find the top K number of document (TopK) and its score (TopKScore)
		for (int i=0; i<100 ; i++){
			for (int j=0; j<DOCLIST.size() ; j++){
				if (max < Scores[j]){
					max = Scores[j];
					elementnr = j;
				}
			}
			TopK [i] = DOCLIST.get(elementnr).getLocation();
			TopKScore [i] = Scores [elementnr];
			Scores [elementnr]=0;
			max=0;
		}
		//for(int j=0; j< TopKScore.length; j++){
		//	System.out.println(TopK [j] + "         " + TopKScore[j]);
		//}
		ReadWriteData.setTopK(TopK);
		ReadWriteData.setTopKScore(TopKScore);	
	}
}
