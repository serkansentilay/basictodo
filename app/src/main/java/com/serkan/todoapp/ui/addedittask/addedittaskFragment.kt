package com.serkan.todoapp.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.serkan.todoapp.R
import com.serkan.todoapp.databinding.TaskaddfragmenteditBinding
import com.serkan.todoapp.util.exhaustive
import kotlinx.coroutines.flow.collect

class addedittaskFragment:Fragment(R.layout.taskaddfragmentedit) {

    private val viewModel:addedittaskViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = TaskaddfragmenteditBinding.bind(view)
        binding.apply {
            taskaddnameedittext.setText(viewModel.taskName)
            taskaddimportantcheck.isChecked = viewModel.taskImportance
            taskaddimportantcheck.jumpDrawablesToCurrentState()
            taskaddcreatedtext.isVisible = viewModel.task !=null
            taskaddcreatedtext.text = "Created: ${viewModel.task?.createdDateFormatted}"

            taskaddnameedittext.addTextChangedListener {
                viewModel.taskName = it.toString()
            }
            taskaddimportantcheck.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }
            fabSavcheck.setOnClickListener {
                viewModel.onSaveClick()
            }

        }
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            viewModel.addEditTaskEvent.collect { event ->
                when(event){
                    is addedittaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage ->{
                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_LONG).show()
                    }
                    is addedittaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.taskaddnameedittext.clearFocus()
                        setFragmentResult(
                            "add_edit_request"
                            , bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }.exhaustive
            }
        }










    }




















}