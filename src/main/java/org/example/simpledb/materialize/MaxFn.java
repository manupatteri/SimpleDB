package org.example.simpledb.materialize;

import org.example.simpledb.query.Constant;
import org.example.simpledb.query.Scan;

/**
 * The <i>max</i> aggregation function.
 * @author Edward Sciore
 */
public class MaxFn implements AggregationFn {
   private String fldname;
   private Constant val;
   
   /**
    * Create a max aggregation function for the specified field.
    * @param fldname the name of the aggregated field
    */
   public MaxFn(String fldname) {
      this.fldname = fldname;
   }
   
   /**
    * Start a new maximum to be the 
    * field value in the current record.
    * @see simpledb.materialize.AggregationFn#processFirst(simpledb.query.Scan)
    */
   public void processFirst(Scan s) {
      val = s.getVal(fldname);
   }
   
   /**
    * Replace the current maximum by the field value
    * in the current record, if it is higher.
    * @see simpledb.materialize.AggregationFn#processNext(simpledb.query.Scan)
    */
   public void processNext(Scan s) {
      Constant newval = s.getVal(fldname);
      if (newval.compareTo(val) > 0)
         val = newval;
   }
   
   /**
    * Return the field's name, prepended by "maxof".
    * @see simpledb.materialize.AggregationFn#fieldName()
    */
   public String fieldName() {
      return "maxof" + fldname;
   }
   
   /**
    * Return the current maximum.
    * @see simpledb.materialize.AggregationFn#value()
    */
   public Constant value() {
      return val;
   }
}
