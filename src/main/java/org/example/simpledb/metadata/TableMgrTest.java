package org.example.simpledb.metadata;

import org.example.simpledb.record.Layout;
import org.example.simpledb.record.Schema;
import org.example.simpledb.server.SimpleDB;
import static java.sql.Types.INTEGER;
import org.example.simpledb.tx.Transaction;

public class TableMgrTest {
   public static void main(String[] args) throws Exception {
      SimpleDB db = new SimpleDB("tblmgrtest", 400, 8);
      Transaction tx = db.newTx();
      TableMgr tm = new TableMgr(true, tx);

      Schema sch = new Schema();
      sch.addIntField("A");
      sch.addStringField("B", 9);
      tm.createTable("MyTable", sch, tx);

      Layout layout = tm.getLayout("MyTable", tx);
      int size = layout.slotSize();
      Schema sch2 = layout.schema();
      System.out.println("MyTable has slot size " + size);
      System.out.println("Its fields are:");
      for (String fldname : sch2.fields()) {
         String type;
         if (sch2.type(fldname) == INTEGER)
            type = "int";
         else {
            int strlen = sch2.length(fldname);
            type = "varchar(" + strlen + ")";
         }
         System.out.println(fldname + ": " + type);
      }
      tx.commit();
   }
}

