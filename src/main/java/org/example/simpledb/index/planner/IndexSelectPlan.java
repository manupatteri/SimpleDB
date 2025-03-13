package org.example.simpledb.index.planner;

import org.example.simpledb.metadata.IndexInfo;
import org.example.simpledb.plan.Plan;
import org.example.simpledb.index.Index;
import org.example.simpledb.index.query.IndexSelectScan;
import org.example.simpledb.query.Constant;
import org.example.simpledb.query.Scan;
import org.example.simpledb.record.Schema;
import org.example.simpledb.record.TableScan;

/** The Plan class corresponding to the <i>indexselect</i>
  * relational algebra operator.
  * @author Edward Sciore
  */
public class IndexSelectPlan implements Plan {
   private Plan p;
   private IndexInfo ii;
   private Constant val;
   
   /**
    * Creates a new indexselect node in the query tree
    * for the specified index and selection constant.
    * @param p the input table
    * @param ii information about the index
    * @param val the selection constant
    * @param tx the calling transaction 
    */
   public IndexSelectPlan(Plan p, IndexInfo ii, Constant val) {
      this.p = p;
      this.ii = ii;
      this.val = val;
   }
   
   /** 
    * Creates a new indexselect scan for this query
    * @see simpledb.plan.Plan#open()
    */
   public Scan open() {
      // throws an exception if p is not a tableplan.
      TableScan ts = (TableScan) p.open();
      Index idx = ii.open();
      return new IndexSelectScan(ts, idx, val);
   }
   
   /**
    * Estimates the number of block accesses to compute the 
    * index selection, which is the same as the 
    * index traversal cost plus the number of matching data records.
    * @see simpledb.plan.Plan#blocksAccessed()
    */
   public int blocksAccessed() {
      return ii.blocksAccessed() + recordsOutput();
   }
   
   /**
    * Estimates the number of output records in the index selection,
    * which is the same as the number of search key values
    * for the index.
    * @see simpledb.plan.Plan#recordsOutput()
    */
   public int recordsOutput() {
      return ii.recordsOutput();
   }
   
   /** 
    * Returns the distinct values as defined by the index.
    * @see simpledb.plan.Plan#distinctValues(java.lang.String)
    */
   public int distinctValues(String fldname) {
      return ii.distinctValues(fldname);
   }
   
   /**
    * Returns the schema of the data table.
    * @see simpledb.plan.Plan#schema()
    */
   public Schema schema() {
      return p.schema(); 
   }
}
