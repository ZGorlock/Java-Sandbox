/*
 * File:    SpotifyPlaylistCreator.java
 * Package: main
 * Author:  Zachary Gill
 */

package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import commons.access.CmdLine;
import commons.access.Filesystem;
import commons.object.string.StringUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SpotifyPlaylistCreator {
    
    //https://developer.spotify.com/console/get-playlist/?playlist_id=&market=&fields=&additional_types=
    private static final String AUTH_TOKEN = "BQBmfpPTG58ZC3pOH93hGKZlEPJRgbvMTbt1pU3H5DWRQjsdasi0jF8DAxpbPuRbaRYa_u350AASnYac2Fnr15QR4FPKWLx3IL0l8kmV7hrQsKD9iE8M4NNWkRWBaNQx5Zy-ZfteiTjGU689dP_GVLj5nXRm6Dw";
    
    private static final File out = new File("E:\\Documents\\DnD\\Music");
    
    private static final File music = new File("E:\\Documents\\DnD\\Music\\Music");
    
    private static final boolean filterMissing = true;
    
    private static final boolean absolutePlaylist = true;
    
    private static final boolean cleanMusic = true;
    
    private static final boolean useSingleArtist = true;
    
    private static final boolean createRegistry = false;
    
    public static void main(String[] args) throws Exception {
        List<File> allMusic = getMusic();
        List<Playlist> playlists = getPlaylists();
        List<File> playlistMusic = new ArrayList<>();
        
        for (Playlist playlist : playlists) {
            playlistMusic.addAll(createPlaylist(playlist));
        }
        
        printExtraSongs(allMusic, playlistMusic);
        createRegistry(playlists);
        checkPlaylists(false);
    }
    
    private static List<File> getMusic() {
        if (cleanMusic) {
            Filesystem.getDirs(music).forEach(SpotifyPlaylistCreator::cleanArtist);
            Filesystem.getFilesRecursively(music).forEach(SpotifyPlaylistCreator::cleanSong);
        }
        
        return Filesystem.getFilesRecursively(music);
    }
    
    private static List<Playlist> getPlaylists() {
        List<Playlist> playlists = new ArrayList<>();
        for (File playlistShortcut : Filesystem.getFilesRecursively(new File(out, "Spotify"))) {
            List<String> lines = Filesystem.readLines(playlistShortcut);
            String playlistId = lines.get(1)
                    .replaceAll("[^/]+/", "").replaceAll("^/", "")
                    .replaceAll("\\?.+$", "");
            playlists.add(new Playlist(playlistShortcut, playlistId));
        }
        return playlists;
    }
    
    private static List<File> createPlaylist(Playlist playlist) throws Exception {
        List<File> playlistSongs = new ArrayList<>();
        
        System.out.println("Creating playlist: " + playlist.name);
        loadPlaylist(playlist);
        
        List<String> missing = new ArrayList<>();
        for (Track track : playlist.tracks) {
            track.mp3 = cleanTrack(track.mp3);
            
            if (track.mp3.exists() || !filterMissing) {
                playlistSongs.add(track.mp3);
            } else {
                missing.add(track.mp3.getAbsolutePath());
            }
        }
        missing.stream().distinct().forEach(e -> System.err.println("Missing: " + e));
        
        List<String> playlistLines = playlistSongs.stream()
                .map(e -> e.getAbsolutePath().replace((absolutePlaylist ? "" : StringUtility.rShear(music.getAbsolutePath(), music.getName().length())), ""))
                .collect(Collectors.toList());
        Filesystem.writeLines(playlist.m3u, playlistLines);
        
        return playlistSongs;
    }
    
    private static void loadPlaylist(Playlist playlist) throws Exception {
        JSONObject playlistData = loadPlaylistPage("https://api.spotify.com/v1/playlists/" + playlist.id);
        playlist.spotifyName = (String) playlistData.get("name");
        
        long pages = (((Long) ((JSONObject) (playlistData.get("tracks"))).get("total") - 1) / 100) + 1;
        List<JSONArray> tracksDataList = new ArrayList<>();
        do {
            playlistData = loadPlaylistPage("https://api.spotify.com/v1/playlists/" + playlist.id + "/tracks?offset=" + (tracksDataList.size() * 100));
            tracksDataList.add((JSONArray) playlistData.get("items"));
        } while (tracksDataList.size() < pages);
        
        for (JSONArray tracksList : tracksDataList) {
            for (Object trackEntry : tracksList) {
                JSONObject trackData = (JSONObject) ((JSONObject) trackEntry).get("track");
                JSONArray artistData = (JSONArray) trackData.get("artists");
                
                String name = ((String) trackData.get("name")).replace("\\", " - ").replace("/", " - ").replaceAll("\\s+", " ");
                StringBuilder artist = new StringBuilder();
                for (Object value : artistData) {
                    artist.append((artist.length() == 0) ? "" : ", ")
                            .append(((String) ((JSONObject) value).get("name")).replace("\\", " - ").replace("/", " - ").replaceAll("\\s+", " "));
                    if (useSingleArtist) {
                        break;
                    }
                }
                
                Track track = new Track(playlist, name, artist.toString());
                playlist.tracks.add(track);
            }
        }
    }
    
    private static JSONObject loadPlaylistPage(String playlistUrl) throws Exception {
        File tmp = Filesystem.createTemporaryFile();
        String cmd = "curl -X \"GET\" \"" + playlistUrl + "\"" +
                " -H \"Accept: application/json\" -H \"Content-Type: application/json\"" +
                " -H \"Authorization: Bearer " + AUTH_TOKEN + "\"" +
                " > \"" + tmp.getAbsolutePath() + "\"";
        CmdLine.executeCmd(cmd);
        String response = Filesystem.readFileToString(tmp);
        
        JSONParser parser = new JSONParser();
        return (JSONObject) parser.parse(response);
    }
    
    private static File cleanArtist(File artist) {
        if (!cleanMusic) {
            return artist;
        }
        
        if (artist.getName().contains(",")) {
            File newArtist = new File(artist.getParentFile(), artist.getName().replaceAll(",.+$", ""));
            if (newArtist.exists()) {
                for (File song : Filesystem.getFiles(artist)) {
                    File newSong = new File(newArtist, song.getName());
                    if (newSong.exists()) {
                        Filesystem.deleteFile(song);
                    } else {
                        Filesystem.moveFile(song, newSong);
                    }
                }
                if (Filesystem.getFiles(artist).isEmpty()) {
                    Filesystem.deleteDirectory(artist);
                }
            } else {
                Filesystem.renameDirectory(artist, newArtist);
                if (Filesystem.getFiles(newArtist).isEmpty()) {
                    Filesystem.deleteDirectory(newArtist);
                }
            }
            artist = newArtist;
        }
        
        List<File> artistSongs = Filesystem.getFiles(artist);
        for (File artistSong : artistSongs) {
            if (artistSong.getName().endsWith("1.mp3")) {
                File newSong = new File(artistSong.getParentFile(), artistSong.getName().replace("1.mp3", ".mp3"));
                if (newSong.exists()) {
                    Filesystem.deleteFile(artistSong);
                } else {
                    Filesystem.renameFile(artistSong, newSong);
                }
            }
        }
        
        if (Filesystem.getFiles(artist).isEmpty()) {
            Filesystem.deleteDirectory(artist);
        }
        
        return artist;
    }
    
    private static File cleanSong(File song) {
        if (!cleanMusic) {
            return song;
        }
        
        String newName = cleanTitle(song.getName());
        if (!newName.equals(song.getName())) {
            File newSong = new File(song.getParentFile(), newName);
            if (newSong.exists()) {
                Filesystem.deleteFile(song);
            } else {
                Filesystem.renameFile(song, newSong);
            }
            return newSong;
        }
        
        return song;
    }
    
    private static File cleanTrack(File track) {
        if (!cleanMusic || track.exists()) {
            return track;
        }
        
        String newArtist = cleanTitle(track.getParentFile().getName())
                .replaceAll(",.+$", "");
        
        String newName = cleanTitle(track.getName());
        
        if (!newArtist.equals(track.getParentFile().getName()) || !newName.equals(track.getName())) {
            return new File(new File(music, newArtist), newName);
        }
        return track;
    }
    
    private static String cleanTitle(String title) {
        return title
                .replace("\\", " - ")
                .replace("/", "-")
                .replace("|", " - ")
                .replace(":", "-")
                .replace("?", "")
                .replace("*", "")
                .replace(">", "")
                .replace("<", "")
                .replace("\"", "'")
                .replace("Score.mp3", ".mp3")
                .replace("Soundtrack.mp3", ".mp3")
                .replaceAll("\\s?-\\s?\\.mp3", ".mp3")
                .replaceAll("\\s*\\.mp3", ".mp3")
                .replaceAll("\\s+", " ");
    }
    
    private static void printExtraSongs(List<File> allMusic, List<File> playlistMusic) {
        allMusic.stream().filter(e -> !playlistMusic.contains(e))
                .forEach(e -> System.err.println("Extra: " + e.getAbsolutePath()));
    }
    
    private static void createRegistry(List<Playlist> playlists) {
        if (!createRegistry) {
            return;
        }
        
        playlists.sort(Comparator.comparing(o -> o.name));
        
        List<String> registryLines = playlists.stream()
                .map(e -> StringUtility.padRight(e.id, 30) + StringUtility.padRight(e.name, 30) + e.spotifyName)
                .collect(Collectors.toList());
        Filesystem.writeLines(new File(out, "registry.txt"), registryLines);
    }
    
    private static void checkPlaylists(boolean deleteMissingSongs) {
        List<File> playlists = Filesystem.getFiles(out, (e -> e.getName().endsWith(".m3u")));
        
        List<String> allPlaylistItems = new ArrayList<>();
        for (File playlist : playlists) {
            List<String> playlistItems = Filesystem.readLines(playlist);
            playlistItems.removeIf(e -> !new File(e).exists());
            Filesystem.writeLines(playlist, playlistItems);
            allPlaylistItems.addAll(Filesystem.readLines(playlist).stream().filter(e -> !e.isEmpty()).collect(Collectors.toList()));
        }
        System.out.println(allPlaylistItems.size() + " Playlist Items");
        
        List<String> uniquePlaylistItems = allPlaylistItems.stream().distinct().collect(Collectors.toList());
        System.out.println(uniquePlaylistItems.size() + " Unique Playlist Items");
        
        List<String> allSongs = new ArrayList<>();
        for (File song : Filesystem.getFilesRecursively(music)) {
            allSongs.add(song.getAbsolutePath());
        }
        System.out.println(allSongs.size() + " Songs");
        
        List<String> usedSongs = new ArrayList<>();
        List<String> missingSongs = new ArrayList<>();
        allSongs.forEach(e -> {
            if (allPlaylistItems.contains(e)) {
                usedSongs.add(e);
            } else {
                missingSongs.add(e);
            }
        });
        System.out.println(usedSongs.size() + " Used Songs");
        System.out.println(missingSongs.size() + " Missing Songs");
        
        if (deleteMissingSongs) {
            for (String missingSong : missingSongs) {
                Filesystem.deleteFile(new File(missingSong));
            }
            for (File artist : Filesystem.getDirs(music)) {
                if (Filesystem.getFiles(artist).isEmpty()) {
                    Filesystem.deleteDirectory(artist);
                }
            }
        }
        
        for (String playlistItem : allPlaylistItems) {
            File song = new File(playlistItem);
            if (!song.exists()) {
                System.out.println(playlistItem);
            }
        }
    }
    
    private static class Playlist {
        
        File shortcut;
        
        String id;
        
        String name;
        
        String spotifyName;
        
        List<Track> tracks = new ArrayList<>();
        
        File m3u;
        
        Playlist(File shortcut, String id) {
            this.shortcut = shortcut;
            this.id = id;
            this.name = shortcut.getName().replaceAll("\\..+$", "");
            this.m3u = new File(out, (this.name + ".m3u"));
        }
    }
    
    private static class Track {
        
        Playlist playlist;
        
        String track;
        
        String artist;
        
        File mp3;
        
        Track(Playlist playlist, String track, String artist) {
            this.playlist = playlist;
            this.track = track;
            this.artist = artist;
            this.mp3 = new File(new File(music, this.artist), (this.track + ".mp3"));
        }
    }
    
}
