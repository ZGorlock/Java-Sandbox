/*
 * File:    MusicPlayer.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent;

public class MusicPlayer {
    
    public static void main(String[] args) {
        AudioPlayerComponent component = new AudioPlayerComponent();
        MediaPlayer player = component.mediaPlayer();
        player.events().addMediaPlayerEventListener(new MediaPlayerEventListener() {
            @Override
            public void mediaChanged(MediaPlayer mediaPlayer, MediaRef mediaRef) {
                
            }
            
            @Override
            public void opening(MediaPlayer mediaPlayer) {
                
            }
            
            @Override
            public void buffering(MediaPlayer mediaPlayer, float v) {
                
            }
            
            @Override
            public void playing(MediaPlayer mediaPlayer) {
                
            }
            
            @Override
            public void paused(MediaPlayer mediaPlayer) {
                
            }
            
            @Override
            public void stopped(MediaPlayer mediaPlayer) {
                
            }
            
            @Override
            public void forward(MediaPlayer mediaPlayer) {
                
            }
            
            @Override
            public void backward(MediaPlayer mediaPlayer) {
                
            }
            
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                
            }
            
            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long l) {
                
            }
            
            @Override
            public void positionChanged(MediaPlayer mediaPlayer, float v) {
                
            }
            
            @Override
            public void seekableChanged(MediaPlayer mediaPlayer, int i) {
                
            }
            
            @Override
            public void pausableChanged(MediaPlayer mediaPlayer, int i) {
                
            }
            
            @Override
            public void titleChanged(MediaPlayer mediaPlayer, int i) {
                
            }
            
            @Override
            public void snapshotTaken(MediaPlayer mediaPlayer, String s) {
                
            }
            
            @Override
            public void lengthChanged(MediaPlayer mediaPlayer, long l) {
                
            }
            
            @Override
            public void videoOutput(MediaPlayer mediaPlayer, int i) {
                
            }
            
            @Override
            public void scrambledChanged(MediaPlayer mediaPlayer, int i) {
                
            }
            
            @Override
            public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType trackType, int i) {
                
            }
            
            @Override
            public void elementaryStreamDeleted(MediaPlayer mediaPlayer, TrackType trackType, int i) {
                
            }
            
            @Override
            public void elementaryStreamSelected(MediaPlayer mediaPlayer, TrackType trackType, int i) {
                
            }
            
            @Override
            public void corked(MediaPlayer mediaPlayer, boolean b) {
                
            }
            
            @Override
            public void muted(MediaPlayer mediaPlayer, boolean b) {
                
            }
            
            @Override
            public void volumeChanged(MediaPlayer mediaPlayer, float v) {
                
            }
            
            @Override
            public void audioDeviceChanged(MediaPlayer mediaPlayer, String s) {
                
            }
            
            @Override
            public void chapterChanged(MediaPlayer mediaPlayer, int i) {
                
            }
            
            @Override
            public void error(MediaPlayer mediaPlayer) {
                
            }
            
            @Override
            public void mediaPlayerReady(MediaPlayer mediaPlayer) {
                
            }
        });
        
        List<String> playlist = new ArrayList<>();
        playlist.add("resources/sample.aac");
        playlist.add("resources/sample.ac3");
        playlist.add("resources/sample.aiff");
        playlist.add("resources/sample.flac");
        playlist.add("resources/sample.m4a");
        playlist.add("resources/sample.mp3");
        playlist.add("resources/sample.mp4");
        playlist.add("resources/sample.ogg");
        playlist.add("resources/sample.opus");
        playlist.add("resources/sample.ts");
        playlist.add("resources/sample.wav");
        playlist.add("resources/sample.wma");
        
        int i = 0;
        
        player.audio().setVolume(100);
        player.media().play(playlist.get(i));
        System.out.println(playlist.get(i));
        boolean playing = true;
        
        Scanner in = new Scanner(System.in);
        while (true) {
            
            String line = in.nextLine();
            switch (line) {
                case "s":
                    if (playing) {
                        player.controls().pause();
                    } else {
                        player.controls().start();
                    }
                    playing = !playing;
                    break;
                
                case "a":
                    if (i == 0) {
                        break;
                    }
                    i--;
                    player.controls().stop();
                    player.media().play(playlist.get(i));
                    playing = true;
                    System.out.println(playlist.get(i));
                    break;
                
                case "d":
                    if (i == playlist.size() - 1) {
                        break;
                    }
                    i++;
                    player.controls().stop();
                    player.media().play(playlist.get(i));
                    System.out.println(playlist.get(i));
                    playing = true;
                    break;
            }
            
        }
    }
    
}
