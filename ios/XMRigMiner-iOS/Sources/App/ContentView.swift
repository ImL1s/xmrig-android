//
//  ContentView.swift
//  XMRigMiner-iOS
//
//  Main content view
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject var miner: XMRigWrapper
    @State private var showConfig = false
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Status Card
                StatusCardView()
                
                // Stats Grid
                StatsGridView()
                
                Spacer()
                
                // Control Buttons
                ControlButtonsView(showConfig: $showConfig)
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("XMRig Miner")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { showConfig = true }) {
                        Image(systemName: "gear")
                    }
                }
            }
            .sheet(isPresented: $showConfig) {
                ConfigView()
            }
        }
    }
}

// MARK: - Status Card

struct StatusCardView: View {
    @EnvironmentObject var miner: XMRigWrapper
    
    var body: some View {
        VStack(spacing: 16) {
            // Status Indicator
            HStack {
                Circle()
                    .fill(miner.isRunning ? Color.green : Color.gray)
                    .frame(width: 12, height: 12)
                Text(miner.isRunning ? "Mining" : "Stopped")
                    .font(.headline)
                Spacer()
                Text("v\(miner.version)")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            // Hashrate Display
            VStack(spacing: 4) {
                Text(String(format: "%.1f", miner.stats.hashrate10s))
                    .font(.system(size: 48, weight: .bold, design: .monospaced))
                Text("H/s")
                    .font(.title3)
                    .foregroundColor(.secondary)
            }
        }
        .padding()
        .background(Color(.secondarySystemGroupedBackground))
        .cornerRadius(16)
        .padding()
    }
}

// MARK: - Stats Grid

struct StatsGridView: View {
    @EnvironmentObject var miner: XMRigWrapper
    
    var body: some View {
        LazyVGrid(columns: [
            GridItem(.flexible()),
            GridItem(.flexible())
        ], spacing: 12) {
            StatCell(title: "Accepted", value: "\(miner.stats.acceptedShares)", color: .green)
            StatCell(title: "Rejected", value: "\(miner.stats.rejectedShares)", color: .red)
            StatCell(title: "Total Hashes", value: formatHashes(miner.stats.totalHashes), color: .blue)
            StatCell(title: "Threads", value: "\(miner.stats.threads)", color: .orange)
        }
        .padding(.horizontal)
    }
    
    private func formatHashes(_ count: UInt64) -> String {
        if count >= 1_000_000 {
            return String(format: "%.1fM", Double(count) / 1_000_000)
        } else if count >= 1_000 {
            return String(format: "%.1fK", Double(count) / 1_000)
        }
        return "\(count)"
    }
}

struct StatCell: View {
    let title: String
    let value: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 4) {
            Text(value)
                .font(.title2)
                .fontWeight(.semibold)
                .foregroundColor(color)
            Text(title)
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.secondarySystemGroupedBackground))
        .cornerRadius(12)
    }
}

// MARK: - Control Buttons

struct ControlButtonsView: View {
    @EnvironmentObject var miner: XMRigWrapper
    @Binding var showConfig: Bool
    
    var body: some View {
        HStack(spacing: 16) {
            Button(action: {
                if miner.isRunning {
                    miner.stop()
                } else {
                    // TODO: Load config from storage
                    let config = MiningConfig(
                        pool: .supportXMR,
                        threads: 4
                    )
                    if miner.initialize(config: config) {
                        miner.start()
                    }
                }
            }) {
                Label(
                    miner.isRunning ? "Stop" : "Start",
                    systemImage: miner.isRunning ? "stop.fill" : "play.fill"
                )
                .font(.headline)
                .frame(maxWidth: .infinity)
                .padding()
                .background(miner.isRunning ? Color.red : Color.green)
                .foregroundColor(.white)
                .cornerRadius(12)
            }
        }
        .padding()
    }
}

#Preview {
    ContentView()
        .environmentObject(XMRigWrapper())
}
