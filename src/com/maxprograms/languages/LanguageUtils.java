/*******************************************************************************
 * Copyright (c) 2022 - 2025 Maxprograms.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-v10.html
 *
 * Contributors:
 *     Maxprograms - initial API and implementation
 *******************************************************************************/
package com.maxprograms.languages;

import java.io.IOException;
import java.net.URL;
import java.text.Collator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.maxprograms.xml.Element;
import com.maxprograms.xml.SAXBuilder;

public class LanguageUtils {

	private static final Map<String, LanguageBundle> EXTENDED_LANGUAGE_CACHE = new HashMap<>();
	private static final Map<String, List<Language>> COMMON_LANGUAGE_CACHE = new HashMap<>();
	private static RegistryParser registry;

	private LanguageUtils() {
		// do not instantiate
	}

	public static List<Language> getAllLanguages() throws SAXException, IOException, ParserConfigurationException {
		Locale locale = Locale.getDefault();
		LanguageBundle bundle = loadExtendedLanguages(locale);
		return bundle.languages;
	}

	public static List<Language> getCommonLanguages() throws SAXException, IOException, ParserConfigurationException {
		Locale locale = Locale.getDefault();
		String key = localeKey(locale);
		List<Language> cached = COMMON_LANGUAGE_CACHE.get(key);
		if (cached != null) {
			return cached;
		}
		List<Language> list = new Vector<>();
		SAXBuilder builder = new SAXBuilder();
		URL resource = resolveResource("languageList", locale);
		Element root = builder.build(resource).getRootElement();
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element lang = it.next();
			String code = lang.getAttributeValue("code");
			String description = lang.getText();
			list.add(new Language(code, description));
		}
		Collator collator = Collator.getInstance(locale == null ? Locale.getDefault() : locale);
		Collections.sort(list, (l1, l2) -> collator.compare(l1.getDescription(), l2.getDescription()));
		COMMON_LANGUAGE_CACHE.put(key, list);
		return list;
	}

	public static Language getLanguage(String code) throws IOException, SAXException, ParserConfigurationException {
		List<Language> list = getAllLanguages();
		Iterator<Language> it = list.iterator();
		while (it.hasNext()) {
			Language l = it.next();
			if (l.getCode().equals(code)) {
				return l;
			}
		}
		if (registry == null) {
			registry = new RegistryParser(LanguageUtils.class.getResource("language-subtag-registry.txt"));
		}
		String description = registry.getTagDescription(code);
		if (description != null) {
			return new Language(code, description);
		}
		return null;
	}

	public static Language languageFromName(String description)
			throws SAXException, IOException, ParserConfigurationException {
		List<Language> list = getAllLanguages();
		Iterator<Language> it = list.iterator();
		while (it.hasNext()) {
			Language l = it.next();
			if (l.getDescription().equals(description)) {
				return l;
			}
		}
		return null;
	}

	public static String normalizeCode(String code) throws IOException {
		if (registry == null) {
			registry = new RegistryParser(LanguageUtils.class.getResource("language-subtag-registry.txt"));
		}
		return registry.normalizeCode(code);
	}

	public static boolean isBiDi(String code) throws SAXException, IOException, ParserConfigurationException {
		LanguageBundle bundle = loadExtendedLanguages(Locale.getDefault());
		return bundle.bidiCodes.contains(code);
	}

	public static boolean isCJK(String code) {
		return code.startsWith("zh") || code.startsWith("ja") || code.startsWith("ko") || code.startsWith("vi")
				|| code.startsWith("ain") || code.startsWith("aib");
	}

	public static String[] getLanguageNames() throws SAXException, IOException, ParserConfigurationException {
		List<Language> list = getCommonLanguages();
		Iterator<Language> it = list.iterator();
		List<String> result = new Vector<>();
		while (it.hasNext()) {
			result.add(it.next().getDescription());
		}
		return result.toArray(new String[result.size()]);
	}

	private static URL resolveResource(String baseName, Locale locale) throws IOException {
		String language = locale == null ? "" : locale.getLanguage();
		if (language != null && !language.isEmpty()) {
			String candidate = baseName + "_" + language + ".xml";
			URL url = LanguageUtils.class.getResource(candidate);
			if (url != null) {
				return url;
			}
			if (language.length() > 2) {
				candidate = baseName + "_" + language.substring(0, 2) + ".xml";
				url = LanguageUtils.class.getResource(candidate);
				if (url != null) {
					return url;
				}
			}
		}
		URL fallback = LanguageUtils.class.getResource(baseName + ".xml");
		if (fallback != null) {
			return fallback;
		}
		throw new IOException("Language resource not found for " + baseName);
	}

	private static LanguageBundle loadExtendedLanguages(Locale locale)
			throws SAXException, IOException, ParserConfigurationException {
		String key = localeKey(locale);
		LanguageBundle bundle = EXTENDED_LANGUAGE_CACHE.get(key);
		if (bundle != null) {
			return bundle;
		}
		List<Language> list = new Vector<>();
		Set<String> bidi = new TreeSet<>();
		SAXBuilder builder = new SAXBuilder();
		URL resource = resolveResource("extendedLanguageList", locale);
		Element root = builder.build(resource).getRootElement();
		List<Element> children = root.getChildren();
		Iterator<Element> it = children.iterator();
		while (it.hasNext()) {
			Element lang = it.next();
			String code = lang.getAttributeValue("code");
			String description = lang.getText();
			list.add(new Language(code, description));
			if ("true".equals(lang.getAttributeValue("bidi"))) {
				bidi.add(code);
			}
		}
		Collator collator = Collator.getInstance(locale == null ? Locale.getDefault() : locale);
		Collections.sort(list, (l1, l2) -> collator.compare(l1.getDescription(), l2.getDescription()));
		bundle = new LanguageBundle(list, bidi);
		EXTENDED_LANGUAGE_CACHE.put(key, bundle);
		return bundle;
	}

	private static String localeKey(Locale locale) {
		if (locale == null) {
			return "";
		}
		String language = locale.getLanguage();
		return language == null ? "" : language;
	}

	private static class LanguageBundle {
		private final List<Language> languages;
		private final Set<String> bidiCodes;

		LanguageBundle(List<Language> languages, Set<String> bidiCodes) {
			this.languages = languages;
			this.bidiCodes = bidiCodes;
		}
	}
}
