import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  val kotlinVersion: String by System.getProperties()
  kotlin("jvm").version(kotlinVersion)

  id("fabric-loom") version "1.7-SNAPSHOT"
  id("maven-publish")
  id("signing")
}

val modVersion: String by project
val mavenGroup: String by project

val minecraftVersion: String by project
val minecraftTargetVersion: String by project
val yarnMappings: String by project
val loaderVersion: String by project
val fabricKotlinVersion: String by project
val fabricVersion: String by project

val ccVersion: String by project
val ccMcVersion: String by project
val ccTargetVersion: String by project

val cache2kVersion: String by project
val prometheusVersion: String by project

val nightConfigVersion: String by project
val clothConfigVersion: String by project
val clothApiVersion: String by project
val modMenuVersion: String by project

val scLibraryVersion: String by project

val archivesBaseName = "sc-peripherals"
version = modVersion
group = mavenGroup

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = "21"
    apiVersion = "1.9"
    languageVersion = "1.9"
  }
}

repositories {
  mavenLocal {
    content {
      includeModule("io.sc3", "sc-library")
    }
  }

  maven {
    url = uri("https://maven.reconnected.cc/releases")
    content {
      includeGroup("io.sc3")
    }
  }

  maven("https://maven.squiddev.cc") {
    content {
      includeGroup("cc.tweaked")
      includeModule("org.squiddev", "Cobalt")
    }
  }

  maven("https://maven.shedaniel.me") {
    // cloth-config
    content {
      includeGroup("me.shedaniel.cloth")
      includeGroup("me.shedaniel.cloth.api")
    }
  }

  maven("https://maven.terraformersmc.com") {
    // mod-menu
    content {
      includeGroup("com.terraformersmc")
    }
  }
}

dependencies {
  minecraft("com.mojang", "minecraft", minecraftVersion)
  mappings("net.fabricmc", "yarn", yarnMappings, null, "v2")
  modImplementation("net.fabricmc", "fabric-loader", loaderVersion)
  modImplementation("net.fabricmc.fabric-api", "fabric-api", fabricVersion) {
    exclude("net.fabricmc.fabric-api", "fabric-gametest-api-v1")
  }
  modImplementation("net.fabricmc", "fabric-language-kotlin", fabricKotlinVersion)

  modImplementation(include("io.sc3", "sc-library", scLibraryVersion))

  // CC: Tweaked
  modApi("cc.tweaked:cc-tweaked-$ccMcVersion-fabric:$ccVersion") {
    exclude("net.fabricmc.fabric-api", "fabric-gametest-api-v1")
  }

  implementation(include("com.electronwill.night-config", "core", nightConfigVersion))
  implementation(include("com.electronwill.night-config", "toml", nightConfigVersion))

  modApi("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
    exclude("net.fabricmc.fabric-api")
  }
  include("me.shedaniel.cloth", "cloth-config-fabric", clothConfigVersion)
  modImplementation(include("me.shedaniel.cloth.api", "cloth-utils-v1", clothApiVersion))

  modImplementation(include("com.terraformersmc", "modmenu", modMenuVersion))

  implementation(include("org.cache2k", "cache2k-api", cache2kVersion))
  implementation(include("org.cache2k", "cache2k-core", cache2kVersion))
  implementation(include("io.prometheus", "simpleclient", prometheusVersion))
  implementation(include("io.prometheus", "simpleclient_hotspot", prometheusVersion))
  implementation(include("io.prometheus", "simpleclient_httpserver", prometheusVersion))
}

tasks {
  processResources {
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") { expand(mutableMapOf(
      "version" to project.version,
      "minecraft_target_version" to minecraftTargetVersion,
      "fabric_kotlin_version" to fabricKotlinVersion,
      "loader_version" to loaderVersion,
      "cc_target_version" to ccTargetVersion,
    )) }
  }

  jar {
    from("LICENSE") {
      rename { "${it}_${archivesBaseName}" }
    }

    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
  }

  remapJar {
    exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
    destinationDirectory.set(file("${rootDir}/build/final"))
  }

  loom {
    accessWidenerPath.set(file("src/main/resources/sc-peripherals.accesswidener"))

    sourceSets {
      main {
        resources {
          srcDir("src/generated/resources")
          exclude("src/generated/resources/.cache")
        }
      }
    }

    runs {
      configureEach {
        property("fabric.debug.disableModShuffle")
//        vmArgs("-XX:+AllowEnhancedClassRedefinition")
      }
      create("datagen") {
        client()
        name("Data Generation")
        vmArgs(
          "-Dfabric-api.datagen",
          "-Dfabric-api.datagen.output-dir=${file("src/generated/resources")}",
          "-Dfabric-api.datagen.modid=${archivesBaseName}"
        )
        runDir("build/datagen")
      }
    }
  }
}

publishing {
  publications {
    register("mavenJava", MavenPublication::class) {
      from(components["java"])
    }
  }

  repositories {
    maven {
      name = "reconnectedRepo"
      url = uri("https://maven.reconnected.cc/releases")

      if (!System.getenv("MAVEN_USERNAME").isNullOrEmpty()) {
        credentials {
          username = System.getenv("MAVEN_USERNAME")
          password = System.getenv("MAVEN_PASSWORD")
        }
      } else {
        credentials(PasswordCredentials::class)
      }

      authentication {
        create<BasicAuthentication>("basic")
      }
    }
  }
}

