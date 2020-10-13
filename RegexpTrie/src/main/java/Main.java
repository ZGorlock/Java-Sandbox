/*
 * File:    Main.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {
    
    /**
     * A String of meta characters for regex strings.
     */
    public static final List<Character> META_CHARS = "\\^$.()[]{}<>?*+|=:".chars().boxed().map(e -> (char) e.intValue()).collect(Collectors.toList());
    
    public static void main(String[] args) throws IOException {
        System.out.println(optimizeAlternation(Arrays.asList("WHO", "WHAT", "WHEN", "WHERE", "WHY")));
//        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        try {
//            final ArrayList<String> lines = new ArrayList<String>();
//            while (true) {
//                final String line = in.readLine();
//                if (line == null) {
//                    break;
//                }
//                lines.add(line);
//            }
//            System.out.println(RegexpTrie.buildRegex(lines));
//        } finally {
//            in.close();
//        }
    }
    
    /**
     * Optimizes a list of alternations for regex.
     *
     * @param alternations The list of alternations.
     * @return An optimized regex string for the alternation.
     * @see #optimizeAlternationHelper(List, int, StringBuilder)
     */
    public static String optimizeAlternation(List<String> alternations) {
        return optimizeAlternationHelper(new ArrayList<>(alternations), 0, new StringBuilder());
    }
    
    /**
     * Produces an optimized regex string for a list of alternations.
     *
     * @param alternations The list of alternations.
     * @param depth        The current depth into the alternation strings.
     * @param out          The string builder that holds the optimized regex string.
     * @return An optimized regex string for the list of alternations.
     */
    private static String optimizeAlternationHelper(List<String> alternations, int depth, StringBuilder out) {
        final int size = alternations.size();
        if (size < 2) {
            out.append(((size == 1) && (depth < alternations.get(0).length())) ?
                       escapeRegex(alternations.get(0).substring(depth)) : "");
            return out.toString();
        }
    
        alternations.sort((s1, s2) ->
                (s1.length() > depth) ?
                ((s2.length() > depth) ? (s1.charAt(depth) - s2.charAt(depth)) : 1) :
                ((s2.length() > depth) ? -1 : 0));
    
        int index = IntStream.range(0, size).boxed()
                             .filter(i -> alternations.get(i).length() > depth).findFirst().orElse(size);
        if (index == size) {
            return out.toString();
        }
    
        final boolean hasEmpty = index > 0;
        final boolean allSame = alternations.get(index).charAt(depth) == alternations.get(size - 1).charAt(depth);
    
        out.append((!allSame || hasEmpty) ? "(?:" : "");
        if (allSame) {
            out.append(escapeRegexChar(alternations.get(index).charAt(depth)));
            optimizeAlternationHelper(alternations.subList(index, size), depth + 1, out);
        
        } else {
            boolean first = true;
            while (index < size) {
                final char c = alternations.get(index).charAt(depth);
                final int start = index;
                index = IntStream.range(index, size).boxed()
                                 .filter(i -> alternations.get(i).charAt(depth) != c).findFirst().orElse(size);
            
                out.append(first ? "" : '|').append(escapeRegexChar(c));
                optimizeAlternationHelper(alternations.subList(start, index), depth + 1, out);
            
                first = false;
            }
        }
        out.append((!allSame || hasEmpty) ? ')' : "").append(hasEmpty ? '?' : "");
    
        return out.toString();
    }
    
    /**
     * Escapes a string for regex.<br>
     * The string must not be a regex string already.
     *
     * @param str The string.
     * @return The escaped regex string.
     */
    public static String escapeRegex(String str) {
        return str.chars().boxed()
                  .map(i -> escapeRegexChar((char) i.intValue()))
                  .collect(Collectors.joining());
    }
    
    /**
     * Escapes a character for regex.
     *
     * @param c The character.
     * @return The escaped regex character..
     */
    public static String escapeRegexChar(char c) {
        return (META_CHARS.contains(c) ? "\\" : "") + c;
    }
    
}