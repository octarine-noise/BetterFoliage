package mods.betterfoliage.loader;

public abstract class AbstractResolvable<T> {

	public T resolvedObj;
	
	public boolean isResolved = false;

	public T resolve() {
		if (!isResolved) {
			resolvedObj = resolveInternal();
			isResolved = true;
		}
		return resolvedObj;
	}
	
	protected abstract T resolveInternal();
}
