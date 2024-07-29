package com.example.roomlibtesting;

import android.content.Context;

import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.roomlibtesting.account.LAccount;
import com.example.roomlibtesting.account.LAccountDAO;
import com.example.roomlibtesting.file.LFile;
import com.example.roomlibtesting.file.LFileDAO;
import com.example.roomlibtesting.journal.LJournal;
import com.example.roomlibtesting.journal.LJournalDao;

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
	public void testFileInsert() throws ExecutionException, InterruptedException {
		System.out.println("Running file function tests!");

		LFileDAO dao = db.getFileDao();
		LFile firstFile = new LFile(UUID.randomUUID());

		//Test that the loadAll function works
		List<LFile> files = dao.loadAll().get();
		assertTrue(files.isEmpty());



		//Test that a file can be successfully inserted
		System.out.println("INSERT 1 item...");
		dao.insert(firstFile).get();
		files = dao.loadAll().get();
		assertEquals(1, files.size());
		assertEquals(firstFile, files.get(0));


		//Test that an exact match file does not create a duplicate row
		System.out.println("INSERT identical item...");
		dao.insert(firstFile).get();
		files = dao.loadAll().get();
		assertEquals(1, files.size());
		assertEquals(firstFile, files.get(0));


		LFile modifiedFile = files.get(0);
		modifiedFile.isdeleted = true;

		//Test that altering object props but keeping fileuid does NOT create a new row
		System.out.println("INSERT modified item...");
		dao.insert(modifiedFile).get();
		files = dao.loadAll().get();
		assertEquals(1, files.size());
		assertEquals(firstFile, files.get(0));



		LFile secondFile = files.get(0);
		secondFile.fileuid = UUID.randomUUID();

		//Test that an identical file with a different fileuid DOES create a new row
		System.out.println("INSERT item 2...");
		dao.insert(secondFile).get();
		files = dao.loadAll().get();
		assertEquals(2, files.size());
		assertEquals(firstFile, files.get(0));
		assertEquals(secondFile, files.get(1));


		//Test that inserting an identical file again does not create a new row
		System.out.println("INSERT identical item 2...");
		dao.insert(secondFile).get();
		files = dao.loadAll().get();
		assertEquals(2, files.size());
		assertEquals(firstFile, files.get(0));
		assertEquals(secondFile, files.get(1));


		LFile thirdFile = new LFile(UUID.randomUUID());
		LFile fourthFile = new LFile(UUID.randomUUID());
		LFile fifthFile = new LFile(UUID.randomUUID());

		//Test inserting multiple
		System.out.println("INSERT multiple items...");
		dao.insert(thirdFile, fourthFile, fifthFile).get();
		files = dao.loadAll().get();
		assertEquals(5, files.size());
		assertEquals(firstFile, files.get(0));
		assertEquals(secondFile, files.get(1));
		assertEquals(thirdFile, files.get(2));
		assertEquals(fourthFile, files.get(3));
		assertEquals(fifthFile, files.get(4));
	}


	@Test
	public void testFileUpdate() {

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