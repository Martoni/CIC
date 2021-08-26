#include <iostream>
#include <fstream>
#include <ostream>
#include <signal.h>
#include <time.h>
#include <ctype.h>
#include <unistd.h>

#include "VCIC.h"
#include "verilated.h"

#include "pdm.h"
#include "sound_monitor.h"

#include "testb.h"

#define PDM_CLK_PER_NS (222)

//#define PDM_SOURCE "../../assets/flute_si_12_5Mhz.npy"
#define PDM_SOURCE "../../assets/colamonptitfrere_12.5Ms.npy"

using namespace std;

/* Derivate zipcpu testb class */
class TESTBENCH : public TESTB<VCIC> {
private:
    bool m_done = false;
    bool m_test = false;
    int sys_clk_per_ns = 25;
    PDM *m_pdm;

public:

    SoundMonitor <IData> * m_cic;

    void init(void) {
        m_pdm->init();
        m_cic->sm_init();
        m_per = sys_clk_per_ns;
    }

    TESTBENCH(void) {
        m_pdm = new PDM( &m_core->io_pdm_clk,
                         &m_core->io_pdm_data,
                         sys_clk_per_ns, PDM_CLK_PER_NS,
                         PDM_SOURCE, PDM_SOURCE);

        m_cic = new SoundMonitor<IData>( &m_core->io_pcm_bits,
                                         &m_core->io_pcm_valid,
                                         string("cic.raw"));

        init();
    }


    void trace(const char *vcd_trace_file_name) {
        fprintf(stderr, "Opening TRACE(%s)\n", vcd_trace_file_name);
        opentrace(vcd_trace_file_name);
    }

    void close(void) {
        delete m_pdm;
        delete m_cic;
        m_done = true;
    }

    void test_input(bool test_data) {
        m_test = test_data;
    }

    void tick(bool preeval=true) {
        if (m_done)
            return;

        m_pdm->pdm_tick();
        m_cic->sm_tick();

        TESTB<VCIC>::tick(preeval);
    }

    void time_pass_us(int atime){
        for(int i=0; i < (atime*1000)/m_per; i++){
            tick();
        }
    }

    void time_pass_ms(int atime){
        time_pass_us(atime*1000);
    }
};

TESTBENCH *tb;

void usage(void) {
    fprintf(stderr,
            "Usage: main_tb [-vhd]\n"
            "\n"
            "\t-d <tracefile>vcd\tOpens a VCD file to contain a trace of all internal\n"
            "\t\t\t\t(and external) signals\n"
            "\t-h\tDisplays this usage message\n"
            "\t-c <coeffile.txt>\tCoefficients file name\n"
            "\t-a <audiocoeffile.txt>\tAudio coefficients tap file name\n"
            "\t-v\tVerbose\n");
}

void usage_kill(void) {
    fprintf(stderr, "ERR: Invalid usage\n\n");
    usage();
    exit(EXIT_FAILURE);
}

int main(int argc, char** argv, char** env) {

    VerilatedContext* contextp = new VerilatedContext;
    contextp->commandArgs(argc, argv);
    VCIC* top = new VCIC{contextp};

    char *trace_file = NULL;
    char *coeffs_file = NULL;
    char *audio_coeffs_file = NULL;
    bool test_data = false;
    bool verbose_flag;
    int opt;
    while((opt = getopt(argc, argv, "d:htg:c:a:")) != -1) {
        const char DELIMITERS[] = "x, ";
        switch(opt) {
            case 'd':
                if (verbose_flag)
                    fprintf(stderr, "Trace file -> %s\n", optarg);
                trace_file = strdup(optarg);
                break;
            case 'h':
                usage();
                exit(EXIT_SUCCESS);
                break;
            case 't':
                test_data = true;
                break;
            case 'v':
                verbose_flag = true;
            case 'c':
                if (verbose_flag)
                    fprintf(stderr, "tapcoeff file -> %s\n", optarg);
                coeffs_file = strdup(optarg);
                break;
            case 'a':
                if (verbose_flag)
                    fprintf(stderr, "audio TapCoeffs file -> %s\n", optarg);
                audio_coeffs_file = strdup(optarg);
                break;
        }
    }

    /* Testbench stimulis */
    tb = new TESTBENCH();
    if(trace_file != NULL){
        cout << "Opening trace file" << endl;
        tb->opentrace(trace_file);
    }
    tb->reset();

    tb->time_pass_us(10);
    
    int ms10 = 500;
    cout << "Simulating " << ms10*10 << " ms" << endl;
    for(int i=0; i < ms10; i++){
        tb->time_pass_ms(10);
        cout << 10*i << " ms" << endl;
    }

    cout << "End of simulation" << endl;
    tb->m_cic->sm_savefile(true);
    cout << "**" << endl;

    tb->close();
    return EXIT_SUCCESS;
}

