package com.ironsource.mobilcore;

import java.util.LinkedHashMap;
import java.util.Map;

class LruCache<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = 1L;
	private int size;

	public LruCache(int size) {
		super(size, (float) 0.75, true);
		this.size = size;
	}

	public static <K, V> LruCache<K, V> newInstance(int size) {
		return new LruCache<K, V>(size);
	}

	public void setMaxSize(int size) {
		this.size = size;
	}

	@Override
	public V put(K key, V value) {
		synchronized (this) {
			if (get(key) == null) {
				return super.put(key, value);
			}
		}
		return null;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > size;
	}
}
