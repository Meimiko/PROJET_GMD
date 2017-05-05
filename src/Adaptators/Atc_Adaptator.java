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
	
	
	public Atc_Adaptator(){
		CreateIndex("E:/IAMD/GMD/Projet/Projet/Données/atc/br08303.keg","index_atc");
	}
	
	public static void testSider() throws IOException{
		BufferedReader flotFiltre;
		String filtre;
		flotFiltre = new BufferedReader(new FileReader("sider.txt"));
		filtre=flotFiltre.readLine();
		BufferedWriter flot = new BufferedWriter(new FileWriter(new File("src/" +"testSider.txt")));
		int cmpt=0;
		String buff="";
		while(filtre!=null){
			if (filtre.startsWith("CID1")){
				if (!filtre.substring(4,13).equals(buff))
					cmpt++;
				buff=filtre.substring(4,13);
			}
			filtre=flotFiltre.readLine();
		}
		System.out.println(cmpt);
		
	}
	
	//NB : ATC 3386 elements dans les 3386 premieres lignes
		public static void testReadATC() throws IOException{
			BufferedReader flotFiltre;
			String filtre;
			flotFiltre = new BufferedReader(new FileReader("E:/IAMD/GMD/Projet/Projet/Données/atc/br08303.keg"));
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

	      // Optional: for better indexing performance, if you
	      // are indexing many documents, increase the RAM
	      // buffer.  But if you do this, increase the max heap
	      // size to the JVM (eg add -Xmx512m or -Xmx1g):
	      //
	      // iwc.setRAMBufferSizeMB(256.0);

	      IndexWriter writer = new IndexWriter(dir, iwc);
	      
	      //indexDocs(writer, docDir);
	      indexDoc(writer, new File(docDir.toString()));
	    	  

	      // NOTE: if you want to maximize search performance,
	      // you can optionally call forceMerge here.  This can be
	      // a terribly costly operation, so generally it's only
	      // worth it when your index is relatively static (ie
	      // you're done adding documents to it):
	      //
	      // writer.forceMerge(1);

	      writer.close();

	      Date end = new Date();
	      System.out.println(end.getTime() - start.getTime() + " total milliseconds");

	    } catch (IOException e) {
	      System.out.println(" caught a " + e.getClass() +
	       "\n with message: " + e.getMessage());
	    }
	  }
	
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
		  System.out.println(eltcount + " elements ont Ã©tÃ© ajoutÃ© Ã  l'index ");
	  }
	
	/**
	 * Permet d'obtenir la liste des labels des medicament correspondants aux id_atc donnés en entrée
	 * @param ids_atc
	 * @return
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
		    //System.out.println("Nombre de resultat :"+results.totalHits);
		    ScoreDoc[] hits = results.scoreDocs;
		    for (int i=0;i<results.totalHits;i++){
		    	Document doc = searcher.doc(hits[i].doc);
		    	labels.add(doc.get("label"));
		    	//System.out.println(doc.get("id_atc"));
		    	//System.out.println(doc.get("label"));
		    }
	    }
		return labels;
	}
	
	
	//Plus trop utile maintenant...
	private static void SearchElement(String indexPath, String Searchfield,String idField) throws Exception {

	    String index = indexPath;
	    String field = Searchfield;
	    String queries = null;
	    int repeat = 0;
	    boolean raw = false;
	    String queryString = null;
	    int hitsPerPage = 10;
	    
	    IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
	    IndexSearcher searcher = new IndexSearcher(reader);
	    Analyzer analyzer = new StandardAnalyzer();

	    BufferedReader in = null;
	    if (queries != null) {
	      in = Files.newBufferedReader(Paths.get(queries), StandardCharsets.UTF_8);
	    } else {
	      in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
	    }
	    QueryParser parser = new QueryParser(field, analyzer);
	    while (true) {
	      if (queries == null && queryString == null) {                        // prompt the user
	        System.out.println("Enter query: ");
	      }

	      String line = queryString != null ? queryString : in.readLine();

	      if (line == null || line.length() == -1) {
	        break;
	      }

	      line = line.trim();
	      if (line.length() == 0) {
	        break;
	      }
	      
	      Query query = parser.parse(line);
	      System.out.println("Searching for: " + query.toString(field));
	            
	      if (repeat > 0) {                           // repeat & time as benchmark
	        Date start = new Date();
	        for (int i = 0; i < repeat; i++) {
	          searcher.search(query, 100);
	        }
	        Date end = new Date();
	        System.out.println("Time: "+(end.getTime()-start.getTime())+"ms");
	      }

	      doPagingSearch(in, searcher, query, hitsPerPage, raw, queries == null && queryString == null,idField,field);

	      if (queryString != null) {
	        break;
	      }
	    }
	    reader.close();
	  }

	  /**
	   * This demonstrates a typical paging search scenario, where the search engine presents 
	   * pages of size n to the user. The user can then go to the next page if interested in
	   * the next hits.
	   * 
	   * When the query is executed for the first time, then only enough results are collected
	   * to fill 5 result pages. If the user wants to page beyond this limit, then the query
	   * is executed another time and all hits are collected.
	   * 
	   */
	private static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, 
	                                     int hitsPerPage, boolean raw, boolean interactive,
	                                     String idField,String queryField) throws IOException {
	 
	    // Collect enough docs to show 5 pages
	    TopDocs results = searcher.search(query, 5 * hitsPerPage);
	    ScoreDoc[] hits = results.scoreDocs;
	    
	    int numTotalHits = results.totalHits;
	    System.out.println(numTotalHits + " total matching documents");

	    int start = 0;
	    int end = Math.min(numTotalHits, hitsPerPage);
	        
	    while (true) {
	      if (end > hits.length) {
	        System.out.println("Only results 1 - " + hits.length +" of " + numTotalHits + " total matching documents collected.");
	        System.out.println("Collect more (y/n) ?");
	        String line = in.readLine();
	        if (line.length() == 0 || line.charAt(0) == 'n') {
	          break;
	        }

	        hits = searcher.search(query, numTotalHits).scoreDocs;
	      }
	      
	      end = Math.min(hits.length, start + hitsPerPage);
	      
	      for (int i = start; i < end; i++) {
	        if (raw) {                              // output raw format
	          System.out.println("doc="+hits[i].doc+" score="+hits[i].score);
	          continue;
	        }

	        Document doc = searcher.doc(hits[i].doc);
	        String id = doc.get(idField);
	        if (id != null) {
	          System.out.println((i+1) + ". " + id);
	          String field = doc.get(queryField);
	          if (field != null) {
	            System.out.println("   "+queryField+": " + doc.get(queryField));
	          }
	        } else {
	          System.out.println((i+1) + ". " + "No drug for this search");
	        }
	                  
	      }

	      if (!interactive || end == 0) {
	        break;
	      }
	      if (numTotalHits >= end) {
	        boolean quit = false;
	        while (true) {
	          System.out.print("Press ");
	          if (start - hitsPerPage >= 0) {
	            System.out.print("(p)revious page, ");  
	          }
	          if (start + hitsPerPage < numTotalHits) {
	            System.out.print("(n)ext page, ");
	          }
	          System.out.println("(q)uit or enter number to jump to a page.");
	          
	          String line = in.readLine();
	          if (line.length() == 0 || line.charAt(0)=='q') {
	            quit = true;
	            break;
	          }
	          if (line.charAt(0) == 'p') {
	            start = Math.max(0, start - hitsPerPage);
	            break;
	          } else if (line.charAt(0) == 'n') {
	            if (start + hitsPerPage < numTotalHits) {
	              start+=hitsPerPage;
	            }
	            break;
	          } else {
	            int page = Integer.parseInt(line);
	            if ((page - 1) * hitsPerPage < numTotalHits) {
	              start = (page - 1) * hitsPerPage;
	              break;
	            } else {
	              System.out.println("No such page");
	            }
	          }
	        }
	        if (quit) break;
	        end = Math.min(numTotalHits, start + hitsPerPage);
	      }
	    }
	  }

}
