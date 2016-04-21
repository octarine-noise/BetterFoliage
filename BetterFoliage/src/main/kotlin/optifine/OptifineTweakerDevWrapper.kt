package optifine

import mods.octarinecore.tryDefault
import net.minecraft.launchwrapper.IClassTransformer
import net.minecraft.launchwrapper.ITweaker
import net.minecraft.launchwrapper.LaunchClassLoader
import java.io.File
import java.net.URLClassLoader
import java.util.zip.ZipFile

class OptifineTweakerDevWrapper : ITweaker {
    override fun acceptOptions(p0: MutableList<String>?, p1: File?, p2: File?, p3: String?) { }
    override fun getLaunchArguments(): Array<out String>? = Array<String>(0) {""}
    override fun getLaunchTarget() = "net.minecraft.client.main.Main"
    override fun injectIntoClassLoader(classLoader: LaunchClassLoader) {
        classLoader.registerTransformer("optifine.OptifineTransformerDevWrapper")
    }
}

/**
 * Replacement for OptiFine's class transformer. Implements the pre-1.8.x-H5 way of operation.
 *
 * This class is only used in development to debug cross-mod issues with Optifine, and
 * is not part of the release!
 */
class OptifineTransformerDevWrapper : IClassTransformer {

    val ofZip = (this.javaClass.classLoader as? URLClassLoader)?.urLs?.find {
        val zipFile = tryDefault(null) { ZipFile(File(it.toURI())) }
        zipFile?.getEntry("optifine/OptiFineClassTransformer.class") != null
    }?.let { ZipFile(File(it.toURI())) }

    /**
     * Load replacement classes from the OptiFine Jar.
     */
    override fun transform(name: String?, transformedName: String?, classData: ByteArray?) =
        ofZip?.getEntry(name?.replace(".", "/") + ".class")?.let { ofZip.getInputStream(it).readBytes() } ?: classData
}
