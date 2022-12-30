plugins {
    val kotlinVersion = "1.6.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.13.2"
}


dependencies {
    implementation("com.charleskorn.kaml:kaml:0.49.0")
    implementation("org.jsoup:jsoup:1.15.3")
    "shadowLink"("org.jsoup:jsoup")
    "shadowLink"("com.charleskorn.kaml:kaml")
}



group = "me.uncookie"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.aliyun.com/repository/public")
}

