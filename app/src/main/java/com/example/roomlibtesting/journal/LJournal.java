package com.example.roomlibtesting.journal;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.example.roomlibtesting.account.LAccount;
import com.example.roomlibtesting.file.LFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity(tableName = "journal"
		/*,
		foreignKeys = {
			@ForeignKey(entity = LAccount.class,
			parentColumns = "accountuid",
			childColumns = "accountuid",
			onDelete = ForeignKey.CASCADE),
			@ForeignKey(entity = LFile.class,
			parentColumns = "fileuid",
			childColumns = "fileuid",
			onDelete = ForeignKey.CASCADE)
		}*/)
public class LJournal {
	@PrimaryKey(autoGenerate = true)
	public long journalid;

	@NonNull
	public UUID fileuid;
	@NonNull
	public UUID accountuid;


	@ColumnInfo(defaultValue = "false")
	public boolean isdir;
	@ColumnInfo(defaultValue = "false")
	public boolean islink;


	@ColumnInfo(defaultValue = "0")
	public int filesize;

	@NonNull
	@ColumnInfo(defaultValue = "[]")
	public List<String> fileblocks;

	@ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
	public long createtime;



	public LJournal(@NonNull UUID fileuid, @NonNull UUID accountuid) {
		this.fileuid = fileuid;
		this.accountuid = accountuid;

		this.filesize = 0;
		this.isdir = false;
		this.islink = false;
		this.fileblocks = new ArrayList<>();
		this.createtime = -1;
	}

	@NonNull
	@Override
	public String toString() {
		return "LJournal{" +
				"journalid=" + journalid +
				", fileuid=" + fileuid +
				", accountuid=" + accountuid +
				", isdir=" + isdir +
				", islink=" + islink +
				", filesize=" + filesize +
				", fileblocks=" + fileblocks +
				", createtime=" + createtime +
				'}';
	}
}