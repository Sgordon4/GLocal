package com.example.roomlibtesting;

import android.content.Context;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.roomlibtesting.account.LAccount;
import com.example.roomlibtesting.account.LAccountDAO;
import com.example.roomlibtesting.file.LFile;
import com.example.roomlibtesting.file.LFileDAO;
import com.example.roomlibtesting.journal.LJournal;
import com.example.roomlibtesting.journal.LJournalDao;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class TestDatabases {

	Context context;
	LocalDatabase db;
	@Before
	public void setUp() {
		context = InstrumentationRegistry.getInstrumentation().getTargetContext();
		assertEquals("com.example.roomlibtesting", context.getPackageName());

		db = new LocalDatabase.DBBuilder().newInstance(context);

		//Records carry over between tests
		deleteAll();
	}

	public void deleteAll() {
		SupportSQLiteDatabase writeDB = db.getOpenHelper().getWritableDatabase();

		writeDB.delete("account", "1", null);
		writeDB.delete("file", "1", null);
		writeDB.delete("block", "1", null);
		writeDB.delete("journal", "1", null);
	}


	public void triggered() {

	}




	@Test
	public void testAccountFileTrigger() throws ExecutionException, InterruptedException {
		LAccountDAO acctDao = db.getAccountDao();
		LFileDAO fileDao = db.getFileDao();



		System.out.println("Current files:");
		printFiles();

		LAccount new1 = new LAccount();
		acctDao.insert(new1);
		System.out.println("After inserting account:");
		printFiles();

		acctDao.insert(new1);
		System.out.println("After inserting account AGAIN:");
		printFiles();

	}





	@Test
	public void testFilePut() throws ExecutionException, InterruptedException {
		System.out.println("Testing file PUT (INSERT/UPDATE) functionality!");

		LFileDAO dao = db.getFileDao();

		List<LFile> files = dao.loadAll().get();
		assertTrue(files.isEmpty());							//Ensure we start with 0 rows


		//------------------------------------------------------
		LFile firstFile = new LFile(UUID.randomUUID());			//Create a default file to insert
		firstFile.isdir = !firstFile.isdir;						//Ensure file has some non-default data

		System.out.println("Putting initial item...");
		dao.put(firstFile).get();								//Put() our first file
		files = dao.loadAll().get();
		assertEquals(1, files.size());					//Ensure a new row was created
		assertEquals(firstFile, files.get(0));					//Ensure all data was preserved


		//------------------------------------------------------
		System.out.println("Putting identical item...");
		dao.put(firstFile).get();								//Put() our first file AGAIN, unchanged
		files = dao.loadAll().get();
		assertEquals(1, files.size());					//Ensure a new row was NOT created
		assertEquals(firstFile, files.get(0));					//Ensure all data was preserved


		//------------------------------------------------------
		LFile firstFileOld = files.get(0);						//Save the current file attr
		firstFile.isdeleted = true;								//Ensure file has some non-default data

		System.out.println("Putting modified item...");
		dao.put(firstFile).get();								//Put() our MODIFIED file
		files = dao.loadAll().get();
		assertEquals(1, files.size());					//Make sure a new row was not created
		assertEquals(firstFile.fileuid, files.get(0).fileuid);	//Make sure it's the same file
		assertNotEquals(firstFileOld, files.get(0));			//But that it's not exactly the same
		assertEquals(firstFile, files.get(0));					//And the updates have been preserved


		//------------------------------------------------------
		LFile secondFile = new LFile(UUID.randomUUID());		//Make a second default file
		secondFile.modifytime = new Date().getTime();			//Modify the file slightly

		System.out.println("Putting item 2...");
		dao.put(secondFile).get();								//Put() our second file
		files = dao.loadAll().get();
		assertEquals(2, files.size());					//Make sure there are 2 rows now
		assertEquals(firstFile.fileuid, files.get(0).fileuid);	//Ensure the first file still exists
		assertEquals(firstFile, files.get(0));					//And its data has been preserved
		assertEquals(secondFile.fileuid, files.get(1).fileuid);	//Ensure the second file exists
		assertEquals(secondFile, files.get(1));					//And its data has been preserved


		//------------------------------------------------------
		//Test that inserting an identical file again does not create a new row
		System.out.println("Putting identical item 2...");
		dao.put(secondFile).get();								//Put() our second file AGAIN, unchanged
		files = dao.loadAll().get();
		assertEquals(2, files.size());					//Ensure a new row was NOT created
		assertEquals(firstFile, files.get(0));					//Ensure all data was preserved
		assertEquals(secondFile, files.get(1));					//...


		//------------------------------------------------------
		LFile thirdFile = new LFile(UUID.randomUUID());			//Create 2 default files
		LFile fourthFile = new LFile(UUID.randomUUID());		//...
		LFile fifthFile = new LFile(UUID.randomUUID());			//We need to check arraylist type too
		fifthFile.fileblocks = Arrays.asList("FirstBlock, SecondBlock", "ThirdBlock");

		System.out.println("Putting multiple NEW items...");
		dao.put(thirdFile, fourthFile, fifthFile).get();		//Put() all 3 files at once
		files = dao.loadAll().get();
		assertEquals(5, files.size());					//Ensure 3 new rows were created
		assertEquals(firstFile, files.get(0));					//Ensure all data was preserved
		assertEquals(secondFile, files.get(1));					//...
		assertEquals(thirdFile.fileuid, files.get(2).fileuid);	//Check the 3rd file has been added
		assertEquals(thirdFile, files.get(2));					//And its data has been preserved
		assertEquals(fourthFile.fileuid, files.get(3).fileuid);	//Check the 4th file has been added
		assertEquals(fourthFile, files.get(3));					//And its data has been preserved
		assertEquals(fifthFile.fileuid, files.get(4).fileuid);	//Check the 5th file has been added
		assertEquals(fifthFile, files.get(4));					//And its data has been preserved


		//------------------------------------------------------
		LFile sixthFile = new LFile(UUID.randomUUID());			//Create 1 default file
		fifthFile.isdeleted = true;								//Modify a file slightly

		System.out.println("Putting multiple DIFFERENT items...");
		dao.put(fourthFile, fifthFile, sixthFile).get();		//Put() 3 files: unchanged, modified, new
		files = dao.loadAll().get();
		assertEquals(6, files.size());					//Ensure only 1 new row was created
		assertEquals(firstFile, files.get(0));					//Ensure the old data was preserved
		assertEquals(secondFile, files.get(1));					//...
		assertEquals(thirdFile, files.get(2));					//...
		assertEquals(fourthFile, files.get(3));					//Ensure the old file remains unchanged
		assertEquals(fifthFile, files.get(4));					//Ensure the updated file was updated
		assertEquals(sixthFile, files.get(5));					//Ensure the new file was added
	}


	@Test
	public void testFileDelete() {

	}

	@Test
	public void testFileLoads() {

	}



	@Test
	public void testJournalTriggers() {

	}


	@Test
	public void testJournalFunctions() {

	}



	@Test
	public void testBlockFunctions() {

	}





	public void printFiles() throws ExecutionException, InterruptedException {
		LFileDAO dao = db.getFileDao();
		List<LFile> files = dao.loadAll().get();
		System.out.println("Size: "+files.size());
		for(LFile file : files) {
			System.out.println(file);
		}
	}


	public void printJournal() throws ExecutionException, InterruptedException {
		LJournalDao dao = db.getJournalDao();
		List<LJournal> entries = dao.loadAllAfterID(-1).get();
		System.out.println("Journals: "+entries.size());
		for(LJournal entry : entries) {
			System.out.println(entry);
		}
	}


}