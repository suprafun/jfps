/*
 * HALDEN VR PLATFORM
 *
 * RADIATION MODULE
 *
 * $RCSfile: $
 *
 * Author :
 * Date   :
 * Version: $Revision: $ ($Date: $)
 *
 * (c) 2000-2011 Halden Virtual Reality Centre <http://www.ife.no/vr/>,
 * Institutt for energiteknikk. All rights reserved.
 *
 * This code is the property of Halden VR Centre <vr-info@hrp.no> and may
 * only be used in accordance with the terms of the license agreement
 * granted.
 */

package trb.trials4k;

import java.io.*;
import javax.sound.sampled.*;

public class Music02 implements LineListener {

    public static void main(String[] args) throws Exception {
        new Music02().preview();
    }

    public void preview() throws Exception {
        final int frameRate = 16000;
        final int bytesPerFrame = 2;

        // Snare using Karplus-Strong filter. 1 second long.
        int numFrames = 16000;
        byte[] data = new byte[numFrames * bytesPerFrame];

        // Linear congruential PRNG. This seed is conveniently equal to a constant
        // that we already have in the pool, and I think it sounds better than many.
        int rnd = 16000;
        int[] ksbuf = new int[200];
        for (int i = 0; i < ksbuf.length; i++) {
            ksbuf[i] = 0x4000 + ((rnd >> 16) & 0x3fff);
            rnd *= 1103515245;
        }
        for (int i = 0, off = 0; i < numFrames; i++) {
            int nextOff = (off + 1) % ksbuf.length;
            int y = (ksbuf[off] + ksbuf[nextOff]) / 2;

            if ((rnd & 0x10000) == 0) {
                y = -y;
            }
            rnd *= 1103515245;

            ksbuf[off] = y;
            off = nextOff;
            data[2 * i] = (byte) y;
            data[2 * i + 1] = (byte) (y >> 8);
        }

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                frameRate, bytesPerFrame * 8, 1, bytesPerFrame, frameRate, false);

        DataLine.Info info = new DataLine.Info(Clip.class, format);
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.addLineListener(this);

        clip.open(format, data, 0, data.length);
        clip.start();

        // Busy-wait until the listener calls System.exit.
        while (true) {
            Thread.sleep(50);
        }
    }

    public void update(LineEvent le) {
        if (le.getType() == LineEvent.Type.STOP) {
            System.exit(0);
        }
    }
}
