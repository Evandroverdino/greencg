import eduni.simjava.Sim_event;
import gridsim.GridInformationService;
import gridsim.GridSim;
import gridsim.GridSimTags;
//import gridsim.IO_data;

public class GreenGridInformationService extends GridInformationService {

	public GreenGridInformationService(String name)	throws Exception {
        super(name, GridSimTags.DEFAULT_BAUD_RATE);
    }

    protected void processOtherEvent(Sim_event ev)
    {
        int eee;

        switch ( ev.get_tag() )
        {
            case GreenTags.REGISTER_EEE:
            	eee = ( (Integer) ev.get_data() ).intValue();
            	
                System.out.println(super.get_name() + ": Received EEE tag = " + eee
                				+ " at time " + GridSim.clock());
                break;
                
            default:
                break;
        }

    }
}
