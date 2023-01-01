plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.13.2"
}


dependencies {
    implementation("com.charleskorn.kaml:kaml:0.49.0")
    "shadowLink"("com.charleskorn.kaml:kaml")
    implementation("org.jsoup:jsoup:1.15.3")
    "shadowLink"("org.jsoup:jsoup")
    implementation("org.jetbrains.lets-plot:lets-plot-common:3.0.0")
    "shadowLink"("org.jetbrains.lets-plot:lets-plot-common")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:4.2.0")
    "shadowLink"("org.jetbrains.lets-plot:lets-plot-kotlin-jvm")
    implementation("org.apache.xmlgraphics:batik-transcoder:1.16")
    "shadowLink"("org.apache.xmlgraphics:batik-transcoder")
    implementation("org.apache.xmlgraphics:batik-codec:1.16")
    "shadowLink"("org.apache.xmlgraphics:batik-codec")
}



group = "me.uncookie"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.aliyun.com/repository/public")
}

