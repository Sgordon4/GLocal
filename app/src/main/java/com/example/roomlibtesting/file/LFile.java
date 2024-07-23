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

	//@NonNull
	@ColumnInfo(defaultValue = "false")
	public boolean isdir = false;
	//@NonNull
	@ColumnInfo(defaultValue = "false")
	public boolean islink = false;

	//@NonNull
	@ColumnInfo(defaultValue = "0")
	public int filesize = 0;

	@NonNull
	@ColumnInfo(defaultValue = "[]")
	public List<String> fileblocks = new ArrayList<>();
	//public String fileblocks = "[]";

	@ColumnInfo(defaultValue = "0")
	public long changetime;
	@ColumnInfo(defaultValue = "0")
	public long accesstime;
	@ColumnInfo(defaultValue = "0")
	public long modifytime;
	@ColumnInfo(defaultValue = "0")
	public long deletetime;
	//@NonNull
	@ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
	public long createtime = new Date().getTime();


	@Ignore
	public LFile(@NonNull UUID accountuid) {
		this(accountuid, UUID.randomUUID());
	}
	public LFile(@NonNull UUID accountuid, @NonNull UUID fileuid) {
		this.fileuid = fileuid;
		this.accountuid = accountuid;
	}


	@Override
	public String toString() {
		return "LFile{" +
				"fileuid=" + fileuid +
				", accountuid=" + accountuid +
				", isdir=" + isdir +
				", islink=" + islink +
				", fileblocks=" + fileblocks +
				'}';
	}

	public String toKeys() {
		return "fileuid, accountuid, isdir, islink, filesize, fileblocks, changetime, accesstime, modifytime, deletetime, createtime";
	}

	public String toVals() {
		return String.format("'%s', '%s', %s, %s, %s, %s, %s, %s, %s, %s, %s", fileuid, accountuid, isdir, islink, filesize, "'{}'", 0, 0, 0, 0, 0);
	}
}