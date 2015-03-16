package mods.betterfoliage.client.integration;

import mods.betterfoliage.loader.IResolvable;

public abstract class AbstractModIntegration {

	protected static boolean isSomeAvailable(Iterable<IResolvable<?>> elements) {
		for (IResolvable<?> element : elements) if (element.resolve() != null) return true;
		return false;
	}
	
	protected static boolean isAllAvailable(Iterable<IResolvable<?>> elements) {
		for (IResolvable<?> element : elements) if (element.resolve() == null) return false;
		return true;
	}
}
