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

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import be.nabu.glue.api.Executor;
import be.nabu.glue.api.OutputFormatter;
import be.nabu.glue.api.Script;
import be.nabu.glue.api.runs.GlueAttachment;
import be.nabu.glue.api.runs.GlueValidation;
import be.nabu.glue.impl.formatted.FormattedValidation;
import be.nabu.libs.types.binding.json.JSONBinding;
import be.nabu.libs.types.map.MapContent;
import be.nabu.libs.types.map.MapContentWrapper;
import be.nabu.libs.types.map.MapType;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

/**
 * We no longer embed validations and attachments, they can be linked back to steps via ids etc
 */
public class JSONOutputFormatter implements OutputFormatter {

	private Script root;
	private Map<String, Object> current;
	private List<GlueValidation> validations = new ArrayList<GlueValidation>();
	
	private Stack<Map<String, Object>> currentStep = new Stack<Map<String, Object>>();
	private OutputFormatter parent;
	
	public JSONOutputFormatter(OutputFormatter parent) {
		this.parent = parent;
	}

	@Override
	public void start(Script script) {
		if (root == null) {
			root = script;
			current = new HashMap<String, Object>();
			try {
				current.put("started", new Date());
				String title = script.getRoot().getContext() == null ? null : script.getRoot().getContext().getAnnotations().get("title");
				if (title == null) {
					title = script.getName();
				}
				if (title != null) {
					current.put("title", title);
				}
				if (script.getRoot().getContext() != null && script.getRoot().getContext().getDescription() != null) {
					current.put("description", script.getRoot().getContext().getDescription().replaceAll("'", "\\'") + "'");
				}
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if (parent != null) {
			parent.start(script);
		}
	}
	@Override
	public void end(Script script, Date started, Date stopped, Exception exception) {
		if (root.equals(script)) {
			
			// if we stopped the root with an exception, we should unwind some steps
			if (exception != null) {
				while (!currentStep.isEmpty()) {
					Map<String, Object> pop = currentStep.pop();
					pop.put("severity", Severity.ERROR);
					pop.put("stopped", new Date());
				}
			}
			
			current.put("stopped", new Date());
			MapType type = MapContentWrapper.buildFromContent(current);
			JSONBinding binding = new JSONBinding(type, Charset.forName("UTF-8"));
			binding.setPrettyPrint(true);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try {
				binding.marshal(output, new MapContent(type, current));
				if (parent != null) {
					parent.print(new String(output.toByteArray(), "UTF-8"));
				}
			}
			catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		if (parent != null) {
			parent.end(script, started, stopped, exception);
		}
	}

	private Map<String, Object> newStep(String id) {
		List<Map<String, Object>> steps = (List<Map<String, Object>>) current.get("steps");
		if (steps == null) {
			steps = new ArrayList<Map<String, Object>>();
			current.put("steps", steps);
		}
		Map<String, Object> step = new HashMap<String, Object>();
		step.put("id", id);
		step.put("started", new Date());
		steps.add(step);
		return step;
	}
	
	@Override
	public void before(Executor executor) {
		if (executor.getContext() != null && executor.getContext().getAnnotations() != null) {
			String id = executor.getContext().getAnnotations().get("id");
			if (id != null) {
				currentStep.push(newStep(id));
			}
		}
		if (parent != null) {
			parent.before(executor);
		}
	}

	@Override
	public void after(Executor executor) {
		if (executor.getContext() != null && executor.getContext().getAnnotations() != null) {
			String id = executor.getContext().getAnnotations().get("id");
			if (id != null) {
				Map<String, Object> pop = currentStep.pop();
				Severity severity = Severity.INFO;
				if (validations != null) {
					for (GlueValidation validation : validations) {
						if (executor.equals(validation.getExecutor())) {
							if (validation.getSeverity() != null && validation.getSeverity().ordinal() > severity.ordinal()) {
								severity = validation.getSeverity();
							}
						}
					}
				}
				pop.put("severity", severity);
				pop.put("stopped", new Date());
			}
		}
		if (parent != null) {
			parent.after(executor);
		}
	}

	@Override
	public void validated(GlueValidation...validations) {
//		Map<String, Object> target;
//		if (!currentStep.isEmpty()) {
//			target = currentStep.peek();
//		}
//		else {
//			target = current;
//		}
//		if (target != null && validations != null && validations.length > 0) {
//			List<FormattedValidation> object = (List<FormattedValidation>) target.get("validations");
//			if (object == null) {
//				object = new ArrayList<FormattedValidation>();
//				target.put("validations", object);
//			}
//			for (GlueValidation validation : validations) {
//				object.add(FormattedValidation.format(validation));
//			}
//		}
		if (validations != null && validations.length > 0) {
			this.validations.addAll(Arrays.asList(validations));
		}
		if (parent != null) {
			parent.validated(validations);
		}
	}

	@Override
	public void print(Object... messages) {
		Map<String, Object> target;
		if (!currentStep.isEmpty()) {
			target = currentStep.peek();
		}
		else {
			target = current;
		}
		if (target != null && messages != null && messages.length > 0) {
			List<Object> object = (List<Object>) target.get("messages");
			if (object == null) {
				object = new ArrayList<Object>();
				target.put("messages", object);
			}
			object.addAll(Arrays.asList(messages));
		}
		if (parent != null) {
			parent.print(messages);
		}
	}

	@Override
	public boolean shouldExecute(Executor executor) {
		if (parent != null) {
			return parent.shouldExecute(executor);
		}
		return true;
	}

	@Override
	public OutputFormatter getParent() {
		return parent;
	}

	@Override
	public void attached(GlueAttachment... attachments) {
//		Map<String, Object> target;
//		if (!currentStep.isEmpty()) {
//			target = currentStep.peek();
//		}
//		else {
//			target = current;
//		}
//		if (target != null && attachments != null && attachments.length > 0) {
//			List<FormattedAttachment> object = (List<FormattedAttachment>) target.get("attachments");
//			if (object == null) {
//				object = new ArrayList<FormattedAttachment>();
//				target.put("attachments", object);
//			}
//			for (GlueAttachment attachment : attachments) {
//				object.add(FormattedAttachment.format(attachment));
//			}
//		}
		if (parent != null) {
			parent.attached(attachments);
		}
	}

}
