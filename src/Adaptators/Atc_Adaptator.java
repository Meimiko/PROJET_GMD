package Adaptators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Atc_Adaptator {
	
	public static void main(String[] args) throws Exception {
		//testSider();
		//SearchElement("index_atc","label","id_atc");
		
		ArrayList<String> ids_atc=new ArrayList<String>();
		ids_atc.add("A01AA01");
		ids_atc.add("A01AA02");
		ids_atc.add("A01AA03");
		ids_atc.add("A01AA04");
		ids_atc.add("A01AA30");
		ids_atc.add("V10XX02");
		ids_atc.add("V10XX03");
		
		new Atc_Adaptator().getLabel(ids_atc);
		
	}
	
	/**
	 * Constructor of Atc's adaptator
	 */
	public Atc_Adaptator(){
		CreateIndex("br08303.keg","index_atc");
	}
	
	/**
	 * Function which was use to test how to parse ATC's document.
	 * @throws IOException
	 */
		public static void testReadATC() throws IOException{
			BufferedReader flotFiltre;
			String filtre;
			flotFiltre = new BufferedReader(new FileReader("E:/IAMD/GMD/Projet/Projet/Donn馥s/atc/br08303.keg"));
			filtre=flotFiltre.readLine();
			BufferedWriter flot = new BufferedWriter(new FileWriter(new File("src/" +"testAtc.keg")));
			int cmpt=0;
			String buff="";
			while(filtre !=null){
				if (filtre.startsWith("E")){
					cmpt++;
					if (buff.equals(filtre.substring(11, 17))){
						System.out.println("duplicate"+cmpt);
					}
					buff=filtre.substring(11, 17);
					flot.write(buff+"\n");
				}
				filtre=flotFiltre.readLine();
			}
			System.out.println(cmpt);
			flot.close();
			flotFiltre.close();
			
		}

	/**
	 * Create a index from ATC's document
	 * @param docsPath The ATC's path
	 * @param indexPath The path of the index which will be create
	 */
	 private static void CreateIndex(String docsPath,String indexPath) {
	    boolean create = true;

	    final Path docDir = Paths.get(docsPath);
	    if (!Files.isReadable(docDir)) {
	      System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }
	    
	    Date start = new Date();
	    try {
	      System.out.println("Indexing to directory '" + indexPath + "'...");

	      Directory dir = FSDirectory.open(Paths.get(indexPath));
	      Analyzer analyzer = new StandardAnalyzer();
	      IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

	      if (create) {
	        // Create a new index in the directory, removing any
	        // previously indexed documents:
	        iwc.setOpenMode(OpenMode.CREATE);
	      } else {
	        // Add new documents to an existing index:
	        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
	      }

	      IndexWriter writer = new IndexWriter(dir, iwc);
	      
	      //indexDocs(writer, docDir);
	      indexDoc(writer, new File(docDir.toString()));

	      writer.close();

	      Date end = new Date();
	      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() +
	       "\n with message: " + e.getMessage());
	    }
	  }
	
 	/**
	 * Used to create the Atc's index in CreateIndex()
	 * @param writer
	 * @param file
	 * @throws IOException
	 */
	 private static void indexDoc(IndexWriter writer, File file) throws IOException {
		  int eltcount = 0;
		  if (file.canRead() && !file.isDirectory()){
			  try{
				  InputStream ips = new FileInputStream(file);
				  InputStreamReader ipsr = new InputStreamReader(ips);
				  BufferedReader br = new BufferedReader(ipsr);
				  String line;
				  Document doc=null;
				  line=br.readLine();

				  while ((line=br.readLine())!=null ){
					  if (line.startsWith("E")){
						  doc = new Document();
						  eltcount++;
						  String id_atc=line.substring(9, 16);
						  String label=line.substring(17);
						  doc.add(new TextField("id_atc", id_atc, Field.Store.YES));
						  doc.add(new TextField("label", label, Field.Store.YES));
						  writer.addDocument(doc);
					  }
				  }
				  br.close();
			  } catch (Exception e){
				  System.out.println(e.toString());
			  }
		  }
		  System.out.println(eltcount + " elements were had added to the index ");
	  }
	
	 /**
	 * Get drug's Label corresponding to atc's id
	 * @param ids_atc List of atc's id
	 * @return List of drug's label
	 * @throws IOException
	 * @throws ParseException
	 */
	public ArrayList<String> getLabel(ArrayList<String> ids_atc) throws IOException, ParseException{
		ArrayList<String> labels=new ArrayList<String>();
		
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index_atc")));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer();
	    QueryParser parser = new QueryParser("id_atc", analyzer);
	    
	    BufferedReader in = null;
	    for (int j=0;j<ids_atc.size();j++){
		    String line = ids_atc.get(j);
	
		    //line = line.trim();
		    Query query = parser.parse(line);
		    
		    TopDocs results = searcher.search(query, 1000);
		    ScoreDoc[] hits = results.scoreDocs;
		    for (int i=0;i<results.totalHits;i++){
		    	Document doc = searcher.doc(hits[i].doc);
		    	labels.add(doc.get("label"));
		    }
	    }
		return labels;
	}

}
