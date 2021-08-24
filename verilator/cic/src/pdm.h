#ifndef PDM_H
#define PDM_H

#include<cstdint>
#include "verilated.h"

using namespace std;

class PDM {
public:
    string pdm_path_source_fall;
    string pdm_path_source_rise;
    vector<long long> pdm_signals_fall;
    vector<long long> pdm_signals_rise;
    vector<unsigned long> shape;
    CData * pdm_clock;
    CData * pdm_data;
    int sys_clk_per_ns;
    int pdm_clk_per_ns;

public:
    PDM(CData * pdm_clock,
        CData * pdm_data,
        int sys_clk_per_ns,
        int pdm_clk_per_ns,
        string pdm_path_source_fall,
        string pdm_path_source_rise): pdm_clock(pdm_clock),
                         pdm_data(pdm_data),
                         sys_clk_per_ns(sys_clk_per_ns),
                         pdm_clk_per_ns(pdm_clk_per_ns),
                         pdm_path_source_fall(pdm_path_source_fall),
                         pdm_path_source_rise(pdm_path_source_rise) {};
    void init(void);
    void pdm_tick(void);
};

#endif //PDM_H
