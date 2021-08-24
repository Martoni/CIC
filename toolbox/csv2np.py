#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import wave
import struct
import numpy as np
import getopt

class Csv2Np(object):
    def __init__(self, csvfilename, npfilename):
        self.csvfilename = csvfilename
        self.npfilename = npfilename

    def read_csv(self):
        with open(self.csvfilename) as fcsv:
            heads = next(fcsv)
            old_clk = 0
            alist = []
            for line in fcsv:
                atime, clk, data = line.split(',')
                if old_clk == 0 and int(clk) == 1:
                    alist.append(int(data))
                old_clk = int(clk)
        self.npoutput = np.array(alist)
        print(f"output length {len(self.npoutput)}")

    def write_np(self):
        np.save(self.npfilename, self.npoutput)

    def read_np(self):
        self.npoutput = np.load(self.npfilename)

    def write_sound(self):
        freq = 3000000
        sound_sampling = 44100
        decimate = int(freq/sound_sampling)
        # mean and decimate
        alist = []
        full_size = len(self.npoutput)
        for i in range(int(full_size/decimate)):
            alist.append(sum(self.npoutput[i*decimate:(i+1)*decimate]))
        # save as sound
        obj = wave.open(f"{self.npfilename}.wav", "w")
        obj.setnchannels(1) # mono
        obj.setsampwidth(2)
        obj.setframerate(sound_sampling)
        for value in alist:
            data = struct.pack('<h', (value-35)*5000)
            obj.writeframesraw(data)
        obj.close() 


def usage():
    print("Convert pdm csv captured with pulseview to numpy array")


if __name__ == "__main__":
    print("Convert csv to numpy array")
    try:
        opts, args = getopt.getopt(sys.argv[1:], 'hc:n:',
                                ['csv=', 'np=', 'help'])
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    
    csvfilename = None
    npfilename = None
    for opt, arg in opts:
        if opt in ('-h', '--help'):
            usage()
            sys.exit(2)
        elif opt in ('-c', '--csv'):
            csvfilename = arg
        elif opt in ('-n', '--np'):
            npfilename = arg
        else:
            usage()
            sys.exit(2)

    if not None in [csvfilename, npfilename]:
        print(f"Convert csv {csvfilename} to np file {npfilename}.npy")
        c2n = Csv2Np(csvfilename, npfilename)
        c2n.read_csv()
        c2n.write_np()

    elif (csvfilename is None) and (npfilename is not None):
        print(f"Filter np file and write sound file {npfilename}.wav")
        c2n = Csv2Np(csvfilename=None, npfilename=npfilename)
        c2n.read_np()
        c2n.write_sound()
