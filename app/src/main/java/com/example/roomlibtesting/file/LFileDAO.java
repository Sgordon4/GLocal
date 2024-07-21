package com.example.roomlibtesting.file;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.UUID;


/*
For live UI updates, see "Write Observable Queries" in
https://developer.android.com/training/data-storage/room/async-queries#guava-livedata
 */

@Dao
public interface LFileDAO {

	@Query("SELECT * FROM file")
	public ListenableFuture<List<LFile>> loadAll();

	@Query("SELECT * FROM file WHERE fileuid IN (:fileUIDs)")
	public ListenableFuture<List<LFile>> loadByIDs(UUID... fileUIDs);


	@Insert(onConflict = OnConflictStrategy.IGNORE)
	public ListenableFuture<List<Long>> insert(LFile... files);

	@Update
	public ListenableFuture<Integer> update(LFile... files);

	@Delete
	public ListenableFuture<Integer> delete(LFile... files);


	//@Upsert
	//void insertAll(LFile... files);
}