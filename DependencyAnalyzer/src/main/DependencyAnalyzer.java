/*
 * File:    DependencyAnalyzer.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Filesystem;
import commons.object.string.StringUtility;

public class DependencyAnalyzer {
    
    private static final File SOURCE_ROOT_DIR = new File("F:\\Java\\Commons\\src");
    
    private static final File TMP_DIR = new File("tmp");
    
    private static final SourceNode SOURCE_ROOT = new SourceNode(SOURCE_ROOT_DIR);
    
    public static void main(String[] args) {
//        analyzeDependencies();
        
        copySourceWithDependencies(
                List.of(
                        "commons.access.Filesystem",
                        "commons.object.string.StringUtility"
                ), TMP_DIR);
    }
    
    private static void analyzeDependencies() {
        List<SourceNode> java = SOURCE_ROOT.getAllChildren(e -> e.fileType.equals("java"));
        List<SourceNode> d = SourceNode.getNode("commons.access.Filesystem").getAllDependencies();
        int g = 4;
    }
    
    private static void copySourceWithDependencies(List<String> requested, File destDir) {
        requested.stream()
                .map(SourceNode::getNode)
                .filter(Objects::nonNull)
                .flatMap(node -> node.getAllDependencies().stream())
                .filter(node -> !node.fileType.isBlank())
                .distinct()
                .map(node -> node.key.replace(".", "/") + "." + node.fileType)
                .forEach(filePath -> Filesystem.copyFile(new File(SOURCE_ROOT_DIR, filePath), new File(destDir, filePath)));
    }
    
    private static class SourceNode {
        
        public static final Map<String, SourceNode> tree = new HashMap<>();
        
        public static final Function<File, String> keyGenerator = (File sourceFile) ->
                StringUtility.fixFileSeparators(StringUtility.lShear(
                                sourceFile.getAbsolutePath(), SOURCE_ROOT_DIR.getAbsolutePath().length()))
                        .replaceAll("\\.[^.]+$", "")
                        .replaceAll("^/", "").replace("/", ".");
        
        public final File file;
        
        public final String key;
        
        public final String name;
        
        public final String fileType;
        
        public final SourceNode parent;
        
        public final List<SourceNode> children;
        
        public final List<SourceNode> dependencies;
        
        private SourceNode(File file) {
            this.file = file;
            this.key = keyGenerator.apply(this.file);
            this.name = this.file.getName().replaceAll("\\.[^.]+$", "");
            this.fileType = Filesystem.getFileType(this.file);
            
            if (!this.key.isEmpty()) {
                tree.put(this.key, this);
            }
            
            this.parent = this.key.contains(".") ? getNode(this.file.getParentFile()) : null;
            this.children = new ArrayList<>();
            this.dependencies = new ArrayList<>();
            
            loadChildren();
            if (this.key.isBlank()) {
                loadDependencies();
            }
        }
        
        private void loadChildren() {
            if (!children.isEmpty() || !this.fileType.isBlank()) {
                return;
            }
            
            children.addAll(Filesystem.getFilesAndDirs(this.file).stream()
                    .map(SourceNode::new)
                    .distinct()
                    .collect(Collectors.toList()));
        }
        
        private void loadDependencies() {
            if (!dependencies.isEmpty()) {
                return;
            }
            dependencies.add(this);
            
            if (fileType.isBlank()) {
                dependencies.addAll(children);
                children.forEach(SourceNode::loadDependencies);
                
            } else {
                final List<String> lines = Filesystem.readLines(file);
                dependencies.addAll(Stream.concat(
                                tree.values().stream()
                                        .filter(node -> !dependencies.contains(node))
                                        .filter(node -> lines.contains("import " + node.key + (node.fileType.isBlank() ? ".*" : "") + ";")),
                                getSiblings().stream()
                                        .filter(node -> !dependencies.contains(node))
                                        .filter(node -> !node.fileType.isBlank())
                                        .filter(node -> lines.stream()
                                                .filter(line -> !line.startsWith(" *"))
                                                .map(line -> line.replaceAll("(?:^|\\s+)/[/*].+$", ""))
                                                .map(line -> line.replaceAll("(?<!\\\\)\".+(?:(?<!\\\\)\"|$)", ""))
                                                .anyMatch(line -> line.matches("^.*\\W" + node.name + "\\W.*$"))))
                        .distinct()
                        .collect(Collectors.toList()));
            }
        }
        
        public boolean equals(Object o) {
            return (o instanceof SourceNode) &&
                    (((SourceNode) o).key.equals(this.key));
        }
        
        public List<SourceNode> getAllChildren(Predicate<SourceNode> filter) {
            return !this.fileType.isBlank() ? new ArrayList<>() :
                    this.children.stream()
                            .flatMap(child -> Stream.concat(Stream.of(child), child.getAllChildren(filter).stream()))
                            .filter(filter).distinct()
                            .collect(Collectors.toList());
        }
        
        public List<SourceNode> getAllChildren() {
            return getAllChildren(Objects::nonNull);
        }
        
        public List<SourceNode> getSiblings() {
            return tree.values().stream()
                    .filter(node -> Objects.equals(node.parent, parent))
                    .collect(Collectors.toList());
        }
        
        public List<SourceNode> getAllDependencies() {
            List<SourceNode> dependencyList = new ArrayList<>(dependencies);
            
            int count;
            do {
                count = dependencyList.size();
                dependencyList = dependencyList.stream()
                        .flatMap(node -> node.dependencies.stream())
                        .distinct()
                        .collect(Collectors.toList());
            } while (dependencyList.size() != count);
            
            return dependencyList;
        }
        
        public static SourceNode getNode(String key) {
            return tree.get(key);
        }
        
        public static SourceNode getNode(File file) {
            return getNode(keyGenerator.apply(file));
        }
        
    }
    
}
