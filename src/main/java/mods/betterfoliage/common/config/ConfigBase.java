package mods.betterfoliage.common.config;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/** Config base class using annotations
 * @author octarine-noise
 */
public class ConfigBase {

	/** Annotates a field linked to a config file property
	 * @author octarine-noise
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface CfgElement {
		String category();
		String key();
		String comment() default "";
	}
	
	/** Declares a min/max limit on another field
	 * @author octarine-noise
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Limit {
		String min() default "";
		String max() default "";
	}
	
	protected Configuration config;
	
	public void load(File configFile) {
		config = new Configuration(configFile);
		config.load();
		
		for (Field field : getClass().getDeclaredFields()) {
			CfgElement annot = field.getAnnotation(CfgElement.class);
			if (annot == null) continue;
			
			field.setAccessible(true);
			if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
				try {
					Property prop = config.get(annot.category(), annot.key(), field.getBoolean(this));
					field.setBoolean(this, prop.getBoolean(field.getBoolean(this)));
				} catch (Exception e) {
				}
			} else if (field.getType().equals(OptionInteger.class)) {
				try {
					OptionInteger option = (OptionInteger) field.get(this);
					Property prop =  config.get(annot.category(), annot.key(), option.value);
					option.value = prop.getInt(option.value);
				} catch (Exception e) {
				}
			} else if (field.getType().equals(OptionDouble.class)) {
				try {
					OptionDouble option = (OptionDouble) field.get(this);
					Property prop =  config.get(annot.category(), annot.key(), option.value);
					option.value = prop.getDouble(option.value);
				} catch (Exception e) {
				}
			}
		}
		
		validateLimits();
		if (config.hasChanged()) config.save();
	}
	
	protected void validateLimits() {
		for (Field fieldThis : getClass().getDeclaredFields()) {
			Limit annot = fieldThis.getAnnotation(Limit.class);
			if (annot == null) continue;
			
			try {
				Field fieldMin = annot.min().isEmpty() ? null : getClass().getDeclaredField(annot.min());
				Field fieldMax = annot.max().isEmpty() ? null : getClass().getDeclaredField(annot.max());
				fieldThis.setAccessible(true);
				fieldMin.setAccessible(true);
				fieldMax.setAccessible(true);
				
				if (fieldThis.getType().equals(OptionInteger.class)) {
					OptionInteger optionThis = (OptionInteger) fieldThis.get(this);
					OptionInteger optionMin = fieldMin == null ? null : (OptionInteger) fieldMin.get(this);
					OptionInteger optionMax = fieldMax == null ? null : (OptionInteger) fieldMax.get(this);
					if (optionMin != null) optionThis.value = Math.max(optionThis.value, optionMin.value);
					if (optionMax != null) optionThis.value = Math.min(optionThis.value, optionMax.value);
				} else if (fieldThis.getType().equals(OptionDouble.class)) {
					OptionDouble optionThis = (OptionDouble) fieldThis.get(this);
					OptionDouble optionMin = fieldMin == null ? null : (OptionDouble) fieldMin.get(this);
					OptionDouble optionMax = fieldMax == null ? null : (OptionDouble) fieldMax.get(this);
					if (optionMin != null) optionThis.value = Math.max(optionThis.value, optionMin.value);
					if (optionMax != null) optionThis.value = Math.min(optionThis.value, optionMax.value);
				}
			} catch (Exception e) {}
		}
	}
	
	public void save() {
		for (Field field : getClass().getDeclaredFields()) {
			CfgElement annot = field.getAnnotation(CfgElement.class);
			if (annot == null) continue;
			
			field.setAccessible(true);
			if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
				try {
					Property prop = config.get(annot.category(), annot.key(), field.getBoolean(this));
					prop.set(field.getBoolean(this));
				} catch (Exception e) {
				}
			} else if (field.getType().equals(OptionInteger.class)) {
				try {
					OptionInteger option = (OptionInteger) field.get(this);
					Property prop =  config.get(annot.category(), annot.key(), option.value);
					prop.set(option.value);
				} catch (Exception e) {
				}
			} else if (field.getType().equals(OptionDouble.class)) {
				try {
					OptionDouble option = (OptionDouble) field.get(this);
					Property prop =  config.get(annot.category(), annot.key(), option.value);
					prop.set(option.value);
				} catch (Exception e) {
				}
			}
		}
		
		config.save();
	}
}
