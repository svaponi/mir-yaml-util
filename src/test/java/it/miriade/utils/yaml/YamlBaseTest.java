package it.miriade.utils.yaml;

import java.util.Arrays;
import java.util.List;

/**
 * Contenitore di costanti. Tutti i path sono relativi al classpath.
 * 
 * @author svaponi
 *
 */
public class YamlBaseTest {

	/**
	 * Directory base dei file YAML usati nei test
	 */
	public static final String YAML_DIR = "yaml";

	public static final String YAML_DATA_FILE = YAML_DIR + "/data.yml";

	public static final String YAML_ARRAY_FILE = YAML_DIR + "/array.yml";

	public static final String YAML_UTF8_FILE = YAML_DIR + "/utf-8.txt";

	public static final List<String> YAML_FILES = Arrays.asList(YAML_DIR + "/conf-chrome.yml",
			YAML_DIR + "/conf-firefox.yml", YAML_DIR + "/conf-ie.yml", YAML_DIR + "/data.yml");

	public static final List<String> YAML_CONF_FILES = Arrays.asList(YAML_DIR + "/conf-chrome.yml",
			YAML_DIR + "/conf-firefox.yml", YAML_DIR + "/conf-ie.yml");

	public static final String OUTPUT_YAML_FILE = YAML_DIR + "/dump.yml";

	public static String print(Object properties) {
		return properties.toString().replaceAll("\\n", " ").replaceAll("  ", " ");
	}
}
