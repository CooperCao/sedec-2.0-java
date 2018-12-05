package sedec2.base.dsmcc.descriptors;

import sedec2.base.BitReadWriter;
import sedec2.util.BinaryLogger;
import sedec2.util.Logger;

public class SubDescriptor {
    protected byte subDescriptorType;
    protected byte subDescriptorLength;
    protected byte[] additionalInformation;

    public SubDescriptor(BitReadWriter brw) {
        subDescriptorType = (byte) brw.readOnBuffer(8);
        subDescriptorLength = (byte) brw.readOnBuffer(8);

        additionalInformation = new byte[subDescriptorLength];
        for ( int i=0; i<additionalInformation.length; i++ ) {
            additionalInformation[i] = (byte) brw.readOnBuffer(8);
        }
    }

    public void print() {
        Logger.d(String.format("\t subDescriptorType : 0x%d \n", subDescriptorType));
        Logger.d(String.format("\t subDescriptorLength : 0x%d \n", subDescriptorLength));
        BinaryLogger.print(additionalInformation);
    }

    public int getLength() {
        return 2 + additionalInformation.length;
    }
}
