package org.example.app.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageFormatter {

    public static InputStream reformatImage(File img, String formatName) throws IOException {
        BufferedImage image = ImageIO.read((img));

        byte[] bytes;
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(image, formatName, stream);
            bytes = stream.toByteArray();
        }

        return new ByteArrayInputStream(bytes);
    }

}
