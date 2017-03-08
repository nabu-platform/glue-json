package be.nabu.glue.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.nabu.glue.annotations.GlueMethod;
import be.nabu.glue.annotations.GlueParam;
import be.nabu.glue.core.impl.methods.ScriptMethods;
import be.nabu.glue.core.impl.methods.v2.SeriesMethods;
import be.nabu.glue.utils.ScriptRuntime;
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
	public static String stringify(@GlueParam(name = "object") Object object, Boolean prettyPrint) throws IOException {
		if (object == null) {
			return null;
		}
		else if (object instanceof Object[]) {
			object = Arrays.asList((Object[]) object);
		}
		if (object instanceof Iterable) {
			List<?> resolved = SeriesMethods.resolve((Iterable<?>) object);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("list", resolved);
			object = map;
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
		if (prettyPrint != null) {
			binding.setPrettyPrint(prettyPrint);
		}
		binding.marshal(output, content);
		return new String(output.toByteArray(), ScriptRuntime.getRuntime().getScript().getCharset());
	}

	@GlueMethod(description = "Deserializes a JSON string as an object", returns = "The object")
	public static Object objectify(@GlueParam(name = "json") Object object, Boolean allowRaw) throws IOException, ParseException {
		if (object == null) {
			return null;
		}
		JSONBinding binding = new JSONBinding(new MapTypeGenerator(), ScriptRuntime.getRuntime().getScript().getCharset());
		binding.setAllowDynamicElements(true);
		binding.setAddDynamicElementDefinitions(true);
		binding.setIgnoreRootIfArrayWrapper(true);
		binding.setCamelCaseDashes(true);
		binding.setParseNumbers(true);
		if (allowRaw != null) {
			binding.setAllowRaw(allowRaw);
		}
		String string = ScriptMethods.string(object, null);
		return binding.unmarshal(new ByteArrayInputStream(string.getBytes(ScriptRuntime.getRuntime().getScript().getCharset())), new Window[0]);
	}
	
}
