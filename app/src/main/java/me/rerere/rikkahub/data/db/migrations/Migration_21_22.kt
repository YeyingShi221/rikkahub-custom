package me.rerere.rikkahub.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        fun getColumns(table: String): List<String> {
            val cursor = db.query("PRAGMA table_info($table)")
            val cols = mutableListOf<String>()
            while (cursor.moveToNext()) {
                cols.add(cursor.getString(1))
            }
            cursor.close()
            return cols
        }

        val convCols = getColumns("ConversationEntity")
        if (!convCols.contains("custom_system_prompt")) {
            db.execSQL("ALTER TABLE ConversationEntity ADD COLUMN custom_system_prompt TEXT NOT NULL DEFAULT ''")
        }
        if (!convCols.contains("mode_injection_ids")) {
            db.execSQL("ALTER TABLE ConversationEntity ADD COLUMN mode_injection_ids TEXT NOT NULL DEFAULT '[]'")
        }
        if (!convCols.contains("lorebook_ids")) {
            db.execSQL("ALTER TABLE ConversationEntity ADD COLUMN lorebook_ids TEXT NOT NULL DEFAULT '[]'")
        }

        val mediaCols = getColumns("GenMediaEntity")
        if (!mediaCols.contains("type")) {
            db.execSQL("ALTER TABLE GenMediaEntity ADD COLUMN type TEXT NOT NULL DEFAULT 'image_generation'")
        }
        if (!mediaCols.contains("source_paths")) {
            db.execSQL("ALTER TABLE GenMediaEntity ADD COLUMN source_paths TEXT DEFAULT NULL")
        }
    }
}
