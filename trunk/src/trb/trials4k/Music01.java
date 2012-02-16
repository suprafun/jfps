package trb.trials4k;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Music01 {

    public static void main(String args[]) {
        Music01 tss = new Music01();
        tss.run();
    }

    public void run() {
        try {

            // Buffer for the audio sample
            byte audioData[] = new byte[1024*1024*4];

            // Generate a simplistic beep
            float amp = 0.5f;
            byte b = 0;
            for (int i = 0; i < audioData.length; i++) {
                audioData[i] = (byte) (b++ * amp);
                amp -= 0.5f / (float) audioData.length;
            }

            for (int t= 0; t < audioData.length ; t++) {
                //audioData[t] = (byte) (((t << 1) ^ ((t << 1) + (t >> 7) & t >> 12)) | t >> (4 - (1 ^ 7 & (t >> 19))) | t >> 7);
            }

            // Play the beep
            AudioFormat audioFormat = new AudioFormat(8000, 8, 1, true, true);
            DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
            Clip music = (Clip) AudioSystem.getLine(info);
            music.open(audioFormat, audioData, 0, audioData.length);
            music.start();

            BufferedImage image = new BufferedImage(1024, 256, BufferedImage.TYPE_INT_BGR);
            for (int i = 0; i < Math.min(audioData.length*1024, image.getWidth()); i++) {
                image.setRGB(i, (audioData[i*1024]) & 0xff, 0xff);
            }

            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new JLabel(new ImageIcon(image)), BorderLayout.CENTER);
            frame.add(new JButton(new AbstractAction("play") {

                public void actionPerformed(ActionEvent e) {
                    
                }
            }), BorderLayout.SOUTH);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
