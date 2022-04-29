package com.serkan.todoapp.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.serkan.todoapp.data.PreferencesManager
import com.serkan.todoapp.data.Task
import com.serkan.todoapp.data.TaskDao
import com.serkan.todoapp.ui.ADD_TASK_RESULT_OK
import com.serkan.todoapp.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class taskViewModel @ViewModelInject constructor(
    private val taskDao:TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state:SavedStateHandle
    ) :ViewModel() {

    val searchQuery= state.getLiveData("searchQuery","")
    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val tasksFlow=combine(searchQuery.asFlow(),preferencesFlow){
        query,filterPreferences -> Pair(query,filterPreferences)
    }.flatMapLatest { (query,filterPreferences) ->
        taskDao.getTasks(query,filterPreferences.sortOrder,filterPreferences.hideCompleted)
    }

    val tasks=tasksFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
    preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted:Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }


    fun onTaskSelected(task: Task) = viewModelScope.launch{
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(task))
    }
    fun onTaskCheckedChanged(task:Task,isChecked:Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task:Task)=viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task:Task) =viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result:Int){
        when(result){
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")
        }
    }
    private fun showTaskSavedConfirmationMessage(text:String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }
    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToDeletAllCompletedScreen)
    }


    sealed class TasksEvent{
        object NavigateToAddTaskScreen :TasksEvent()
        data class NavigateToEditTaskScreen(val task:Task):TasksEvent()
        data class ShowUndoDeleteTaskMessage(val task:Task):TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg:String):TasksEvent()
        object NavigateToDeletAllCompletedScreen:TasksEvent()

    }



}
enum class SortOrder{BY_NAME,BY_DATE}