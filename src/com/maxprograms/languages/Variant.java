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

public class Variant implements Comparable<Variant> {

	private String code;
	private String description;
	private String prefix;

	public Variant(String code, String description, String prefix) {
		this.code = code;
		this.description = description;
		this.prefix = prefix;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public String getPrefix() {
		return prefix;
	}

	@Override
	public int compareTo(Variant arg0) {
		return description.compareTo(arg0.getDescription());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Variant variant) {
			return code.equals(variant.getCode()) && description.equals(variant.getDescription()) && prefix.equals(variant.getPrefix());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return code.hashCode();
	}
}
