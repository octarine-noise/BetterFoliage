package optifine

import net.minecraft.launchwrapper.IClassTransformer
import net.minecraft.launchwrapper.ITweaker
import net.minecraft.launchwrapper.LaunchClassLoader
import java.io.File

class OptifineTweakerDevWrapper : ITweaker {
    override fun acceptOptions(p0: MutableList<String>?, p1: File?, p2: File?, p3: String?) { }
    override fun getLaunchArguments(): Array<out String>? = Array<String>(0) {""}
    override fun getLaunchTarget() = "net.minecraft.client.main.Main"
    override fun injectIntoClassLoader(classLoader: LaunchClassLoader) {
        classLoader.registerTransformer("optifine.OptifineTransformerDevWrapper")
    }
}

/**
 * Wrapper around Optifine's class transformer.
 *
 * This class is only used in development to debug cross-mod issues with Optifine, and
 * is not part of the release!
 */
class OptifineTransformerDevWrapper : IClassTransformer {

    val ofTransformer = Class.forName("optifine.OptiFineClassTransformer").newInstance() as IClassTransformer

    /**
     * Call the Optifine transformer, but change dots to slashes in class names.
     * This enables the Optifine transformer to load replacements from non-root locations in the jar file.
     */
    override fun transform(name: String?, transformedName: String?, classData: ByteArray?) =
        ofTransformer.transform(name?.replace(".", "/"), transformedName, classData)
}
