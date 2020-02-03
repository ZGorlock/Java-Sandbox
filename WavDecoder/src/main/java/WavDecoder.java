/*
 * File:    WavDecoder.java
 * Package:
 * Author:  Zachary Gill
 */

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

public class WavDecoder {
    
    public static void main(String[] args) {
        File wavFile = new File("resources/a2002011001-e02.wav");
        
        Wav wav = new Wav(wavFile);
        wav.printInfo();
    }
    
    private static class Wav {
        
        File file;
        
        byte[] ChunkID;
        
        byte[] ChunkSize;
        
        byte[] Format;
        
        byte[] Subchunk1ID;
        
        byte[] Subchunk1Size;
        
        byte[] AudioFormat;
        
        byte[] NumChannels;
        
        byte[] SampleRate;
        
        byte[] ByteRate;
        
        byte[] BlockAlign;
        
        byte[] BitsPerSample;
        
        byte[] ExtraParamSize;
        
        byte[] ExtraParams;
        
        byte[] Subchunk2ID;
        
        byte[] Subchunk2Size;
        
        int[][] samples;
        
        public Wav(File wavFile) {
            file = wavFile;
            byte[] fileData;
            try {
                fileData = FileUtils.readFileToByteArray(wavFile);
            } catch (IOException e) {
                System.err.println("Could not read input WAV file");
                return;
            }
            
            int offset = 0;
            ChunkID = Arrays.copyOfRange(fileData, offset, offset = (offset + 4));
            ChunkSize = Arrays.copyOfRange(fileData, offset, offset = (offset + 4));
            Format = Arrays.copyOfRange(fileData, offset, offset = (offset + 4));
            
            Subchunk1ID = Arrays.copyOfRange(fileData, offset, offset = (offset + 4));
            Subchunk1Size = Arrays.copyOfRange(fileData, offset, offset = (offset + 4));
            AudioFormat = Arrays.copyOfRange(fileData, offset, offset = (offset + 2));
            NumChannels = Arrays.copyOfRange(fileData, offset, offset = (offset + 2));
            SampleRate = Arrays.copyOfRange(fileData, offset, offset = (offset + 4));
            ByteRate = Arrays.copyOfRange(fileData, offset, offset = (offset + 4));
            BlockAlign = Arrays.copyOfRange(fileData, offset, offset = (offset + 2));
            BitsPerSample = Arrays.copyOfRange(fileData, offset, offset = (offset + 2));
            
            if (!new String(Arrays.copyOfRange(fileData, offset, offset + 2)).equalsIgnoreCase("da")) {
                ExtraParamSize = Arrays.copyOfRange(fileData, offset, offset = (offset + 2));
                ExtraParams = Arrays.copyOfRange(fileData, offset, offset = (offset + ByteBuffer.wrap(ExtraParamSize).order(ByteOrder.LITTLE_ENDIAN).getShort()));
            } else {
                ExtraParamSize = new byte[2];
                ExtraParams = new byte[0];
            }
            
            Subchunk2ID = Arrays.copyOfRange(fileData, offset, offset = (offset + 4));
            Subchunk2Size = Arrays.copyOfRange(fileData, offset, offset = (offset + 4));
            
            int sampleSize = ByteBuffer.wrap(BitsPerSample).order(ByteOrder.LITTLE_ENDIAN).getShort();
            int numChannels = ByteBuffer.wrap(NumChannels).order(ByteOrder.LITTLE_ENDIAN).getShort();
            int channelSize = sampleSize / numChannels;
            int sampleCount = (fileData.length - offset) / (sampleSize / 8);
            samples = new int[sampleCount][numChannels];
            
            for (int sample = 0; sample < sampleCount; sample++) {
                for (int channel = 0; channel < numChannels; channel++) {
                    samples[sample][channel] =
                            new BigInteger(ByteBuffer.wrap(Arrays.copyOfRange(fileData, offset, offset = (offset + (channelSize / 8))))
                                                     .order(ByteOrder.LITTLE_ENDIAN).array()).intValue();
                }
            }
        }
        
        public void printInfo() {
            System.out.println("FileName: " + file.getName());
            System.out.println("FileSize: " + file.length());
            System.out.println();
            System.out.println("ChunkID: " + new String(ChunkID, StandardCharsets.UTF_8));
            System.out.println("ChunkSize: " + ByteBuffer.wrap(ChunkSize).order(ByteOrder.LITTLE_ENDIAN).getInt());
            System.out.println("Format: " + new String(Format, StandardCharsets.UTF_8));
            System.out.println();
            System.out.println("Subchunk1ID: " + new String(Subchunk1ID, StandardCharsets.UTF_8));
            System.out.println("Subchunk1Size: " + ByteBuffer.wrap(Subchunk1Size).order(ByteOrder.LITTLE_ENDIAN).getInt());
            System.out.println("AudioFormat: " + ByteBuffer.wrap(AudioFormat).order(ByteOrder.LITTLE_ENDIAN).getShort());
            System.out.println("NumChannels: " + ByteBuffer.wrap(NumChannels).order(ByteOrder.LITTLE_ENDIAN).getShort());
            System.out.println("SampleRate: " + ByteBuffer.wrap(SampleRate).order(ByteOrder.LITTLE_ENDIAN).getInt());
            System.out.println("ByteRate: " + ByteBuffer.wrap(ByteRate).order(ByteOrder.LITTLE_ENDIAN).getInt());
            System.out.println("BlockAlign: " + ByteBuffer.wrap(BlockAlign).order(ByteOrder.LITTLE_ENDIAN).getShort());
            System.out.println("BitsPerSample: " + ByteBuffer.wrap(BitsPerSample).order(ByteOrder.LITTLE_ENDIAN).getShort());
            System.out.println("ExtraParamSize: " + ByteBuffer.wrap(ExtraParamSize).order(ByteOrder.LITTLE_ENDIAN).getShort());
            System.out.println("ExtraParams: " + new String(ExtraParams, StandardCharsets.UTF_8));
            System.out.println();
            System.out.println("Subchunk2ID: " + new String(Subchunk2ID, StandardCharsets.UTF_8));
            System.out.println("Subchunk2Size: " + ByteBuffer.wrap(Subchunk2Size).order(ByteOrder.LITTLE_ENDIAN).getInt());
        }
        
    }
    
}
