package org.example.simpledb.jdbc.network;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.example.simpledb.plan.Planner;
import org.example.simpledb.server.SimpleDB;
import org.example.simpledb.tx.Transaction;

/**
 * The RMI server-side implementation of RemoteConnection.
 * @author Edward Sciore
 */
@SuppressWarnings("serial") 
class RemoteConnectionImpl extends UnicastRemoteObject implements RemoteConnection {
   private SimpleDB db;
   private Transaction currentTx;
   private Planner planner;
   
   /**
    * Creates a remote connection
    * and begins a new transaction for it.
    * @throws RemoteException
    */
   RemoteConnectionImpl(SimpleDB db) throws RemoteException {
      this.db = db;
      currentTx = db.newTx();
      planner = db.planner();
   }
   
   /**
    * Creates a new RemoteStatement for this connection.
    * @see simpledb.jdbc.network.RemoteConnection#createStatement()
    */
   public RemoteStatement createStatement() throws RemoteException {
      return new RemoteStatementImpl(this, planner);
   }
   
   /**
    * Closes the connection.
    * The current transaction is committed.
    * @see simpledb.jdbc.network.RemoteConnection#close()
    */
   public void close() throws RemoteException {
      currentTx.commit();
   }
   
// The following methods are used by the server-side classes.
   
   /**
    * Returns the transaction currently associated with
    * this connection.
    * @return the transaction associated with this connection
    */
   Transaction getTransaction() {  
      return currentTx;
   }
   
   /**
    * Commits the current transaction,
    * and begins a new one.
    */
   void commit() {
      currentTx.commit();
      db.printFileMgrStats("COMMIT");
      currentTx = db.newTx();
   }
   
   /**
    * Rolls back the current transaction,
    * and begins a new one.
    */
   void rollback() {
      currentTx.rollback();
      db.printFileMgrStats("ROLLBACK");
      currentTx = db.newTx();
   }
}

