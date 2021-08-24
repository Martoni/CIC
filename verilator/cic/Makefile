# Verilator makefile for UltraSoundMatrixWb

SIM=verilator
PWD=$(shell pwd)
CHISELDIR=$(PWD)/../..
CHISELSRCDIR=$(CHISELDIR)/src/main/scala
VERILATOR_ROOT = /usr/local/share/verilator

VG=valgrind

# PDM data sources
PDM_INC=pdm_vec.h


TOOLBOXDIR=../../toolbox
ASSETSDIR=../../assets

#PDM_SOURCE=$(ASSETSDIR)/flute_si_12_5Mhz.npy
PDM_SOURCE=$(ASSETSDIR)/colamonptitfrere_12.5Ms.npy
NP2VECTOR=$(TOOLBOXDIR)/np2CppVector.py

TIMESCALE=1ns/1ns
TOPLEVEL=CIC

VERILATOR_OPT=--threads 14 --trace -Wall

CPP_LIBRARIES=src/pdm.cpp

VERILATOR_CPP_SOURCES=src/testb.h\
					  src/npy.hpp\
					  src/sim_main.cpp\
					  src/pdm.h\
					  $(CPP_LIBRARIES)

CHISEL_SOURCES=$(CHISELSRCDIR)/cic.scala

.PHONY: simu

simu: obj_dir/V$(TOPLEVEL)
	@echo "***********************************"
	@echo "* launch simulation WITHOUT traces*"
	@echo "***********************************"
	./$<
	#$(VG) --tool=memcheck --track-origins=yes ./$<

simu_traces: obj_dir/V$(TOPLEVEL)
	@echo "********************************"
	@echo "* launch simulation with traces*"
	@echo "* traces files :"$(TOPLEVEL)".vcd"
	@echo "********************************"
	./$< -d $(TOPLEVEL).vcd

obj_dir/V$(TOPLEVEL): obj_dir/V$(TOPLEVEL).mk $(VERILATOR_CPP_SOURCES)
	make -j -C obj_dir -f V$(TOPLEVEL).mk V$(TOPLEVEL) -Isrc/

obj_dir/V$(TOPLEVEL).mk: obj_dir/$(TOPLEVEL).v $(CPP_LIBRARIES) $(VERILATOR_CFG)
	verilator $(VERILATOR_OPT) --timescale-override $(TIMESCALE)\
		--cc --exe src/sim_main.cpp obj_dir/$(TOPLEVEL).v $(CPP_LIBRARIES) $(VERILATOR_CFG)
	touch $@

obj_dir/$(TOPLEVEL).v: $(CHISEL_SOURCES) $(TAPCOEFFS)
	mkdir -p obj_dir
	cd $(CHISELDIR); sbt "runMain cic.$(TOPLEVEL)Driver"
	mv -v $(CHISELDIR)/$(TOPLEVEL).v obj_dir/

obj_dir/$(PDM_INC): $(PDM_SOURCE)
	python $(NP2VECTOR) -n $< -v $@

mrproper:
	rm -rvf obj_dir

clean_verilog:
	rm -v obj_dir/$(TOPLEVEL).v
