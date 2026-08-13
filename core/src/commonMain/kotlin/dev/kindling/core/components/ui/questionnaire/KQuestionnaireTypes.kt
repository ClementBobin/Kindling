package dev.kindling.core.components.ui.questionnaire

enum class KQuestionnaireChoiceType {
    RADIO, CHECKBOX
}

data class KQuestionnaireChoiceItem(
    val id: String,
    val label: String,
    val description: String? = null,
    val shortcut: String? = null,
    val enabled: Boolean = true
)