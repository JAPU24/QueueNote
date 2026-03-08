package com.adrian.queuenote

import java.util.UUID

enum class ProcessStatus {
    PENDIENTE,
    EN_ESPERA,
    COMPLETADO
}

data class SubTask(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val done: Boolean = false
)

data class TaskGroup(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "General",
    val subtasks: List<SubTask> = emptyList()
)

data class ProcessItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val status: ProcessStatus = ProcessStatus.PENDIENTE,
    val groups: List<TaskGroup> = listOf(TaskGroup())
)
