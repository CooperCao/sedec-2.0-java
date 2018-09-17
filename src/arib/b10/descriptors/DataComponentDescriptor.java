package arib.b10.descriptors;

import base.BitReadWriter;
import util.Logger;

public class DataComponentDescriptor extends Descriptor {
    protected int data_component_id;
    protected byte[] additional_dta_component_info;
    
    public DataComponentDescriptor(BitReadWriter brw) {
        super(brw);
        
        data_component_id = brw.ReadOnBuffer(16);
        additional_dta_component_info = new byte[descriptor_length-2];
        
        for ( int i=0; i<descriptor_length-2; i++ ) {
            additional_dta_component_info[i] = (byte) brw.ReadOnBuffer(8);
        }
    }

    @Override
    public void PrintDescriptor() {
        super._PrintDescriptorHeader_();
        
        Logger.d(String.format("\t data_component_id : 0x%x \n",  data_component_id));
        for ( int i=0; i<additional_dta_component_info.length; i ++ ) {
        Logger.d(String.format("\t additional_data_component_info[%d] : 0x%x \n", 
                i, additional_dta_component_info[i]));
        }
    }

    @Override
    protected void updateDescriptorLength() {
        descriptor_length = 2 + additional_dta_component_info.length;
    }

}
