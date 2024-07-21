package com.example.roomlibtesting.account;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "account")
public class LAccount {
	@PrimaryKey
	@NonNull
	public UUID accountuid;

	@NonNull
	public String email;
	@NonNull
	public String displayname;
	@NonNull
	public String password;

	@NonNull
	public UUID rootfileuid;

	public long logintime;
	public long changetime;
	public long deletetime;
	//@NonNull
	public long createtime = new Date().getTime();



	public LAccount(@NonNull String email, @NonNull String displayname, @NonNull String password) {
		this.accountuid = UUID.randomUUID();
		this.rootfileuid = UUID.randomUUID();

		this.email = email;
		this.displayname = displayname;
		this.password = password;
	}
}