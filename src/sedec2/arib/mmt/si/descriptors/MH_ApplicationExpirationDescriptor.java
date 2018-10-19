package sedec2.arib.mmt.si.descriptors;

import sedec2.base.BitReadWriter;
import sedec2.util.Logger;

public class MH_ApplicationExpirationDescriptor extends Descriptor {
    protected long expiration_date_and_time;
    
    public MH_ApplicationExpirationDescriptor(BitReadWriter brw) {
        super(brw);
        
        expiration_date_and_time = brw.ReadOnBuffer(40);
    }
    
    @Override
    public void PrintDescriptor() {
        super._PrintDescriptorHeader_();
        
        Logger.d(String.format("expiration_date_and_time : 0x%x \n", 
                expiration_date_and_time));
    }

    @Override
    protected void updateDescriptorLength() {
        descriptor_length = 5;
    }
}
