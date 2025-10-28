package entity;

import java.util.*;

/**
 * Represents a physical sensor node in the greenhouse network.
 * Each node can collect multiple types of sensor data including temperature,
 * humidity, light levels, and pH values from the connected environment simulator.
 * The node manages both sensors and actuators for complete greenhouse automation.
 */
public class SensorNode {
    private final String nodeId;
    private final String name;
    private final String location;
    private final String ipAddress;
    private final List<String> sensors;
    private final Map<String, Object> actuators;
    private final EnvironmentSimulator environment;

    /**
     * Constructs a new SensorNode with the specified parameters.
     * Initializes sensors and actuators with default values.
     *
     * @param nodeId the unique identifier for this sensor node
     * @param name the display name of this sensor node
     * @param location the physical location of the sensor node in the greenhouse
     * @param ipAddress the IP address of this sensor node in the network
     * @param environment the environment simulator that provides base sensor values
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public SensorNode(String nodeId, String name, String location, String ipAddress, EnvironmentSimulator environment) {
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
        this.environment = environment;
        this.sensors = new ArrayList<>();
        this.actuators = new HashMap<>();
        initializeDefaults();
    }

    /**
     * Initializes the sensor node with default sensors and actuators.
     * Sets up standard greenhouse monitoring equipment.
     */
    private void initializeDefaults() {
        // Initialize standard greenhouse sensors
        sensors.addAll(List.of("ph_sensor", "light_sensor", "temperature_sensor", "humidity_sensor"));

        // Initialize actuators with default states
        actuators.put("fan", false);           // Boolean: false = OFF, true = ON
        actuators.put("window", "CLOSED");     // String: "OPEN" or "CLOSED"
        actuators.put("water_pump", false);    // Boolean: false = OFF, true = ON
        actuators.put("co2_generator", false); // Boolean: false = OFF, true = ON
    }

    /**
     * Collects current sensor readings from the environment simulator.
     * Generates realistic sensor data including slight pH variations.
     *
     * @return a Map containing sensor readings with keys matching sensor names
     */
    public Map<String, Object> getSensorData() {
        Map<String, Object> data = new HashMap<>();

        // Get real-time data from environment simulator
        data.put("temperature_sensor", environment.getTemperature());
        data.put("humidity_sensor", environment.getHumidity());
        data.put("light_sensor", environment.getLightLevel());

        // Generate realistic pH with slight variation (5.6 - 6.4 range)
        double ph = 6.0 + (Math.random() * 0.8 - 0.4);
        data.put("ph_sensor", Math.round(ph * 10.0) / 10.0);

        return data;
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
            System.out.println("Unknown actuators: " + target);
            return;
        }

        // Execute command based on actuator type
        switch (target) {
            case "fan", "water_pump", "co2_generator" -> {
                // Boolean actuators: parse string action to boolean
                boolean state = Boolean.parseBoolean(action);
                actuators.put(target, state);
                System.out.println(target + " set to: " + (state ? "ON" : "OFF"));
            }
            case "window" -> {
                // String actuator: validate and set window position
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
     * Gets the unique identifier of this sensor node.
     *
     * @return the node ID as a String
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Gets the unique name of this sensor node
     *
     * @return the node Name as a String
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the physical location of this sensor node in the greenhouse.
     *
     * @return the location as a String
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the IP address of this sensor node.
     *
     * @return the IP address as a String
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Gets a copy of the sensors list to prevent external modification.
     *
     * @return a new List containing the sensor names
     */
    public List<String> getSensors() {
        return new ArrayList<>(sensors);
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
     * @param actuatorName the name of the actuator
     * @return the current state of the actuator, or null if not found
     */
    public Object getActuatorState(String actuatorName) {
        return actuators.get(actuatorName);
    }

    /**
     * Checks if the sensor node has a specific sensor.
     *
     * @param sensorName the name of the sensor to check
     * @return true if the sensor exists, false otherwise
     */
    public boolean hasSensor(String sensorName) {
        return sensors.contains(sensorName);
    }

    /**
     * Checks if the sensor node has a specific actuator.
     *
     * @param actuatorName the name of the actuator to check
     * @return true if the actuator exists, false otherwise
     */
    public boolean hasActuator(String actuatorName) {
        return actuators.containsKey(actuatorName);
    }

    @Override
    public String toString() {
        return String.format("SensorNode{nodeId='%s', name='%s', location='%s', ipAddress='%s', sensors=%d, actuators=%d}",
                nodeId, name, location, ipAddress, sensors.size(), actuators.size());
    }


}

