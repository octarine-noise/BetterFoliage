package mods.betterfoliage.client.integration;

import mods.betterfoliage.loader.AbstractResolvable;

public abstract class AbstractModIntegration {

	protected static boolean isSomeAvailable(Iterable<AbstractResolvable<?>> elements) {
		for (AbstractResolvable<?> element : elements) if (element.resolve() != null) return true;
		return false;
	}
	
	protected static boolean isAllAvailable(Iterable<AbstractResolvable<?>> elements) {
		for (AbstractResolvable<?> element : elements) if (element.resolve() == null) return false;
		return true;
	}
}
