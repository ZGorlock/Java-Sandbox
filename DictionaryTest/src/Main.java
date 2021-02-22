/*
 * File:    Main.java
 * Package:
 * Author:  Zachary Gill
 */

import java.util.Scanner;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.PointerUtils;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.data.list.PointerTargetNode;
import net.sf.extjwnl.dictionary.Dictionary;

public class Main {
    
    public static Dictionary dictionary = null;
    
    public static void main(String[] args) throws JWNLException {
        dictionary = Dictionary.getDefaultResourceInstance();
        if (dictionary == null) {
            System.out.println("Dictionary error");
        }
        
        Scanner s = new Scanner(System.in);
        while (true) {
            String word = s.nextLine();
            
            noun(word);
            verb(word);
            adjective(word);
            adverb(word);
            
            System.out.println();
            System.out.println();
        }
    }
    
    public static void noun(String param) throws JWNLException {
        IndexWord word = dictionary.getIndexWord(POS.NOUN, param);
        
        if (word != null) {
            System.out.println("NOUN");
            int i = 0;
            for (Synset s : word.getSenses()) {
                i++;
                System.out.println(i + ". " + s);
                Main.produceAdditionalSynsetInformation(word, s);
            }
            System.out.println();
        }
    }
    
    public static void verb(String param) throws JWNLException {
        IndexWord word = dictionary.getIndexWord(POS.VERB, param);
        
        if (word != null) {
            System.out.println("VERB");
            int i = 0;
            for (Synset s : word.getSenses()) {
                i++;
                System.out.println(i + ". " + s.getGloss());
                Main.produceAdditionalSynsetInformation(word, s);
            }
            System.out.println();
        }
    }
    
    public static void adjective(String param) throws JWNLException {
        IndexWord word = dictionary.getIndexWord(POS.ADJECTIVE, param);
        
        if (word != null) {
            System.out.println("ADJECTIVE");
            int i = 0;
            for (Synset s : word.getSenses()) {
                i++;
                System.out.println(i + ". " + s.getGloss());
                Main.produceAdditionalSynsetInformation(word, s);
            }
            System.out.println();
        }
    }
    
    public static void adverb(String param) throws JWNLException {
        IndexWord word = dictionary.getIndexWord(POS.ADVERB, param);
        
        if (word != null) {
            System.out.println("ADVERB");
            int i = 0;
            for (Synset s : word.getSenses()) {
                i++;
                System.out.println(i + ". " + s.getGloss());
                Main.produceAdditionalSynsetInformation(word, s);
            }
            System.out.println();
        }
    }
    
    public static void produceAdditionalSynsetInformation(IndexWord word, Synset s) throws JWNLException {
        getSynonyms(word, s);
        
        getAntonyms(word, s);
        
        getHolonyms(word, s);
        //getMemberHolonyms(word, s);
        //getPartHolonyms(word, s);
        //getSubstanceHolonyms(word, s);
        
        getDirectHyponyms(word, s);
        
        getDirectHypernyms(word, s);
        
        getMeronyms(word, s);
        //getMemberMeronyms(word, s);
        //getPartMeronyms(word, s);
        //getSubstanceMeronyms(word, s);
        
        getPertainyms(word, s);
        
        //getParticipleOf(word, s);
        //getVerbGroup(word, s);
        
        //getAttributes(word, s);
        //getCauses(word, s);
        //getEntailments(word, s);
        
        //getCoordinateTerms(word, s);
        getAlsoSees(word, s);
    }
    
    public static void getSynonyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder synonyms = new StringBuilder();
        if (s.getPOS().equals(POS.ADJECTIVE)) {
            for (Object pt : PointerUtils.getSynonyms(s)) {
                PointerTargetNode ptn = (PointerTargetNode) pt;
                for (Word w : ptn.getSynset().getWords()) {
                    if (synonyms.length() > 0) {
                        synonyms.append(", ");
                    }
                    synonyms.append(w.getLemma());
                }
            }
        } else {
            for (Word w : s.getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (synonyms.length() > 0) {
                        synonyms.append(", ");
                    }
                    synonyms.append(w.getLemma());
                }
            }
        }
        if (synonyms.length() > 0) {
            System.out.println("    Synonyms: " + synonyms);
        }
    }
    
    public static void getAntonyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder antonyms = new StringBuilder();
        for (Object pt : PointerUtils.getAntonyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (antonyms.length() > 0) {
                        antonyms.append(", ");
                    }
                    antonyms.append(w.getLemma());
                }
            }
        }
        if (antonyms.length() > 0) {
            System.out.println("    Antonyms: " + antonyms);
        }
    }
    
    public static void getHolonyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder holonyms = new StringBuilder();
        for (Object pt : PointerUtils.getHolonyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (holonyms.length() > 0) {
                        holonyms.append(", ");
                    }
                    holonyms.append(w.getLemma());
                }
            }
        }
        if (holonyms.length() > 0) {
            System.out.println("    Holonyms: " + holonyms);
        }
    }
    
    public static void getMemberHolonyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder memberHolonyms = new StringBuilder();
        for (Object pt : PointerUtils.getMemberHolonyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (memberHolonyms.length() > 0) {
                        memberHolonyms.append(", ");
                    }
                    memberHolonyms.append(w.getLemma());
                }
            }
        }
        if (memberHolonyms.length() > 0) {
            System.out.println("    Member holonyms: " + memberHolonyms);
        }
    }
    
    public static void getPartHolonyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder partHolonyms = new StringBuilder();
        for (Object pt : PointerUtils.getPartHolonyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (partHolonyms.length() > 0) {
                        partHolonyms.append(", ");
                    }
                    partHolonyms.append(w.getLemma());
                }
            }
        }
        if (partHolonyms.length() > 0) {
            System.out.println("    Part holonyms: " + partHolonyms);
        }
    }
    
    public static void getSubstanceHolonyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder substanceHolonyms = new StringBuilder();
        for (Object pt : PointerUtils.getSubstanceHolonyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (substanceHolonyms.length() > 0) {
                        substanceHolonyms.append(", ");
                    }
                    substanceHolonyms.append(w.getLemma());
                }
            }
        }
        if (substanceHolonyms.length() > 0) {
            System.out.println("    Substance holonyms: " + substanceHolonyms);
        }
    }
    
    public static void getDirectHyponyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder directHyponyms = new StringBuilder();
        for (Object pt : PointerUtils.getDirectHyponyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (directHyponyms.length() > 0) {
                        directHyponyms.append(", ");
                    }
                    directHyponyms.append(w.getLemma());
                }
            }
        }
        if (directHyponyms.length() > 0) {
            System.out.println("    Hyponyms: " + directHyponyms);
        }
    }
    
    public static void getDirectHypernyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder directHypernyms = new StringBuilder();
        for (Object pt : PointerUtils.getDirectHypernyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (directHypernyms.length() > 0) {
                        directHypernyms.append(", ");
                    }
                    directHypernyms.append(w.getLemma());
                }
            }
        }
        if (directHypernyms.length() > 0) {
            System.out.println("    Direct hypernyms: " + directHypernyms);
        }
    }
    
    public static void getMeronyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder meronyms = new StringBuilder();
        for (Object pt : PointerUtils.getMeronyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (meronyms.length() > 0) {
                        meronyms.append(", ");
                    }
                    meronyms.append(w.getLemma());
                }
            }
        }
        if (meronyms.length() > 0) {
            System.out.println("    Meronyms: " + meronyms);
        }
    }
    
    public static void getMemberMeronyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder memberMeronyms = new StringBuilder();
        for (Object pt : PointerUtils.getMemberMeronyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (memberMeronyms.length() > 0) {
                        memberMeronyms.append(", ");
                    }
                    memberMeronyms.append(w.getLemma());
                }
            }
        }
        if (memberMeronyms.length() > 0) {
            System.out.println("    Member meronyms: " + memberMeronyms);
        }
    }
    
    public static void getPartMeronyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder partMeronyms = new StringBuilder();
        for (Object pt : PointerUtils.getPartMeronyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (partMeronyms.length() > 0) {
                        partMeronyms.append(", ");
                    }
                    partMeronyms.append(w.getLemma());
                }
            }
        }
        if (partMeronyms.length() > 0) {
            System.out.println("    Part meronyms: " + partMeronyms);
        }
    }
    
    public static void getSubstanceMeronyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder substanceMeronyms = new StringBuilder();
        for (Object pt : PointerUtils.getSubstanceMeronyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (substanceMeronyms.length() > 0) {
                        substanceMeronyms.append(", ");
                    }
                    substanceMeronyms.append(w.getLemma());
                }
            }
        }
        if (substanceMeronyms.length() > 0) {
            System.out.println("    Substance meronyms: " + substanceMeronyms);
        }
    }
    
    public static void getPertainyms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder pertainyms = new StringBuilder();
        for (Object pt : PointerUtils.getPertainyms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (pertainyms.length() > 0) {
                        pertainyms.append(", ");
                    }
                    pertainyms.append(w.getLemma());
                }
            }
        }
        if (pertainyms.length() > 0) {
            System.out.println("    Pertainyms: " + pertainyms);
        }
    }
    
    public static void getParticipleOf(IndexWord word, Synset s) throws JWNLException {
        StringBuilder participleOf = new StringBuilder();
        for (Object pt : PointerUtils.getParticipleOf(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (participleOf.length() > 0) {
                        participleOf.append(", ");
                    }
                    participleOf.append(w.getLemma());
                }
            }
        }
        if (participleOf.length() > 0) {
            System.out.println("    Participle of: " + participleOf);
        }
    }
    
    public static void getVerbGroup(IndexWord word, Synset s) throws JWNLException {
        StringBuilder verbGroup = new StringBuilder();
        for (Object pt : PointerUtils.getVerbGroup(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (verbGroup.length() > 0) {
                        verbGroup.append(", ");
                    }
                    verbGroup.append(w.getLemma());
                }
            }
            
        }
        if (verbGroup.length() > 0) {
            System.out.println("    Verb group: " + verbGroup);
        }
    }
    
    public static void getAttributes(IndexWord word, Synset s) throws JWNLException {
        StringBuilder attributes = new StringBuilder();
        for (Object pt : PointerUtils.getAttributes(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (attributes.length() > 0) {
                        attributes.append(", ");
                    }
                    attributes.append(w.getLemma());
                }
            }
        }
        if (attributes.length() > 0) {
            System.out.println("    Attributes: " + attributes);
        }
    }
    
    public static void getCauses(IndexWord word, Synset s) throws JWNLException {
        StringBuilder causes = new StringBuilder();
        for (Object pt : PointerUtils.getCauses(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (causes.length() > 0) {
                        causes.append(", ");
                    }
                    causes.append(w.getLemma());
                }
            }
        }
        if (causes.length() > 0) {
            System.out.println("    Causes: " + causes);
        }
    }
    
    public static void getEntailments(IndexWord word, Synset s) throws JWNLException {
        StringBuilder entailments = new StringBuilder();
        for (Object pt : PointerUtils.getEntailments(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (entailments.length() > 0) {
                        entailments.append(", ");
                    }
                    entailments.append(w.getLemma());
                }
            }
        }
        if (entailments.length() > 0) {
            System.out.println("    Entailments: " + entailments);
        }
    }
    
    public static void getCoordinateTerms(IndexWord word, Synset s) throws JWNLException {
        StringBuilder coordinateTerms = new StringBuilder();
        for (Object pt : PointerUtils.getCoordinateTerms(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (coordinateTerms.length() > 0) {
                        coordinateTerms.append(", ");
                    }
                    coordinateTerms.append(w.getLemma());
                }
            }
        }
        if (coordinateTerms.length() > 0) {
            System.out.println("    Coordinate terms: " + coordinateTerms);
        }
    }
    
    public static void getAlsoSees(IndexWord word, Synset s) throws JWNLException {
        StringBuilder alsoSee = new StringBuilder();
        for (Object pt : PointerUtils.getAlsoSees(s)) {
            PointerTargetNode ptn = (PointerTargetNode) pt;
            for (Word w : ptn.getSynset().getWords()) {
                if (!w.getLemma().equals(word.getLemma())) {
                    if (alsoSee.length() > 0) {
                        alsoSee.append(", ");
                    }
                    alsoSee.append(w.getLemma());
                }
            }
        }
        if (alsoSee.length() > 0) {
            System.out.println("    Also see: " + alsoSee);
        }
    }
    
}
