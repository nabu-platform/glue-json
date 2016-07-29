package be.nabu.glue.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;

import be.nabu.glue.ScriptRuntime;
import be.nabu.glue.annotations.GlueMethod;
import be.nabu.glue.annotations.GlueParam;
import be.nabu.glue.impl.methods.ScriptMethods;
import be.nabu.libs.evaluator.annotations.MethodProviderClass;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.json.JSONBinding;
import be.nabu.libs.types.map.MapTypeGenerator;

@MethodProviderClass(namespace = "json")
public class JSONMethods {
	
	@GlueMethod(description = "Serializes an object as JSON", returns = "The json string")
	@SuppressWarnings({ "unchecked" })
	public static String stringify(@GlueParam(name = "object") Object object) throws IOException {
		if (object == null) {
			return null;
		}
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ComplexContent content;
		if (object instanceof ComplexContent) {
			content = (ComplexContent) object;
		}
		else {
			content = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(object);
		}
		JSONBinding binding = new JSONBinding(content.getType(), ScriptRuntime.getRuntime().getScript().getCharset());
		binding.setIgnoreRootIfArrayWrapper(true);
		binding.marshal(output, content);
		return new String(output.toByteArray(), ScriptRuntime.getRuntime().getScript().getCharset());
	}

	@GlueMethod(description = "Deserializes a JSON string as an object", returns = "The object")
	public static Object objectify(@GlueParam(name = "json") Object object) throws IOException, ParseException {
		if (object == null) {
			return null;
		}
		JSONBinding binding = new JSONBinding(new MapTypeGenerator(), ScriptRuntime.getRuntime().getScript().getCharset());
		binding.setAllowDynamicElements(true);
		binding.setAddDynamicElementDefinitions(true);
		binding.setIgnoreRootIfArrayWrapper(true);
		binding.setCamelCaseDashes(true);
		binding.setParseNumbers(true);
		String string = ScriptMethods.string(object, null);
		return binding.unmarshal(new ByteArrayInputStream(string.getBytes(ScriptRuntime.getRuntime().getScript().getCharset())), new Window[0]);
	}
	
}
