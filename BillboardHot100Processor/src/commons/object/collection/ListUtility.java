/*
 * File:    ListUtility.java
 * Package: commons.object.collection
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import commons.math.BoundUtility;
import commons.math.MathUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional list functionality.
 */
public final class ListUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ListUtility.class);
    
    
    //Constants
    
    /**
     * The default list class to use when one is not specified.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Class<? extends List<?>> DEFAULT_LIST_CLASS = (Class) ArrayList.class;
    
    
    //Static Methods
    
    /**
     * Creates a new list instance of a certain class.
     *
     * @param clazz The class of the list.
     * @param <T>   The type of the list.
     * @param <L>   The class of the list.
     * @return The list instance.
     */
    public static <T, L extends List<T>> List<T> emptyList(Class<L> clazz) {
        switch (clazz.getSimpleName()) {
            case "ArrayList":
                return new ArrayList<>();
            case "LinkedList":
                return new LinkedList<>();
            case "Stack":
                return new Stack<>();
            case "Vector":
                return new Vector<>();
            default:
                return emptyList();
        }
    }
    
    /**
     * Creates a new list instance.
     *
     * @param <T> The type of the list.
     * @return The list instance.
     * @see #emptyList(Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> emptyList() {
        return emptyList((Class<List<T>>) DEFAULT_LIST_CLASS);
    }
    
    /**
     * Creates a new unmodifiable list from an existing list.
     *
     * @param list The list.
     * @param <T>  The type of the list.
     * @return The unmodifiable list.
     * @see Collections#unmodifiableList(List)
     */
    public static <T> List<T> unmodifiableList(List<T> list) {
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Creates a new unmodifiable list instance of a certain class.
     *
     * @param clazz The class of the list.
     * @param <T>   The type of the list.
     * @param <L>   The class of the list.
     * @return The unmodifiable list instance.
     * @see #emptyList(Class)
     * @see #unmodifiableList(List)
     */
    public static <T, L extends List<T>> List<T> unmodifiableList(Class<L> clazz) {
        return unmodifiableList(emptyList(clazz));
    }
    
    /**
     * Creates a new unmodifiable list instance.
     *
     * @param <T> The type of the list.
     * @return The unmodifiable list instance.
     * @see #unmodifiableList(Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> unmodifiableList() {
        return unmodifiableList((Class<List<T>>) DEFAULT_LIST_CLASS);
    }
    
    /**
     * Creates a new synchronized list from an existing list.
     *
     * @param list The list.
     * @param <T>  The type of the list.
     * @return The synchronized list.
     * @see Collections#synchronizedList(List)
     */
    public static <T> List<T> synchronizedList(List<T> list) {
        return Collections.synchronizedList(list);
    }
    
    /**
     * Creates a new synchronized list instance of a certain class.
     *
     * @param clazz The class of the list.
     * @param <T>   The type of the list.
     * @param <L>   The class of the list.
     * @return The synchronized list instance.
     * @see #emptyList(Class)
     * @see #synchronizedList(List)
     */
    public static <T, L extends List<T>> List<T> synchronizedList(Class<L> clazz) {
        return synchronizedList(emptyList(clazz));
    }
    
    /**
     * Creates a new synchronized list instance.
     *
     * @param <T> The type of the list.
     * @return The synchronized list instance.
     * @see #synchronizedList(Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> synchronizedList() {
        return synchronizedList((Class<List<T>>) DEFAULT_LIST_CLASS);
    }
    
    /**
     * Creates a new list of a certain class, type, and length, filled with a default value or null.
     *
     * @param clazz  The class of the list.
     * @param type   The type of the list.
     * @param fill   The object to fill the list with, or null.
     * @param length The length of the list.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created list.
     * @see #emptyList(Class)
     */
    @SuppressWarnings("unchecked")
    private static <T, L extends List<?>> List<T> create(Class<L> clazz, Class<T> type, T fill, int length) {
        return IntStream.range(0, length).mapToObj(i -> fill)
                .collect(Collectors.toCollection(() -> emptyList((Class<List<T>>) clazz)));
    }
    
    /**
     * Creates a new list of a certain class, type, and length, filled with null.
     *
     * @param clazz  The class of the list.
     * @param type   The type of the list.
     * @param length The length of the list.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created list.
     * @see #create(Class, Class, Object, int)
     */
    public static <T, L extends List<?>> List<T> create(Class<L> clazz, Class<T> type, int length) {
        return create(clazz, type, null, length);
    }
    
    /**
     * Creates a new list of a certain class, type, and length, filled with a default value.
     *
     * @param clazz  The class of the list.
     * @param fill   The object to fill the list with.
     * @param length The length of the list.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created list.
     * @see #create(Class, Class, Object, int)
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<?>> List<T> create(Class<L> clazz, T fill, int length) {
        return create(clazz, (Class<T>) fill.getClass(), fill, length);
    }
    
    /**
     * Creates a new list of a certain class and type.
     *
     * @param clazz The class of the list.
     * @param type  The type of the list.
     * @param <T>   The type of the list.
     * @param <L>   The class of the list.
     * @return The created list.
     * @see #create(Class, Class, int)
     */
    public static <T, L extends List<?>> List<T> create(Class<L> clazz, Class<T> type) {
        return create(clazz, type, 0);
    }
    
    /**
     * Creates a new list of a certain type and length, filled with null.
     *
     * @param type   The type of the list.
     * @param length The length of the list.
     * @param <T>    The type of the list.
     * @return The created list.
     * @see #create(Class, Class, int)
     */
    public static <T> List<T> create(Class<T> type, int length) {
        return create(DEFAULT_LIST_CLASS, type, length);
    }
    
    /**
     * Creates a new list of a certain type and length, filled with a default value.
     *
     * @param fill   The object to fill the list with.
     * @param length The length of the list.
     * @param <T>    The type of the list.
     * @return The created list.
     * @see #create(Class, Object, int)
     */
    public static <T> List<T> create(T fill, int length) {
        return create(DEFAULT_LIST_CLASS, fill, length);
    }
    
    /**
     * Creates a new list of a certain type.
     *
     * @param type The type of the list.
     * @param <T>  The type of the list.
     * @return The created list.
     * @see #create(Class, int)
     */
    public static <T> List<T> create(Class<T> type) {
        return create(DEFAULT_LIST_CLASS, type);
    }
    
    /**
     * Creates a new 2D list of a certain class, type, and dimensions, filled with a default value or null.
     *
     * @param clazz  The class of the list.
     * @param type   The type of the list.
     * @param fill   The object to fill the list with, or null.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created list.
     * @see #create(Class, Class, Object, int)
     */
    @SuppressWarnings("unchecked")
    private static <T, L extends List<?>> List<List<T>> create2D(Class<L> clazz, Class<T> type, T fill, int width, int height) {
        return IntStream.range(0, width).mapToObj(i -> create(clazz, type, fill, height))
                .collect(Collectors.toCollection(() -> emptyList((Class<List<List<T>>>) clazz)));
    }
    
    /**
     * Creates a new 2D list of a certain class, type, and dimensions, filled with null.
     *
     * @param clazz  The class of the list.
     * @param type   The type of the list.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created list.
     * @see #create2D(Class, Class, Object, int, int)
     */
    public static <T, L extends List<?>> List<List<T>> create2D(Class<L> clazz, Class<T> type, int width, int height) {
        return create2D(clazz, type, null, width, height);
    }
    
    /**
     * Creates a new 2D list of a certain class, type, and dimensions, filled with a default value.
     *
     * @param clazz  The class of the list.
     * @param fill   The object to fill the list with.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created list.
     * @see #create2D(Class, Class, Object, int, int)
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<?>> List<List<T>> create2D(Class<L> clazz, T fill, int width, int height) {
        return create2D(clazz, (Class<T>) fill.getClass(), fill, width, height);
    }
    
    /**
     * Creates a new 2D list of a certain class and type.
     *
     * @param clazz The class of the list.
     * @param type  The type of the list.
     * @param <T>   The type of the list.
     * @param <L>   The class of the list.
     * @return The created list.
     * @see #create2D(Class, Class, int, int)
     */
    public static <T, L extends List<?>> List<List<T>> create2D(Class<L> clazz, Class<T> type) {
        return create2D(clazz, type, 0, 0);
    }
    
    /**
     * Creates a new 2D list of a certain type and dimensions, filled with null.
     *
     * @param type   The type of the list.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param <T>    The type of the list.
     * @return The created list.
     * @see #create2D(Class, Class, int, int)
     */
    public static <T> List<List<T>> create2D(Class<T> type, int width, int height) {
        return create2D(DEFAULT_LIST_CLASS, type, width, height);
    }
    
    /**
     * Creates a new 2D list of a certain type and dimensions, filled with a default value.
     *
     * @param fill   The object to fill the list with.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param <T>    The type of the list.
     * @return The created list.
     * @see #create2D(Class, Object, int, int)
     */
    public static <T> List<List<T>> create2D(T fill, int width, int height) {
        return create2D(DEFAULT_LIST_CLASS, fill, width, height);
    }
    
    /**
     * Creates a new 2D list of a certain type.
     *
     * @param type The type of the list.
     * @param <T>  The type of the list.
     * @return The created list.
     * @see #create2D(Class, Class)
     */
    public static <T> List<List<T>> create2D(Class<T> type) {
        return create2D(DEFAULT_LIST_CLASS, type);
    }
    
    /**
     * Creates a new 3D list of a certain class, type, and dimensions, filled with a default value or null.
     *
     * @param clazz  The class of the list.
     * @param type   The type of the list.
     * @param fill   The object to fill the list with, or null.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param depth  The depth of the list.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created list.
     * @see #create2D(Class, Class, Object, int, int)
     */
    @SuppressWarnings({"SuspiciousNameCombination", "unchecked"})
    private static <T, L extends List<?>> List<List<List<T>>> create3D(Class<L> clazz, Class<T> type, T fill, int width, int height, int depth) {
        return IntStream.range(0, width).mapToObj(i -> create2D(clazz, type, fill, height, depth))
                .collect(Collectors.toCollection(() -> emptyList((Class<List<List<List<T>>>>) clazz)));
    }
    
    /**
     * Creates a new 3D list of a certain class, type and dimensions, filled with null.
     *
     * @param clazz  The class of the list.
     * @param type   The type of the list.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param depth  The depth of the list.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created list.
     * @see #create3D(Class, Object, int, int, int)
     */
    public static <T, L extends List<?>> List<List<List<T>>> create3D(Class<L> clazz, Class<T> type, int width, int height, int depth) {
        return create3D(clazz, type, null, width, height, depth);
    }
    
    /**
     * Creates a new 3D list of a certain class, type, and dimensions, filled with a default value.
     *
     * @param clazz  The class of the list.
     * @param fill   The object to fill the list with.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param depth  The depth of the list.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created list.
     * @see #create3D(Class, Class, Object, int, int, int)
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<?>> List<List<List<T>>> create3D(Class<L> clazz, T fill, int width, int height, int depth) {
        return create3D(clazz, (Class<T>) fill.getClass(), fill, width, height, depth);
    }
    
    /**
     * Creates a new 3D list of a certain class and type.
     *
     * @param clazz The class of the list.
     * @param type  The type of the list.
     * @param <T>   The type of the list.
     * @param <L>   The class of the list.
     * @return The created list.
     * @see #create3D(Class, Class, int, int, int)
     */
    public static <T, L extends List<?>> List<List<List<T>>> create3D(Class<L> clazz, Class<T> type) {
        return create3D(clazz, type, 0, 0, 0);
    }
    
    /**
     * Creates a new 3D list of a certain type and dimensions, filled with null.
     *
     * @param type   The type of the list.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param depth  The depth of the list.
     * @param <T>    The type of the list.
     * @return The created list.
     * @see #create3D(Class, Class, int, int, int)
     */
    public static <T> List<List<List<T>>> create3D(Class<T> type, int width, int height, int depth) {
        return create3D(DEFAULT_LIST_CLASS, type, width, height, depth);
    }
    
    /**
     * Creates a new 3D list of a certain type and dimensions, filled with a default value.
     *
     * @param fill   The object to fill the list with.
     * @param width  The width of the list.
     * @param height The height of the list.
     * @param depth  The depth of the list.
     * @param <T>    The type of the list.
     * @return The created list.
     * @see #create3D(Class, Object, int, int, int)
     */
    public static <T> List<List<List<T>>> create3D(T fill, int width, int height, int depth) {
        return create3D(DEFAULT_LIST_CLASS, fill, width, height, depth);
    }
    
    /**
     * Creates a new 3D list of a certain type.
     *
     * @param type The type of the list.
     * @param <T>  The type of the list.
     * @return The created list.
     * @see #create3D(Class, Class)
     */
    public static <T> List<List<List<T>>> create3D(Class<T> type) {
        return create3D(DEFAULT_LIST_CLASS, type);
    }
    
    
    
    
    
    
    
    /**
     * Converts a stream to a new list of a certain type.
     *
     * @param clazz  The class of the list.
     * @param stream The source stream.
     * @param <T>    The type of the list and the source stream.
     * @return The list created from the source stream.
     * @see #create(Class)
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<?>> List<T> asList(Class<L> clazz, Stream<T> stream) {
        return Optional.ofNullable(stream).orElse(Stream.empty())
                .collect(Collectors.toCollection(() -> (List<T>) create(clazz)));
    }
    
    /**
     * Converts a stream to a new list.
     *
     * @param stream The source stream.
     * @param <T>    The type of the list and the source stream.
     * @return The list created from the source stream.
     * @see #asList(Class, Stream)
     */
    public static <T> List<T> asList(Stream<T> stream) {
        return asList(DEFAULT_LIST_CLASS, stream);
    }
    
    /**
     * Converts a collection to a new list of a certain class.
     *
     * @param clazz      The class of the list.
     * @param collection The source collection.
     * @param <T>        The type of the list and the source collection.
     * @param <L>   The class of the list.
     * @return The list created from the source collection.
     * @see #asList(Class, Stream)
     */
    public static <T, L extends List<?>> List<T> asList(Class<L> clazz, Collection<T> collection) {
        return asList(clazz, Stream.ofNullable(collection).flatMap(Collection::stream));
    }
    
    /**
     * Converts a collection to a new list.
     *
     * @param collection The source collection.
     * @param <T>        The type of the list and the source collection.
     * @return The list created from the source collection.
     * @see #asList(Class, Collection)
     */
    public static <T> List<T> asList(Collection<T> collection) {
        return asList(DEFAULT_LIST_CLASS, collection);
    }
    
    
    /**
     * Converts an array to a new list of a certain class.
     *
     * @param clazz The class of the list.
     * @param array The source array.
     * @param <T>   The type of the list and the source array.
     * @param <L>   The class of the list.
     * @return The list created from the source array.
     * @see #asList(Class, Stream)
     */
    public static <T, L extends List<?>> List<T> asList(Class<L> clazz, T[] array) { //TODO test name
        return asList(clazz, Stream.ofNullable(array).flatMap(Arrays::stream));
    }
    
    /**
     * Converts an array to a new list.
     *
     * @param array The source array.
     * @param <T>   The type of the list and the source array.
     * @return The list created from the source array.
     * @see #asList(Class, Object[])
     */
    public static <T> List<T> asList(T[] array) {
        return asList(DEFAULT_LIST_CLASS, array);
    }
    
    
    
    
    
    
    
    
    /**
     * Creates and populates a new list of a certain class.
     *
     * @param clazz    The class of the list.
     * @param elements The elements to populate the list with.
     * @param <T>      The type of the list.
     * @param <L>      The class of the list.
     * @return The created and populated list.
     * @see #asList(Class, Object[])
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<?>> List<T> listOf(Class<L> clazz, T... elements) {
        return asList(clazz, elements);
    }
    
    /**
     * Creates and populates a new list.
     *
     * @param elements The elements to populate the list with.
     * @param <T>      The type of the list.
     * @return The created and populated list.
     * @see #listOf(Class, Object[])
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> listOf(T... elements) {
        return listOf(DEFAULT_LIST_CLASS, elements);
    }
    
    /**
     * Creates and populates a new list of a certain class.
     *
     * @param clazz       The class of the list.
     * @param length      The length of the list.
     * @param indexMapper The function that uses the element indices to produce the elements to populate the list with.
     * @param <T>         The type of the list.
     * @param <L>         The class of the list.
     * @return The created and populated list.
     * @see #asList(Class, Stream)
     */
    public static <T, L extends List<?>> List<T> listOf(Class<L> clazz, int length, IntFunction<? extends T> indexMapper) {
        return asList(clazz, IntStream.range(0, length).mapToObj(indexMapper));
    }
    
    /**
     * Creates and populates a new list.
     *
     * @param length      The length of the list.
     * @param indexMapper The function that uses the element indices to produce the elements to populate the list with.
     * @param <T>         The type of the list.
     * @return The created and populated list.
     * @see #listOf(Class, int, IntFunction)
     */
    public static <T> List<T> listOf(int length, IntFunction<? extends T> indexMapper) {
        return listOf(DEFAULT_LIST_CLASS, length, indexMapper);
    }
    
    /**
     * Creates and populates a new list of a certain class.
     *
     * @param clazz    The class of the list.
     * @param length   The length of the list.
     * @param supplier The supplier that supplies the elements to populate the list with.
     * @param <T>      The type of the list.
     * @param <L>      The class of the list.
     * @return The created and populated list.
     * @see #listOf(Class, int, IntFunction)
     */
    public static <T, L extends List<?>> List<T> listOf(Class<L> clazz, int length, Supplier<? extends T> supplier) {
        return listOf(clazz, length, (i -> supplier.get()));
    }
    
    /**
     * Creates and populates a new list.
     *
     * @param length   The length of the list.
     * @param supplier The supplier that supplies the elements to populate the list with.
     * @param <T>      The type of the list.
     * @return The created and populated list.
     * @see #listOf(Class, int, Supplier)
     */
    public static <T> List<T> listOf(int length, Supplier<? extends T> supplier) {
        return listOf(DEFAULT_LIST_CLASS, length, supplier);
    }
    
    /**
     * Creates and populates a new list of a certain class.
     *
     * @param clazz  The class of the list.
     * @param length The length of the list.
     * @param fill   The value to populate the list with.
     * @param <T>    The type of the list.
     * @param <L>    The class of the list.
     * @return The created and populated list.
     * @see #listOf(Class, int, Supplier)
     */
    public static <T, L extends List<?>> List<T> listOf(Class<L> clazz, int length, T fill) {
        return listOf(clazz, length, (() -> fill));
    }
    
    /**
     * Creates and populates a new list.
     *
     * @param length The length of the list.
     * @param fill   The value to populate the list with.
     * @param <T>    The type of the list.
     * @return The created and populated list.
     * @see #listOf(Class, int, Object)
     */
    public static <T> List<T> listOf(int length, T fill) {
        return listOf(DEFAULT_LIST_CLASS, length, fill);
    }
    
    /**
     * Clones a list.
     *
     * @param list The list.
     * @param <T>  The type of the list.
     * @param <L>  The class of the and the original list.
     * @return The clone of the list.
     * @see #asList(Class, Collection)
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<T>> L clone(L list) {
        return Optional.ofNullable(list)
                .map(e -> (L) asList(e.getClass(), e))
                .orElse(null);
    }
    
    /**
     * Casts a list to a list of a specific class.
     *
     * @param list  The list.
     * @param clazz The list class to cast to.
     * @param <T>   The type of the list and the original list.
     * @param <L>   The list class to cast to.
     * @return The casted list, or the same list if the class is the same as specified.
     * @see #asList(Class, Collection)
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<T>> L cast(List<T> list, Class<L> clazz) {
        return Optional.ofNullable(list)
                .map(e -> (L) (e.getClass().equals(clazz) ? e :
                           asList(e.getClass(), e)))
                .orElse(null);
    }
    
    /**
     * Maps an array to a new list of a certain class.
     *
     * @param clazz  The class of the list.
     * @param array  The source array.
     * @param mapper The function that maps the elements from the source array to the new list.
     * @param <T>    The type of the list.
     * @param <U>    The type of the source array.
     * @param <L>    The class of the list.
     * @return The list mapped from the source array.
     * @see #asList(Class, Stream)
     */
    @SuppressWarnings("unchecked")
    public static <T, U, L extends List<?>> List<T> map(Class<L> clazz, U[] array, Function<? super U, ? extends T> mapper) { //TODO test
        return Optional.ofNullable(array)
                .map(e -> (List<T>) asList(clazz, Arrays.stream(e).map(mapper)))
                .orElse(null);
    }
    
    /**
     * Maps an array to a new list.
     *
     * @param array  The source array.
     * @param mapper The function that maps the elements from the source array to the new list.
     * @param <T>    The type of the list.
     * @param <U>    The type of the source array.
     * @return The list mapped from the source array.
     * @see #map(Class, Object[], Function)
     */
    public static <T, U> List<T> map(U[] array, Function<? super U, ? extends T> mapper) {
        return map(DEFAULT_LIST_CLASS, array, mapper);
    }
    
    /**
     * Maps a collection to a new list of a certain class.
     *
     * @param clazz      The class of the list.
     * @param collection The source collection.
     * @param mapper     The function that maps the elements from the source collection to the new list.
     * @param <T>        The type of the list.
     * @param <U>        The type of the source collection.
     * @param <L>        The class of the list.
     * @return The list mapped from the source collection.
     * @see #asList(Class, Stream)
     */
    @SuppressWarnings("unchecked")
    public static <T, U, L extends List<?>> List<T> map(Class<L> clazz, Collection<U> collection, Function<? super U, ? extends T> mapper) {
        return Optional.ofNullable(collection)
                .map(e -> (List<T>) asList(clazz, e.stream().map(mapper)))
                .orElse(null);
    }
    
    /**
     * Maps a collection to a new list.
     *
     * @param collection The source collection.
     * @param mapper     The function that maps the elements from the source collection to the new list.
     * @param <T>        The type of the list.
     * @param <U>        The type of the source collection.
     * @return The list mapped from the source collection.
     * @see #map(Class, Collection, Function)
     */
    public static <T, U> List<T> map(Collection<U> collection, Function<? super U, ? extends T> mapper) {
        return map(DEFAULT_LIST_CLASS, collection, mapper);
    }
    
    /**
     * Creates a sub list from a list.
     *
     * @param list The list.
     * @param from The index to start the sub list at.
     * @param to   The index to end the sub list at.
     * @param <T>  The type of the list.
     * @param <L>  The class of the list.
     * @return The sub list.
     * @throws IndexOutOfBoundsException When the from or to indices are out of bounds of the list.
     * @see #asList(Class, Collection)
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<T>> L subList(L list, int from, int to) throws IndexOutOfBoundsException {
        return Optional.ofNullable(list)
                .map(e -> (L) asList(e.getClass(), e.subList(from, to)))
                .orElse(null);
    }
    
    /**
     * Creates a sub list from a list.
     *
     * @param list The list.
     * @param from The index to start the sub list at.
     * @param <T>  The type of the list.
     * @param <L>  The class of the list.
     * @return The sub list.
     * @throws IndexOutOfBoundsException When the from or to indices are out of bounds of the list.
     * @see #subList(List, int, int)
     */
    public static <T, L extends List<T>> L subList(L list, int from) throws IndexOutOfBoundsException {
        return Optional.ofNullable(list)
                .map(e -> subList(list, from, list.size()))
                .orElse(null);
    }
    
    /**
     * Merges two lists.
     *
     * @param list1 The first list.
     * @param list2 The second list.
     * @param <T>   The type of the lists.
     * @param <L>   The class of the list.
     * @return The merged list.
     * @see #asList(Class, Stream)
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<T>> L merge(L list1, List<? extends T> list2) {
        return Optional.ofNullable(list1)
                .map(e -> (L) asList(e.getClass(), 
                        Stream.concat(Stream.ofNullable(list1), Stream.ofNullable(list2)).flatMap(Collection::stream)))
                .orElse(null);
    }
    
    /**
     * Splits a list into a list of lists of a certain length.
     *
     * @param list   The list.
     * @param length The length of the resulting lists.
     * @param <T>    The type of the list.
     * @return The list of lists of the specified length.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<List<T>> split(List<T> list, int length) {
        final int split = BoundUtility.truncateNum(length, 1, list.size()).intValue();
        final List<List<T>> result = create2D(list.getClass(), (Class<T>) list.getClass(), (int) Math.ceil(list.size() / (double) split), split);
        IntStream.range(0, list.size()).forEach(i ->
                result.get(i / split).set((i % split), list.get(i)));
        return result;
    }
    
    /**
     * Reverses a list.
     *
     * @param list The list.
     * @param <T>  The type of the list.
     * @param <L>  The class of the list.
     * @return The reversed list.
     */
    public static <T, L extends List<T>> L reverse(L list) {
        final L reversed = clone(list);
        Collections.reverse(reversed);
        return reversed;
    }
    
    /**
     * Shuffles a list.
     *
     * @param list The list.
     * @param <T>  The type of the list.
     * @param <L>  The class of the list.
     * @return The shuffled list.
     */
    public static <T, L extends List<T>> L shuffle(L list) {
        final L shuffled = clone(list);
        Collections.shuffle(shuffled);
        return shuffled;
    }
    
    /**
     * Determines if a list is null or empty.
     *
     * @param list The list.
     * @param <T>  The type of the list.
     * @return Whether the list is null or empty.
     */
    public static <T> boolean isNullOrEmpty(List<T> list) {
        return (list == null) || list.isEmpty();
    }
    
    /**
     * Determines if a list equals another list.
     *
     * @param list1      The first list.
     * @param list2      The second list.
     * @param checkOrder Whether to check the order of the lists or not.
     * @return Whether the lists are equal or not.
     */
    public static boolean equals(List<?> list1, List<?> list2, boolean checkOrder) {
        return ((list1 == null) || (list2 == null)) ? ((list1 == null) && (list2 == null)) : ((list1.size() == list2.size()) &&
                (checkOrder ? IntStream.range(0, list1.size()).allMatch(i -> Objects.equals(list1.get(i), list2.get(i))) :
                 list1.stream().allMatch(e -> list2.contains(e) && (numberOfOccurrences(list1, e) == numberOfOccurrences(list2, e)))));
    }
    
    /**
     * Determines if a list equals another list.
     *
     * @param list1 The first list.
     * @param list2 The second list.
     * @return Whether the lists are equal or not.
     * @see #equals(List, List, boolean)
     */
    public static boolean equals(List<?> list1, List<?> list2) {
        return equals(list1, list2, true);
    }
    
    /**
     * Determines if a list of strings equals another list of strings, regardless of case.
     *
     * @param list1      The first list.
     * @param list2      The second list.
     * @param checkOrder Whether to check the order of the lists or not.
     * @return Whether the lists of strings are equal or not, regardless of case.
     * @see #equals(List, List, boolean)
     */
    public static boolean equalsIgnoreCase(List<String> list1, List<String> list2, boolean checkOrder) {
        return ((list1 == null) || (list2 == null)) ? ((list1 == null) && (list2 == null)) : ((list1.size() == list2.size()) &&
                equals(map(list1, String::toUpperCase), map(list2, String::toUpperCase)));
    }
    
    /**
     * Determines if a list of strings equals another list of strings, regardless of case.
     *
     * @param list1 The first list.
     * @param list2 The second list.
     * @return Whether the lists of strings are equal or not, regardless of case.
     * @see #equalsIgnoreCase(List, List, boolean)
     */
    public static boolean equalsIgnoreCase(List<String> list1, List<String> list2) {
        return equalsIgnoreCase(list1, list2, true);
    }
    
    /**
     * Adds an element to a list and returns the list.
     *
     * @param list    The list.
     * @param element The element.
     * @param <T>     The type of the list.
     * @param <L>     The class of the list.
     * @return The list.
     */
    public static <T, L extends List<T>> L addAndGet(L list, T element) {
        list.add(element);
        return list;
    }
    
    /**
     * Adds an element to a list and returns the list.
     *
     * @param list    The list.
     * @param index   The index to add the element at.
     * @param element The element.
     * @param <T>     The type of the list.
     * @param <L>     The class of the list.
     * @return The list.
     */
    public static <T, L extends List<T>> L addAndGet(L list, int index, T element) {
        list.add(index, element);
        return list;
    }
    
    /**
     * Adds a collection of elements to a list and returns the list.
     *
     * @param list     The list.
     * @param elements The collection of elements.
     * @param <T>      The type of the list.
     * @param <L>      The class of the list.
     * @return The list.
     */
    public static <T, L extends List<T>> L addAllAndGet(L list, Collection<? extends T> elements) {
        list.addAll(elements);
        return list;
    }
    
    /**
     * Adds a collection of elements to a list and returns the list.
     *
     * @param list     The list.
     * @param index    The index to add the elements at.
     * @param elements The collection of elements.
     * @param <T>      The type of the list.
     * @param <L>      The class of the list.
     * @return The list.
     */
    public static <T, L extends List<T>> L addAllAndGet(L list, int index, Collection<? extends T> elements) {
        list.addAll(index, elements);
        return list;
    }
    
    /**
     * Sets an element in a list and returns the list.
     *
     * @param list    The list.
     * @param index   The index in the list.
     * @param element The element.
     * @param <T>     The type of the list.
     * @param <L>     The class of the list.
     * @return The list.
     */
    public static <T, L extends List<T>> L setAndGet(L list, int index, T element) {
        list.set(index, element);
        return list;
    }
    
    /**
     * Removes an element from a list and returns the list.
     *
     * @param list    The list.
     * @param element The element.
     * @param <T>     The type of the list.
     * @param <L>     The class of the list.
     * @return The list.
     */
    public static <T, L extends List<T>> L removeAndGet(L list, T element) {
        list.remove(element);
        return list;
    }
    
    /**
     * Removes an element from a list and returns the list.
     *
     * @param list  The list.
     * @param index The index of the element.
     * @param <T>   The type of the list.
     * @param <L>   The class of the list.
     * @return The list.
     */
    public static <T, L extends List<T>> L removeAndGet(L list, int index) {
        list.remove(index);
        return list;
    }
    
    /**
     * Removes a collection of elements from a list and returns the list.
     *
     * @param list     The list.
     * @param elements The collection of elements.
     * @param <T>      The type of the list.
     * @param <L>      The class of the list.
     * @return The list.
     */
    public static <T, L extends List<T>> L removeAllAndGet(L list, Collection<? extends T> elements) {
        list.removeAll(elements);
        return list;
    }
    
    /**
     * Determines if a list contains an element.
     *
     * @param list    The list.
     * @param element The element.
     * @return Whether the list contains the specified element or not.
     */
    public static boolean contains(List<?> list, Object element) {
        return !isNullOrEmpty(list) && list.contains(element);
    }
    
    /**
     * Determines if a list of strings contains an element, regardless of case.
     *
     * @param list    The list.
     * @param element The element.
     * @return Whether the list contains the specified string or not, regardless of case.
     * @see #contains(List, Object)
     */
    public static boolean containsIgnoreCase(List<String> list, String element) {
        return !isNullOrEmpty(list) && contains(map(list, String::toUpperCase), element.toUpperCase());
    }
    
    /**
     * Determines if a list contains any of a set of elements.
     *
     * @param list     The list.
     * @param elements The elements.
     * @return Whether the list contains any of the specified elements or not.
     */
    public static boolean containsAny(List<?> list, Object... elements) { //TODO test
        return !isNullOrEmpty(list) && Arrays.stream(elements).anyMatch(list::contains);
    }
    
    /**
     * Determines the number of occurrences of an element in a list.
     *
     * @param list    The list.
     * @param element The element.
     * @param <T>     The type of the list.
     * @return The number of occurrences of the specified element in the list.
     */
    public static <T> int numberOfOccurrences(List<? extends T> list, T element) {
        return isNullOrEmpty(list) ? 0 :
               (int) list.stream().filter(e -> Objects.equals(e, element)).count();
    }
    
    /**
     * Determines the number of occurrences of a string element in a list, regardless of case.
     *
     * @param list    The list.
     * @param element The element.
     * @return The number of occurrences of the specified string element in the list, regardless of case.
     */
    public static int numberOfOccurrencesIgnoreCase(List<String> list, String element) {
        return isNullOrEmpty(list) ? 0 :
               (int) list.stream().filter(e -> StringUtility.equalsIgnoreCase(e, element)).count();
    }
    
    /**
     * Returns the index of an element in a list.
     *
     * @param list    The list.
     * @param element The element.
     * @param <T>     The type of the list.
     * @return The index of the element in the list, or -1 if it does not exist.
     */
    public static <T> int indexOf(List<? extends T> list, T element) {
        return isNullOrEmpty(list) ? -1 :
               list.indexOf(element);
    }
    
    /**
     * Returns the index of a string in a list, regardless of case.
     *
     * @param list    The list.
     * @param element The element.
     * @return The index of the string in the list, regardless of case, or -1 if it does not exist.
     */
    public static int indexOfIgnoreCase(List<String> list, String element) {
        return isNullOrEmpty(list) ? -1 :
               IntStream.range(0, list.size())
                       .filter(i -> StringUtility.equalsIgnoreCase(list.get(i), element))
                       .findFirst().orElse(-1);
    }
    
    /**
     * Returns an element from a list at a specified index, or a default value if the index is invalid.
     *
     * @param list         The list.
     * @param index        The index.
     * @param defaultValue The default value.
     * @param <T>          The type of the list.
     * @return The element in the list at the specified index, or the default value if the index is invalid.
     */
    public static <T> T getOrDefault(List<T> list, int index, T defaultValue) {
        return (!isNullOrEmpty(list) && BoundUtility.inListBounds(index, list)) ?
               list.get(index) : defaultValue;
    }
    
    /**
     * Returns an element from a list at a specified index, or null if the index is invalid.
     *
     * @param list  The list.
     * @param index The index.
     * @param <T>   The type of the list.
     * @return The element in the list at the specified index, or null if the index is invalid.
     * @see #getOrDefault(List, int, Object)
     */
    public static <T> T getOrNull(List<T> list, int index) {
        return getOrDefault(list, index, null);
    }
    
    /**
     * Determines if any element in a list is null.
     *
     * @param list The list.
     * @return Whether or not any element in the list is null.
     */
    public static boolean anyNull(List<?> list) {
        return list.stream().anyMatch(Objects::isNull);
    }
    
    /**
     * Determines if any element in a list is null.
     *
     * @param list The list.
     * @return Whether or not any element in the list is null.
     * @see #anyNull(List)
     */
    public static boolean anyNull(Object... list) {
        return anyNull(Arrays.asList(list));
    }
    
    /**
     * Removes null elements from a list.
     *
     * @param list The list.
     * @param <T>  The type of the list.
     * @param <L>  The class of the list.
     * @return The list with null elements removed.
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<T>> L removeNull(L list) {
        return (L) listOf(list.getClass(), list.stream().filter(Objects::nonNull).toArray());
    }
    
    /**
     * Removes duplicate elements from a list.
     *
     * @param list The list to operate on.
     * @param <T>  The type of the list.
     * @param <L>  The class of the list.
     * @return The list with duplicate elements removed.
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<T>> L removeDuplicates(L list) {
        return (L) listOf(list.getClass(), list.stream().distinct().toArray());
    }
    
    /**
     * Selects a random element from a list.
     *
     * @param list The list to select from.
     * @param <T>  The type of the list.
     * @return A random element from the list.
     */
    public static <T> T selectRandom(List<T> list) {
        return isNullOrEmpty(list) ? null :
               list.get(MathUtility.random(list.size() - 1));
    }
    
    /**
     * Selects a random subset of a list.
     *
     * @param list The list to select from.
     * @param n    The number of elements to select.
     * @param <T>  The type of the list.
     * @param <L>  The class of the list.
     * @return A random subset of the list.
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<T>> L selectN(L list, int n) {
        return (L) (isNullOrEmpty(list) ? emptyList() :
                    (n <= 0) ? emptyList(list.getClass()) :
                    (n > list.size()) ? shuffle(list) :
                    subList(shuffle(list), 0, n));
    }
    
    /**
     * Copies a list to the end of itself a number of times making a list n times the original length.
     *
     * @param list  The list to duplicate.
     * @param times The number of copies of the list to add.
     * @param <T>   The type of the list.
     * @param <L>   The class of the list.
     * @return A list of double size with duplicated elements.
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<T>> L duplicateInOrder(L list, int times) {
        return (L) (isNullOrEmpty(list) ? emptyList() :
                    (times <= 0) ? emptyList(list.getClass()) :
                    (times == 1) ? clone(list) :
                    listOf(list.getClass(), listOf(times, list).stream().flatMap(Collection::stream).toArray()));
    }
    
    /**
     * Copies a list to the end of itself making a list double the original length.
     *
     * @param list The list to duplicate.
     * @param <T>  The type of the list.
     * @param <L>  The class of the list.
     * @return A list of double size with duplicated elements.
     * @see #duplicateInOrder(List, int)
     */
    public static <T, L extends List<T>> L duplicateInOrder(L list) {
        return duplicateInOrder(list, 2);
    }
    
    /**
     * Sorts a list by the number of occurrences of each entry in the list.
     *
     * @param list    The list to sort.
     * @param reverse Whether to sort in reverse or not.
     * @param <T>     The type of the list.
     * @param <L>     The class of the list.
     * @return The list sorted by the number of occurrences of each entry in the list.
     */
    @SuppressWarnings("unchecked")
    public static <T, L extends List<T>> L sortByNumberOfOccurrences(L list, boolean reverse) {
        return (L) (isNullOrEmpty(list) ? emptyList() :
                    listOf(list.getClass(), list.stream()
                            .collect(Collectors.groupingBy(Function.identity())).values().stream()
                            .sorted(Comparator.comparingInt(e -> (e.size() * (reverse ? 1 : -1))))
                            .flatMap(Collection::stream).toArray()));
    }
    
    /**
     * Sorts a list by the number of occurrences of each entry in the list.
     *
     * @param list The list to sort.
     * @param <T>  The type of the list.
     * @param <L>  The class of the list.
     * @return The list sorted by the number of occurrences of each entry in the list.
     * @see #sortByNumberOfOccurrences(List, boolean)
     */
    public static <T, L extends List<T>> L sortByNumberOfOccurrences(L list) {
        return sortByNumberOfOccurrences(list, false);
    }
    
}
