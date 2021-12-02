package org.example.app.utils;

import com.machinezoo.sourceafis.FingerprintImage;
import com.machinezoo.sourceafis.FingerprintImageOptions;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FingerprintRecognition {

    private final static int DEFAULT_DPI = 500;

    public FingerprintRecognition() {}

    private FingerprintTemplate getTemplate(String fileName) throws IOException {
        double dpi;
        try {
            dpi = dpi(fileName);
        } catch (ImageReadException e) {
            e.printStackTrace();
            dpi = DEFAULT_DPI;
        }

        return new FingerprintTemplate(
                new FingerprintImage(
                        Files.readAllBytes(Paths.get(fileName)),
                        new FingerprintImageOptions()
                                .dpi(dpi)
                )
        );
    }

    public FingerprintMatchResult matchResult(String probeFileName,
                                              String candidateFileName, double accuracy) throws IOException {
        FingerprintTemplate probe = getTemplate(probeFileName);
        FingerprintTemplate candidate = getTemplate(candidateFileName);

        double threshold = getThreshold(accuracy);
        double score = getMatcher(probe).match(candidate);

        return new FingerprintMatchResult(score, threshold);
    }

    private FingerprintMatcher getMatcher(FingerprintTemplate probe) {
        return new FingerprintMatcher(probe);
    }

    private double getThreshold(double accuracy) {
        double x = 1. - accuracy;
        return -Math.log10(x) * 10;
    }

    private double dpi(String imageFileName) throws IOException, ImageReadException {
        ImageInfo imageInfo = Imaging.getImageInfo(new File(imageFileName));
        int physicalWidthDpi = imageInfo.getPhysicalWidthDpi();

        if (physicalWidthDpi == -1) {
            physicalWidthDpi = DEFAULT_DPI;
        }

        return physicalWidthDpi;
    }
}
