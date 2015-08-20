This plugin adds support for reading json, for example suppose you have this simple json file:

```json
{"name": "/",
        "children": [
                {"name": "test1"},
                {"name": "test2"}
        ]
}
```

You can parse it using:

```python
object = json.objectify("text.json")
```

If you have the glue XML plugin you can also easily transform it to XML:

```python
echo(xml.stringify(object))
```

Which would output:

```xml
<anonymous>
	<name>/</name>
	<children>
		<name>test1</name>
	</children>
	<children>
		<name>test2</name>
	</children>
</anonymous>
```

And if you have the glue types plugin you have native access to the parsed JSON object (and XML object):

```python
echo("Root: " + object/name)
for (child : object/children)
        echo("\t" + child/name)

echo("All children:")
echo(object/children/name)
```

Which would output:

```
Root: /
	test1
	test2
All children:
test1
test2
```

