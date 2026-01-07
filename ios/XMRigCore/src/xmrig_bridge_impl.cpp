#include "xmrig_bridge.h"

// XMRig core headers
#include "App.h"
#include "base/kernel/Process.h"
#include "core/Controller.h"
#include "core/Miner.h"
#include "backend/common/interfaces/IBackend.h"
#include "backend/common/Hashrate.h"
#include "net/Network.h"

#include <iostream>
#include <thread>
#include <vector>
#include <string>
#include <mutex>
#include <atomic>
#include <fstream>
#include <unistd.h>
#include <signal.h>

using namespace xmrig;

// Global state
static std::thread g_mining_thread;
static std::atomic<bool> g_is_running{false};
static std::atomic<bool> g_should_stop{false};
static Process* g_process = nullptr;
static App* g_app = nullptr;
static Controller* g_controller = nullptr;
static std::string g_config_path = "";
static std::mutex g_mutex;

// Stats cache (updated periodically)
static std::atomic<double> g_hashrate_10s{0.0};
static std::atomic<double> g_hashrate_60s{0.0};
static std::atomic<double> g_hashrate_15m{0.0};
static std::atomic<uint64_t> g_accepted_shares{0};
static std::atomic<uint64_t> g_rejected_shares{0};
static std::atomic<int> g_threads{0};

extern "C" {

int xmrig_init(const char* config_json) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (g_is_running) return -1;

    // Save config to a temporary file
    char temp_path[] = "/tmp/xmrig_config_XXXXXX.json";
    int fd = mkstemps(temp_path, 5);
    if (fd == -1) return -2;
    
    close(fd);
    g_config_path = temp_path;
    
    std::ofstream out(g_config_path);
    if (!out.is_open()) return -3;
    
    out << config_json;
    out.close();

    g_should_stop = false;
    
    return 0;
}

int xmrig_start(void) {
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (g_is_running) return 0;
    if (g_config_path.empty()) return -1;

    g_is_running = true;
    g_should_stop = false;
    
    g_mining_thread = std::thread([]() {
        std::vector<const char*> args = {"xmrig", "-c", g_config_path.c_str()};
        int argc = (int)args.size();
        char** argv = const_cast<char**>(args.data());

        g_process = new Process(argc, argv);
        g_app = new App(g_process);
        
        // exec() blocks until mining stops
        g_app->exec();
        
        g_is_running = false;
        
        delete g_app;
        delete g_process;
        g_app = nullptr;
        g_process = nullptr;
        g_controller = nullptr;
    });
    
    // Detach so it runs independently
    g_mining_thread.detach();

    return 0;
}

void xmrig_stop(void) {
    if (!g_is_running) return;
    
    g_should_stop = true;
    
    // Send SIGINT to trigger graceful shutdown
    // XMRig handles SIGINT for clean exit
    raise(SIGINT);
    
    // Give it some time to shut down
    std::this_thread::sleep_for(std::chrono::milliseconds(500));
    
    g_is_running = false;
}

bool xmrig_is_running(void) {
    return g_is_running.load();
}

void xmrig_get_stats(XMRigStats* stats) {
    if (!stats) return;
    memset(stats, 0, sizeof(XMRigStats));
    
    stats->is_mining = g_is_running.load();
    
    if (!g_is_running) return;
    
    // Try to get hashrate from the miner backends
    // This is a simplified approach - in reality we'd need to access
    // the controller through proper XMRig internals
    
    // For now, return cached values that would be updated by a timer
    stats->hashrate_10s = g_hashrate_10s.load();
    stats->hashrate_60s = g_hashrate_60s.load();
    stats->hashrate_15m = g_hashrate_15m.load();
    stats->accepted_shares = g_accepted_shares.load();
    stats->rejected_shares = g_rejected_shares.load();
    stats->threads = g_threads.load();
    
    // Note: To properly implement this, we would need to:
    // 1. Hook into XMRig's event system
    // 2. Or, read from XMRig's HTTP API (if enabled)
    // 3. Or, parse XMRig's log output
    // 
    // The current implementation provides the framework,
    // but full integration requires modifications to XMRig's build
    // to expose internal state.
}

double xmrig_get_hashrate(void) {
    return g_hashrate_10s.load();
}

void xmrig_set_threads(int threads) {
    g_threads = threads;
    // Note: Changing threads at runtime requires restarting
    // with a new config file
}

void xmrig_cleanup(void) {
    xmrig_stop();
    
    std::lock_guard<std::mutex> lock(g_mutex);
    
    if (!g_config_path.empty()) {
        unlink(g_config_path.c_str());
        g_config_path.clear();
    }
    
    // Reset all stats
    g_hashrate_10s = 0.0;
    g_hashrate_60s = 0.0;
    g_hashrate_15m = 0.0;
    g_accepted_shares = 0;
    g_rejected_shares = 0;
    g_threads = 0;
}

const char* xmrig_version(void) {
    return "6.25.0";
}

// Additional helper to update stats from outside (e.g., by parsing logs)
void xmrig_update_stats(double hr10s, double hr60s, double hr15m, 
                         uint64_t accepted, uint64_t rejected, int threads) {
    g_hashrate_10s = hr10s;
    g_hashrate_60s = hr60s;
    g_hashrate_15m = hr15m;
    g_accepted_shares = accepted;
    g_rejected_shares = rejected;
    g_threads = threads;
}

} // extern "C"
