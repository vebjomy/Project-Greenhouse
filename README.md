# 🌿 Smart Greenhouse Network System

**Course:** IDATA2304 – Computer Communication and Network Programming  
**Team:** Project group 7 

Welcome to the **Smart Greenhouse Network System** project!  
This project is part of the **IDATA2304 – Computer Communication and Network Programming** course at **NTNU Ålesund**.

The project focuses on designing and implementing a **networked smart farming system** that simulates real-world greenhouse monitoring and control.  
Our solution consists of **sensor/actuator nodes** and **control-panel nodes** communicating over the Internet via a custom **TCP-based application-layer protocol**.

---

### 🎯 Key Objectives
- Develop a **custom communication protocol** between nodes.
- Simulate realistic **sensor and actuator behavior**.
- Visualize greenhouse data through a simple **GUI interface**.
- Ensure scalability for multiple nodes and sensor types.
- Apply clean-code and professional development practices using Git.

---

> “Smart control, efficient growth.”



---

## 🎯 Project Goal
The goal of this project is to design and implement a **distributed greenhouse control system**.  
The system will consist of several **nodes**, each representing a small environment with various **sensors** and **actuators**.  
All nodes will communicate using a **custom application-layer protocol** built on **TCP** (and optionally UDP).

A **Control Panel** will allow farmers to monitor environmental data and send control commands to actuators in real time.

### 🌱 What We Aim to Achieve
- Build a **TCP-based communication protocol** for exchanging data between nodes.
- Simulate sensor and actuator behavior through software.
- Develop a **Control Panel application** to visualize and manage the greenhouse environment.
- Ensure **scalability**, **reliability**, and **clean code quality**.

### 💻 Core Deliverables
1. Custom protocol documentation (`protocol.md`)  
2. Functional code for:
   - Sensor/actuator nodes  
   - Control-panel nodes  
   - Central server  
3. Sprint reports and project documentation  
4. Video presentation demonstrating the system

---

## 🧩 System Overview
### 🧩 System Architecture
The system consists of **three main components**:

| Component | Description |
|------------|-------------|
| **Sensor/Actuator Node** | Simulates environmental data such as temperature, humidity, pH, and light intensity. Can receive actuator commands (e.g., open window, start fan). |
| **Control Panel Node** | Displays sensor data and allows users to send control commands to specific nodes. |
| **Server Node** | Routes communication between multiple sensors and control panels, logs messages, and ensures scalability. |


### 💻 Technical Requirements

Hardware Requirements 
Non-functional Requirements
### 🌐 Communication
All communication occurs over **TCP sockets** using a **custom application-layer protocol** designed by the team.  
The protocol defines how nodes exchange messages, handle errors, and maintain reliable data flow.

### 🧠 Example Data Flow
1. Sensor node generates simulated data.  
2. Data is sent to the control panel node via TCP.  
3. The control panel visualizes the data.  
4. User sends a command (e.g., turn on heater) back to the sensor node.  

### 🪶 Features
- Multi-node support  
- Modular sensor/actuator design  
- Extensible message format  
- GUI-based visualization  
- Clean separation of concerns (MVC) 

---

## 📁 Main Wiki Sections
- [📘 Documentation](../wiki/Documentation)
- [⚙️ Implementation](../wiki/Implementation)
- [🧠 Research & References](../wiki/Research-&-References)
- [🧩 Appendix](../wiki/Appendix)

---

## 👥 Team Members
| Name | Role | Specific Responsibilities| 
|------|------|--------------------------|
| Vebjørn Otneim Myklebust | Developer |Backend |
| Dymitri Daniel Thorgeirsson | Developer |Backend and Documentation|
| Eyob Mengsteab Berhane | Developer |Documentation|
| Arkadii Navrotskyi | Scrum Master/Developer |Backend and Frontend|

### 🧩 Collaboration

Our team collaborates through multiple tools to stay organized and maintain effective communication:

- 💻 **GitHub** – version control, issue tracking, and code review.  
- 🧠 **Git branching workflow** – each member works on their own branch to prevent merge conflicts.  
- 💬 **Discord** – used for quick communication, daily discussions, and sharing progress updates.  
- 🗓 **Scrum meetings** – scheduled weekly to review progress, assign tasks, and plan sprints.  
- 🧾 **GitHub Wiki** – used for maintaining all documentation, reports, and meeting notes.

> Our workflow ensures transparency, smooth coordination, and consistent progress across all project areas.


---

