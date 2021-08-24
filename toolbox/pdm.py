#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import matplotlib.pyplot as plt
import numpy as np
import math

def pdm(values):
    n = len(values)
    pdmval = np.zeros(n)
    error = np.zeros(n+1)
    for i in range(n):
       pdmval[i] = 1 if values[i] >= error[i] else 0
       error[i+1] = pdmval[i] - values[i] + error[i]
    return pdmval, error[:n] 

def slipping_mean(values, meansize):
    n = len(values)
    meanvals = np.zeros(n+meansize)
    for i in range(n):
        meanvals[i] = np.mean(values[i-meansize:i])
    return meanvals

# Generate simple sinus 
n = 400
fclk = 4e6
fsound = 20000
t = np.arange(n)/fclk
x = 0.5 + 0.5*np.sin(2*np.pi*fsound*t)

# plot pdm signal
pdmval, error = pdm(x)

meansize = 40 
meanvals = slipping_mean(pdmval, meansize)

#plt.step(t, error)
plt.step(t, pdmval)
# print sinus
plt.plot(t, x)
# print slipping mean
halfmeansize = int(meansize/2)
print(f"halfmeansize {halfmeansize}")
plt.plot(t, meanvals[halfmeansize:-(halfmeansize)])

plt.show()
