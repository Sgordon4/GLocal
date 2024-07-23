package com.example.roomlibtesting;

import android.content.Context;

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

		//db = Room.databaseBuilder(context, LocalDatabase.class, "database-name").build();
		db = new LocalDatabase.DBBuilder().newInstance(context);
	}


	public void triggered() {

	}



	@Test
	public void testAccountFileTrigger() throws ExecutionException, InterruptedException {
		LAccountDAO acctDao = db.getAccountDao();
		LFileDAO fileDao = db.getFileDao();


		LFile file = new LFile(UUID.randomUUID());
		System.out.println("AAAAAAAAAAAAAA");
		System.out.println(file.fileblocks);

		System.out.println("Current files:");
		printFiles();

		LAccount new1 = new LAccount("email@first.com", "First Account", "12345");
		acctDao.insert(new1);
		System.out.println("After inserting account:");
		printFiles();

		acctDao.insert(new1);
		System.out.println("After inserting account AGAIN:");
		printFiles();

	}



	@Ignore
	@Test
	public void databaseTest() throws ExecutionException, InterruptedException {
		LFileDAO dao = db.getFileDao();

		LFile A = new LFile(UUID.randomUUID());
		LFile B = new LFile(UUID.randomUUID());

		//Ensure insert inserts
		System.out.println("Inserting A and B...");
		System.out.println(dao.insert(A, B).get());
		printFiles();

		LFile newA = new LFile(A.accountuid, A.fileuid);
		newA.isdir = true;

		//Ensure insert does not update
		System.out.println("Inserting duplicate A...");
		System.out.println(dao.insert(newA).get());
		printFiles();

		//Ensure update updates
		System.out.println("Updating A...");
		System.out.println(dao.update(newA).get());
		printFiles();

		//Ensure delete deletes
		System.out.println("Deleting A...");
		System.out.println(dao.delete(A).get());
		printFiles();

		System.out.println("Inserting A again...");
		System.out.println(dao.insert(A).get());
		printFiles();
	}

	public void printFiles() throws ExecutionException, InterruptedException {
		LFileDAO dao = db.getFileDao();
		List<LFile> files = dao.loadAll().get();
		System.out.println("Size: "+files.size());
		for(LFile file : files) {
			System.out.println(file);
		}
	}


}