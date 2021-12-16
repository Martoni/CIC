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

### prerequisite

To run tests, SNAPSHOT version of chisel3 is required :

- Chisel3:

```
git clone https://github.com/chipsalliance/chisel3.git
cd chisel3
sbt publishLocal
```

- firrtl:

```
git clone https://github.com/chipsalliance/firrtl.git
cd firrtl
sbt publishLocal
```

- Treadle:

```
git clone https://github.com/chipsalliance/treadle.git
cd treadle
sbt publishLocal
```

- chiseltest:

```
git clone https://github.com/ucb-bar/chisel-testers2.git
cd chisel-testers2
sbt publishLocal
```

### Launch test

```
$sbt -mem 16384 "testOnly cic.BasicTest -- -DwriteVcd=1"
```
