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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * This class represents a case study where tasks are distributed considering
 * GGreen heuristics. 
 * 
 * Author: Fabio Coutinho
 * Date: August 2011
 */
public class GGreenCaseStudies extends GridSim {
	
    private Integer userId;
    private String name;
    private GridletList taskList;
    private GridletList executedTaskList;
    private int numberOfResources;
    private int scenarioID;
    private final int typeOfScenario;
	private Hashtable<Integer, Double> energyConsumption;
	private double maxExecutionTime;
	
	public GGreenCaseStudies(String name, int numberOfResources, int typeOfScenario, int scenarioID, String tasksPath) throws Exception {
		
		super(name, 9600);
		this.name = name;
	    this.numberOfResources = numberOfResources;
	    this.executedTaskList = new GridletList();
        this.userId = new Integer( getEntityId(name) ); // Gets an ID for this entity
        
        System.out.println("Creating a grid user entity with name = " + name + ", and id = " + this.userId);

        this.scenarioID = scenarioID;
        this.typeOfScenario = typeOfScenario;
        this.taskList = createGridletFromFile(this.userId, tasksPath); // Creates a list of Gridlets (tasks) from a file previously executed by HGreenCaseStudies        
        this.energyConsumption = new Hashtable<Integer, Double>();
        this.maxExecutionTime = 0.0;

	}

	public static MachineList createMachines(int numWNs, int numCores, int mipsRating) {

		MachineList wnList =  new MachineList();
		Machine m =  null;
		for (int i = 0; i < numWNs; i++) {
			m =  new Machine(i, numCores, mipsRating); //Can we free up memory?
			wnList.add(m);
		}
		return wnList;
	}

	public static GreenGridResource createGridResource(String name, String arch, int numCores, int mipsRating, int cpe, 
														int powerLevel0, int powerLevel1, int powerLevel2, int powerLevel3,
														int powerLevel4, int powerLevel5, int powerLevel6, int powerLevel7, 
														int powerLevel8, int powerLevel9, int powerLevel10)
	{
		System.out.println("Creating Grid Resources");
		LinkedList<Integer> weekendList = new LinkedList<Integer>();
		LinkedList<Integer> holidayList = new LinkedList<Integer>();
    	ResourceCalendar resCal = new ResourceCalendar(1.0, 1, 1, 1, weekendList, holidayList, 1);

    	// creating site s1
        MachineList mList = createMachines(1, numCores, mipsRating);
        
        // TIME_SHARED(round-robin scheduling) and SPACE_SHARED(queuing systems)
        ResourceCharacteristics resConfig = new ResourceCharacteristics(arch, "SL",  mList, ResourceCharacteristics.SPACE_SHARED, -3, 1);

        GreenGridResource gridResource = null;

        try {
        	
        	gridResource = new GreenGridResource(name, Link.DEFAULT_BAUD_RATE, resConfig, resCal, null, cpe, 
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
	 *  Configures the environment and creates grid resources
	 */
	public static void createGridEnvironment() {

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
	
	/**
	 *  Gets resource power at receiving one more job
	 */
	public int getPowerWithNextJob(int resourceId)
	{
		
		super.send(resourceId, GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, userId);

		// waiting to get a resource characteristics
		ResourceCharacteristics resChar = (ResourceCharacteristics) super.receiveEventObject();
		int loadLevel = -1;
		
		if ( (resChar != null) && (resChar.getNumFreePE()>0) )
		{
			int OC = resChar.getNumPE() - resChar.getNumFreePE();
			loadLevel = (int) Math.round((OC+1)/(double)resChar.getNumPE()*10);

			switch (loadLevel) {
				case 0:
		            // Request to resource entity to send its LEVEL_0            
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_0, userId);			
					break;
				case 1:
					// Request to resource entity to send its LEVEL_1
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_1, userId);			
		            break;
				case 2:            
		            // Request to resource entity to send its LEVEL_2         
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_2, userId);       
		            break;
				case 3:
		            // Request to resource entity to send its LEVEL_3         
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_3, userId);			
		            break;
				case 4:
		            // Request to resource entity to send its LEVEL_4            
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_4, userId);			
					break;
				case 5:
					// Request to resource entity to send its LEVEL_5
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_5, userId);			
		            break;
				case 6:            
		            // Request to resource entity to send its LEVEL_6         
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_6, userId);       
		            break;
				case 7:
		            // Request to resource entity to send its LEVEL_7         
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_7, userId);			
		            break;
				case 8:            
		            // Request to resource entity to send its LEVEL_8         
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_8, userId);       
		            break;
				case 9:
		            // Request to resource entity to send its LEVEL_9         
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_9, userId);			
		            break;			
				default:
		            // Request to resource entity to send its LEVEL_10         
		            super.send(resourceId, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_10, userId);
					break;
			}
			
			// waiting to get a resource characteristic (power)
			Integer objLevel = (Integer) super.receiveEventObject();
	        
	    	return objLevel.intValue();
		}
		else  // resChar.getNumFreePE() == 0
			return -1;
	}
	
	/**
	 *  Returns id resource that executes next job with minimal power
	 */
	public int getMinPowerResource(LinkedList<Integer> resourceIdList)
	{
		
		int minPower = 999999;
		int minResId = -1;
		for (int i = 0; i < resourceIdList.size(); i++)
	    {
	        // get Resource ID 
	        int resourceId = ( (Integer)resourceIdList.get(i) ).intValue();
	        int tempPower = getPowerWithNextJob(resourceId);
	        if ( (tempPower < minPower) && (tempPower != -1) )
	        {
		        minPower = tempPower;
		        minResId = resourceId;
	        }
	        else if (tempPower == minPower) { // in case of tie
	        	if (resourceId < minResId) // minor Id is the tie-breaker
	        		minResId = resourceId;
	        }
	    }
		
		return minResId;
	}
	
	public void allocNextTaskToMinPowerResource(Gridlet nextTask, int resourceId)
	{
		Gridlet execTask;
		
		super.send(resourceId, GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, userId);

		// waiting to get a resource characteristics
		ResourceCharacteristics resChar = (ResourceCharacteristics) super.receiveEventObject();
		if ( (resChar != null) && (resChar.getNumFreePE() > 0) )
		{
            // assigns the next task to min power resource 
			super.gridletSubmit(nextTask, resourceId);
			super.recordStatistics("\"Submitting task " + nextTask.getGridletID() + " to " + resourceId + "\"", "");
			System.out.println(" ******  Task " + nextTask.getGridletID() + " allocated to Resource " + resChar.getResourceName() + "  ******");
			
            execTask = super.gridletReceive(nextTask.getGridletID(), userId, resourceId);
            System.out.println("Receiving Gridlet " + execTask.getGridletID());

            // Records this event into "stat.txt" file for statistical purposes
			super.recordStatistics("\"Received gridlet " + execTask.getGridletID() +  " from resID " + resourceId + "\"", "");

			executedTaskList.add(execTask);
			
			System.out.println("Total of ExecutedTasks: " +executedTaskList.size());
			
			super.recordStatistics("========== Execution Time: " + execTask.getActualCPUTime(), "==========");
			
			resChar.setStatusPE(PE.BUSY, 0, resChar.getNumBusyPE());
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
        
        while (taskList.size() > 0) 
		{
            // get the resource operating with minimal power
            int idMinResource = getMinPowerResource(resourceIdList);
            if (idMinResource == -1) // all resources are fully busy
            	break;
            Gridlet nextTask = getMaxTask();
            allocNextTaskToMinPowerResource(nextTask, idMinResource);
            // removes the executed task
            taskList.remove(nextTask);
		}
        
        calculateMaxExecutionTime();
        
        calculateEnergyConsumption();
        
        printGridletList();
        
        printResourceCosts();
        
        // shut down all the entities, including GridStatistics entity since
        super.shutdownGridStatisticsEntity();
        super.shutdownUserEntity();
        super.terminateIOEntities();
    }
    
	//Initializes the algorithm GGreen
	public static void main(String[] args) {
		try 
		{
			//expects three arguments as input
			int scenarioID_ = Integer.parseInt(args[0]);
			int typeOfScenario_ = Integer.parseInt(args[1]);
			String tasksPath_ = args[2];
			
	        System.out.println("Initializing GGreen GridSim Simulation, scenario " + scenarioID_);
	        
	        System.out.println("Tasks Path: " + tasksPath_);
	        
	        GridSim.init(1, Calendar.getInstance(), true, null, null, "report");
	        
	        GreenGridInformationService greenGIS = new GreenGridInformationService(GreenTags.GIS_NAME);
	
	        GridSim.setGIS(greenGIS);
	        
	        createGridEnvironment();
	        
	        new GGreenCaseStudies("GGreenCaseStudies", 5, typeOfScenario_, scenarioID_, tasksPath_);
	        
	        GridSim.startGridSimulation();
		}
		catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Unwanted errors happen");
        }
	}
	
	/**
     * Writes(appending) the energy consumption total to output file
     * @param energyConsumptionTotal  the energy consumption total
     */
	private void writeOutput(double energyConsumptionTotal) {
	   	try {
    		File file = new File("output/C" + this.typeOfScenario  + "/" + this.getSelectedGridletList().size() + "/results_" + this.name + 
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
        
    /**
     * Creates the tasks (Gridlet objects) of the simulated workflow of scenario C1 (high variance)
     */ 
    @SuppressWarnings("unused")
	private GridletList createGridletToC1(Integer userID)
    {
        long file_size = 300;
        long output_size = 300;
        // Creates a container to store Gridlets
        GridletList list = new GridletList();
     
        Gridlet gridlet1 =  new Gridlet(1, 12000, file_size, output_size);
        Gridlet gridlet2 =  new Gridlet(2, 16000, file_size, output_size);
        Gridlet gridlet3 =  new Gridlet(3, 50000, file_size, output_size);
        Gridlet gridlet4 =  new Gridlet(4, 5000, file_size, output_size);
        Gridlet gridlet5 =  new Gridlet(5, 7000, file_size, output_size);
        Gridlet gridlet6 =  new Gridlet(6, 46000, file_size, output_size);
        Gridlet gridlet7 =  new Gridlet(7, 40000, file_size, output_size);
        Gridlet gridlet8 =  new Gridlet(8, 28000, file_size, output_size);
        Gridlet gridlet9 =  new Gridlet(9, 27000, file_size, output_size);
        Gridlet gridlet10 =  new Gridlet(10, 48000, file_size, output_size);
        Gridlet gridlet11 =  new Gridlet(11, 22000, file_size, output_size);
        Gridlet gridlet12 =  new Gridlet(12, 31000, file_size, output_size);
        Gridlet gridlet13 =  new Gridlet(13, 9000, file_size, output_size);
        Gridlet gridlet14 =  new Gridlet(14, 26000, file_size, output_size);
        Gridlet gridlet15 =  new Gridlet(15, 34000, file_size, output_size);
        Gridlet gridlet16 =  new Gridlet(16, 50000, file_size, output_size);
        Gridlet gridlet17 =  new Gridlet(17, 49000, file_size, output_size);
        Gridlet gridlet18 =  new Gridlet(18, 19000, file_size, output_size);
        Gridlet gridlet19 =  new Gridlet(19, 43000, file_size, output_size);
        Gridlet gridlet20 =  new Gridlet(20, 26000, file_size, output_size);
        Gridlet gridlet21 =  new Gridlet(21, 33000, file_size, output_size);
        Gridlet gridlet22 =  new Gridlet(22, 27000, file_size, output_size);
        Gridlet gridlet23 =  new Gridlet(23, 42000, file_size, output_size);
        Gridlet gridlet24 =  new Gridlet(24, 27000, file_size, output_size);
        Gridlet gridlet25 =  new Gridlet(25, 73000000, file_size, output_size);
        Gridlet gridlet26 =  new Gridlet(26, 394000000, file_size, output_size);
        Gridlet gridlet27 =  new Gridlet(27, 159000000, file_size, output_size);
        Gridlet gridlet28 =  new Gridlet(28, 353000000, file_size, output_size);
        Gridlet gridlet29 =  new Gridlet(29, 367000000, file_size, output_size);
        Gridlet gridlet30 =  new Gridlet(30, 482000000, file_size, output_size);
        Gridlet gridlet31 =  new Gridlet(31, 439000000, file_size, output_size);
        Gridlet gridlet32 =  new Gridlet(32, 413000000, file_size, output_size);
        Gridlet gridlet33 =  new Gridlet(33, 453000000, file_size, output_size);
        Gridlet gridlet34 =  new Gridlet(34, 420000000, file_size, output_size);
        Gridlet gridlet35 =  new Gridlet(35, 401000000, file_size, output_size);
        Gridlet gridlet36 =  new Gridlet(36, 92000000, file_size, output_size);
        Gridlet gridlet37 =  new Gridlet(37, 365000000, file_size, output_size);
        Gridlet gridlet38 =  new Gridlet(38, 260000000, file_size, output_size);
        Gridlet gridlet39 =  new Gridlet(39, 273000000, file_size, output_size);
        Gridlet gridlet40 =  new Gridlet(40, 270000000, file_size, output_size);
        Gridlet gridlet41 =  new Gridlet(41, 413000000, file_size, output_size);
        Gridlet gridlet42 =  new Gridlet(42, 357000000, file_size, output_size);
        Gridlet gridlet43 =  new Gridlet(43, 333000000, file_size, output_size);
        Gridlet gridlet44 =  new Gridlet(44, 384000000, file_size, output_size);
        Gridlet gridlet45 =  new Gridlet(45, 468000000, file_size, output_size);
        Gridlet gridlet46 =  new Gridlet(46, 207000000, file_size, output_size);
        Gridlet gridlet47 =  new Gridlet(47, 240000000, file_size, output_size);
        Gridlet gridlet48 =  new Gridlet(48, 214000000, file_size, output_size);


        
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
    }
    
    /**
     * Creates the tasks (Gridlet objects) of the simulated workflow of scenario C2 (low variance)
     */ 
    @SuppressWarnings("unused")
	private GridletList createGridletToC2(Integer userID)
    {
        long file_size = 300;
        long output_size = 300;
        // Creates a container to store Gridlets
        GridletList list = new GridletList();
     
      Gridlet gridlet38 =  new Gridlet(38, 500000, file_size, output_size); // G1
      Gridlet gridlet35 =  new Gridlet(35, 700000, file_size, output_size);
      Gridlet gridlet4  =  new Gridlet(4, 1300000, file_size, output_size);
      Gridlet gridlet46 =  new Gridlet(46, 1500000, file_size, output_size);
      Gridlet gridlet45 =  new Gridlet(45, 1700000, file_size, output_size);
      Gridlet gridlet5  =  new Gridlet(5, 1900000, file_size, output_size);
      Gridlet gridlet1  =  new Gridlet(1, 2300000, file_size, output_size);
      Gridlet gridlet33 =  new Gridlet(33, 2700000, file_size, output_size);
      Gridlet gridlet15 =  new Gridlet(15, 3500000, file_size, output_size);
      Gridlet gridlet47 =  new Gridlet(47, 3600000, file_size, output_size);
      Gridlet gridlet18 =  new Gridlet(18, 3900000, file_size, output_size);
      Gridlet gridlet16 =  new Gridlet(16, 4300000, file_size, output_size);
      Gridlet gridlet26 =  new Gridlet(26, 4500000, file_size, output_size);
      Gridlet gridlet22 =  new Gridlet(22, 4500000, file_size, output_size);
      Gridlet gridlet12 =  new Gridlet(12, 4900000, file_size, output_size);
      Gridlet gridlet23 =  new Gridlet(23, 4900000, file_size, output_size);
      Gridlet gridlet17 =  new Gridlet(17, 5000000, file_size, output_size); // G2
      Gridlet gridlet42 =  new Gridlet(42, 8000000, file_size, output_size);
      Gridlet gridlet6  =  new Gridlet(6, 9000000, file_size, output_size);
      Gridlet gridlet27 =  new Gridlet(27, 10000000, file_size, output_size);
      Gridlet gridlet30 =  new Gridlet(30, 11000000, file_size, output_size);
      Gridlet gridlet7  =  new Gridlet(7, 11000000, file_size, output_size);
      Gridlet gridlet41 =  new Gridlet(41, 12000000, file_size, output_size);
      Gridlet gridlet31 =  new Gridlet(31, 13000000, file_size, output_size);
      Gridlet gridlet9  =  new Gridlet(9, 16000000, file_size, output_size);
      Gridlet gridlet28 =  new Gridlet(28, 19000000, file_size, output_size);
      Gridlet gridlet37 =  new Gridlet(37, 22000000, file_size, output_size);
      Gridlet gridlet29 =  new Gridlet(29, 24000000, file_size, output_size);
      Gridlet gridlet20 =  new Gridlet(20, 24000000, file_size, output_size);
      Gridlet gridlet32 =  new Gridlet(32, 26000000, file_size, output_size);
      Gridlet gridlet11 =  new Gridlet(11, 41000000, file_size, output_size);
      Gridlet gridlet40 =  new Gridlet(40, 42000000, file_size, output_size);
      Gridlet gridlet48 =  new Gridlet(48, 69000000, file_size, output_size);// G3
      Gridlet gridlet19 =  new Gridlet(19, 73000000, file_size, output_size); 
      Gridlet gridlet36 =  new Gridlet(36, 93000000, file_size, output_size);
      Gridlet gridlet39 =  new Gridlet(39, 132000000, file_size, output_size);
      Gridlet gridlet8  =  new Gridlet(8, 155000000, file_size, output_size);
      Gridlet gridlet34 =  new Gridlet(34, 162000000, file_size, output_size);
      Gridlet gridlet13 =  new Gridlet(13, 176000000, file_size, output_size);
      Gridlet gridlet43 =  new Gridlet(43, 177000000, file_size, output_size);
      Gridlet gridlet10 =  new Gridlet(10, 182000000, file_size, output_size);
      Gridlet gridlet2  =  new Gridlet(2, 254000000, file_size, output_size);
      Gridlet gridlet24 =  new Gridlet(24, 304000000, file_size, output_size);
      Gridlet gridlet21 =  new Gridlet(21, 336000000, file_size, output_size);
      Gridlet gridlet14 =  new Gridlet(14, 395000000, file_size, output_size);
      Gridlet gridlet3  =  new Gridlet(3, 445000000, file_size, output_size);
      Gridlet gridlet25 =  new Gridlet(25, 461000000, file_size, output_size);
      Gridlet gridlet44 =  new Gridlet(44, 465000000, file_size, output_size);
      
      // setting the owner of these Gridlets
      gridlet38.setUserID(userID);
      gridlet35.setUserID(userID);
      gridlet4.setUserID(userID);
      gridlet46.setUserID(userID);
      gridlet45.setUserID(userID);
      gridlet5.setUserID(userID);
      gridlet1.setUserID(userID);
      gridlet33.setUserID(userID);
      gridlet15.setUserID(userID);
      gridlet47.setUserID(userID);
      gridlet18.setUserID(userID);
      gridlet16.setUserID(userID);
      gridlet26.setUserID(userID);
      gridlet22.setUserID(userID);
      gridlet12.setUserID(userID);
      gridlet23.setUserID(userID);
      gridlet17.setUserID(userID);
      gridlet42.setUserID(userID);
      gridlet6.setUserID(userID);
      gridlet27.setUserID(userID);
      gridlet30.setUserID(userID);
      gridlet7.setUserID(userID);
      gridlet41.setUserID(userID);
      gridlet31.setUserID(userID);
      gridlet9.setUserID(userID);
      gridlet28.setUserID(userID);
      gridlet37.setUserID(userID);
      gridlet29.setUserID(userID);
      gridlet20.setUserID(userID);
      gridlet32.setUserID(userID);
      gridlet11.setUserID(userID);
      gridlet40.setUserID(userID);
      gridlet48.setUserID(userID);
      gridlet19.setUserID(userID);
      gridlet36.setUserID(userID);
      gridlet39.setUserID(userID);
      gridlet8.setUserID(userID);
      gridlet34.setUserID(userID);
      gridlet13.setUserID(userID);
      gridlet43.setUserID(userID);
      gridlet10.setUserID(userID);
      gridlet2.setUserID(userID);
      gridlet24.setUserID(userID);
      gridlet21.setUserID(userID);
      gridlet14.setUserID(userID);
      gridlet3.setUserID(userID);
      gridlet25.setUserID(userID);
      gridlet44.setUserID(userID);
 
      // Store the Gridlets into a list
      list.add(gridlet38);
      list.add(gridlet35);
      list.add(gridlet4);
      list.add(gridlet46);
      list.add(gridlet45);
      list.add(gridlet5);
      list.add(gridlet1);
      list.add(gridlet33);
      list.add(gridlet15);
      list.add(gridlet47);
      list.add(gridlet18);
      list.add(gridlet16);
      list.add(gridlet26);
      list.add(gridlet22);
      list.add(gridlet12);
      list.add(gridlet23);
      list.add(gridlet17);
      list.add(gridlet42);
      list.add(gridlet6);
      list.add(gridlet27);
      list.add(gridlet30);
      list.add(gridlet7);
      list.add(gridlet41);
      list.add(gridlet31);
      list.add(gridlet9);
      list.add(gridlet28);
      list.add(gridlet37);
      list.add(gridlet29);
      list.add(gridlet20);
      list.add(gridlet32);
      list.add(gridlet11);
      list.add(gridlet40);
      list.add(gridlet48);
      list.add(gridlet19);
      list.add(gridlet36);
      list.add(gridlet39);
      list.add(gridlet8);
      list.add(gridlet34);
      list.add(gridlet13);
      list.add(gridlet43);
      list.add(gridlet10);
      list.add(gridlet2);
      list.add(gridlet24);
      list.add(gridlet21);
      list.add(gridlet14);
      list.add(gridlet3);
      list.add(gridlet25);
      list.add(gridlet44);

        System.out.println("Creating " + list.size() + " Tasks");
        return list;
    }
    
    /**
     * Creates the tasks (Gridlet objects) of the simulated workflow of scenario C3 (same task weight)
     */ 
    @SuppressWarnings("unused")
	private GridletList createGridletToC3(Integer userID)
    {
        long file_size = 300;
        long output_size = 300;
        // Creates a container to store Gridlets
        GridletList list = new GridletList();
     
        Gridlet gridlet1 =  new Gridlet(1, 125514760, file_size, output_size);
        Gridlet gridlet2 =  new Gridlet(2, 125514760, file_size, output_size);
        Gridlet gridlet3 =  new Gridlet(3, 125514760, file_size, output_size);
        Gridlet gridlet4 =  new Gridlet(4, 125514760, file_size, output_size);
        Gridlet gridlet5 =  new Gridlet(5, 125514760, file_size, output_size);
        Gridlet gridlet6 =  new Gridlet(6, 125514760, file_size, output_size);
        Gridlet gridlet7 =  new Gridlet(7, 125514760, file_size, output_size);
        Gridlet gridlet8 =  new Gridlet(8, 125514760, file_size, output_size);
        Gridlet gridlet9 =  new Gridlet(9, 125514760, file_size, output_size);
        Gridlet gridlet10 =  new Gridlet(10, 125514760, file_size, output_size);
        Gridlet gridlet11 =  new Gridlet(11, 125514760, file_size, output_size);
        Gridlet gridlet12 =  new Gridlet(12, 125514760, file_size, output_size);
        Gridlet gridlet13 =  new Gridlet(13, 125514760, file_size, output_size);
        Gridlet gridlet14 =  new Gridlet(14, 125514760, file_size, output_size);
        Gridlet gridlet15 =  new Gridlet(15, 125514760, file_size, output_size);
        Gridlet gridlet16 =  new Gridlet(16, 125514760, file_size, output_size);
        Gridlet gridlet17 =  new Gridlet(17, 125514760, file_size, output_size);
        Gridlet gridlet18 =  new Gridlet(18, 125514760, file_size, output_size);
        Gridlet gridlet19 =  new Gridlet(19, 125514760, file_size, output_size);
        Gridlet gridlet20 =  new Gridlet(20, 125514760, file_size, output_size);
        Gridlet gridlet21 =  new Gridlet(21, 125514760, file_size, output_size);
        Gridlet gridlet22 =  new Gridlet(22, 125514760, file_size, output_size);
        Gridlet gridlet23 =  new Gridlet(23, 125514760, file_size, output_size);
        Gridlet gridlet24 =  new Gridlet(24, 125514760, file_size, output_size);
        Gridlet gridlet25 =  new Gridlet(25, 125514760, file_size, output_size);
        Gridlet gridlet26 =  new Gridlet(26, 125514760, file_size, output_size);
        Gridlet gridlet27 =  new Gridlet(27, 125514760, file_size, output_size);
        Gridlet gridlet28 =  new Gridlet(28, 125514760, file_size, output_size);
        Gridlet gridlet29 =  new Gridlet(29, 125514760, file_size, output_size);
        Gridlet gridlet30 =  new Gridlet(30, 125514760, file_size, output_size);
        Gridlet gridlet31 =  new Gridlet(31, 125514760, file_size, output_size);
        Gridlet gridlet32 =  new Gridlet(32, 125514760, file_size, output_size);
        Gridlet gridlet33 =  new Gridlet(33, 125514760, file_size, output_size);
        Gridlet gridlet34 =  new Gridlet(34, 125514760, file_size, output_size);
        Gridlet gridlet35 =  new Gridlet(35, 125514760, file_size, output_size);
        Gridlet gridlet36 =  new Gridlet(36, 125514760, file_size, output_size);
        Gridlet gridlet37 =  new Gridlet(37, 125514760, file_size, output_size);
        Gridlet gridlet38 =  new Gridlet(38, 125514760, file_size, output_size);
        Gridlet gridlet39 =  new Gridlet(39, 125514760, file_size, output_size);
        Gridlet gridlet40 =  new Gridlet(40, 125514760, file_size, output_size);
        Gridlet gridlet41 =  new Gridlet(41, 125514760, file_size, output_size);
        Gridlet gridlet42 =  new Gridlet(42, 125514760, file_size, output_size);
        Gridlet gridlet43 =  new Gridlet(43, 125514760, file_size, output_size);
        Gridlet gridlet44 =  new Gridlet(44, 125514760, file_size, output_size);
        Gridlet gridlet45 =  new Gridlet(45, 125514760, file_size, output_size);
        Gridlet gridlet46 =  new Gridlet(46, 125514760, file_size, output_size);
        Gridlet gridlet47 =  new Gridlet(47, 125514760, file_size, output_size);
        Gridlet gridlet48 =  new Gridlet(48, 125514760, file_size, output_size);
        
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
    }
    
	/**
     * Process the line extracted from taskfile and return a task
     */
	private Gridlet processLine(String line, long file_size, long output_size) {
	    //use a Scanner to parse the content of each line 
	    Scanner scanner = new Scanner(line);
	    scanner.useDelimiter("\t");
	    if (scanner.hasNext()) {
		  int id = scanner.nextInt();
		  long weight = scanner.nextLong();
		  scanner.close();
		  return new Gridlet(id, weight, file_size, output_size);
	    }
	    scanner.close();
		return null;
	}
	
    /**
     * Creates the tasks (Gridlet objects) of the simulated workflow from a file
     */ 
    private GridletList createGridletFromFile(Integer userID, String strFile) {
    	
        long file_size = 300;
        long output_size = 300;
        // Creates a container to store Gridlets
        GridletList list = new GridletList();
        File file;
        BufferedReader inputReader = null;
	   	try {
	   		 file = new File (strFile);
			 inputReader =  new BufferedReader(new FileReader(file));
 		     String line = null;

			 while ((line = inputReader.readLine()) != null) {
				 
				 if (line.startsWith("Task_ID"))
					continue;
				 
				 Gridlet gridlet = processLine(line, file_size, output_size);
				 
				 if (gridlet != null)
		        	 gridlet.setUserID(userID);
		        	 list.add(gridlet);
			 }
			 
			 inputReader.close();
		}
	   	
	   	catch (IOException ex) {
			  ex.printStackTrace();
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
