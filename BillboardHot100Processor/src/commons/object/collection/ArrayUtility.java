/*
 * File:    ArrayUtility.java
 * Package: commons.object.collection
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.collection;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
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
 * A resource class that provides additional array functionality.
 */
public final class ArrayUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ArrayUtility.class);
    
    
    //Constants

    /**
     * The default array type to use when one is not specified.
     */
    private static final Class<?> DEFAULT_ARRAY_TYPE = Object.class;
    
    
    //Static Methods
    
    /**
     * Creates a new array instance of a certain type and length.
     *
     * @param type The type of the array.
     * @param length The length of the array.
     * @param <T>  The type of the array.
     * @return The array instance.
     * @see Array#newInstance(Class, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] emptyArray(Class<T> type, int length) { //TODO test
        return (T[]) Array.newInstance(type, length);
    }
    
    /**
     * Creates a new array instance of a certain type.
     *
     * @param type The type of the array.
     * @param <T>  The type of the array.
     * @return The array instance.
     * @see #emptyArray(Class, int)
     */
    public static <T> T[] emptyArray(Class<T> type) {
        return emptyArray(type, 0);
    }
    
    /**
     * Creates a new array instance of a certain length.
     *
     * @param length The length of the array.
     * @param <T>  The type of the array.
     * @return The array instance.
     * @see #emptyArray(Class, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] emptyArray(int length) {
        return emptyArray((Class<T>) DEFAULT_ARRAY_TYPE, length);
    }
    
    /**
     * Creates a new array instance
     *
     * @param <T>  The type of the array.
     * @return The array instance.
     * @see #emptyArray(int)
     */
    public static <T> T[] emptyArray() {
        return emptyArray(0);
    }
    
    
    
    
    
    
    /**
     * Creates a new array of a certain type and length, filled with a default value or null.
     *
     * @param type   The type of the array.
     * @param fill   The object to fill the array with, or null.
     * @param length The length of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #emptyArray(Class, int) 
     */
    private static <T> T[] create(Class<T> type, T fill, int length) {
        return IntStream.range(0, length).mapToObj(i -> fill)
                .toArray(i -> emptyArray(type, i));
    }
    
    /**
     * Creates a new array of a certain type and length, filled with null.
     *
     * @param type   The type of the array.
     * @param length The length of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create(Class, Object, int)
     */
    public static <T> T[] create(Class<T> type, int length) {
        return create(type, null, length);
    }
    
    /**
     * Creates a new array of a certain type and length, filled with a default value.
     *
     * @param fill   The object to fill the array with.
     * @param length The length of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create(Class, Object, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] create(T fill, int length) {
        return create((Class<T>) fill.getClass(), fill, length);
    }
    
    /**
     * Creates a new array of a certain type.
     *
     * @param type The type of the array.
     * @param <T>  The type of the array.
     * @return The created array.
     * @see #create(Class, int)
     */
    public static <T> T[] create(Class<T> type) {
        return create(type, 0);
    }
    
    /**
     * Creates a new array of a certain length.
     *
     * @param <T>  The type of the array.
     * @return The created array.
     * @see #create(Class, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] create(int length) {
        return create((Class<T>) DEFAULT_ARRAY_TYPE, length);
    }
    
    /**
     * Creates a new 2D array of a certain type and dimensions, filled with a default value, or null.
     *
     * @param type   The type of the array.
     * @param fill   The object to fill the array with, or null.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create(Class, Object, int)
     */
    private static <T> T[][] create2D(Class<T> type, T fill, int width, int height) {
        return IntStream.range(0, width).mapToObj(i -> create(type, fill, height))
                .toArray(ArrayUtility::emptyArray);
    }
    
    /**
     * Creates a new 2D array of a certain type and dimensions, filled with null.
     *
     * @param type   The type of the array.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create2D(Class, Object, int, int)
     */
    public static <T> T[][] create2D(Class<T> type, int width, int height) {
        return create2D(type, null, width, height);
    }
    
    /**
     * Creates a new 2D array of a certain type and dimensions, filled with a default value.
     *
     * @param fill   The object to fill the array with.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create2D(Class, Object, int, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][] create2D(T fill, int width, int height) {
        return create2D((Class<T>) fill.getClass(), fill, width, height);
    }
    
    /**
     * Creates a new 2D array of a certain type.
     *
     * @param type The type of the array.
     * @param <T>  The type of the array.
     * @return The created array.
     * @see #create2D(Class, int, int)
     */
    public static <T> T[][] create2D(Class<T> type) {
        return create2D(type, 0, 0);
    }
    
    /**
     * Creates a new 2D array of certain dimensions.
     *
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param <T>  The type of the array.
     * @return The created array.
     * @see #create2D(Class, int, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][] create2D(int width, int height) {
        return create2D((Class<T>) DEFAULT_ARRAY_TYPE, width, height);
    }
    
    /**
     * Creates a new 3D array of a certain type and dimensions, filled with a default value or null.
     *
     * @param type   The type of the array.
     * @param fill   The object to fill the array with, or null.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param depth  The depth of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create2D(Class, Object, int, int)
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private static <T> T[][][] create3D(Class<T> type, T fill, int width, int height, int depth) {
        return IntStream.range(0, width).mapToObj(i -> create2D(type, fill, height, depth))
                .toArray(ArrayUtility::emptyArray);
    }
    
    /**
     * Creates a new 3D array of a certain type and dimensions, filled with null.
     *
     * @param type   The type of the array.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param depth  The depth of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create3D(Class, Object, int, int, int)
     */
    public static <T> T[][][] create3D(Class<T> type, int width, int height, int depth) {
        return create3D(type, null, width, height, depth);
    }
    
    /**
     * Creates a new 3D array of a certain type and dimensions, filled with a default value.
     *
     * @param fill   The object to fill the array with.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param depth  The depth of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create3D(Class, Object, int, int, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][][] create3D(T fill, int width, int height, int depth) {
        return create3D((Class<T>) fill.getClass(), fill, width, height, depth);
    }
    
    /**
     * Creates a new 3D array of a certain type.
     *
     * @param type The type of the array.
     * @param <T>  The type of the array.
     * @return The created array.
     * @see #create3D(Class, Object, int, int, int)
     */
    public static <T> T[][][] create3D(Class<T> type) {
        return create3D(type, 0, 0, 0);
    }
    
    /**
     * Creates a new 3D array of certain dimensions.
     *
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param depth  The depth of the array.
     * @param <T>  The type of the array.
     * @return The created array.
     * @see #create3D(Class, Object, int, int, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][][] create3D(int width, int height, int depth) {
        return create3D((Class<T>) DEFAULT_ARRAY_TYPE, width, height, depth);
    }
    
    
    
    
    /**
     * Converts a stream to a new array of a certain type.
     *
     * @param type       The type of the array.
     * @param stream The source stream.
     * @param <T>        The type of the array and the source stream.
     * @return The array created from the source stream.
     * @see #create(Class, int)
     */
    public static <T> T[] asArray(Class<T> type, Stream<? extends T> stream) {
        return Optional.ofNullable(stream).orElse(Stream.empty())
                .toArray(i -> create(type, i));
    }
    
    /**
     * Converts a stream to a new array.
     *
     * @param stream The source stream.
     * @param <T>        The type of the array and the source stream.
     * @return The array created from the source stream.
     * @see #asArray(Class, Stream)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] asArray(Stream<T> stream) {
        return asArray((Class<T>) DEFAULT_ARRAY_TYPE, stream);
    }
    
    
    /**
     * Converts a collection to a new array of a certain type.
     *
     * @param type       The type of the array.
     * @param collection The source collection.
     * @param <T>        The type of the array and the source collection.
     * @return The array created from the source collection.
     * @see #asArray(Class, Stream)
     */
    public static <T> T[] asArray(Class<T> type, Collection<? extends T> collection) {
        return asArray(type, Stream.ofNullable(collection).flatMap(Collection::stream));
    }
    
    /**
     * Converts a collection to a new array.
     *
     * @param collection The source collection.
     * @param <T>        The type of the array and the source collection.
     * @return The array created from the source collection.
     * @see #asArray(Class, Collection)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] asArray(Collection<T> collection) {
        return asArray((Class<T>) DEFAULT_ARRAY_TYPE, collection);
    }
    
    /**
     * Converts an array to a new array of a certain type.
     *
     * @param type       The type of the array.
     * @param array The source array.
     * @param <T>        The type of the array.
     * @param <U>        The type of the source array.
     * @return The array created from the source array.
     * @see #asArray(Class, Stream)
     */
    public static <T, U extends T> T[] asArray(Class<T> type, U[] array) { //TODO test name
        return asArray(type, Stream.ofNullable(array).flatMap(Arrays::stream));
    }
    /**
     * Converts an array to a new array of a certain type.
     *
     * @param array The source array.
     * @param <T>        The type of the array and the source array.
     * @return The array created from the source array.
     * @see #asArray(Class, Object[])
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] asArray(T[] array) {
        return asArray((Class<T>) DEFAULT_ARRAY_TYPE, array);
    }
    
    
    
    
    /**
     * Creates and populates a new array of a certain type.
     *
     * @param type     The type of the array.
     * @param elements The elements to populate the array with.
     * @param <T>      The type of the array.
     * @param <U>      The type of the elements.
     * @return The created and populated array.
     * @see #asArray(Class, Object[])
     */
    @SuppressWarnings("unchecked")
    public static <T, U extends T> T[] arrayOf(Class<T> type, U... elements) {
        return asArray(type, elements);
    }
    
    /**
     * Creates and populates a new array.
     *
     * @param elements The elements to populate the array with.
     * @param <T>      The type of the array.
     * @return The created and populated array.
     * @see #arrayOf(Class, Object[])
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayOf(T... elements) {
        return arrayOf((Class<T>) DEFAULT_ARRAY_TYPE, elements);
    }
    
    /**
     * Creates and populates a new array of a certain type.
     *
     * @param type          The type of the array.
     * @param length        The length of the array.
     * @param indexMapper The function that uses the element indices to produce the elements to populate the array with.
     * @param <T>           The type of the array.
     * @return The created and populated array.
     * @see #asArray(Class, Stream)
     */
    public static <T> T[] arrayOf(Class<T> type, int length, IntFunction<? extends T> indexMapper) {
        return asArray(type, IntStream.range(0, length).mapToObj(indexMapper));
    }
    
    /**
     * Creates and populates a new array.
     *
     * @param length        The length of the array.
     * @param indexMapper The function that uses the element indices to produce the elements to populate the array with.
     * @param <T>           The type of the array.
     * @return The created and populated array.
     * @see #arrayOf(Class, int, IntFunction)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayOf(int length, IntFunction<? extends T> indexMapper) {
        return arrayOf((Class<T>) DEFAULT_ARRAY_TYPE, length, indexMapper);
    }
    
    /**
     * Creates and populates a new array of a certain type.
     *
     * @param type            The type of the array.
     * @param length          The length of the array.
     * @param supplier The supplier that supplies the elements to populate the array with.
     * @param <T>             The type of the array.
     * @return The created and populated array.
     * @see #arrayOf(Class, int, IntFunction)
     */
    public static <T> T[] arrayOf(Class<T> type, int length, Supplier<? extends T> supplier) {
        return arrayOf(type, length, (IntFunction<T>) (i -> supplier.get()));
    }
    
    /**
     * Creates and populates a new array.
     *
     * @param length          The length of the array.
     * @param supplier The supplier that supplies the elements to populate the array with.
     * @param <T>             The type of the array.
     * @return The created and populated array.
     * @see #arrayOf(Class, int, Supplier)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayOf(int length, Supplier<? extends T> supplier) {
        return arrayOf((Class<T>) DEFAULT_ARRAY_TYPE, length, supplier);
    }
    
    /**
     * Creates and populates a new array of a certain type.
     *
     * @param type     The type of the array.
     * @param fill The value to populate the array with.
     * @param <T>      The type of the array.
     * @return The created and populated array.
     * @see #arrayOf(Class, int, Supplier)
     */
    public static <T> T[] arrayOf(Class<T> type, int length, T fill) {
        return arrayOf(type, length, (Supplier<T>) (() -> fill));
    }
    
    /**
     * Creates and populates a new array.
     *
     * @param fill The value to populate the array with.
     * @param <T>      The type of the array.
     * @return The created and populated array.
     * @see #arrayOf(Class, int, Object)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayOf(int length, T fill) {
        return arrayOf((Class<T>) DEFAULT_ARRAY_TYPE, length, fill);
    }
    
    
    
    
    
    
    
    
    /**
     * Clones an array.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return The clone of the array.
     * @see #asArray(Object[])
     */
    public static <T> T[] clone(T[] array) {
        return Optional.ofNullable(array)
                .map(ArrayUtility::asArray)
                .orElse(null);
    }
    
    /**
     * Casts an array to am array of a specific type.
     *
     * @param array The original array.
     * @param type   The type to cast to.
     * @param <T>   The type of the array.
     * @param <U>   The type of the original array.
     * @return The casted array.
     * @see #asArray(Object[])
     */
    public static <T, U extends T> T[] cast(U[] array, Class<T> type) {
        return Optional.ofNullable(array)
                .map(ArrayUtility::asArray)
                .orElse(null);
    }
    
    
    /**
     * Maps an array to a new array of a certain type.
     *
     * @param type          The type of the array.
     * @param array The source array.                      
     * @param mapper The function that maps the elements from the source array to the new array.
     * @param <T>           The type of the array.
     * @param <U>           The type of the source array.
     * @return The array mapped from the source array.
     * @see #asArray(Class, Stream)
     */
    public static <T, U> T[] map(Class<T> type, U[] array, Function<? super U, ? extends T> mapper) { //TODO test
        return Optional.ofNullable(array)
                .map(e -> asArray(type, Arrays.stream(e).map(mapper)))
                .orElse(null);
    }
    
    /**
     * Maps an array to a new array.
     *
     * @param array The source array                      
     * @param mapper The function that maps the elements from the source array to the new array.
     * @param <T>           The type of the array.
     * @param <U>           The type of the source array.
     * @return The array mapped from the source array.
     * @see #map(Class, Object[], Function)
     */
    @SuppressWarnings("unchecked")
    public static <T, U> T[] map(U[] array, Function<? super U, ? extends T> mapper) {
        return map((Class<T>) DEFAULT_ARRAY_TYPE, array, mapper);
    }
    
    /**
     * Maps a collection to a new array of a certain type.
     *
     * @param type          The type of the array.
     * @param collection The source collection.                      
     * @param mapper The function that maps the elements from the source collection to the new array.
     * @param <T>           The type of the array.
     * @param <U>           The type of the source collection.
     * @return The array mapped from the source collection.
     * @see #asArray(Class, Stream)
     */
    public static <T, U> T[] map(Class<T> type, Collection<U> collection, Function<? super U, ? extends T> mapper) {
        return Optional.ofNullable(collection)
                .map(e -> asArray(type, e.stream().map(mapper)))
                .orElse(null);
    }
    
    /**
     * Maps a collection to a new array.
     *
     * @param collection The source collection.                      
     * @param mapper The function that maps the elements from the source collection to the new array.
     * @param <T>           The type of the array.
     * @param <U>           The type of the source collection.
     * @return The array mapped from the source collection.
     * @see #map(Class, Collection, Function)
     */
    @SuppressWarnings("unchecked")
    public static <T, U> T[] map(Collection<U> collection, Function<? super U, ? extends T> mapper) {
        return map((Class<T>) DEFAULT_ARRAY_TYPE, collection, mapper);
    }
    
    
    
    /**
     * Creates a sub array from an array.
     *
     * @param array The array.
     * @param from  The index to start the sub array at.
     * @param to    The index to end the sub array at.
     * @param <T>   The type of the array.
     * @return The sub array.
     * @throws ArrayIndexOutOfBoundsException When the from or to indices are out of bounds of the array.
     * @throws IllegalArgumentException When the from index is greater than the to index.
     * @see Arrays#copyOfRange(Object[], int, int)
     */
    public static <T> T[] subArray(T[] array, int from, int to) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        return Optional.ofNullable(array)
                .map(e -> Arrays.copyOfRange(array, from, to))
                .orElse(null);
    }
    
    /**
     * Creates a sub array from an array.
     *
     * @param array The array.
     * @param from  The index to start the sub array at.
     * @param <T>   The type of the array.
     * @return The sub array.
     * @throws ArrayIndexOutOfBoundsException When the from or to indices are out of bounds of the array.
     * @throws IllegalArgumentException When the from index is greater than the to index.
     * @see #subArray(Object[], int, int)
     */
    public static <T> T[] subArray(T[] array, int from) throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
        return Optional.ofNullable(array)
                .map(e -> subArray(array, from, array.length))
                .orElse(null);
    }
    
    /**
     * Merges two arrays.
     *
     * @param array1 The first array.
     * @param array2 The second array.
     * @param type   The type of the array.
     * @param <T>    The type of the first array.
     * @param <U>    The type of the second array.
     * @return The merged array.
     * @see #asArray(Class, Stream)
     */
    public static <T, U extends T> T[] merge(T[] array1, U[] array2, Class<T> type) {
        return asArray(type, Stream.concat(Stream.ofNullable(array1), Stream.ofNullable(array2)).flatMap(Arrays::stream));
    }
    
    /**
     * Splits an array into an array of arrays of a certain length.
     *
     * @param array  The array.
     * @param length The length of the resulting arrays.
     * @param type   The type of the array.
     * @param <T>    The type of the array.
     * @return The array of arrays of the specified length.
     */
    public static <T> T[][] split(T[] array, int length, Class<T> type) {
        final int split = BoundUtility.truncateNum(length, 1, array.length).intValue();
        final T[][] result = create2D(type, (int) Math.ceil(array.length / (double) split), split);
        IntStream.range(0, array.length).forEach(i ->
                result[i / split][i % split] = array[i]);
        return result;
    }
    
    /**
     * Reverses an array.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return The reversed array.
     */
    public static <T> T[] reverse(T[] array) {
        final T[] reversed = clone(array);
        IntStream.range(0, array.length / 2).forEach(i -> {
            final T tmp = reversed[i];
            reversed[i] = reversed[reversed.length - i - 1];
            reversed[reversed.length - i - 1] = tmp;
        });
        return reversed;
    }
    
    /**
     * Shuffles an array.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return The shuffled array.
     */
    public static <T> T[] shuffle(T[] array) {
        final T[] shuffled = clone(array);
        IntStream.range(0, array.length).forEach(i -> {
            final int index = MathUtility.random(i, (array.length - 1));
            final T tmp = array[index];
            array[index] = array[i];
            array[i] = tmp;
        });
        return shuffled;
    }
    
    /**
     * Determines if an array is null or empty.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return Whether the array is null or empty.
     */
    public static <T> boolean isNullOrEmpty(T[] array) {
        return (array == null) || (array.length == 0);
    }
    
    /**
     * Determines if an array equals another array.
     *
     * @param array1     The first array.
     * @param array2     The second array.
     * @param checkOrder Whether to check the order of the arrays or not.
     * @param <T>        The type of the first array.
     * @param <U>       The type of the second array.
     * @return Whether the arrays are equal or not.
     */
    public static <T, U> boolean equals(T[] array1, U[] array2, boolean checkOrder) {
        return ((array1 == null) || (array2 == null)) ? ((array1 == null) && (array2 == null)) : ((array1.length == array2.length) &&
                (checkOrder ? IntStream.range(0, array1.length).allMatch(i -> Objects.equals(array1[i], array2[i])) :
                 Arrays.stream(array1).allMatch(e -> contains(array2, e) && (numberOfOccurrences(array1, e) == numberOfOccurrences(array2, e)))));
    }
    
    /**
     * Determines if an array equals another array.
     *
     * @param array1 The first array.
     * @param array2 The second array.
     * @param <T>    The type of the first array.
     * @param <U>   The type of the second array.
     * @return Whether the arrays are equal or not.
     * @see #equals(Object[], Object[], boolean)
     */
    public static <T, U> boolean equals(T[] array1, U[] array2) {
        return equals(array1, array2, true);
    }
    
    /**
     * Determines if an array of strings equals another array of strings, regardless of case.
     *
     * @param array1     The first array.
     * @param array2     The second array.
     * @param checkOrder Whether to check the order of the arrays or not.
     * @return Whether the arrays of strings are equal or not, regardless of case.
     * @see #equals(Object[], Object[], boolean) 
     */
    public static boolean equalsIgnoreCase(String[] array1, String[] array2, boolean checkOrder) {
        return ((array1 == null) || (array2 == null)) ? ((array1 == null) && (array2 == null)) : ((array1.length == array2.length) &&
                equals(map(array1, String::toUpperCase), map(array2, String::toUpperCase)));
    }
    
    /**
     * Determines if an array of strings equals another array of strings, regardless of case.
     *
     * @param array1 The first array.
     * @param array2 The second array.
     * @return Whether the arrays of strings are equal or not, regardless of case.
     * @see #equalsIgnoreCase(String[], String[], boolean)
     */
    public static boolean equalsIgnoreCase(String[] array1, String[] array2) {
        return equalsIgnoreCase(array1, array2, true);
    }
    
    /**
     * Determines if an element exists in an array.
     *
     * @param array   The array.
     * @param element The element.
     * @param <T>     The type of the array.
     * @return Whether the array contains the specified element or not.
     */
    public static <T> boolean contains(T[] array, T element) {
        return !isNullOrEmpty(array) && (indexOf(array, element) >= 0);
    }
    
    /**
     * Determines if a string exists in an array, regardless of case.
     *
     * @param array   The array.
     * @param element The element.
     * @return Whether the array contains the specified string or not, regardless of case.
     * @see #contains(Object[], Object)
     */
    public static boolean containsIgnoreCase(String[] array, String element) {
        return !isNullOrEmpty(array) && contains(map(array, String::toUpperCase), element.toUpperCase());
    }
    
    /**
     * Determines the number of occurrences of an element in an array.
     *
     * @param array   The array.
     * @param element The element.
     * @param <T>     The type of the array.
     * @return The number of occurrences of the specified element in the array.
     */
    public static <T> int numberOfOccurrences(T[] array, T element) {
        return isNullOrEmpty(array) ? 0 :
               (int) Arrays.stream(array).filter(e -> Objects.equals(e, element)).count();
    }
    
    /**
     * Determines the number of occurrences of a string element in an array, regardless of case.
     *
     * @param array   The array.
     * @param element The element.
     * @return The number of occurrences of the specified string element in the array, regardless of case.
     */
    public static int numberOfOccurrencesIgnoreCase(String[] array, String element) {
        return isNullOrEmpty(array) ? 0 :
               (int) Arrays.stream(array).filter(e -> StringUtility.equalsIgnoreCase(e, element)).count();
    }
    
    /**
     * Returns the index of an element in an array.
     *
     * @param array   The array.
     * @param element The element.
     * @param <T>     The type of the array.
     * @return The index of the element in the array, or -1 if it does not exist.
     */
    public static <T> int indexOf(T[] array, T element) {
        return isNullOrEmpty(array) ? -1 :
               Arrays.asList(array).indexOf(element);
    }
    
    /**
     * Returns the index of a string in an array, regardless of case.
     *
     * @param array   The array.
     * @param element The element.
     * @return The index of the string in the array, regardless of case, or -1 if it does not exist.
     */
    public static int indexOfIgnoreCase(String[] array, String element) {
        return isNullOrEmpty(array) ? -1 :
               IntStream.range(0, array.length)
                       .filter(i -> StringUtility.equalsIgnoreCase(array[i], element))
                       .findFirst().orElse(-1);
    }
    
    /**
     * Returns an element from an array at a specified index, or a default value if the index is invalid.
     *
     * @param array        The array.
     * @param index        The index.
     * @param defaultValue The default value.
     * @param <T>          The type of the array.
     * @return The element in the array at the specified index, or the default value if the index is invalid.
     */
    public static <T> T getOrDefault(T[] array, int index, T defaultValue) {
        return (!isNullOrEmpty(array) && BoundUtility.inArrayBounds(index, array)) ?
               array[index] : defaultValue;
    }
    
    /**
     * Returns an element from an array at a specified index, or null if the index is invalid.
     *
     * @param array The array.
     * @param index The index.
     * @param <T>   The type of the array.
     * @return The element in the array at the specified index, or null if the index is invalid.
     * @see #getOrDefault(Object[], int, Object)
     */
    public static <T> T getOrNull(T[] array, int index) {
        return getOrDefault(array, index, null);
    }
    
    /**
     * Determines if any element in an array is null.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return Whether or not any element in the array is null.
     */
    public static <T> boolean anyNull(T[] array) {
        return Arrays.stream(array).anyMatch(Objects::isNull);
    }
    
    /**
     * Removes null elements from an array.
     *
     * @param array The array.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return The array with null elements removed.
     */
    public static <T> T[] removeNull(T[] array, Class<T> type) {
        return Arrays.stream(array).filter(Objects::nonNull).toArray(i -> create(type, i));
    }
    
    /**
     * Removes duplicate elements from an array.
     *
     * @param array The array to operate on.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return The array with duplicate elements removed.
     */
    public static <T> T[] removeDuplicates(T[] array, Class<T> type) {
        return Arrays.stream(array).distinct().toArray(i -> create(type, i));
    }
    
    /**
     * Selects a random element from an array.
     *
     * @param array The array to select from.
     * @param <T>   The type of the array.
     * @return A random element from the array.
     */
    public static <T> T selectRandom(T[] array) {
        return isNullOrEmpty(array) ? null :
               array[MathUtility.random(array.length - 1)];
    }
    
    /**
     * Selects a random subset of an array.
     *
     * @param array The array to select from.
     * @param n     The number of elements to select.
     * @param <T>   The type of the array.
     * @return A random subset of the array.
     */
    public static <T> T[] selectN(T[] array, int n) {
        final T[] shuffled = shuffle(array);
        return (n >= array.length) ? shuffled :
               subArray(shuffled, 0, Math.max(n, 0));
    }
    
    /**
     * Copies an array to the end of itself a number of times making an array n times the original length.
     *
     * @param array The array to duplicate.
     * @param times The number of copies of the array to add.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return An array of double size with duplicated elements.
     */
    public static <T> T[] duplicateInOrder(T[] array, int times, Class<T> type) {
        return (times <= 0) ? emptyArray(type) :
               (times == 1) ? clone(array) :
               IntStream.range(0, times).mapToObj(i -> array).flatMap(Arrays::stream).toArray(i -> create(type, i));
    }
    
    /**
     * Copies an array to the end of itself making an array double the original length.
     *
     * @param array The array to duplicate.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return A array of double size with duplicated elements.
     * @see #duplicateInOrder(Object[], int, Class)
     */
    public static <T> T[] duplicateInOrder(T[] array, Class<T> type) {
        return duplicateInOrder(array, 2, type);
    }
    
    /**
     * Sorts an array by the number of occurrences of each entry in the array.
     *
     * @param array   The array to sort.
     * @param reverse Whether to sort in reverse or not.
     * @param type    The type of the array.
     * @param <T>     The type of the array.
     * @return The array sorted by the number of occurrences of each entry in the array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] sortByNumberOfOccurrences(T[] array, boolean reverse, Class<T> type) {
        return arrayOf(type, (T[]) Arrays.stream(array)
                .collect(Collectors.groupingBy(Function.identity())).values().stream()
                .sorted(Comparator.comparingInt(e -> (e.size() * (reverse ? 1 : -1))))
                .flatMap(Collection::stream).toArray());
    }
    
    /**
     * Sorts an array by the number of occurrences of each entry in the array.
     *
     * @param array The array to sort.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return The array sorted by the number of occurrences of each entry in the array.
     * @see #sortByNumberOfOccurrences(Object[], boolean, Class)
     */
    public static <T> T[] sortByNumberOfOccurrences(T[] array, Class<T> type) {
        return sortByNumberOfOccurrences(array, false, type);
    }
    
}
