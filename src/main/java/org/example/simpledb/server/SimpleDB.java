package org.example.simpledb.server;

import java.io.File;
import org.example.simpledb.file.FileMgr;
import org.example.simpledb.log.LogMgr;
import org.example.simpledb.buffer.BufferMgr;
import org.example.simpledb.plan.BasicQueryPlanner;
import org.example.simpledb.plan.BasicUpdatePlanner;
import org.example.simpledb.plan.Planner;
import org.example.simpledb.plan.QueryPlanner;
import org.example.simpledb.plan.UpdatePlanner;
import org.example.simpledb.tx.Transaction;
import org.example.simpledb.metadata.MetadataMgr;


/**
 * The class that configures the system.
 * 
 * @author Edward Sciore
 */
public class SimpleDB {
   public static int BLOCK_SIZE = 400;
   public static int BUFFER_SIZE = 8;
   public static String LOG_FILE = "simpledb.log";

   private  FileMgr     fm;
   private  BufferMgr   bm;
   private  LogMgr      lm;
   private  MetadataMgr mdm;
   private Planner planner;

   /**
    * A constructor useful for debugging.
    * @param dirname the name of the database directory
    * @param blocksize the block size
    * @param buffsize the number of buffers
    */
   public SimpleDB(String dirname, int blocksize, int buffsize) {
      File dbDirectory = new File(dirname);
      fm = new FileMgr(dbDirectory, blocksize);
      lm = new LogMgr(fm, LOG_FILE);
      bm = new BufferMgr(fm, lm, buffsize); 
   }
   
   /**
    * A simpler constructor for most situations. Unlike the
    * 3-arg constructor, it also initializes the metadata tables.
    * @param dirname the name of the database directory
    */
   public SimpleDB(String dirname) {
      this(dirname, BLOCK_SIZE, BUFFER_SIZE); 
      Transaction tx = newTx();
      boolean isnew = fm.isNew();
      if (isnew)
         System.out.println("creating new database");
      else {
         System.out.println("recovering existing database");
         tx.recover();
      }
      mdm = new MetadataMgr(isnew, tx);
      QueryPlanner qp = new BasicQueryPlanner(mdm);
      UpdatePlanner up = new BasicUpdatePlanner(mdm);
//    QueryPlanner qp = new HeuristicQueryPlanner(mdm);
//    UpdatePlanner up = new IndexUpdatePlanner(mdm);
      planner = new Planner(qp, up);
      tx.commit();
   }
   
   /**
    * A convenient way for clients to create transactions
    * and access the metadata.
    */
   public Transaction newTx() {
      return new Transaction(fm, lm, bm);
   }
   
   public MetadataMgr mdMgr() {
      return mdm;
   }
   
   public Planner planner() {
      return planner;
   }

   // These methods aid in debugging
   public FileMgr fileMgr() {
      return fm;
   }   
   public LogMgr logMgr() {
      return lm;
   }   
   public BufferMgr bufferMgr() {
      return bm;
   }
   public void printFileMgrStats(String stage) {
      System.out.println("Stage=" + stage + "|readCount=" + fm.getReadCount() + "|writeCount=" + fm.getWriteCount());
      fm.resetCounts();
   }
 }
