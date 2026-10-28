// 每个版本子项目（versions/<版本>）都会用这份脚本构建，stonecutter.current.version 是当前目标版本。
plugins {
    id("fabric-loom") version "1.19.3"
}

val mcVersion = stonecutter.current.version
val javaRelease = (property("java_release") as String).toInt()

version = "${property("mod_version")}+$mcVersion"
group = property("maven_group") as String
base { archivesName = property("archives_base_name") as String }

repositories { mavenCentral() }

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    if (mcVersion == "26.3") {
        // 26.1+ 起游戏自带官方名、不再混淆：没有映射文件，也不需要重映射，
        // 依赖按 Fabric 迁移指南用普通 implementation（versions/26.3 里已开 disableObfuscation）。
        add("implementation", "net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
        add("implementation", "net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    } else {
        add("mappings", "net.fabricmc:yarn:${property("yarn_mappings")}:v2")
        add("modImplementation", "net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
        add("modImplementation", "net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    }
}

val mcDependency = property("mc_dependency") as String
val javaMajor = property("java_major") as String

tasks.named<ProcessResources>("processResources") {
    inputs.property("version", project.version)
    filteringCharset = "UTF-8"
    // 只做占位符替换。不要用 Gradle expand：它会把 JSON 字符串里的 \n 解释成真实换行，
    // 把 fabric.mod.json 弄坏；另外 description 里的中文必须按 UTF-8 读写。
    filesMatching("fabric.mod.json") {
        filter { line -> line
            .replace("\${version}", project.version.toString())
            .replace("\${mc_dependency}", mcDependency)
            .replace("\${java_major}", javaMajor)
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = javaRelease
}

java {
    // 不固定 toolchain：编译用 options.release 切 17/21，运行构建的 JVM 由环境提供（>=21 即可）。
    sourceCompatibility = JavaVersion.toVersion(javaRelease)
    targetCompatibility = JavaVersion.toVersion(javaRelease)
    withSourcesJar()
}
