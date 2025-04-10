package org.example.simpledb.index.btree;

import org.example.simpledb.file.BlockId;
import org.example.simpledb.query.Constant;
import org.example.simpledb.tx.Transaction;
import org.example.simpledb.record.Layout;

/**
 * A B-tree directory block.
 * @author Edward Sciore
 */
public class BTreeDir {
   private Transaction tx;
   private Layout layout;
   private BTPage contents;
   private String filename;

   /**
    * Creates an object to hold the contents of the specified
    * B-tree block.
    * @param blk a reference to the specified B-tree block
    * @param layout the metadata of the B-tree directory file
    * @param tx the calling transaction
    */
   BTreeDir(Transaction tx, BlockId blk, Layout layout) {
      this.tx = tx;
      this.layout = layout;
      contents = new BTPage(tx, blk, layout);
      filename = blk.fileName();
   }

   /**
    * Closes the directory page.
    */
   public void close() {
      contents.close();
   }

   /**
    * Returns the block number of the B-tree leaf block
    * that contains the specified search key.
    * @param searchkey the search key value
    * @return the block number of the leaf block containing that search key
    */
   public int search(Constant searchkey) {
      BlockId childblk = findChildBlock(searchkey);
      while (contents.getFlag() > 0) {
         contents.close();
         contents = new BTPage(tx, childblk, layout);
         childblk = findChildBlock(searchkey);
      }
      return childblk.number();
   }

   /**
    * Creates a new root block for the B-tree.
    * The new root will have two children:
    * the old root, and the specified block.
    * Since the root must always be in block 0 of the file,
    * the contents of the old root will get transferred to a new block.
    * @param e the directory entry to be added as a child of the new root
    */
   public void makeNewRoot(DirEntry e) {
      Constant firstval = contents.getDataVal(0);
      int level = contents.getFlag();
      BlockId newblk = contents.split(0, level); //ie, transfer all the records
      DirEntry oldroot = new DirEntry(firstval, newblk.number());
      insertEntry(oldroot);
      insertEntry(e);
      contents.setFlag(level+1);
   }

   /**
    * Inserts a new directory entry into the B-tree block.
    * If the block is at level 0, then the entry is inserted there.
    * Otherwise, the entry is inserted into the appropriate
    * child node, and the return value is examined.
    * A non-null return value indicates that the child node
    * split, and so the returned entry is inserted into
    * this block.
    * If this block splits, then the method similarly returns
    * the entry information of the new block to its caller;
    * otherwise, the method returns null.
    * @param e the directory entry to be inserted
    * @return the directory entry of the newly-split block, if one exists; otherwise, null
    */
   public DirEntry insert(DirEntry e) {
      if (contents.getFlag() == 0)
         return insertEntry(e);
      BlockId childblk = findChildBlock(e.dataVal());
      BTreeDir child = new BTreeDir(tx, childblk, layout);
      DirEntry myentry = child.insert(e);
      child.close();
      return (myentry != null) ? insertEntry(myentry) : null;
   }

   private DirEntry insertEntry(DirEntry e) {
      int newslot = 1 + contents.findSlotBefore(e.dataVal());
      contents.insertDir(newslot, e.dataVal(), e.blockNumber());
      if (!contents.isFull())
         return null;
      // else page is full, so split it
      int level = contents.getFlag();
      int splitpos = contents.getNumRecs() / 2;
      Constant splitval = contents.getDataVal(splitpos);
      BlockId newblk = contents.split(splitpos, level);
      return new DirEntry(splitval, newblk.number());
   }

   private BlockId findChildBlock(Constant searchkey) {
      int slot = contents.findSlotBefore(searchkey);
      if (contents.getDataVal(slot+1).equals(searchkey))
         slot++;
      int blknum = contents.getChildNum(slot);
      return new BlockId(filename, blknum);
   }
}
