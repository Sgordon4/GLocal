package com.example.roomlibtesting.account;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LAccountDAO {
	@Query("SELECT * FROM account")
	List<LAccount> getAll();

	@Query("SELECT * FROM account WHERE accountuid IN (:accountUIDs)")
	List<LAccount> getAllByIds(int[] accountUIDs);


	@Insert(onConflict = OnConflictStrategy.IGNORE)
	void insertAll(LAccount... accounts);

	@Delete
	void delete(LAccount account);
}