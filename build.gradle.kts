plugins {
    id ("fabric-loom")
    id ("maven-publish")
    id ("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose") version "0.3.0-build140"
}

fun prop(str: String): String = project.property(str).toString()

base {
    archivesBaseName = prop("archives_base_name")
}
version = prop("mod_version")
group = prop("maven_group")

minecraft {
}


repositories {
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    jcenter()
    maven (
        url = "https://jitpack.io"
    ){
        metadataSources {
            mavenPom()
            artifact()
        }
        content {
            includeGroupByRegex ("com.github.Chocohead")
        }
    }
}

dependencies {
    //to change the versions see the gradle.properties file
    minecraft ("com.mojang:minecraft:1.16.5")
    mappings ("net.fabricmc:yarn:${prop("yarn_mappings")}:v2")
    modImplementation ("net.fabricmc:fabric-loader:${prop("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation ("net.fabricmc.fabric-api:fabric-api:${prop("fabric_version")}"){
//        exclude()
    }

    modImplementation ("net.fabricmc:fabric-language-kotlin:${prop("fabric_kotlin_version")}"){
        exclude (group = "net.fabricmc.fabric-api")
    }

//    modImplementation("com.lettuce.fudge:fabric-ktx:$fabric_ktx_version"){
//        exclude group : "net.fabricmc.fabric-api"
//    }
    modRuntime ("com.github.Chocohead:Data-Breaker-Lower:24be1a2"){
        exclude (module = "fabric-loader")
        exclude (group = "net.fabricmc.fabric-api")
    }

    implementation(compose.runtime)


    // PSA: Some older mods, compiled on Loom 0.2.1, might have outdated Maven POMs.
    // You may need to force-disable transitiveness on them.
}



