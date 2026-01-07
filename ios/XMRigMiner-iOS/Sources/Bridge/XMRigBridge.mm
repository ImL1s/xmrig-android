/**
 * XMRig Bridge Implementation for iOS
 * Objective-C++ wrapper for Swift interop
 */

#import <Foundation/Foundation.h>
#import "xmrig_bridge.h"

// Forward declaration - actual implementation would link to XMRig static lib
// This is a placeholder that demonstrates the interface

static bool g_is_mining = false;
static XMRigStats g_stats = {0};
static int g_threads = 0;

@interface XMRigBridge : NSObject

+ (instancetype)shared;
- (BOOL)initializeWithConfig:(NSString *)jsonConfig;
- (BOOL)startMining;
- (void)stopMining;
- (BOOL)isRunning;
- (NSDictionary *)getStats;
- (double)getCurrentHashrate;
- (void)setThreads:(int)count;
- (NSString *)getVersion;

@end

@implementation XMRigBridge

+ (instancetype)shared {
    static XMRigBridge *instance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[XMRigBridge alloc] init];
    });
    return instance;
}

- (BOOL)initializeWithConfig:(NSString *)jsonConfig {
    const char *config = [jsonConfig UTF8String];
    int result = xmrig_init(config);
    return result == 0;
}

- (BOOL)startMining {
    int result = xmrig_start();
    return result == 0;
}

- (void)stopMining {
    xmrig_stop();
}

- (BOOL)isRunning {
    return xmrig_is_running();
}

- (NSDictionary *)getStats {
    XMRigStats stats;
    xmrig_get_stats(&stats);
    
    return @{
        @"hashrate_10s": @(stats.hashrate_10s),
        @"hashrate_60s": @(stats.hashrate_60s),
        @"hashrate_15m": @(stats.hashrate_15m),
        @"total_hashes": @(stats.total_hashes),
        @"accepted_shares": @(stats.accepted_shares),
        @"rejected_shares": @(stats.rejected_shares),
        @"is_mining": @(stats.is_mining),
        @"threads": @(stats.threads)
    };
}

- (double)getCurrentHashrate {
    return xmrig_get_hashrate();
}

- (void)setThreads:(int)count {
    xmrig_set_threads(count);
}

- (NSString *)getVersion {
    const char *version = xmrig_version();
    return [NSString stringWithUTF8String:version];
}

@end

// C API Implementation (placeholder for actual XMRig integration)

int xmrig_init(const char* config_json) {
    // TODO: Parse JSON and initialize XMRig
    NSLog(@"XMRig init with config: %s", config_json);
    return 0;
}

int xmrig_start(void) {
    g_is_mining = true;
    NSLog(@"XMRig mining started");
    return 0;
}

void xmrig_stop(void) {
    g_is_mining = false;
    NSLog(@"XMRig mining stopped");
}

bool xmrig_is_running(void) {
    return g_is_mining;
}

void xmrig_get_stats(XMRigStats* stats) {
    if (stats) {
        *stats = g_stats;
        stats->is_mining = g_is_mining;
        stats->threads = g_threads;
    }
}

double xmrig_get_hashrate(void) {
    return g_stats.hashrate_10s;
}

void xmrig_set_threads(int threads) {
    g_threads = threads;
}

void xmrig_cleanup(void) {
    xmrig_stop();
}

const char* xmrig_version(void) {
    return "6.25.0";
}
