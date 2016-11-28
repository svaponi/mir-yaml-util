package it.miriade.utils.yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Utility per leggere i file YAML. Dispone di getter compatibili con la
 * notazione property file, es. get("test.users.pippo.age")
 * 
 * @author svaponi
 *
 */
@SuppressWarnings("unchecked")
public class YamlHandler {

	// throwOnInvalidKey = false => ritorna NULL se la key è invalida o
	// inesistente
	// throwOnInvalidKey = true => solleva InvalidKeyException se la key è
	// invalida o inesistente

	private final Logger log = LoggerFactory.getLogger(getClass());
	private String yamlFilePath;
	private String yamlFileName;
	private String loadErrorMessage;
	private boolean loadError = false;
	private boolean throwOnInvalidKey;
	private Map<String, Object> data;

	/**
	 * Solleva InvalidKeyException se la key è invalida o inesistente.
	 * 
	 * @param yamlFilePath
	 *            path del file YAML
	 */
	public YamlHandler(String yamlFilePath) {
		this(yamlFilePath, true);
	}

	/**
	 * 
	 * @param yamlFilePath
	 *            path del file YAML
	 * @param throwOnInvalidKey
	 *            Se TRUE, solleva InvalidKeyException se la key è invalida o
	 *            inesistente
	 */
	public YamlHandler(String yamlFilePath, boolean throwOnInvalidKey) {
		this.throwOnInvalidKey = throwOnInvalidKey;
		this.yamlFilePath = yamlFilePath;
		log.debug("Loading YAML file: {}", yamlFilePath);
		try {
			File yamlFile = new File(ClassLoader.getSystemResource(yamlFilePath).getPath());
			this.yamlFileName = yamlFile.getName();
			data = (Map<String, Object>) new Yaml().load(new FileInputStream(yamlFile));
		} catch (IOException e) {
			loadError = true;
			loadErrorMessage = e.getMessage();
			data = new HashMap<>();
			log.error(e.getMessage(), e);
		}
	}

	public String getYamlFilePath() {
		return yamlFilePath;
	}

	public String getYamlFileName() {
		return yamlFileName;
	}

	public String getLoadErrorMessage() {
		return loadErrorMessage;
	}

	public boolean isLoadError() {
		return loadError;
	}

	public boolean isThrowOnInvalidKey() {
		return throwOnInvalidKey;
	}

	/*
	 * Metodi per navigare la mappa usando la notazione delle property
	 */

	public String getString(String key) {
		return __get(String.class, key);
	}

	public Integer getInteger(String key) {
		return __get(Integer.class, key);
	}

	public Double getDouble(String key) {
		return __get(Double.class, key);
	}

	public Boolean getBoolean(String key) {
		return __get(Boolean.class, key);
	}

	public List<?> getList(String key) {
		return __get(List.class, key);
	}

	public Map<String, Object> getMap(String key) {
		return __get(Map.class, key);
	}

	private <T> T __get(Class<T> clazz, String key) {
		Object tmp = get(key);
		if (tmp != null && !clazz.isAssignableFrom(tmp.getClass()))
			throw new WrongTypeException("Actual type is " + tmp.getClass());
		return (T) tmp;
	}

	/*
	 * Innietta gli argomenti args nella keyTemplate per comporre la chiave da
	 * utilizzare, es. keyTemplate = "?.users.?.userid", args = {"TEST",
	 * "PIPPO"} ==> key = "TEST.users.PIPPO.userid"
	 */

	public Object get(String keyTemplate, String... args) {
		for (String arg : args)
			keyTemplate = keyTemplate.replaceFirst("\\?", arg);
		return get(keyTemplate);
	}

	public String getString(String keyTemplate, String... args) {
		return getString(buildKey(keyTemplate, args));
	}

	public Integer getInteger(String keyTemplate, String... args) {
		return getInteger(buildKey(keyTemplate, args));
	}

	public Double getDouble(String keyTemplate, String... args) {
		return getDouble(buildKey(keyTemplate, args));
	}

	public Boolean getBoolean(String keyTemplate, String... args) {
		return getBoolean(buildKey(keyTemplate, args));
	}

	public List<?> getList(String keyTemplate, String... args) {
		return getList(buildKey(keyTemplate, args));
	}

	public Map<String, ?> getMap(String keyTemplate, String... args) {
		return getMap(buildKey(keyTemplate, args));
	}

	public String buildKey(String keyTemplate, String... args) {
		for (String arg : args)
			keyTemplate = keyTemplate.replaceFirst("\\?", arg);
		return keyTemplate;
	}

	/**
	 * Naviga l'oggetto Map secondo la notazione delle property dove il '.'
	 * indice livello successivo.
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		try {
			Object result = _get(key, data);
			log.debug("[{}] {} = {}", yamlFileName, key, result);
			return result;
		} catch (InvalidKeyException e) {
			if (throwOnInvalidKey)
				throw new InvalidKeyException("Invalid key expression: " + key);
			else
				return null;
		}
	}

	private Object _get(String key, Map<String, Object> data) {
		if (key == null || key.isEmpty())
			return "";

		log.trace("{} = {}", key, data);

		String[] keys = key.split("\\.");

		/*
		 * Controllo se la prima chiave (prima del primo punto) identifica un
		 * array, in tal caso prendo l'elemento indicato dall'indice tra le [].
		 */
		Res res = detectArray(keys[0]);
		Object tmp;
		if (res != null) {
			log.trace("Found array expression");
			tmp = data.get(res.key);
			if (tmp instanceof List<?>)
				try {
					tmp = ((List<?>) tmp).get(res.index);
					log.trace("{}[{}] = {}", res.key, res.index, tmp);
				} catch (IndexOutOfBoundsException e) {
					throw new InvalidKeyException();
				}
			else
				throw new InvalidKeyException();
		} else
			tmp = data.get(keys[0]);

		/*
		 * Controllo se ho un oggetto mappa e se ho ancora livelli da scendere
		 * (identificati dal '.')
		 */
		if (tmp instanceof Map<?, ?> && key.contains("."))
			return _get(key.substring(keys[0].length() + 1), (Map<String, Object>) tmp);
		else if (tmp != null)
			return tmp;
		else
			throw new InvalidKeyException();
	}

	private static final Pattern r = Pattern.compile("(.*)\\[(\\d*)\\](.*)");

	private Res detectArray(String key) {
		Matcher m = r.matcher(key);
		if (m.find())
			return new Res(m.group(1), Integer.parseInt(m.group(2)));
		return null;
	}

	private class Res {
		String key;
		int index;

		public Res(String key, int index) {
			super();
			this.index = index;
			this.key = key;
		}
	}

	public class InvalidKeyException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public InvalidKeyException() {
			super();
		}

		public InvalidKeyException(String message) {
			super(message);
		}
	}

	public class WrongTypeException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public WrongTypeException() {
			super();
		}

		public WrongTypeException(String message) {
			super(message);
		}
	}
}
