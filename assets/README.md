#Some sample to test CIC filter.

## xz

Some files are too big for git. Then it was compressed with xz program. Should
be uncompressed with xz:

```
$ xz filename.xz 
```

## csv

CSV files as been recorded with sigrok, the format is :

```
time, clk, data
0,1,0
0,1,0
0,0,0
...
```

There is a bug in pulseview that record always 0 time value.

## npy

`npy` files are python numpy array that record only data value each rising edge
of clock.


## wave

Wave file are restitued sound value.

- colamonptitfrere_12.5Ms_slippingmean.npy.wav: flute playing filtered with
  python slipping mean method 
