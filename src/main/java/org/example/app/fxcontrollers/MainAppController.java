package org.example.app.fxcontrollers;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.example.app.utils.FingerprintMatchResult;
import org.example.app.utils.FingerprintRecognition;
import org.example.app.utils.ImageFormatter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class MainAppController {

    private final static String[] supportedExtensions = new String[]{"jpg", "png", "bmp", "jpeg", "gif"};

    @FXML
    private ImageView MainFingerprintImgView;

    @FXML
    private ImageView TestingFingerprintImgView;

    @FXML
    private BorderPane TestingImgBorderPane;

    @FXML
    private Button MainFingerprintUploadBtn;

    @FXML
    private Button TestingFingerprintUploadBtn;

    @FXML
    private TextField AccuracyTextField;

    @FXML
    private Button MatchingBtn;

    private final BooleanBinding disableMatchingBtn;

    private final static double DEFAULT_ACCURACY = 0.9999;

    private final static Predicate<String> decimalPredicate = Pattern.compile("0,\\d{0,5}").asMatchPredicate();

    private final FingerprintRecognition recognition;

    private final DoubleProperty accuracy;

    private final StringProperty probeImageUrl;

    private final StringProperty candidateImageUrl;

    private final Property<Boolean> lastResult;

    public MainAppController() {
        accuracy = new SimpleDoubleProperty();
        probeImageUrl = new SimpleStringProperty(null);
        candidateImageUrl = new SimpleStringProperty(null);

        disableMatchingBtn = Bindings.createBooleanBinding(this::disableMatchingBtn, probeImageUrl, candidateImageUrl);

        recognition = new FingerprintRecognition();
        lastResult = new SimpleObjectProperty<>(null);
    }

    private boolean disableMatchingBtn() {
        return probeImageUrl.get() == null || candidateImageUrl.get() == null;
    }

    @FXML
    public void initialize() {
        configAccuracyTextField();
        configMainFingerprintUploadBtn();
        configTestingFingerprintUploadBtn();
        configViews();
        configMatchingBtn();
        configBorderPane();
    }

    private void configViews() {
        MainFingerprintImgView.imageProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                lastResult.setValue(null);
            }
        });

        TestingFingerprintImgView.imageProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                lastResult.setValue(null);
            }
        }));
    }

    private void configMatchingBtn() {
        MatchingBtn.disableProperty().bind(disableMatchingBtn);
        MatchingBtn.setOnMouseClicked(event -> showMatching());
    }

    private void showMatching() {
        double accuracy = this.accuracy.get();
        String probeImage = probeImageUrl.get();
        String candidateImage = candidateImageUrl.get();

        try {
            FingerprintMatchResult result = recognition.matchResult(probeImage, candidateImage, accuracy);
            lastResult.setValue(result.isMatch());
            showResult(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configBorderPane() {
        StringConverter<Boolean> converter = new StringConverter<>() {
            private final String style = "-fx-border-color: %s; -fx-border-width:5px";

            private final String defaultStyle = String.format(style, "transparent");
            private final String isMatchStyle = String.format(style, "green");
            private final String isNotMatchStyle = String.format(style, "red");

            @Override
            public String toString(Boolean object) {
                if (object == null) {
                    return defaultStyle;
                }

                return object ? isMatchStyle : isNotMatchStyle;
            }

            @Override
            public Boolean fromString(String string) {
                return null;
            }
        };


        Bindings.bindBidirectional(TestingImgBorderPane.styleProperty(), lastResult, converter);
    }

    private void showResult(FingerprintMatchResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fingerprint match result");
        alert.setHeaderText("Matching result:");
        alert.setContentText(String.format(
                "Matching score: %.4f\nThreshold: %.4f\nAre matches: %s", result.score(), result.threshold(),
                result.isMatch() ? "Yes" : "No"
                ));
        alert.show();
    }

    private void configMainFingerprintUploadBtn() {
        MainFingerprintUploadBtn.setOnMouseClicked(event -> {
            File file = chooseImageFile();
            setupImageInView(MainFingerprintImgView, file);
            setUrl(probeImageUrl, file);
        });
    }

    private void setUrl(StringProperty property, File file) {
        if (file == null) {
            return;
        }

        property.set(file.toPath().toString());
    }

    private void configTestingFingerprintUploadBtn() {
        TestingFingerprintUploadBtn.setOnMouseClicked(event -> {
            File file = chooseImageFile();
            setupImageInView(TestingFingerprintImgView, file);
            setUrl(candidateImageUrl, file);
        });
    }

    private void setupImageInView(ImageView imgView, File imageFile) {
        if (imageFile == null) {
            return;
        }

        Image image;
        if (isSupported(imageFile)) {
            image = new Image(imageFile.toURI().toString());
        } else {
            image = reformatImage(imageFile, "png");
        }

        imgView.setImage(image);
    }

    private Image reformatImage(File imageFile, String format) {
        try(InputStream stream = ImageFormatter.reformatImage(imageFile, format)) {
            return new Image(stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isSupported(File imageFile) {
        Path path = imageFile.toPath();

        boolean supported = false;

        int i = 0;
        int size = supportedExtensions.length;
        while (!supported && i < size) {
            supported = path.endsWith(supportedExtensions[i]);
            i++;
        }

        return supported;
    }

    private Window getWindow() {
        return MatchingBtn.getScene().getWindow();
    }

    private File chooseImageFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open fingerprint image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png", "*.bmp", "*.tif")
        );

        return chooser.showOpenDialog(getWindow());
    }

    private void configAccuracyTextField() {
        DecimalFormat format = new DecimalFormat("0.#####");
        format.getDecimalFormatSymbols().setDecimalSeparator(',');
        AccuracyTextField.setTextFormatter(getDecimalTextFormatter());
        AccuracyTextField.setText(format.format(DEFAULT_ACCURACY));

        Bindings.bindBidirectional(AccuracyTextField.textProperty(), accuracy, converter(format));
        this.accuracy.set(DEFAULT_ACCURACY);
    }

    @SuppressWarnings("unchecked")
    private StringConverter<Number> converter(DecimalFormat format) {
        StringConverter<? extends Number> converter = new NumberStringConverter(format);
        return (StringConverter<Number>) converter;
    }

    private TextFormatter<Object> getDecimalTextFormatter() {
        return new TextFormatter<>(c -> {
            if (!decimalPredicate.test(c.getControlNewText())) {
                return null;
            }
            else {
                return c;
            }
        });
    }
}
