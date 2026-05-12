package fr.monfort.taskmanager.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fr.monfort.taskmanager.data.model.Task
import fr.monfort.taskmanager.data.repository.TaskRepository
import fr.monfort.taskmanager.domain.usecase.CreateTask
import fr.monfort.taskmanager.domain.usecase.DeleteTask
import fr.monfort.taskmanager.domain.usecase.ModifyTask
import fr.monfort.taskmanager.domain.usecase.MoveTask
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel(
    private val repository: TaskRepository
) : ViewModel() {
    val tasks : StateFlow<List<Task>> = repository.tasks

    fun createTask(title: String, parentId: String? = null) {
        CreateTask(repository).execute(title, parentId)
    }

    fun modifyTitle(taskId: String, newTitle: String) {
        ModifyTask(repository).execute(taskId, newTitle)
    }

    fun deleteTask(taskId: String) {
        DeleteTask(repository).execute(taskId)
    }

    fun moveTask(taskId: String, newParentId: String?, newOrder: Int) {
        MoveTask(repository).execute(taskId, newParentId, newOrder)
    }
}

class HomeViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repository) as T
    }
}