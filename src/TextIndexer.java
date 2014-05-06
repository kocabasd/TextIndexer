import java.io.File;
import java.io.IOException;

public class TextIndexer {
		private final static File FolderLocation = new File("C:/20_newsgroups_subset/20_newsgroups_subset/");
		private final static String QueryLocation = "C:/topics/";
		private final static String IndexLocation = "./output/";	
		private static String Topic = "topic1";
		private static String Experiment = "experiment5";
		private static boolean bagOfWords = true;
		private static boolean Stem = true;
		private static boolean tfidf = true;		
		private static boolean CreateIndex = true;
	    
		/**
		 * Experiment 1 :	bagOfWords = true	- Stem = true	 -	tfidf = true
		 * Experiment 2 :	bagOfWords = true 	- Stem = true	 -	tfidf = false
		 * Experiment 3 :	bagOfWords = true 	- Stem = false	 -	tfidf = true
		 * Experiment 4 :	bagOfWords = true 	- Stem = false	 -	tfidf = false
		 * Experiment 5 :	bagOfWords = false	- Stem = true	 -	tfidf = 
		 * Experiment 6 :	bagOfWords = false 	- Stem = true	 -	tfidf = false
		 * Experiment 7 :	bagOfWords = false 	- Stem = false	 -	tfidf = true
		 * Experiment 8 :	bagOfWords = false 	- Stem = false	 -	tfidf = false
		 */	

		public static void main(String[] args) throws IOException{
	/*		if (args.length != 2){
				System.err.println("Usage: java TextIndexer arg0 arg1");
				System.exit(0);
			}
			
			Topic = "topic" + args[0];
			Experiment = "experiment" + args[1];
			if (args[1].equals("1")){
				System.out.println("1");
				bagOfWords = true;
				Stem = true;
				tfidf = true;
			}
			if (args[1].equals("2")){
				System.out.println("2");
				bagOfWords = true;
				Stem = true;
				tfidf = false;
			}
			if (args[1].equals("3")){
				System.out.println("3");
				bagOfWords = true;
				Stem = false;
				tfidf = true;
			}
			if (args[1].equals("4")){
				System.out.println("4");
				bagOfWords = true;
				Stem = false;
				tfidf = false;
			}
			if (args[1].equals("5")){
				System.out.println("5");
				bagOfWords = false;
				Stem = true;
				tfidf = true;
			}
			if (args[1].equals("6")){
				System.out.println("6");
				bagOfWords = true;
				Stem = true;
				tfidf = false;
			}
			if (args[1].equals("7")){
				System.out.println("7");
				bagOfWords = false;
				Stem = false;
				tfidf = true;
			}
			if (args[1].equals("8")){
				System.out.println("8");
				bagOfWords = false;
				Stem = false;
				tfidf = false;
			}
			String result1 = bagOfWords ? "bagOfWords" : "bigram";
			String result2 = Stem ? "yes" : "no";
			String result3 = tfidf ? "yes" : "no";
			System.out.println( result1 +"Stem:" + result2 + "  tfidf:" + result3);
		*/
			ReadWriteData data = new ReadWriteData();
			if(CreateIndex)
				data.ReadData(FolderLocation, Stem, bagOfWords);
			else
				data.readIndex(IndexLocation);
			data.ReadQuery(QueryLocation+Topic, Stem, bagOfWords);	
			
			if(tfidf){
				TfIdf score = new TfIdf();
				score.calculateTF();	
				score.multiplyIDF();
			}
			Search search = new Search();
			search.fastCosineScore();
			data.WriteData(IndexLocation, Topic, Experiment);
			//if(CreateIndex)
			//	data.writeIndex(IndexLocation);
			
		}
}

