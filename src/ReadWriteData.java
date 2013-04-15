import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;


public class ReadWriteData {
	final static File FOLDER_NAME = new File("C:/20_newsgroups_subset/rec.sport.baseball");
	final static String FOLDER = "C:\\20_newsgroups_subset\\rec.sport.baseball\\";
	final static String OUTPUT_FILE_NAME = "C:\\20_newsgroups_subset\\11";
    final static Charset ENCODING = StandardCharsets.UTF_8;
    static ArrayList<String> DOCLIST= new ArrayList<String>();
    static ArrayList<String> TERMLIST= new ArrayList<String>();
    static ArrayList<Double> IDF= new ArrayList<Double>();
    private Scanner x;
	private BufferedWriter y;
	private static ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
	
	public static void main(String... aArgs) throws IOException{
		ReadWriteData data = new ReadWriteData();
		data.listFilesForFolder(FOLDER_NAME);
		for (int i =1; i<DOCLIST.size(); i++){
			System.out.println(DOCLIST.get(i));
			data.openFileToRead(FOLDER+ DOCLIST.get(i));
			data.readFile(i);
		}
		data.calculateTFIDF();		
		System.out.println(IDF);
		data.openFileToWrite(OUTPUT_FILE_NAME+".arff");
		data.writeFile();
		data.closeFile();	
	}


	    
	public void listFilesForFolder(final File folderName) {
				DOCLIST.add("DOCID");
  	   for (final File fileEntry : folderName.listFiles()) {
	      if (fileEntry.isDirectory()) {
	        System.out.println("Reading files under the folder "+folderName.getAbsolutePath());
	        listFilesForFolder(fileEntry);
	      } else {
	        if (fileEntry.isFile()) 
	        	DOCLIST.add(fileEntry.getName());
		  }
  	   }
  	   list.add(DOCLIST);
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
	    int i,j,k, l, count;
	    boolean NewTerm=true;
		String strLine;
		String[] temp;
		String[] a;
		  while (x.hasNextLine())   {
			  strLine = x.nextLine();	
			  strLine =Stemming(strLine);
			  temp = strLine.split(" ");
			  for (i=0;i<temp.length;i++){
				  NewTerm=true;
				  if (temp[i].equals("")) continue;	
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
					  
				  
				  if (TERMLIST.isEmpty()){
					  TERMLIST.add(temp[i]);
					  list.add(new ArrayList<String>());
					  list.get(list.size()-1).add(temp[i]);
			  	  	  for (k=0; k < DOCLIST.size();k++)
			  	  		list.get(list.size()-1).add("0");
			  	  	  list.get(list.size()-1).set(docId,"1");			  	  	
				  }
				  else {
					  for(j=0; j < TERMLIST.size(); j++){	
						  if(TERMLIST.get(j).contentEquals(temp[i])){
					  	  	  count = Integer.parseInt(list.get(j+1).get(docId));
					  	  	  count++;
					  	  	  list.get(j+1).set(docId,Integer.toString(count));	
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
				  	  	  list.get(list.size()-1).set(docId,"1");						  
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

	public void calculateTFIDF(){
		double df=0, tf = 0, inversedf ;
		for (int i=1; i< list.size();i++){
			for (int j=1; j< list.get(0).size();j++){
				if (list.get(i).get(j)!= "0"){
					df++;
				}
			}
			inversedf = (DOCLIST.size()-1)/df;
			System.out.println(df);
			
			df=0;
			IDF.add(Math.log(inversedf));
		}
		for (int i=1; i< list.size();i++){
			for (int j=1; j< list.get(0).size();j++){
				if (list.get(i).get(j)!= "0"){
					tf = 1 + Math.log(Integer.parseInt(list.get(i).get(j)));
					list.get(i).set(j,Double.toString(IDF.get(i-1) * tf));
					
				}
			}
		}		
	}
	
	public void writeFile(){	
		try {
			y.write("@relation rec.sport.baseball\n\n");
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
					else {
						y.write(list.get(j).get(i));
						y.write(",");
					}
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
			y.close();
		} catch (IOException c) {
		    System.err.format("IOException: %s%n", c);
		}		
	}
}