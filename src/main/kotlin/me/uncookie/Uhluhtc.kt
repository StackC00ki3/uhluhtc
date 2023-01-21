package me.uncookie

import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.yamlMap
import jetbrains.datalore.base.math.ipow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import me.uncookie.features.Dice
import me.uncookie.features.MonsterDB.db
import me.uncookie.features.MonsterDB.monNameTranslation
import me.uncookie.features.MonsterDB.monQuery
import me.uncookie.features.Tiles.genImage
import me.uncookie.features.Translation.monTranslation
import me.uncookie.utils.PlotImageExport
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.info
import org.jetbrains.letsPlot.geom.geomHLine
import org.jetbrains.letsPlot.geom.geomLine
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.geom.geomText
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.pos.positionNudge
import org.jetbrains.letsPlot.scale.scaleYContinuous

object Uhluhtc : KotlinPlugin(
    JvmPluginDescription(
        id = "me.uncookie.uhluhtc",
        name = "uhluhtc",
        version = "1.0-SNAPSHOT",
    ) {
        author("UnfortuneCookie")
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        logger.info { "Loaded ${db.size} variants' monsterDB" }

        //main event
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            //feat: assist DragonBOT
            assistDragonBOT()

            //feat: translate the monster name
            monNameTranslation()

            //feat: query the monster
            monQuery()

            //feat: caculate xdy
            rngCalculator()

            //feat: help
            help()
        }
    }

    private suspend fun GroupMessageEvent.assistDragonBOT() {
        if (message.content.startsWith("查询怪物") && this.message.content.length > 4) {
            val monName = message.content.removePrefix("查询怪物")
            val monEnName = monTranslation.entries.find { it.value == monName }?.key
            if (monEnName != null) {
                val upIm = mutableListOf<Image>()
                genImage(monEnName).forEach {
                    upIm += it.uploadAsImage(group, "png")
                    withContext(Dispatchers.IO) {
                        it.close()
                    }
                }
                group.sendMessage(PlainText("$monName:\n") + upIm)
            } else {
                var res = "怪物 $monName 存在于以下分支:\n"
                val count = db.filter {
                    it.yamlMap.get<YamlList>("monsters")?.items
                        ?.any { mon -> mon.yamlMap.getScalar("name")?.content?.lowercase() == monName.lowercase() }
                        ?: false
                }
                    .onEach {
                        val variant = it.yamlMap.getScalar("variant")?.content
                        val prefix = it.yamlMap.getScalar("prefix")?.content
                        res += "$variant: 查询请发 #$prefix?$monName\n"
                    }
                    .size

                if (count > 0) {
                    group.sendMessage(res.removeSuffix("\n"))
                }
            }
        }
    }

    private suspend fun GroupMessageEvent.rngCalculator() {
        if (message.content.startsWith("比较") && this.message.content.length > 3) {
            val fMsg = message.content.removePrefix("比较")
            val cond = mutableListOf<String>()
            val sum = mutableListOf<Int>()
            val prob = mutableListOf<Double>()
            val data = mapOf(
                "图例 (均值 / 标准差)" to cond,
                "面数和" to sum,
                "概率" to prob
            )
            val means = mutableMapOf<Int, Double>()
            val max = mutableListOf<Int>()
            Regex("([0-9]+)d([0-9]+)(\\+|-?)([0-9]*)").findAll(fMsg).forEach {
                val d = Dice(it.groupValues[1].toInt(), it.groupValues[2].toInt())
                try {
                    val caculateResult = withTimeoutOrNull(5000L) {
                        d.calculate()
                        "OK"
                    }
                    if (caculateResult == null) {
                        group.sendMessage("${d.turns}d${d.sides} 的计算耗时已超过5000ms，该运算的时间复杂度为${(d.turns * d.sides).ipow(2)}，为避免滥用已跳过本次运算")
                        return@forEach
                    }
                } catch (e:OutOfMemoryError){
                    group.sendMessage("内存不足，跳过 ${d.turns}d${d.sides}")
                    return@forEach
                }
                d.prob.run {
                    if (it.groupValues[3].isEmpty() || it.groupValues[4].isEmpty()) {
                        sum += keys.toList()
                        prob += values.toList()
                    } else {
                        sum += keys.map { k ->
                            when (it.groupValues[3]) {
                                "+" -> k + it.groupValues[4].toInt()
                                "-" -> k - it.groupValues[4].toInt()
                                else -> 0 //impossible
                            }
                        }
                            .toList()
                        prob += values.toList()
                    }
                    val name = it.groupValues[0]
                    cond += List(size) { "$name (${d.fMean} / ${d.fSdeviation})" }
                    means += maxByOrNull { a -> a.value }!!.key to maxByOrNull { a -> a.value }!!.value
                    max += maxByOrNull { a -> a.key }!!.key
                }
            }
            var p = letsPlot(data) + geomLine {
                x = "面数和"; y = "概率"; color = "图例 (均值 / 标准差)"
            } + scaleYContinuous(format = ".2%")
            means.forEach {
                p += geomHLine(
                    yintercept = it.value,
                    linetype = "dashed"
                ) + geomText(
                    label = it.value.toString(),
                    y = it.value,
                    x = max.maxOf { it },
                    labelFormat = ".2%",
                    size = 5
                ) + geomPoint(
                    x = it.key,
                    y = it.value,
                    color = "red"
                ) + geomText(
                    label = it.key.toString(),
                    position = positionNudge(y = 0.001),
                    x = it.key,
                    y = it.value,
                    size = 6
                )
            }
            //ggsave(p, "ok.html")
            val inStream = PlotImageExport.buildImageFromRawSpecs(p.toSpec(), PlotImageExport.Format.PNG, 1.0, 72.0)
            inStream.sendAsImageTo(group, "png")
            withContext(Dispatchers.IO) {
                inStream.close()
            }
        }
    }

    private suspend fun GroupMessageEvent.help() {
        if (message.content == "卢克") {
            group.sendMessage(
                "功能：\n1.辅助龙龙: 查询怪物<中文> 发送怪物贴图 ; 查询怪物<英文> 在所有nh分支中搜索该怪物并发送可选列表\n" +
                        "2.翻译消息中的怪物名称: 翻译<需要翻译的内容(可包含不是怪物名的内容)>\n" +
                        "3.查询怪物详细信息： #<分支简称>?<英文怪兽名> （分支简称可用查询怪物<英文>来获取）\n" +
                        "4.RNG骰子概率比较/计算器: 比较<若干个 xdy (如：2d12, 3d2)（用空格分隔）>"
            )
        }
    }

}


