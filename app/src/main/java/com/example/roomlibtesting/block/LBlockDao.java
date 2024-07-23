package com.example.roomlibtesting.block;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.roomlibtesting.account.LAccount;
import com.example.roomlibtesting.file.LFile;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface LBlockDao {
	@Query("SELECT * FROM block")
	ListenableFuture<List<LBlock>> loadAll();

	@Query("SELECT * FROM block WHERE blockhash IN (:blockHashes)")
	ListenableFuture<List<LBlock>> loadAllByHash(String... blockHashes);


	@Insert(onConflict = OnConflictStrategy.IGNORE)
	ListenableFuture<List<Long>> insert(LBlock... blocks);

	@Update
	ListenableFuture<Integer> update(LBlock... blocks);

	@Delete
	ListenableFuture<Integer> delete(LBlock... blocks);
}