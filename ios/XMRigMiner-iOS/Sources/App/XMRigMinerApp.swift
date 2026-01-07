//
//  XMRigMinerApp.swift
//  XMRigMiner-iOS
//
//  Main app entry point
//

import SwiftUI

@main
struct XMRigMinerApp: App {
    @StateObject private var miner = XMRigWrapper()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(miner)
        }
    }
}
