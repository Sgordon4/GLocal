package com.example.roomlibtesting;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.roomlibtesting.account.LAccount;
import com.example.roomlibtesting.account.LAccountDAO;
import com.example.roomlibtesting.file.LFile;
import com.example.roomlibtesting.file.LFileDAO;

import java.util.UUID;


@Database(entities = {LAccount.class, LFile.class}, version = 1)
@TypeConverters({LocalConverters.class})
public abstract class LocalDatabase extends RoomDatabase {


	public abstract LAccountDAO getAccountDao();
	public abstract LFileDAO getFileDao();



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


							//TODO There's an issue with the way I'm using on conflict that I haven't figured out

							//When an account is created, add a root file as well
							db.execSQL("CREATE TRIGGER IF NOT EXISTS make_root_file AFTER INSERT ON account FOR EACH ROW "+
									"BEGIN "+
										"INSERT OR IGNORE INTO file (accountuid, fileuid, isdir) VALUES ('"+UUID.randomUUID()+"', '"+UUID.randomUUID()+"', true); "+
									"END;");


							//TODO Add the Journal DB, and then hook this up
							/*
							//When a file is updated, add a record to the Journal
							db.execSQL("CREATE TRIGGER IF NOT EXISTS file_update_to_journal AFTER INSERT ON file FOR EACH ROW "+
									"BEGIN "+
									"INSERT INTO journal (accountuid, fileuid, isdir) VALUES ('"+UUID.randomUUID()+"', '"+UUID.randomUUID()+"', true); "+
									"END;");
							 */



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