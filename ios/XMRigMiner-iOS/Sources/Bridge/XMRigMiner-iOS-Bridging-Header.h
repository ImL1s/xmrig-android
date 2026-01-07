//
//  XMRigMiner-iOS-Bridging-Header.h
//  XMRigMiner-iOS
//
//  Bridging header to expose Objective-C code to Swift
//

#ifndef XMRigMiner_iOS_Bridging_Header_h
#define XMRigMiner_iOS_Bridging_Header_h

#import <Foundation/Foundation.h>
#include "xmrig_bridge.h"

// Forward declare the XMRigBridge Objective-C class
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

#endif /* XMRigMiner_iOS_Bridging_Header_h */
