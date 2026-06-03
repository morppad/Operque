package com.morppad.operque.data.repository

import com.morppad.operque.data.model.Comment
import com.morppad.operque.data.model.CreateCommentDto
import com.morppad.operque.data.services.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class CommentRepository {
    private companion object {
        const val CommentsTable = "comments"
    }

    private val client get() = SupabaseClientProvider.client
    private val profileRepository = ProfileRepository()

    suspend fun getTaskComments(taskId: String): List<Comment> {
        return client.from(CommentsTable).select {
            filter { eq("task_id", taskId) }
            order("created_at", Order.ASCENDING)
        }.decodeList<Comment>()
    }

    suspend fun addComment(taskId: String, text: String) {
        val userId = requireNotNull(profileRepository.currentUserId()) { "No active user session" }
        client.from(CommentsTable).insert(
            CreateCommentDto(
                taskId = taskId,
                userId = userId,
                text = text.trim()
            )
        )
    }
}
