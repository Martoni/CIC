#include "pdm.h"
#include <iostream>
#include "npy.hpp"

using namespace std;

void PDM::init(void){
    cout << "Initialize PDM class" ;
    pdm_signals_fall.clear();
    pdm_signals_rise.clear();
    shape.clear();
    bool fortran_order;
    npy::LoadArrayFromNumpy(pdm_path_source_fall, shape,
                            fortran_order, pdm_signals_fall);
    shape.clear();
    npy::LoadArrayFromNumpy(pdm_path_source_rise, shape,
                            fortran_order, pdm_signals_rise);
    cout << "pdm_signals_fall size " << pdm_signals_fall.size() << endl;
    cout << "pdm_signals_rise size " << pdm_signals_rise.size() << endl;
    }

void PDM::pdm_tick(void){
    int pdm_per = 1 + ceil(pdm_clk_per_ns/sys_clk_per_ns);
    int half_pdm_per = pdm_per/2;
    static uint64_t tickcount = 0;
    static uint64_t pdm_index_fall = 0;
    static uint64_t pdm_index_rise = 0;
    static int old_clk = 0;

    if ((tickcount%pdm_per) < half_pdm_per){
        *pdm_clock = 0;
        /* Falling edge */
        if(old_clk == 1){
            *pdm_data = pdm_signals_fall[pdm_index_fall];
            if(pdm_index_fall >= pdm_signals_fall.size()){
                cerr << "No more pdm samples in file" << endl;
            } else {
                pdm_index_fall++;
            }
        }
    } else {
        *pdm_clock = 1;
        /* Rising edge */
        if(old_clk == 0){
           *pdm_data = pdm_signals_rise[pdm_index_rise];
           if(pdm_index_rise >= pdm_signals_rise.size()){
             cerr << "No more pdm samples in file" << endl;
           } else {
             pdm_index_rise++;
           }
        }
    }

    tickcount++;
    old_clk = *pdm_clock;
}
