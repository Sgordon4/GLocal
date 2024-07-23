package com.example.roomlibtesting.file;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity(tableName = "file")
public class LFile {
	@PrimaryKey
	@NonNull
	public UUID fileuid;
	@NonNull
	public UUID accountuid;


	@ColumnInfo(defaultValue = "false")
	public boolean isdir;
	@ColumnInfo(defaultValue = "false")
	public boolean islink;

	@NonNull
	@ColumnInfo(defaultValue = "[]")
	public List<String> fileblocks;
	@ColumnInfo(defaultValue = "-1")
	public int filesize;

	@ColumnInfo(defaultValue = "false")
	public boolean isdeleted;

	@ColumnInfo(defaultValue = "-1")
	//Last time the file properties (database row) were changed
	public long changetime;
	@ColumnInfo(defaultValue = "-1")
	//Last time the file contents were modified
	public long modifytime;
	@ColumnInfo(defaultValue = "-1")
	//Last time the file contents were read
	public long accesstime;
	@ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
	public long createtime;


	@Ignore
	public LFile(@NonNull UUID accountuid) {
		this(accountuid, UUID.randomUUID());
	}
	public LFile(@NonNull UUID accountuid, @NonNull UUID fileuid) {
		this.fileuid = fileuid;
		this.accountuid = accountuid;

		this.isdir = false;
		this.islink = false;
		this.fileblocks = new ArrayList<>();
		this.filesize = 0;
		this.isdeleted = false;
		this.changetime = -1;
		this.modifytime = -1;
		this.accesstime = -1;
		this.createtime = new Date().getTime();

	}


	@Override
	public String toString() {
		return "LFile{" +
				"fileuid=" + fileuid +
				", accountuid=" + accountuid +
				", isdir=" + isdir +
				", islink=" + islink +
				", filesize=" + filesize +
				", fileblocks=" + fileblocks +
				", isdeleted=" + isdeleted +
				'}';
	}
}