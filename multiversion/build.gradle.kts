// Stonecutter 模板：拷到仓库根目录并改名为 build.gradle.kts（替换现有 build.gradle）。
// 每条 `stonecutter` 相关写法以 https://github.com/kikugie/stonecutter 的文档为准。
plugins {
    id("fabric-loom") version "1.10.5"
}

val mcVersion = stonecutter.current.version

version = "1.0.0+$mcVersion"
group = "cn.blockforge.generated"

base { archivesName = "deeprebuild-$mcVersion" }

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    // 1.19.4 / 1.20.1 / 1.21.11 用 Yarn；26.3 起官方已去混淆，用官方映射：
    // mappings(loom.officialMojangMappings())
    mappings("net.fabricmc:yarn:${property("yarn_mappings") as String}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version") as String}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version") as String}")
}

processResources {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"
    filesMatching("fabric.mod.json") {
        filter { line -> line.replace("\${version}", project.version.toString()) }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(property("java_version") as String == "21" ? 21 else 17)
    options.encoding = "UTF-8"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of((property("java_version") as String).toInt())
    withSourcesJar()
}
