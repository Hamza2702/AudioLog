import java.io.File;

import javax.sound.sampled.*;
public class AudioLog {

    public static void main(String[] args) throws LineUnavailableException{
        try {
            // Get default input
            Mixer.Info[] mixers = AudioSystem.getMixerInfo();
            Mixer.Info defaultInput = null;

            for (Mixer.Info mInfo : mixers) {
                Mixer mixer = AudioSystem.getMixer(mInfo);
                Line.Info[] targetLineInfos = mixer.getTargetLineInfo();

                if (targetLineInfos.length > 0) {
                    try {
                        TargetDataLine testLine = (TargetDataLine) mixer.getLine(targetLineInfos[0]);
                        testLine.open();
                        testLine.close();
                        defaultInput = mInfo;
                        break;
                    } catch (Exception e) {
                        // not a usable input
                    }
                }
            }

            if (defaultInput == null) {
                System.out.println("No input device found");
                return;
            }

            // Audio format
            AudioFormat format = new AudioFormat(44100.0f, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            // Get mixer and target line
            Mixer mixer = AudioSystem.getMixer(defaultInput);
            TargetDataLine line = (TargetDataLine) mixer.getLine(info);

            // Open line and capture
            line.open(format);
            line.start();
            System.out.println("Recording from: " + defaultInput.getName());

            // Capture audio
            AudioInputStream audioStream = new AudioInputStream(line);

            // wavFile output
            String fileName = "Recorded_Audio";
            String ending = ".wav";
            File wavFile = new File(fileName + ending);
            int count = 1;

            // Add increasing number on file name
            while (wavFile.exists()){
                wavFile = new File(fileName + "_" + count + ending);
                count++;
            }

            // Background thread to stop after (seconds)
            Thread stopper = new Thread(() -> {
                try {
                    Thread.sleep(5000); // 5 seconds
                    line.stop();
                    line.close();
                    System.out.println("Recording stopped");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            stopper.start();

            // Write to file
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, wavFile);
            System.out.println("Audio saved to: " + wavFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
