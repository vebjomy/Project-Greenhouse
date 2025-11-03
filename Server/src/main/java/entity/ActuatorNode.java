package entity;

import java.util.HashMap;
import java.util.Map;

public class ActuatorNode {
    private final String nodeId;
    private final String name;
    private final String location;
    private final String ipAddress;
    private final Map<String, Object> actuators;

    /**
     * Constructs a new ActuatorNode with the specified parameters.
     * Initializes actuators with default values.
     *
     * @param nodeId the unique identifier for this actuator node
     * @param name the display name of this actuator node
     * @param location the physical location of the actuator node in the greenhouse
     * @param ipAddress the IP address of this actuator node in the network
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public ActuatorNode(String nodeId, String name, String location, String ipAddress, EnvironmentSimulator environment) {
        if (nodeId == null || nodeId.trim().isEmpty()) {
            throw new IllegalArgumentException("NodeId cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("IP Address cannot be null or empty");
        }
        if (environment == null) {
            throw new IllegalArgumentException("Environment simulator cannot be null");
        }

        this.nodeId = nodeId;
        this.name = name;
        this.location = location;
        this.ipAddress = ipAddress;
        this.actuators = new HashMap<>();
        initializeDefaults();
    }

    /**
     * Initializes the actuator node with default actuators.
     * Sets up standard greenhouse automation equipment.
     */
    private void initializeDefaults() {
        // Initialize actuators with default states
        actuators.put("fan", false);           // Boolean: false = OFF, true = ON
        actuators.put("window", "CLOSED");     // String: "OPEN" or "CLOSED"
        actuators.put("water_pump", false);    // Boolean: false = OFF, true = ON
        actuators.put("co2_generator", false); // Boolean: false = OFF, true = ON
    }

    /**
     * Executes a command on the specified actuator.
     * Updates the actuator state based on the target and action parameters.
     *
     * @param target the actuator to control (fan, window, water_pump, co2_generator)
     * @param action the action to perform (true/false for boolean actuators, OPEN/CLOSED for window)
     * @param params additional parameters (currently unused but available for future extensions)
     */
    public void executeCommand(String target, String action, Map<String, Object> params) {
        if (!actuators.containsKey(target)) {
            System.out.println("Unknown actuator: " + target);
            return;
        }

        switch (target) {
            case "fan", "water_pump", "co2_generator" -> {
                boolean state = Boolean.parseBoolean(action);
                actuators.put(target, state);
                System.out.println(target + " set to: " + (state ? "ON" : "OFF"));
            }
            case "window" -> {
                if ("OPEN".equalsIgnoreCase(action) || "CLOSED".equalsIgnoreCase(action)) {
                    actuators.put(target, action.toUpperCase());
                    System.out.println("Window set to: " + action.toUpperCase());
                } else {
                    System.out.println("Invalid window action. Use OPEN or CLOSED");
                }
            }
            default -> System.out.println("Cannot execute action on: " + target);
        }
    }

    /**
     * Gets the unique identifier of this actuator node.
     *
     * @return the node ID as a String
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Gets the unique name of this actuator node
     *
     * @return the node Name as a String
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the physical location of this actuator node in the greenhouse.
     *
     * @return the location as a String
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the IP address of this actuator node.
     *
     * @return the IP address as a String
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Gets a copy of the actuators map to prevent external modification.
     *
     * @return a new Map containing actuator names and their current states
     */
    public Map<String, Object> getActuators() {
        return new HashMap<>(actuators);
    }

    /**
     * Gets the current state of a specific actuator.
     *
     * @param actuatorName the name of the actuator to check
     * @return the current state of the actuator, or null if not found
     */
    public Object getActuatorState(String actuatorName) {
        return actuators.get(actuatorName);
    }

    /**
     * Checks if the actuator node has a specific actuator.
     *
     * @param actuatorName the name of the actuator to check
     * @return true if the actuator exists, false otherwise
     */
    public boolean hasActuator(String actuatorName) {
        return actuators.containsKey(actuatorName);
    }

    @Override
    public String toString() {
        return String.format("ActuatorNode{nodeId='%s', name='%s', location='%s', ipAddress='%s', actuators=%d}",
                nodeId, name, location, ipAddress, actuators.size());
    }
}
