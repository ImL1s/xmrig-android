//
//  ConfigView.swift
//  XMRigMiner-iOS
//
//  Mining configuration view
//

import SwiftUI

struct ConfigView: View {
    @Environment(\.dismiss) private var dismiss
    @State private var walletAddress = ""
    @State private var selectedPool = 0
    @State private var threads = 4
    @State private var workerName = ""
    
    private let pools = [
        ("SupportXMR", PoolConfig.supportXMR),
        ("MoneroOcean", PoolConfig.moneroOcean),
        ("HashVault", PoolConfig.hashVault),
        ("2Miners", PoolConfig.twoMiners)
    ]
    
    private var maxThreads: Int {
        ProcessInfo.processInfo.processorCount
    }
    
    var body: some View {
        NavigationStack {
            Form {
                // Wallet Section
                Section("Wallet") {
                    TextField("Monero Address", text: $walletAddress)
                        .font(.system(.body, design: .monospaced))
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                    
                    TextField("Worker Name (Optional)", text: $workerName)
                        .autocapitalization(.none)
                }
                
                // Pool Section
                Section("Pool") {
                    Picker("Select Pool", selection: $selectedPool) {
                        ForEach(0..<pools.count, id: \.self) { index in
                            Text(pools[index].0).tag(index)
                        }
                    }
                    .pickerStyle(.menu)
                    
                    HStack {
                        Text("URL")
                        Spacer()
                        Text(pools[selectedPool].1.url)
                            .foregroundColor(.secondary)
                            .font(.caption)
                    }
                }
                
                // Performance Section
                Section("Performance") {
                    Stepper("Threads: \(threads)", value: $threads, in: 1...maxThreads)
                    
                    HStack {
                        Text("CPU Cores")
                        Spacer()
                        Text("\(maxThreads)")
                            .foregroundColor(.secondary)
                    }
                }
                
                // Info Section
                Section {
                    HStack {
                        Text("XMRig Version")
                        Spacer()
                        Text("6.25.0")
                            .foregroundColor(.secondary)
                    }
                    
                    HStack {
                        Text("Algorithm")
                        Spacer()
                        Text("RandomX")
                            .foregroundColor(.secondary)
                    }
                } header: {
                    Text("Info")
                } footer: {
                    Text("⚠️ Mining consumes significant CPU resources and battery. Use responsibly.")
                        .font(.caption)
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        saveConfig()
                        dismiss()
                    }
                    .disabled(walletAddress.isEmpty)
                }
            }
        }
    }
    
    private func saveConfig() {
        var pool = pools[selectedPool].1
        pool.user = walletAddress
        if !workerName.isEmpty {
            pool.pass = workerName
        }
        
        let config = MiningConfig(
            pool: pool,
            threads: threads
        )
        
        // TODO: Save to UserDefaults or file
        print("Saving config: \(config)")
    }
}

#Preview {
    ConfigView()
}
