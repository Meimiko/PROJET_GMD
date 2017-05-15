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
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class HPOAnnotations_Adaptator {

	private static String db = "HPO/hpo_annotations.sqlite";
	private static Connection con = null;
	private static Statement st = null;

	/**
	 * Constructor which call the methods to index HPOAnnotations.sqlite
	 * @throws IOException
	 */
	public HPOAnnotations_Adaptator() throws IOException{
		SQLiteConnect();
		Indexer("index_HPOAnnotations" ,"HPOAnnotations.txt", true);
	}


	/**
	 * used for tests
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws IOException, ParseException {
		new HPOAnnotations_Adaptator();
	}


	/**
	 * Create a file text with the all sqlite db. This file test will be indexed.
	 * @throws IOException
	 */
	public static void SQLiteConnect() throws IOException{
		try{
			Class.forName("org.sqlite.JDBC");
			con=DriverManager.getConnection("jdbc:sqlite:"+db);
			st = con.createStatement();
			System.out.println("connection established");

			FileWriter file=new FileWriter("HPOAnnotations.txt");

			String myQuery = "SELECT disease_db, disease_id, disease_label, sign_id FROM phenotype_annotation";		
			ResultSet res = st.executeQuery(myQuery);

			while(res.next()){
				String disease_db = res.getString(1);
				String disease_id = res.getString(2);
				String disease_label = res.getString(3);
				String sign_id = res.getString(4);
				file.write(disease_db+"SEPARATEUR"+disease_id+"SEPARATEUR"+disease_label+"SEPARATEUR"+sign_id+"SEPARATEUR\n");
			}

			file.close();
			res.close();
			st.close();
			con.close();
		} catch(SQLException ex){
			System.err.println("SQLException information");
			while(ex!=null){
				System.err.println("Error msg :" + ex.getMessage());
				System.err.println("SQLSTATE :" + ex.getSQLState());
				System.err.println("Error code :" + ex.getErrorCode());
				ex.printStackTrace();
				ex=ex.getNextException();
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * Index the file text created with SQliteConnect().
	 * @param indexPath = name of the new index
	 * @param docsPath = path of the file text
	 * @param create = boolean (true)
	 */
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

	/**
	 * Used to create the index in Indexer()
	 * @param writer
	 * @param file
	 * @throws IOException
	 */
	static void indexDoc(IndexWriter writer, File file) throws IOException {
		if (file.canRead() && !file.isDirectory()){
			try{
				InputStream ips = new FileInputStream(file);
				InputStreamReader ipsr = new InputStreamReader(ips);
				BufferedReader br = new BufferedReader(ipsr);
				String line=br.readLine();
				Document doc=null;

				while(!(line==null)){
					doc = new Document();
					String[] elts = line.split("SEPARATEUR");
					doc.add(new TextField("disease_db",elts[0],Field.Store.YES));
					doc.add(new TextField("disease_id",elts[1],Field.Store.YES));
					doc.add(new TextField("disease_label",elts[2],Field.Store.YES));
					doc.add(new TextField("sign_id",elts[3],Field.Store.YES));
					writer.addDocument(doc);
					line=br.readLine();
				}
				br.close();
			} catch (Exception e){
				System.out.println(e.toString());
			}
		}
	}



}

