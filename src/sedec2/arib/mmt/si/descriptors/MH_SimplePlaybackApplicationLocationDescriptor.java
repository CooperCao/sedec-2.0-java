package sedec2.arib.mmt.si.descriptors;

import sedec2.base.BitReadWriter;
import sedec2.util.Logger;

public class MH_SimplePlaybackApplicationLocationDescriptor extends Descriptor {
    protected byte[] initial_path_byte;
    
    public MH_SimplePlaybackApplicationLocationDescriptor(BitReadWriter brw) {
        super(brw);
        
        initial_path_byte = new byte[descriptor_length];
        
        for ( int i=0; i<descriptor_length; i++ ) {
            initial_path_byte[i] = (byte) brw.ReadOnBuffer(8);
        }
    }
    
    @Override
    public void PrintDescriptor() {
        super._PrintDescriptorHeader_();
        
        Logger.d(String.format("initial_path : %s \n", new String(initial_path_byte)));
    }

    @Override
    protected void updateDescriptorLength() {
        descriptor_length = initial_path_byte.length;
    }
}
