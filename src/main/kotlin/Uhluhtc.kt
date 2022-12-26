package me.uncookie

import com.charleskorn.kaml.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        "conferred" to "食用可获得抗性"
    )

    private val flTranslation = mapOf(
        "FlDisplaces" to "影流之主",
        "FlVanDmgRduc" to "有伤害减免",
        "FlBlinkAway" to "能闪现",
        "FlFairy" to "fairy",
        "FlScentTracker" to "tracks scents",
        "FlOviparous" to "oviparous",
        "FlTouchPetrifies" to "石化之触",
        "FlInvisible" to "能隐身",
        "FlWallwalk" to "能穿墙",
        "FlFly" to "能飞",
        "FlSwim" to "能游泳",
        "FlAmorphous" to "amorphous",
        "FlTunnel" to "会打洞",
        "FlCling" to "clings",
        "FlConceal" to "conceals",
        "FlAmphibious" to "amphibious",
        "FlBreathless" to "不需要呼吸",
        "FlSeeInvis" to "可看破隐身",
        "FlThickHide" to "thick hide",
        "FlRegen" to "生命恢复",
        "FlUnSolid" to "非固态",
        "FlInfravisible" to "可被红外线侦测",
        "FlCovetous" to "贪婪",
        "FlMindless" to "无脑",
        "FlNoPoly" to "玩家不能变形为该生物",
        "FlTeleport" to "会传送",
        "FlUndead" to "亡灵",
        "FlDemon" to "恶魔",
        "FlVegan" to "极端素食主义者",
        "FlVegetarian" to "素食主义者",
        "FlStalk" to "stalker",
        "FlMetallivore" to "metallivore",
        "FlPoisonous" to "有毒",
        "FlLithivore" to "lithivore",
        "FlPassesBars" to "能挤过铁栅栏",
        "FlHatesSilver" to "怕银质物品"
    )

    private val atTranslation = mapOf(
        "AtNone" to "passive",
        "AtClaw" to "claw",
        "AtBite" to "bite",
        "AtKick" to "kick",
        "AtButt" to "butt",
        "AtTouch" to "touch",
        "AtSting" to "sting",
        "AtHug" to "hug",
        "AtSpit" to "spit",
        "AtEngulf" to "engulf",
        "AtBreath" to "breath",
        "AtExplode" to "explode",
        "AtSuicideExplode" to "suicide explode",
        "AtGaze" to "gaze",
        "AtTentacle" to "tentacle",
        "AtWeapon" to "weapon",
        "AtCast" to "cast",
        "AtScre" to "scream",
        "AtMultiply" to "multiply",
        "AtArrow" to "arrow",
        "AtReach" to "reach",
        "AtMirror" to "mirror",
        "AtWhip" to "whip",
        "AtMMagical" to "mmmmagical",
        "AtReachingBite" to "reaching",
        "AtLash" to "lashing",
        "AtTrample" to "trample",
        "AtScratch" to "scratch",
        "AtIllurien" to "illurien-swallow",
        "AtTinker" to "tinker",
        "AtPhaseNonContact" to "non-contacting-phase",
        "AtBeamNonContact" to "non-contacting-beam",
        "AtMillionArms" to "million-weaponized-arms",
        "AtSpin" to "spin",
        "AtAny" to "any",
        "AtRangedThorns" to "ranged-thorns",
        "AtCloseRangeBreath" to "close-range-breath",
        "AtOffhandedWeapon" to "offhanded-weapon",
        "AtOffOffhandedWeapon" to "offoffhanded-weapon",
        "AtNonContactAttack" to "non-contact-attack",
        "AtReachTouch" to "longreaching-touch",
        "AtReachBite" to "longreaching-bite",
        "AtPassiveWideGaze" to "passive-gaze",
        "AtHitsIfTwoPreviousHitsConnect" to "hits-if-two-previous-hits-connect",
        "AtLashingVine" to "lashing-vines",
        "AtBlackGoat" to "black-goat-shenanigans",
        "AtAutoHit" to "autohit",
        "AtAdjacent" to "adjacent",
        "AtTalk" to "talk",
        "AtTailSlap" to "tailslap",
        "AtVolley" to "volley",
        "AtWolfHeadBite" to "wolfhead-bite"
    )

    private val adTranslation = mapOf(
        "AdDimness" to "dimness",
        "AdMapAmnesia" to "map-amnesia",
        "AdIncreaseWeight" to "increase-weight",
        "AdCast" to "cast",
        "AdChaos" to "chaos",
        "AdVomitInducing" to "vomit-inducing",
        "AdNegativeEnchantment" to "negative-enchantment",
        "AdVaporization" to "vaporization",
        "AdStoneEdge" to "stone-edge",
        "AdLitterBlob" to "litter-blob",
        "AdCreateTrap" to "create-trap",
        "AdRngIntervention" to "rng-intervention",
        "AdIdentityAttack" to "identity-attack",
        "AdFrenzy" to "frenzy",
        "AdNether" to "nether",
        "AdInsanity" to "insanity",
        "AdNastyTrap" to "nasty-trap",
        "AdSkillCapReduce" to "skill-cap-reducting",
        "AdDreamAttack" to "dream-eating",
        "AdBadRandomEffect" to "bad-random-effect",
        "AdFumble" to "fumble",
        "AdVenomous" to "venomous",
        "AdVulnerability" to "vulnerability-inducing",
        "AdCurseItems" to "curse-items",
        "AdSludge" to "sludge",
        "AdMasterBlaster" to "masterblaster",
        "AdPits" to "pits",
        "AdIceBlock" to "iceblock",
        "AdStinkingCloud" to "stinking-cloud",
        "AdFeelPain" to "feel-pain",
        "AdDeadGaze" to "deadly-gaze",
        "AdGravity" to "gravity",
        "AdSound" to "sound",
        "AdVampireDrain" to "vampire-drain",
        "AdNegativeProtection" to "negative-protection",
        "AdDepression" to "depressing",
        "AdPoisonStat" to "poison-sting",
        "AdNexus" to "nexus",
        "AdSuckEquipment" to "suck-equipment",
        "AdBanishment" to "banishment",
        "AdCursedUnihorn" to "cursed-unicorn-horn",
        "AdLazyness" to "lazy",
        "AdPlasma" to "plasma",
        "AdDrainsAllSortsOfStuff" to "drains-all-sorts-of-stuff",
        "AdFakeMessages" to "fake-message",
        "AdCharisma" to "charisma-taking",
        "AdWrath" to "wrath",
        "AdDrainLifeOrStats" to "drain-life-and/or-stats",
        "AdInertia" to "inertia",
        "AdThirsty" to "thirsty",
        "AdMana" to "mana",
        "AdSilverStarlightRapier" to "silver-starlight-rapier",
        "AdRandomGaze" to "random gaze",
        "AdHalfDragon" to "half-dragon",
        "AdStealByTeleportation" to "steal-by-teleportation",
        "AdFear" to "fear",
        "AdBlackWebShadow" to "black-web-shadow",
        "AdNetzach" to "netzach",
        "AdWatcherTentacleGaze" to "magical-tentacle-gaze",
        "AdNumb" to "numb",
        "AdFreezeSolid" to "freeze-solid",
        "AdWither" to "wither",
        "AdBurn" to "burn",
        "AdDisplacement" to "displacement",
        "AdTinker" to "tinker",
        "AdFireworks" to "fireworks",
        "AdOona" to "oona",
        "AdStudy" to "study",
        "AdCalm" to "calm",
        "AdTickle" to "tickle",
        "AdPoly" to "poly",
        "AdBehead" to "behead",
        "AdCancellation" to "cancellation",
        "AdPhys" to "physical",
        "AdMagicMissile" to "magic missile",
        "AdFire" to "fire",
        "AdCold" to "cold",
        "AdSleep" to "sleep",
        "AdDisintegrate" to "disintegrate",
        "AdElectricity" to "shock",
        "AdStrDrain" to "drain str",
        "AdAcid" to "acid",
        "AdSpecial1" to "special1",
        "AdSpecial2" to "special2",
        "AdBlind" to "blind",
        "AdStun" to "stun",
        "AdSlow" to "slow",
        "AdParalyse" to "paralyze",
        "AdLevelDrain" to "level drain",
        "AdMagicDrain" to "magic drain",
        "AdLegs" to "legwound",
        "AdStone" to "petrification",
        "AdSticking" to "sticky",
        "AdGoldSteal" to "gold steal",
        "AdItemSteal" to "item steal",
        "AdSeduce" to "seduce",
        "AdTeleport" to "teleport",
        "AdRust" to "rust",
        "AdConfuse" to "confuse",
        "AdDigest" to "digest",
        "AdHeal" to "heal",
        "AdWrap" to "wrap",
        "AdWere" to "lycanthropy",
        "AdDexDrain" to "drain dex",
        "AdConDrain" to "drain con",
        "AdIntDrain" to "drain int",
        "AdDisease" to "disease",
        "AdRot" to "rot",
        "AdSex" to "sex",
        "AdHallucination" to "hallucination",
        "AdDeath" to "Death",
        "AdPestilence" to "Pestilence",
        "AdFamine" to "Famine",
        "AdSlime" to "slime",
        "AdDisenchant" to "disenchant",
        "AdCorrode" to "corrosion",
        "AdClerical" to "clerical",
        "AdSpell" to "spell",
        "AdRandomBreath" to "random breath",
        "AdAmuletSteal" to "amulet steal",
        "AdCurse" to "curse",
        "AdBlink" to "blink",
        "AdLevelTeleport" to "level teleport",
        "AdDecapitate" to "decapitate",
        "AdFreeze" to "freeze",
        "AdPunisher" to "punisher",
        "AdDrown" to "drown",
        "AdShred" to "shred",
        "AdJailer" to "jailer",
        "AdBoulderArrow" to "boulder-arrow",
        "AdBoulderArrowRandomSpread" to "boulder-arrow-random-spread",
        "AdMultiElementCounterAttackThatAngersTons" to "multi-elemental-counterattack-that-angers-tons",
        "AdPoison" to "poison",
        "AdWisdom" to "wisdom",
        "AdVorpal" to "vorpal",
        "AdStealQuestArtifact" to "steals-quest-artifact",
        "AdSpawnChaos" to "spawn-chaos",
        "AdIronBall" to "iron-ball",
        "AdGrow" to "grow",
        "AdSilver" to "silver",
        "AdAbduction" to "abduction",
        "AdElementalGaze" to "elemental-gaze",
        "AdAsmodeusBlood" to "asmodeus-blood",
        "AdMirror" to "mirror",
        "AdLeviathan" to "leviathan",
        "AdUnknownPriest" to "unknown-priest",
        "AdMalk" to "immobilizing-destroying",
        "AdTentacle" to "tentacle",
        "AdWet" to "wet",
        "AdHeadSpike" to "head-spike",
        "AdTele" to "teleportation",
        "AdLethe" to "lethe-wet",
        "AdHorn" to "horn",
        "AdSolar" to "solar",
        "AdEscalatingDamage" to "escalating-damage",
        "AdSoul" to "soul",
        "AdMist" to "mist",
        "AdSuck" to "suck",
        "AdDrainLuck" to "drain luck",
        "AdSpore" to "spores",
        "AdLava" to "lava",
        "AdSunflower" to "sunflowerpower",
        "AdFernExplosion" to "fernxplosion",
        "AdMandrake" to "mandrake-shriek",
        "AdPhysRetaliate" to "retaliate",
        "AdVamp" to "vampire",
        "AdWebs" to "webs",
        "AdWeeping" to "levtele-drain",
        "AdGaro" to "rumor-dispense",
        "AdGaroMaster" to "oracle-dispense",
        "AdLoadstones" to "loadstone-throw",
        "AdRemoveEngravings" to "remove-engravings",
        "AdIllurien" to "illurien-swallow",
        "AdLightRay" to "light-ray",
        "AdRemoveLight" to "remove-light",
        "AdDisarm" to "disarm",
        "AdIdentityNastiness" to "identity-nastiness",
        "AdItemager" to "item-damaging",
        "AdAntimatter" to "antimatter",
        "AdPain" to "PAIN",
        "AdTech" to "technology",
        "AdMemoryReduce" to "memory-reduce",
        "AdSkillReduce" to "skill-reduce",
        "AdStatDamage" to "stat-damage",
        "AdGearDamage" to "gear-damaging",
        "AdThievery" to "thievery",
        "AdLavaTiles" to "lava-tiles",
        "AdDeletesYourGame" to "data-delete",
        "AdDrainAlignment" to "drain-alignment",
        "AdAddSins" to "makes-you-a-dirty-sinner",
        "AdContamination" to "contamination",
        "AdAggravateMonster" to "makes-you-aggravate-monsters",
        "AdDestroyEq" to "destroys-equipment",
        "AdTrembling" to "gives-you-parkinsons",
        "AdAny" to "any",
        "AdCurseArmor" to "curse-armor",
        "AdIncreaseSanity" to "increase-sanity",
        "AdReallyBadEffect" to "really-bad-effect",
        "AdBleedout" to "bleedout",
        "AdShank" to "shank",
        "AdDrainScore" to "drain-score",
        "AdTerrainTerror" to "terrain-terror",
        "AdFeminism" to "feminism",
        "AdLevitation" to "levitation",
        "AdReduceMagicCancellation" to "reduce-magic-cancellation",
        "AdIllusion" to "illusion",
        "AdSpecificRegularAttack" to "specific-regular-attack",
        "AdSpecificNastyTrap" to "specific-nasty-trap",
        "AdDebuff" to "debuff",
        "AdNivellation" to "nivellation",
        "AdTechDrain" to "technique-drain",
        "AdBlasphemy" to "makes-your-god-angry-at-you",
        "AdDropItems" to "drop-items",
        "AdRemoveErosionProof" to "remove-erosion-proof",
        "AdFlame" to "flame",
        "AdPsionic" to "psionic",
        "AdLoud" to "loud",
        "AdKnockback" to "knockback",
        "AdWater" to "water",
        "AdPitAttack" to "create-pit",
        "AdDrainConstitution" to "drain-constitution",
        "AdDrainStrength" to "drain-strength",
        "AdDrainCharisma" to "drain-charisma",
        "AdDrainDexterity" to "drain-dexterity",
        "AdFleshHook" to "flesh-hook",
        "AdImplantEgg" to "implant-egg",
        "AdDessicate" to "dessicate",
        "AdArrowOfSlaying" to "arrow-of-slaying",
        "AdArchonFire" to "archon-fire",
        "AdGoldify" to "goldify",
        "AdMoonlightRapier" to "moonlight-rapier",
        "AdMummyRot" to "rot",
        "AdMindWipe" to "mindwipe",
        "AdSlowStoning" to "slow-stoning",
        "AdInflictDoubt" to "inflict-doubt",
        "AdRevelatoryWhisper" to "revelatory-whisper",
        "AdPull" to "pull",
        "AdMercuryBlade" to "mercury-blade",
        "AdBloodFrenzy" to "bloodfrenzy",
        "AdPollen" to "pollen",
        "AdElementalCold" to "elemental-cold",
        "AdElementalPoison" to "elemental-poison",
        "AdElementalFire" to "elemental-fire",
        "AdElementalElectric" to "elemental-electric",
        "AdElementalAcid" to "elemental-acid",
        "AdFourSeasons" to "fourseasons",
        "AdCreateSphere" to "create-sphere",
        "AdConflictTouch" to "conflict-touch",
        "AdAntiBloodAttack" to "antiblood-attack",
        "AdFirePoisonPhysicalBlindness" to "fire+poison+physical+blind",
        "AdCharm" to "charm",
        "AdScald" to "scald",
        "AdEatGold" to "eat-gold",
        "AdQuarkFlavour" to "quark-flavour",
        "AdMildHunger" to "mild-hunger",
        "AdShoe" to "SHOE-ATTACK",
        "AdLaser" to "laser",
        "AdNuke" to "nuke",
        "AdUnholy" to "unholy",
        "AdHoly" to "holy",
        "AdLokoban" to "lokoban",
        "AdRock" to "rock",
        "AdHalluSick" to "hallusick",
        "AdYank" to "yank",
        "AdBigExplosion" to "big-explosion",
        "AdExplodingMMSpellbook" to "exploding-magic-missile-spellbooks",
        "AdAlignmentBlast" to "alignment-blast",
        "AdReleaseAlignmentSpirits" to "release-alignment-spirits",
        "AdCrystalMemories" to "crystal-memories",
        "AdDilithiumCrystals" to "dilithium-crystals",
        "AdMirrorBlast" to "mirror-blast",
        "AdVoidWhispers" to "void-whispers",
        "AdWarMachineGaze" to "war-machine-gaze",
        "AdSimurgh" to "simurgh",
        "AdInjectLarva" to "inject-larva",
        "AdMakeSkeletons" to "make-skeletons",
        "AdPotionEffect" to "potion-effect",
        "AdKidnap" to "kidnap",
        "AdLaws" to "law",
        "AdGetLost" to "get-lost",
        "AdTransmute" to "transmute",
        "AdGrowHeads" to "grow-heads",
        "AdForgetItems" to "1%-forget-items",
        "AdWind" to "wind",
        "AdQuills" to "quills",
        "AdVoidDisintegrate" to "void-disintegrate",
        "AdPerHitDie" to "per-hit-die",
        "AdSeverePoison" to "severe-poison",
        "AdHolyUnholyEnergy" to "holy-unholy-energy"
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
            var lc = ""     //leave_corpse
            db.firstOrNull { it.yamlMap.getScalar("prefix")?.content == variant }
                ?.yamlMap?.get<YamlList>("monsters")?.items
                ?.firstOrNull { it.yamlMap.getScalar("name")?.content?.lowercase() == monName.lowercase() }
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
                            re.substring(0, re.length - 2) + "\n"
                        }
                        else -> translation[k.content] + ": " + vf + "\n"
                    }
                }

            logger.debug { "res:$res" }

            //submit result
            if (res.isNotBlank()) {
                //generate the image of monster symbol
                val im = genSymImage(sym, color)
                val upIm = im.uploadAsImage(group, "png")
                withContext(Dispatchers.IO) {
                    im.close()
                }
                group.sendMessage(upIm + res)
            } else {
                group.sendMessage("查无此怪")
            }
        }
    }

    private fun genSymImage(sym: String, color: String): InputStream {
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