// Stonecutter 多版本模板：拷到仓库根目录并改名为 settings.gradle.kts 后生效。
// 版本号请以 https://github.com/kikugie/stonecutter 的当前发布为准。
pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.6.1"
}

stonecutter {
    create(rootProject) {
        versions("1.19.4", "1.20.1", "1.21.11", "26.3")
        vcsVersion = "1.19.4"
    }
}

rootProject.name = "deeprebuild"
