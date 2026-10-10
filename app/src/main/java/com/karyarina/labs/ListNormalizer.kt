package com.karyarina.labs

import kotlin.random.Random


object ListNormalizer {
    fun generateRandomList(size: Int, min: Int, max: Int): List<Int> {
        val result = mutableListOf<Int>()
        for (i in 0 until size) {
            result.add(Random.nextInt(min, max + 1))
        }
        return result
    }
    fun findMax(list: List<Int>): Int? {
        return list.maxOrNull()
    }
    fun normalizeByMax(list: List<Int>): List<Double> {
        val max = findMax(list)
        if (list.isEmpty() || max == null || max == 0) {
            return emptyList() }
        val result = mutableListOf<Double>()
        for (element in list) {
            result.add(element.toDouble() / max)
        }
        return result
    }
    fun buildResultText(source: List<Int>, normalized: List<Double>): String {
        if (source.isEmpty()) {
            return "Список пуст"
        }
        val sourceText = "Исходный список: " + source.joinToString(", ")
        val maxText = "Максимальный элемент: " + findMax(source)

        if (normalized.isEmpty()) {
            return "$sourceText\n$maxText\n\nНормализация невозможна"
        }
        val normalizedText = "Нормализованный список: " + normalized.joinToString(", ") { "%.2f".format(it) }
        return "$sourceText\n$maxText\n\n$normalizedText"
    }
}
