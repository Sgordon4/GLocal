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

import java.util.ArrayList;
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
public class TestFileDatabase {

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




	@Test
	public void testFileLoads() throws ExecutionException, InterruptedException {
		LFileDAO fileDao = db.getFileDao();

		//Make 600 random default test files
		List<LFile> testFiles = new ArrayList<>();
		for(int i = 0; i < 600; i++) {
			testFiles.add(new LFile(UUID.randomUUID()));
		}

		//The row count limit on the query is 500 (see LFileDAO), and we need to test that as well as offsets,
		// so I'm making a set of 20 files right on the line of 500 to test both
		int startIndex = 491;

		//Set 20 or so to one common account UUID
		UUID commonAccountUUID = UUID.randomUUID();
		for(int i = startIndex; i < startIndex+20; i++) {
			testFiles.get(i).accountuid = commonAccountUUID;
		}
		//Give some of the files some non-standard data (I'm just picking 2 random ones)
		testFiles.get(startIndex+13).accesstime = new Date().getTime();
		testFiles.get(startIndex+18).fileblocks = new ArrayList<>(Arrays.asList("FirstBlock", "SecondBlock"));

		//Add them all to the table
		fileDao.put(testFiles.toArray(new LFile[0])).get();


		//------------------------------------------------------

		//Grab all files with the common accountuid
		List<LFile> byAccount = fileDao.loadAllByAccount(commonAccountUUID).get();
		assertEquals(20, byAccount.size());			//Ensure we have 20 files
		for(int i = 0; i < 20; i++) {
			LFile file = byAccount.get(i);
			LFile expected = testFiles.get(startIndex+i);

			assertEquals(commonAccountUUID, file.accountuid);	//Ensure each file has the correct accountuid
			assertEquals(expected, file);						//Ensure the files have retained their data
		}


		//Grab all files with the common accountuid, but offset by 10
		List<LFile> byAccountOff = fileDao.loadAllByAccount(10, commonAccountUUID).get();
		assertEquals(10, byAccountOff.size());			//Ensure we have only 10/20 files
		for(int i = 0; i < 10; i++) {
			LFile file = byAccountOff.get(i);
			LFile expected = testFiles.get(startIndex+10+i);

			assertEquals(commonAccountUUID, file.accountuid);	//Ensure each file has the correct accountuid
			assertEquals(expected, file);						//Ensure the files have retained their data
		}


		//------------------------------------------------------

		//Get a single file by its UUID
		LFile expected = testFiles.get(startIndex+13);
		List<LFile> actual = fileDao.loadByUID(expected.fileuid).get();
		assertEquals(1, actual.size());				//Ensure we only have 1 file
		assertEquals(expected, actual.get(0));					//Ensure the file has retained its data


		//Get multiple files by UUID
		LFile expected2 = testFiles.get(startIndex+18);
		List<LFile> actual2 = fileDao.loadByUID(expected.fileuid, expected2.fileuid).get();
		assertEquals(2, actual2.size());				//Ensure we have 2 files
		assertEquals(expected, actual2.get(0));					//Ensure the first file has retained its data
		assertEquals(expected2, actual2.get(1));				//And the second file has retained its data

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
		fifthFile.fileblocks = Arrays.asList("FirstBlock", "SecondBlock", "ThirdBlock", "Has: some. punctuation,,");

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
	public void testDelete() throws ExecutionException, InterruptedException {
		
		LFileDAO dao = db.getFileDao();

		List<LFile> files = dao.loadAll().get();
		assertTrue(files.isEmpty());							//Ensure we start with 0 rows


		//------------------------------------------------------
		//Delete single

		//Make a bunch of files
		LFile firstFile = new LFile(UUID.randomUUID());
		LFile secondFile = new LFile(UUID.randomUUID());
		LFile thirdFile = new LFile(UUID.randomUUID());
		LFile fourthFile = new LFile(UUID.randomUUID());
		LFile fifthFile = new LFile(UUID.randomUUID());

		//Put them all in the database
		System.out.println("Putting multiple items...");
		dao.put(firstFile, secondFile, thirdFile, fourthFile, fifthFile).get();

		files = dao.loadAll().get();
		assertEquals(5, files.size());					//Ensure we have 5 files


		System.out.println("Deleting single item...");
		dao.delete(secondFile).get();							//Delete the second file
		files = dao.loadAll().get();
		assertEquals(4, files.size());					//Ensure we now have 4 files
		assertTrue(files.contains(firstFile));					//Ensure all files are present
		assertFalse(files.contains(secondFile));				//EXCEPT file2
		assertTrue(files.contains(thirdFile));					//...
		assertTrue(files.contains(fourthFile));					//...
		assertTrue(files.contains(fifthFile));					//...


		System.out.println("Putting single item...");
		dao.put(secondFile).get();								//Insert the second file again
		files = dao.loadAll().get();
		assertEquals(5, files.size());					//Ensure we now have 5 files
		assertTrue(files.contains(firstFile));					//Ensure all files are present
		assertTrue(files.contains(secondFile));					//INCLUDING file2
		assertTrue(files.contains(thirdFile));					//...
		assertTrue(files.contains(fourthFile));					//...
		assertTrue(files.contains(fifthFile));					//...


		System.out.println("Deleting single item again...");
		dao.delete(secondFile).get();							//Delete the second file again
		files = dao.loadAll().get();
		assertEquals(4, files.size());					//Ensure we now have 4 files


		//------------------------------------------------------
		//Delete multiple

		System.out.println("Deleting multiple items...");
		dao.delete(secondFile, thirdFile, fifthFile).get();		//Delete multiple files
		files = dao.loadAll().get();
		assertEquals(2, files.size());					//Ensure we now have 2 files
		assertTrue(files.contains(firstFile));					//Ensure all files are present
		assertFalse(files.contains(secondFile));				//EXCEPT file2
		assertFalse(files.contains(thirdFile));					//file3
		assertTrue(files.contains(fourthFile));					//...
		assertFalse(files.contains(fifthFile));					//and file5


		System.out.println("Putting multiple items...");
		dao.put(secondFile, thirdFile, fifthFile).get();		//Insert the deleted files again
		files = dao.loadAll().get();
		assertEquals(5, files.size());					//Ensure we have 5 files again
		assertTrue(files.contains(firstFile));					//Ensure all files are present
		assertTrue(files.contains(secondFile));					//...
		assertTrue(files.contains(thirdFile));					//...
		assertTrue(files.contains(fourthFile));					//...
		assertTrue(files.contains(fifthFile));					//...


		System.out.println("Deleting multiple items again...");
		dao.delete(firstFile, thirdFile).get();					//Delete multiple files again
		files = dao.loadAll().get();
		assertEquals(3, files.size());					//Ensure we now have 3 files
		assertFalse(files.contains(firstFile));					//Ensure file1 is missing
		assertTrue(files.contains(secondFile));					//...
		assertFalse(files.contains(thirdFile));					//As well as file3
		assertTrue(files.contains(fourthFile));					//...
		assertTrue(files.contains(fifthFile));					//...
	}

	@Test
	public void testDeleteByUUID() throws ExecutionException, InterruptedException {

		LFileDAO dao = db.getFileDao();

		List<LFile> files = dao.loadAll().get();
		assertTrue(files.isEmpty());                            //Ensure we start with 0 rows


		//------------------------------------------------------
		//Make a bunch of files
		LFile firstFile = new LFile(UUID.randomUUID());
		LFile secondFile = new LFile(UUID.randomUUID());
		LFile thirdFile = new LFile(UUID.randomUUID());
		LFile fourthFile = new LFile(UUID.randomUUID());
		LFile fifthFile = new LFile(UUID.randomUUID());

		//Put them all in the database
		System.out.println("Putting multiple items...");
		dao.put(firstFile, secondFile, thirdFile, fourthFile, fifthFile).get();

		files = dao.loadAll().get();
		assertEquals(5, files.size());                    //Ensure we have 5 files


		System.out.println("Deleting single item...");
		dao.delete(secondFile.fileuid).get();                    //Delete the second file
		files = dao.loadAll().get();
		assertEquals(4, files.size());                    //Ensure we now have 4 files
		assertTrue(files.contains(firstFile));                    //Ensure all files are present
		assertFalse(files.contains(secondFile));                //EXCEPT file2
		assertTrue(files.contains(thirdFile));                    //...
		assertTrue(files.contains(fourthFile));                    //...
		assertTrue(files.contains(fifthFile));                    //...


		System.out.println("Putting single item...");
		dao.put(secondFile).get();                                //Insert the second file again
		files = dao.loadAll().get();
		assertEquals(5, files.size());                    //Ensure we now have 5 files
		assertTrue(files.contains(firstFile));                    //Ensure all files are present
		assertTrue(files.contains(secondFile));                    //INCLUDING file2
		assertTrue(files.contains(thirdFile));                    //...
		assertTrue(files.contains(fourthFile));                    //...
		assertTrue(files.contains(fifthFile));                    //...


		System.out.println("Deleting single item again...");
		dao.delete(secondFile.fileuid).get();                    //Delete the second file again
		files = dao.loadAll().get();
		assertEquals(4, files.size());                    //Ensure we now have 4 files


		//------------------------------------------------------
		//Delete multiple

		System.out.println("Deleting multiple items...");
		dao.delete(secondFile.fileuid, thirdFile.fileuid, fifthFile.fileuid).get();    //Delete multiple files
		files = dao.loadAll().get();
		assertEquals(2, files.size());                    //Ensure we now have 2 files
		assertTrue(files.contains(firstFile));                    //Ensure all files are present
		assertFalse(files.contains(secondFile));                //EXCEPT file2
		assertFalse(files.contains(thirdFile));                    //file3
		assertTrue(files.contains(fourthFile));                    //...
		assertFalse(files.contains(fifthFile));                    //and file5


		System.out.println("Putting multiple items...");
		dao.put(secondFile, thirdFile, fifthFile).get();        //Insert the deleted files again
		files = dao.loadAll().get();
		assertEquals(5, files.size());                    //Ensure we have 5 files again
		assertTrue(files.contains(firstFile));                    //Ensure all files are present
		assertTrue(files.contains(secondFile));                    //...
		assertTrue(files.contains(thirdFile));                    //...
		assertTrue(files.contains(fourthFile));                    //...
		assertTrue(files.contains(fifthFile));                    //...


		System.out.println("Deleting multiple items again...");
		dao.delete(firstFile.fileuid, thirdFile.fileuid).get();    //Delete multiple files again
		files = dao.loadAll().get();
		assertEquals(3, files.size());                    //Ensure we now have 3 files
		assertFalse(files.contains(firstFile));                    //Ensure file1 is missing
		assertTrue(files.contains(secondFile));                    //...
		assertFalse(files.contains(thirdFile));                    //As well as file3
		assertTrue(files.contains(fourthFile));                    //...
		assertTrue(files.contains(fifthFile));                    //...
	}
}