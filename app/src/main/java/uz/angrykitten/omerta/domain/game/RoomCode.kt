package uz.angrykitten.omerta.domain.game

import kotlin.random.Random

/**
 * Six-char alphanumeric room codes. Confusable glyphs are excluded (no 0/O,
 * no 1/I/L) so a guest can read a code off another phone screen without
 * misreading.
 */
object RoomCode {

    private const val LENGTH = 6
    private const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"

    fun generate(random: Random = Random.Default): String =
        (1..LENGTH).map { ALPHABET.random(random) }.joinToString("")

    fun isValid(code: String): Boolean =
        code.length == LENGTH && code.all { it in ALPHABET }

    /** Normalises user input: uppercases, strips whitespace, max length capped. */
    fun normalize(input: String): String =
        input.uppercase()
            .filter { it in ALPHABET }
            .take(LENGTH)
}
