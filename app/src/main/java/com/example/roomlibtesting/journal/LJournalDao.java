package com.example.roomlibtesting.journal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.roomlibtesting.block.LBlock;
import com.example.roomlibtesting.file.LFile;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface LJournalDao {
	//TODO Should we change this to 'journalid >= :journalID'? Idk, might make slightly more sense (loadFromID)
	@Query("SELECT * FROM journal WHERE journalid > :journalID")
	ListenableFuture<List<LJournal>> loadAllAfterID(long journalID);

	//Journal is append-only, no need to update
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	ListenableFuture<List<Long>> insert(LJournal... entries);

	//Might remove since this is append-only, but it's here to mulligan
	@Delete
	ListenableFuture<Integer> delete(LJournal... entries);
}