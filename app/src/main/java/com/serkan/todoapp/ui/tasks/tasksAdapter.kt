package com.serkan.todoapp.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.serkan.todoapp.data.Task
import com.serkan.todoapp.databinding.TaskItemBinding
import kotlin.coroutines.coroutineContext

class tasksAdapter(private val listener:onItemClickListener): ListAdapter<Task,tasksAdapter.TasksViewHolder>(DiffCallback()) {

   inner class TasksViewHolder(private val binding:TaskItemBinding):RecyclerView.ViewHolder(binding.root){
        init {
           binding.apply {
               root.setOnClickListener {
                   val position = adapterPosition
                   if(position != RecyclerView.NO_POSITION){
                       val task = getItem(position)
                       listener.onItemClick(task)
                   }
               }
               taskitemcheckbox.setOnClickListener {
                   val position = adapterPosition
                   if(position != RecyclerView.NO_POSITION){
                       val task = getItem(position)
                       listener.onCheckboxClick(task,taskitemcheckbox.isChecked)
                   }
               }

           }

        }
       fun bind(task:Task){
           binding.apply {
               taskitemcheckbox.isChecked=task.completed
               taskitemtextview.text =task.name
               taskitemtextview.paint.isStrikeThruText = task.completed
               taskimagepriority.isVisible = task.important
           }
       }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding=TaskItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
         val currentItem=getItem(position)
         holder.bind(currentItem)
    }


    class DiffCallback:DiffUtil.ItemCallback<Task>(){
        override fun areItemsTheSame(oldItem: Task, newItem: Task)=oldItem.id==newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task)=oldItem==newItem

    }

    interface onItemClickListener{
        fun onItemClick(task:Task)
        fun onCheckboxClick(task:Task,isChecked:Boolean)
    }
}