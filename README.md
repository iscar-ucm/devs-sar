# DEVS-SAR: Release published for Journal of Simulation 2022, Cloud Computing in the Modern Era.

DEVS-SAR is a Cloud-deployable DEVS-based framework for optimizing UAV trajectories and sensor strategies in target-search missions. DEVS provides it  with a well-established, flexible and verifiable modeling strategy to include different models for the UAV, sensor and target dynamics; the target and sensor uncertainty, and the optimizing process. It is implemented in xDEVS and deployable over a set of containers in the Google Cloud Platform. 

Currently there are two main applications: FlightSimulator (FS), Evaluator (EV) and Optimizer (OP).

To test this software, clone this repository, open a bash terminal inside, and run:

```java
# Run Optimizer (OP) tool
java -cp devs-sar-optimizer.jar Main.AppMain ScenarioName
# Run Evaluator (EV) tool
java -cp devs-sar-evaluator.jar Main.AppMain ScenarioName
# Run FlightSimulator (FS) tool
java -cp devs-sar-evaluator.jar Main.AppMain ScenarioName
```
ScenarioName is the name of the folder and .json file defining the scenario (both must have the same name). Edit the ScenarioName.json to change the scenario definition (UAVs, sensors, target, algorithms, etc). Additional configuration data is stored in data/algorithms, data/uavs, data/sensors, etc. The json files can be edited to change the behaviours of the models.
Optimizer tool:
Scenarios to be executed should be in data/scenarios/Optimizer/ScenarioName.
Results are saved in data/csv/spec1/Optimizer/ScenarioName. The results are stored per Algorithm and Run.

Evaluator tool:
Scenarios to be executed should be in data/scenarios/Evaluator/ScenarioName.
Results are saved in data/csv/spec1/Evaluator/ScenarioName.

# JDK dependency
JDK 8 and onwards.
# Libraries dependencies
xdevs-1.20190715, opencsv-5.0, json-simple-1.1.1, ejml-ddense-0.38, ejml-core-0.38,
commons-logging-1.2, commons-lang3-3.11, commons-collections4-4.4, commons-beanutils-1.9.4.
