package com.example.roomlibtesting;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.example.roomlibtesting.account.LAccount;
import com.example.roomlibtesting.account.LAccountDAO;
import com.example.roomlibtesting.block.LBlock;
import com.example.roomlibtesting.block.LBlockDao;
import com.example.roomlibtesting.file.LFile;
import com.example.roomlibtesting.file.LFileDAO;
import com.example.roomlibtesting.journal.LJournal;
import com.example.roomlibtesting.journal.LJournalDao;

import java.util.UUID;


@Database(entities = {LAccount.class, LFile.class, LJournal.class, LBlock.class}, version = 1)
@TypeConverters({LocalConverters.class})
public abstract class LocalDatabase extends RoomDatabase {


	public abstract LAccountDAO getAccountDao();
	public abstract LFileDAO getFileDao();
	public abstract LJournalDao getJournalDao();
	public abstract LBlockDao getBlockDao();



	public static class DBBuilder {
		private static final String DB_NAME = "glocal.db";

		public LocalDatabase newInstance(Context context) {
			return Room.databaseBuilder(context, LocalDatabase.class, DB_NAME)
					.addCallback(new RoomDatabase.Callback() {
						@Override
						public void onCreate(@NonNull SupportSQLiteDatabase db) {
							super.onCreate(db);

							LFile a = new LFile(UUID.randomUUID());
							//db.execSQL("INSERT INTO file ("+a.toKeys()+") values ("+a.toVals()+");");



							//DO NOT USE
							//When an account is created, add a root file as well (if it does not already exist)
							/*
							db.execSQL(
									"CREATE TRIGGER IF NOT EXISTS make_root_file AFTER INSERT ON account FOR EACH ROW "+
									"BEGIN "+
										"INSERT OR IGNORE INTO file (accountuid, fileuid, isdir) " +
										"VALUES (NEW.accountuid, NEW.rootfileuid, true); "+
									"END;");
							 */


							//---------------------------------------------------------------------
							//Journal triggers

							//When a file row is inserted or updated, add a record to the Journal.
							//These are both identical, but you can't watch both INSERT and UPDATE with the same trigger
							db.execSQL("CREATE TRIGGER IF NOT EXISTS file_insert_to_journal AFTER INSERT ON file FOR EACH ROW "+
									"BEGIN "+
										"INSERT INTO journal (accountuid, fileuid, isdir, islink, filesize, fileblocks, isdeleted) " +
										"VALUES (NEW.accountuid, NEW.fileuid, NEW.isdir, NEW.islink, NEW.filesize, NEW.fileblocks, NEW.isdeleted); "+
									"END;");
							db.execSQL("CREATE TRIGGER IF NOT EXISTS file_update_to_journal AFTER UPDATE ON file FOR EACH ROW "+
									"BEGIN "+
										"INSERT INTO journal (accountuid, fileuid, isdir, islink, filesize, fileblocks, isdeleted) " +
										"VALUES (NEW.accountuid, NEW.fileuid, NEW.isdir, NEW.islink, NEW.filesize, NEW.fileblocks, NEW.isdeleted); "+
									"END;");

							//Note: No DELETE trigger, since to 'delete' a file we actually set the isdeleted bit.
							// Actual row deletion would be the result of admin work like scheduled cleanup.

							//---------------------------------------------------------------------
							//Update changetime triggers

							//TODO Untested, might cause issues. E.x. does this cause an infinite loop by updating the changetime,
							// or does it correctly modify only the incoming data?
							db.execSQL("CREATE TRIGGER IF NOT EXISTS update_changetime_file BEFORE UPDATE ON file FOR EACH ROW "+
									"BEGIN "+
									"UPDATE file SET changetime = CURRENT_TIMESTAMP WHERE fileuid = NEW.fileuid; "+
									"END;");

							db.execSQL("CREATE TRIGGER IF NOT EXISTS update_changetime_account BEFORE UPDATE ON account FOR EACH ROW "+
									"BEGIN "+
									"UPDATE account SET changetime = CURRENT_TIMESTAMP WHERE accounxtuid = NEW.accountuid; "+
									"END;");


						}
					}).build();
		}
	}
}

/*
db.execSQL("CREATE TRIGGER IF NOT EXISTS write_file_to_journal AFTER INSERT ON account FOR EACH ROW "+
									"BEGIN "+
										"INSERT INTO file (accountuid, fileuid) VALUES (NEW.accountuid, '"+UUID.randomUUID()+"'); "+
									"END;");
									//"ON CONFLICT IGNORE;");
 */