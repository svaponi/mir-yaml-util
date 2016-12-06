package it.miriade.utils.yaml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * Test delle funzionalità base della libreria SnakeYaml
 * 
 * @see https://bitbucket.org/asomov/snakeyaml
 * @author svaponi
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class YamlTest extends YamlBaseTest {

	Yaml yaml = new Yaml();

	@Test
	public void t01_read() {
		// System.out.println("t01_read");
		for (String YAML_FILE : YAML_FILES)
			try {
				Object result = yaml
						.load(new FileInputStream(new File(ClassLoader.getSystemResource(YAML_FILE).getPath())));
				Assert.assertNotNull(YAML_FILE + " > result is null", result);
				Assert.assertTrue(YAML_FILE + " > result is not assignable to Map",
						Map.class.isAssignableFrom(result.getClass()));
				Assert.assertFalse(YAML_FILE + " > result is empty", result.toString().isEmpty());
			} catch (IOException e) {
				e.printStackTrace();
			}
		// System.out.println("t01_read > OK");
	}

	@Test
	public void t02_dump() throws IOException {
		// System.out.println("t02_dump");
		DumperOptions options = new DumperOptions();
		options.setWidth(50);
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		yaml = new Yaml(options);

		Map<String, Object> doc = new HashMap<String, Object>();
		for (int y = 0; y < 3; y++) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("name", "Silenthand Olleander");
			data.put("race", "Human");
			data.put("traits", new String[] { "ONE_HAND", "ONE_EYE" });
			Random random = new Random();
			Map<String, Object> inner = new HashMap<String, Object>();
			for (int i = 0; i < 3; i++) {
				data.put("KEY_" + i, random.nextInt());
				inner.put("INNER_KEY_" + i, random.nextInt());
				inner.put("inner-traits_" + i,
						new Object[] { random.nextInt(), random.nextBoolean(), random.nextFloat(), "ONE_EYE" });
			}
			data.put("inner-data", inner);
			doc.put("doc" + y, data);
		}
		String output = yaml.dump(doc);

		String outputFile = ClassLoader.getSystemResource(YAML_DIR).getFile() + "/dump.yml";
		// System.out.println("Writing file: " + outputFile);
		new File(outputFile).delete();
		try (PrintWriter printwriter = new PrintWriter(outputFile, "UTF-8")) {
			printwriter.print(output);
		}

		// System.out.println("Reading new file: " + outputFile);
		StringBuffer buf = new StringBuffer();
		try (BufferedReader br = new BufferedReader(new FileReader(outputFile))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null)
				buf.append(sCurrentLine).append("\n");
		}

		Assert.assertEquals("Dumped file is not the same", output, buf.toString());
		// System.out.println("t02_dump > OK");
	}

	@Test
	public void t03_testLoadFromString() {
		// System.out.println("t03_testLoadFromString");
		String document = "hello: 25";
		Map<?, ?> map = (Map<?, ?>) yaml.load(document);
		Assert.assertEquals("{hello=25}", map.toString());
		Assert.assertEquals(25, map.get("hello"));
		// System.out.println("t03_testLoadFromString > OK");
	}

	@Test
	public void t04_testLoadFromStream() throws FileNotFoundException {
		// System.out.println("t04_testLoadFromStream");
		File file = new File(ClassLoader.getSystemResource(YAML_UTF8_FILE).getPath());
		InputStream input = new FileInputStream(file);
		Object data = yaml.load(input);
		Assert.assertEquals("test", data);
		data = yaml.load(new ByteArrayInputStream("test2".getBytes()));
		Assert.assertEquals("test2", data);
		// System.out.println("t04_testLoadFromStream > OK");
	}
}
