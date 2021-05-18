# DEVS-Planner
UAV Path Planner based on DEVs. 

Two DEVS specifications: SPEC1 & SPEC2.
Currently there are two main applications: FlightSimulator (FS) and Evaluator (EV).

Folder Structure:

 |- build: application objects.
 |
 |- data: config files, scenarios and results.
 |  |
 |  |- csv: folder to save scenarios results. CREATE THIS FOLDER MANUAL.
 |  |  |
 |  |  | - Spec1: results executed with SPEC1 apps. CREATE THIS FOLDER MANUAL.
 |  |  |
 |  |  | - Spec2: results executed with SPEC2 apps. CREATE THIS FOLDER MANUAL.
 |  |
 |  |- motionmodels: config files for UAV motion models.
 |  |
 |  |- scenarios: divided in EV and FS scenarios and common for both SPECs.
 |  |
 |  |- sensors: config files for sensor payloads and motion models.
 |  |
 |  |- uavs: config files for UAV flight dynamics parameters.
 |
 |- src
 |  |
 |  |- models: sources defining data structures and functionalities.
 |  |
 |  |- utils: supporting code.
 |  |
 |  |- xdevs: DEVS models implementation.
 |  |  |
 |  |  | - Evaluator: SPEC1 EV DEVS-models.
 |  |  |
 |  |  | - FlightSimulator: SPEC1 FS DEVS-models.
 |  |  |
 |  |  | - Spec2: EV & FS DEVS-models needed for SPEC2.
 |
 |- lib: external libraries used by the application: xDEVS, EJML, OpenCSV, JSON-simple.
 |
 |- Test: test packages used for testing application functionalities.

Use of FlightSimulator tool:

1- Go to main AppMain class.
2- Change the following code:`
``  private static final String DEVSPEC = "spec1"; // spec2
    private static final String TOOL = "Optimizator"; //Evaluator //FlightSimulator
    private static final String SCENARIO = "ScenarioPADS3Dinamic_base_1UAVradar";
``
   Save AppMain and Clean and Build the project.
3- In DEVSPEC use the desired spec, "spec1" or "spec2".
4- In TOOL use "Evaluator".
5- In SCENARIO use the desired scenario. This is the name of the folder in the
   corresponding data/scenarios/Evaluator path. The .json file defining the 
   scenario must have the same name than the folder.
6- Make sure you have a [nameUAV]Cntrl.csv and [nameSensor]Cntrl.csv per each
   UAV and sensor defined in the scenario file with the desired control actions.
7- Run AppMain.