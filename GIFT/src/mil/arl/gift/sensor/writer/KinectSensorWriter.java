/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.writer;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.PngWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import mil.arl.gift.common.ImageData;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.ImageFormatEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.io.TimeUtil;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.ImageValue;
import mil.arl.gift.sensor.AbstractEventProducer.EventProducerInformation;
import net.jpountz.lz4.LZ4BlockOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for writing sensor and/or filtered data to a file
 * using a string delimited encoding or to a binary file for Kinect specific
 * data.
 *
 * @author jleonard
 */
public class KinectSensorWriter extends GenericSensorDelimitedWriter {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(KinectSensorWriter.class);

    /**
     * Output stream for the custom format color binary file
     */
    private FileOutputStream colorBinaryFileWriter;

    /**
     * Output stream for the custom format depth binary file
     */
    private FileOutputStream depthBinaryFileWriter;
    
    /**
     * Compression format for the color images
     */
    private generated.sensor.ImageCompressionFormat colorCompressionFormat = null;
    
    /**
     * Compression format for the depth images
     */
    private generated.sensor.ImageCompressionFormat depthCompressionFormat = null;
    
    private boolean hasWrittenColorHeader = false, hasWrittenDepthHeader = false;

    /**
     * Class constructor - configure using the writer configuration input for
     * this writer
     *
     * @param configuration parameters to configure this writer
     * @param eventProducerInformation attributes for which data will be written for 
     */
    public KinectSensorWriter(generated.sensor.KinectSensorWriter configuration, EventProducerInformation eventProducerInformation) {
        super(configuration.getFilePrefix(), CSV_EXTENSION, eventProducerInformation);
        
        colorCompressionFormat = configuration.getColorCompression();
        
        depthCompressionFormat = configuration.getDepthCompression();
        
        if(configuration != null && configuration.getDirectoryToWrite() != null) {
            logger.warn("A sensor writer contains a legacy configuration to write to '" + configuration.getDirectoryToWrite() + "'. The "
                    + "configured directory will be ignored, and sensor output from this writer will instead be written to '"
                    + PackageUtil.getDomainSessions() + "'.");
         }
    }

    @Override
    public boolean initialize(int userId, int domainSessionId, String experimentID) {

        if (super.initialize(userId, domainSessionId, experimentID)) {

            String colorFileName = getPrefix() + "_Color_" + USER_ID_TOKEN + userId + "_" + DOMAIN_SESSION_ID_TOKEN + domainSessionId + "_" + TimeUtil.formatTimeLogFilename(System.currentTimeMillis()) + ".dat";

            try {

                colorBinaryFileWriter = new FileOutputStream(getDomainSessionFolder() + FILE_SEPARATOR + colorFileName);

            } catch (@SuppressWarnings("unused") IOException ex) {

                return false;
            }

            String depthFileName = getPrefix() + "_Depth_" + USER_ID_TOKEN + userId + "_" + DOMAIN_SESSION_ID_TOKEN + domainSessionId + "_" + TimeUtil.formatTimeLogFilename(System.currentTimeMillis()) + ".dat";

            try {

                depthBinaryFileWriter = new FileOutputStream(getDomainSessionFolder() + FILE_SEPARATOR + depthFileName);

            } catch (@SuppressWarnings("unused") IOException ex) {

                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    protected void writeAttributes(long time, Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorValues) throws IOException{

        // Copy the map to make modifications to it
        Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> values = new HashMap<>(sensorValues);
        
        // Take and remove the Kinect color and depth channels, leaving only skeleton and face data
        // The color and depth data will be written in binary format to their own files
        ImageValue colorImageValue = (ImageValue) values.get(SensorAttributeNameEnum.COLOR_CHANNEL);

        values.remove(SensorAttributeNameEnum.COLOR_CHANNEL);

        ImageValue depthImageValue = (ImageValue) values.get(SensorAttributeNameEnum.DEPTH_CHANNEL);

        values.remove(SensorAttributeNameEnum.DEPTH_CHANNEL);
        
        //write the remaining attributes to the csv file
        super.writeAttributes(time, values);


        // Write the color data out to its own binary file
        if (colorImageValue != null) {

            try {

                // If the header hasn't been written, write it
                if (!hasWrittenColorHeader) {

                    // Write the header, this is a custom format
                    writeColorDataHeader(colorImageValue.getImageData(), colorBinaryFileWriter, colorCompressionFormat);

                    hasWrittenColorHeader = true;
                }

                // Write the data, this is a custom format
                writeColorData(colorImageValue.getImageData(), time, colorBinaryFileWriter, colorCompressionFormat);

            } catch (IOException | RuntimeException e) {

                logger.error("Caught exception while writing Color data", e);
            }
        }

        // Write the depth data out to its own binary file
        if (depthImageValue != null) {

            try {

                // If the header hasn't been written, write it
                if (!hasWrittenDepthHeader) {

                    // Write the header, this is a custom format
                    writeDepthDataHeader(depthImageValue.getImageData(), depthBinaryFileWriter, depthCompressionFormat);

                    hasWrittenDepthHeader = true;
                }

                // Write the data, this is a custom format
                writeDepthData(depthImageValue.getImageData(), time, depthBinaryFileWriter, depthCompressionFormat);

            } catch (IOException e) {

                logger.error("Caught exception while writing Depth data", e);
            }
        }
    }

    @Override
    public void finished() {

        try {

            colorBinaryFileWriter.close();

        } catch (IOException ex) {

            logger.error("Caught exception while closing the Color binary file.", ex);
        }

        try {

            depthBinaryFileWriter.close();

        } catch (IOException ex) {

            logger.error("Caught exception while closing the Depth binary file.", ex);
        }

        super.finished();
    }

    /**
     * Writes a custom header for the color data binary file
     * 
     * @param imageData The image data with the configuration of the image
     * @param outputStream The color data binary file output stream
     * @param compressionFormat The format to compress the color data with
     * @throws IOException 
     */
    private static void writeColorDataHeader(ImageData imageData, FileOutputStream outputStream, generated.sensor.ImageCompressionFormat compressionFormat) throws IOException {

        byte[] charBytes = new byte[2 * 3];

        // If the compression format is PNG, only width and height need to be known
        if (compressionFormat == generated.sensor.ImageCompressionFormat.PNG) {

            ByteBuffer.wrap(charBytes).putChar('P').putChar('N').putChar('G');

            outputStream.write(charBytes);

        } else {

            // If the compression format is LZ4 or none at all, the underlying format needs to be written
            if (compressionFormat == generated.sensor.ImageCompressionFormat.LZ_4) {

                ByteBuffer.wrap(charBytes).putChar('L').putChar('Z').putChar('4');

            } else if (compressionFormat == null) {

                ByteBuffer.wrap(charBytes).putChar('R').putChar('A').putChar('W');

            } else {

                throw new UnsupportedOperationException("Color image compression not supported: " + imageData.getFormat());
            }

            outputStream.write(charBytes);

            if (imageData.getFormat() == ImageFormatEnum.BAYER_GRGB) {

                ByteBuffer.wrap(charBytes).putChar('B').putChar('A').putChar('Y');

            } else if (imageData.getFormat() == ImageFormatEnum.YUV_422_UYVY) {

                ByteBuffer.wrap(charBytes).putChar('Y').putChar('U').putChar('V');

            } else if (imageData.getFormat() == ImageFormatEnum.RGB_8_BGRA) {

                ByteBuffer.wrap(charBytes).putChar('R').putChar('G').putChar('B');

            } else if (imageData.getFormat() == ImageFormatEnum.KINECT_INFRARED) {

                ByteBuffer.wrap(charBytes).putChar('I').putChar('N').putChar('F');

            } else {

                throw new UnsupportedOperationException("Color image format not supported: " + imageData.getFormat());
            }

            outputStream.write(charBytes);
        }

        // Write the image width
        byte[] widthBytes = new byte[4];

        ByteBuffer.wrap(widthBytes).putInt(imageData.getWidth());

        outputStream.write(widthBytes);

        // Write the image height
        byte[] heightBytes = new byte[4];

        ByteBuffer.wrap(heightBytes).putInt(imageData.getHeight());

        outputStream.write(heightBytes);
    }

    /**
     * Writes an entry of color data to the color data binary file
     * 
     * @param colorFrame The color data
     * @param elapsedTime The data timestamp
     * @param outputStream The color data binary file output stream
     * @param compressionFormat The format to compress the color data with
     * @throws IOException 
     */
    private static void writeColorData(ImageData colorFrame, long elapsedTime, FileOutputStream outputStream, generated.sensor.ImageCompressionFormat compressionFormat) throws IOException {

        byte[] longBytes = new byte[8];

        ByteBuffer.wrap(longBytes).putLong(elapsedTime);

        outputStream.write(longBytes);

        ByteArrayInputStream colorStream = new ByteArrayInputStream(colorFrame.getData());

        // Write the image in PNG format to the file, the PNG spec can determine
        // how many bytes to read in upon decode
        if (compressionFormat == generated.sensor.ImageCompressionFormat.PNG) {

            ImageInfo imageInfo = new ImageInfo(colorFrame.getWidth(), colorFrame.getHeight(), 8, true, false, false);

            PngWriter pngWriter = new PngWriter(outputStream, imageInfo);

            pngWriter.setCompLevel(0);

            pngWriter.setShouldCloseStream(false);

            byte[] bytes = new byte[imageInfo.bytesPerRow];

            int read = colorStream.read(bytes);

            int row = 0;

            int offset = 0;

            byte tmp;

            while (read != -1) {

                // It is possible to read less than the bytes per row
                // Read until you have read enough bytes
                if ((read + offset) != imageInfo.bytesPerRow) {

                    offset += read;

                    read = colorStream.read(bytes, offset, bytes.length - offset);

                } else {

                    // Convert the Kinect's BGRA format to RGBA
                    for (int i = 0; i < bytes.length; i += 4) {

                        tmp = bytes[i];

                        bytes[i] = bytes[i + 2];

                        bytes[i + 2] = tmp;

                        // For some reason the alpha channel is zero, set it to 1
                        bytes[i + 3] = (byte) (0xFF);
                    }

                    pngWriter.writeRowByte(bytes, row);

                    row += 1;

                    offset = 0;

                    read = colorStream.read(bytes);
                }
            }

            pngWriter.end();

        // Write the image to the file with LZ4 compression
        } else if (compressionFormat == generated.sensor.ImageCompressionFormat.LZ_4) {

            // The LZ4 decode algorithm just needs to know how big the original
            // data was in order to decode
            byte[] originalSizeBytes = new byte[4];

            ByteBuffer.wrap(originalSizeBytes).putInt(colorFrame.getData().length);

            outputStream.write(originalSizeBytes);

            @SuppressWarnings("resource")
            LZ4BlockOutputStream lz4Stream = new LZ4BlockOutputStream(outputStream);

            byte[] bytes = new byte[1024];

            int read = colorStream.read(bytes);

            while (read != -1) {

                lz4Stream.write(bytes, 0, read);

                read = colorStream.read(bytes);
            }

            lz4Stream.finish();

        // Write the raw data to file, the size of this data is fixed and can be
        // calculated by the width, height, and number of channels
        } else if (compressionFormat == null) {

            byte[] bytes = new byte[1024];

            int read = colorStream.read(bytes);

            while (read != -1) {

                outputStream.write(bytes, 0, read);

                read = colorStream.read(bytes);
            }

        } else {

            throw new UnsupportedOperationException("Color compression format not supported: " + compressionFormat.name());
        }

        colorStream.close();

        // Write changes to file
        outputStream.flush();

    }

    /**
     * Writes a custom header for the depth data binary file
     *
     * @param imageData The image data with the configuration of the image
     * @param outputStream The depth data binary file output stream
     * @param compressionFormat The format to compress the depth data with
     * @throws IOException
     */
    private static void writeDepthDataHeader(ImageData imageData, FileOutputStream outputStream, generated.sensor.ImageCompressionFormat compressionFormat) throws IOException {

        byte[] charBytes = new byte[2 * 3];

        // Write the compression format
        if (compressionFormat == generated.sensor.ImageCompressionFormat.PNG) {

            ByteBuffer.wrap(charBytes).putChar('P').putChar('N').putChar('G');

        } else if (compressionFormat == generated.sensor.ImageCompressionFormat.LZ_4) {

            ByteBuffer.wrap(charBytes).putChar('L').putChar('Z').putChar('4');

        } else if (compressionFormat == null) {

            ByteBuffer.wrap(charBytes).putChar('R').putChar('A').putChar('W');

        } else {

            throw new UnsupportedOperationException("Depth Compression format not supported: " + compressionFormat);
        }

        outputStream.write(charBytes);

        // Write the image width
        byte[] widthBytes = new byte[4];

        ByteBuffer.wrap(widthBytes).putInt(imageData.getWidth());

        outputStream.write(widthBytes);

        // Write the image height
        byte[] heightBytes = new byte[4];

        ByteBuffer.wrap(heightBytes).putInt(imageData.getHeight());

        outputStream.write(heightBytes);
    }

    /**
     * Writes an entry of depth data to the depth data binary file
     *
     * @param colorFrame The depth data
     * @param elapsedTime The data timestamp
     * @param outputStream The depth data binary file output stream
     * @param compressionFormat The format to compress the depth data with
     * @throws IOException
     */
    private static void writeDepthData(ImageData depthFrame, long elapsedTime, FileOutputStream outputStream, generated.sensor.ImageCompressionFormat compressionFormat) throws IOException {

        byte[] longBytes = new byte[8];

        ByteBuffer.wrap(longBytes).putLong(elapsedTime);

        outputStream.write(longBytes);

        ByteArrayInputStream depthStream = new ByteArrayInputStream(depthFrame.getData());

        // Write the image in PNG format to the file, the PNG spec can determine
        // how many bytes to read in upon decode
        if (compressionFormat == generated.sensor.ImageCompressionFormat.PNG) {

            ImageInfo imageInfo = new ImageInfo(depthFrame.getWidth(), depthFrame.getHeight(), 16, false, true, false);

            PngWriter pngWriter = new PngWriter(outputStream, imageInfo);

            pngWriter.setCompLevel(0);

            pngWriter.setShouldCloseStream(false);

            byte[] bytes = new byte[imageInfo.bytesPerRow];

            int[] bytesMask = new int[imageInfo.bytesPerRow / 2];

            int read = depthStream.read(bytes);

            int row = 0;

            int offset = 0;

            while (read != -1) {

                // It is possible to read less than the bytes per row
                // Read until you have read enough bytes
                if ((read + offset) != imageInfo.bytesPerRow) {

                    offset += read;

                    read = depthStream.read(bytes, offset, bytes.length - offset);

                } else {

                    // Flip endianess for PNG
                    for (int i = 0; i < bytes.length; i += 2) {

                        int lowBit = bytes[i + 1] & 0xFF;

                        int highBit = bytes[i] & 0xFF;

                        bytesMask[i / 2] = highBit + 256 * lowBit;
                    }

                    pngWriter.writeRow(bytesMask, row);

                    row += 1;

                    offset = 0;

                    read = depthStream.read(bytes);
                }
            }

            pngWriter.end();

        // Write the image to the file with LZ4 compression
        } else if (compressionFormat == generated.sensor.ImageCompressionFormat.LZ_4) {

            // The LZ4 decode algorithm just needs to know how big the original
            // data was in order to decode
            byte[] originalSizeBytes = new byte[4];

            ByteBuffer.wrap(originalSizeBytes).putInt(depthFrame.getData().length);

            outputStream.write(originalSizeBytes);

            @SuppressWarnings("resource")
            LZ4BlockOutputStream lz4Stream = new LZ4BlockOutputStream(outputStream);

            byte[] bytes = new byte[1024];

            int read = depthStream.read(bytes);

            while (read != -1) {

                lz4Stream.write(bytes, 0, read);

                read = depthStream.read(bytes);
            }

            lz4Stream.finish();

        // Write the raw data to file, the size of this data is fixed and can be
        // calculated by the width, height, and number of channels
        } else if (compressionFormat == null) {

            byte[] bytes = new byte[1024];

            int read = depthStream.read(bytes);

            while (read != -1) {

                outputStream.write(bytes, 0, read);

                read = depthStream.read(bytes);
            }

        } else {

            throw new UnsupportedOperationException("Depth compression format not supported: " + compressionFormat.name());
        }

        depthStream.close();

        // Write changes to file
        outputStream.flush();
    }
}
