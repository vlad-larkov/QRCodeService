package qrcodeapi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.BufferedImage;
import java.util.Map;

@RestController
public class Controller {


    @GetMapping("/api/health")
    public ResponseEntity<?> getHealth() {
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/api/qrcode")
    public ResponseEntity<?> getImage(@RequestParam(defaultValue = "250", required = false) int size, @RequestParam(defaultValue = "png", required = false) String type, @RequestParam String contents, @RequestParam(defaultValue = "L", required = false) String correction) {
        if (contents == null || contents.isBlank()) {
            return new ResponseEntity<>(Map.of("error", "Contents cannot be null or blank"), HttpStatus.BAD_REQUEST);//400
        }
        if (size > 350 || size < 150) {
            return new ResponseEntity<>(Map.of("error", "Image size must be between 150 and 350 pixels"), HttpStatus.BAD_REQUEST);
        } else if (!(correction.equals("L") || correction.equals("M") || correction.equals("Q") || correction.equals("H"))) {
            return new ResponseEntity<>(Map.of("error", "Permitted error correction levels are L, M, Q, H"), HttpStatus.BAD_REQUEST);
        } else if (type == null) {
            return new ResponseEntity<>(Map.of("error", "Only png, jpeg and gif image types are supported"), HttpStatus.BAD_REQUEST);
        } else if (!(type.equalsIgnoreCase("png") || type.equalsIgnoreCase("jpeg") || type.equalsIgnoreCase("gif"))) {
            return new ResponseEntity<>(Map.of("error", "Only png, jpeg and gif image types are supported"), HttpStatus.BAD_REQUEST);
        }

        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, ?> hints;
        switch (correction) {
            // Case statements
            case "L":
                hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                break;
            case "M":
                hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
                break;
            case "Q":
                hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.Q);
                break;
            case "H":
                hints = Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
                break;
            // Default case statement
            default:
                return new ResponseEntity<>(Map.of("error", "Permitted error correction levels are L, M, Q, H"), HttpStatus.BAD_REQUEST);
        }
        try {
            BitMatrix bitMatrix = writer.encode(contents, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return ResponseEntity
                    .ok()
                    .contentType(new MediaType("image", type))
                    .body(bufferedImage);
        } catch (WriterException e) {
            // handle the WriterException
        }
        return new ResponseEntity<>(Map.of("error", "Unhandled exception in getImage method"), HttpStatus.BAD_REQUEST);
    }

}
