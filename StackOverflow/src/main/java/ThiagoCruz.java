/*
 * File:    ThiagoCruz.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThiagoCruz {
    
    //Maria souza silva = Maria souza silva = ok
    //Maria souza silva = Maria silva = ok
    //Maria souza silva = Maria Carvalho = Nok
    //Maria souza silva = Ana souza silva = Nok
    //Maria de souza silva = Maria de = Nok
    //Maria de souza silva = Maria souza = OK
    
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("^(?<firstName>[^\\s]+)\\s((de|da)(\\s|$))?(?<otherName>.*)$");
        
        List<String> targetList = Arrays.asList("Maria souza silva", "Maria souza silva", "Maria souza silva", "Maria souza silva", "Maria de souza silva", "Maria de souza silva");
        List<String> sourceList = Arrays.asList("Maria souza silva", "Maria silva", "Maria Carvalho", "Ana souza silva", "Maria de", "Maria souza");
        
        for (int i = 0; i < targetList.size(); i++) {
            checkName(targetList.get(i), sourceList.get(i));
        }
        
        
    }
    
    private static void checkName(String target, String source) {
        Pattern pattern = Pattern.compile("^(?<firstName>[^\\s]+)\\s((de|da)(\\s|$))?(?<otherName>.*)$");
        Matcher targetMatcher = pattern.matcher(target.trim().toLowerCase());
        Matcher sourceMatcher = pattern.matcher(source.trim().toLowerCase());
        if (!targetMatcher.matches() || !sourceMatcher.matches()) {
            System.out.println("Nok");
        }
    
        boolean ok = true;
        if (!sourceMatcher.group("firstName").equals(targetMatcher.group("firstName"))) {
            ok = false;
        } else {
            String[] otherSourceName = sourceMatcher.group("otherName").split("\\s");
            String[] otherTargetName = targetMatcher.group("otherName").split("\\s");
        
            int targetIndex = 0;
            for (String s : otherSourceName) {
                boolean hit = false;
                for (; targetIndex < otherTargetName.length; targetIndex++) {
                    if (s.equals(otherTargetName[targetIndex])) {
                        hit = true;
                        break;
                    }
                }
                if (!hit) {
                    ok = false;
                    break;
                }
            }
        }
        System.out.println(ok ? "ok" : "Nok");
    }
    
}
