/*******************************************************************************
 * Copyright (c) 2022-2026 Maxprograms. All rights reserved.
 *
 * This software is the proprietary property of Maxprograms.
 * Use, modification, and distribution are subject to the terms of the 
 * Software License Agreement found in the root of this distribution 
 *
 * Unauthorized redistribution or commercial use is strictly prohibited.
 *******************************************************************************/
package com.maxprograms.languages;

import java.io.IOException;
import java.io.Serializable;
import java.text.Collator;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Language implements Comparable<Language>, Serializable {

	private static final long serialVersionUID = -5391793426888923842L;

	private String code;
	private String description;
	private String suppresedScript;

	private static Collator collator;

	public Language(String code, String description) {
		this.code = code;
		this.description = description;
		suppresedScript = "";
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Language lang) {
			return code.equals(lang.getCode()) && description.equals(lang.getDescription());
		}
		return false;
	}

	@Override
	public int compareTo(Language arg0) {
		if (collator == null) {
			Locale locale = Locale.getDefault();
			String resource = "extendedLanguageList_" + locale.getLanguage() + ".xml";
			if (Language.class.getResourceAsStream(resource) == null) {
				collator = Collator.getInstance(locale);
			} else {
				collator = Collator.getInstance( Locale.forLanguageTag("en"));
			}			
		}
		return collator.compare(description, arg0.getDescription());
	}

	public void setSuppressedScript(String value) {
		suppresedScript = value;
	}

	public String getSuppresedScript() {
		return suppresedScript;
	}

	public boolean isBiDi() throws SAXException, IOException, ParserConfigurationException {
		return LanguageUtils.isBiDi(code);
	}

	public boolean isCJK() {
		return LanguageUtils.isCJK(code);
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}
}
