import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;

public class Lucene {
    final static Charset ENCODING = StandardCharsets.UTF_8;
    
	protected void createIndex(String folderName, String indexPath) throws IOException {
		//Most sophisticated analyzer that knows about certain token types, lowercases, removes stop words,  ..
		Analyzer analyzer = new  StandardAnalyzer(Version.LUCENE_48);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_48, analyzer);
		Directory indexDirectory = FSDirectory.open(new File(indexPath));
		// Create a new index in the directory, removing any previously indexed document
		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		IndexWriter indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
		indexDocs(indexWriter, new File(folderName));	
		
		indexWriter.close();
	}
	
	protected void 	indexDocs (IndexWriter indexWriter, File folderName) throws IOException {
		for (File fileEntry : folderName.listFiles()) {
			if (fileEntry.isDirectory()) {
				//System.out.println("Reading files under the folder "+folderName.getAbsolutePath()+fileEntry);
				indexDocs(indexWriter, fileEntry);
			} 
			else {
				if (fileEntry.isFile()){
					FileInputStream fis;
					try {
						fis = new FileInputStream(fileEntry);
					} catch (FileNotFoundException fnfe) {
					    return;
					}
					try {
						Document newDoc = new Document();
						Field pathField = new StringField("path", fileEntry.getParentFile().getName() +"/" + fileEntry.getName(), Field.Store.YES);
						newDoc.add(pathField);
						newDoc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))));						
						indexWriter.addDocument(newDoc);
					}
					finally {
						fis.close();
					}
				}
			}
		}
	}
	@SuppressWarnings("deprecation")
	protected void searchIndex(String indexPath, String queryPath, String experimentNo) throws IOException, ParseException {
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
		
		
		Directory indexDirectory = FSDirectory.open(new File(indexPath));
		IndexReader indexReader = IndexReader.open(indexDirectory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		if (experimentNo.contentEquals("2")){
				indexSearcher.setSimilarity(new BM25Similarity());
		}
		else if(experimentNo.contentEquals("3")) 
				indexSearcher.setSimilarity(new BM25LSimilarity());
		

		String escapedLine = "";
		QueryParser parser = new QueryParser(Version.LUCENE_48, "contents", analyzer);
		if (queryPath != null) {
			Scanner x = new Scanner(new File(queryPath)); 	
			String strLine = x.nextLine();
			while (x.hasNextLine())   {			  
				strLine = x.nextLine();
				strLine.trim();
				if (strLine.isEmpty()) 
					 break;			  
			}
			while (x.hasNextLine())   {
				strLine = x.nextLine();
				strLine = strLine.trim();
				if (!(strLine == null || strLine.length() <= 0)) {
					
					escapedLine += QueryParser.escape(strLine);
				}				
			}
			Query query = parser.parse(escapedLine);			
		
			
			
			
			TopDocs hits = indexSearcher.search(query, 100);
			int i =1;
			try {
				FileWriter fstream = new FileWriter("./output/" + queryPath.substring(10) +"experiment"+ experimentNo);
				BufferedWriter y = new BufferedWriter(fstream);
			for(ScoreDoc scoreDoc : hits.scoreDocs) {
				Document doc = indexSearcher.doc(scoreDoc.doc);	
						
				y.write(queryPath.substring(10) + " Q0 " +doc.get("path") + " " + i + " " + ((Double.toString(scoreDoc.score) + "00000").substring(0,5)) + " group2-experiment" + experimentNo);
				if (i!=100)
					y.write("\n");
				i++;
			/*	if(i<4){
					System.out.println(indexSearcher.explain(query, scoreDoc.doc).toString());
					System.out.println("*****************************");
				}
			*/
			}
			
			
			x.close();
			y.flush();
			y.close();
			} catch (IOException c) {
				System.err.format("IOException: %s%n", c);
			}
		}		
		else {
			System.out.println("Enter query");
		}
		indexReader.close();
	}

	
}
