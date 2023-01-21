package me.uncookie.features

import com.charleskorn.kaml.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.uncookie.Uhluhtc
import me.uncookie.features.Translation.adTranslation
import me.uncookie.features.Translation.atTranslation
import me.uncookie.features.Translation.flTranslation
import me.uncookie.features.Translation.monTranslation
import me.uncookie.features.Translation.translation
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.debug
import org.jsoup.HttpStatusException

object MonsterDB {
    val db = Uhluhtc.dataFolder
        .walk()
        .filter { it.isFile && it.name.endsWith(".yaml") }
        .map {
            Yaml.default.parseToYamlNode(it.inputStream())
        }
        .toSet()

    suspend fun GroupMessageEvent.monNameTranslation() {
        if (message.content.startsWith("翻译") && this.message.content.length > 3) {
            var fMsg = message.content.removePrefix("翻译")
            monTranslation.forEach { (k, v) -> fMsg = fMsg.replace(k, v) }
            group.sendMessage(fMsg)
        }
    }

    suspend fun GroupMessageEvent.monQuery() {
        if (!this.message.content.startsWith("#")
            || !this.message.content.contains("?")
            || this.message.content.length <= 2
        ) {
            return
        }

        //split out variant prefix and monster name
        val variant = this.message.content.run { subSequence(1, indexOf("?")) }
        val monName = this.message.content.removePrefix("#$variant?")
        Uhluhtc.logger.debug { "var:$variant           monName:$monName" }

        var res = ""    //plain text result
        var color = ""  //symbol color
        var sym = ""    //symbol
        var lc = ""     //leave_corpse
        var name = ""   //monster name
        db.firstOrNull { it.yamlMap.getScalar("prefix")?.content == variant }
            ?.yamlMap?.get<YamlList>("monsters")?.items
            ?.firstOrNull {
                it.yamlMap.getScalar("name")?.content?.lowercase() == monName.lowercase()
                        || (it.yamlMap.getScalar("name")?.content?.lowercase()
                        == monTranslation.entries.find { e -> e.value == monName }?.key?.lowercase())
            }
            ?.yamlMap?.entries?.forEach { (k, v) ->
                //formatted value
                val vf = v.contentToString().replace("'", "")
                    .replace("[", "")
                    .replace("]", "")
                    .replace("null", "无")
                res += when (k.content) {
                    //optimize msg
                    "generates" -> "生成于: $vf"
                    "not-generated-normally" -> if (vf == "No") "  (不随机生成)" else "  (随机生成)"
                    "appears-in-small-groups" -> if (vf == "Yes") " (成小群出现)" else ""
                    "appears-in-large-groups" -> if (vf == "Yes") " (成大群出现)\n$lc" else "\n$lc"
                    "genocidable" -> if (vf == "Yes") "可灭绝\n" else "不可灭绝\n"
                    "leaves-corpse" -> {
                        lc = if (vf == "Yes") "死后会留下尸体\n" else "死后不留下尸体\n"
                        ""
                    }
                    "symbol" -> {
                        sym = vf
                        "符号: $vf\n"
                    }
                    "color" -> {
                        color = vf
                        ""
                    }
                    "attacks" -> {
                        var re = "攻击: "
                        v.yamlList.items.forEach {
                            re += (atTranslation[it.yamlList[0].yamlScalar.content]
                                ?: it.yamlList[0].yamlScalar.content) +
                                    " " +
                                    (adTranslation[it.yamlList[1].yamlScalar.content]
                                        ?: it.yamlList[1].yamlScalar.content) +
                                    " " +
                                    it.yamlList[2].yamlScalar.content + "d" + it.yamlList[3].yamlScalar.content +
                                    ", "
                        }
                        re.substring(0, re.length - 2) + "\n"
                    }
                    "flags" -> {
                        var re = "属性: "
                        v.yamlList.items.forEach {
                            re += (flTranslation[it.yamlScalar.content] ?: it.yamlScalar.content) + ", "
                        }
                        re.substring(0, re.length - 2)
                    }
                    "name" -> {
                        name = vf
                        "怪物名: ${monTranslation[name] ?: name}\n"
                    }
                    else -> translation[k.content] + ": " + vf + "\n"
                }
            }

        Uhluhtc.logger.debug { "res:$res" }

        //submit result
        if (res.isNotBlank()) {
            //generate the image of monster symbol
            val im = Tiles.genSymImage(sym, color)
            val upIm = im.uploadAsImage(group, "png")
            withContext(Dispatchers.IO) {
                im.close()
            }

            try {
                val im2 = Tiles.genWikiImage(name)
                val upIm2 = im2.uploadAsImage(group, "png")
                withContext(Dispatchers.IO) {
                    im.close()
                }
                group.sendMessage(upIm + upIm2 + res)
            } catch (_: HttpStatusException) {
                group.sendMessage(upIm + res)
            }

        } else {
            group.sendMessage("查无此怪")
        }
    }
}
