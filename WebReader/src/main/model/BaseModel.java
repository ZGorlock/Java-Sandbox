/*
 * File:    BaseModel.java
 * Package: main.model
 * Author:  Zachary Gill
 */

package main.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import commons.access.Internet;
import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public abstract class BaseModel {
    
    //Fields
    
    public String url;
    
    protected Page page;
    
    
    //Constructors
    
    protected BaseModel(String url) {
        this.url = url;
        this.page = new Page(url);
        
        processPage();
        releasePage();
    }
    
    protected BaseModel() {
    }
    
    
    //Methods
    
    protected void processPage() {
    }
    
    protected final void releasePage() {
        page.release();
    }
    
    @SuppressWarnings("unchecked")
    protected List<List<Map.Entry<String, Optional<String>>>> getDisplayFields() {
        return ListUtility.listOf(List.of(
                Map.entry("URL", Optional.ofNullable(url))));
    }
    
    protected String formatDisplayFields(List<List<Map.Entry<String, Optional<String>>>> displayFields) {
        final int maxFieldLength = displayFields.stream().flatMap(Collection::stream).map(Map.Entry::getKey).mapToInt(String::length).max().orElse(0);
        return displayFields.stream().flatMap(Collection::stream)
                .map(e -> String.join("",
                        //StringUtility.spaces(e.getKey().isBlank() ? 0 : 4),
                        e.getKey() + (e.getKey().isBlank() ? "" : ": "),
                        StringUtility.spaces(e.getKey().isBlank() ? 0 : (maxFieldLength - e.getKey().length())),
                        e.getValue().orElse("N/A"))
                ).collect(Collectors.joining(System.lineSeparator()));
    }
    
    @Override
    public String toString() {
        return formatDisplayFields(getDisplayFields());
    }
    
    protected Pattern getUrlPattern(String pageId, boolean allowSubPage, String idPattern) {
        return Pattern.compile("^" +
                "(?<baseUrl>(?<protocol>.*)://(?<domain>[^/]+))" +
                "(?<subDomain>(?:/[^/?#].+)*)/" +
                "(?<page>" + Pattern.quote(pageId) + ")" +
                "(?<subPage>" + (allowSubPage ? "(?:/[^/?#]*)*" : "") + ")/" +
                "(?<id>" + idPattern + ")" +
                "(?<parameters>[/?#].*)?" +
                "$");
    }
    
    protected Pattern getUrlPattern(String pageId, boolean allowSubPage) {
        return getUrlPattern(pageId, allowSubPage, "[^/#?]+");
    }
    
    protected Pattern getUrlPattern(String pageId) {
        return getUrlPattern(pageId, false);
    }
    
    
    //Inner Classes
    
    protected static class Page extends PageElement {
        
        //Fields
        
        public String url;
        
        public Document document;
        
        
        //Constructors
        
        public Page(String url) {
            this.url = url;
            
            load();
        }
        
        
        //Methods
        
        public void load() {
            this.document = Internet.getHtml(url);
            this.element = document;
        }
        
        public void release() {
            this.document = null;
            this.element = null;
        }
        
    }
    
    protected static class PageElement {
        
        //Fields
        
        public Element element;
        
        
        //Constructors
        
        public PageElement(Element element) {
            this.element = element;
        }
        
        private PageElement() {
        }
        
        
        //Methods
        
        /**
         * Find element.
         */
        public PageElement getElement(BiFunction<Element, String, Elements> elementSearch, List<String> elementSearchArgumentOptions, BiFunction<Element, String, Elements> elementSubSearch, String elementSubSearchArgument) {
            return elementSearchArgumentOptions.stream().sequential()
                    .map(s -> elementSearch.apply(element, s)).flatMap(Collection::stream)
                    .map(e -> Optional.ofNullable(elementSubSearchArgument).map(s -> elementSubSearch.apply(e, s)).orElse(new Elements(e))).flatMap(Collection::stream)
                    .filter(Objects::nonNull).filter(e -> !StringUtility.isNullOrBlank(e.text())).findFirst()
                    .map(PageElement::new).orElse(null);
        }
        
        public PageElement getElement(BiFunction<Element, String, Elements> elementSearch, String elementSearchArgument, BiFunction<Element, String, Elements> elementSubSearch, String elementSubSearchArgument) {
            return getElement(elementSearch, List.of(elementSearchArgument), elementSubSearch, elementSubSearchArgument);
        }
        
        public PageElement getElement(BiFunction<Element, String, Elements> elementSearch, List<String> elementSearchArgumentOptions) {
            return getElement(elementSearch, elementSearchArgumentOptions, (e, s) -> new Elements(e), null);
        }
        
        public PageElement getElement(BiFunction<Element, String, Elements> elementSearch, String elementSearchArgument) {
            return getElement(elementSearch, List.of(elementSearchArgument));
        }
        
        public PageElement getElement(BiFunction<Element, String, Elements> elementSearch, List<String> elementSearchArgumentOptions, String elementSubSearchArgument) {
            return getElement(elementSearch, elementSearchArgumentOptions, elementSearch, elementSubSearchArgument);
        }
        
        public PageElement getElement(BiFunction<Element, String, Elements> elementSearch, String elementSearchArgument, String elementSubSearchArgument) {
            return getElement(elementSearch, List.of(elementSearchArgument), elementSubSearchArgument);
        }
        
        /**
         * Find by element id.
         */
        public PageElement getElementById(List<String> elementIdOptions, String subElementId) {
            return getElement((e, s) -> new Elements(e.getElementById(s)), elementIdOptions, subElementId);
        }
        
        public PageElement getElementById(String elementId, String subElementId) {
            return getElementById(List.of(elementId), subElementId);
        }
        
        public PageElement getElementById(List<String> elementIdOptions) {
            return getElementById(elementIdOptions, null);
        }
        
        public PageElement getElementById(String elementId) {
            return getElementById(elementId, null);
        }
        
        /**
         * Find by element class.
         */
        public PageElement getElementByClass(List<String> elementClassOptions, String subElementClass) {
            return getElement(Element::getElementsByClass, elementClassOptions, subElementClass);
        }
        
        public PageElement getElementByClass(String elementClass, String subElementClass) {
            return getElementByClass(List.of(elementClass), subElementClass);
        }
        
        public PageElement getElementByClass(List<String> elementClassOptions) {
            return getElementByClass(elementClassOptions, null);
        }
        
        public PageElement getElementByClass(String elementClass) {
            return getElementByClass(elementClass, null);
        }
        
        /**
         * Find by element tag.
         */
        public PageElement getElementByTag(List<String> elementTagOptions) {
            return getElement(Element::getElementsByTag, elementTagOptions);
        }
        
        public PageElement getElementByTag(String elementTag) {
            return getElementByTag(List.of(elementTag));
        }
        
        /**
         * Find by element attribute.
         */
        public PageElement getElementByAttribute(List<String> elementAttributeOptions) {
            return getElement(Element::getElementsByAttribute, elementAttributeOptions);
        }
        
        public PageElement getElementByAttribute(String elementAttribute) {
            return getElementByAttribute(List.of(elementAttribute));
        }
        
        /**
         * Find by element attribute value.
         */
        public PageElement getElementByAttributeValue(List<String> elementAttributeOptions, String elementAttributeValue) {
            return getElement((e, s) -> e.getElementsByAttributeValue(s, elementAttributeValue), elementAttributeOptions);
        }
        
        public PageElement getElementByAttributeValue(String elementAttribute, String elementAttributeValue) {
            return getElementByAttributeValue(List.of(elementAttribute), elementAttributeValue);
        }
        
        /**
         * Find element text.
         */
        public String getText(BiFunction<Element, String, Elements> elementSearch, List<String> elementSearchArgumentOptions, BiFunction<Element, String, Elements> elementSubSearch, String elementSubSearchArgument) {
            return Optional.ofNullable(getElement(elementSearch, elementSearchArgumentOptions, elementSubSearch, elementSubSearchArgument))
                    .map(e -> e.element).map(Element::text).map(String::strip).orElse(null);
        }
        
        public String getText(BiFunction<Element, String, Elements> elementSearch, String elementSearchArgument, BiFunction<Element, String, Elements> elementSubSearch, String elementSubSearchArgument) {
            return getText(elementSearch, List.of(elementSearchArgument), elementSubSearch, elementSubSearchArgument);
        }
        
        public String getText(BiFunction<Element, String, Elements> elementSearch, List<String> elementSearchArgumentOptions) {
            return getText(elementSearch, elementSearchArgumentOptions, (e, s) -> new Elements(e), null);
        }
        
        public String getText(BiFunction<Element, String, Elements> elementSearch, String elementSearchArgument) {
            return getText(elementSearch, List.of(elementSearchArgument));
        }
        
        public String getText(BiFunction<Element, String, Elements> elementSearch, List<String> elementSearchArgumentOptions, String elementSubSearchArgument) {
            return getText(elementSearch, elementSearchArgumentOptions, elementSearch, elementSubSearchArgument);
        }
        
        public String getText(BiFunction<Element, String, Elements> elementSearch, String elementSearchArgument, String elementSubSearchArgument) {
            return getText(elementSearch, List.of(elementSearchArgument), elementSubSearchArgument);
        }
        
        /**
         * Find text by element id.
         */
        public String getTextById(List<String> elementIdOptions, String subElementId) {
            return getText((e, s) -> new Elements(e.getElementById(s)), elementIdOptions, subElementId);
        }
        
        public String getTextById(String elementId, String subElementId) {
            return getTextById(List.of(elementId), subElementId);
        }
        
        public String getTextById(List<String> elementIdOptions) {
            return getTextById(elementIdOptions, null);
        }
        
        public String getTextById(String elementId) {
            return getTextById(elementId, null);
        }
        
        /**
         * Find text by element class.
         */
        public String getTextByClass(List<String> elementClassOptions, String subElementClass) {
            return getText(Element::getElementsByClass, elementClassOptions, subElementClass);
        }
        
        public String getTextByClass(String elementClass, String subElementClass) {
            return getTextByClass(List.of(elementClass), subElementClass);
        }
        
        public String getTextByClass(List<String> elementClassOptions) {
            return getTextByClass(elementClassOptions, null);
        }
        
        public String getTextByClass(String elementClass) {
            return getTextByClass(elementClass, null);
        }
        
        /**
         * Find text by element tag.
         */
        public String getTextByTag(List<String> elementTagOptions) {
            return getText(Element::getElementsByTag, elementTagOptions);
        }
        
        public String getTextByTag(String elementTag) {
            return getTextByTag(List.of(elementTag));
        }
        
        /**
         * Find text by element attribute.
         */
        public String getTextByAttribute(List<String> elementAttributeOptions) {
            return getText(Element::getElementsByAttribute, elementAttributeOptions);
        }
        
        public String getTextByAttribute(String elementAttribute) {
            return getTextByAttribute(List.of(elementAttribute));
        }
        
        /**
         * Find text by element attribute value.
         */
        public String getTextByAttributeValue(List<String> elementAttributeOptions, String elementAttributeValue) {
            return getText((e, s) -> e.getElementsByAttributeValue(s, elementAttributeValue), elementAttributeOptions);
        }
        
        public String getTextByAttributeValue(String elementAttribute, String elementAttributeValue) {
            return getTextByAttributeValue(List.of(elementAttribute), elementAttributeValue);
        }
        
        private Optional<String> formatTextAsNumber(String text, boolean decimal) {
            return Optional.ofNullable(text)
                    .map(e -> e.replaceAll("[^\\s\\d.]", ""))
                    .map(String::strip)
                    .map(e -> e.replaceAll(("[\\s" + (decimal ? "" : ".") + "].+$"), ""));
        }
        
        public Integer textToInt(String text) {
            return formatTextAsNumber(text, false)
                    .map(Integer::parseInt).orElse(null);
        }
        
        public Long textToLong(String text) {
            return formatTextAsNumber(text, false)
                    .map(Long::parseLong).orElse(null);
        }
        
        public Float textToFloat(String text) {
            return formatTextAsNumber(text, true)
                    .map(Float::parseFloat).orElse(null);
        }
        
        public Double textToDouble(String text) {
            return formatTextAsNumber(text, true)
                    .map(Double::parseDouble).orElse(null);
        }
        
        public List<String> textToList(String text, String deliminator) {
            return Optional.ofNullable(text)
                    .map(e -> e.split("\\s*" + Pattern.quote(deliminator) + "\\s*"))
                    .map(Arrays::asList).orElse(null);
        }
        
    }
    
}
