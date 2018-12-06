package sedec2.dvb.ts.dsmcc.objectcarousel.messages;

import java.util.ArrayList;
import java.util.List;

import sedec2.base.BitReadWriter;
import sedec2.dvb.ts.dsmcc.objectcarousel.messages.descriptors.CompatibilityDescriptor;
import sedec2.util.BinaryLogger;
import sedec2.util.Logger;

public class DownloadInfoIndication extends DownloadControlMessage {
    protected int downloadId;
    protected int blockSize;
    protected byte windowSize;
    protected byte ackPeriod;
    protected int tCDownloadWindow;
    protected int tCDownloadScenario;
    protected CompatibilityDescriptor compatibilityDescriptor;
    protected int numberOfModules;
    protected List<Module> modules = new ArrayList<>();
    protected int privateDataLength;
    protected byte[] privateDataByte;

    public class Module {
        public int moduleId;
        public int moduleSize;
        public byte moduleVersion;
        public byte moduleInfoLength;
        public byte[] moduleInfoByte;
    }

    public DownloadInfoIndication(BitReadWriter brw) {
        super(brw);

        downloadId = brw.readOnBuffer(32);
        blockSize = brw.readOnBuffer(16);
        windowSize = (byte) brw.readOnBuffer(8);
        ackPeriod = (byte) brw.readOnBuffer(8);
        tCDownloadWindow = brw.readOnBuffer(32);
        tCDownloadScenario = brw.readOnBuffer(32);

        compatibilityDescriptor = new CompatibilityDescriptor(brw);
        numberOfModules = brw.readOnBuffer(16);

        for ( int i=0; i<numberOfModules; i++ ) {
            Module module = new Module();
            module.moduleId = brw.readOnBuffer(16);
            module.moduleSize = brw.readOnBuffer(32);
            module.moduleVersion = (byte) brw.readOnBuffer(8);
            module.moduleInfoLength = (byte) brw.readOnBuffer(8);
            module.moduleInfoByte = new byte[module.moduleInfoLength];
            for ( int k=0; k<module.moduleInfoByte.length; k++ ) {
                module.moduleInfoByte[k] = (byte) brw.readOnBuffer(8);
            }
            modules.add(module);
        }

        privateDataLength = brw.readOnBuffer(16);
        privateDataByte = new byte[privateDataLength];
        for ( int i=0; i<privateDataByte.length; i++ ) {
            privateDataByte[i] = (byte) brw.readOnBuffer(8);
        }
    }

    @Override
    public int getDownloadId() {
        return downloadId;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public byte getWindowSize() {
        return windowSize;
    }

    public byte getAckPeriod() {
        return ackPeriod;
    }

    public int getTCDownloadWindow() {
        return tCDownloadWindow;
    }

    public int getTCDownloadScenario() {
        return tCDownloadScenario;
    }

    public CompatibilityDescriptor getCompatabilityDescriptor() {
        return compatibilityDescriptor;
    }

    public int getNumberOfModules() {
        return numberOfModules;
    }

    public List<Module> getModules() {
        return modules;
    }

    public byte[] getPrivateDataByte() {
        return privateDataByte;
    }

    @Override
    public void print() {
        super._print_();

        Logger.d(String.format("downloadId : 0x%x \n", downloadId));
        Logger.d(String.format("blockSize : 0x%x \n", blockSize));
        Logger.d(String.format("windowSize : 0x%x \n", windowSize));
        Logger.d(String.format("ackPeriod : 0x%x \n", ackPeriod));
        Logger.d(String.format("tCDownloadWindow : 0x%x \n", tCDownloadWindow));
        Logger.d(String.format("tCDownloadScenario : 0x%x \n", tCDownloadScenario));

        compatibilityDescriptor.print();
        Logger.d(String.format("numberOfModules : 0x%x \n", numberOfModules));

        for ( int i=0; i<modules.size(); i++ ) {
            Module module = modules.get(i);
            Logger.d(String.format("[%d] moduleId : 0x%x \n", i, module.moduleId));
            Logger.d(String.format("[%d] moduleSize : 0x%x \n", i, module.moduleSize));
            Logger.d(String.format("[%d] moduleVersion : 0x%x \n",
                    i, module.moduleVersion));
            Logger.d(String.format("[%d] moduleInfoLength : 0x%x \n",
                    i, module.moduleInfoLength));
            BinaryLogger.print(module.moduleInfoByte);
        }

        Logger.d(String.format("privateDataByte : \n"));
        BinaryLogger.print(privateDataByte);
    }

    @Override
    public int getLength() {
        int header_length = super.getLength();
        int payload_length = 20 + compatibilityDescriptor.getLength() +
                privateDataByte.length;

        for ( int i=0; i<modules.size(); i++ ) {
            Module module = modules.get(i);
            payload_length += ( 8 + module.moduleInfoByte.length );
        }
        return header_length + payload_length;
    }
}
