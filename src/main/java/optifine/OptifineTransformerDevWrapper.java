package optifine;

import net.minecraft.launchwrapper.IClassTransformer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class OptifineTransformerDevWrapper implements IClassTransformer {

    public static String OPTIFINE_CLASSNAME = "optifine/OptiFineClassTransformer.class";
    private ZipFile ofZip = null;

    public  OptifineTransformerDevWrapper() {
        Stream<URL> loaderSources = Arrays.stream(((URLClassLoader) getClass().getClassLoader()).getURLs());
        Optional<URL> optifineURL = loaderSources.filter(this::isOptifineJar).findFirst();
        optifineURL.ifPresent(url -> ofZip = getZip(url));
    }

    private ZipFile getZip(URL url) {
        try {
            return new ZipFile(new File(url.toURI()));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isOptifineJar(URL url) {
        ZipFile zip = getZip(url);
        return zip != null && zip.getEntry(OPTIFINE_CLASSNAME) != null;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (ofZip == null) return basicClass;
        ZipEntry replacement = ofZip.getEntry(name.replace(".", "/") + ".class");
        if (replacement == null) return basicClass;

        try {
            return readAll(ofZip.getInputStream(replacement));
        } catch (IOException e) {
            return basicClass;
        }
    }

    private byte[] readAll(InputStream is) throws IOException {
        byte[] buf = new byte[4096];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len;
        do {
            len = is.read(buf, 0, 4096);
            if (len > 0) bos.write(buf, 0, len);
        } while (len > -1);
        is.close();
        return bos.toByteArray();
    }
}
