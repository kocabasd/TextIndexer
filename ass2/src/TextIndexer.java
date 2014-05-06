import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;

public class TextIndexer {
		private static String Directory = "C:/20_newsgroups_subset/20_newsgroups_subset/";
		private static String indexPath = "C:/20_newsgroups_subset/";
		private static String topic;
		private static String queryPath;
		private static boolean CreateIndex = false;
		private static String experiment;

		public static void main(String[] args) throws IOException, ParseException{
			/**
			 * Experiment 1 :	Lucene Default Similarity
			 * Experiment 2 :	BM25Similarity with Lucene
			 * Experiment 3 :	BM25LSimilarity of the Paper
			 */
			if (args.length < 2){
				System.err.println("Usage: java TextIndexer arg0 arg1");
				System.exit(0);
			}
			
			topic = "topic" + args[0];
			experiment = args[1];
			if (args[2].contentEquals("0"))
				CreateIndex = true;
			System.out.println("Topic: "+ topic +"  Experiment: "+ experiment +"  Create Index:"+ CreateIndex);
			Lucene lucene = new Lucene();
			if(CreateIndex)
				lucene.createIndex(Directory, indexPath);
			queryPath = "C:/topics/" + topic;
			lucene.searchIndex(indexPath, queryPath, experiment );
			
			
		}
}

