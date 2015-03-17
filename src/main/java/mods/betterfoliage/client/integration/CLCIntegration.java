package mods.betterfoliage.client.integration;

import mods.betterfoliage.BetterFoliage;
import mods.betterfoliage.client.render.RenderBlockAOBase.ShadingValues;
import mods.betterfoliage.loader.impl.CodeRefs;

/** Helper methods for dealing with Colored Lights Core.
 * @author octarine-noise
 */
public class CLCIntegration {

	public static boolean isPresent = false;
	
	/** Hide constructor */
    private CLCIntegration() {}
    
    public static void init() {
    	if (CodeRefs.cCLCLoader.resolve() != null) {
    		isPresent = true;
        	BetterFoliage.log.info("Found Colored Lights Core - setting up compatibility");
    	}
    }
    
    public static ShadingValues avgShading(ShadingValues shading1, ShadingValues shading2) {
    	if (shading1 == null || shading2 == null) return null;
    	
	    ShadingValues result = new ShadingValues();
	    result.red = (shading1.red + shading2.red) * 0.5f;
	    result.green = (shading1.green + shading2.green) * 0.5f;
	    result.blue = (shading1.blue + shading2.blue) * 0.5f;
	    result.brightness = isPresent ? avgBrightnessCLC(shading1.brightness, shading2.brightness) : (shading1.brightness + shading2.brightness) / 2;
	    return result;
    }
    
    public static int avgBrightnessCLC(int brightness1, int brightness2) {
    	int light1 = (brightness1 >> 4) & 15;
    	int red1 = (brightness1 >> 8) & 15;
    	int green1 = (brightness1 >> 12) & 15;
    	int blue1 = (brightness1 >> 16) & 15;
    	int sky1 = (brightness1 >> 20) & 15;
    	
    	int light2 = (brightness2 >> 4) & 15;
    	int red2 = (brightness2 >> 8) & 15;
    	int green2 = (brightness2 >> 12) & 15;
    	int blue2 = (brightness2 >> 16) & 15;
    	int sky2 = (brightness2 >> 20) & 15;
    	
    	int lightA = (light1 + light2) / 2;
    	int redA = (red1 + red2) / 2;
    	int greenA = (green1 + green2) / 2;
    	int blueA = (blue1 + blue2) / 2;
    	int skyA = (sky1 + sky2) / 2;
    	
    	return (lightA << 4) | (redA << 8) | (greenA << 12) | (blueA << 16) | (skyA << 20);
    }
}
