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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegistryParser {

	private List<RegistryEntry> entries;
	private Map<String, Language> languages;
	private Map<String, Region> regions;
	private Map<String, Script> scripts;
	private Map<String, Variant> variants;

	// Private-use ranges
	private String privateLanguageStart;
	private String privateLanguageEnd;
	private String privateScriptStart;
	private String privateScriptEnd;
	private String[][] privateRegionRanges;

	private void parseRegistry(URL url) throws IOException {
		try (InputStream input = url.openStream()) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
				String line = "";
				entries = new ArrayList<>();
				StringBuilder buffer = new StringBuilder();
				while ((line = reader.readLine()) != null) {
					if (line.trim().equals("%%")) {
						entries.add(new RegistryEntry(buffer.toString().replace("\n  ", " ")));
						buffer = new StringBuilder();
					} else {
						buffer.append(line);
						buffer.append('\n');
					}
				}
			}
		}
		languages = new HashMap<>();
		regions = new HashMap<>();
		scripts = new HashMap<>();
		variants = new HashMap<>();
		privateRegionRanges = new String[0][];
		List<String[]> regionRangesList = new ArrayList<>();
		Iterator<RegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			RegistryEntry entry = it.next();
			String type = entry.getType();
			if (type == null) {
				continue;
			}
			if (type.equals("language")) {
				String description = entry.getDescription();
				String subtag = entry.getSubtag();
				if (subtag != null && subtag.contains("..")) {
					// Private-use range like "qaa..qtz"
					String[] range = subtag.split("\\.\\.");
					if (range.length == 2) {
						privateLanguageStart = range[0].toLowerCase();
						privateLanguageEnd = range[1].toLowerCase();
					}
					continue;
				}
				if (subtag != null) {
					if (description.indexOf('|') != -1) {
						// trim and use only the first name
						description = description.substring(0, description.indexOf('|') - 1);
					}
					if (subtag.equals("el")) {
						// official description is "Modern Greek (1453-)", use a familiar name
						description = "Greek";
					}
					description = description.replaceAll("\\(.*\\)", "").trim();
					Language lang = new Language(subtag, description.trim());
					String suppressedScript = entry.get("Suppress-Script");
					if (suppressedScript != null) {
						lang.setSuppressedScript(suppressedScript);
					}
					languages.put(subtag, lang);
				}
			}
			if (type.equals("region")) {
				String description = entry.getDescription();
				String subtag = entry.getSubtag();
				if (subtag != null && subtag.contains("..")) {
					// Private-use range like "QM..QZ" or "XA..XZ"
					String[] range = subtag.split("\\.\\.");
					if (range.length == 2) {
						regionRangesList.add(new String[] { range[0].toUpperCase(), range[1].toUpperCase() });
					}
					continue;
				}
				if (subtag != null) {
					regions.put(subtag, new Region(subtag, description.trim()));
				}
			}
			if (type.equals("script")) {
				String description = entry.getDescription();
				description = description.replace('(', '[');
				description = description.replace(')', ']');
				String subtag = entry.getSubtag();
				if (subtag != null && subtag.contains("..")) {
					// Private-use range like "Qaaa..Qabx"
					String[] range = subtag.split("\\.\\.");
					if (range.length == 2) {
						privateScriptStart = range[0].substring(0, 1).toUpperCase()
								+ range[0].substring(1).toLowerCase();
						privateScriptEnd = range[1].substring(0, 1).toUpperCase() + range[1].substring(1).toLowerCase();
					}
					continue;
				}
				if (subtag != null) {
					scripts.put(subtag, new Script(subtag, description.trim()));
				}
			}
			if (type.equals("variant")) {
				String description = entry.getDescription();
				description = description.replace('(', '[');
				description = description.replace(')', ']');
				String subtag = entry.getSubtag();
				String prefix = entry.get("Prefix");
				if (subtag != null) {
					variants.put(subtag, new Variant(subtag, description.trim(), prefix));
				}
			}
		}
		privateRegionRanges = regionRangesList.toArray(new String[0][]);
	}

	private boolean isPrivateLanguage(String code) {
		if (privateLanguageStart == null || privateLanguageEnd == null) {
			return false;
		}
		String lowerCode = code.toLowerCase();
		return lowerCode.compareTo(privateLanguageStart) >= 0 && lowerCode.compareTo(privateLanguageEnd) <= 0;
	}

	private boolean isPrivateScript(String code) {
		if (privateScriptStart == null || privateScriptEnd == null) {
			return false;
		}
		String normalizedCode = code.substring(0, 1).toUpperCase() + code.substring(1).toLowerCase();
		return normalizedCode.compareTo(privateScriptStart) >= 0 && normalizedCode.compareTo(privateScriptEnd) <= 0;
	}

	private boolean isPrivateRegion(String code) {
		String upperCode = code.toUpperCase();
		for (String[] range : privateRegionRanges) {
			if (upperCode.compareTo(range[0]) >= 0 && upperCode.compareTo(range[1]) <= 0) {
				return true;
			}
		}
		return false;
	}

	public String getRegistryDate() {
		Iterator<RegistryEntry> it = entries.iterator();
		while (it.hasNext()) {
			RegistryEntry entry = it.next();
			Set<String> set = entry.getTypes();
			if (set.contains("File-Date")) {
				return entry.get("File-Date");
			}
		}
		return null;
	}

	public RegistryParser(URL url) throws IOException {
		parseRegistry(url);
	}

	public RegistryParser() throws IOException {
		URL url = RegistryParser.class.getResource("language-subtag-registry.txt");
		parseRegistry(url);
	}

	public String getTagDescription(String tag) {
		String[] parts = tag.split("-");
		if (parts.length == 1) {
			// language part only
			if (languages.containsKey(tag.toLowerCase())) {
				return languages.get(tag.toLowerCase()).getDescription();
			}
			if (isPrivateLanguage(tag)) {
				return Messages.getString("RegistryParser.0");
			}
		} else if (parts.length == 2) {
			// contains either script or region
			boolean isPrivateLang = isPrivateLanguage(parts[0]);
			if (!languages.containsKey(parts[0].toLowerCase()) && !isPrivateLang) {
				return "";
			}
			String langDesc = isPrivateLang ? Messages.getString("RegistryParser.0")
					: languages.get(parts[0].toLowerCase()).getDescription();
			Language lang = isPrivateLang ? null : languages.get(parts[0].toLowerCase());
			if (parts[1].length() == 2 && (regions.containsKey(parts[1].toUpperCase()) || isPrivateRegion(parts[1]))) {
				// could be a country code
				String regionDesc = isPrivateRegion(parts[1]) ? Messages.getString("RegistryParser.0")
						: regions.get(parts[1].toUpperCase()).getDescription();
				return langDesc + " (" + regionDesc + ")";
			}
			if (parts[1].length() == 3 && regions.containsKey(parts[1])) {
				// could be a UN region code
				Region reg = regions.get(parts[1]);
				return langDesc + " (" + reg.getDescription() + ")";
			}
			if (parts[1].length() == 4) {
				// could have script
				String script = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1).toLowerCase();
				if (lang != null && script.equals(lang.getSuppresedScript())) {
					return "";
				}
				if (scripts.containsKey(script)) {
					return langDesc + " (" + scripts.get(script).getDescription() + ")";
				}
				if (isPrivateScript(script)) {
					return langDesc + " (" + Messages.getString("RegistryParser.0") + ")";
				}
			}
			// try with a variant
			if (!isPrivateLang && variants.containsKey(parts[1].toLowerCase())) {
				Variant variant = variants.get(parts[1].toLowerCase());
				if (variant != null && variant.getPrefix().equals(parts[0].toLowerCase())) {
					// variant is valid for the language code
					return langDesc + " (" + variant.getDescription() + ")";
				}
			}
			if (isPrivateLang) {
				return Messages.getString("RegistryParser.0");
			}
		} else if (parts.length == 3) {
			boolean isPrivateLang = isPrivateLanguage(parts[0]);
			if (!languages.containsKey(parts[0].toLowerCase()) && !isPrivateLang) {
				return "";
			}
			String langDesc = isPrivateLang ? Messages.getString("RegistryParser.0")
					: languages.get(parts[0].toLowerCase()).getDescription();
			Language lang = isPrivateLang ? null : languages.get(parts[0].toLowerCase());
			if (parts[1].length() == 4) {
				// could be script + region or variant
				String script = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1).toLowerCase();
				if (lang != null && script.equals(lang.getSuppresedScript())) {
					return "";
				}
				boolean isPrivateScr = isPrivateScript(script);
				if (scripts.containsKey(script) || isPrivateScr) {
					String scrDesc = isPrivateScr ? Messages.getString("RegistryParser.0")
							: scripts.get(script).getDescription();
					// check if next part is a region or variant
					boolean isPrivateReg = isPrivateRegion(parts[2]);
					if (regions.containsKey(parts[2].toUpperCase()) || isPrivateReg) {
						String regDesc = isPrivateReg ? Messages.getString("RegistryParser.0")
								: regions.get(parts[2].toUpperCase()).getDescription();
						return langDesc + " (" + scrDesc + ", " + regDesc + ")";
					}
					if (!isPrivateLang && variants.containsKey(parts[2].toLowerCase())) {
						Variant variant = variants.get(parts[2].toLowerCase());
						if (variant != null && variant.getPrefix().equals(parts[0].toLowerCase())) {
							// variant is valid for the language code
							return langDesc + " (" + scrDesc + ", " + variant.getDescription()
									+ ")";
						}
					}
				}
			} else {
				// could be region + variant
				boolean isPrivateReg = isPrivateRegion(parts[1]);
				if ((parts[1].length() == 2 || parts[1].length() == 3)
						&& (regions.containsKey(parts[1].toUpperCase()) || isPrivateReg)) {
					// could be a region code, check if next part is a variant
					String regDesc = isPrivateReg ? Messages.getString("RegistryParser.0")
							: regions.get(parts[1].toUpperCase()).getDescription();
					if (!isPrivateLang && variants.containsKey(parts[2].toLowerCase())) {
						Variant variant = variants.get(parts[2].toLowerCase());
						if (variant != null && variant.getPrefix().equals(parts[0].toLowerCase())) {
							// variant is valid for the language code
							return langDesc + " (" + regDesc + " - "
									+ variant.getDescription() + ")";
						}
					}
					// For private-use languages with regions, return description
					if (isPrivateLang) {
						return langDesc + " (" + regDesc + ")";
					}
				}
			}
		}
		return "";
	}

	public String normalizeCode(String code) {
		String[] parts = code.split("-");
		if (parts.length == 1) {
			// language part only
			if (languages.containsKey(code.toLowerCase())) {
				return code.toLowerCase();
			}
			if (isPrivateLanguage(code)) {
				return code.toLowerCase();
			}
		} else if (parts.length == 2) {
			// contains either script or region
			boolean isPrivateLang = isPrivateLanguage(parts[0]);
			if (!languages.containsKey(parts[0].toLowerCase()) && !isPrivateLang) {
				return "";
			}
			Language lang = isPrivateLang ? null : languages.get(parts[0].toLowerCase());
			if (parts[1].length() == 2 && (regions.containsKey(parts[1].toUpperCase()) || isPrivateRegion(parts[1]))) {
				// could be a country code
				return parts[0].toLowerCase() + "-" + parts[1].toUpperCase();
			}
			if (parts[1].length() == 3 && regions.containsKey(parts[1])) {
				// could be a UN region code
				return parts[0].toLowerCase() + "-" + parts[1];
			}
			if (parts[1].length() == 4) {
				// could have script
				String script = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1).toLowerCase();
				if (lang != null && script.equals(lang.getSuppresedScript())) {
					return "";
				}
				if (scripts.containsKey(script) || isPrivateScript(script)) {
					return parts[0].toLowerCase() + "-" + script;
				}
			}
			// try with a variant
			if (!isPrivateLang && variants.containsKey(parts[1].toLowerCase())) {
				Variant variant = variants.get(parts[1].toLowerCase());
				if (variant != null && variant.getPrefix().equals(parts[0].toLowerCase())) {
					// variant is valid for the language code
					return parts[0].toLowerCase() + "-" + variant.getCode();
				}
			}
		} else if (parts.length == 3) {
			boolean isPrivateLang = isPrivateLanguage(parts[0]);
			if (!languages.containsKey(parts[0].toLowerCase()) && !isPrivateLang) {
				return "";
			}
			Language lang = isPrivateLang ? null : languages.get(parts[0].toLowerCase());
			if (parts[1].length() == 4) {
				// could be script + region or variant
				String script = parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1).toLowerCase();
				if (lang != null && script.equals(lang.getSuppresedScript())) {
					return "";
				}
				boolean isPrivateScr = isPrivateScript(script);
				if (scripts.containsKey(script) || isPrivateScr) {
					String scrCode = isPrivateScr ? script : scripts.get(script).getCode();
					// check if next part is a region or variant
					boolean isPrivateReg = isPrivateRegion(parts[2]);
					if (regions.containsKey(parts[2].toUpperCase()) || isPrivateReg) {
						String regCode = isPrivateReg ? parts[2].toUpperCase()
								: regions.get(parts[2].toUpperCase()).getCode();
						return parts[0].toLowerCase() + "-" + scrCode + "-" + regCode;
					}
					if (!isPrivateLang && variants.containsKey(parts[2].toLowerCase())) {
						Variant variant = variants.get(parts[2].toLowerCase());
						if (variant != null && variant.getPrefix().equals(parts[0].toLowerCase())) {
							// variant is valid for the language code
							return parts[0].toLowerCase() + "-" + scrCode + "-" + variant.getCode();
						}
					}
				}
			} else {
				// could be region + variant
				boolean isPrivateReg = isPrivateRegion(parts[1]);
				if ((parts[1].length() == 2 || parts[1].length() == 3)
						&& (regions.containsKey(parts[1].toUpperCase()) || isPrivateReg)) {
					// could be a region code, check if next part is a variant
					String regCode = isPrivateReg ? parts[1].toUpperCase()
							: regions.get(parts[1].toUpperCase()).getCode();
					if (!isPrivateLang && variants.containsKey(parts[2].toLowerCase())) {
						Variant variant = variants.get(parts[2].toLowerCase());
						if (variant != null && variant.getPrefix().equals(parts[0].toLowerCase())) {
							// variant is valid for the language code
							return parts[0].toLowerCase() + "-" + regCode + "-" + variant.getCode();
						}
					}
					// For private-use languages with regions, return normalized code
					if (isPrivateLang) {
						return parts[0].toLowerCase() + "-" + regCode;
					}
				}
			}
		}
		return "";
	}

}
