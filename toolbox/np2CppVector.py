#! /usr/bin/python3
# -*- coding: utf-8 -*-
#-----------------------------------------------------------------------------
# Author:   Fabien Marteau <fabien.marteau@armadeus.com>
# Created:  30/04/2021
#-----------------------------------------------------------------------------
#  Copyright (2021)  Armadeus Systems
#-----------------------------------------------------------------------------
""" np2CppVector
"""

import sys
import getopt
import numpy as np

class np2CppVector(object):
    """
    """

    def __init__(self, npname, vectorname):
        self.pdm = np.load(npname)
        self.npname = npname
        self.vectorname = vectorname

    def saveVector(self):
        with open(self.vectorname, "w") as fv:
            fv.write(f"/* file {self.npname} */\n")
            fv.write("#include <cstdint>\n")
            fv.write("vector<uint8_t> pdm_vec {\n")
            for value in self.pdm:
                fv.write(f"{value},\n")
            fv.write("};\n")
            fv.write(f"/* End of vector from {self.npname} */")
        

def usages():
    print("Usages:")
    print("python csvtobitstream.py [options]")
    print("-h, --help                   print this help")
    print("-n, --np=[numpyfile]         Give the numpy array to load")
    print("-v, --vector=[vectorfile]    Give the vector file (*.h) to save")


if __name__ == "__main__":
    if sys.version_info[0] < 3:
        raise Exception("Must be using Python 3")

    try:
        opts, args = getopt.getopt(sys.argv[1:], "hn:v:",
                                    ["help", "np=", "vector="])
    except getopt.GetoptError:
        usages()
        sys.exit(2)

    npname = None
    vectorname = None
    for opt, arg in opts:
        if opt in ["-h", "--help"]:
            usages()
            sys.exit(0)
        elif opt in ["-n", "--np"]:
            npname = arg
        elif opt in ["-v", "--vector"]:
            vectorname = arg

    if npname is None:
        print("give numpy file")
        usages()
        sys.exit(2)
    if vectorname is None:
        print("give vector output filename")
        usages()
        sys.exit(2)

    np2cpp = np2CppVector(npname, vectorname)
    np2cpp.saveVector()
