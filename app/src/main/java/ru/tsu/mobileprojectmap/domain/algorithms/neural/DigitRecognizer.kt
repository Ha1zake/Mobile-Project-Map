package ru.tsu.mobileprojectmap.domain.algorithms.neural

/**
 * Простейший распознаватель цифр по 5x5 бинарной сетке.
 *
 * В контексте учебного проекта это работает как "модель": мы сравниваем ввод с набором эталонных
 * шаблонов и выбираем цифру с минимальным числом расхождений.
 */
object DigitRecognizer {

    fun recognizeDigit(cells: List<Boolean>, gridSize: Int = 5): Int? {
        val expectedSize = gridSize * gridSize
        require(cells.size == expectedSize) {
            "Неверный размер сетки: expected=$expectedSize actual=${cells.size}"
        }

        val filledCount = cells.count { it }
        if (filledCount == 0) return null

        val templates = digitTemplates(gridSize)

        var bestDigit: Int? = null
        var bestMismatch = Int.MAX_VALUE

        for (digit in 0..9) {
            val template = templates[digit] ?: continue
            var mismatch = 0
            for (i in 0 until expectedSize) {
                if (cells[i] != template[i]) mismatch++
            }
            if (mismatch < bestMismatch) {
                bestMismatch = mismatch
                bestDigit = digit
            }
        }

        // Если ввод сильно "не похож" ни на одну цифру — считаем, что распознавание не удалось.
        // Для 25 клеток порог 12 обычно даёт стабильный результат.
        return if (bestMismatch <= 12) bestDigit else null
    }

    private fun digitTemplates(gridSize: Int): Map<Int, List<Boolean>> {
        require(gridSize == 5) {
            "Сейчас распознаватель реализован только для 5x5."
        }

        // 1 - закрашено, 0 - пусто
        val raw = mapOf(
            0 to listOf(
                "01110",
                "10001",
                "10001",
                "10001",
                "01110"
            ),
            1 to listOf(
                "00100",
                "01100",
                "00100",
                "00100",
                "01110"
            ),
            2 to listOf(
                "01110",
                "10001",
                "00010",
                "00100",
                "11111"
            ),
            3 to listOf(
                "11110",
                "00001",
                "01110",
                "00001",
                "11110"
            ),
            4 to listOf(
                "10010",
                "10010",
                "11111",
                "00010",
                "00010"
            ),
            5 to listOf(
                "11111",
                "10000",
                "11110",
                "00001",
                "11110"
            ),
            6 to listOf(
                "01110",
                "10000",
                "11110",
                "10001",
                "01110"
            ),
            7 to listOf(
                "11111",
                "00001",
                "00010",
                "00100",
                "00100"
            ),
            8 to listOf(
                "01110",
                "10001",
                "01110",
                "10001",
                "01110"
            ),
            9 to listOf(
                "01110",
                "10001",
                "01111",
                "00001",
                "01110"
            )
        )

        return raw.mapValues { (_, pattern) ->
            pattern.flatMap { row ->
                row.map { ch -> ch == '1' }
            }
        }
    }
}

