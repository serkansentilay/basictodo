package com.serkan.todoapp.ui.delete

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.serkan.todoapp.data.TaskDao
import com.serkan.todoapp.module.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class deleteallcompletedViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) :ViewModel() {
    fun onConfirmClick() = applicationScope.launch{
        taskDao.deleteCompletedTasks()
    }
}