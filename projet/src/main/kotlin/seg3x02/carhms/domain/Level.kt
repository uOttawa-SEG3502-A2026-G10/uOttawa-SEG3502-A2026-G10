package seg3x02.carhms.domain

enum class Level {
    BEGINNER, INTERMEDIATE, ADVANCED, EXPERT;

    companion object {
        fun parse(value: String): Level = when (value.trim().lowercase()) {
            "beginner", "débutant", "debutant", "easy", "facile" -> BEGINNER
            "intermediate", "intermédiaire", "intermediaire", "moderate", "modéré", "modere" -> INTERMEDIATE
            "advanced", "avancé", "avance", "difficult", "difficile" -> ADVANCED
            "expert", "very difficult", "very_difficult", "très difficile", "tres difficile" -> EXPERT
            else -> throw IllegalArgumentException("Unknown level: $value")
        }
    }

    fun permits(difficulty: Level) = difficulty.ordinal <= ordinal + 1
    fun canLead(difficulty: Level) = difficulty.ordinal <= ordinal
    fun difficultyName() = listOf("easy", "moderate", "difficult", "very difficult")[ordinal]
}
