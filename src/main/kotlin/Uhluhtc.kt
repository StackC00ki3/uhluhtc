package me.uncookie

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.yamlMap
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.info
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO


object Uhluhtc : KotlinPlugin(
    JvmPluginDescription(
        id = "me.uncookie.uhluhtc",
        name = "uhluhtc",
        version = "1.0-SNAPSHOT",
    ) {
        author("UnfortuneCookie")
    }
) {
    //translation map
    private val translation = mapOf(
        "name" to "怪物名",
        "symbol" to "符号",
        "base-level" to "基础等级",
        "difficulty" to "难度",
        "speed" to "速度",
        "ac" to "AC",
        "mr" to "魔抗",
        "alignment" to "阵营",
        "attacks" to "攻击",
        "weight" to "重量",
        "nutrition" to "营养价值",
        "size" to "体型",
        "resistances" to "抗性",
        "conferred" to "食用可获得抗性",
        "flags" to "属性",
    )

    override fun onEnable() {
        logger.info { "Plugin loaded" }

        //prepare monsterDB
        val db = dataFolder
            .walk()
            .filter { it.isFile }
            .map {
                Yaml.default.parseToYamlNode(it.inputStream())
            }
            .toMutableSet()
        logger.info { "Loaded ${db.size} variants' monsterDB" }

        //main event
        globalEventChannel().subscribeAlways<GroupMessageEvent> {
            if (!this.message.content.startsWith("#")
                || !this.message.content.contains("?")
                || this.message.content.length <= 2
            ) {
                return@subscribeAlways
            }

            //split out variant prefix and monster name
            val variant = this.message.content.run { subSequence(1, indexOf("?")) }
            val monName = this.message.content.removePrefix("#$variant?")
            logger.info { "var:$variant           monName:$monName" }

            var res = ""    //plain text result
            var color = ""  //symbol color
            var sym = ""    //symbol
            db.firstOrNull { it.yamlMap.getScalar("prefix")?.content == variant }
                ?.yamlMap?.get<YamlList>("monsters")?.items
                ?.firstOrNull { it.yamlMap.getScalar("name")?.content?.lowercase() == monName.lowercase() }
                ?.yamlMap?.entries?.forEach { (k, v) ->
                    //formatted value
                    val vf = v.contentToString().replace("'", "")
                        .replace("[", "")
                        .replace("]", "")
                    res += when (k.content) {
                        //optimize msg
                        "generates" -> "出现频率: $vf"
                        "not-generated-normally" -> if (vf == "No") " (不随机出现)" else ""
                        "appears-in-small-groups" -> if (vf == "Yes") " (成小群出现)" else ""
                        "appears-in-large-groups" -> if (vf == "Yes") " (成大群出现)" else ""
                        "genocidable" -> if (vf == "Yes") "可灭绝\n" else "不可灭绝\n"
                        "leaves-corpse" -> if (vf == "Yes") "\n死后会留下尸体\n" else "\n死后不留下尸体\n"
                        "symbol" -> {
                            sym = vf
                            "符号: $vf\n"
                        }
                        "color" -> {
                            color = vf
                            ""
                        }
                        else -> translation[k.content] + ": " + vf + "\n"
                    }
                }

            //generate the image of monster symbol
            val im = genImage(sym, color).uploadAsImage(group, "png")

            logger.debug { "res:$res" }

            //submit result
            if (res.isNotBlank()) {
                group.sendMessage(im + res)
            } else {
                group.sendMessage("查无此怪")
            }
        }
    }

    private fun genImage(sym: String, color: String): InputStream {
        val image = BufferedImage(13, 22, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()

        //get default color set
        g.color = (Class.forName("java.awt.Color").getField(color.lowercase()).get(null) ?: Color.white) as Color

        //font
        val font = Font("DejaVuSansMono", Font.PLAIN, 18)
        g.font = font
        val metrics = g.getFontMetrics(font)

        //center pos
        val positionX: Int = (image.width - metrics.stringWidth(sym)) / 2
        val positionY: Int = (image.height - metrics.height) / 2 + metrics.ascent

        g.drawString(sym, positionX, positionY)

        //output
        val os = ByteArrayOutputStream()
        ImageIO.write(image, "png", os)
        return ByteArrayInputStream(os.toByteArray())
    }
}