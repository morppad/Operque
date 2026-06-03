package com.morppad.operque.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String,
    @SerialName("task_id")
    val taskId: String,
    @SerialName("user_id")
    val userId: String,
    val text: String,
    @SerialName("created_at")
    val createdAt: Instant? = null
)

@Serializable
data class CreateCommentDto(
    @SerialName("task_id")
    val taskId: String,
    @SerialName("user_id")
    val userId: String,
    val text: String
)
