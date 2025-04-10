package org.example.simpledb.record;

import static java.sql.Types.INTEGER;
import org.example.simpledb.file.BlockId;
import org.example.simpledb.query.Constant;
import org.example.simpledb.query.UpdateScan;
import org.example.simpledb.tx.Transaction;

/**
 * Provides the abstraction of an arbitrarily large array
 * of records.
 * @author sciore
 */
public class TableScan implements UpdateScan
{
   private Transaction tx;
   private Layout layout;
   private RecordPage rp;
   private String filename;
   private int currentslot;

   public TableScan(Transaction tx, String tblname, Layout layout) {
      this.tx = tx;
      this.layout = layout;
      filename = tblname + ".tbl";
      if (tx.size(filename) == 0)
         moveToNewBlock();
      else 
         moveToBlock(0);
   }

   // Methods that implement Scan

   public void beforeFirst() {
      moveToBlock(0);
   }

   public boolean next() {
      currentslot = rp.nextAfter(currentslot);
      while (currentslot < 0) {
         if (atLastBlock())
            return false;
         moveToBlock(rp.block().number()+1);
         currentslot = rp.nextAfter(currentslot);
      }
      return true;
   }

   public int getInt(String fldname) {
      return rp.getInt(currentslot, fldname);
   }

   public String getString(String fldname) {
      return rp.getString(currentslot, fldname);
   }

   public Constant getVal(String fldname) {
      if (layout.schema().type(fldname) == INTEGER)
         return new Constant(getInt(fldname));
      else
         return new Constant(getString(fldname));
   }

   public boolean hasField(String fldname) {
      return layout.schema().hasField(fldname);
   }

   public void close() {
      if (rp != null)
         tx.unpin(rp.block());
   }

   // Methods that implement UpdateScan

   public void setInt(String fldname, int val) {
      rp.setInt(currentslot, fldname, val);
   }
   
   public void setString(String fldname, String val) {
      rp.setString(currentslot, fldname, val);
   }

   public void setVal(String fldname, Constant val) {
      if (layout.schema().type(fldname) == INTEGER)
         setInt(fldname, val.asInt());
      else
         setString(fldname, val.asString());
   }

   public void insert() {
      currentslot = rp.insertAfter(currentslot);
      while (currentslot < 0) {
         if (atLastBlock()) 
            moveToNewBlock();
         else 
            moveToBlock(rp.block().number()+1);
         currentslot = rp.insertAfter(currentslot);
      }
   }

   public void delete() {
      rp.delete(currentslot);
   }

   public void moveToRid(RID rid) {
      close();
      BlockId blk = new BlockId(filename, rid.blockNumber());
      rp = new RecordPage(tx, blk, layout);
      currentslot = rid.slot();
   }

   public RID getRid() {
      return new RID(rp.block().number(), currentslot);
   }

   // Private auxiliary methods

   private void moveToBlock(int blknum) {
      close();
      BlockId blk = new BlockId(filename, blknum);
      rp = new RecordPage(tx, blk, layout);
      currentslot = -1;
   }

   private void moveToNewBlock() {
      close();
      BlockId blk = tx.append(filename);
      rp = new RecordPage(tx, blk, layout);
      rp.format();
      currentslot = -1;
   }

   private boolean atLastBlock() {
      return rp.block().number() == tx.size(filename) - 1;
   }
}
