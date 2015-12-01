import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.Gridlet;
import gridsim.GridletList;
import gridsim.Machine;
import gridsim.MachineList;
import gridsim.PE;
import gridsim.ResourceCalendar;
import gridsim.ResourceCharacteristics;
import gridsim.net.Link;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * This class represents a case study where tasks are distributed considering
 * HGreen heuristics. 
 * 
 * Author: Fabio Coutinho
 * Date: August 2011
 */
public class HGreenCaseStudies extends GridSim {
	
    private Integer userId;
    private final String name;
    private GridletList taskList;
    private GridletList executedTaskList;
    private int numberOfResources;
    private int scenarioID;
    private int taskSize;
    private final int typeOfScenario;
	private Hashtable<Integer, Double> energyConsumption;
	private double maxExecutionTime;
	
	public HGreenCaseStudies(String name, int numberOfResources, int typeOfScenario, int scenarioID, int taskSize) throws Exception {
		super(name, 9600);
		this.name = name;
	    this.numberOfResources = numberOfResources;
	    this.executedTaskList = new GridletList();
        // Gets an ID for this entity
        this.userId = new Integer( getEntityId(name) );
        System.out.println("Creating a grid user entity with name = " + name + ", and id = " + this.userId);

        // Creates a list of Gridlets (tasks) for this grid user according to the scenario
        this.taskSize = taskSize;
        this.typeOfScenario = typeOfScenario;
        switch (this.typeOfScenario) {
		case 1:
			this.taskList = createGridletToC1( this.userId );
			break;

		case 2:
			this.taskList = createGridletToC2( this.userId );
			break;
		case 3:
			this.taskList = createGridletToC3( this.userId );
			break;
		default:
			break;
		}
        
        this.scenarioID = scenarioID;
        this.writeTaskWeights();
        this.energyConsumption = new Hashtable<Integer, Double>();
        this.maxExecutionTime = 0.0;
	}

	public static MachineList createMachines(int numWNs, int numCores, int mipsRating) {
		MachineList wnList =  new MachineList();
		Machine m =  null;
		for (int i = 0; i < numWNs; i++) {
			m =  new Machine(i, numCores, mipsRating);
			wnList.add(m);
		}
		return wnList;
	}
	
	public static GreenGridResource createGridResource(String name, String arch, int numCores, 
														int mipsRating, int eee, int powerLevel0,
														int powerLevel1, int powerLevel2, int powerLevel3,
														int powerLevel4, int powerLevel5, int powerLevel6,
														int powerLevel7, int powerLevel8, int powerLevel9, 
														int powerLevel10) {
		System.out.println("Creating Grid Resources");
		LinkedList<Integer> weekendList = new LinkedList<Integer>();
		LinkedList<Integer> holidayList = new LinkedList<Integer>();
    	ResourceCalendar resCal = new ResourceCalendar(1.0, 1, 1, 1, weekendList, holidayList, 1);

    	// creating site s1
        MachineList mList = createMachines(1, numCores, mipsRating);
        
        // TIME_SHARED(round-robin scheduling) and SPACE_SHARED(queuing systems)
        ResourceCharacteristics resConfig = new ResourceCharacteristics(arch, "SL", 
        		mList, ResourceCharacteristics.SPACE_SHARED, -3, 1);

        GreenGridResource gridResource = null;

        try
        {
        	gridResource = new GreenGridResource(name, Link.DEFAULT_BAUD_RATE, resConfig, resCal, null, eee, 
        										 powerLevel0, powerLevel1, powerLevel2, powerLevel3,
        										 powerLevel4, powerLevel5, powerLevel6, powerLevel7,
        										 powerLevel8, powerLevel9, powerLevel10);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
		
		return gridResource;
	}

	/**
	 * 
	 */
	public static void createGridEnvironment() {
		/*
		Site System						  Cores	HEPSPEC	H./core		MIPSRating	eee
		S1	Supermicro 6025B (Xeon E5345)	8	58.98	7.37		1174		435
		S2	HP ProLiant DL380 (Xeon 5160)	4	38.58	9.64		1535		382
		S3	HP ProLiant DL360 (Xeon L5530)	8	100.44	12.56(16.0)	2000(2548)	2013(1586)
		S4	Dell 2970 (Opteron 2356)		8	59.74	7.47		1190		545
		S5	Bull SAS R440 (Xeon X5670)		12	93.72	7.81(18.22)	1244(2902)	2831
		S6	SGI Altix XE320 (Xeon E5420)	8	66.40	8.3			1322		681
		
				S1	S2	S3	S4	S5	S6
		l = 0	219	172	88	139	60	155
		l = 1	234	177	117	164	107	172
		l = 2	248	182	131	185	122	185
		l = 3	260	187	144	205	133	197
		l = 4	273	195	155	223	144	206
		l = 5	286	205	167	236	157	214
		l = 6	298	218	183	249	173	221
		l = 7	309	229	197	263	191	231
		l = 8	318	242	212	277	211	247
		l = 9	327	252	230	292	229	255
		l = 10	334	258	245	302	247	260
		
		Site System						            Cores	HEPSPEC	 H./core	 MIPSRating	eee
		S3	HP ProLiant DL360 (Xeon L5530)       	8		100.44	 12.56(16.0) 2000(2548)	2013(1586)
		S5	Bull SAS R440 (Xeon X5670)		        12		93.72	 7.81(18.22) 1244(2902)	2831
		S7	Dell R620 (Xeon E5-2670)	            16	    281	     17.5		 2796		4446
		S8	Supermicro 1021M-UR+B (Opteron 2376HE)	8	    65.85	 8.23		 1311		1044
		S9	IBM System x3650 M3 (Xeon  X5675)	    12	    165.54	 13.7		 2196		3152
		S10	HP ProLiant DL380 G6 (Xeon L5530)       8       77.16    9.64        1536       2012
		
				S1(S7) S2(10) S3  S4(S8) S5  S6(S9)
		l = 0	54     63     88  119    60  56
		l = 1	78     95     117 130    107 94
		l = 2	88     109    131 141 	 122 106
		l = 3	99     118    144 150    133 115
		l = 4	115	   125    155 159    144 125
		l = 5	126	   133    167 168    157 137
		l = 6	143	   142    183 177    173 151
		l = 7	165	   153    197 185    191 168
		l = 8	196	   164    212 193    211 184
		l = 9	226	   175    230 202    229 200
		l = 10	243	   187    245 210    247 218

		Site System						            Cores	HEPSPEC	 H./core	 MIPSRating	eee
		S7	Dell R620 (Xeon E5-2670)	            16	    281	     17.5		 2796		4446
		S9  IBM System x3650 M3 (Xeon  X5675)	    12	    165.54	 13.7		 2196		3152
		S10	HP ProLiant DL380 G6 (Xeon L5530)       8       77.16    9.64        1536       2012
		S11	Dell PowerEdge T620 (Xeon E5-2670)      16      298.9    18.6        2962       4730
		S12	Dell PowerEdge T620 (Xeon E5-2660)      16      311.5    19.4        3089       4929
		S13	Dell PowerEdge T620 (Xeon E5-2660)      16      308      19.2        3057       4874
		
				S1(S7) S2(10) S3(11)  S4(12) S5(13)  S6(S9)
		l = 0	54     63     50      51     50      56
		l = 1	78     95     74      74     73      94
		l = 2	88     109    83      84 	 83      106
		l = 3	99     118    94      96     96      115
		l = 4	115	   125    110     115    114     125
		l = 5	126	   133    119     123    122     137
		l = 6	143	   142    135     140    139     151
		l = 7	165	   153    156     161    162     168
		l = 8	196	   164    183     183    181     184
		l = 9	226	   175    212     207    206     200
		l = 10	243	   187    227     246    240     218

		Site System						                      Cores Specint06 MIPSRating eee
		S14 Huawei XH621 V2 (Xeon E5-2650 v2)                 16    57.6      3000       3712
		S15 IBM Corporation IBM System x3650 M3               12    43.8      2281       3152
		S16 Hewlett-Packard Company ProLiant DL380 G7         12    38.8      2021       3052
		S17 SGI Altix XE250 (Xeon processor X5272)            4     28.4      1479       450
		S18 Sun Microsystems Netra X4250 (Xeon L5408 2.13GHz) 8     20.3      1057       437
		
				S1(17) S2(18) S3(S14) S4(S15) S5(S16)
		l = 0	186    225    142     56      53
		l = 1	197    235    183     94      85
		l = 2	207    243    201     106     96
		l = 3	216    251    218     115     105
		l = 4	226    259    235     125     112
		l = 5	233    267    250     137     120
		l = 6	239    273    268     151     128
		l = 7	253    280    295     168     136
		l = 8	269    286    324     184     147
		l = 9	275    291    358     200     160
		l = 10	280    296    386     218     172
		
		Site System						                      Cores Specint06 MIPSRating eee
		S19 IBM System x iDataPlex dx360 M3 (Xeon L5640)      12    33.2      1729       3159
		
				S1(17) S2(18) S3(S19) S4(S15) S5(S16)
		l = 0	186    225    93      56      53
		l = 1	197    235    162     94      85
		l = 2	207    243    182     106     96
		l = 3	216    251    199     115     105
		l = 4	226    259    216     125     112
		l = 5	233    267    234     137     120
		l = 6	239    273    255     151     128
		l = 7	253    280    276     168     136
		l = 8	269    286    298     184     147
		l = 9	275    291    318     200     160
		l = 10	280    296    341     218     172
		
		*/
		
		System.out.println("Creating Grid Resources");

        createGridResource("s1", "SGI Altix XE250 (Xeon processor X5272)",        4, 1479, 450, 186, 197, 207, 216, 226, 233, 239, 253, 269, 275, 280);
        createGridResource("s2", "Microsystems Netra X4250 (Xeon L5408 2.13GHz)", 8, 1057, 437, 225, 235, 243, 251, 259, 267, 273, 280, 286, 291, 296);		
        createGridResource("s3", "IBM System x iDataPlex dx360 M3 (Xeon L5640)",  12, 1729, 3159, 93, 162, 182, 199, 216, 234, 255, 276, 298, 318, 341);
        createGridResource("s4", "IBM Corporation IBM System x3650 M3",           12, 2281, 3152, 56, 94, 106, 115, 125, 137, 151, 168, 184, 200, 218);
        createGridResource("s5", "Hewlett-Packard Company ProLiant DL380 G7",     12, 2021, 3052, 53, 85, 96, 105, 112, 120, 128, 136, 147, 160, 172);
	}
	
	public Gridlet getMaxTask() {
		double maxLength = 0;
		Gridlet maxGridlet = null;
		for (int i = 0; i < this.taskList.size(); i++)
		{
			Gridlet gridlet = (Gridlet) this.taskList.get(i);
			if ( gridlet.getGridletLength() > maxLength )
			{
				maxLength = gridlet.getGridletLength();
				maxGridlet = gridlet;
			}
		}
		return maxGridlet;
	}
	
	public LinkedList<Integer> getSortedByEeeResourceIdList(LinkedList<Integer> resourceIdList) {
		int maxEee = 0;
		int resIDMaxSPECpower = -1;
		int maxIndex = -1;
		
		LinkedList<Integer> sortedByEeeResourceIdList = new LinkedList<Integer>();
		LinkedList<Integer> tempResourceIdList = new LinkedList<Integer>();
		tempResourceIdList.addAll(resourceIdList);
		
		while (tempResourceIdList.size()>1) 
		{
		    for (int i = 0; i < tempResourceIdList.size(); i++)
		    {
		        // get Resource ID 
		        int resourceID = ( (Integer)tempResourceIdList.get(i) ).intValue();
		
		        // Request to resource entity to send its SPECpower(eee)
		        super.send(resourceID, GridSimTags.SCHEDULE_NOW, 
		     		   GreenTags.RESOURCE_EEE, userId);
		        
		        // waiting to get a resource eee
		        Integer objEee = (Integer) super.receiveEventObject();
		        int resEee = objEee.intValue();
		        
		        if (resEee > maxEee)
		        {
		        	maxEee = resEee;
		        	maxIndex = i;
		        	resIDMaxSPECpower = resourceID;
		        }
		        // record this event into "stat.txt" file
		       //super.recordStatistics("\"Received SPECpower" + resEee + "from " + resourceID + "\"", "");
		    }
		    sortedByEeeResourceIdList.add(new Integer(resIDMaxSPECpower));
		    tempResourceIdList.remove(maxIndex);
        	maxEee = -1;
		}
		sortedByEeeResourceIdList.add(tempResourceIdList.getFirst());  // adds last resourceId (minimal eee)
		
		return sortedByEeeResourceIdList;
	}
	
	public void allocTasksUpToFillResource(Integer resourceId)
	{
		int resID = resourceId.intValue();
		int indexPE=0;
		double load = 0.00;
		Gridlet execTask;
		
		while ( (taskList.size() > 0) && (load < 100) ) 
		{
			super.send(resID, GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, userId);

			// waiting to get a resource characteristics
			ResourceCharacteristics resChar = (ResourceCharacteristics) super.receiveEventObject();
			if (resChar != null)
			{
				load = (resChar.getNumPE() - resChar.getNumFreePE())*100 / resChar.getNumPE();
                // assigns the task to resource 
    			Gridlet task = getMaxTask();
    			super.gridletSubmit(task, resID);
    			super.recordStatistics("\"Submitting task " + task.getGridletID() + " to " + resID + "\"", "");

                execTask = super.gridletReceive(task.getGridletID(), userId, resID);
                System.out.println("Receiving Gridlet " + execTask.getGridletID());

                // Records this event into "stat.txt" file for statistical purposes
    			super.recordStatistics("\"Received gridlet " + execTask.getGridletID() +  " from resID " + resID + "\"", "");

    			//System.out.println("Adding task " +execTask.getGridletID()+ "to ExecutedTaskList");
    			
    			executedTaskList.add(execTask);
    			
    			System.out.println("Total of ExecutedTasks: " +executedTaskList.size());
    			
    			super.recordStatistics("========== Execution Time: " + execTask.getActualCPUTime(), "==========");
    			
    			resChar.setStatusPE(PE.BUSY, 0, indexPE);
                indexPE++;
    			
    			taskList.remove(task);
                if (resChar.getNumFreePE() == 0)
                	load = 100.0;
			}
		}
	}
	
	public boolean taskWasExecuted(Gridlet task)
	{
		for (int i = 0; i < executedTaskList.size(); i++) {
			Gridlet t = (Gridlet)executedTaskList.get(0);
			if (task.getGridletID() == t.getGridletID())
				return true;
		}
		return false;
	}
	
	
    /**
     * The core method that handles communications among GridSim entities
     */
    public void body()
    {
        LinkedList<Integer> resourceIdList;

        // waiting to get list of resources. 
        while (true)
        {
            // need to pause for a while to wait GridResources finish registering to GIS
            super.gridSimHold(1.0);    // hold by 1 second

            resourceIdList = super.getGridResourceList();
            System.out.println("Number of grid resources: " + resourceIdList.size());
            if (resourceIdList.size() == this.numberOfResources)
                break;
            else
                System.out.println("Waiting to get list of resources from GIS...");
        }

        // get all the resources available sorted by spec power benchmark
        LinkedList<Integer> sortedByEeeResourceIdList = getSortedByEeeResourceIdList(resourceIdList);
        
        // printResourceIDList(sortedByEeeResourceIdList);
        
        // assign "heavy tasks" to energy-efficient resources
        for (int i = 0; i < sortedByEeeResourceIdList.size(); i++)
       		allocTasksUpToFillResource(sortedByEeeResourceIdList.get(i));

        
		System.out.println("Total of ExecutedTasks at body: " +executedTaskList.size());
		
        calculateMaxExecutionTime();
        
        calculateEnergyConsumption();
        
        printGridletList();
        
        printResourceCosts();
        
        // shut down all the entities, including GridStatistics entity since
        super.shutdownGridStatisticsEntity();
        super.shutdownUserEntity();
        super.terminateIOEntities();
    }
		
    public static void finalizeSimulation() {
    	GridSim.setGIS(null);
    	
    }
    
	public static void main(String[] args) {
	try 
	{
		int scenarioID_ = Integer.parseInt(args[0]);
		int typeOfScenario_ = Integer.parseInt(args[1]);
		int taskSize_ = Integer.parseInt(args[2]);
		
        System.out.println("Initializing HGreen GridSim Simulation, scenario " + scenarioID_);
        
        GridSim.init(1, Calendar.getInstance(), true, null, null, "report");
        
        GreenGridInformationService greenGIS = new GreenGridInformationService(GreenTags.GIS_NAME);

        GridSim.setGIS(greenGIS);
        
        createGridEnvironment();
        
        new HGreenCaseStudies("HGreenCaseStudies", 5, typeOfScenario_, scenarioID_, taskSize_);
        
        GridSim.startGridSimulation();
	} 
	catch (Exception e)
    {
        e.printStackTrace();
        System.out.println("Unwanted errors happen");
    }
	}

	/**
     * Writes the task weights to the output file
     */
	private void writeTaskWeights() {
	   	try {
	   		String strDirectory = "output/C" + this.typeOfScenario + "/" + this.taskSize;
	   		new File(strDirectory).mkdirs();
    		File file = new File(strDirectory + "/tasks_" + this.scenarioID + ".txt");
    		if(file.exists()){
    			file.delete();
    		}
    		file.createNewFile();
    		//true = append file
		    FileWriter fileWritter = new FileWriter(file.getAbsolutePath(), true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	        
	        bufferWritter.write("Task_ID" + "\t" + "Weight");
	        for (int i = 0; i < this.taskList.size(); i++) {
	        	Gridlet task = (Gridlet)taskList.get(i);
	        	bufferWritter.write("\n" + task.getGridletID() + "\t" + (long)task.getGridletLength());
			}
	        bufferWritter.close();
 
    	} catch(IOException e){
    		e.printStackTrace();
    	}
	}
	
	
	/**
     * Writes(appending) the energy consumption total to output file
     * @param energyConsumptionTotal  the energy consumption total
     */
	private void writeOutput(double energyConsumptionTotal) {
	   	try {
    		File file = new File("output/C" + this.typeOfScenario  + "/" + this.taskSize + "/results_" + this.name + 
					 			 "_C" + this.typeOfScenario + "_" + this.getSelectedGridletList().size() + "tasks" + ".txt");
    		boolean isNewFile = false;
    		//if file doesnt exists, then create it
    		if(!file.exists()){
    			file.createNewFile();
    			isNewFile = true;
    		}
 
    		//true = append file
		    FileWriter fileWritter = new FileWriter(file.getAbsolutePath(), true);
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	        if (isNewFile)
	        	bufferWritter.write("Scenario_ID" + "\t" + "Energy" + "\t" +"Time");
	        bufferWritter.write("\n" + this.scenarioID + "\t" + (long)energyConsumptionTotal + "\t" + Math.round(this.maxExecutionTime));
	        bufferWritter.close();
 
    	} catch(IOException e){
    		e.printStackTrace();
    	}	
	}
	
	
	/**
     * Prints the ResourceIDs and your position in list
     * @param resourceIDlist  list of resource IDs
     */
    private void printResourceCosts()
    {
    	double energyConsumptionTotal = 0;
    	for (int i = 0; i < super.getGridResourceList().size(); i++) {
    		Integer resID = ((Integer)super.getGridResourceList().get(i));
			System.out.print("Resource ID: " + resID);
			Double wastedEnergy = (Double)this.energyConsumption.get(resID);
			System.out.println(" Energy Consumption(Watt-hour): " + wastedEnergy/3600 + " Wh");
			energyConsumptionTotal = energyConsumptionTotal + (wastedEnergy/3600);
		}
    	System.out.println("Energy Consumption Total: " + energyConsumptionTotal);
    	writeOutput(Math.round(energyConsumptionTotal));
    }
    
	/**
     * Gets the tasks list executed in the resource identified by resID
     * @param resID resource identification
     */ 
    private GridletList getTaskListExecutedByResource(int resID)
    {
    	GridletList resourceTasks = new GridletList();
    	Gridlet gridlet;
    	
    	for (int i = 0; i < getSelectedGridletList().size(); i++) {
    		gridlet = (Gridlet) getSelectedGridletList().get(i);
    		if (gridlet.getResourceID() == resID)
    			resourceTasks.add(gridlet);
		}
    	return resourceTasks;
    }
    
	/**
     * Gets the executed task with minimal Time Execution
     * @param list  list of tasks
     */ 
    private Gridlet getTaskWithMinimalExecutionTime(LinkedList<Gridlet> list)
    {
    	double minExexTime = 999999999;
    	Gridlet gridlet;
    	Gridlet minGridlet = null;
    	for (int i = 0; i < list.size(); i++) {
    		gridlet = (Gridlet) list.get(i);
    		if (gridlet.getActualCPUTime() < minExexTime) {
    			minExexTime = gridlet.getActualCPUTime();
    			minGridlet = gridlet;
    		}
		}
    	
    	return minGridlet;
    }
    
	/**
     * Gets the cost to the load equals ZERO
     */ 
    private double getCostToZeroLoad(int resID)
    {
    	super.send(resID, GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, userId);

		// waiting to get a resource characteristics
		ResourceCharacteristics resChar = (ResourceCharacteristics) super.receiveEventObject();
    	if (resChar != null)
    		super.send(resID, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_0, userId);

    	// waiting to get a resource characteristics
		Integer objLevel = (Integer) super.receiveEventObject();
        
    	return objLevel.intValue();
    }
    
	/**
     * Gets the cost according to the load of a resource
     * @param list  list of tasks
     */ 
    private double getCostAccordingToLoad(LinkedList<Gridlet> taskList, int resID)
    {
    	//double load = 0.0;
    	int load = 0;
		super.send(resID, GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, userId);

		// waiting to get a resource characteristics
		ResourceCharacteristics resChar = (ResourceCharacteristics) super.receiveEventObject();
    	if (resChar != null)
    		load = (int) Math.round(taskList.size()/(double)resChar.getNumPE()*10); // obtém o nível de carga do recurso
    		//load = taskList.size()*100 / resChar.getNumPE();
    	
		switch (load) {
			case 0:
	            // Request to resource entity to send its LEVEL_0            
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	            		   GreenTags.RESOURCE_POWER_LEVEL_0, userId);			
				break;
			case 1:
				// Request to resource entity to send its LEVEL_1
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	         		   GreenTags.RESOURCE_POWER_LEVEL_1, userId);			
	            break;
			case 2:            
	            // Request to resource entity to send its LEVEL_2         
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	            		   GreenTags.RESOURCE_POWER_LEVEL_2, userId);       
	            break;
			case 3:
	            // Request to resource entity to send its LEVEL_3         
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	            		   GreenTags.RESOURCE_POWER_LEVEL_3, userId);			
	            break;
			case 4:
	            // Request to resource entity to send its LEVEL_4            
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	            		   GreenTags.RESOURCE_POWER_LEVEL_4, userId);			
				break;
			case 5:
				// Request to resource entity to send its LEVEL_5
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	         		   GreenTags.RESOURCE_POWER_LEVEL_5, userId);			
	            break;
			case 6:            
	            // Request to resource entity to send its LEVEL_6         
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	            		   GreenTags.RESOURCE_POWER_LEVEL_6, userId);       
	            break;
			case 7:
	            // Request to resource entity to send its LEVEL_7         
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	            		   GreenTags.RESOURCE_POWER_LEVEL_7, userId);			
	            break;
			case 8:            
	            // Request to resource entity to send its LEVEL_8         
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	            		   GreenTags.RESOURCE_POWER_LEVEL_8, userId);       
	            break;
			case 9:
	            // Request to resource entity to send its LEVEL_9         
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	            		   GreenTags.RESOURCE_POWER_LEVEL_9, userId);			
	            break;			
			default:
	            // Request to resource entity to send its LEVEL_10         
	            super.send(resID, GridSimTags.SCHEDULE_NOW, 
	            		   GreenTags.RESOURCE_POWER_LEVEL_10, userId);
				break;
		}
		/*
		// resource is idle or load under 25%
		if (load < 25)
            // Request to resource entity to send its LEVEL_0            
            super.send(resID, GridSimTags.SCHEDULE_NOW, 
            		   GreenTags.RESOURCE_POWER_LEVEL_0, userId);
		else if (load < 50)
            // Request to resource entity to send its LEVEL_1         
            super.send(resID, GridSimTags.SCHEDULE_NOW, 
            		   GreenTags.RESOURCE_POWER_LEVEL_1, userId);
		else if (load < 75)
            // Request to resource entity to send its LEVEL_2         
            super.send(resID, GridSimTags.SCHEDULE_NOW, 
            		   GreenTags.RESOURCE_POWER_LEVEL_2, userId);
		else
            // Request to resource entity to send its LEVEL_3         
            super.send(resID, GridSimTags.SCHEDULE_NOW, 
            		   GreenTags.RESOURCE_POWER_LEVEL_3, userId);
    	*/
    	
		// waiting to get a resource characteristics
		Integer objLevel = (Integer) super.receiveEventObject();
        
    	return objLevel.intValue();
    }
    
	/**
     * Calculates the Maximal Execution Time over all tasks.
     */ 
    private void calculateMaxExecutionTime()
    {
    	Gridlet gridlet;
    	double maxTimeExecution = 0;
    	for (int i = 0; i < this.getSelectedGridletList().size(); i++) {
    		gridlet = (Gridlet) this.getSelectedGridletList().get(i);
			if (gridlet.getActualCPUTime(gridlet.getResourceID()) > maxTimeExecution)
				maxTimeExecution = gridlet.getActualCPUTime(gridlet.getResourceID());
		}
    	this.maxExecutionTime = maxTimeExecution;
    	System.out.println("Maximal Task Execution Time: " + this.maxExecutionTime);
    }
    
    
	/**
     * Calculates the Energy Consumption of the Resources and store them in energyConsumption Hashtable
     */ 
    private void calculateEnergyConsumption()
    {
    	for (int i = 0; i < super.getGridResourceList().size(); i++) {
    		double resourceEnergyConsumption = 0.0;
    		double lastExecutionTime = 0;
    		int resourceId = ((Integer)super.getGridResourceList().get(i)).intValue();
    		GridletList resourceTaskList = getTaskListExecutedByResource(resourceId);
    		
    		while (!resourceTaskList.isEmpty()) {
	        	Gridlet minTask = getTaskWithMinimalExecutionTime(resourceTaskList);
	        	double cost = getCostAccordingToLoad(resourceTaskList, resourceId);
	        	resourceEnergyConsumption = resourceEnergyConsumption + cost*(minTask.getActualCPUTime(resourceId) - lastExecutionTime);
	        	System.out.print(">> TASK ID: " + minTask.getGridletID());
	        	System.out.print(" | COST: " + cost);
	        	System.out.print(" | TIME: " + (minTask.getActualCPUTime(resourceId) - lastExecutionTime) + " seconds");
	        	System.out.println(" | Partial Energy Cost: " + resourceEnergyConsumption);

	        	lastExecutionTime = minTask.getActualCPUTime(resourceId);
	        	resourceTaskList.remove(minTask);
    		}
        	// calculate complement time
        	double complementTime = this.maxExecutionTime - lastExecutionTime;
    		System.out.println("Complement Time: " + complementTime);
        	// complement time is the waiting time for heaviest task finishing in another resource
        	resourceEnergyConsumption = resourceEnergyConsumption + getCostToZeroLoad(resourceId)*complementTime;
    		
    		energyConsumption.put(new Integer(resourceId), new Double(resourceEnergyConsumption));
		}
    }
    
    
    /**
     * Prints the Gridlet objects
     */
    private void printGridletList()
    {
    	//this.printEnergyConsumption();
    	
        Gridlet gridlet;

        String indent = "\t";
        String stripe = "===========";
        System.out.println("\n" + stripe + " OUTPUT for " + this.name + " " + stripe + "\n");
        System.out.println("Task ID" + indent + "STATUS" + indent + "Res. ID" + indent + "Execution Time");

        for (int i = 0; i < this.getSelectedGridletList().size(); i++)
        {
            gridlet = (Gridlet) this.getSelectedGridletList().get(i);
            System.out.print(gridlet.getGridletID() + indent);

            if (gridlet.getGridletStatus() == Gridlet.SUCCESS)
                System.out.print("SUCCESS  ");

            System.out.println(gridlet.getResourceID() + indent + gridlet.getActualCPUTime(gridlet.getResourceID()));
        }
    }
    
    /*private GridletList createGridletTemp(Integer userID)
    {
        long file_size = 300;
        long output_size = 300;
        // Creates a container to store Gridlets
        GridletList list = new GridletList();
     
        Gridlet gridlet1 =  new Gridlet(1, 507000, file_size, output_size);
        Gridlet gridlet2 =  new Gridlet(2, 50089, file_size, output_size);
        Gridlet gridlet3 =  new Gridlet(3, 115000, file_size, output_size);
        Gridlet gridlet4 =  new Gridlet(4, 2000000, file_size, output_size);
    
        gridlet1.setUserID(userID);
        gridlet2.setUserID(userID);
        gridlet3.setUserID(userID);
        gridlet4.setUserID(userID);
        list.add(gridlet1);
        list.add(gridlet2);
        list.add(gridlet3);
        list.add(gridlet4);
        System.out.println("Creating " + list.size() + " Tasks");
        return list;
    }*/
    
    /**
     * Creates the tasks (Gridlet objects) of the simulated workflow 
     */ 
    /*private GridletList createGridletToPaper(Integer userID)
    {
        long file_size = 300;
        long output_size = 300;
        // Creates a container to store Gridlets
        GridletList list = new GridletList();
     
        Gridlet gridlet1 =  new Gridlet(1, 507000, file_size, output_size);
        Gridlet gridlet2 =  new Gridlet(2, 50089, file_size, output_size);
        Gridlet gridlet3 =  new Gridlet(3, 115000, file_size, output_size);
        Gridlet gridlet4 =  new Gridlet(4, 2000000, file_size, output_size);
        Gridlet gridlet5 =  new Gridlet(5, 78650, file_size, output_size);
        Gridlet gridlet6 =  new Gridlet(6, 5480, file_size, output_size);
        Gridlet gridlet7 =  new Gridlet(7, 13500, file_size, output_size);
        Gridlet gridlet8 =  new Gridlet(8, 50859879, file_size, output_size);
        Gridlet gridlet9 =  new Gridlet(9, 3250, file_size, output_size);
        Gridlet gridlet10 = new Gridlet(10, 250654, file_size, output_size);
        Gridlet gridlet11 = new Gridlet(11, 50000, file_size, output_size);
        Gridlet gridlet12 = new Gridlet(12, 3265863, file_size, output_size);
        Gridlet gridlet13 = new Gridlet(13, 150999, file_size, output_size);
        Gridlet gridlet14 = new Gridlet(14, 4500, file_size, output_size);
        Gridlet gridlet15 = new Gridlet(15, 750897, file_size, output_size); 
        Gridlet gridlet16 = new Gridlet(16, 1480, file_size, output_size);
        Gridlet gridlet17 = new Gridlet(17, 3050, file_size, output_size);
        Gridlet gridlet18 = new Gridlet(18, 548500, file_size, output_size);
        Gridlet gridlet19 = new Gridlet(19, 19250, file_size, output_size);
        Gridlet gridlet20 = new Gridlet(20, 18598, file_size, output_size);
        Gridlet gridlet21 = new Gridlet(21, 378450698, file_size, output_size); // maxTask
        Gridlet gridlet22 = new Gridlet(22, 7365, file_size, output_size);
        Gridlet gridlet23 = new Gridlet(23, 16500, file_size, output_size);
        Gridlet gridlet24 = new Gridlet(24, 65000, file_size, output_size);
        Gridlet gridlet25 = new Gridlet(25, 675000, file_size, output_size); 
        Gridlet gridlet26 = new Gridlet(26, 1480789, file_size, output_size);
        Gridlet gridlet27 = new Gridlet(27, 350789, file_size, output_size);
        Gridlet gridlet28 = new Gridlet(28, 48500, file_size, output_size);
        Gridlet gridlet29 = new Gridlet(29, 92509778, file_size, output_size);
        Gridlet gridlet30 = new Gridlet(30, 5980456, file_size, output_size);
        Gridlet gridlet31 = new Gridlet(31, 45698, file_size, output_size);
        Gridlet gridlet32 = new Gridlet(32, 765000, file_size, output_size);
        Gridlet gridlet33 = new Gridlet(33, 1234650, file_size, output_size);
        Gridlet gridlet34 = new Gridlet(34, 850000, file_size, output_size);
        Gridlet gridlet35 = new Gridlet(35, 37500, file_size, output_size); 
        Gridlet gridlet36 = new Gridlet(36, 248045, file_size, output_size);
        Gridlet gridlet37 = new Gridlet(37, 33500, file_size, output_size);
        Gridlet gridlet38 = new Gridlet(38, 486577, file_size, output_size);
        Gridlet gridlet39 = new Gridlet(39, 19250, file_size, output_size);
        Gridlet gridlet40 = new Gridlet(40, 85987897, file_size, output_size);
        Gridlet gridlet41 = new Gridlet(41, 45698, file_size, output_size); 
        Gridlet gridlet42 = new Gridlet(42, 376597, file_size, output_size);
        Gridlet gridlet43 = new Gridlet(43, 4650, file_size, output_size);
        Gridlet gridlet44 = new Gridlet(44, 855360, file_size, output_size);
        Gridlet gridlet45 = new Gridlet(45, 37509, file_size, output_size); 
        Gridlet gridlet46 = new Gridlet(46, 24800, file_size, output_size);
        Gridlet gridlet47 = new Gridlet(47, 6706987, file_size, output_size);
        Gridlet gridlet48 = new Gridlet(48, 124789, file_size, output_size);

        
        // setting the owner of these Gridlets
        gridlet1.setUserID(userID);
        gridlet2.setUserID(userID);
        gridlet3.setUserID(userID);
        gridlet4.setUserID(userID);
        gridlet5.setUserID(userID);
        gridlet6.setUserID(userID);
        gridlet7.setUserID(userID);
        gridlet8.setUserID(userID);
        gridlet9.setUserID(userID);
        gridlet10.setUserID(userID);
        gridlet11.setUserID(userID);
        gridlet12.setUserID(userID);
        gridlet13.setUserID(userID);
        gridlet14.setUserID(userID);
        gridlet15.setUserID(userID);
        gridlet16.setUserID(userID);
        gridlet17.setUserID(userID);
        gridlet18.setUserID(userID);
        gridlet19.setUserID(userID);
        gridlet20.setUserID(userID);
        gridlet21.setUserID(userID);
        gridlet22.setUserID(userID);
        gridlet23.setUserID(userID);
        gridlet24.setUserID(userID);
        gridlet25.setUserID(userID);
        gridlet26.setUserID(userID);
        gridlet27.setUserID(userID);
        gridlet28.setUserID(userID);
        gridlet29.setUserID(userID);
        gridlet30.setUserID(userID);
        gridlet31.setUserID(userID);
        gridlet32.setUserID(userID);
        gridlet33.setUserID(userID);
        gridlet34.setUserID(userID);
        gridlet35.setUserID(userID);
        gridlet36.setUserID(userID);
        gridlet37.setUserID(userID);
        gridlet38.setUserID(userID);
        gridlet39.setUserID(userID);
        gridlet40.setUserID(userID);
        gridlet41.setUserID(userID);
        gridlet42.setUserID(userID);
        gridlet43.setUserID(userID);
        gridlet44.setUserID(userID);
        gridlet45.setUserID(userID);
        gridlet46.setUserID(userID);
        gridlet47.setUserID(userID);
        gridlet48.setUserID(userID);
   
        // Store the Gridlets into a list
        list.add(gridlet1);
        list.add(gridlet2);
        list.add(gridlet3);
        list.add(gridlet4);
        list.add(gridlet5);
        list.add(gridlet6);
        list.add(gridlet7);
        list.add(gridlet8);
        list.add(gridlet9);
        list.add(gridlet10);
        list.add(gridlet11);
        list.add(gridlet12);
        list.add(gridlet13);
        list.add(gridlet14);
        list.add(gridlet15);
        list.add(gridlet16);
        list.add(gridlet17);
        list.add(gridlet18);
        list.add(gridlet19);
        list.add(gridlet20);
        list.add(gridlet21);
        list.add(gridlet22);
        list.add(gridlet23);
        list.add(gridlet24);
        list.add(gridlet25);
        list.add(gridlet26);
        list.add(gridlet27);
        list.add(gridlet28);
        list.add(gridlet29);
        list.add(gridlet30);
        list.add(gridlet31);
        list.add(gridlet32);
        list.add(gridlet33);
        list.add(gridlet34);
        list.add(gridlet35);
        list.add(gridlet36);
        list.add(gridlet37);
        list.add(gridlet38);
        list.add(gridlet39);
        list.add(gridlet40);
        list.add(gridlet41);
        list.add(gridlet42);
        list.add(gridlet43);
        list.add(gridlet44);
        list.add(gridlet45);
        list.add(gridlet46);
        list.add(gridlet47);
        list.add(gridlet48);

        System.out.println("Creating " + list.size() + " Tasks");
        return list;
    }*/
    
    /**
     * Creates the tasks (Gridlet objects) of the simulated workflow of scenario C1 (high variance)
     */ 
    private GridletList createGridletToC1(Integer userID)
    {
        long file_size = 300;
        long output_size = 300;
        // Creates a container to store Gridlets
        GridletList list = new GridletList();
        int limit = this.taskSize/2;
        int mod = this.taskSize % 2;

        
        // creating the first group of tasks (lightweight)
        int min = 15;
        int max = 50;
        long randomNumber;
        // first part of tasks with weight between 150000 and 500000 MI
        for (int i = 1; i <= limit; i++) {
        	randomNumber = (long)(Math.random()*(max - min + 1)) + min;
        	long weight = randomNumber*10000;
        	Gridlet gridlet =  new Gridlet(i, weight, file_size, output_size);
        	gridlet.setUserID(userID);
        	list.add(gridlet);
		}
     
        // creating the second group of tasks (heavyweight)
        // last part of tasks with weight between 150 and 500 million MI
        for (int i = limit+1; i <= ((limit*2) + mod); i++) {
        	randomNumber = (long)(Math.random()*(max - min + 1)) + min;
        	long weight = randomNumber*10000000;
        	Gridlet gridlet =  new Gridlet(i, weight, file_size, output_size);
        	gridlet.setUserID(userID);
        	list.add(gridlet);
		}

        System.out.println("Creating " + list.size() + " Tasks");
        return list;
    }
    
    /**
     * Creates the tasks (Gridlet objects) of the simulated workflow of scenario C2 (low variance)
     */ 
    private GridletList createGridletToC2(Integer userID) {
    	
        long file_size = 300;
        long output_size = 300;
        int limit;
        int mod;
        
        // Creates a container to store tasks
        GridletList list = new GridletList();
        	
        limit = this.taskSize/3;
        mod = this.taskSize % 3;
        	
        //creating the first group of tasks (lightweight)
        int min = 5;
        int max = 50;
        long randomNumber;
        
        for (int i = 1; i <= limit; i++) {
        	randomNumber = (long)(Math.random()*(max - min + 1)) + min;
        	long weight = randomNumber*100000;
        	Gridlet gridlet =  new Gridlet(i,  weight, file_size, output_size);
        	gridlet.setUserID(userID);
        	list.add(gridlet);
        }
         
        //creating the second group of tasks (middle-weight)
        for (int i = limit+1; i <= limit*2; i++) {
        	randomNumber = (long)(Math.random()*(max - min + 1)) + min;
        	long weight = randomNumber*1000000;
        	Gridlet gridlet =  new Gridlet(i,  weight, file_size, output_size);
        	gridlet.setUserID(userID);
        	list.add(gridlet);
        }
            
        //creating the third group of tasks (heavy weight)
        for (int i = (limit*2)+1; i <= ((limit*3) + mod); i++) {
        	randomNumber = (long)(Math.random()*(max - min + 1)) + min;
        	long weight = randomNumber*10000000;
        	Gridlet gridlet =  new Gridlet(i,  weight, file_size, output_size);
        	gridlet.setUserID(userID);
        	list.add(gridlet);
        }
            
        System.out.println("Creating " + list.size() + " Tasks");
        return list;
    }
    
    /**
     * Creates the tasks (Gridlet objects) of the simulated workflow of scenario C3 (same task weight)
     */ 
    private GridletList createGridletToC3(Integer userID)
    {
        long file_size = 300;
        long output_size = 300;
        // Creates a container to store Gridlets
        GridletList list = new GridletList();

        int min = 5;
        int max = 500;
        // creating tasks with weight between 5 and 500 milhoes de MI
        long randomNumber;
        randomNumber = (long)(Math.random()*(max - min + 1)) + min;
        long weight = randomNumber*1000000;
        for (int i = 1; i <= this.taskSize; i++) {
        	Gridlet gridlet =  new Gridlet(i, weight, file_size, output_size);
        	gridlet.setUserID(userID);
        	list.add(gridlet);
		}

        System.out.println("Creating " + list.size() + " Tasks");
        return list;
    }
    
    /**
     * Gets the selected list of Gridlets
     * @return a list of Gridlets
     */
    public GridletList getSelectedGridletList() {
        return this.executedTaskList;
    }
    
}
