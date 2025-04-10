package org.example.simpledb.tx.recovery;

import java.util.Iterator;
import org.example.simpledb.server.SimpleDB;
import org.example.simpledb.file.BlockId;
import org.example.simpledb.file.FileMgr;
import org.example.simpledb.file.Page;
import org.example.simpledb.log.LogMgr;

public class PrintLogFile {
   public static void main(String[] args) {
      SimpleDB db = new SimpleDB("studentdb", 400, 8);
      FileMgr fm = db.fileMgr();
      LogMgr lm = db.logMgr();
      String filename = "simpledb.log";

      int lastblock = fm.length(filename) - 1;
      BlockId blk = new BlockId(filename, lastblock);
      Page p = new Page(fm.blockSize());
      fm.read(blk, p);
      Iterator<byte[]> iter = lm.iterator();
      while (iter.hasNext()) {
         byte[] bytes = iter.next();
         LogRecord rec = LogRecord.createLogRecord(bytes);
         System.out.println(rec);
      }
   }
}
