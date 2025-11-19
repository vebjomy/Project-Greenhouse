# Network protocol documentation
## Introduction
   This document describes the application-layer network protocol for the "Smart Greenhouse" project. It facilitates communication between a central Server (simulating the greenhouse nodes) and one or more Client (control panels).

## Terminology
* Server: The central application (`GreenhouseServer.java`) that manages the state of all nodes, runs simulations, and handles communication with clients.

* Client: A control panel application (`ClientApi.java`) used by a "farmer" to view data and control actuators.

* Node: A logical entity representing a single "Sensor/Actuator Node" in a greenhouse. In this architecture, nodes are simulated and managed by the Server.

* Sensor: A component on a Node that provides data (e.g., temperature).

* Actuator: A component on a Node that can be controlled (e.g., fan).

## Underlying Transport
   Transport: TCP/IP.

Port: 5555

Marshalling: Text-based. Each message is a single JSON object, terminated by a newline character (\n).

Encoding: UTF-8.

### Justification:

* TCP over UDP: TCP was chosen because it guarantees reliable, in-order delivery of messages. This is crucial for commands and configuration, eliminating the need to implement our own reliability layer.


* JSON: JSON was chosen for its human-readability and the wide availability of parsers in Java (like Jackson), which simplifies development.

### Overall Architecture
   Actors: The system has two main actors: the Server and the Client.

Model: The architecture is a Centralized Client-Server model.

The Server is the single source of truth. It holds the state of all nodes, simulates sensor readings, and applies actuator commands.

Multiple Clients (control panels) can connect to this single server simultaneously.


Scalability: The protocol supports multiple sensor/actuator types and multiple nodes.


### Justification:
A centralized model simplifies state management and ensures all clients see a consistent view of the system, which meets the project's fundamental requirements.

### Information Flow
   The protocol uses a hybrid push/pull model:

Pull (Client-initiated): The client pulls the system structure by sending `get_topology`.

Push (Server-initiated): After a client subscribes, the server pushes all `sensor_update` (including actuator status) and `node_change` events to the client. This is more efficient than constant polling.

Push (Client-initiated): The client pushes command messages to the server to control actuators.

### Protocol Type
   Connection: 
   * Connection-oriented (built on TCP).

State: 
* Stateful. The server must maintain the state of each client, specifically their subscription lists (`ClientRegistry.java`).

### Justification: 
A stateful, connection-oriented protocol is necessary to manage subscriptions. The server needs to know which client to send what updates to.

# 0. Common Structure
## 0.1. Message Envelope

Every message is a JSON object with at least these fields:

```java
{
"type": "string",          // message type
"id": "uuid-optional",     // correlation id for requests
"ts": 1730123456789        // optional timestamp (epoch ms)
}
```

# 0.2. Acknowledgments and Errors

* Server confirms success with ack
* On failure, server returns error with code and message

```java
{
  "type":"ack","id":"c-123","status":"ok"
}
{"type":"error","id":"c-123","code":"INVALID_ARG","message":"unknown actuator: turbo"}
```
### Error codes:

INVALID_ARG, NOT_FOUND, ALREADY_EXISTS, UNSUPPORTED, FORBIDDEN, INTERNAL.

## 0.3. Heartbeat

Optional keep-alive messages:

```java
{"type":"ping"}
{"type":"pong"}
```
# 1. Session Management
##   1.1. hello / welcome

Client announces itself; server responds with metadata.

### Client → Server
```java
{
"type":"hello",
"id":"c-1",
"clientId":"ui-7c5e",
"user":"local",
"capabilities":["topology","commands","subscribe"]
}
```
### Server → Client
```java
{
"type":"welcome",
"server":"greenhouse",
"version":"1.0",
"motd":"ready"
}
```


Authentication is handled locally in the client; the protocol itself does not require credentials (can be extended later with token).

# 2. Topology: Nodes, Sensors, Actuators
##   2.1. Node Model
```java
   {
   "id": "node-1",
   "name": "Greenhouse A-1",
   "location": "North-West Corner",
   "ip": "192.168.1.50",
   "sensors": ["temperature","humidity","light","ph"],
   "actuators": ["fan","water_pump","co2","window"]
   }
```
##   2.2. Sensors

| Name          | Unit | Range     | Example |
| ------------- | ---- | --------- | ------- |
| `temperature` | °C   | -20..60   | `23.4`  |
| `humidity`    | % RH | 0..100    | `56.2`  |
| `light`       | lux  | 0..200000 | `420`   |
| `ph`          | pH   | 0..14     | `6.4`   |

##   2.3. Actuators

| Actuator     | Command Example                              | Description                                 |
| ------------ | -------------------------------------------- | ------------------------------------------- |
| `fan`        | `{"action":"set","params":{"on":true}}`      | Turns the ventilation fan on/off            |
| `water_pump` | `{"action":"set","params":{"on":true}}`      | Controls irrigation pump                    |
| `co2`        | `{"action":"set","params":{"on":true}}`      | Controls CO₂ generator                      |
| `window`     | `{"action":"set","params":{"level":"OPEN"}}` | Controls window openness (CLOSED/HALF/OPEN) |

# 3. Node Management
##   3.1. Get Topology
### Client → Server
```java
{
  "type":"get_topology","id":"c-2"
}
```


### Server → Client
```java
{
"type":"topology","nodes":[{ /* Node */ }, { /* Node */ }]
}
```
##   3.2. Create / Update / Delete Node

### Create
```java
{"type":"create_node","id":"c-3","node":{
"name":"Greenhouse B-2",
"location":"South",
"ip":"192.168.1.77",
"sensors":["temperature","light"],
"actuators":["fan","window"]
}}
{"type":"ack","id":"c-3","status":"ok","nodeId":"node-9"}
```

### Update
```java
{"type":"update_node","id":"c-4","nodeId":"node-9","patch":{"name":"Greenhouse B-2 (Tomatoes)"}}
{"type":"ack","id":"c-4","status":"ok"}
```

### Delete
```java
{"type":"delete_node","id":"c-5","nodeId":"node-9"}
{"type":"ack","id":"c-5","status":"ok"}
```

## 3.3. Component Management via Edit Node

The system supports **atomic updates** to node components (sensors and actuators) through the `update_node` message. This replaces the entire component list in a single operation, ensuring consistency.

### Workflow

1. **Client** opens Edit Node Dialog (pre-populated with current components)
2. **User** adds/removes sensors or actuators via UI toggles
3. **Client** sends `update_node` with complete new lists
4. **Server** replaces old lists atomically

### Example: Adding a pH Sensor

**Current state:**
```json
{
  "nodeId": "node-1",
  "sensors": ["temperature", "humidity", "light"],
  "actuators": ["fan", "water_pump"]
}
```

**Client → Server:**
```json
{
  "type": "update_node",
  "id": "c-4",
  "nodeId": "node-1",
  "patch": {
    "sensors": ["temperature", "humidity", "light", "ph"],
    "actuators": ["fan", "water_pump"]
  }
}
```

**Server → Client:**
```json
{
  "type": "ack",
  "id": "c-4",
  "status": "ok"
}
```

**Server broadcasts to all subscribed clients:**
```json
{
  "type": "node_change",
  "op": "updated",
  "node": {
    "id": "node-1",
    "name": "Demo Greenhouse",
    "location": "Central",
    "ip": "127.0.0.1",
    "sensors": ["temperature", "humidity", "light", "ph"],
    "actuators": ["fan", "water_pump"]
  }
}
```

### Example: Removing an Actuator

**Client → Server:**
```json
{
  "type": "update_node",
  "id": "c-5",
  "nodeId": "node-1",
  "patch": {
    "sensors": ["temperature", "humidity", "light", "ph"],
    "actuators": ["fan"]
  }
}
```

### Benefits of Atomic Updates

- **Consistency**: No race conditions when adding/removing multiple components
- **Simplicity**: Single message type for all component changes
- **Efficiency**: Reduces network overhead compared to multiple `add_component`/`remove_component` calls
- **UI-friendly**: Matches the "Edit Node Dialog" workflow where user modifies all components at once
## 3.4. Sampling Interval
### Set Sampling Interval
```java
{"type":"set_sampling","id":"c-8","nodeId":"node-1","intervalMs":1000}
{"type":"ack","id":"c-8","status":"ok"}
```
# 4. Subscription (Live Updates)
##   4.1. Subscribe / Unsubscribe
### Subscribe
```java
{"type":"subscribe","id":"c-9","nodes":["node-1","node-2"],"events":["sensor_update","node_change"]}
```

### Unsubscribe
```java
{"type":"unsubscribe","id":"c-10","nodes":["node-2"],"events":["sensor_update"]}
```
Server responds with ack for both:
```java
{"type":"ack","id":"c-9","status":"ok"}
{"type":"ack","id":"c-10","status":"ok"}
```
##   4.2. sensor_update (Server → Client)
```java
{
  "type":"sensor_update",
  "nodeId":"node-1",
  "timestamp":1730123999000,
  "data":{
        "temperature": 22.6,
        "humidity": 55.2,
        "light": 420,
        "ph": 6.4,
        "fan": "ON",
        "water_pump": "OFF",
        "co2": "OFF",
        "window": "CLOSED"
  }
}
```
##   4.3. node_change (Server → Client)
```java
{"type":"node_change","op":"added","node":{ /* Node */ }}
{"type":"node_change","op":"updated","node":{ /* Node */ }}
{"type":"node_change","op":"removed","nodeId":"node-9"}
```
# 5. Commands (Actuator Control)
##   5.1. Unified Command Format
### Client → Server
```java
{
  "type":"command",
  "id":"c-11",
  "nodeId":"node-1",
  "target":"fan",
  "action":"set",
  "params":{"on":true}
}
```
### Server → Client
```java
{"type":"ack","id":"c-11","status":"ok"}
```
##   5.2. Command Payloads by Actuator
| Actuator   | Params                                      | Description            |
| ---------- | ------------------------------------------- | ---------------------- |
| fan        | `{ "on": boolean }`                         | Toggle ventilation fan |
| water_pump | `{ "on": boolean }`                         | Toggle irrigation      |
| co2        | `{ "on": boolean }`                         | Toggle CO₂ generation  |
| window     | `{ "level": "CLOSED" \| "HALF" \| "OPEN" }` | Adjust window position |

## 5.3. Examples
```java
{"type":"command","id":"c-12","nodeId":"node-1","target":"water_pump","action":"set","params":{"on":false}}
        {"type":"command","id":"c-13","nodeId":"node-1","target":"window","action":"set","params":{"level":"OPEN"}}
        {"type":"command","id":"c-14","nodeId":"node-1","target":"co2","action":"set","params":{"on":true}}
```


# 6. Message Reference
| Type               | Direction       | Purpose                            |
| ------------------ | --------------- |------------------------------------|
| `hello`            | Client → Server | Start session                      |
| `welcome`          | Server → Client | Server info                        |
| `ping` / `pong`    | Both            | Keep-alive                         |
| `get_topology`     | Client → Server | Request node list                  |
| `topology`         | Server → Client | Node list                          |
| `create_node`      | Client → Server | Add new node                       |
| `update_node`      | Client → Server | Modify node (including components) |
| `delete_node`      | Client → Server | Remove node                        |
| `set_sampling`     | Client → Server | Set data interval                  |
| `subscribe`        | Client → Server | Subscribe to updates               |
| `unsubscribe`      | Client → Server | Stop receiving updates             |
| `sensor_update`    | Server → Client | Live telemetry                     |
| `node_change`      | Server → Client | Node added/updated/removed         |
| `command`          | Client → Server | Actuator control                   |
| `ack`              | Server → Client | Success response                   |
| `error`            | Server → Client | Failure response                   |


# 7. Realistic Scenario

1. Client connects to Server's TCP port.

2. Client sends `{"type":"hello", "id":"c-1"}`.

3. Server responds `{"type":"welcome"}`.

4. Client sends `{"type":"get_topology", "id":"c-2"}` to learn about the system.

5. Server responds `{"type":"topology", "id":"c-2", "nodes":[...]}`.

6. Client (UI) displays the nodes.

7. Client sends `{"type":"subscribe", "id":"c-3", "nodes":["node-1"]}` to get live data for "node-1".

8. Server responds `{"type":"ack", "id":"c-3"}`.

9. Server now starts pushing sensor_update messages to the client every *X* seconds.

10. Client (UI) displays the incoming sensor data and actuator statuses.

11. User clicks the *"Open Window"* button.

12. Client sends `{"type":"command", "id":"c-4", "nodeId":"node-1", "target":"window", "params":{"level":"OPEN"}}`.

13. Server receives the command, updates its simulation state, and responds `{"type":"ack", "id":"c-4"}`.

14. The next sensor_update message from the server will now contain "window": "OPEN" in its data object, automatically updating the client's UI.

# 8. Security
* No security mechanisms (authentication, authorization, encryption) are implemented in this protocol

# 9. Notes
* The client generates id for requests; the server echoes it in ack or error.
* If the client is not subscribed to sensor_update, it won’t receive live updates.
* Sensor units and ranges are fixed in the spec (see §2.2).
* window.level must match the client enum (CLOSED, HALF, OPEN) for direct mapping.
* All timestamps are in milliseconds since epoch (UTC).







