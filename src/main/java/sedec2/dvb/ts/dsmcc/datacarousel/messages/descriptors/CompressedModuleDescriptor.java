package sedec2.dvb.ts.dsmcc.datacarousel.messages.descriptors;

import sedec2.base.BitReadWriter;
import sedec2.base.Descriptor;
import sedec2.util.Logger;

public class CompressedModuleDescriptor extends Descriptor {
    protected byte compression_method;
    protected int original_size;

    public CompressedModuleDescriptor(BitReadWriter brw) {
        super(brw);

        compression_method = (byte) brw.readOnBuffer(8);
        original_size = brw.readOnBuffer(32);
    }

    public byte getCompressionMethod() {
        return compression_method;
    }

    public int getOriginalSize() {
        return original_size;
    }

    @Override
    public void print() {
        super._print_();

        Logger.d(String.format("\t compression_method: 0x%x \n", compression_method));
        Logger.d(String.format("\t original_size : 0x%x \n", original_size));
    }

    @Override
    protected void updateDescriptorLength() {
        descriptor_length = 5;
    }
}
