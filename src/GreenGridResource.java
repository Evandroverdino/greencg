import eduni.simjava.Sim_event;
import gridsim.AllocPolicy;
import gridsim.GridResource;
import gridsim.GridSim;
import gridsim.GridSimTags;
import gridsim.IO_data;
import gridsim.ResourceCalendar;
import gridsim.ResourceCharacteristics;

public class GreenGridResource extends GridResource {

	private int eee;
	private int powerLevel0;
	private int powerLevel1;
	private int powerLevel2;
	private int powerLevel3;
	private int powerLevel4;
	private int powerLevel5;
	private int powerLevel6;
	private int powerLevel7;
	private int powerLevel8;
	private int powerLevel9;
	private int powerLevel10;
	
	public GreenGridResource(String name, double link, ResourceCharacteristics resChar, ResourceCalendar resCal,
								AllocPolicy alloc, int eee, int powerLevel0, int powerLevel1, 
								int powerLevel2, int powerLevel3, int powerLevel4, int powerLevel5,
								int powerLevel6, int powerLevel7, int powerLevel8, int powerLevel9,
								int powerLevel10) throws Exception {
		
		super(name, link, resChar, resCal, alloc);
		this.eee = eee;
		this.powerLevel0 = powerLevel0;
		this.powerLevel1 = powerLevel1;
		this.powerLevel2 = powerLevel2;
		this.powerLevel3 = powerLevel3;
		this.powerLevel4 = powerLevel4;
		this.powerLevel5 = powerLevel5;
		this.powerLevel6 = powerLevel6;
		this.powerLevel7 = powerLevel7;
		this.powerLevel8 = powerLevel8;
		this.powerLevel9 = powerLevel9;
		this.powerLevel10 = powerLevel10;
	}
	
    protected void processOtherEvent(Sim_event ev) {
    	
        if (ev == null) {
        	
            System.out.println(super.get_name() + ".processOtherEvent(): " + "Error - an event is null.");
            return;
        }
        
        try  {
        	
            // get the sender ID.
        	Integer obj = (Integer) ev.get_data();

            // get the sender name
            String name = GridSim.getEntityName( obj.intValue() );
            
            switch (ev.get_tag()) {
            
            	case GreenTags.RESOURCE_EEE:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_EEE, 
            			new IO_data(new Integer(eee), SIZE, GridSim.getEntityId(name)));
                    
                    System.out.println(super.get_name() + " received EEE tag from " + name + " at time " + GridSim.clock());
                    break;

            	case GreenTags.RESOURCE_POWER_LEVEL_0:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_0, 
            			new IO_data(new Integer(powerLevel0), SIZE, GridSim.getEntityId(name)));
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_0 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_1:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_1, 
            			new IO_data(new Integer(powerLevel1), SIZE, GridSim.getEntityId(name)));
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_1 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_2:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_2, 
            			new IO_data(new Integer(powerLevel2), SIZE, GridSim.getEntityId(name)) );
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_2 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_3:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_3, 
            			new IO_data(new Integer(powerLevel3), SIZE, GridSim.getEntityId(name)) );
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_3 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_4:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_4, 
            			new IO_data(new Integer(powerLevel4), SIZE, GridSim.getEntityId(name)) );
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_4 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_5:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_5, 
            			new IO_data(new Integer(powerLevel5), SIZE, GridSim.getEntityId(name)) );
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_5 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_6:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_6, 
            			new IO_data(new Integer(powerLevel6), SIZE, GridSim.getEntityId(name)) );
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_6 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_7:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_7, 
            			new IO_data(new Integer(powerLevel7), SIZE, GridSim.getEntityId(name)) );
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_7 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_8:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_8, 
            			new IO_data(new Integer(powerLevel8), SIZE, GridSim.getEntityId(name)) );
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_8 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_9:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_9, 
            			new IO_data(new Integer(powerLevel9), SIZE, GridSim.getEntityId(name)) );
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_9 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
            	case GreenTags.RESOURCE_POWER_LEVEL_10:
            		super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.RESOURCE_POWER_LEVEL_10, 
            			new IO_data(new Integer(powerLevel10), SIZE, GridSim.getEntityId(name)) );
            		
                    System.out.println(super.get_name() + ": received POWER_LEVEL_10 tag from " + name + " at time " + GridSim.clock());
                    break;
                    
                default:
                    break;
            }
        }
        catch (ClassCastException c) {
            System.out.println(super.get_name() + 
                    ".processOtherEvent(): Exception occurs.");
        }
    }

    protected void registerOtherEntity() {
    	
        int SIZE = 12;  // size of Integer object incl. overhead

        // get the GIS entity ID
        int gisID = GridSim.getEntityId(GreenTags.GIS_NAME);
        
        // get the GIS entity name
        String gisName = GridSim.getEntityName(gisID);
        
        // register CPE tag to the GIS entity
        System.out.println(super.get_name() + " registers CPE tag to " + gisName +  " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_EEE, new IO_data(new Integer(eee), SIZE, gisID));
        
        // register POWER_LEVEL0 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_0 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_0, new IO_data(new Integer(powerLevel0), SIZE, gisID));

        // register POWER_LEVEL1 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_1 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_1, new IO_data(new Integer(powerLevel1), SIZE, gisID));

        // register POWER_LEVEL2 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_2 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_2, new IO_data(new Integer(powerLevel2), SIZE, gisID));
        
        // register POWER_LEVEL3 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_3 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_3, new IO_data(new Integer(powerLevel3), SIZE, gisID));

        // register POWER_LEVEL4 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_4 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_4, new IO_data(new Integer(powerLevel4), SIZE, gisID));
        
        // register POWER_LEVEL5 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_5 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_5, new IO_data(new Integer(powerLevel5), SIZE, gisID));

        // register POWER_LEVEL6 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_6 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_6, new IO_data(new Integer(powerLevel6), SIZE, gisID));

        // register POWER_LEVE7 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_7 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_7, new IO_data(new Integer(powerLevel7), SIZE, gisID));

        // register POWER_LEVEL8 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_8 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_8, new IO_data(new Integer(powerLevel8), SIZE, gisID));

        // register POWER_LEVEL9 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_9 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_9, new IO_data(new Integer(powerLevel9), SIZE, gisID));

        // register POWER_LEVEL10 tag to the GIS entity
        System.out.println(super.get_name() + " registers POWER_LEVEL_10 tag to " + gisName + " at time " + GridSim.clock());
        
        super.send(super.output, GridSimTags.SCHEDULE_NOW, GreenTags.REGISTER_POWER_LEVEL_10, new IO_data(new Integer(powerLevel10), SIZE, gisID));
	}
}
