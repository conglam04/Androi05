package com.example.todolist.Data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.todolist.Data.entity.RecurrenceRule;


@Dao
public interface RecurrenceRuleDao {
    @Insert
    long insert(RecurrenceRule rule);

    @Update
    void update(RecurrenceRule rule);

    @Delete
    void delete(RecurrenceRule rule);

    @Query("SELECT * FROM recurrence_rules WHERE ruleId = :ruleId")
    LiveData<RecurrenceRule> getRuleById(long ruleId);

    @Query("SELECT * FROM recurrence_rules WHERE taskId = :taskId LIMIT 1")
    RecurrenceRule getRuleByTaskId(long taskId);

    @Query("DELETE FROM recurrence_rules WHERE taskId = :taskId")
    void deleteByTaskId(long taskId);
}