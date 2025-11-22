package com.example.schedulingSystem

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ScheduleDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ScheduleContract.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(ScheduleContract.SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun insertSchedules(schedules: List<ScheduleItem>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (item in schedules) {
                val values = ContentValues().apply {
                    put(ScheduleContract.ScheduleEntry.COLUMN_SCHEDULE_ID, item.schedule_ID)
                    put(ScheduleContract.ScheduleEntry.COLUMN_DAY_NAME, item.day_name)
                    put(ScheduleContract.ScheduleEntry.COLUMN_TIME_START, item.time_start)
                    put(ScheduleContract.ScheduleEntry.COLUMN_TIME_END, item.time_end)
                    put(ScheduleContract.ScheduleEntry.COLUMN_SUBJECT_CODE, item.subject_code)
                    put(ScheduleContract.ScheduleEntry.COLUMN_SUBJECT_NAME, item.subject_name)
                    put(ScheduleContract.ScheduleEntry.COLUMN_SECTION_NAME, item.section_name)
                    put(ScheduleContract.ScheduleEntry.COLUMN_SECTION_YEAR, item.section_year)
                    put(ScheduleContract.ScheduleEntry.COLUMN_ROOM_NAME, item.room_name)
                    put(ScheduleContract.ScheduleEntry.COLUMN_NAME_FIRST, item.name_first)
                    put(ScheduleContract.ScheduleEntry.COLUMN_NAME_LAST, item.name_last)
                }
                // replace handles insert or update if primary key conflicts
                db.replace(ScheduleContract.ScheduleEntry.TABLE_NAME, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllSchedules(): List<ScheduleItem> {
        val schedules = mutableListOf<ScheduleItem>()
        val db = readableDatabase
        val cursor = db.query(
            ScheduleContract.ScheduleEntry.TABLE_NAME,
            null, null, null, null, null, null
        )

        with(cursor) {
            while (moveToNext()) {
                val item = ScheduleItem(
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_SCHEDULE_ID)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_DAY_NAME)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_TIME_START)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_TIME_END)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_SUBJECT_CODE)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_SUBJECT_NAME)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_SECTION_NAME)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_SECTION_YEAR)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_ROOM_NAME)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_NAME_FIRST)),
                    getString(getColumnIndexOrThrow(ScheduleContract.ScheduleEntry.COLUMN_NAME_LAST))
                )
                schedules.add(item)
            }
        }
        cursor.close()
        return schedules
    }

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Schedule.db"
    }
}
