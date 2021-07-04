import sys
import getopt
import matplotlib.pyplot as plt
import numpy as np

class PdmPlot(object):
    def __init__(self, npfilename):
        self.nparray = np.load(npfilename)

    def show_spectre(self):
        plt.magnitude_spectrum(self.nparray, Fs=3000000, scale="dB", color='C1')
        plt.show()

def usage():
    print("plot some data on pdm captures")

if __name__ == "__main__":
    print("plot numpy array")
    try:
        opts, args = getopt.getopt(sys.argv[1:], 'hn:',
                                ['np=', 'help'])
    except getopt.GetoptError:
        usage()
        sys.exit(2)
    
    npfilename = None
    for opt, arg in opts:
        if opt in ('-h', '--help'):
            usage()
            sys.exit(2)
        elif opt in ('-n', '--np'):
            npfilename = arg

    pp = PdmPlot(npfilename)
    pp.show_spectre()
