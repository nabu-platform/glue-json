/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
import be.nabu.libs.types.map.MapContent;
import be.nabu.libs.types.map.MapTypeGenerator;

@MethodProviderClass(namespace = "json")
public class JSONMethods {
	
	@GlueMethod(description = "Serializes an object as JSON", returns = "The json string")
	@SuppressWarnings({ "unchecked" })
	public static String stringify(@GlueParam(name = "object") Object object, @GlueParam(name = "pretty") Boolean prettyPrint, @GlueParam(name = "raw") Boolean allowRaw, @GlueParam(name = "full") Boolean full, @GlueParam(name = "forceRoot") Boolean forceRoot) throws IOException {
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
		if (allowRaw != null) {
			binding.setAllowRaw(allowRaw);
		}
		binding.setIgnoreRootIfArrayWrapper(forceRoot == null || forceRoot == false);
		if (prettyPrint != null) {
			binding.setPrettyPrint(prettyPrint);
		}
		if (full != null && full) {
			binding.setMarshalExplicitNullValues(true);
		}
		binding.marshal(output, content);
		return new String(output.toByteArray(), ScriptRuntime.getRuntime().getScript().getCharset());
	}

	@GlueMethod(description = "Deserializes a JSON string as an object", returns = "The object")
	public static Object objectify(@GlueParam(name = "json") Object object, @GlueParam(name = "raw") Boolean allowRaw, @GlueParam(name = "dashes") Boolean allowDashes, @GlueParam(name = "stringsOnly") Boolean stringsOnly) throws IOException, ParseException {
		if (object == null) {
			return null;
		}
		MapTypeGenerator complexTypeGenerator = new MapTypeGenerator(allowRaw != null && allowRaw);
		complexTypeGenerator.setWrapMaps(true);
		JSONBinding binding = new JSONBinding(complexTypeGenerator, ScriptRuntime.getRuntime().getScript().getCharset());
		binding.setAllowDynamicElements(true);
		binding.setAddDynamicElementDefinitions(true);
		binding.setIgnoreRootIfArrayWrapper(true);
		binding.setCamelCaseDashes(allowDashes == null || !allowDashes);
		binding.setParseNumbers(true);
		if (allowRaw != null) {
			binding.setAllowRaw(allowRaw);
		}
		if (stringsOnly != null) {
			binding.setAddDynamicStringsOnly(stringsOnly);
		}
		String string = ScriptMethods.string(object, null);
		return binding.unmarshal(new ByteArrayInputStream(string.getBytes(ScriptRuntime.getRuntime().getScript().getCharset())), new Window[0]);
	}
	
}
