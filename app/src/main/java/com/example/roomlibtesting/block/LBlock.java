package com.example.roomlibtesting.block;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "block")
public class LBlock {
	@PrimaryKey
	@NonNull
	public String blockhash;

	@ColumnInfo(defaultValue = "-1")
	public int blocksize;

	@ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
	public long createtime;


	public LBlock(@NonNull String blockhash) {
		this.blockhash = blockhash;
		this.blocksize = -1;
		this.createtime = new Date().getTime();
	}
}