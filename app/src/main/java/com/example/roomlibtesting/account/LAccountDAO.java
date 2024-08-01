package com.example.roomlibtesting.account;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.UUID;

@Dao
public interface LAccountDAO {
	@Query("SELECT * FROM account LIMIT 500")
	ListenableFuture<List<LAccount>> loadAll();
	@Query("SELECT * FROM account LIMIT 500 OFFSET :offset")
	ListenableFuture<List<LAccount>> loadAll(int offset);

	@Query("SELECT * FROM account WHERE accountuid IN (:accountUIDs)")
	ListenableFuture<List<LAccount>> loadByUID(UUID... accountUIDs);


	@Upsert
	ListenableFuture<List<Long>> put(LAccount... accounts);

	@Delete
	ListenableFuture<Integer> delete(LAccount account);
}