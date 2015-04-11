package org.lumberjack.server;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.InflaterInputStream;

/**
 * Created on 7/4/15.
 */
public class LumberjackDecoder extends FrameDecoder {

    private long windowSize;
    private long sequenceNumber;
    private final Logger LOG = LoggerFactory.getLogger(LumberjackDecoder.class);

    private final byte FRAME_WINDOWSIZE = 0x57, FRAME_DATA = 0x44, FRAME_COMPRESSED = 0x43, FRAME_ACK = 0x41;

    public LumberjackDecoder() {
    }

    @Override
    protected Object decode(ChannelHandlerContext channelHandlerContext, Channel channel, ChannelBuffer channelBuffer) throws Exception {

        return processBuffer(channel, channelBuffer);
    }


    private List<LogEvent> processBuffer(Channel channel, ChannelBuffer channelBuffer) throws IOException {

        channelBuffer.markReaderIndex();
        byte version = channelBuffer.readByte();
        byte frameType = channelBuffer.readByte();
        List<LogEvent> logEvents = null;

        //System.out.println("Frame Type " + frameType);

        switch (frameType) {
            case FRAME_WINDOWSIZE: //'W'
                processWindowSizeFrame(channelBuffer);
                break;
            case FRAME_DATA: //'D'
                logEvents = Arrays.asList(processDataFrame(channelBuffer));
                //eventListener.onEvent(logEvent);
                break;
            case FRAME_COMPRESSED: //'C'
                logEvents = processCompressedFrame(channel, channelBuffer);
                break;
        }
        if (windowSize != 0 && sequenceNumber == windowSize) {
            sendAck(channel);
//            System.out.println("Total Log Count " + logCount);
        }
        return logEvents;
    }

    private List<LogEvent> processCompressedFrame(Channel channel, ChannelBuffer channelBuffer) throws IOException {
        if (channelBuffer.readableBytes() >= 4) {
            long payloadLength = channelBuffer.readUnsignedInt();
//            System.out.println("Payload Length " + payloadLength);
//            System.out.println("Readable Bytes " + channelBuffer.readableBytes());
            if (channelBuffer.readableBytes() < payloadLength) {
                channelBuffer.resetReaderIndex();
            } else {
                byte[] data = new byte[(int) payloadLength];
                channelBuffer.readBytes(data);
//                System.out.println("data length " + data.length);

                if (data.length == payloadLength) {
                    InputStream in =
                            new InflaterInputStream(new ByteArrayInputStream(data));
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                    }
                    in.close();
                    out.close();
                    data = out.toByteArray();
                    return processCompressedDataFrames(channel, ChannelBuffers.copiedBuffer(data));
                }
            }
        } else {
            channelBuffer.resetReaderIndex();
        }
        return null;
    }

    private List<LogEvent> processCompressedDataFrames(Channel channel, ChannelBuffer channelBuffer) throws IOException {
        List<LogEvent> logEvents = new LinkedList<LogEvent>();
        while (channelBuffer.readable()) {
            logEvents.addAll(processBuffer(channel, channelBuffer));
        }
        return logEvents;
    }

    private void processWindowSizeFrame(ChannelBuffer channelBuffer) {
        if (channelBuffer.readableBytes() < 4) {
            channelBuffer.resetReaderIndex();
        } else {
            windowSize = channelBuffer.readUnsignedInt();
//            System.out.println("Window Size " + windowSize);
        }
    }

    private LogEvent processDataFrame(ChannelBuffer channelBuffer) {
        sequenceNumber = channelBuffer.readUnsignedInt();
        long pairCount = channelBuffer.readUnsignedInt();
        Map<String, String> logDataMap = new HashMap<String, String>((int) pairCount);
        for (int i = 0; i < pairCount; i++) {
            long keyLength = channelBuffer.readUnsignedInt();
            byte[] bytes = new byte[(int) keyLength];
            channelBuffer.readBytes(bytes);
            String key = new String(bytes);

            long valueLength = channelBuffer.readUnsignedInt();
            bytes = new byte[(int) valueLength];
            channelBuffer.readBytes(bytes);
            String value = new String(bytes);

            logDataMap.put(key, value);
        }

        return createLogEvent(logDataMap);
    }

//    private LogEvent createLogEvent(Map<String, String> logDataMap) {
//        LogEvent logEvent = new LogEvent(logDataMap);
//        return logEvent;
//    }

    private LogEvent createLogEvent(Map<String, String> logDataMap) {
        LogEvent logEvent = new LogEvent(logDataMap);
        return logEvent;
    }

    private void sendAck(final Channel channel) throws IOException {
        ChannelBuffer buffer = ChannelBuffers.buffer(6);
        buffer.writeBytes(new byte[]{0x31, FRAME_ACK});
        buffer.writeInt((int) sequenceNumber);
        ChannelFuture future = channel.write(buffer);
        future.awaitUninterruptibly();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);
        LOG.warn("Exception while process channel. So closing the channel " + ctx.getChannel(), e.getCause());
        e.getChannel().close();
    }
}
