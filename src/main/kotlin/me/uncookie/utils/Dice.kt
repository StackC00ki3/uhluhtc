package me.uncookie.utils

import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt


class Dice(turns: Int, sides: Int) {
    private val df = DecimalFormat("#.00")
    private fun fm(d: Double): Double = df.format(d).toDouble()
    val mean = turns.toDouble() * (sides.toDouble() + 1.0) / 2.0
    val fMean = fm(mean)
    val deviation = turns.toDouble() * (sides.toDouble().pow(2) - 1) / 12
    val total = sides.toDouble().pow(turns)
    val sDeviation = sqrt(deviation)
    val fSdeviation = fm(sDeviation)
    //val max = turns * sides
    //val min = turns
    //val cmb = turns.toDouble().pow(sides.toDouble())
    var prob = mutableMapOf<Int, Double>()

    init {
        prob = calculate(sides,turns)
    }

    fun calculate(m: Int, n: Int, x: Int = m*n): MutableMap<Int, Double> {
        /* Create a table to store the results of subproblems.
    One extra row and column are used for simplicity
    (Number of dice is directly used as row index and sum is directly used as column index).
    The entries in 0th row and 0th column are never used. */
        val table = Array(n + 1) { LongArray(x + 1) }
        /* Table entries for only one dice */
        var j = 1
        while (j <= m && j <= x) {
            table[1][j] = 1
            j++
        }

        /* Fill rest of the entries in table using recursive relation
    i: number of dice, z: sum */for (i in 2..n) {
            for (z in 1..x) {
                var k = 1
                while (k < z && k <= m) {
                    table[i][z] += table[i - 1][z - k]
                    k++
                }
            }
        }
        val res = mutableMapOf<Int,Double>()
        (n..n*m).forEach { res[it] = 0.0 }
        for (i in 2..n) {
            for (z in 1..x) {
                //print("${table[i][z]} ")
                if (table[i][z] != 0L) {
                    res[z] = table[i][z].toDouble() / total
                }
            }
            //println()
        }
        //println(res)
        return res
    }

}

