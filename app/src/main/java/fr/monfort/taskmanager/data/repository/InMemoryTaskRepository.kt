package fr.monfort.taskmanager.data.repository

import androidx.compose.runtime.mutableStateListOf
import fr.monfort.taskmanager.data.model.Task
import fr.monfort.taskmanager.data.model.TaskNode
import fr.monfort.taskmanager.domain.usecase.TaskTreeBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTaskRepository : TaskRepository {
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    override val tasks : StateFlow<List<Task>> = _tasks.asStateFlow()
    private val treeBuilder = TaskTreeBuilder()

    override fun getChildren(parentId: String?): List<Task> {
        return _tasks.value.filter { it.parentId == parentId }
    }

    override fun getTaskById(id: String): Task? {
        return _tasks.value.find { it.id == id }
    }

    override fun addTask(task: Task) {
        _tasks.update { it + task}
    }

    override fun deleteTask(taskId: String) {
        _tasks.update { current ->
            val toDelete = collectDescendants(current, taskId) + taskId
            current.filterNot { it.id in toDelete }
        }
    }

    override fun updateTask(task: Task) {
        _tasks.update { current ->
            current.map { if (it.id == task.id) task else it }
        }
    }

    override fun updateTitle(taskId: String, newTitle: String) {
        val task = getTaskById(taskId) ?: return
        updateTask(task.copy(title = newTitle))
    }

    override fun moveTask(taskId: String, newParentId: String?, newOrder: Int) {
        val task = getTaskById(taskId) ?: return

        updateTask(
            task.copy(
                parentId = newParentId,
                order = newOrder
            )
        )
    }

    private fun collectDescendants(all: List<Task>, rootId: String): Set<String> {
        val result = mutableSetOf<String>()
        val queue = ArrayDeque<String>().apply { add(rootId) }

        while(queue.isNotEmpty()) {
            val current = queue.removeFirst()
            all.filter { it.parentId == current }.forEach {
                result.add(it.id)
                queue.add(it.id)
            }
        }

        return result
    }
}