package be.nabu.glue.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

import be.nabu.glue.ScriptRuntime;
import be.nabu.glue.impl.methods.ScriptMethods;
import be.nabu.libs.evaluator.annotations.MethodProviderClass;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.json.JSONBinding;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.types.map.MapTypeGenerator;

@MethodProviderClass(namespace = "json")
public class JSONMethods {
	
	@SuppressWarnings("rawtypes")
	public static String stringify(Object object) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ComplexContent content;
		if (object instanceof ComplexContent) {
			content = (ComplexContent) object;
		}
		else {
			content = new BeanInstance(object);
		}
		JSONBinding binding = new JSONBinding(content.getType(), ScriptRuntime.getRuntime().getScript().getCharset());
		binding.marshal(output, content);
		return new String(output.toByteArray(), ScriptRuntime.getRuntime().getScript().getCharset());
	}
	
	public static Object objectify(Object object) throws IOException, ParseException {
		JSONBinding binding = new JSONBinding(new MapTypeGenerator(), ScriptRuntime.getRuntime().getScript().getCharset());
		binding.setAllowDynamicElements(true);
		binding.setAddDynamicElementDefinitions(true);
		String string = ScriptMethods.string(object);
		return binding.unmarshal(new ByteArrayInputStream(string.getBytes(ScriptRuntime.getRuntime().getScript().getCharset())), new Window[0]);
	}
	
}
