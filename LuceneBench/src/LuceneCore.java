
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;

import java.nio.file.Paths;

public class LuceneCore 
{
	// The index object, points to a repository
	Directory index;

	// Specify the analyzer for tokenizing text.
	// The same analyzer should be used for indexing and searching
	SimpleAnalyzer analyzer;

	// Writing object, linked to the repository
	IndexWriter writer = null;

	// Document
	Document doc;
	TextField titleField;
	TextField contentField;
	TextField domainField;
	TextField urlField;
	StoredField dateField;

	LuceneCore() {
		// Instantiate the analyzer
		analyzer = new SimpleAnalyzer();
	}

	/* Open or create the index */
	public void openIndex(String directory, boolean newIndex) {
		try {
			// Link the directory on the FileSystem to the application
			//https://lucene.apache.org/core/7_5_0/core/org/apache/lucene/store/FSDirectory.html
			//NIOFSDirectory for Java, but not Windows
			index = FSDirectory.open(Paths.get(directory));
			//index = MMapDirectory.open(Paths.get(directory));

			IndexWriterConfig conf = new IndexWriterConfig(analyzer);
			//Java command-line:  -Xmx8g -Xms8g -server (start menu: configure java)
			//see https://home.apache.org/~mikemccand/lucenebench/indexing.html
			conf.setRAMBufferSizeMB(2048);
			writer = new IndexWriter( index, conf );
		} catch (Exception e) {
			System.out.println("Got an Exception: " + e.getMessage());
		}
	}

	public Long search(String querystr, boolean display) 
	{
		Long totalHits=0L;
		try {
			// Instantiate a query parser
			MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "content","domain","url"},analyzer);
			parser.setDefaultOperator( MultiFieldQueryParser.AND_OPERATOR );//parser.AND_OPERATOR

			// Parse
			Query q = parser.parse(querystr);

			// Instantiate a searcher
			IndexReader indexReader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(indexReader);

			// We look for top-10 results
			int hitsPerPage = 10;
			int totalHitsTreshold=Integer.MAX_VALUE;
			// Ranker
			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, totalHitsTreshold);
			// Search
			searcher.search(q, collector);
			// Retrieve the top-10 documents
			TopDocs topDocs=collector.topDocs();
			//TopDocs topDocs = searcher.search(q, hitsPerPage);

			ScoreDoc[] hits = topDocs.scoreDocs;
			totalHits=topDocs.totalHits.value;

			Highlighter highlighter = new Highlighter(new QueryScorer(q));		

			// Display results
			if (display)
			{
				System.out.println("query: "+querystr + " hits: " + hits.length+" totalHits: "+String.format("%,d",totalHits));
				for (int i = 0; i < hits.length; ++i) {
					int docId = hits[i].doc;
					Document d = searcher.doc(docId);
					String title=d.get("title");
					String content=d.get("content");
					String domain=d.get("domain");
					String url=d.get("url");
					String date=d.get("date");
					String kwic ="";	
										
					kwic = highlighter.getBestFragment(analyzer, "content", content);
					
					System.out.println((i + 1) + ". " + title);
					System.out.println(kwic);
					System.out.println(url);
					System.out.println(domain);
					System.out.println(date);
					System.out.println();				
				}
			}

			// Close the searcher
			//searcher.close();
		} catch (Exception e) {
			System.out.println("Got an Exception: " + e.getMessage());
		}
		return totalHits;
	}

	public void addDoc(DocObject  document) {
		try {
			// Instantiate a new document
			Document doc = new Document();

			// Add the field to the document		
			doc.add(new TextField("title", document.title, Field.Store.YES));
			doc.add(new TextField("content", document.content, Field.Store.YES));
			doc.add(new TextField("domain", document.domain, Field.Store.YES));
			doc.add(new TextField("url", document.url, Field.Store.YES));
			doc.add(new StoredField("date", document.date));
			
			// And add the document to the index
			writer.addDocument(doc);
			//for Near Real Time Search
			//writer.flush();
			//writer.commit();
		} catch (Exception e) {
			System.out.println("Got an Exception: " + e.getMessage());
		}
	}

	// Close the index
	public void closeIndex() {
		try {
			writer.close();
		} catch (Exception e) {
			System.out.println("Got an Exception: " + e.getMessage());
		}
	}
}
