/*

Copyright (C) 2011,2012   Michael Dirska, DL1BFF (dl1bff@mdx.de)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.awt.image.MemoryImageSource;
import java.util.Random;

import java.net.DatagramSocket;
import java.net.DatagramPacket;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;




public class UPDisplayA implements Runnable {

	int pixels[];
	MemoryImageSource source;
	
	int size;
	int width;
	int height;
	
	
	// int onPixel = 0xFF6060FF;
	int onPixel = 0xFF202080;
	int offPixel = 0xFFE0E0FF;
	int spacerPixel = 0xFFE0E0E0;
	
	static SourceDataLine	line = null;

	UPDisplayA()
	{
		
	    width = pixelSize * 128;
	    height = pixelSize * 64;
	    size = width * height;
	    pixels = new int[size];

	    
	    for (int i = 0; i < size; i++) {
		pixels[i] = spacerPixel;
	    }

	    source = new MemoryImageSource(width, height, pixels, 0, width);
	    source.setAnimated(true);
	   
	}
	
	public void run() {
	    Thread me = Thread.currentThread( );
	    me.setPriority(Thread.MIN_PRIORITY);
		
		
		DatagramSocket s;

		try {
			s = new DatagramSocket(45232);
			s.setReuseAddress(true);

	    while (true) {
		
		
		byte p[] = new byte[1344];
		
		DatagramPacket pp = new DatagramPacket(p, p.length);

	    s.receive(pp);
		
		
		int x;
		int y;
		
		for (x = 0; x < 128; x++)
		{
			for (y=0; y < 64; y++)
			{
				int b = ((int) p[(x >> 3) | (y << 4)]) & 0xff;
				
				int color =  ( (b & (1 << ( 7 - (x & 0x07)))) != 0 ) ? onPixel : offPixel;
				
				int i;
				int j;
				
				for (i=0; i < (pixelSize - 1); i++)
				{
					for (j=0; j < (pixelSize - 1); j++)
					{
						pixels[ (y*pixelSize + i) * (128 * pixelSize) + (x*pixelSize + j) ] = color;
					}
				}
			}
		}
		
		source.newPixels(0,0, width, height, true);
		
		if (line != null)
		{
			int av = line.available();
			
			if (av >= 320)
			{
				line.write(p, 1024, 320);
			}  // otherwise: skip this packet
		}
		
	    } // while
		
		} catch (Exception e)
		{
		}
	}

	
    private static int pixelSize = 5;
   
    private static void createAndShowGUI() {
        
		JFrame frame = new JFrame("UP4DAR Remote Display + Audio");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		UPDisplayA f = new UPDisplayA();
		
		new Thread(f).start();
		
		Image img = frame.createImage(f.source);
        
		JLabel myLabel = new JLabel ( new ImageIcon(img));
        myLabel.setPreferredSize(new Dimension(128 * pixelSize + 20 , 64 * pixelSize + 20));
        frame.getContentPane().add(myLabel, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }
	
	

    public static void main(String[] args) {
		
		if (args.length > 0) {
			try {
				int firstArg;
	
				firstArg = Integer.parseInt(args[0]);
				
				if ((firstArg > 1) && (firstArg < 20))
				{
					pixelSize = firstArg;
				}
			} catch (NumberFormatException e) {
				System.err.println("Argument must be an integer");
				System.exit(1);
			}
		}
		
		AudioFormat	audioFormat = new AudioFormat( 8000, 16, 1, true, true);
		
		DataLine.Info	info = new DataLine.Info(SourceDataLine.class,
												 audioFormat);
		try
		{
			line = (SourceDataLine) AudioSystem.getLine(info);

			line.open(audioFormat, 128000);
		}
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}

		line.start();
		
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
