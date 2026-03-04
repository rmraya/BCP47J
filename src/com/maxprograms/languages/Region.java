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

public class Region implements Comparable<Region> {

	private String code;
	private String description;

	public Region(String code, String description) {
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
	public int compareTo(Region arg0) {
		return description.compareTo(arg0.getDescription());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Region reg) {
			return code.equals(reg.getCode()) && description.equals(reg.getDescription());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}
}
