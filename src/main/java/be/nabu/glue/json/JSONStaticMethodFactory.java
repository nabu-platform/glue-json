package be.nabu.glue.json;

import java.util.ArrayList;
import java.util.List;

import be.nabu.glue.core.api.StaticMethodFactory;

public class JSONStaticMethodFactory implements StaticMethodFactory {

	@Override
	public List<Class<?>> getStaticMethodClasses() {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		classes.add(JSONMethods.class);
		return classes;
	}

}
