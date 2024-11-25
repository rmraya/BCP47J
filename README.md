# BCP47J

A library written in Java for handling Language Tags in compliance with [BCP47](https://www.ietf.org/rfc/bcp/bcp47.txt).

This library is part of [OpenXLIFF Filters](https://github.com/rmraya/OpenXLIFF)

## Usage

Use the static methods provided by class `com.maxprograms.languages.LanguageUtils`

| Method | Description |
| --- | --- |
| `List<Language> getAllLanguages()` | Returns a list of all languages from BCP47 |
| `List<Language> getCommonLanguages()` | Returns a list of most common languages |
| `Language getLanguage(String code)` | Returns a language given its code |
| `Language languageFromName(String description)` | Returns a language given its description |
| `String normalizeCode(String code)` | Normalizes a language code to the format described in BCP47 |
| `boolean isBiDi(String code)` | Returns true if the language is written right-to-left |
| `boolean isCJK(String code)` | Returns true if the language is written in CJK script |
| `String[] getLanguageNames()` | Returns an array with the names of most common languages |

### Localization

This library includes lists of languages with their names in English and Spanish.

Set `Locale` to `en` (default) or `es` to get the names in English or Spanish respectively.

```java

Locale.setDefault(Locale.forLanguageTag("es"));
String[] names = LanguageUtils.getLanguageNames();

```

Contatct <tech@maxprograms.com> to localize the names to other languages.

### Example

```java
import com.maxprograms.languages.Language;
import com.maxprograms.languages.LanguageUtils;

...

boolean isValidLanguage(String code) {
    return LanguageUtils.getLanguage(code) != null;
}

```
