/*******************************************************************************
 * Copyright (c) 2022-2026 Maxprograms. All rights reserved.
 *
 * This software is the proprietary property of Maxprograms.
 * Use, modification, and distribution are subject to the terms of the 
 * Software License Agreement found in the root of this distribution 
 * and at http://www.maxprograms.com/
 *
 * Unauthorized redistribution or commercial use is strictly prohibited.
 *******************************************************************************/
package com.maxprograms.languages;

public class Script implements Comparable<Script> {

	private String code;
	private String description;

	public Script(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public int compareTo(Script arg0) {
		return description.compareTo(arg0.getDescription());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Script script) {
			return code.equals(script.getCode()) && description.equals(script.getDescription());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}
}
