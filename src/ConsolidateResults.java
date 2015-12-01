//import gridsim.Gridlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class ConsolidateResults {


	public static void main(String[] args) {
		
		//int numScenarios_ = Integer.parseInt(args[0]);
		int typeOfScenario_ = Integer.parseInt(args[1]);
		
		//consolidateToRandom(numScenarios_, typeOfScenario_);
		consolidateToHGreenGGreen(typeOfScenario_);
	}

	public static void consolidateToHGreenGGreen(int typeOfScenario_)
	{
		File file12HGreen;
        BufferedReader inputReader12HGreen = null;
		File file12GGreen;
        BufferedReader inputReader12GGreen = null;
		File file24HGreen;
        BufferedReader inputReader24HGreen = null;
		File file24GGreen;
        BufferedReader inputReader24GGreen = null;
		File file36HGreen;
        BufferedReader inputReader36HGreen = null;
		File file36GGreen;
        BufferedReader inputReader36GGreen = null;
		File file48HGreen;
        BufferedReader inputReader48HGreen = null;
		File file48GGreen;
        BufferedReader inputReader48GGreen = null;
        
	   	try {
	   		String strFile12HGreen = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
	   		 						 "/12/results_HGreenCaseStudies_C" + typeOfScenario_ + "_12tasks.txt";
	   		String strFile12GGreen = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
									 "/12/results_GGreenCaseStudies_C" + typeOfScenario_ + "_12tasks.txt";
	   		String strFile24HGreen = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
				 					 "/24/results_HGreenCaseStudies_C" + typeOfScenario_ + "_24tasks.txt";
	   		String strFile24GGreen = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
	   								 "/24/results_GGreenCaseStudies_C" + typeOfScenario_ + "_24tasks.txt";
	   		String strFile36HGreen = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
				 					 "/36/results_HGreenCaseStudies_C" + typeOfScenario_ + "_36tasks.txt";
	   		String strFile36GGreen = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
	   								 "/36/results_GGreenCaseStudies_C" + typeOfScenario_ + "_36tasks.txt";
	   		String strFile48HGreen = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
				 					 "/48/results_HGreenCaseStudies_C" + typeOfScenario_ + "_48tasks.txt";
	   		String strFile48GGreen = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
	   								 "/48/results_GGreenCaseStudies_C" + typeOfScenario_ + "_48tasks.txt";
	   		
	   		
	   		 file12HGreen = new File (strFile12HGreen);
			 inputReader12HGreen =  new BufferedReader(new FileReader(file12HGreen));
 		     String line12HGreen = null;
 		     
	   		 file12GGreen = new File (strFile12GGreen);
			 inputReader12GGreen =  new BufferedReader(new FileReader(file12GGreen));
 		     String line12GGreen = null;
 		     
	   		 file24HGreen = new File (strFile24HGreen);
			 inputReader24HGreen =  new BufferedReader(new FileReader(file24HGreen));
 		     String line24HGreen = null;
 		     
	   		 file24GGreen = new File (strFile24GGreen);
			 inputReader24GGreen =  new BufferedReader(new FileReader(file24GGreen));
 		     String line24GGreen = null;
 		     
	   		 file36HGreen = new File (strFile36HGreen);
			 inputReader36HGreen =  new BufferedReader(new FileReader(file36HGreen));
 		     String line36HGreen = null;
 		     
	   		 file36GGreen = new File (strFile36GGreen);
			 inputReader36GGreen =  new BufferedReader(new FileReader(file36GGreen));
 		     String line36GGreen = null;
 		     
	   		 file48HGreen = new File (strFile48HGreen);
			 inputReader48HGreen =  new BufferedReader(new FileReader(file48HGreen));
 		     String line48HGreen = null;
 		     
	   		 file48GGreen = new File (strFile48GGreen);
			 inputReader48GGreen =  new BufferedReader(new FileReader(file48GGreen));
 		     String line48GGreen = null;
 		     

			 
     		File consolidatedFile = new File("C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
     										 "/consolidated_results_HGreen_GGreen_C" + typeOfScenario_ + "_12_24_36_48.txt");
		    FileWriter fileWritter = new FileWriter(consolidatedFile); //true = append file
	        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
 		     
 		    while ( ((line12HGreen = inputReader12HGreen.readLine()) != null) && ((line12GGreen = inputReader12GGreen.readLine()) != null) && 
 		    		((line24HGreen = inputReader24HGreen.readLine()) != null) && ((line24GGreen = inputReader24GGreen.readLine()) != null) &&
 		    		((line36HGreen = inputReader36HGreen.readLine()) != null) && ((line36GGreen = inputReader36GGreen.readLine()) != null) &&
 		    		((line48HGreen = inputReader48HGreen.readLine()) != null) && ((line48GGreen = inputReader48GGreen.readLine()) != null) ) {
 		    	if (line12HGreen.startsWith("Scenario_ID"))
 		    		line12HGreen = line12HGreen + "_HGreen";
 		    	if (line12GGreen.startsWith("Scenario_ID"))
 		    		line12GGreen = line12GGreen + "_GGreen";
 		    	if (line24HGreen.startsWith("Scenario_ID"))
 		    		line24HGreen = line24HGreen + "_HGreen";
 		    	if (line24GGreen.startsWith("Scenario_ID"))
 		    		line24GGreen = line24GGreen + "_GGreen";
 		    	if (line36HGreen.startsWith("Scenario_ID"))
 		    		line36HGreen = line36HGreen + "_HGreen";
 		    	if (line36GGreen.startsWith("Scenario_ID"))
 		    		line36GGreen = line36GGreen + "_GGreen";
 		    	if (line48HGreen.startsWith("Scenario_ID"))
 		    		line48HGreen = line48HGreen + "_HGreen";
 		    	if (line48GGreen.startsWith("Scenario_ID"))
 		    		line48GGreen = line48GGreen + "_GGreen";
 		    	
 		    	String consolidatedLine = line12HGreen + "\t" + line12GGreen + "\t" + line24HGreen + "\t" + line24GGreen + 
 		    							  "\t" + line36HGreen + "\t" + line36GGreen + "\t" + line48HGreen + "\t" + line48GGreen + "\n";
				 //System.out.println("LINHA: " + consolidatedLine );
				 bufferWritter.write(consolidatedLine);
 		     }
		     bufferWritter.close();
	   	}  catch(IOException ex) {
 		   	 ex.printStackTrace(); 
 		   }
	}
	
	public static void consolidateToRandom(int numScenarios_, int typeOfScenario_)
	{
	
		File file12;
        BufferedReader inputReader12 = null;
		File file24;
        BufferedReader inputReader24 = null;
		File file36;
        BufferedReader inputReader36 = null;
		File file48;
        BufferedReader inputReader48 = null;
        
        for (int i=1; i<=numScenarios_; i++) {

		   	try {
		   		String strFile12 = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
		   		 					"/random/results_RandomCaseStudies_C" + typeOfScenario_ + "_12-tasks_" + (i+100) + ".txt";
		   		String strFile24 = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
									"/random/results_RandomCaseStudies_C" + typeOfScenario_ + "_24-tasks_" + (i+100) + ".txt";
		   		String strFile36 = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
									"/random/results_RandomCaseStudies_C" + typeOfScenario_ + "_36-tasks_" + (i+100) + ".txt";
		   		String strFile48 = "C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + 
									"/random/results_RandomCaseStudies_C" + typeOfScenario_ + "_48-tasks_" + (i+100) + ".txt";
		   		
		   		 file12 = new File (strFile12);
				 inputReader12 =  new BufferedReader(new FileReader(file12));
	 		     String line12 = null;
	 		     
		   		 file24 = new File (strFile24);
				 inputReader24 =  new BufferedReader(new FileReader(file24));
	 		     String line24 = null;
		   		 
	 		     file36 = new File (strFile36);
				 inputReader36 =  new BufferedReader(new FileReader(file36));
	 		     String line36 = null;
		   		 
	 		     file48 = new File (strFile48);
				 inputReader48 =  new BufferedReader(new FileReader(file48));
	 		     String line48 = null;
				 
	     		File consolidatedFile = new File("C:/Users/fabio/Documents/My Java Projects/HGreen/output/C" + typeOfScenario_ + "/random/" +
	     										 "consolidated/results_random_C" + typeOfScenario_ + "_12_24_36_48_" + (i+100) + ".txt");
			    FileWriter fileWritter = new FileWriter(consolidatedFile); //true = append file
		        BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
	 		     
	 		    while ( ((line12 = inputReader12.readLine()) != null) && ((line24 = inputReader24.readLine()) != null) 
	 		    		 && ((line36 = inputReader36.readLine()) != null) && ((line48 = inputReader48.readLine()) != null) ) {
					 String consolidatedLine = line12 + "\t" + line24 + "\t" + line36 + "\t" + line48 + "\n";
					 //System.out.println("LINHA: " + consolidatedLine );
					 bufferWritter.write(consolidatedLine);
	 		     }
			     bufferWritter.close();
		   	}  catch(IOException ex) {
	 		   	 ex.printStackTrace(); 
	 		   }
        }

	}
}
