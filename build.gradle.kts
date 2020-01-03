plugins {
    kotlin("jvm").version("1.3.61")
    id("net.minecraftforge.gradle").version("3.0.157")
    id("org.spongepowered.mixin").version("0.7-SNAPSHOT")
}

repositories {
    maven("http://files.minecraftforge.net/maven")
    maven("https://repo.spongepowered.org/maven")
    maven("https://minecraft.curseforge.com/api/maven")
}

dependencies {
    "minecraft"("net.minecraftforge:forge:${properties["mcVersion"]}-${properties["forgeVersion"]}")

    "implementation"("kottle:Kottle:${properties["kottleVersion"]}")
    "implementation"("org.spongepowered:mixin:0.8-SNAPSHOT")
}

sourceSets {
    get("main").resources.srcDir("src/forge/resources")
    get("main").java.srcDir("src/forge/java")
}
kotlin.sourceSets {
    get("main").kotlin.srcDir("src/forge/kotlin")
}

minecraft {
    mappings(properties["mappingsChannel"] as String, properties["mappingsVersion"] as String)
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    runs.create("client") {
        workingDirectory(file("run"))
        properties["forge.logging.markers"] = "CORE"
        properties["forge.logging.console.level"] = "debug"
        mods.create("betterfoliage") {
            source(sourceSets["main"])
        }
    }
}

mixin {
    add(sourceSets["main"], "betterfoliage.refmap.json")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    target.compilations.configureEach {
        kotlinOptions.freeCompilerArgs += listOf("-Xno-param-assertions", "-Xno-call-assertions")
    }
}

tasks.getByName<Jar>("jar") {
    archiveName = "BetterFoliage-${project.version}-Forge-${properties["mcVersion"]}.jar"
    manifest {
        from(file("src/main/resources/META-INF/MANIFEST.MF"))
        attributes["Implementation-Version"] = project.version
    }
}