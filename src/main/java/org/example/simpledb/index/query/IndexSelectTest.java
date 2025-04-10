package org.example.simpledb.index.query;
import org.example.simpledb.plan.Plan;
import org.example.simpledb.plan.TablePlan;
import org.example.simpledb.query.Constant;
import org.example.simpledb.query.Scan;
import org.example.simpledb.record.RID;
import org.example.simpledb.record.TableScan;
import org.example.simpledb.server.SimpleDB;
import org.example.simpledb.index.Index;
import org.example.simpledb.metadata.IndexInfo;
import org.example.simpledb.metadata.MetadataMgr;
import org.example.simpledb.tx.Transaction;
import org.example.simpledb.index.planner.IndexSelectPlan;

import java.util.Map;

// Find the grades of student 6.

public class IndexSelectTest {
	public static void main(String[] args) {
		SimpleDB db = new SimpleDB("studentdb");
      MetadataMgr mdm = db.mdMgr();
      Transaction tx = db.newTx();

		// Find the index on StudentId.
		Map<String, IndexInfo> indexes = mdm.getIndexInfo("enroll", tx);
		IndexInfo sidIdx = indexes.get("studentid");

		// Get the plan for the Enroll table
		Plan enrollplan = new TablePlan(tx, "enroll", mdm);
		
		// Create the selection constant
		Constant c = new Constant(6);
		
		// Two different ways to use the index in simpledb:
		useIndexManually(sidIdx, enrollplan, c);		
		useIndexScan(sidIdx, enrollplan, c);
		
		tx.commit();
	}
	
	private static void useIndexManually(IndexInfo ii, Plan p, Constant c) {
		// Open a scan on the table.
		TableScan s = (TableScan) p.open();  //must be a table scan
		Index idx = ii.open();

		// Retrieve all index records having the specified dataval.
		idx.beforeFirst(c);
		while (idx.next()) {
			// Use the datarid to go to the corresponding Enroll record.
			RID datarid = idx.getDataRid();
			s.moveToRid(datarid);  // table scans can move to a specified RID.
			System.out.println(s.getString("grade"));
		}
		idx.close();
		s.close();
	}
	
	private static void useIndexScan(IndexInfo ii, Plan p, Constant c) {
		// Open an index select scan on the enroll table.
		Plan idxplan = new IndexSelectPlan(p, ii, c);
		Scan s = idxplan.open();
		
		while (s.next()) {
			System.out.println(s.getString("grade"));
		}
		s.close();
	}
}
