package com.example.sololeveling.logic

/**
 * Robust enum for Player Stats to replace string literals.
 */
enum class StatType(val displayName: String) {
    FITNESS("Fitness"),       // STR
    KNOWLEDGE("Knowledge"),   // INT
    DISCIPLINE("Discipline"), // VIT
    AWARENESS("Awareness"),   // AGI
    CHARISMA("Charisma"),     // CHR
    LUCK("Luck");             // LUK

    companion object {
        fun fromString(value: String): StatType? {
            return when (value.uppercase()) {
                "STR", "FITNESS", "FIT" -> FITNESS
                "INT", "KNOWLEDGE", "KNL" -> KNOWLEDGE
                "VIT", "DISCIPLINE", "DISC" -> DISCIPLINE
                "AGI", "AWARENESS", "AWK" -> AWARENESS
                "CHR", "CHARISMA" -> CHARISMA
                "LUK", "LUCK" -> LUCK
                else -> null
            }
        }
    }
}
