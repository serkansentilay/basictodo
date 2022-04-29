package com.serkan.todoapp.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.serkan.todoapp.R
import com.serkan.todoapp.data.Task
import com.serkan.todoapp.databinding.TaskfragmentBinding
import com.serkan.todoapp.util.exhaustive
import com.serkan.todoapp.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class tasksFragment :Fragment(R.layout.taskfragment),tasksAdapter.onItemClickListener{
        private val viewModel:taskViewModel by viewModels()
        private lateinit var searcView:SearchView
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)
                val binding =TaskfragmentBinding.bind(view)
                val tasksAdapter=tasksAdapter(this)
                binding.apply {
                        taskrecyclerview.apply {
                                adapter=tasksAdapter
                                layoutManager=LinearLayoutManager(requireContext())
                                setHasFixedSize(true)
                        }

                        ItemTouchHelper(object:ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT or
                        ItemTouchHelper.LEFT){
                                override fun onMove(
                                        recyclerView: RecyclerView,
                                        viewHolder: RecyclerView.ViewHolder,
                                        target: RecyclerView.ViewHolder
                                ): Boolean {
                                        return false
                                }

                                override fun onSwiped(
                                        viewHolder: RecyclerView.ViewHolder,
                                        direction: Int
                                ) {
                                        val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                                        viewModel.onTaskSwiped(task)
                                }

                        }).attachToRecyclerView(taskrecyclerview)

                        fabAddbutn.setOnClickListener {
                                viewModel.onAddNewTaskClick()
                        }
                }

                setFragmentResultListener("add_edit_request"){_,bundle ->
                        val result = bundle.getInt("add_edit_result")
                        viewModel.onAddEditResult(result)
                }


                viewModel.tasks.observe(viewLifecycleOwner){
                        tasksAdapter.submitList(it)
                }
                viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                        viewModel.tasksEvent.collect {event ->
                                when(event){
                                        is taskViewModel.TasksEvent.ShowUndoDeleteTaskMessage ->{
                                                Snackbar.make(requireView(),"Task deleted",Snackbar.LENGTH_LONG)
                                                        .setAction("UNDO"){
                                                                viewModel.onUndoDeleteClick(event.task)
                                                        }.show()
                                        }
                                        is taskViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                                             val action = tasksFragmentDirections.actionTasksFragmentToAddedittaskFragment(null,"New Task")
                                             findNavController().navigate(action)
                                        }
                                        is taskViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                                                val action = tasksFragmentDirections.actionTasksFragmentToAddedittaskFragment(event.task,"Edit Task")
                                                findNavController().navigate(action)
                                        }
                                        is taskViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                                                Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_LONG).show()

                                        }
                                        taskViewModel.TasksEvent.NavigateToDeletAllCompletedScreen -> {
                                                val action = tasksFragmentDirections.actionGlobalDeleteallcompleted()
                                                findNavController().navigate(action)
                                        }
                                }.exhaustive
                        }
                }




                setHasOptionsMenu(true)

        }

        override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.menu_tasks_fragment,menu)
                val searchItem=menu.findItem(R.id.action_search)
                searcView=searchItem.actionView as SearchView

                val pendingQuery = viewModel.searchQuery.value
                if(pendingQuery != null && pendingQuery.isNotEmpty()){
                        searchItem.expandActionView()
                        searcView.setQuery(pendingQuery,false)
                }

                searcView.onQueryTextChanged {
                                viewModel.searchQuery.value=it
                }

                viewLifecycleOwner.lifecycleScope.launch {
                        menu.findItem(R.id.action_hide_completed_taks).isChecked=
                                viewModel.preferencesFlow.first().hideCompleted
                }
        }


        override fun onOptionsItemSelected(item: MenuItem): Boolean {
                return when(item.itemId){
                        R.id.action_sort_byname ->{
                                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                                true
                        }
                        R.id.action_sort_by_datecreated ->{
                                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                                true
                        }
                        R.id.action_hide_completed_taks ->{
                                item.isChecked=!item.isChecked
                                viewModel.onHideCompletedClick(item.isChecked)
                                true
                        }
                        R.id.action_delete_all_completed ->{
                                viewModel.onDeleteAllCompletedClick()
                                true
                        }
                        else -> super.onOptionsItemSelected(item)
                }
        }

        override fun onDestroy() {
                super.onDestroy()
                searcView.setOnQueryTextListener(null)
        }

        override fun onItemClick(task: Task) {
                        viewModel.onTaskSelected(task)
        }

        override fun onCheckboxClick(task: Task, isChecked: Boolean) {
                        viewModel.onTaskCheckedChanged(task,isChecked)
        }
}