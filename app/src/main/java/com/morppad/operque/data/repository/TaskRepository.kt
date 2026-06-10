package com.morppad.operque.data.repository

import com.morppad.operque.data.model.CreateTaskDto
import com.morppad.operque.data.model.Task
import com.morppad.operque.data.model.UpdateTaskStatusDto
import com.morppad.operque.data.services.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class TaskRepository {
    private companion object {
        const val TasksTable = "tasks"
    }

    private val client get() = SupabaseClientProvider.client
    private val profileRepository = ProfileRepository()

    suspend fun getCurrentUserTasks(): List<Task> {
        val userId = profileRepository.currentUserId() ?: return emptyList()
        return client.from(TasksTable).select {
            filter { eq("user_id", userId) }
            order("created_at", Order.DESCENDING)
        }.decodeList<Task>()
    }

    suspend fun getAllTasks(): List<Task> {
        return client.from(TasksTable).select {
            order("created_at", Order.DESCENDING)
        }.decodeList<Task>()
    }

    suspend fun createTaskForUser(userId: String, title: String, description: String?) {
        client.from(TasksTable).insert(
            CreateTaskDto(
                userId = userId,
                title = title.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() }
            )
        )
    }

    suspend fun updateTaskStatus(taskId: String, status: String) {
        val userId = requireNotNull(profileRepository.currentUserId()) { "No active user session" }
        client.from(TasksTable).update(UpdateTaskStatusDto(status)) {
            filter {
                eq("id", taskId)
                eq("user_id", userId)
            }
        }
    }

    suspend fun updateTaskStatusAsManager(taskId: String, status: String) {
        client.from(TasksTable).update(UpdateTaskStatusDto(status)) {
            filter { eq("id", taskId) }
        }
    }

    suspend fun deleteTaskAsManager(taskId: String) {
        client.from(TasksTable).delete {
            filter { eq("id", taskId) }
        }
    }
}
