package NER;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;

import SQLUtils.QueryDatabase;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class AnnotateText {

	// StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing,
	// and co-reference resolution

	/* Initialize the properties to use with the StanfordCoreNLP */

	public static void initialize() {

		System.out.println("Initializing properties.....");

		Properties props = new Properties();

		props.put("annotators", "tokenize,ssplit,pos,lemma,ner");
		pipeline = new StanfordCoreNLP(props, true);

	}

	public static StanfordCoreNLP pipeline;

	public static String readFile(String fileName, ArrayList<String> props)
			throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(fileName));
		try {
			StringBuilder sb = new StringBuilder();

			// Test Property
			// props.add("IFCPOSTALADDRESS");

			while (br.ready()) {
				String line = br.readLine();

				for (String property : props) {
					if (line.contains(property)) {
						sb.append(line);
						sb.append("\n");
					}
				}
			}
			br.close();
			return getPOSAnnotations(sb.toString());

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return "";

	}

	public static void writeRDF(String outputDirectory) {

		// For default output in user's home directory.

		String[] pkgPath = { "ifcEnrichment" };
		File f = new File(System.getProperty("user.home"));
		File subDir = f;
		for (String pkg : pkgPath) {
			subDir = new File(subDir, pkg);

		}

		String txtUI = outputDirectory + "/enrichmentTriples.txt";
		BufferedReader br = null;
		String line = "";
		String splitBy = ",";

		Writer writer = null;

		try {

			br = new BufferedReader(new FileReader(txtUI));

			// writer = new BufferedWriter(new OutputStreamWriter(
			// new
			// FileOutputStream(subDir.getAbsoluteFile()+"/"+"ifcEnrichment.ttl",true),
			// "UTF-8"));

			// Initialise counter for Links.

			int linkCounter = 1;

			while ((line = br.readLine()) != null) {

				// Split the retrieved results into parts.

				String[] subparts = line.split(splitBy);
				try {

					writer = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(outputDirectory + "/"
									+ "enrichmentTriples.ttl", true), "UTF-8"));

					// Write the prefixes used for the Enrichment Triples file.

					if (linkCounter == 1) {
						writer.write("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ."
								+ "\n"
								+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
								+ "\n"
								+ "@prefix vol: <http://purl.org/vol/ns#> ."
								+ "\n"
								+ "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."
								+ "\n"
								+ "@prefix void: <http://rdfs.org/ns/void#> ."
								+ "\n" + "@prefix : <#> ." + "\n" + "\n" + "\n");
					}

					// Write the enrichment triples to RDF (.ttl) file.

					if (subparts[5].contains("http")) {
						writer.write(":" + subparts[1] + " a void:Dataset."
								+ "\n" + "\t" + ":" + subparts[1]
								+ "_linkset a void:Linkset;" + "\n" + "\t"
								+ "\t" + "void:target :" + subparts[1] + ";\n"
								+ "\t" + "\t" + "rdfs:label \"dataset_id "
								+ subparts[0] + " resource_id " + subparts[2]
								+ "\"^^xsd:string.\n");
						writer.write("<" + subparts[3] + ">" + " " + "<"
								+ subparts[4] + ">" + " " + "<" + subparts[5]
								+ ">;" + "\n" + "." + "\n");

					}

					else {
						writer.write(":" + subparts[1] + " a void:Dataset."
								+ "\n" + "\t" + ":" + subparts[1]
								+ "_linkset a void:Linkset;" + "\n" + "\t"
								+ "\t" + "void:target :" + subparts[1] + ";\n"
								+ "\t" + "\t" + "rdfs:label \"dataset_id "
								+ subparts[0] + " resource_id " + subparts[2]
								+ "\"^^xsd:string.\n");
						writer.write("<" + subparts[3] + ">" + " " + "<"
								+ subparts[4] + ">" + " \"" + subparts[5]
								+ "\"^^xsd:string;\n" + "." + "\n");
					}

				} catch (IOException ex) {
				} finally {
					try {
						writer.close();
					} catch (Exception ex) {
					}
				}

				// Increment the Counter.

				linkCounter++;

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Location pivots are successfully initialised!");
	}

	public static String getPOSAnnotations(String content) {

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(content);

		// run all Annotators on this text

		pipeline.annotate(document);
		return getAnnotatedDocument(document);
	}

	/*
	 * Sets the annotation for a document, which are split for the different
	 * sentences. The annotations focus on POS, NER, and CO-Reference
	 * resolution.
	 */

	private static String getAnnotatedDocument(Annotation annotation) {
		StringBuffer sb = new StringBuffer();

		// Gather all the sentences in this document.

		List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {

			// Traversing the words in the current sentence, a CoreLabel is a
			// CoreMap with additional token-specific methods

			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				String word = token.get(TextAnnotation.class);
				// String pos = token.get(PartOfSpeechAnnotation.class);
				String ner = token.get(NamedEntityTagAnnotation.class);

				sb.append(word);
				sb.append(" ");
				sb.append(ner);
				sb.append(" ");
			}

			sb.append("\n");
		}

		return sb.toString();
	}

	/* Parse for the Named Entities: Persons, Locations, and Organizations. */

	public static void parseForNamedEntities(String output, String directory)
			throws FileNotFoundException, UnsupportedEncodingException,
			SQLException {

		System.out.println("Setting delimiters for IFC file contents.......");

		String delims = "[ ]+";
		String[] tokens = output.split(delims);
		// ArrayList<String> pers = new ArrayList<String>();
		ArrayList<String> locs = new ArrayList<String>();
		// ArrayList<String> orgs = new ArrayList<String>();

		for (int i = 0; i < tokens.length - 1; i++) {

			/*
			 * if (tokens[i+1].toString().equals("PERSON")){
			 * System.out.println("Found entity (of type PERSON)  :"
			 * +tokens[i]); pers.add(tokens[i]); } else
			 */
			if (tokens[i + 1].toString().equals("LOCATION")) {

				// System.out.println("Found entity (of type LOCATION)  :"
				// +tokens[i]);

				locs.add(tokens[i]);
			}

			/*
			 * else if (tokens[i+1].toString().equals("ORGANIZATION")){
			 * System.out.println("Found entity (of type ORGANIZATION)  :"
			 * +tokens[i]); orgs.add(tokens[i]); }
			 */

		}

		// Converting ArrayList to HashSet to remove duplicates

		// LinkedHashSet<String> listToSetPers = new
		// LinkedHashSet<String>(pers);
		LinkedHashSet<String> listToSetLocs = new LinkedHashSet<String>(locs);
		// LinkedHashSet<String> listToSetOrgs = new
		// LinkedHashSet<String>(orgs);

		// Creating ArrayList without duplicate values

		// List<String> PersWithoutDuplicates = new
		// ArrayList<String>(listToSetPers);
		ArrayList<String> LocsWithoutDuplicates = new ArrayList<String>(
				listToSetLocs);
		// List<String> OrgsWithoutDuplicates = new
		// ArrayList<String>(listToSetOrgs);

		// Print the lists containing unique elements

		// System.out.println("Persons:"+PersWithoutDuplicates);
		System.out.println("Locations:" + LocsWithoutDuplicates);
		// System.out.println("Organizations:"+OrgsWithoutDuplicates);

		// Pass ArrayList to Query

		QueryDatabase.genQuery(LocsWithoutDuplicates, directory);

	}

	public static void main(String[] args) throws IOException, SQLException {

		// String file = "../SDA/InputFiles/Duplex_A_20110907_optimized.ifc";

		ArrayList<String> properties = new ArrayList<String>();

		String file = null;

		if (args.length > 0) {

			file = args[0];
		}

		if (args.length > 1) {
			for (int num = 2; num < args.length; num++) {
				properties.add(args[num]);
			}
			System.out.println("Properties :" + properties);
		}

		AnnotateText.initialize();

		System.out.println("Initialization COMPLETE.");
		System.out.println("Parsing IFC file for LOCATION names...");
		System.out.println("Please wait, this may take a while!");

		String text = AnnotateText.readFile(file, properties);
		AnnotateText.parseForNamedEntities(text, args[1]);
		AnnotateText.writeRDF(args[1]);
		System.out.println("The ENRICHMENT process has successfully completed!");

	}
}
