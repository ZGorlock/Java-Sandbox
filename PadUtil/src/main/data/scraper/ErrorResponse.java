/*
 * File:    ErrorResponse.java
 * Package: main.data.scraper
 * Author:  Zachary Gill
 */

package main.data.scraper;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface ErrorResponse<T> extends Function<String, Supplier<T>> {
    
    //Methods
    
    Supplier<T> respond(String errorMessage);
    
    @Override
    default Supplier<T> apply(String errorMessage) {
        return invoke(null, errorMessage);
    }
    
    
    //Static Methods
    
    static <T> Supplier<T> invoke(T response, String errorMessage) {
        return () -> {
            Optional.ofNullable(errorMessage)
                    .filter(e -> !e.isBlank())
                    .ifPresent(System.err::println);
            return response;
        };
    }
    
    static <T> Supplier<T> invoke(String errorMessage) {
        return invoke(null, errorMessage);
    }
    
    static <T> Supplier<T> invoke() {
        return invoke(null);
    }
    
}
