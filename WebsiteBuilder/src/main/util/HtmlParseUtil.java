/*
 * File:    HtmlParseUtil.java
 * Package: main.util
 * Author:  Zachary Gill
 */

package main.util;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import main.util.persistence.VariableUtil;
import org.jsoup.nodes.Element;

public final class HtmlParseUtil {
    
    //Static Methods
    
    public static Optional<Element> selectFirst(Element root, String selector) {
        return Optional.ofNullable(root)
                .map(e -> e.selectFirst(selector));
    }
    
    public static Optional<Element> selectFirst(Element root, Integer selectorVariable) {
        return Optional.ofNullable(selectorVariable)
                .map(VariableUtil::get)
                .flatMap(e -> selectFirst(root, e));
    }
    
    public static Optional<String> text(Element root, String selector) {
        return selectFirst(root, selector)
                .map(Element::text);
    }
    
    public static Optional<String> text(Element root, Integer selectorVariable) {
        return Optional.ofNullable(selectorVariable)
                .map(VariableUtil::get)
                .flatMap(e -> text(root, e));
    }
    
    public static Optional<String> attr(Element root, String selector, String attr) {
        return selectFirst(root, selector)
                .map(e -> e.attr(attr));
    }
    
    public static Optional<String> attr(Element root, Integer selectorVariable, String attr) {
        return Optional.ofNullable(selectorVariable)
                .map(VariableUtil::get)
                .flatMap(e -> attr(root, e, attr));
    }
    
    public static Stream<Element> select(Element root, String selector) {
        return Optional.ofNullable(root)
                .map(e -> e.select(selector))
                .stream().flatMap(Collection::stream);
    }
    
    public static Stream<Element> select(Element root, Integer selectorVariable) {
        return Optional.ofNullable(selectorVariable)
                .map(VariableUtil::get)
                .map(e -> select(root, e))
                .orElseGet(Stream::empty);
    }
    
}
