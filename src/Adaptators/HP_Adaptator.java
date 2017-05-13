package Adaptators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

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


public class HP_Adaptator {
	
	private int IDNumberOBO=0; // opt1
	private int Sign_idSQLITE=0; // opt1
	
	public static void main(String args[]) throws IOException, ParseException, ClassNotFoundException, SQLException{
		
		/*ArrayList<String> HpoboName = new ArrayList<String>();
		HpoboName.add("Functional abnormality of the bladder");
		HpoboName.add("Increased ratio of VWF propeptide to VWF antigen");
		HpoboName.add("Abnormality of inferior alveolar nerve");
		new HP_Adaptator().nameToId(HpoboName);*/
		
		ArrayList<String> oboId = new ArrayList<String>();
		oboId.add("HP:0003142");
		oboId.add("HP:0011120");
		oboId.add("HP:0030f735");
		oboId.add("HP:0000006");
		new HP_Adaptator().oboIdToSqliteDiseaselabel(oboId);
	}
	
	public HP_Adaptator(){
		Indexer("Index_HP.obo","HPO/HP.obo",true);
	}
	
	public static void Indexer(String indexPath, String docsPath, boolean create) {

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

		  static void indexDoc(IndexWriter writer, File file) throws IOException {
			  if (file.canRead() && !file.isDirectory()){
				  try{
					  InputStream ips = new FileInputStream(file);
					  InputStreamReader ipsr = new InputStreamReader(ips);
					  BufferedReader br = new BufferedReader(ipsr);
					  String line=br.readLine();
					  Document doc=null;
					  
					  while((line=br.readLine())!=null){
						  if (line.startsWith("[Term]")){
							  doc = new Document();
							  line=br.readLine();
							  while (!(line.equals("[Term]"))){
								  if(line.startsWith("id:")){
									  String content = line;
									  content = content.substring("id:".length()+1);
									  doc.add(new TextField("ID",content,Field.Store.YES));
								  }
								  else if(line.startsWith("name:")){
									  String content = line;
									  content = content.substring("name:".length()+1);
									  doc.add(new TextField("name",content,Field.Store.YES));
								  }
								  if(line.startsWith("synonym:")){
									  String contentsyn="";
									  while(line.startsWith("synonym:")){
										  String content = line;
										  content = content.substring("synonym:".length()+2);
										  String[] contentTab = content.split("\"");
										  contentsyn += contentTab[0]+";";									  
										  line = br.readLine();
									  }
									  doc.add(new TextField("synonyms",contentsyn,Field.Store.YES));
								  }
								  else if(line.startsWith("xref:")){
									  String content = line;
									  content = content.substring("xref:".length()+1);
									  doc.add(new TextField("xref",content,Field.Store.YES));
								  }
								  else if(line.startsWith("is_a:")){
									  String content = line;
									  content = content.substring("is_a:".length()+1);
									  doc.add(new TextField("is_a",content,Field.Store.YES));
								  }
								  line=br.readLine();
								  if(line==null){ // to quit the while loop
									  line="[Term]";
								  }
							  }			  
							  writer.addDocument(doc);
						  }
					  }				  
					  br.close();
				  } catch (Exception e){
					  System.out.println(e.toString());
				  }
			  }
		  }
		  
		  public ArrayList<String> nameToId(ArrayList<String> HpoboName) throws IOException, ParseException{
				ArrayList<String> Ids=new ArrayList<String>();
				
				IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("index_HP.obo")));
			    IndexSearcher searcher = new IndexSearcher(reader);
			    Analyzer analyzer = new StandardAnalyzer();
			    QueryParser parser = new QueryParser("name", analyzer);
			    
			  //synoym opt
			    QueryParser synonymParser = new QueryParser("synonyms", analyzer); 
			    
			    //Synonyms opt
			    int n=HpoboName.size();
			    for (int j=0;j<n;j++){			    	
			    	String name = HpoboName.get(j);
			    	Query synonymQuery = synonymParser.parse(name);
			    	TopDocs synonymresults = searcher.search(synonymQuery, 1000);
			    	ScoreDoc[] synonymHits = synonymresults.scoreDocs;
			    	Document synonymdoc = searcher.doc(synonymHits[0].doc);
			    	String[] syno = synonymdoc.get("synonyms").split(";");
			    	for (int i=0;i<syno.length;i++){
			    		HpoboName.add(syno[i]);
			    	}
			    	System.out.println("Synonyms found for "+HpoboName.get(j)+" : "+synonymdoc.get("synonyms"));
			    }
			    
			    	
			    
			    for (int j=0;j<HpoboName.size();j++){
				    String line = HpoboName.get(j);
				    Query query = parser.parse(line);  
				    TopDocs results = searcher.search(query, 1000);
				    //System.out.println("Nombre de resultat :"+results.totalHits);
				    ScoreDoc[] hits = results.scoreDocs;
				    for (int i=0;i<results.totalHits;i++){
				    	Document doc = searcher.doc(hits[i].doc);
				    	Ids.add(doc.get("ID"));
				    	this.IDNumberOBO+=1; // opt1
				    	
				    }			    
			    }
			    System.out.println("Number of HPO.Obo ID which match with the symptom name entered by the user : "+this.IDNumberOBO); // opt1
				return Ids;
			}
		  
		  
		  public ArrayList<String> oboIdToSqliteDiseaselabel(ArrayList<String> oboId) throws IOException, ParseException, ClassNotFoundException, SQLException {
				ArrayList<String> sqliteDiseaseLabel=new ArrayList<String>();
				ArrayList<String> sqlitesignid=new ArrayList<String>();
				
				String db = "HPO/hpo_annotations.sqlite";
				Class.forName("org.sqlite.JDBC");
				Connection con=DriverManager.getConnection("jdbc:sqlite:"+db);
				Statement st = con.createStatement();
				System.out.println("connection established");

				for(int i=0;i<oboId.size();i++){
					String myQuery = "SELECT disease_label, sign_id FROM phenotype_annotation WHERE sign_id="+"\""+oboId.get(i)+"\"";	
					ResultSet res = st.executeQuery(myQuery);
					while(res.next()){
						sqliteDiseaseLabel.add(res.getString(1));
						if(!sqlitesignid.contains(res.getString(2))){sqlitesignid.add(res.getString(2));} // opt1
						//System.out.println(res.getString(2));
					}
					res.close();
				}
				st.close();
				con.close();
				this.Sign_idSQLITE = sqlitesignid.size();
				System.out.println("Number of different HPOAnnotations.Sqlite sign_id corresponding to the "+this.IDNumberOBO+" HPO.Obo ID : "+this.Sign_idSQLITE); // opt1
				return sqliteDiseaseLabel;
			}

		public int getNameNumberOBO() { // opt1
			return IDNumberOBO;
		}

			  
	  }
		  
		




