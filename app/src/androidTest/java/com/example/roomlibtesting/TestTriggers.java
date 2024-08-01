package com.example.roomlibtesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.roomlibtesting.account.LAccount;
import com.example.roomlibtesting.account.LAccountDAO;
import com.example.roomlibtesting.file.LFile;
import com.example.roomlibtesting.file.LFileDAO;
import com.example.roomlibtesting.journal.LJournal;
import com.example.roomlibtesting.journal.LJournalDao;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TestTriggers {

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
	public void testAccountFileTrigger() throws ExecutionException, InterruptedException {
		LAccountDAO acctDao = db.getAccountDao();
		LFileDAO fileDao = db.getFileDao();


		List<LAccount> accounts = acctDao.loadAll().get();
		List<LFile> files = fileDao.loadAll().get();
		assertTrue(accounts.isEmpty());							//Ensure we start with 0 rows in account
		assertTrue(files.isEmpty());							//Ensure we start with 0 rows in file


		//------------------------------------------------------

		//This should create a new account, thus triggering a file insert...
		LAccount firstAccount = new LAccount();					//Create a default account to start
		acctDao.put(firstAccount).get();						//Insert the account


		//Grab the current set of accounts and files
		accounts = acctDao.loadAll().get();
		files = fileDao.loadAll().get();
		assertEquals(1, accounts.size());				//Ensure we have 1 account
		assertEquals(1, files.size());					//Ensure we have 1 file
		assertEquals(firstAccount, accounts.get(0));			//Ensure the account has retained its data
		assertEquals(firstAccount.accountuid, files.get(0).accountuid);	//Ensure the file's account is correct
		assertEquals(firstAccount.rootfileuid, files.get(0).fileuid);	//Ensure the file's uid is correct


		//------------------------------------------------------

		//This should update the existing account, not activating the insert trigger...
		firstAccount.isdeleted = true;							//Slightly modify the account
		acctDao.put(firstAccount).get();						//Update the account


		//Grab the current set of accounts and files
		accounts = acctDao.loadAll().get();
		files = fileDao.loadAll().get();
		assertEquals(1, accounts.size());				//Ensure we still have 1 account
		assertEquals(1, files.size());					//Ensure we still have 1 file
		assertEquals(firstAccount, accounts.get(0));			//Ensure the account has retained its data
		assertEquals(firstAccount.accountuid, files.get(0).accountuid);	//Ensure the file's account is correct
		assertEquals(firstAccount.rootfileuid, files.get(0).fileuid);	//Ensure the file's uid is correct


		//------------------------------------------------------

		//This should delete the existing account, not activating the insert trigger...
		acctDao.delete(firstAccount).get();						//Delete the account


		//Grab the current set of accounts and files
		accounts = acctDao.loadAll().get();
		files = fileDao.loadAll().get();
		assertEquals(0, accounts.size());				//Ensure we now have 0 accounts
		assertEquals(1, files.size());					//Ensure we still have 1 file
		assertEquals(firstAccount.accountuid, files.get(0).accountuid);	//Ensure the file's account is correct
		assertEquals(firstAccount.rootfileuid, files.get(0).fileuid);	//Ensure the file's uid is correct


		//------------------------------------------------------

		//This should create 2 new accounts, this activating the insert trigger twice...
		LAccount secondAccount = new LAccount();				//Create a second account
		secondAccount.displayname = "Second Account";
		LAccount thirdAccount = new LAccount();					//Create a third account
		thirdAccount.email = "Something@somewhere.com";
		acctDao.put(secondAccount, thirdAccount).get();			//Insert the accounts


		//Grab the current set of accounts and files
		accounts = acctDao.loadAll().get();
		files = fileDao.loadAll().get();
		assertEquals(2, accounts.size());				//Ensure we now have 2 accounts
		assertEquals(3, files.size());					//Ensure we now have 3 files

		assertEquals(secondAccount, accounts.get(0));			//Ensure account2 has retained its data
		assertEquals(thirdAccount, accounts.get(1));			//Ensure account3 has retained its data

		assertEquals(firstAccount.accountuid, files.get(0).accountuid);	//Ensure file1's data is correct
		assertEquals(firstAccount.rootfileuid, files.get(0).fileuid);	//...
		assertEquals(secondAccount.accountuid, files.get(1).accountuid);//Ensure file2's data is correct
		assertEquals(secondAccount.rootfileuid, files.get(1).fileuid);	//...
		assertEquals(thirdAccount.accountuid, files.get(2).accountuid);	//Ensure file3's data is correct
		assertEquals(thirdAccount.rootfileuid, files.get(2).fileuid);	//...
	}




	@Test
	public void testFileJournalTrigger() throws ExecutionException, InterruptedException {
		LFileDAO fileDao = db.getFileDao();
		LJournalDao journalDao = db.getJournalDao();

		List<LFile> files = fileDao.loadAll().get();
		List<LJournal> journals = journalDao.loadAllAfterID(-1).get();
		assertTrue(files.isEmpty());							//Ensure we start with 0 rows in file
		assertTrue(journals.isEmpty());							//Ensure we start with 0 rows in journal


		//------------------------------------------------------

		LFile firstFile = new LFile(UUID.randomUUID());			//Create a file to start
		firstFile.isdir = true;									//Give it some non-default data

		


	}




}
