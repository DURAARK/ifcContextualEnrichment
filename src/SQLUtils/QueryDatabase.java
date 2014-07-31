package SQLUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.jdbc.ScriptRunner;



public class QueryDatabase {

    public static final String USERNAME = null;
    public static final String PASSWORD = null;

    public List<String> getTermsForEnrichment(String fileInput) {
        List<String> terms = new ArrayList<String>();

        try {
            FileInputStream fstream = new FileInputStream(fileInput);

            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
                String[] word = strLine.split("\t");
                if(terms.contains(word)) {
                    break;
                } else {
                    terms.add(strLine);
                }

            }
            //Close the input stream
            in.close();

        } catch (Exception e) { //Catch exception if any
            e.printStackTrace();
        }
        System.out.println("TERMS: "+terms);
        return terms;


    }


    public void runSQLscript() throws ClassNotFoundException, SQLException {
        String SQLScriptFilePath = "Queries/example.sql";

        // Establish MySQL Connection
        Class.forName("com.mysql.jdbc.Driver");
        Connection con = DriverManager.getConnection(
                             "jdbc:mysql://db.l3s.uni-hannover.de:3307/ld_dataset_crawler", USERNAME, PASSWORD);

        try {
            // Initialize object for ScripRunner
            ScriptRunner sr = new ScriptRunner(con);

            // Give the input file to Reader
            Reader reader = new BufferedReader(
                new FileReader(SQLScriptFilePath));

            // Execute script
            sr.runScript(reader);
        } catch (Exception e) {
            System.err.println("Failed to Execute" + SQLScriptFilePath
                               + " The error is " + e.getMessage());
        }

    }

    public static ResultSet getResultsFromQuery(String Query) throws SQLException {

        try {
            Connection conn = DriverManager.getConnection(
                                  "jdbc:mysql://db.l3s.uni-hannover.de:3307/ld_dataset_crawler", USERNAME, PASSWORD) ;
            Statement stmt = conn.createStatement() ;

            String[] pkgPath = { "ifcEnrichment" };
            File f = new File(System.getProperty("user.home"));
            File subDir = f;
            for (String pkg : pkgPath) {
                subDir = new File(subDir,pkg);
            }

            // System.out.println(f.getAbsoluteFile());
            //System.out.println(subDir.getAbsoluteFile());

            PrintWriter writer = new PrintWriter
            (new OutputStreamWriter
             (new BufferedOutputStream
              (new FileOutputStream(subDir.getAbsoluteFile()+"/results.txt",true)), "UTF-8"));
            //String query = "select * from results_1000 ;" ;
            String query = Query;
            ResultSet results = stmt.executeQuery(query) ;

            while (results.next()) {

                writer.append(results.getString("dataset_id")).append(",")
                .append(results.getString("dataset_name")).append(",")
                .append(results.getString("resource_id")).append(",")
                .append(results.getString("resource_uri")).append(",")
                .append(results.getString("property_uri")).append(",")
                .append(results.getString("value")).append("\n");
            }
            conn.close();
            stmt.close();
            results.close();
            writer.close();
            return results;
        }

        catch (Exception exc) {
            exc.printStackTrace();
        }
        return null;
    }

    public static void genQuery(ArrayList<String> pivotArr) throws SQLException {

        ArrayList<String> pivots = pivotArr;

        for(int i=0; i<pivots.size(); i++) {

            String q = "select ri.dataset_id, d.dataset_name, rv.resource_id, ri.resource_uri, rv.property_uri, rv.value "
                       + "from dataset d,"	+ "resource_instances ri,"+ " resource_values rv"
                       + " where (d.dataset_id=ri.dataset_id and ri.resource_id=rv.resource_id and rv.value like '%"
                       + pivots.get(i)+"%');";

            //To retrieve particular types of triples (eg. related to energy), add : and ri.resource_uri like '%energy%'

            getResultsFromQuery(q);
        }

    }





    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        /*

        QueryDatabase obj = new QueryDatabase();
        obj.getTermsForEnrichment("InputFiles/TermsForEnrichment.txt");


        // Run an SQL script from a file
        obj.runSQLscript();
        QueryDatabase.getResultsFromQuery(q);

         */
    }


}

