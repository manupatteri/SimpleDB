import org.example.simpledb.tx.Transaction;
import org.example.simpledb.plan.Plan;
import org.example.simpledb.plan.Planner;
import org.example.simpledb.query.*;
import org.example.simpledb.server.SimpleDB;

/* This is a version of the StudentMajor program that
 * accesses the SimpleDB classes directly (instead of
 * connecting to it as a JDBC client). 
 * 
 * These kind of programs are useful for debugging
 * your changes to the SimpleDB source code.
 */

public class StudentMajorNoJDBC {
   public static void main(String[] args) {
      try {
         // analogous to the driver
         SimpleDB db = new SimpleDB("studentdb");

         // analogous to the connection
         Transaction tx  = db.newTx();
         Planner planner = db.planner();
         
         // analogous to the statement
         String qry = "select SName, DName "
               + "from DEPT, STUDENT "
               + "where MajorId = DId";
         Plan p = planner.createQueryPlan(qry, tx);
         
         // analogous to the result set
         Scan s = p.open();
         
         System.out.println("Name\tMajor");
         while (s.next()) {
            String sname = s.getString("sname"); //SimpleDB stores field names
            String dname = s.getString("dname"); //in lower case
            System.out.println(sname + "\t" + dname);
         }
         s.close();
         tx.commit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }
}
