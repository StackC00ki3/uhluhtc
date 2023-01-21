package me.uncookie.features

import kotlinx.coroutines.yield
import java.math.BigDecimal
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt


class Dice(val turns: Int, val sides: Int) {
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

    suspend fun calculate(m: Int = sides, n: Int = turns, x: Int = m * n) {
        /* Create a table to store the results of subproblems.
    One extra row and column are used for simplicity
    (Number of dice is directly used as row index and sum is directly used as column index).
    The entries in 0th row and 0th column are never used. */
        val table = Array(n + 1) { Array<BigDecimal>(x + 1) { BigDecimal.valueOf(0, 4) } }
        /* Table entries for only one dice */
        var j = 1
        while (j <= m && j <= x) {
            table[1][j] = BigDecimal.valueOf(10000, 4)
            j++
        }

        /* Fill rest of the entries in table using recursive relation
    i: number of dice, z: sum */for (i in 2..n) {
            for (z in 1..x) {
                var k = 1
                while (k < z && k <= m) {
                    yield()
                    table[i][z] += table[i - 1][z - k]
                    k++
                }
            }
        }
        val res = mutableMapOf<Int, Double>()
        for (z in n..x) {
            //print("${table[n][z]} ")
            if (table[n][z] != BigDecimal.ZERO) {
                val out = table[n][z] / total.toBigDecimal()
                //print("${table[n][z]} ")
                res[z] = out.toDouble()
            }
        }
        //println(res)
        prob = res
    }
}

