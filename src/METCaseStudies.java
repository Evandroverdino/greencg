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
 * MET strategy (Minimum Execution Time) - assigns each task, in arbitrary order, 
 * to the machine with the best expected execution time for that task. 
 * 
 * Author: Fabio Coutinho
 * Date: September 2014
 */
public class METCaseStudies extends GridSim {
	
    private Integer userId;
    private String name;
    private GridletList taskList;
    private GridletList executedTaskList;
    private int numberOfResources;
    private int scenarioID;
    private final int typeOfScenario;
	private Hashtable<Integer, Double> energyConsumption;
	private double maxExecutionTime;
	
	public METCaseStudies(String name, int numberOfResources, int typeOfScenario, int scenarioID, String tasksPath) throws Exception {
		
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
	
	
	/**
	 *  Returns randomly one task
	 */
	public Gridlet getRandomTask() {
		
        int minTaskId = 0;
        int maxTaskId = this.taskList.size()-1;
        int randomNumber;

       	randomNumber = (int)(Math.random()*(maxTaskId - minTaskId + 1)) + minTaskId;

		Gridlet randomGridlet = (Gridlet) this.taskList.get(randomNumber);
		
		return randomGridlet;
	}

	
	/**
	 *  Returns the resource id list sorted by MIPS value (based on SPECint)
	 */
	public LinkedList<Integer> getSortedByMIPSResourceIdList(LinkedList<Integer> resourceIdList) {
		int maxMIPS = 0;
		int resIDMaxMIPS = -1;
		int maxIndex = -1;
		
		LinkedList<Integer> sortedByMIPSResourceIdList = new LinkedList<Integer>();
		LinkedList<Integer> tempResourceIdList = new LinkedList<Integer>();
		tempResourceIdList.addAll(resourceIdList);
		
		while (tempResourceIdList.size()>1) 
		{
		    for (int i = 0; i < tempResourceIdList.size(); i++)
		    {
		        // get Resource ID 
		        int resourceID = ( (Integer)tempResourceIdList.get(i) ).intValue();
		
		        // Request to resource entity to get MIPS value
				super.send(resourceID, GridSimTags.SCHEDULE_NOW, GridSimTags.RESOURCE_CHARACTERISTICS, userId);

				// waiting to get a resource characteristics
				ResourceCharacteristics resChar = (ResourceCharacteristics) super.receiveEventObject();

				int resMIPS = resChar.getMIPSRatingOfOnePE();
		        
		        if (resMIPS > maxMIPS)
		        {
		        	maxMIPS = resMIPS;
		        	maxIndex = i;
		        	resIDMaxMIPS = resourceID;
		        }
		    }
		    sortedByMIPSResourceIdList.add(new Integer(resIDMaxMIPS));
		    tempResourceIdList.remove(maxIndex);
        	maxMIPS = -1;
		}
		sortedByMIPSResourceIdList.add(tempResourceIdList.getFirst());  // adds last resourceId (minimal mips)
		
		return sortedByMIPSResourceIdList;
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

				// assigns a random task to resource 
    			Gridlet task = getRandomTask();
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

        // get all the resources available sorted by MIPS value
        LinkedList<Integer> sortedByMIPSResourceIdList = getSortedByMIPSResourceIdList(resourceIdList);
        
        // printResourceIDList(sortedByEeeResourceIdList);
        
        // assigns tasks in arbitrary order priority to the best expected execution time
        for (int i = 0; i < sortedByMIPSResourceIdList.size(); i++)
       		allocTasksUpToFillResource(sortedByMIPSResourceIdList.get(i));

        
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
    
	//Initializes the algorithm MET
	public static void main(String[] args) {
		try 
		{
			//expects three arguments as input
			int scenarioID_ = Integer.parseInt(args[0]);
			int typeOfScenario_ = Integer.parseInt(args[1]);
			String tasksPath_ = args[2];
			
	        System.out.println("Initializing MET GridSim Simulation, scenario " + scenarioID_);
	        
	        System.out.println("Tasks Path: " + tasksPath_);
	        
	        GridSim.init(1, Calendar.getInstance(), true, null, null, "report");
	        
	        GreenGridInformationService greenGIS = new GreenGridInformationService(GreenTags.GIS_NAME);
	
	        GridSim.setGIS(greenGIS);
	        
	        createGridEnvironment();
	        
	        METCaseStudies cs1 = new METCaseStudies("METCaseStudies", 5, typeOfScenario_, scenarioID_, tasksPath_);
	        
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
     * Prints the total energy consumption and writes is along with time to the results file
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
     * Process the line extracted from taskfile and return a task
     */
	private Gridlet processLine(String line, long file_size, long output_size) {
	    //use a Scanner to parse the content of each line 
	    Scanner scanner = new Scanner(line);
	    scanner.useDelimiter("\t");
	    if (scanner.hasNext()) {
		  int id = scanner.nextInt();
		  long weight = scanner.nextLong();
		  return new Gridlet(id, weight, file_size, output_size);
	    }
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
				 
				 if (line.startsWith("Task_ID")) // descarta o cabecalho
					continue;
				 
				 Gridlet gridlet = processLine(line, file_size, output_size);
				 
				 if (gridlet != null)
		        	 gridlet.setUserID(userID);
		        	 list.add(gridlet);
			 }
			 
			 inputReader.close(); //fiz mudanças aqui
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
