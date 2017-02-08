package io.amath.imagefactory;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * Created by zhaolin on 2017/2/8.
 */
public class DpiHelper {
    private static final double INCH_2_CM = 2.54;
    public static void main(String[] args){
        try {
            File input = new File(DpiHelper.class.getResource("/resource/test.png").toURI());
            File output = new File(DpiHelper.class.getResource("/resource/").toString().replace("file:/","")+"test_300.png");
            RenderedImage gridImage = ImageIO.read(input);
            new DpiHelper().processPng(gridImage, 300, new FileOutputStream(output));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void processPng(RenderedImage image, int dpi, OutputStream output) throws IOException {
        final String formatName = "png";
        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                continue;
            }

            setDPI(metadata, dpi);

            final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
            try {
                writer.setOutput(stream);
                writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
            } finally {
                stream.close();
            }
            break;
        }
    }

    private void setDPI(IIOMetadata metadata, int dpi) throws IIOInvalidTreeException {

        // for PMG, it's dots per millimeter
        double dotsPerMilli = 1.0 * dpi / 10 / INCH_2_CM;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }
}
