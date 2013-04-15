import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;


public class ReadWriteData {
	final static String TOPIC = "topic1";	
	final static String CLASS = "misc.forsale";
	final static String DOCUMENT_ID = "76057";
	final static String SIZE = "large";
    // if the constraints FrequencyUpperThreshold, FrequencyLowerThreshold are 0, 
    // the program will not use the FrequencyThreshold
	// large	FrequencyUpperThreshold = 0.0 FrequencyLowerThreshold = 0.0
	// medium 	FrequencyUpperThreshold = 100.0 FrequencyLowerThreshold = 2.0
	// small 	FrequencyUpperThreshold = 40.0	FrequencyLowerThreshold = 5.0
	public static double FrequencyUpperThreshold = 0.0 ;
	public static double FrequencyLowerThreshold = 0.0 ;	
	final static File FOLDER_NAME = new File("C:/20_newsgroups_subset/"+CLASS);
	final static String OUTPUT_ARRF_FILE_NAME = "C:\\20_newsgroups_subset\\TextIndexer"+" "+SIZE;	
	final static String OUTPUT_TXT_FILE_NAME = "C:\\20_newsgroups_subset\\"+SIZE+"_"+TOPIC+"_groupQ";	
    static String queryID = "C:\\20_newsgroups_subset\\"+CLASS+"\\"+DOCUMENT_ID;
    final static Charset ENCODING = StandardCharsets.UTF_8;
    static ArrayList<String> DOCLIST= new ArrayList<String>();
    static ArrayList<String> TERMLIST= new ArrayList<String>();
    static ArrayList<Integer> QUERY= new ArrayList<Integer>();
    static String [] TopK =new String[10];
    static double [] TopKScore =new double[10];
	static ArrayList<Double> IDF= new ArrayList<Double>();
    private Scanner x;
	private BufferedWriter y;
	private static ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
	
	public static void main(String... aArgs) throws IOException{
		ReadWriteData data = new ReadWriteData();
		DOCLIST.add("DOCID");
		data.listFilesForFolder(FOLDER_NAME);
		list.add(DOCLIST);
		for (int i =1; i<DOCLIST.size(); i++){
			//System.out.println(DOCLIST.get(i));
			data.openFileToRead(DOCLIST.get(i));
			data.readFile(i);
		}
		//System.out.println(list);
		data.calculateTF();
		data.frequencyThresholding();		
		data.multiplyIDF();
		//System.out.println(IDF);
		data.openFileToWrite(OUTPUT_ARRF_FILE_NAME+".arff");
		//to write ArrfFile(), you need to comment the 2 function below => data.openFileToWrite(OUTPUT_TXT_FILE_NAME+".txt");		data.writeFile();
		//data.writeArrfFile();
		data.fastCosineScore(queryID);
		data.openFileToWrite(OUTPUT_TXT_FILE_NAME+".txt");
		data.writeFile();
		data.closeFile();	
	}

   
	public void listFilesForFolder(final File folderName) {				
  	   for (final File fileEntry : folderName.listFiles()) {
	      if (fileEntry.isDirectory()) {
	        System.out.println("Reading files under the folder "+folderName.getAbsolutePath()+fileEntry);
	        listFilesForFolder(fileEntry);
	      } else {
	        if (fileEntry.isFile()) 
	        	DOCLIST.add(fileEntry.getParent()+"\\"+fileEntry.getName());
		  }
  	   }  	   
  	}

	public void openFileToRead(String fileName){
		try {
			x = new Scanner(new File(fileName)); 
		}
		catch (Exception e){
			System.out.println("File could not find");
		}
	}

	public void openFileToWrite(String fileName){
		try {
			FileWriter fstream = new FileWriter(fileName);
			y = new BufferedWriter(fstream);
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}
	
	public void readFile(int docId) throws IOException{	
	    int i,j,k, l;
	    double count;
	    boolean NewTerm=true;
		String strLine;
		String[] temp;
		String[] a;
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
			  strLine =Stemming(strLine);
			  temp = strLine.split(" ");
			  for (i=0;i<temp.length;i++){
				  if (temp[i].equals("")) continue;
				  NewTerm=true;
				  // if line contains apostrophe or comma, weka gives error. So change the words like below
				  if (temp[i].contains("'")){
					  if(temp[i].endsWith("'")){
						  a = temp[i].split("'");					 
						  temp[i] = "'";
						  for (l=0;l<a.length;l++){
							  	temp[i]+=  a[l];
						  		temp[i]+="\\'";
						  }
						  temp[i] += "'";						  
					  }
					  else{
						  a = temp[i].split("'");
						  temp[i] = "'";
						  for (l=0;l<a.length-1;l++){
							  	temp[i]+=  a[l];
						  		temp[i]+="\\'";
						  }
						  temp[i] +=a[a.length-1];
						  temp[i] += "'";
					  }
				  }
				  if (temp[i].contains(",")){
					  a = temp[i].split(",");					  
					  temp[i] = "'";
					  for (l=0;l<a.length;l++){
						  	temp[i]+=  a[l];
						  	if (l<a.length-1)
						  		temp[i]+="\\,";
					  }
					  temp[i] += "'";
				  }
				  //if it is first term
				  if (TERMLIST.isEmpty()){
					  TERMLIST.add(temp[i]);
					  list.add(new ArrayList<String>());
					  list.get(list.size()-1).add(temp[i]);
			  	  	  for (k=0; k < DOCLIST.size();k++)
			  	  		list.get(list.size()-1).add("0");
			  	  	  list.get(list.size()-1).set(docId,"1");			  	  	
				  }
				  // if not the first term, check in termlist if it exist or not
				  else {
					  for(j=0; j < TERMLIST.size(); j++){	
						  if(TERMLIST.get(j).contentEquals(temp[i])){
					  	  	  count = Double.parseDouble(list.get(j+1).get(docId));
					  	  	  count++;
					  	  	  list.get(j+1).set(docId,Double.toString(count));	
							  NewTerm = false;
							  break;
						  }
					  }
					  if(NewTerm) {					
						  TERMLIST.add(temp[i]);
						  list.add(new ArrayList<String>());
						  list.get(list.size()-1).add(temp[i]);
				  	  	  for (k=0; k < DOCLIST.size();k++)
				  	  		list.get(list.size()-1).add("0");
				  	  	  list.get(list.size()-1).set(docId,"1.0");						  
					  }					  
				  }	 
			  }
		  }	
	}
	
	public String Stemming(String input) throws IOException {
	    StringBuilder sb = new StringBuilder();
		Version matchVersion = Version.LUCENE_CURRENT;
		Analyzer analyzer = new StandardAnalyzer(matchVersion);
	    TokenStream stream = analyzer.tokenStream("field", new StringReader(input));
	    stream = new PorterStemFilter(stream);
	    // get the CharTermAttribute from the TokenStream
	    CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
	    try {
	      stream.reset();
	      while (stream.incrementToken()) {
	          if (sb.length() > 0) {
	              sb.append(" ");
	          }
	          sb.append(termAtt.toString());
	      }	      
	      stream.end();
	    }
	    finally {
	      stream.close();
	    }
	    return sb.toString();	    
	}

	// calculate the TF score and change it in matrix
	public void calculateTF(){
		double tf;		
		for (int i=1; i< list.size();i++){
			for (int j=1; j< list.get(0).size();j++){
				if (list.get(i).get(j)!= "0"){
					tf = 1 + Math.log(Double.parseDouble(list.get(i).get(j)));
					list.get(i).set(j,Double.toString(tf));
				}
			}
		}		
	}
	
	// to provide different nr of terms, using different frequency threshold
	public void frequencyThresholding(){
		double score=0;
		if (FrequencyUpperThreshold != 0 || FrequencyLowerThreshold !=0) {
			if (FrequencyUpperThreshold != 0) {
				for (int i=1; i< list.size();i++){
					for (int j=1; j< list.get(0).size();j++){
						score += Double.parseDouble(list.get(i).get(j));
					}
					if (FrequencyUpperThreshold < score){
						list.remove(i);
						TERMLIST.remove(i-1);
						i--;
					}
					score =0;
				}
			}
			if (FrequencyLowerThreshold != 0) {
				for (int i=1; i< list.size();i++){
					for (int j=1; j< list.get(0).size();j++){
						score += Double.parseDouble(list.get(i).get(j));
					}					
					if (FrequencyLowerThreshold > score){
						list.remove(i);
						TERMLIST.remove(i-1);
						i--;
					}
					score =0;
				}
			}
		}
	}
	

	// calculate the IDF score and multiply it in matrix
	public void multiplyIDF(){
		double df=0, tf, inversedf ;
		for (int i=1; i< list.size();i++){
			for (int j=1; j< list.get(0).size();j++){
				if (list.get(i).get(j)!= "0"){
					df++;
				}
			}
			inversedf = (DOCLIST.size()-1)/df;
			df=0;
			IDF.add(Math.log(inversedf));
		}
		for (int i=1; i< list.size();i++){
			for (int j=1; j< list.get(0).size();j++){
				if (list.get(i).get(j)!= "0"){
					tf = Double.parseDouble(list.get(i).get(j));
					list.get(i).set(j,Double.toString(IDF.get(i-1) * tf));
					
				}
			}
		}		
	}
	
	public void writeArrfFile(){
	
		try {
			y.write("@relation 20_newsgroups_subset\n\n");
			for (int i=0; i< list.get(0).size();i++){
				for (int j=0; j< list.size();j++){
					if (i==0){
						y.write("@attribute ");
						y.write(list.get(j).get(i));
						y.write(" real \n");
						if (j==list.size()-1){
							y.write("\n");
							y.write("@data\n");
						}
					}
					else{
						if (j==0){
							y.write(list.get(0).get(i).substring(list.get(0).get(i).length()-5));
						}
						else {
							y.write(list.get(j).get(i));
						}
						y.write(",");
					}
				}
				y.write("\n");
			}	
		} catch (IOException c) {
			System.err.format("IOException: %s%n", c);
		}
	}

	public void fastCosineScore(String queryID){
		double [] Scores =new double[DOCLIST.size()];
		double [] Length =new double[DOCLIST.size()];
		double count = 0;
		int Qid=0;

		for (int i=1; i< DOCLIST.size(); i++){
			Scores[i] =0 ;
			for (int j=0; j< TERMLIST.size(); j++){
				if (list.get(j+1).get(i) != "0"){
					count++;
					if (DOCLIST.get(i).equals(queryID)){
						QUERY.add(j+1);
						Qid = i;
					}	
				}
			}
			Length[i]=count;
			count =0;
		}

		for (int i=0; i < QUERY.size(); i++){
			for(int j=1; j<DOCLIST.size(); j++){
				if (j!= Qid)
					Scores[j] += Double.parseDouble(list.get(QUERY.get(i)).get(j)) * Double.parseDouble(list.get(QUERY.get(i)).get(Qid));
			}
		}
		
		for(int j=1; j<DOCLIST.size(); j++){
			Scores[j]=Scores[j]/Length[j];
		}
		double max=0;
		int elementnr=0;
		for (int i=0; i<10 ; i++){
			for (int j=0; j<DOCLIST.size() ; j++){
				if (max<Scores[j]){
					max = Scores[j];
					elementnr = j;
				}
			}
			TopK[i]=DOCLIST.get(elementnr);
			TopKScore [i]= Scores [elementnr];
			Scores [elementnr]=0;
			max=0;
		}		
			
		//for(int j=1; j<DOCLIST.size(); j++){
		//	System.out.println(Scores[j]);	
		//}
	}
	
	public void writeFile(){	
		try {
			for(int j=0; j<10; j++){
				y.write(TOPIC+" Q0 ");
				y.write(TopK[j].substring(24,(TopK[j].length()-6)));
				y.write("/");
				y.write(TopK[j].substring(TopK[j].length()-5));	
				y.write(" "+(j+1)+" ");
				y.write(Double.toString(TopKScore[j]).substring(0,5));	
				y.write(" groupQ_"+SIZE);
				if (j!=9)
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
		} catch (IOException c) {
		    System.err.format("IOException: %s%n", c);
		}		
	}
}