package com.serkan.todoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.serkan.todoapp.module.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase:RoomDatabase() {
    abstract fun taskDao():TaskDao

    class Callback @Inject constructor(private val database:Provider<TaskDatabase>,
    @ApplicationScope private val applicationScope:CoroutineScope):
        RoomDatabase.Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao=database.get().taskDao()
            applicationScope.launch {
                dao.insert(Task("woke up", completed = true))
                dao.insert(Task("study math",important = true))
                dao.insert(Task("learn kotlin"))
                dao.insert(Task("watch movie"))
                dao.insert(Task("sleep",important = true))
            }

        }
    }
}