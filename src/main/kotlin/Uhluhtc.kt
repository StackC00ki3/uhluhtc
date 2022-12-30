package me.uncookie

import com.charleskorn.kaml.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.info
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
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
        "FlFairy" to "仙女",
        "FlScentTracker" to "tracks scents",
        "FlOviparous" to "卵生",
        "FlTouchPetrifies" to "石化之触",
        "FlInvisible" to "能隐身",
        "FlWallwalk" to "能穿墙",
        "FlFly" to "能飞",
        "FlSwim" to "能游泳",
        "FlAmorphous" to "无固定形态",
        "FlTunnel" to "会打洞",
        "FlCling" to "clings",
        "FlConceal" to "conceals",
        "FlAmphibious" to "两栖动物",
        "FlBreathless" to "不需要呼吸",
        "FlSeeInvis" to "可看破隐身",
        "FlThickHide" to "皮厚",
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
        "AtNone" to "被动",
        "AtClaw" to "爪击",
        "AtBite" to "咬",
        "AtKick" to "踢",
        "AtButt" to "臀击",
        "AtTouch" to "触摸",
        "AtSting" to "蛰",
        "AtHug" to "抱",
        "AtSpit" to "吐",
        "AtEngulf" to "吞噬",
        "AtBreath" to "吐息",
        "AtExplode" to "爆炸",
        "AtSuicideExplode" to "自爆",
        "AtGaze" to "凝视",
        "AtTentacle" to "触手",
        "AtWeapon" to "武器",
        "AtCast" to "施",
        "AtScre" to "尖叫",
        "AtMultiply" to "multiply",
        "AtArrow" to "arrow",
        "AtReach" to "reach",
        "AtMirror" to "mirror",
        "AtWhip" to "鞭打",
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
        "AtTalk" to "话疗",
        "AtTailSlap" to "tailslap",
        "AtVolley" to "volley",
        "AtWolfHeadBite" to "wolfhead-bite"
    )

    private val adTranslation = mapOf(
        "AdDimness" to "dimness",
        "AdMapAmnesia" to "遗忘地图",
        "AdIncreaseWeight" to "增加体重",
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
        "AdCurseItems" to "诅咒物品",
        "AdSludge" to "sludge",
        "AdMasterBlaster" to "masterblaster",
        "AdPits" to "pits",
        "AdIceBlock" to "iceblock",
        "AdStinkingCloud" to "stinking-cloud",
        "AdFeelPain" to "feel-pain",
        "AdDeadGaze" to "死亡凝视",
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
        "AdNumb" to "麻木",
        "AdFreezeSolid" to "freeze-solid",
        "AdWither" to "凋零",
        "AdBurn" to "焚烧",
        "AdDisplacement" to "displacement",
        "AdTinker" to "tinker",
        "AdFireworks" to "fireworks",
        "AdOona" to "oona",
        "AdStudy" to "study",
        "AdCalm" to "calm",
        "AdTickle" to "tickle",
        "AdPoly" to "poly",
        "AdBehead" to "斩首",
        "AdCancellation" to "cancellation",
        "AdPhys" to "physical",
        "AdMagicMissile" to "magic missile",
        "AdFire" to "火",
        "AdCold" to "冰",
        "AdSleep" to "催眠",
        "AdDisintegrate" to "分解",
        "AdElectricity" to "电",
        "AdStrDrain" to "drain str",
        "AdAcid" to "酸",
        "AdSpecial1" to "special1",
        "AdSpecial2" to "special2",
        "AdBlind" to "致盲",
        "AdStun" to "stun",
        "AdSlow" to "slow",
        "AdParalyse" to "麻痹",
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
        "AdDisease" to "疾病",
        "AdRot" to "rot",
        "AdSex" to "sex",
        "AdHallucination" to "幻觉",
        "AdDeath" to "Death",
        "AdPestilence" to "Pestilence",
        "AdFamine" to "Famine",
        "AdSlime" to "slime",
        "AdDisenchant" to "disenchant",
        "AdCorrode" to "corrosion",
        "AdClerical" to "clerical",
        "AdSpell" to "咒语",
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
        "AdPoison" to "毒",
        "AdWisdom" to "wisdom",
        "AdVorpal" to "斩首",
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
        "AdDisarm" to "缴械",
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

    private val monTranslation = mapOf(
        "giant ant" to "巨型蚂蚁",
        "killer bee" to "杀人蜂",
        "soldier ant" to "兵蚁",
        "fire ant" to "火蚁",
        "giant beetle" to "巨型甲虫",
        "queen bee" to "蜂王",
        "acid blob" to "酸滴",
        "quivering blob" to "颤抖的斑点",
        "gelatinous cube" to "黏胶立方怪",
        "chickatrice" to "小鸡蛇",
        "cockatrice" to "鸡蛇",
        "pyrolisk" to "火鸡蛇",
        "jackal" to "豺狼",
        "fox" to "狐狸",
        "coyote" to "土狼",
        "werejackal" to "豺狼人",
        "little dog" to "小狗",
        "dingo" to "澳洲野狗",
        "dog" to "狗",
        "large dog" to "大狗",
        "wolf" to "狼",
        "werewolf" to "狼人",
        "winter wolf cub" to "冬狼崽",
        "warg" to "座狼",
        "winter wolf" to "冬狼",
        "hell hound pup" to "地狱小猎犬",
        "hell hound" to "地狱猎犬",
        "Cerberus" to "刻耳柏洛斯",
        "gas spore" to "气体孢子",
        "floating eye" to "浮眼",
        "freezing sphere" to "冻结球",
        "flaming sphere" to "火焰球",
        "shocking sphere" to "电球",
        "kitten" to "小猫",
        "housecat" to "家猫",
        "jaguar" to "美洲豹",
        "lynx" to "猞猁",
        "panther" to "黑豹",
        "large cat" to "大猫",
        "tiger" to "老虎",
        "gremlin" to "小鬼",
        "gargoyle" to "石像鬼",
        "winged gargoyle" to "飞翼石像鬼",
        "hobbit" to "霍比特人",
        "dwarf" to "矮人",
        "bugbear" to "熊地精",
        "dwarf lord" to "矮人领主",
        "dwarf king" to "矮人王",
        "mind flayer" to "夺心魔",
        "master mind flayer" to "夺心魔大师",
        "manes" to "灵魂",
        "homunculus" to "雏形人",
        "imp" to "小恶魔",
        "lemure" to "劣魔",
        "quasit" to "夸塞魔",
        "tengu" to "天狗",
        "blue jelly" to "蓝色果冻",
        "spotted jelly" to "珍珠果冻",
        "ochre jelly" to "赭冻怪",
        "kobold" to "狗头人",
        "large kobold" to "大狗头人",
        "kobold lord" to "狗头人领主",
        "kobold shaman" to "狗头人萨满",
        "leprechaun" to "小矮妖",
        "small mimic" to "小拟型怪",
        "large mimic" to "大拟型怪",
        "giant mimic" to "巨型拟型怪",
        "wood nymph" to "木仙女",
        "water nymph" to "水仙女",
        "mountain nymph" to "山仙女",
        "goblin" to "地精",
        "hobgoblin" to "大地精",
        "orc" to "兽人",
        "hill orc" to "丘陵兽人",
        "Mordor orc" to "魔多兽人",
        "Uruk-hai" to "强兽人",
        "orc shaman" to "兽人萨满",
        "orc-captain" to "兽人队长",
        "rock piercer" to "岩石锥子",
        "iron piercer" to "铁锥子",
        "glass piercer" to "玻璃锥子",
        "rothe" to "洛斯兽",
        "mumak" to "猛犸",
        "leocrotta" to "狼狗",
        "wumpus" to "狮头象",
        "titanothere" to "雷兽",
        "baluchitherium" to "俾路支兽",
        "mastodon" to "乳齿象",
        "sewer rat" to "褐鼠",
        "giant rat" to "巨鼠",
        "rabid rat" to "狂鼠",
        "wererat" to "鼠人",
        "rock mole" to "岩石鼹鼠",
        "woodchuck" to "土拨鼠",
        "cave spider" to "洞穴蜘蛛",
        "centipede" to "蜈蚣",
        "giant spider" to "巨型蜘蛛",
        "scorpion" to "蝎子",
        "lurker above" to "潜伏者",
        "trapper" to "捕兽者",
        "pony" to "小马",
        "white unicorn" to "白色独角兽",
        "gray unicorn" to "灰色独角兽",
        "black unicorn" to "黑色独角兽",
        "horse" to "马",
        "warhorse" to "战马",
        "fog cloud" to "雾云",
        "dust vortex" to "尘埃旋涡",
        "ice vortex" to "冰旋涡",
        "energy vortex" to "能量旋涡",
        "steam vortex" to "蒸汽旋涡",
        "fire vortex" to "火旋涡",
        "baby long worm" to "幼长蠕虫",
        "baby purple worm" to "幼紫蠕虫",
        "long worm" to "长蠕虫",
        "purple worm" to "紫蠕虫",
        "grid bug" to "电子虫",
        "xan" to "玄蚊",
        "yellow light" to "黄光",
        "black light" to "黑光",
        "zruty" to "山区巨人",
        "couatl" to "羽蛇",
        "Aleax" to "亚历克斯",
        "Angel" to "天使",
        "ki-rin" to "麒麟",
        "Archon" to "执政官",
        "bat" to "蝙蝠",
        "giant bat" to "巨蝙蝠",
        "raven" to "乌鸦",
        "vampire bat" to "吸血蝙蝠",
        "plains centaur" to "平原半人马",
        "forest centaur" to "森林半人马",
        "mountain centaur" to "山半人马",
        "baby gray dragon" to "幼灰龙",
        "baby silver dragon" to "幼银龙",
        "baby red dragon" to "幼红龙",
        "baby white dragon" to "幼白龙",
        "baby orange dragon" to "幼橙龙",
        "baby black dragon" to "幼黑龙",
        "baby blue dragon" to "幼蓝龙",
        "baby green dragon" to "幼绿龙",
        "baby yellow dragon" to "幼黄龙",
        "gray dragon" to "灰龙",
        "silver dragon" to "银龙",
        "red dragon" to "红龙",
        "white dragon" to "白龙",
        "orange dragon" to "橙龙",
        "black dragon" to "黑龙",
        "blue dragon" to "蓝龙",
        "green dragon" to "绿龙",
        "yellow dragon" to "黄龙",
        "stalker" to "潜行者",
        "air elemental" to "空气元素",
        "fire elemental" to "火元素",
        "earth elemental" to "土元素",
        "water elemental" to "水元素",
        "lichen" to "地衣",
        "brown mold" to "棕霉菌",
        "yellow mold" to "黄霉菌",
        "green mold" to "绿霉菌",
        "red mold" to "红霉菌",
        "shrieker" to "尖叫蕈",
        "violet fungus" to "紫真菌",
        "gnome" to "侏儒",
        "gnome lord" to "侏儒领主",
        "gnomish wizard" to "侏儒巫师",
        "gnome king" to "侏儒王",
        "giant" to "巨人",
        "stone giant" to "石头巨人",
        "hill giant" to "丘陵巨人",
        "fire giant" to "火巨人",
        "frost giant" to "霜巨人",
        "ettin" to "双头巨人",
        "storm giant" to "风暴巨人",
        "titan" to "提坦",
        "minotaur" to "弥诺陶洛斯",
        "jabberwock" to "颊脖龙",
        "Keystone Kop" to "吉斯通警察",
        "Kop Sergeant" to "警察中士",
        "Kop Lieutenant" to "警察中尉",
        "Kop Kaptain" to "警察上尉",
        "lich" to "巫妖",
        "demilich" to "半巫妖",
        "master lich" to "巫妖大师",
        "arch-lich" to "大巫妖",
        "kobold mummy" to "狗头人木乃伊",
        "gnome mummy" to "侏儒木乃伊",
        "orc mummy" to "兽人木乃伊",
        "dwarf mummy" to "矮人木乃伊",
        "elf mummy" to "精灵木乃伊",
        "human mummy" to "人类木乃伊",
        "ettin mummy" to "双头木乃伊",
        "giant mummy" to "巨人木乃伊",
        "red naga hatchling" to "红幼纳迦",
        "black naga hatchling" to "黑幼纳迦",
        "golden naga hatchling" to "金幼纳迦",
        "guardian naga hatchling" to "幼纳迦守卫",
        "red naga" to "红纳迦",
        "black naga" to "黑纳迦",
        "golden naga" to "金纳迦",
        "guardian naga" to "纳迦守卫",
        "ogre" to "食人魔",
        "ogre lord" to "食人魔领主",
        "ogre king" to "食人魔王",
        "gray ooze" to "灰泥怪",
        "brown pudding" to "棕色布丁",
        "green slime" to "绿色黏液",
        "black pudding" to "黑色布丁",
        "quantum mechanic" to "量子力学",
        "rust monster" to "锈怪",
        "disenchanter" to "解魔怪",
        "garter snake" to "束带蛇",
        "snake" to "蛇",
        "water moccasin" to "水蝮蛇",
        "python" to "巨蟒",
        "pit viper" to "响尾蛇",
        "cobra" to "眼镜蛇",
        "troll" to "巨魔",
        "ice troll" to "冰巨魔",
        "rock troll" to "岩石巨魔",
        "water troll" to "水巨魔",
        "Olog-hai" to "欧罗海",
        "umber hulk" to "土巨怪",
        "vampire" to "吸血鬼",
        "vampire lord" to "吸血鬼领主",
        "Vlad the Impaler" to "穿刺者弗拉德",
        "barrow wight" to "古墓尸妖",
        "wraith" to "幽灵",
        "Nazgul" to "戒灵",
        "xorn" to "索尔石怪",
        "monkey" to "猴子",
        "ape" to "猿",
        "owlbear" to "枭熊",
        "yeti" to "雪人",
        "carnivorous ape" to "食肉猿",
        "sasquatch" to "北美野人",
        "kobold zombie" to "狗头人僵尸",
        "gnome zombie" to "侏儒僵尸",
        "orc zombie" to "兽人僵尸",
        "dwarf zombie" to "矮人僵尸",
        "elf zombie" to "精灵僵尸",
        "human zombie" to "人类僵尸",
        "ettin zombie" to "双头僵尸",
        "ghoul" to "食尸鬼",
        "giant zombie" to "巨人僵尸",
        "skeleton" to "骷髅",
        "straw golem" to "稻草魔像",
        "paper golem" to "纸魔像",
        "rope golem" to "绳子魔像",
        "gold golem" to "金魔像",
        "leather golem" to "皮革魔像",
        "wood golem" to "木魔像",
        "flesh golem" to "肉魔像",
        "clay golem" to "土魔像",
        "stone golem" to "石魔像",
        "glass golem" to "玻璃魔像",
        "iron golem" to "铁魔像",
        "human" to "人",
        "wererat" to "鼠人",
        "werejackal" to "豺狼人",
        "werewolf" to "狼人",
        "elf" to "精灵",
        "Woodland-elf" to "伍德兰精灵",
        "Green-elf" to "绿精灵",
        "Grey-elf" to "灰精灵",
        "elf-lord" to "精灵领主",
        "Elvenking" to "精灵王",
        "doppelganger" to "变形怪",
        "shopkeeper" to "店主",
        "guard" to "警卫",
        "prisoner" to "囚犯",
        "Oracle" to "神谕",
        "aligned priest" to "虔诚的牧师",
        "high priest" to "高级祭司",
        "soldier" to "士兵",
        "sergeant" to "中士",
        "nurse" to "护士",
        "lieutenant" to "中尉",
        "captain" to "上尉",
        "watchman" to "警卫员",
        "watch captain" to "警卫员队长",
        "Medusa" to "美杜莎",
        "Wizard of Yendor" to "岩德巫师",
        "Croesus" to "克罗伊斯",
        "Charon" to "卡隆",
        "ghost" to "鬼魂",
        "shade" to "魂灵",
        "water demon" to "水妖",
        "succubus" to "魅魔",
        "horned devil" to "有角的魔鬼",
        "incubus" to "梦魇",
        "erinys" to "伊里逆丝",
        "barbed devil" to "哈玛魔",
        "marilith" to "六臂蛇魔",
        "vrock" to "弗洛魔",
        "hezrou" to "狂战魔",
        "bone devil" to "骨魔",
        "ice devil" to "冰魔",
        "nalfeshnee" to "判魂魔",
        "pit fiend" to "深渊恶魔",
        "sandestin" to "桑德斯廷",
        "balrog" to "炎魔",
        "Juiblex" to "朱比烈斯",
        "Yeenoghu" to "伊诺胡",
        "Orcus" to "奥迦斯",
        "Geryon" to "吉里昂",
        "Dispater" to "迪斯帕特",
        "Baalzebub" to "巴力西卜",
        "Asmodeus" to "阿斯莫德",
        "Demogorgon" to "狄摩高根",
        "Death" to "死亡",
        "Pestilence" to "瘟疫",
        "Famine" to "饥荒",
        "mail daemon" to "邮件幽灵程序",
        "djinni" to "灯神",
        "jellyfish" to "水母",
        "piranha" to "水虎鱼",
        "shark" to "鲨鱼",
        "giant eel" to "巨型鳗鱼",
        "electric eel" to "电鳗",
        "kraken" to "海妖",
        "newt" to "蝾螈",
        "gecko" to "壁虎",
        "iguana" to "鬣蜥",
        "baby crocodile" to "幼鳄鱼",
        "lizard" to "蜥蜴",
        "chameleon" to "变色龙",
        "crocodile" to "鳄鱼",
        "salamander" to "火蜥蜴",
        "long worm tail" to "长蠕虫尾",
        "archeologist" to "考古学家",
        "barbarian" to "野蛮人",
        "caveman" to "穴居人",
        "cavewoman" to "女穴居人",
        "healer" to "医生",
        "knight" to "骑士",
        "monk" to "僧侣",
        "priest" to "牧师",
        "priestess" to "女牧师",
        "ranger" to "游侠",
        "rogue" to "盗贼",
        "samurai" to "武士",
        "tourist" to "游客",
        "valkyrie" to "女武神",
        "wizard" to "巫师",
        "Lord Carnarvon" to "卡那封勋爵",
        "Pelias" to "珀利阿斯",
        "Shaman Karnov" to "萨满卡诺夫",
        "Hippocrates" to "希波克拉底",
        "King Arthur" to "亚瑟王",
        "Grand Master" to "宗师",
        "Arch Priest" to "大祭司",
        "Orion" to "俄里翁",
        "Master of Thieves" to "盗贼大师",
        "Lord Sato" to "萨托领主",
        "Twoflower" to "双花",
        "Norn" to "诺恩",
        "Neferet the Green" to "绿衣娜菲利特",
        "Minion of Huhetotl" to "修堤库特里的奴才",
        "Thoth Amon" to "图特阿蒙",
        "Chromatic Dragon" to "彩色龙",
        "Cyclops" to "独眼巨人",
        "Ixoth" to "恶龙",
        "Master Kaen" to "凯恩大师",
        "Nalzok" to "纳宗魔",
        "Scorpius" to "蝎弩",
        "Master Assassin" to "刺客大师",
        "Ashikaga Takauji" to "足利尊氏",
        "Lord Surtur" to "叙尔特领主",
        "Dark One" to "黑暗魔君",
        "student" to "学者",
        "chieftain" to "酋长",
        "neanderthal" to "尼安德特人",
        "attendant" to "护理者",
        "page" to "实习骑士",
        "abbot" to "方丈",
        "acolyte" to "侍祭",
        "hunter" to "猎人",
        "thug" to "刺客",
        "ninja" to "忍者",
        "roshi" to "禅师",
        "guide" to "导游",
        "warrior" to "战士",
        "apprentice" to "魔法学徒"
    )

    //monster database
    private val db = dataFolder
        .walk()
        .filter { it.isFile }
        .map {
            Yaml.default.parseToYamlNode(it.inputStream())
        }
        .toSet()

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
        }
    }

    private suspend fun GroupMessageEvent.assistDragonBOT() {
        if (message.content.startsWith("查询怪物") && this.message.content.length > 4) {
            val monName = message.content.removePrefix("查询怪物")
            val monEnName = monTranslation.entries.find { it.value == monName }?.key
            if (monEnName != null) {
                val im = genWikiImage(monEnName)
                val upIm = im.uploadAsImage(group, "png")
                group.sendMessage(PlainText("$monName:\n") + upIm)
                withContext(Dispatchers.IO) {
                    im.close()
                }
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

                if (count > 0){
                    group.sendMessage(res.removeSuffix("\n"))
                }
            }
        }
    }

    private suspend fun GroupMessageEvent.monNameTranslation() {
        if (message.content.startsWith("翻译:") && this.message.content.length > 2) {
            var fMsg = message.content.removePrefix("翻译:")
            monTranslation.forEach { (k, v) -> fMsg = fMsg.replace(k, v) }
            group.sendMessage(fMsg)
        }
    }

    private suspend fun GroupMessageEvent.monQuery() {
        if (!this.message.content.startsWith("#")
            || !this.message.content.contains("?")
            || this.message.content.length <= 2
        ) {
            return
        }

        //split out variant prefix and monster name
        val variant = this.message.content.run { subSequence(1, indexOf("?")) }
        val monName = this.message.content.removePrefix("#$variant?")
        logger.debug { "var:$variant           monName:$monName" }

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

        logger.debug { "res:$res" }

        //submit result
        if (res.isNotBlank()) {
            //generate the image of monster symbol
            val im = genSymImage(sym, color)
            val upIm = im.uploadAsImage(group, "png")
            withContext(Dispatchers.IO) {
                im.close()
            }

            try {
                val im2 = genWikiImage(name)
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

    private fun genSymImage(sym: String, color: String): InputStream {
        val image = BufferedImage(13, 22, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        val colors = mapOf(
            "black" to Color.decode("#000000"),
            "blue" to Color.decode("#0000AA"),
            "green" to Color.decode("#00AA00"),
            "cyan" to Color.decode("#00AAAA"),
            "red" to Color.decode("#AA0000"),
            "magenta" to Color.decode("#AA00AA"),
            "brown" to Color.decode("#AA5500"),
            "white" to Color.decode("#AAAAAA"),
            "gray" to Color.decode("#555555"),
            "brightblue" to Color.decode("#5555FF"),
            "brightgreen" to Color.decode("#55FF55"),
            "brightcyan" to Color.decode("#55FFFF"),
            "brightred" to Color.decode("#FF5555"),
            "brightmagenta" to Color.decode("#FF55FF"),
            "yellow" to Color.decode("#FFFF55"),
            "brightwhite" to Color.decode("#FFFFFF"),
        )
        //get default color set
        g.color = colors[color.lowercase()] ?: Color.white

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

    private fun genWikiImage(monName: String): InputStream {
        val doc = Jsoup.connect("https://nethackwiki.com/wiki/File:${monName.replace(" ", "_")}.png").get()
        val link = "https://nethackwiki.com" + doc.select("#file > a").attr("href")
        return Jsoup.connect(link).ignoreContentType(true).maxBodySize(3000000).ignoreHttpErrors(true).execute()
            .bodyStream()
    }
}