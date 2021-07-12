package mods.betterfoliage.config

import mods.betterfoliage.config.match.Node
import mods.betterfoliage.config.match.parser.BlockConfigParser
import mods.betterfoliage.config.match.parser.ParseException
import mods.betterfoliage.config.match.parser.TokenMgrError
import mods.betterfoliage.util.HasLogger
import mods.betterfoliage.util.stripStart
import net.minecraft.resources.IResourceManager
import net.minecraft.util.ResourceLocation
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Level.ERROR

class BlockConfig : HasLogger() {
    lateinit var rules: List<Node.MatchAll>

    fun readConfig(manager: IResourceManager) {
        val configs = manager.listResources("config/betterfoliage") { it.endsWith(".rules") }
        rules = configs.flatMap { configLocation ->
            val resource = manager.getResource(configLocation)
            val parser = BlockConfigParser(resource.inputStream)
                .apply { configFile = configLocation.stripStart("config/betterfoliage/").toString() }
            try {
                mutableListOf<Node.MatchAll>().apply { parser.matchFile(this) }
            } catch (e: ParseException) {
                parseError(e, configLocation)
            } catch (e: TokenMgrError) {
                parseError(e, configLocation)
            }
        }
    }

    fun parseError(e: Throwable, location: ResourceLocation): List<Node.MatchAll> {
        "Error parsing block config $location: ${e.message}".let {
            logger.log(ERROR, it)
            detailLogger.log(ERROR, it)
        }
        return emptyList()
    }
}
