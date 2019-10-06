package com.township.manager;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface NoticeDao {

    @Query("SELECT * FROM Notice")
    List<Notice> getAll();

    @Query("SELECT * FROM Wing\n" +
            "INNER JOIN NoticeWing ON NoticeWing.wing_id = Wing.wing_id\n" +
            "WHERE NoticeWing.notice_id = :notice_id")
    List<Wing> getWings(String notice_id);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insert(Notice... notices);

    @Delete
    void delete(Notice notice);

}
