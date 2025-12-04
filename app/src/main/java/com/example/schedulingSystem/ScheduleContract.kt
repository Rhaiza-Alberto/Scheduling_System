package com.example.schedulingSystem

import android.provider.BaseColumns

object ScheduleContract {

    // The SQL strings now reference the constant from the nested object (ScheduleEntry.TABLE_NAME)

    val SQL_CREATE_ENTRIES = """ 
        CREATE TABLE ${ScheduleEntry.TABLE_NAME} (
            ${BaseColumns._ID} INTEGER PRIMARY KEY,
            ${ScheduleEntry.COLUMN_SCHEDULE_ID} TEXT UNIQUE,
            ${ScheduleEntry.COLUMN_DAY_NAME} TEXT,
            ${ScheduleEntry.COLUMN_TIME_START} TEXT,
            ${ScheduleEntry.COLUMN_TIME_END} TEXT,
            ${ScheduleEntry.COLUMN_SUBJECT_CODE} TEXT,
            ${ScheduleEntry.COLUMN_SUBJECT_NAME} TEXT,
            ${ScheduleEntry.COLUMN_SECTION_NAME} TEXT,
            ${ScheduleEntry.COLUMN_SECTION_YEAR} TEXT,
            ${ScheduleEntry.COLUMN_ROOM_NAME} TEXT,
            ${ScheduleEntry.COLUMN_NAME_FIRST} TEXT,
            ${ScheduleEntry.COLUMN_NAME_LAST} TEXT,
            ${ScheduleEntry.COLUMN_SCHEDULE_STATUS} TEXT
        )
    """ .trimIndent()

    const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ScheduleEntry.TABLE_NAME}"

    object ScheduleEntry : BaseColumns {
        // ✅ FIX: Define the TABLE_NAME constant here to resolve the reference
        const val TABLE_NAME = "schedule"

        // --- Column Definitions ---
        const val COLUMN_SCHEDULE_ID = "schedule_id"
        const val COLUMN_DAY_NAME = "day_name"
        const val COLUMN_TIME_START = "time_start"
        const val COLUMN_TIME_END = "time_end"
        const val COLUMN_SUBJECT_CODE = "subject_code"
        const val COLUMN_SUBJECT_NAME = "subject_name"
        const val COLUMN_SECTION_NAME = "section_name"
        const val COLUMN_SECTION_YEAR = "section_year"
        const val COLUMN_ROOM_NAME = "room_name"
        const val COLUMN_NAME_FIRST = "name_first"
        const val COLUMN_NAME_LAST = "name_last"
        const val COLUMN_SCHEDULE_STATUS = "schedule_status"
    }
}