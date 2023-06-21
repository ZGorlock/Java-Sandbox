/*
 * File:    Subreddit.java
 * Package: main.entity.shortcut.subreddit
 * Author:  Zachary Gill
 */

package main.entity.shortcut.subreddit;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.access.Internet;
import commons.object.string.StringUtility;
import main.entity.shortcut.Shortcut;
import main.util.persistence.VariableUtil;
import org.jsoup.nodes.Node;

public class Subreddit extends Shortcut {
    
    //Constants
    
    public static final String BASE_SUBREDDIT_URL = VariableUtil.get(0x916d);
    
    public static final Permission CHECK_HEALTH = Permission.AUTO;
    
    public static final Permission CHECK_HEALTH_AGAIN = Permission.DENY;
    
    public static final Permission MOVE_BANNED = Permission.AUTO;
    
    public static final Permission DELETE_BANNED = Permission.DENY;
    
    public static final Permission MOVE_PRIVATE = Permission.AUTO;
    
    public static final Permission DELETE_PRIVATE = Permission.DENY;
    
    
    //Static Fields
    
    public static final Map<File, Subreddit> loaded = new ConcurrentHashMap<>();
    
    public static final Map<String, SubredditStatus.Status> healthStatus = new ConcurrentHashMap<>();
    
    
    //Fields
    
    protected String filter;
    
    protected SortMethod sort;
    
    protected SubredditAttributes attributes;
    
    protected SubredditStatus status;
    
    
    //Constructors
    
    protected Subreddit(File shortcut, boolean autoClean) {
        super(shortcut, false);
        
        if (this.url.equals(BASE_SUBREDDIT_URL + '/')) {
            System.err.println(StringUtility.format("Empty subreddit shortcut: '{}'", getSource().getAbsolutePath()));
        }
        
        this.name = url.replace(BASE_SUBREDDIT_URL, "").replaceAll("/.*$", "");
        this.filter = url.contains("f=") ? url.replaceAll(".*[?&](f=[^?&=]+).*$", "$1") : "";
        this.sort = SortMethod.parseQueryParameter(url);
        
        this.attributes = new SubredditAttributes(this);
        this.status = new SubredditStatus(this);
        
        if (autoClean) {
            autoClean();
        }
    }
    
    public Subreddit(File shortcut) {
        this(shortcut, CLEAN.auto());
    }
    
    
    //Methods
    
    @Override
    protected boolean doCleanFile() {
        return Stream.of(
                super.doCleanFile(),
                CHECK_HEALTH.auto() && checkHealth()
        ).anyMatch(e -> e);
    }
    
    public boolean checkHealth() {
        if (CHECK_HEALTH.denied() || !permitCheckHealth() || !permitProcessing(CHECK_HEALTH_AGAIN.allowed())) {
            return false;
        }
        return doCheckHealth();
    }
    
    protected boolean permitCheckHealth() {
        return CHECK_HEALTH.allowed() &&
                !getAttributes().isBanned() && !getAttributes().isPrivate();
    }
    
    protected boolean doCheckHealth() {
        System.out.println(StringUtility.format("Checking health: '{}'", getSource().getAbsolutePath()));
        
        final SubredditStatus.Status status = Optional.ofNullable(healthStatus.get(getSubDisplayName()))
                .orElseGet(() -> Optional.ofNullable(Internet.getHtml(getBaseUrl()))
                        .map(e -> e.getElementById(VariableUtil.get(0x7272)))
                        .map(Node::toString)
                        .map(e -> e.replaceAll("<!--.*?-->", ""))
                        .map(e -> {
                            if (e.contains(StringUtility.format(VariableUtil.get(0xc706), getName()))) {
                                return SubredditStatus.Status.BANNED;
                            } else if (e.contains(StringUtility.format(VariableUtil.get(0xb7b5), getName()))) {
                                return SubredditStatus.Status.PRIVATE;
                            } else {
                                return SubredditStatus.Status.ALIVE;
                            }
                        }).orElse(SubredditStatus.Status.UNKNOWN));
        
        this.status.status = status;
        healthStatus.put(getSubDisplayName(), status);
        if (status == SubredditStatus.Status.UNKNOWN) {
            return false;
        }
        
        switch (status) {
            case BANNED:
                System.err.println(StringUtility.format("Banned: '{}'", getSource().getAbsolutePath()));
                getAttributes().getAttributeMap().put(SubredditAttributes.Attribute.BANNED, true);
                if (MOVE_BANNED.allowed()) {
                    move(new File(new File(getParentFile(), SubredditCategory.BANNED_FOLDER_NAME), getFileName()));
                } else if (DELETE_BANNED.allowed()) {
                    delete();
                } else {
                    return false;
                }
                return true;
            
            case PRIVATE:
                System.err.println(StringUtility.format("Private: '{}'", getSource().getAbsolutePath()));
                getAttributes().getAttributeMap().put(SubredditAttributes.Attribute.PRIVATE, true);
                if (MOVE_PRIVATE.allowed()) {
                    move(new File(new File(getParentFile(), SubredditCategory.PRIVATE_FOLDER_NAME), getFileName()));
                } else if (DELETE_PRIVATE.allowed()) {
                    delete();
                } else {
                    return false;
                }
                return true;
            
            default:
                return false;
        }
    }
    
    @Override
    protected boolean needsProcessing() {
        return needsProcessing(Subreddit.class);
    }
    
    @Override
    public String toString() {
        return getSubDisplayName();
    }
    
    
    //Getters
    
    public String getSubName() {
        return getName();
    }
    
    public String getFilter() {
        return filter;
    }
    
    public SortMethod getSort() {
        return sort;
    }
    
    public SubredditAttributes getAttributes() {
        return attributes;
    }
    
    public String getSubDisplayName() {
        return "r/" + getSubName();
    }
    
    public String getBaseUrl() {
        return BASE_SUBREDDIT_URL + getSubName() + '/';
    }
    
    public String getUrl(SortMethod sortMethod) {
        final String sort = sortMethod.getQueryParameter();
        return getBaseUrl() + sort +
                (getFilter().isEmpty() ? "" : (sort.contains("?") ? "&" : "?")) + getFilter();
    }
    
    @Override
    public String getUrl() {
        return getUrl(getSort());
    }
    
    public String getShortcutText(SortMethod sortMethod) {
        return getShortcutContentsFromUrl(getUrl(sortMethod));
    }
    
    @Override
    public String getShortcutText() {
        return getShortcutText(getSort());
    }
    
    
    //Static Methods
    
    public static Subreddit loadSubreddit(File shortcut) {
        if (shortcut.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return loaded.computeIfAbsent(shortcut, Subreddit::new);
    }
    
    public static List<Subreddit> loadAllSubreddits(File libraryDir, boolean includeMulti, boolean includeHidden) {
        if (!libraryDir.isDirectory()) {
            throw new UnsupportedOperationException();
        }
        return findSubredditShortcutsInFolder(libraryDir, includeMulti, includeHidden).stream()
                .map(Subreddit::loadSubreddit).collect(Collectors.toList());
    }
    
    public static List<Subreddit> loadAllSubreddits(File shortcutDir, boolean includeMulti) {
        return loadAllSubreddits(shortcutDir, includeMulti, false);
    }
    
    public static List<Subreddit> loadAllSubreddits(File libraryDir) {
        return loadAllSubreddits(libraryDir, false);
    }
    
    public static List<File> findSubredditShortcutsInFolder(File libraryDir, boolean includeMulti, boolean includeHidden) {
        return findShortcutsInFolder(libraryDir).stream()
                .filter(e -> (e.getName().endsWith(DEFAULT_SHORTCUT_EXTENSION) &&
                        (includeMulti || !e.getName().startsWith(META_PREFIX)) &&
                        (includeHidden || libraryDir.getName().startsWith(META_PREFIX) ||
                                (!e.getParentFile().getAbsolutePath().contains(SubredditCategory.BANNED_FOLDER_NAME) && !e.getParentFile().getAbsolutePath().contains(SubredditCategory.PRIVATE_FOLDER_NAME)))
                ))
                .collect(Collectors.toList());
    }
    
    public static List<File> findSubredditShortcutsInFolder(File libraryDir, boolean multi) {
        return findSubredditShortcutsInFolder(libraryDir, multi, false);
    }
    
    public static List<File> findSubredditShortcutsInFolder(File libraryDir) {
        return findSubredditShortcutsInFolder(libraryDir, true);
    }
    
}
