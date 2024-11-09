# DEVS-SAR: Release published for Journal of Simulation 2022, Cloud Computing in the Modern Era.

DEVS-SAR is a Cloud-deployable DEVS-based framework for optimizing UAV trajectories and sensor strategies in target-search missions. DEVS provides it  with a well-established, flexible and verifiable modeling strategy to include different models for the UAV, sensor and target dynamics; the target and sensor uncertainty, and the optimizing process. It is implemented in xDEVS and deployable over a set of containers in the Google Cloud Platform. 

Currently there are four main applications: FlightSimulator (FS), Evaluator (EV), Optimizer (OP) and Islands (IS).

To test this software, clone this repository, open a bash terminal inside, and run:

```java
java -cp devs-sar.jar Main.AppMain TOOL [DEVSPEC] SCENARIO
```

Possible values for TOOL: FlightSimulator, Evaluator, Optimizer and Islands

Possible values for DEVSPEC: spec1 or spec2.

SCENARIO is the name of the folder and .json file defining the scenario (both must have the same name).

Evaluator tool:
"ntp" could be provided as a fourth parameter to avoid recording the whole target trajectory (beliefs).
Scenarios to be executed should be in data/scenarios/Evaluator/ScenarioName.
Edit the ScenarioName.json to change the scenario definition (UAVs, sensors, target and objective functions).
Control signals for each UAV and dynamic sensor must be placed in the same forlder. Take a look to the provided examples.
Results are saved in data/csv/spec1/Evaluator/ScenarioName.

Optimizer tool:
[DEVSPEC] is not a mandatory parameter, spec1 is always used for this tool.
Scenarios to be executed should be in data/scenarios/Optimizer/ScenarioName.
Edit the ScenarioName.json to change the scenario definition (algorithms, parameters and operators).
Algorithms parameters, horizon control and end criteria can be modified editing the ScenarioName.json.
Results are saved in data/csv/spec1/Optimizer/ScenarioName. The results are stored per Algorithm and Run.

Islands tool:
[DEVSPEC] is not a mandatory parameter, spec1 is always used for this tool.
Scenarios to be executed should be in data/scenarios/Islands/ScenarioName.
Edit the ScenarioName.json to change the scenario definition (islands topology and migration operator).
Results are saved in data/csv/spec1/Islands/ScenarioName. The results are stored per Algorithm and Run.

Additional configuration data is stored in data/algorithms, data/uavs, data/sensors, etc. The json files can be edited to change the behavior's of the models.


JDK dependency

JDK 8 and onwards.

Libraries dependencies

xdevs-1.20190715, opencsv-5.0, json-simple-1.1.1, ejml-ddense-0.38, ejml-core-0.38,
commons-logging-1.2, commons-lang3-3.11, commons-collections4-4.4, commons-beanutils-1.9.4.