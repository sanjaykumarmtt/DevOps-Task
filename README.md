# Redis Cluster Lifecycle Tool

An enterprise-grade, comprehensive Java CLI tool that orchestrates Ansible over Podman to provision, operate, and perform a zero-downtime rolling upgrade of a 6-node Redis Cluster with verified data integrity.

## System Architecture

The infrastructure relies on a 6-node Redis cluster (3 masters + 3 replicas) running inside Ubuntu containers managed via Podman. Our Java CLI tool (`redis-tool`) serves as the central orchestrator, programmatically invoking Ansible playbooks to execute all cluster lifecycle events.

### Network & Node Topology

The containers run on a static subnet with fixed IPs and predictably mapped SSH ports to support Ansible's remote execution:

| Node Name    | Role    | IP Address   | SSH Port |
|--------------|---------|--------------|----------|
| redis-node-1 | Master  | 10.10.0.11   | 2222     |
| redis-node-2 | Master  | 10.10.0.12   | 2223     |
| redis-node-3 | Master  | 10.10.0.13   | 2224     |
| redis-node-4 | Replica | 10.10.0.14   | 2225     |
| redis-node-5 | Replica | 10.10.0.15   | 2226     |
| redis-node-6 | Replica | 10.10.0.16   | 2227     |

Ansible connects to these managed nodes securely via key-based SSH authentication without requiring passwords.

## Prerequisite Check Logic

To ensure absolute reliability, the Java tool executes a global, mandatory prerequisite check at the very entry point before routing to any business logic.

1. **Dynamic Binary Parsing:** The application dynamically runs and parses version outputs from system binaries to detect Podman (e.g., 4.9.3) and Ansible (e.g., 2.16.3).
2. **Version Enforcement:** The tool strictly verifies that Ansible is running version `>= 2.14` and that a compatible container runtime exists.
3. **Strict Error Handling:** If any dependencies are missing or outdated, the tool presents explicit installation instructions (e.g., pointing to `pip install ansible` or the official Podman docs) and immediately terminates via `System.exit(1)`.

Example runtime output:
```text
✓ Podman 4.9.3 found
✓ Ansible 2.16.3 found
Proceeding...
```

## Directory Structure Tree

The project strictly follows the requested production-ready `/submission` layout:

```text
submission/
├── redis-tool                  # Primary CLI wrapper script (Bash) calling the compiled JAR
├── ansible/
│   ├── ansible.cfg             # Ansible local configuration file
│   ├── inventory/
│   │   └── hosts.ini           # Static inventory file mapping IPs, roles, and SSH ports
│   ├── playbooks/
│   │   ├── provision.yml       # Orchestrates the initial Redis setup and clustering
│   │   ├── upgrade.yml         # Drives the zero-downtime rolling upgrade mechanism
│   │   └── status.yml          # Fetches dynamic metrics from all cluster nodes
│   └── roles/
│       └── redis/              # Standalone Ansible role for Redis installation and config
│           ├── tasks/
│           ├── handlers/
│           ├── templates/      # Jinja2 templates (e.g., redis.conf.j2)
│           └── defaults/
├── infra/
│   ├── Containerfile           # Ubuntu base image definition with OpenSSH server
│   └── compose.yml             # Podman compose configuration for spinning up the 6 nodes
├── README.md                   # This comprehensive technical document
└── output/                     # Historical logs from test executions
    ├── provision_output.txt
    ├── data_seed_output.txt
    ├── status_output.txt
    ├── upgrade_output.txt
    └── verify_output.txt
```

## Comprehensive Usage Guide

To utilize the suite, ensure the `infra/compose.yml` environment is up and running via Podman, then execute the following steps via the `./redis-tool` script.

### 1. Provision the Cluster
Install Redis, configure cluster settings, and bind the 6 nodes together into a master-replica topology.

```bash
./redis-tool provision --version 7.0.15 --masters 3 --replicas-per-master 1
```

### 2. Seed & Verify Data
Inject deterministic key-value pairs to baseline the cluster, simulating a real-world workload prior to any upgrade.

```bash
# Seed 1000 keys distributed across hash slots
./redis-tool data seed --keys 1000

# Verify read access and payload accuracy
./redis-tool data verify
```

### 3. Check Real-Time Status
Poll the cluster for live telemetry, checking version parity, key distribution, memory footprints, and replication health.

```bash
./redis-tool status
```

### 4. Execute Rolling Upgrade
Orchestrate a zero-client-downtime rolling upgrade to the target version, promoting replicas securely before upgrading the former masters.

```bash
./redis-tool upgrade --target-version 7.2.6 --strategy rolling
```

### 5. Final Full Verification
Run a rigorous post-upgrade health check evaluating topology completeness, zero data loss, and synchronous replication state.

```bash
./redis-tool verify --full
```

## Best Practices

- **Isolated State Management:** All dynamic runtime state files, localized caches, and runtime keys generated within the `data/` directory are strictly excluded from version control via our `.gitignore`. This ensures a clean repository state and avoids accidental leakage of secure assets or test artifacts.
- **Idempotency:** The Ansible tasks are crafted to be purely idempotent, meaning repeated executions of `provision` on an existing cluster will not result in data loss or configuration drift.
- **Strict Isolation:** No direct `redis-cli` access or manual SSH manipulation is required. The Java wrapper encapsulates all complexity, ensuring a safe execution pipeline.
