Cascacaded Integrator Comb filter
==================================


## Generate Verilog

To generate module in verilog do :

```Shell
$ cd CIC
$ sbt
sbt:CIC> show discoveredMainClasses
[info] compiling 1 Scala source to /home/fabienm/myfpga/CIC/target/scala-2.12/classes ...
[info] * cic.CICDriver
[success] Total time: 5 s, completed 16 juil. 2021 07:26:55
sbt:CIC> runMain cic.CICDriver
[info] running cic.CICDriver 
Generate CIC verilog
Elaborating design...
Done elaborating.
[success] Total time: 3 s, completed 16 juil. 2021 07:27:05
sbt:CIC> 
```

## Run tests

### Prepare assets

- First unflat sample file :

```Shell
$ cd assets/
$ xz -d colamonptitfrere_12.5Ms.csv.xz
```

- then convert it to numpy data format :

```Shell
$  python ../toolbox/csv2np.py --csv=colamonptitfrere_12.5Ms.csv --np=colamonptitfrere_12.5Ms
```

- and finally to pdm data format :

```Shell
$ python ../toolbox/csv2np.py --np=colamonptitfrere_12.5Ms.npy --pdm=colamonptitfrere_12.5Ms.pdm
```

### Launch test

```
$sbt -mem 16384 "testOnly cic.BasicTest -- -DwriteVcd=1"
```
