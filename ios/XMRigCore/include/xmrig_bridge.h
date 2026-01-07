/**
 * XMRig Bridge API for iOS
 * C interface for Swift/Objective-C interop
 */

#ifndef XMRIG_BRIDGE_H
#define XMRIG_BRIDGE_H

#ifdef __cplusplus
extern "C" {
#endif

#include <stdbool.h>
#include <stdint.h>

/**
 * Mining statistics structure
 */
typedef struct {
    double hashrate_10s;
    double hashrate_60s;
    double hashrate_15m;
    uint64_t total_hashes;
    uint64_t accepted_shares;
    uint64_t rejected_shares;
    bool is_mining;
    int threads;
} XMRigStats;

/**
 * Initialize XMRig with JSON configuration
 * @param config_json JSON string with mining configuration
 * @return 0 on success, error code otherwise
 */
int xmrig_init(const char* config_json);

/**
 * Start mining
 * @return 0 on success, error code otherwise
 */
int xmrig_start(void);

/**
 * Stop mining
 */
void xmrig_stop(void);

/**
 * Check if miner is currently running
 * @return true if mining, false otherwise
 */
bool xmrig_is_running(void);

/**
 * Get current mining statistics
 * @param stats Pointer to XMRigStats structure to fill
 */
void xmrig_get_stats(XMRigStats* stats);

/**
 * Get current hashrate (10s average)
 * @return Hashrate in H/s
 */
double xmrig_get_hashrate(void);

/**
 * Set number of mining threads
 * @param threads Number of threads (0 = auto)
 */
void xmrig_set_threads(int threads);

/**
 * Cleanup and release resources
 */
void xmrig_cleanup(void);

/**
 * Get XMRig version string
 * @return Version string (e.g., "6.25.0")
 */
const char* xmrig_version(void);

#ifdef __cplusplus
}
#endif

#endif /* XMRIG_BRIDGE_H */
