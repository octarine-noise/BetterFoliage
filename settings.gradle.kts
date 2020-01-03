pluginManagement {
    repositories {
        maven("http://files.minecraftforge.net/maven")
        maven("https://repo.spongepowered.org/maven")
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.let { it.namespace == "net.minecraftforge" && it.name == "gradle"} ) useModule("net.minecraftforge.gradle:ForgeGradle:${requested.version}")
            if (requested.id.let { it.namespace == "org.spongepowered" && it.name == "mixin"} ) useModule("org.spongepowered:mixingradle:${requested.version}")
        }
    }
}
