package zexamples.arib;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;

import sedec2.arib.b10.TableFactory;
import sedec2.arib.b10.tables.ProgramAssociationTable;
import sedec2.arib.b10.tables.ProgramMapTable;
import sedec2.arib.extractor.ts.TsDemultiplexer;
import sedec2.base.Table;
import sedec2.util.CommandLineParam;
import sedec2.util.ConsoleProgress;
import sedec2.util.FileTs188PacketReader;
import sedec2.util.HttpTsPacketReader;
import sedec2.util.PacketReader;

/**
 * SimpleTsCoordinator is an example which's using TsDemultiplexer of sedec2 to get information
 * of TS as asynchronous mechanism, this is a mechanism for better performance, better visibility.
 */
class SimpleTsCoordinator implements TsDemultiplexer.Listener {
    protected static final String TAG = SimpleTsCoordinator.class.getSimpleName();
    protected CommandLine commandLine;
    protected ProgramMapTable pmt = null;
    protected ProgramAssociationTable pat = null;
    protected TsDemultiplexer ts_demuxer = null;

    protected final String download_path = "./download/";
    protected final String video_download_path = download_path + "/video/";
    protected final String audio_download_path = download_path + "/audio/";

    protected Map<Integer, BufferedOutputStream> video_bs_map = new HashMap<>();
    protected Map<Integer, BufferedOutputStream> audio_bs_map = new HashMap<>();

    int ddb_counter = 0;
    int dsi_dii_counter = 0;
    public SimpleTsCoordinator(CommandLine commandLine) {
        this.commandLine = commandLine;

        ts_demuxer = new TsDemultiplexer();
        ts_demuxer.addEventListener(this);

        ts_demuxer.enableSiFilter();
        ts_demuxer.enableAudioFilter();
        ts_demuxer.enableVideoFilter();

        ts_demuxer.addSiFilter(0x0000); // PAT

//        ts_demuxer.enableSiLogging();
    }

    public void destroy() {
        ts_demuxer.removeEventListener(this);
        ts_demuxer.destroy();
        ts_demuxer = null;
    }

    public void clearQueue() {
        ts_demuxer.clearQueue();
    }

    public boolean put(byte[] ts_raw) {
        return ts_demuxer.put(ts_raw);
    }

    @Override
    public void onReceivedTable(int packet_id, Table table) {
        if ( commandLine.hasOption(CommandLineParam.SHOW_TABLES) ) table.print();

        switch ( table.getTableId() ) {
            case TableFactory.PROGRAM_ASSOCIATION_TABLE:
                if ( pat == null ) {
                    pat = (ProgramAssociationTable)table;
                    List<ProgramAssociationTable.Program> programs = pat.getPrograms();
                    for ( int i=0; i<programs.size(); i++ ) {
                        ProgramAssociationTable.Program program = programs.get(i);
                        ts_demuxer.addSiFilter(program.getPid());
                    }
                }
                break;
            case TableFactory.PROGRAM_MAP_TABLE:
                if ( pmt == null ) {
                    pmt = (ProgramMapTable)table;
                    List<ProgramMapTable.Program> programs = pmt.getPrograms();
                    for ( int i=0; i<programs.size(); i++ ) {
                        ProgramMapTable.Program program = programs.get(i);
                        switch ( program.stream_type ) {
                            case 0x01:
                            case 0x02:
                                // video
                                ts_demuxer.addVideoFilter(program.elementary_PID);
                                break;
                            case 0x03:
                            case 0x04:
                                // audio
                                ts_demuxer.addAudioFilter(program.elementary_PID);
                                break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onReceivedAudio(int packet_id, byte[] buffer) {
        try {
            if ( audio_bs_map.containsKey(packet_id) == false ) {
                new File(audio_download_path).mkdirs();
                audio_bs_map.put(packet_id, new BufferedOutputStream(new FileOutputStream(
                        new File(String.format("%s/audio.pes.0x%04x",
                                audio_download_path, packet_id)))));
            }

            if ( commandLine.hasOption(CommandLineParam.EXTRACT) ) {
                BufferedOutputStream audio_bs = audio_bs_map.get(packet_id);
                audio_bs.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceivedVideo(int packet_id, byte[] buffer) {
        try {
            if ( video_bs_map.containsKey(packet_id) == false ) {
                new File(video_download_path).mkdirs();
                video_bs_map.put(packet_id, new BufferedOutputStream(new FileOutputStream(
                        new File(String.format("%s/video.pes.0x%04x",
                                video_download_path, packet_id)))));
            }

            if ( commandLine.hasOption(CommandLineParam.EXTRACT) ) {
                BufferedOutputStream video_bs = video_bs_map.get(packet_id);
                video_bs.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * TsPacketDecoder is an application as example for getting
 * <ul>
 * <li> Tables which are include in SI of TS
 * </ul>
 */
public class TsPacketDecoder extends BaseSimpleDecoder {
    @Override
    public void justDoIt(CommandLine commandLine) {
        String target_file = commandLine.getOptionValue(CommandLineParam.TS_TYPE);
        SimpleTsCoordinator simple_ts_coordinator = new SimpleTsCoordinator(commandLine);
        ConsoleProgress progress_bar = new ConsoleProgress("TS").
                show(true, true, true, true, true, false);
        /**
         * Getting each one TS packet from specific file.
         * It assume that platform should give a TS packet to us as input of TSExtractor
         */
        PacketReader ts_reader = new FileTs188PacketReader(target_file);

        if ( commandLine.hasOption(CommandLineParam.REMOTE_RESOURCES) )
            ts_reader = new HttpTsPacketReader(target_file);

        if ( false == ts_reader.open() ) return;

        progress_bar.start(ts_reader.filesize());

        while ( ts_reader.readable() > 0) {
            final byte[] ts_packet = ts_reader.readPacket();
            if ( ts_packet == null ||
                    ts_packet.length == 0 ||
                    ts_packet[0] != 0x47 ) continue;
            /**
             * Putting a TS packet into SimpleTsCoordinator
             * and you can get both the results of as table of MPEG2
             * from event listener which you registered to TsDemultiplexer
             */
            if ( false == simple_ts_coordinator.put(ts_packet) ) break;
            if ( commandLine.hasOption(CommandLineParam.SHOW_PROGRESS) )
                progress_bar.update(ts_packet.length);
        }

        simple_ts_coordinator.clearQueue();

        progress_bar.stop();
        ts_reader.close();
        ts_reader = null;

        /**
         * Destroy of SimpleTsCoordinator to not handle and released by garbage collector
         */
        simple_ts_coordinator.destroy();
        simple_ts_coordinator = null;
    }
}
