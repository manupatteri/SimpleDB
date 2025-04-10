package org.example.simpledb.multibuffer;

import org.example.simpledb.materialize.MaterializePlan;
import org.example.simpledb.materialize.TempTable;
import org.example.simpledb.query.Scan;
import org.example.simpledb.query.UpdateScan;
import org.example.simpledb.record.Schema;
import org.example.simpledb.tx.Transaction;
import org.example.simpledb.plan.Plan;

/**
 * The Plan class for the multi-buffer version of the
 * <i>product</i> operator.
 * @author Edward Sciore
 */
public class MultibufferProductPlan implements Plan {
   private Transaction tx;
   private Plan lhs, rhs;
   private Schema schema = new Schema();

   /**
    * Creates a product plan for the specified queries.
    * @param lhs the plan for the LHS query
    * @param rhs the plan for the RHS query
    * @param tx the calling transaction
    */
   public MultibufferProductPlan(Transaction tx, Plan lhs, Plan rhs) {
      this.tx = tx;
      this.lhs = new MaterializePlan(tx, lhs);
      this.rhs = rhs;
      schema.addAll(lhs.schema());
      schema.addAll(rhs.schema());
   }

   /**
    * A scan for this query is created and returned, as follows.
    * First, the method materializes its LHS and RHS queries.
    * It then determines the optimal chunk size,
    * based on the size of the materialized RHS file and the
    * number of available buffers.
    * It creates a chunk plan for each chunk, saving them in a list.
    * Finally, it creates a multiscan for this list of plans,
    * and returns that scan.
    * @see simpledb.plan.Plan#open()
    */
   public Scan open() {
      Scan leftscan = lhs.open();
      TempTable tt = copyRecordsFrom(rhs);
      return new MultibufferProductScan(tx, leftscan, tt.tableName(), tt.getLayout());
   }

   /**
    * Returns an estimate of the number of block accesses
    * required to execute the query. The formula is:
    * <pre> B(product(p1,p2)) = B(p2) + B(p1)*C(p2) </pre>
    * where C(p2) is the number of chunks of p2.
    * The method uses the current number of available buffers
    * to calculate C(p2), and so this value may differ
    * when the query scan is opened.
    * @see simpledb.plan.Plan#blocksAccessed()
    */
   public int blocksAccessed() {
      // this guesses at the # of chunks
      int avail = tx.availableBuffs();
      int size = new MaterializePlan(tx, rhs).blocksAccessed();
      int numchunks = size / avail;
      return rhs.blocksAccessed() +
            (lhs.blocksAccessed() * numchunks);
   }

   /**
    * Estimates the number of output records in the product.
    * The formula is:
    * <pre> R(product(p1,p2)) = R(p1)*R(p2) </pre>
    * @see simpledb.plan.Plan#recordsOutput()
    */
   public int recordsOutput() {
      return lhs.recordsOutput() * rhs.recordsOutput();
   }

   /**
    * Estimates the distinct number of field values in the product.
    * Since the product does not increase or decrease field values,
    * the estimate is the same as in the appropriate underlying query.
    * @see simpledb.plan.Plan#distinctValues(java.lang.String)
    */
   public int distinctValues(String fldname) {
      if (lhs.schema().hasField(fldname))
         return lhs.distinctValues(fldname);
      else
         return rhs.distinctValues(fldname);
   }

   /**
    * Returns the schema of the product,
    * which is the union of the schemas of the underlying queries.
    * @see simpledb.plan.Plan#schema()
    */
   public Schema schema() {
      return schema;
   }

   private TempTable copyRecordsFrom(Plan p) {
      Scan   src = p.open(); 
      Schema sch = p.schema();
      TempTable t = new TempTable(tx, sch);
      UpdateScan dest = (UpdateScan) t.open();
      while (src.next()) {
         dest.insert();
         for (String fldname : sch.fields())
            dest.setVal(fldname, src.getVal(fldname));
      }
      src.close();
      dest.close();
      return t;
   }
}
