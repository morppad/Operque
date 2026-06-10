package com.morppad.operque.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object TaskStatus {
    const val Todo = "todo"
    const val InProgress = "in_progress"
    const val Done = "done"

    val All = listOf(Todo, InProgress, Done)
}

@Serializable
data class Task(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    val status: String = TaskStatus.Todo,
    @SerialName("created_at")
    val createdAt: Instant? = null,
    @SerialName("updated_at")
    val updatedAt: Instant? = null
)

@Serializable
data class CreateTaskDto(
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val description: String? = null,
    val status: String = TaskStatus.Todo
)

@Serializable
data class UpdateTaskStatusDto(
    val status: String
)
