package com.adeven.adjustio;

import java.util.HashMap;
import java.util.Map;

public class AdjustIoFactory {
	private static Map<Class,Object> classMap = new HashMap<Class, Object>();
	private static Boolean locked = false;
	
	public static void registerType(Class classType, Object object){
		if (!locked) {
			classMap.put(classType, object);
		}
	}
	
	public static Object getInstance(Class classType) {
		return classMap.get(classType);
	}
	
	public static void setLocked(Boolean locked) {
		AdjustIoFactory.locked = locked;
	}
}
