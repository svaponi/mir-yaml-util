# mir-yaml-util
Simple utility to parse YAML files and provides "property-file" like syntax to access properties. It uses [snakeyaml](https://github.com/asomov/snakeyaml).

## How to
Example of how to use the YamlHandler object.

```Java
static YamlHandler arrayYmlNullOnInvalidKey;
static YamlHandler arrayYmlThrowOnInvalidKey;

@BeforeClass
public static void setup() {
	arrayYmlThrowOnInvalidKey = new YamlHandler(YAML_ARRAY_FILE, true);
	arrayYmlNullOnInvalidKey = new YamlHandler(YAML_ARRAY_FILE, false);
}

@SuppressWarnings("unchecked")
@Test
public void t03_read_array() {
	System.out.println("t03_read_array");

	Assert.assertEquals("value4", arrayYmlNullOnInvalidKey.get("key1.array2[0].key3.key4"));
	Assert.assertEquals("value4B", arrayYmlNullOnInvalidKey.get("key1.array2[1].key3.array4[1]"));
	Assert.assertEquals("value4F", arrayYmlNullOnInvalidKey.get("key1.array2[2].key3.array4[5]"));
	Object array4 = arrayYmlNullOnInvalidKey.get("key1.array2[2].key3.array4");
	Assert.assertTrue("Array returned with class List", List.class.isAssignableFrom(array4.getClass()));
	int letter = 'A';
	for (String item : ((List<String>) array4))
		Assert.assertEquals("Array items match", item, "value4" + ((char) letter++));

	System.out.println("t03_read_array > OK");
}

@Rule
public final ExpectedException exception = ExpectedException.none();

@Test
public void t04_missing_key() {
	System.out.println("t04_missing_key");

	exception.expect(InvalidKeyException.class);
	Assert.assertNull("Missing key", arrayYmlThrowOnInvalidKey.get("key1.array2[0].key3.key5"));

	System.out.println("t04_missing_key > OK");
}

@Test
public void t05_missing_key_2() {
	System.out.println("t05_missing_key_2");

	exception.expect(InvalidKeyException.class);
	Assert.assertNull("Missing key", arrayYmlThrowOnInvalidKey.get("key1.array2[0].key9.key1"));

	System.out.println("t05_missing_key_2 > OK");
}

@Test
public void t06_array_out_of_index() {
	System.out.println("t06_array_out_of_index");

	exception.expect(InvalidKeyException.class);
	Assert.assertNull("Array out of index", arrayYmlThrowOnInvalidKey.get("key1.array2[5].key3.key5"));

	System.out.println("t06_array_out_of_index > OK");
}

@Test
public void t07_invalid_key() {
	System.out.println("t07_invalid_key");

	exception.expect(InvalidKeyException.class);
	Assert.assertNull("Missing intermediate key", arrayYmlThrowOnInvalidKey.get("key1.array9[0].key3.key5"));

	System.out.println("t07_invalid_key > OK");
}

@Test
public void t08_return_null() {
	System.out.println("t08_return_null");

	Assert.assertNull("Missing key", arrayYmlNullOnInvalidKey.get("key1.array2[0].key3.key5"));
	Assert.assertNull("Missing key", arrayYmlNullOnInvalidKey.get("key1.array2[0].key9.key1"));
	Assert.assertNull("Array out of index", arrayYmlNullOnInvalidKey.get("key1.array2[5].key3.key5"));
	Assert.assertNull("Missing intermediate key", arrayYmlNullOnInvalidKey.get("key1.array9[0].key3.key5"));

	System.out.println("t08_return_null > OK");
}
	
```


```yaml
key1:
  key2: value2
  array2:
    -
      key3:
        key4: value4
    -
      key3:
        array4:
          - "value4A"
          - "value4B"
          - "value4C"
          - "value4D"
    -
      key3:
        array4:
          - "value4A"
          - "value4B"
          - "value4C"
          - "value4D"
          - "value4E"
          - "value4F"
```