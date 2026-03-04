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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class OfficeParser {
    
    private Map<String, String> languageMap;
    
    public OfficeParser() throws IOException {
        languageMap = new HashMap<>();        
        URL url = RegistryParser.class.getResource("Office.txt");
        loadMap(url);
    }

    public String getLCID(String lang) {
        return languageMap.containsKey(lang) ? languageMap.get(lang) : "";
    }

    public boolean isSupported(String lang) {
        return !getLCID(lang).isEmpty(); 
    }

    private void loadMap(URL url) throws IOException {
        try (InputStream input = url.openStream()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_16LE))) {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\t");
                    languageMap.put(parts[1], parts[0]);
                }
            }
        }
    }
}
