////////////////////////////////////////////////////////////////////////////////
//
// Filename:     testb.h
//
// Project:    vgasim, a Verilator based VGA simulator demonstration
//
// Purpose:    A wrapper for a common interface to a clocked FPGA core
//        begin exercised in Verilator.
//
// Creator:    Dan Gisselquist, Ph.D.
//        Gisselquist Technology, LLC
//
////////////////////////////////////////////////////////////////////////////////
//
// Copyright (C) 2017-2020, Gisselquist Technology, LLC
//
// This program is free software (firmware): you can redistribute it and/or
// modify it under the terms of  the GNU General Public License as published
// by the Free Software Foundation, either version 3 of the License, or (at
// your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTIBILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program.  (It's in the $(ROOT)/doc directory.  Run make with no
// target there if the PDF file isn't present.)  If not, see
// <http://www.gnu.org/licenses/> for a copy.
//
// License:    GPL, v3, as defined and found on www.gnu.org,
//        http://www.gnu.org/licenses/gpl.html
//
//
////////////////////////////////////////////////////////////////////////////////
//
//
#ifndef    TESTB_H
#define    TESTB_H

#include <stdio.h>
#include <stdint.h>
#include <verilated_vcd_c.h>


using namespace std;

template <class VA>    class TESTB {
public:
  VA        *m_core;
  VerilatedVcdC*    m_trace;
  uint64_t    m_tickcount;
  uint32_t    m_per=10;

  TESTB(void) : m_trace(NULL), m_tickcount(0l) {
      m_core = new VA;
      Verilated::traceEverOn(true);
      m_core->clock = 0;

      eval(); // Get our initial values set properly.
  }
  virtual ~TESTB(void) {
      closetrace();
      delete m_core;
      m_core = NULL;
  }

  virtual void opentrace(const char *vcdname) {
      if (!m_trace) {
          m_trace = new VerilatedVcdC;
          m_core->trace(m_trace, 99); // trace 99 levels of hierarchy
          m_trace->open(vcdname);
      }
  }

  virtual void closetrace(void) {
      if (m_trace) {
          m_trace->close();
          delete m_trace;
          m_trace = NULL;
      }
  }

  virtual void eval(void) {
      m_core->eval();
  }

  /* traces will be m_per times timescale */
  virtual void tick(bool preeval=true) {
      m_tickcount++;

      // Make sure we have our evaluations straight before the top
      // of the clock.  This is necessary since some of the
      // connection modules may have made changes, for which some
      // logic depends.  This forces that logic to be recalculated
      // before the top of the clock.
      if(preeval)
          eval();
      if (m_trace) m_trace->dump((vluint64_t)(m_per*m_tickcount-2));
      m_core->clock = 1;
      eval();
      if (m_trace) m_trace->dump((vluint64_t)(m_per*m_tickcount));
      m_core->clock = 0;
      eval();
      if (m_trace) {
          m_trace->dump((vluint64_t)(m_per*m_tickcount + m_per/2));
          m_trace->flush();
      }
  }

  virtual void reset(void) {
      m_core->reset = 1;
      tick();
      m_core->reset = 0;
  }

  unsigned long tickcount(void) {
      return m_tickcount;
  }
};

#endif
