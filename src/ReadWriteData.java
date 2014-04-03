import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class ReadWriteData {
    final static Charset ENCODING = StandardCharsets.UTF_8;
    private static ArrayList<Document> DOCLIST= new ArrayList<Document>();
    private static ArrayList<String> TERMARRAY= new ArrayList<String>();    private static Hashtable<String, Integer > TERMLIST = new Hashtable<String, Integer >();
	private static Hashtable<String, Double > QUERY= new Hashtable<String, Double >();
    static String [] TopK =new String[100];
    static double [] TopKScore =new double[100];
    private Scanner x;
	private BufferedWriter y;

	public void ReadData(File FOLDER_NAME, Boolean Stem, Boolean bagOfWords){	
		listFilesForFolder(FOLDER_NAME);
		for (int j =0; j<DOCLIST.size(); j++){
			openFileToRead(DOCLIST.get(j).getLocation());
			try {
				readFile(j, Stem, bagOfWords);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void WriteData(String OUTPUT_NAME, String Topic, String Experiment){
		openFileToWrite(OUTPUT_NAME+Topic+ Experiment);
		writeFile(Topic, Experiment);
		closeFile();	
	}
 
	protected void listFilesForFolder(final File folderName) {
		for (final File fileEntry : folderName.listFiles()) {
			if (fileEntry.isDirectory()) {
				//System.out.println("Reading files under the folder "+folderName.getAbsolutePath()+fileEntry);
				listFilesForFolder(fileEntry);
			} 
			else {
				if (fileEntry.isFile()){					
					Document newDoc = new Document(fileEntry.getParent()+"\\"+fileEntry.getName());
					DOCLIST.add(newDoc);
				}
			}
  	   	}
	}

	protected void openFileToRead(String fileName){
		try {
			x = new Scanner(new File(fileName)); 
		}
		catch (Exception e){
			System.out.println("File could not find");
		}
	}

	protected void openFileToWrite(String fileName){
		try {
			FileWriter fstream = new FileWriter(fileName);
			y = new BufferedWriter(fstream);
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}
	
	protected void readIndex(String IndexLocation) throws IOException{
		openFileToRead(IndexLocation);
		String strLine;
		String[] temp;
		int docId = 0;
		if(x.hasNextLine()){
			strLine = x.nextLine();	
			temp = strLine.split(" ");
			for (int i=0; i < temp.length; i++)
				TERMARRAY.add(temp[i]);		
		}
		if(x.hasNextLine()){
			strLine = x.nextLine();
			temp = strLine.split(" ");			
			for (int i=0; i<temp.length; i++){
				Document newDoc = new Document(null);
				DOCLIST.add(newDoc);
				DOCLIST.get(i).setLocation(temp[i]);
			}
		}
		while (x.hasNextLine())   {			
			strLine = x.nextLine();
			temp = strLine.split(" ");
			for (int i=0; i<temp.length; i++){
				if(temp[i] != "0")
					DOCLIST.get(docId).setLIST(TERMARRAY.get(i),Double.parseDouble(temp[i]));
			}
			docId++;
		}

	}
	
	protected void ReadQuery(String fileName, Boolean Stem, Boolean bagOfWords) throws IOException{
		openFileToRead(fileName);
		String strLine;
		String[] temp;
		double count;
		while (x.hasNextLine())   {			  
			strLine = x.nextLine();
			strLine.trim();
			if (strLine.isEmpty()) 
				 break;			  
		}
		while (x.hasNextLine())   {
			strLine = x.nextLine();	
			if(Stem)
				strLine = Vocabulary.Stemming(strLine);
			temp = strLine.split(" ");
			for (int i=0; i < temp.length; i++){
				if (temp[i].equals("")) 
					continue;
				if(bagOfWords){
					if(!QUERY.containsKey(temp[i]))
						QUERY.put(temp[i],1.0);					
					else{
						count = QUERY.get(temp[i]) +1.00 ;
						QUERY.put(temp[i], count);
					}
				}
				else{
					if(!(temp.length <= i+1)){						
						if(!QUERY.containsKey(temp[i]+","+temp[i+1])){
							QUERY.put(temp[i]+","+temp[i+1],1.0);	
						}
						else{
							count = QUERY.get(temp[i]+","+temp[i+1]) +1.00 ;
							QUERY.put(temp[i]+","+temp[i+1], count);							
						}
					}
				}
							
			}
		}

	}

	protected void readFile(int docId, boolean Stem, boolean bagOfWords) throws IOException{	
		double count;
		String strLine;
		String[] temp;
		//Dont read the first a few line until the break line
		while (x.hasNextLine())   {			  
			strLine = x.nextLine();
			strLine.trim();
			if (strLine.isEmpty()) 
				 break;			  
		}
		// Read line and use Stemming function
		while (x.hasNextLine())   {
			strLine = x.nextLine();
			if(Stem)
				strLine = Vocabulary.Stemming(strLine);
			temp = strLine.split(" ");
			for (int i=0; i<temp.length; i++){
				if (temp[i].equals("")) 
					continue;
				//if it is first term
				if (TERMLIST.isEmpty()){
					if(bagOfWords){
						TERMLIST.put(temp[i], 1);
						TERMARRAY.add(temp[i]);
						DOCLIST.get(docId).setLIST(temp[i],1.0);
						
					}
					else{
						if(!(temp.length <= i+1)){
							TERMLIST.put(temp[i]+"," + temp[i+1], 1);
							TERMARRAY.add(temp[i]+"," + temp[i+1]);
							DOCLIST.get(docId).setLIST(temp[i]+"," + temp[i+1], 1.0);
						}
					}
				}
				// if not the first term, check in termlist if it exist or not
				else {
					if(bagOfWords){
						if(DOCLIST.get(docId).getLIST().containsKey(temp[i])){
							  count = DOCLIST.get(docId).getLIST().get(temp[i]) +1;
							  DOCLIST.get(docId).setLIST(temp[i], count);
						}
						else {						  
								  if(!TERMLIST.containsKey(temp[i])){
									  TERMARRAY.add(temp[i]);
									  TERMLIST.put(temp[i], 1);
								  }
								  else{
									  count = TERMLIST.get(temp[i])+1;
									  TERMLIST.put(temp[i], (int)count);
								  }
							  DOCLIST.get(docId).setLIST(temp[i], 1.0);		
						}
					}
					else{
						if(!(temp.length <= i+1)){
							if(DOCLIST.get(docId).getLIST().containsKey(temp[i]+"," + temp[i+1])){
								count = DOCLIST.get(docId).getLIST().get(temp[i]+"," + temp[i+1]) +1;
								DOCLIST.get(docId).setLIST(temp[i]+"," + temp[i+1], count);
							}
							else{
								if(!TERMLIST.containsKey(temp[i]+"," + temp[i+1])){
									  TERMARRAY.add(temp[i]+"," + temp[i+1]);
									  TERMLIST.put(temp[i]+"," + temp[i+1], 1);
								  }
								else{
									  count = TERMLIST.get(temp[i]+"," + temp[i+1])+1;
									  TERMLIST.put(temp[i]+"," + temp[i+1], (int)count);
								  }
								DOCLIST.get(docId).setLIST(temp[i]+"," + temp[i+1], 1.0);
							}
						}
					}
				}
			}
		}
	}
		
	public void writeFile(String Topic, String Experiment){	
		try {
			System.out.println("writeFile");
			String[] temp;
			String Score;
			for(int j=0; j<100; j++){
				y.write(Topic);
				y.write(" Q0 ");
				temp = TopK[j].split("\\\\");
				y.write(temp[3]);
				y.write("\\");
				y.write(temp[4]);	
				y.write(" "+(j+1)+" ");
				Score = Double.toString(TopKScore[j]) + "00000";
				y.write(Score.substring(0,5));	
				y.write(" group2-"+Experiment);
				if (j!=99)
					y.write("\n");
			}
		} 
		catch (IOException c) {
				System.err.format("IOException: %s%n", c);
		}
	}
		
	public void writeIndex(String OUTPUT_NAME){
		try {	
			openFileToWrite(OUTPUT_NAME);
			for (int termId=0; termId< TERMARRAY.size();termId++){
				y.write(TERMARRAY.get(termId));
				y.write(" ");
			}
			y.write("\n");
			for (int docId=0; docId< DOCLIST.size();docId++){
				y.write(DOCLIST.get(docId).getLocation());
				y.write(" ");
			}
			y.write("\n");
			for (int docId=0; docId< DOCLIST.size();docId++){
				for (int termId=0; termId< TERMARRAY.size();termId++){				
				
					if(DOCLIST.get(docId).getLIST().containsKey(TERMARRAY.get(termId))){
						y.write(Double.toString(DOCLIST.get(docId).getLIST().get(TERMARRAY.get(termId))));						
					}
					else {
						y.write("0");
					}
					y.write(" ");
				}
				y.write("\n");
			}
		} catch (IOException c) {
			System.err.format("IOException: %s%n", c);
		}
	}
		
	public void closeFile(){
		try {
			x.close();
			y.flush();
			y.close();
		} 
		catch (IOException c) {
			System.err.format("IOException: %s%n", c);
		}		
	}

	public static ArrayList<Document> getDOCLIST() {
		return DOCLIST;
	}

	public static void setDOCLIST(ArrayList<Document> dOCLIST) {
		DOCLIST = dOCLIST;
	}

	public static Hashtable<String, Double > getQUERY() {
		return QUERY;
	}

	public static void setQUERY(Hashtable<String, Double > qUERY) {
		QUERY = qUERY;
	}

	public static ArrayList<String> getTERMARRAY() {
		return TERMARRAY;
	}

	public static void setTERMLIST(ArrayList<String> tERMARRAY) {
		TERMARRAY = tERMARRAY;
	}

	public static String[] getTopK() {
		return TopK;
	}

	public static void setTopK(String[] topK) {
		TopK = topK;
	}

	public static double[] getTopKScore() {
		return TopKScore;
	}

	public static void setTopKScore(double[] topKScore) {
		TopKScore = topKScore;
	}

	public static Hashtable<String, Integer> getTERMLIST() {
		return TERMLIST;
	}

	public static void setTERMLIST(Hashtable<String, Integer> tERMLIST) {
		TERMLIST = tERMLIST;
	}

}