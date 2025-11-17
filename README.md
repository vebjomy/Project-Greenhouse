# 🌿 Smart Greenhouse Network System

**Course:** IDATA2304 – Computer Communication and Network Programming  
**Team:** Project group 7  
**Institution:** NTNU Ålesund

---

## 📋 Table of Contents
- [Overview](#-overview)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Technology Stack](#-technology-stack)
- [Getting Started](#-getting-started)
- [Usage Guide](#-usage-guide)
- [Project Structure](#-project-structure)
- [Protocol](#-protocol)
- [Team](#-team)
- [Documentation](#-documentation)

---

## 🎯 Overview

Welcome to the **Smart Greenhouse Network System** – a distributed IoT application designed for real-time greenhouse monitoring and control.

This project is part of the **IDATA2304** course at **NTNU Ålesund** and demonstrates professional network programming practices using a custom **TCP-based application-layer protocol**.

### What We Built

A complete **client-server system** where:
- 🖥️ **Server** simulates multiple greenhouse nodes with realistic sensor behavior
- 💻 **Client** provides a JavaFX GUI for monitoring and controlling actuators
- 🔌 **Protocol** enables reliable, real-time communication over TCP/IP

> **"Smart control, efficient growth."**

---

## ✨ Key Features

### 🌱 Real-Time Monitoring
- **4 Sensor Types**: Temperature, Humidity, Light, pH
- **Physics Simulation**: Realistic environmental behavior

### 🎛️ Actuator Control
- **Fan**: Ventilation control (affects temperature, humidity)
- **Water Pump**: Irrigation control (affects humidity, pH)
- **CO₂ Generator**: CO₂ enrichment (affects temperature, pH)
- **Window**: Adjustable ventilation (CLOSED/HALF/OPEN, affects light)

### 👥 Multi-User System
- **Authentication**: Secure login/registration
- **Role-Based Access**: Admin and User roles
- **User Management**: Admin panel for CRUD operations

### 📊 Advanced UI
- **Dashboard**: Real-time sensor cards with color-coded status
- **Statistics**: Average values and bar charts
- **Activity Log**: Timestamped action history
- **Command Terminal**: Advanced operations via CLI

### 🔧 Scalability
- **Multiple Nodes**: Support for 10+ greenhouse nodes
- **Concurrent Clients**: Multiple control panels simultaneously
- **Extensible Protocol**: Easy to add new sensor/actuator types

---

## 🏗️ System Architecture
```
┌─────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │   Client 1  │  │   Client 2  │  │   Client N  │     │
│  │  (JavaFX)   │  │  (JavaFX)   │  │  (JavaFX)   │     │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘     │
└─────────┼─────────────────┼─────────────────┼───────────┘
          │                 │                 │
          └─────────────────┼─────────────────┘
                            │
                   TCP/IP (Port 5555)
                   JSON Messages
                            │
┌───────────────────────────┼───────────────────────────────┐
│                    SERVER LAYER                           │
│                   ┌────────┴────────┐                     │
│                   │ GreenhouseServer│                     │
│                   └────────┬────────┘                     │
│          ┌─────────────────┼─────────────────┐           │
│          │                 │                 │           │
│   ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐    │
│   │ClientHandler│  │ NodeManager │  │SensorEngine │    │
│   │(per client) │  │   (State)   │  │(Simulation) │    │
│   └─────────────┘  └─────────────┘  └──────┬──────┘    │
│                                             │           │
│                                    ┌────────▼────────┐  │
│                                    │EnvironmentState │  │
│                                    │ (Physics Model) │  │
│                                    └─────────────────┘  │
└───────────────────────────────────────────────────────┘
```

### Communication Model

**Hybrid Push/Pull Architecture:**
1. **Pull**: Client requests initial topology (`get_topology`)
2. **Push**: Server broadcasts real-time updates (`sensor_update`)
3. **Command**: Client sends actuator commands (`command`)

---

## 💻 Technology Stack

### Server Side
| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 |
| Transport | TCP Sockets | - |
| Threading | ExecutorService | - |
| Serialization | Jackson JSON | 2.17.0 |
| Simulation | ScheduledExecutorService | - |
| Database | JSON file (users.json) | - |

### Client Side
| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 |
| UI Framework | JavaFX | 20 |
| JSON Processing | Jackson | 2.17.0 |
| Async API | CompletableFuture | - |
| Build Tool | Maven | 3.8+ |

### Protocol
- **Transport**: TCP/IP
- **Port**: 5555
- **Format**: JSON (newline-delimited)
- **Encoding**: UTF-8
- **Message Types**: 20+ (hello, topology, command, sensor_update, etc.)

---

## 🚀 Getting Started

### Prerequisites
```bash
# Check Java version (must be 21+)
java -version

# Check Maven version (must be 3.8+)
mvn -version
```

### Installation

**1. Clone the repository:**
```bash
git clone https://github.com/vebjomy/Project-Greenhouse.git
cd Project-Greenhouse
```

**2. Build the project:**
```bash
mvn clean install
```

### Running the Application

**Option 1: Using Maven (Recommended)**
```bash
# Terminal 1 - Start Server
cd Server
mvn exec:java -Dexec.mainClass="server.ServerApp"

# Terminal 2 - Start Client
cd Client
mvn javafx:run
```

**Option 2: Using JAR files**
```bash
# Build JARs first
mvn clean package

# Terminal 1 - Server
java -jar Server/target/Server-1.0-SNAPSHOT.jar

# Terminal 2 - Client
java -jar Client/target/Client-1.0-SNAPSHOT.jar
```

### First Login

1. **Enter Server IP**: `127.0.0.1` (for local testing)
2. **Login with default admin account**:
   - Username: `admin`
   - Password: `admin123`
3. **Or register a new account**

---

## 📖 Usage Guide

### Dashboard Operations

#### 1. **Adding a New Node**
```
1. Click "+ Add Node" button
2. Fill in details:
   - Name: e.g., "Greenhouse A-1"
   - Location: e.g., "North Wing"
   - IP: Use "Auto-generate" or enter manually
3. Select sensors and actuators (click to toggle)
4. Click "OK"
```

#### 2. **Controlling Actuators**
```
Fan/Water Pump/CO₂:
  • Click "SET ON" to activate
  • Click "SET OFF" to deactivate

Window:
  • Click "CLOSED" for closed position
  • Click "HALF" for partial opening
  • Click "OPEN" for full opening
```

#### 3. **Viewing Statistics**
```
1. Click "Statistics" in left sidebar
2. View:
   - Average values across all nodes
   - Bar charts per sensor type
   - Color-coded visualization
```

#### 4. **Managing Users** (Admin only)
```
1. Click "Users" in left sidebar
2. Operations:
   - Add User: Create new accounts
   - Edit User: Modify username/role
   - Delete User: Remove accounts
```

#### 5. **Using Command Terminal**
```
Commands available:
  help              - Show all commands
  topology          - List all nodes
  status            - Show connection status
  ping              - Test server connection
  create_node <name> - Create new node
```

---

## 📁 Project Structure
```
Project-Greenhouse/
│
├── Client/                          # JavaFX GUI Application
│   ├── src/main/java/
│   │   ├── App/                     # Application entry point
│   │   │   └── MainApp.java         # Main class
│   │   ├── controller/              # MVC Controllers
│   │   │   ├── DashboardController.java
│   │   │   ├── LoginController.java
│   │   │   ├── StatisticsController.java
│   │   │   └── UsersController.java
│   │   ├── core/                    # Core client logic
│   │   │   ├── ClientApi.java       # High-level API
│   │   │   ├── ClientState.java     # State management
│   │   │   └── RequestManager.java  # Request correlation
│   │   ├── model/                   # Data models
│   │   │   ├── Node.java            # Node representation
│   │   │   └── User.java            # User model
│   │   ├── ui/                      # JavaFX Views
│   │   │   ├── DashboardView.java
│   │   │   ├── LoginScreenView.java
│   │   │   ├── StatisticsView.java
│   │   │   ├── UsersView.java
│   │   │   └── components/          # Reusable UI components
│   │   │       ├── TemperatureSensorView.java
│   │   │       ├── HumiditySensorView.java
│   │   │       ├── FanActuatorView.java
│   │   │       └── ...
│   │   ├── service/                 # Business services
│   │   │   └── AuthenticationService.java
│   │   └── net/                     # Network layer
│   │       └── NetworkClient.java   # TCP client
│   └── src/main/resources/          # Resources
│       ├── css/                     # Stylesheets
│       ├── images/                  # Images
│       └── icons/                   # Icon assets
│
├── Server/                          # TCP Server Application
│   └── src/main/java/
│       └── server/                  # Server core
│           ├── ServerApp.java       # Main entry point
│           ├── GreenhouseServer.java # TCP server
│           ├── ClientHandler.java   # Per-client handler
│           ├── ClientRegistry.java  # Session management
│           ├── NodeManager.java     # Node CRUD operations
│           ├── NodeRuntime.java     # Runtime state
│           ├── SensorEngine.java    # Simulation scheduler
│           ├── EnvironmentState.java # Physics model
│           └── UserService.java     # User management
│
├── protocol/                        # Shared Protocol Layer
│   ├── src/main/java/
│   │   ├── dto/                     # Data Transfer Objects
│   │   │   ├── Auth.java
│   │   │   ├── Command.java
│   │   │   ├── SensorUpdate.java
│   │   │   ├── Topology.java
│   │   │   └── ... (20+ message types)
│   │   └── net/                     # Network utilities
│   │       ├── MessageCodec.java    # JSON serialization
│   │       └── MessageTypes.java    # Message type constants
│   └── protocol.md                  # Protocol documentation
│
├── .github/                         # GitHub configuration
├── wiki/                            # Project wiki (documentation)
│   ├── Home.md
│   ├── Documentation.md
│   ├── Implementation.md
│   ├── Features.md
│   ├── Appendix.md
│   └── Scrum-Meetings.md
│
├── pom.xml                          # Parent POM
├── README.md                        # This file
└── users.json                       # User database (created at runtime)
```

---

## 📡 Protocol

### Message Format

Every message is a JSON object terminated by `\n`:
```json
{
  "type": "message_type",
  "id": "correlation-id",
  "...": "type-specific fields"
}
```

### Key Message Types

| Type | Direction | Purpose |
|------|-----------|---------|
| `hello` / `welcome` | Bidirectional | Session handshake |
| `auth` / `auth_response` | C→S / S→C | Authentication |
| `get_topology` / `topology` | C→S / S→C | Node discovery |
| `create_node` | Client → Server | Add new node |
| `subscribe` | Client → Server | Subscribe to updates |
| `sensor_update` | Server → Client | Real-time data |
| `command` | Client → Server | Control actuators |
| `ack` / `error` | Server → Client | Response |

### Example Communication Flow
```json
// 1. Client connects and authenticates
C→S: {"type":"hello","id":"c-1","clientId":"ui-7c5e"}
S→C: {"type":"welcome","server":"GreenhouseServer","version":"1.0"}
C→S: {"type":"auth","id":"c-2","username":"admin","password":"admin123"}
S→C: {"type":"auth_response","id":"c-2","success":true,"role":"admin"}

// 2. Client requests topology
C→S: {"type":"get_topology","id":"c-3"}
S→C: {"type":"topology","id":"c-3","nodes":[{"id":"node-1",...}]}

// 3. Client subscribes to real-time updates
C→S: {"type":"subscribe","id":"c-4","nodes":["*"],"events":["sensor_update"]}
S→C: {"type":"ack","id":"c-4","status":"ok"}

// 4. Server pushes updates
S→C: {"type":"sensor_update","nodeId":"node-1","timestamp":1730123999000,
      "data":{"temperature":22.6,"humidity":55.2,"fan":"ON",...}}

// 5. Client sends command
C→S: {"type":"command","id":"c-5","nodeId":"node-1","target":"fan",
      "action":"set","params":{"on":true}}
S→C: {"type":"ack","id":"c-5","status":"ok"}
```

**Full Protocol Documentation**: [protocol/protocol.md](protocol/protocol.md)

---

## 👥 Team

| Name | Role | Responsibilities |
|------|------|------------------|
| **Vebjørn Otneim Myklebust** | Developer | Backend Development, Server Architecture |
| **Dymitri Daniel Thorgeirsson** | Developer | Backend Development, Protocol Design, Documentation |
| **Eyob Mengsteab Berhane** | Developer | Backend Development, Testing, Documentation |
| **Arkadii Navrotskyi** | Scrum Master / Developer | Backend & Frontend Development, UI/UX Design |

### Collaboration Tools

- 💻 **GitHub**: Version control, issue tracking, code review
- 🌿 **Git Workflow**: Feature branches, pull requests, code reviews
- 💬 **Discord**: Daily communication, progress updates
- 📅 **Scrum**: Weekly sprints, sprint retrospectives
- 📚 **GitHub Wiki**: Centralized documentation

---

## 📚 Documentation

### Project Wiki
Visit our [GitHub Wiki](../../wiki) for comprehensive documentation:
- 🏠 [Home](../../wiki/Home) - Project overview
- 📘 [Documentation](../../wiki/Documentation) - Architecture, protocol, user manual
- ⚙️ [Implementation](../../wiki/Implementation) - Technical details, code structure
- ✅ [Features](../../wiki/Features) - Feature checklist and status
- 🧩 [Appendix](../../wiki/Appendix) - UML diagrams, glossary
- 📅 [Scrum Meetings](../../wiki/Scrum-Meetings) - Sprint reports

### Key Documents
- [Protocol Specification](protocol/protocol.md) - Complete protocol documentation
- [Project Requirements](IDATA2304_CCNP_Project.pdf) - Original assignment
- [API Documentation](../../wiki/Documentation#api-documentation) - ClientApi reference

---

## 🎓 Learning Outcomes

This project demonstrates mastery of:
- ✅ **Network Programming**: TCP sockets, client-server architecture
- ✅ **Protocol Design**: Custom application-layer protocol
- ✅ **Concurrent Programming**: Multi-threaded server, async client
- ✅ **GUI Development**: JavaFX, MVC pattern, reactive UI
- ✅ **Software Engineering**: Clean code, SOLID principles, documentation
- ✅ **Team Collaboration**: Git workflow, Scrum methodology

---

## 📝 License

This project is part of academic coursework at NTNU Ålesund.  
**Course**: IDATA2304 – Computer Communication and Network Programming  
**Semester**: Fall 2025

---

## 🙏 Acknowledgments

- **NTNU Ålesund** for providing the project assignment
- **Course Instructors** for guidance and feedback

---

## 📞 Contact

For questions or feedback about this project:
- 📧 Create an issue on [GitHub Issues](../../issues)
- 📚 Check the [Wiki](../../wiki) for detailed documentation
- 💬 Review the [Protocol Documentation](protocol/protocol.md)

---

<div align="center">

**Built with ❤️ by Project Group 7**

[View Demo](../../wiki) • [Report Bug](../../issues) • [Read Docs](../../wiki/Documentation)

</div>

