package org.example.simpledb.index;

import java.util.Map;

import org.example.simpledb.plan.Plan;
import org.example.simpledb.plan.TablePlan;
import org.example.simpledb.query.Constant;
import org.example.simpledb.query.UpdateScan;
import org.example.simpledb.record.RID;
import org.example.simpledb.server.SimpleDB;
import org.example.simpledb.metadata.IndexInfo;
import org.example.simpledb.metadata.MetadataMgr;
import org.example.simpledb.tx.Transaction;

public class IndexRetrievalTest {
   public static void main(String[] args) {
      SimpleDB db = new SimpleDB("studentdb");
      Transaction tx = db.newTx();
      MetadataMgr mdm = db.mdMgr();

      // Open a scan on the data table.
      Plan studentplan = new TablePlan(tx, "student", mdm);
      UpdateScan studentscan = (UpdateScan) studentplan.open();

      // Open the index on MajorId.
      Map<String, IndexInfo> indexes = mdm.getIndexInfo("student", tx);
      IndexInfo ii = indexes.get("majorid");
      Index idx = ii.open();

      // Retrieve all index records having a dataval of 20.
      idx.beforeFirst(new Constant(20));
      while (idx.next()) {
         // Use the datarid to go to the corresponding STUDENT record.
         RID datarid = idx.getDataRid();
         studentscan.moveToRid(datarid);
         System.out.println(studentscan.getString("sname"));
      }

      // Close the index and the data table.
      idx.close();
      studentscan.close();
      tx.commit();
   }
}
