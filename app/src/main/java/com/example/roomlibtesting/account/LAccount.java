package com.example.roomlibtesting.account;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Entity(tableName = "account")
public class LAccount {
	@PrimaryKey
	@NonNull
	public UUID accountuid;
	@NonNull
	public UUID rootfileuid;

	@Nullable
	public String email;
	@Nullable
	public String displayname;
	@Nullable
	public String password;

	@ColumnInfo(defaultValue = "false")
	public boolean isdeleted;

	@ColumnInfo(defaultValue = "-1")
	public long logintime;
	@ColumnInfo(defaultValue = "-1")
	public long changetime;
	@ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
	public long createtime;



	public LAccount(){
		this(UUID.randomUUID(), UUID.randomUUID());
	}
	public LAccount(@NonNull UUID accountuid, @NonNull UUID rootfileuid) {
		this.accountuid = accountuid;
		this.rootfileuid = rootfileuid;

		this.email = null;
		this.displayname = null;
		this.password = null;

		this.isdeleted = false;

		this.logintime = -1;
		this.changetime = -1;
		this.createtime = new Date().getTime();
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LAccount lAccount = (LAccount) o;
		return isdeleted == lAccount.isdeleted && logintime == lAccount.logintime && changetime == lAccount.changetime && createtime == lAccount.createtime && Objects.equals(accountuid, lAccount.accountuid) && Objects.equals(rootfileuid, lAccount.rootfileuid) && Objects.equals(email, lAccount.email) && Objects.equals(displayname, lAccount.displayname) && Objects.equals(password, lAccount.password);
	}

	@Override
	public int hashCode() {
		return Objects.hash(accountuid, rootfileuid, email, displayname, password, isdeleted, logintime, changetime, createtime);
	}
}