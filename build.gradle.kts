plugins {
    kotlin("jvm").version("1.4.20")
    id("net.minecraftforge.gradle").version("4.1.12")
    id("org.spongepowered.mixin").version("0.7-SNAPSHOT")
}
//apply(plugin = "org.spongepowered.mixin")

repositories {
    maven("https://files.minecraftforge.net/maven")
    maven("https://repo.spongepowered.org/maven")
    maven("https://minecraft.curseforge.com/api/maven")
    maven("https://maven.shedaniel.me/")
    maven("https://www.cursemaven.com")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
}

dependencies {
    "minecraft"("net.minecraftforge:forge:${properties["mcVersion"]}-${properties["forgeVersion"]}")

//    "api"(fg.deobf("curse.maven:clothconfig-348521:2938583"))

//    "implementation"(fg.deobf("curse.maven:biomesoplenty-220318:2988999"))
    "implementation"("thedarkcolour:kotlinforforge:1.7.0")

    "implementation"("org.spongepowered:mixin:0.8-SNAPSHOT")
}

configurations["annotationProcessor"].extendsFrom(configurations["implementation"])
sourceSets {
    get("main").ext["refMap"] = "betterfoliage.refmap.json"
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

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    target.compilations.configureEach {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += listOf("-Xno-param-assertions", "-Xno-call-assertions")

    }
}

tasks.getByName<Jar>("jar") {
    archiveName = "BetterFoliage-${project.version}-Forge-${properties["mcVersion"]}.jar"
    manifest {
        from(file("src/main/resources/META-INF/MANIFEST.MF"))
        attributes["Implementation-Version"] = project.version
    }
    exclude("net")
    filesMatching("META-INF/mods.toml") { expand(project.properties) }
    filesMatching("mcmod.info") { expand(project.properties) }
}