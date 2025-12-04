package com.example.schedulingSystem

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.schedulingSystem.ScheduleItem

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
                    put(ScheduleContract.ScheduleEntry.COLUMN_SCHEDULE_ID, item.scheduleID)
                    put(ScheduleContract.ScheduleEntry.COLUMN_DAY_NAME, item.dayName)
                    put(ScheduleContract.ScheduleEntry.COLUMN_TIME_START, item.timeStart)
                    put(ScheduleContract.ScheduleEntry.COLUMN_TIME_END, item.timeEnd)
                    put(ScheduleContract.ScheduleEntry.COLUMN_SUBJECT_CODE, item.subjectCode)
                    put(ScheduleContract.ScheduleEntry.COLUMN_SUBJECT_NAME, item.subjectName)
                    put(ScheduleContract.ScheduleEntry.COLUMN_SECTION_NAME, item.sectionName)
                    put(ScheduleContract.ScheduleEntry.COLUMN_SECTION_YEAR, item.sectionYear)
                    put(ScheduleContract.ScheduleEntry.COLUMN_ROOM_NAME, item.roomName)
                    put(ScheduleContract.ScheduleEntry.COLUMN_NAME_FIRST, item.nameFirst)
                    put(ScheduleContract.ScheduleEntry.COLUMN_NAME_LAST, item.nameLast)
                    put(ScheduleContract.ScheduleEntry.COLUMN_SCHEDULE_STATUS, item.scheduleStatus)
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

        // The JOIN query to fetch all required data from the normalized tables
        val query = """
        SELECT
            S.schedule_ID,
            D.day_name,
            T.time_start,
            T.time_end,
            Sub.subject_code,
            Sub.subject_name,
            Sec.section_name,
            Sec.section_year,
            R.room_name,
            N.name_first,
            N.name_last,
            S.schedule_status
        FROM
            Schedule S
        INNER JOIN Day D ON S.day_ID = D.day_ID
        INNER JOIN Time T ON S.time_ID = T.time_ID
        INNER JOIN Subject Sub ON S.subject_ID = Sub.subject_ID
        INNER JOIN Section Sec ON S.section_ID = Sec.section_ID
        INNER JOIN Room R ON S.room_ID = R.room_ID
        INNER JOIN Teacher Tea ON S.teacher_ID = Tea.teacher_ID
        INNER JOIN Person P ON Tea.person_ID = P.person_ID
        INNER JOIN Name N ON P.name_ID = N.name_ID
    """.trimIndent()

        // Use db.rawQuery to execute the JOIN statement
        val cursor = db.rawQuery(query, null)

        with(cursor) {
            // NOTE: The column names in the contract MUST match the ALIASES in the SELECT list (e.g., "schedule_ID", "day_name", etc.)
            while (moveToNext()) {
                val item = ScheduleItem(
                    // Use the SELECT list ALIASES for column names
                    getString(getColumnIndexOrThrow("schedule_ID")),
                    getString(getColumnIndexOrThrow("day_name")),
                    getString(getColumnIndexOrThrow("time_start")),
                    getString(getColumnIndexOrThrow("time_end")),
                    getString(getColumnIndexOrThrow("subject_code")),
                    getString(getColumnIndexOrThrow("subject_name")),
                    getString(getColumnIndexOrThrow("section_name")),
                    getString(getColumnIndexOrThrow("section_year")),
                    getString(getColumnIndexOrThrow("room_name")),
                    getString(getColumnIndexOrThrow("name_first")),
                    getString(getColumnIndexOrThrow("name_last")),
                    getString(getColumnIndexOrThrow("schedule_status"))
                )
                schedules.add(item)
            }
        }
        cursor.close()
        return schedules
    }

    companion object {
        // Increment version to trigger onUpgrade
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "scheduling-system.db"
    }
}
