package diamondyuan.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Qrcode {


  private static BitMatrix deleteWhite(BitMatrix matrix) {
    int[] rec = matrix.getEnclosingRectangle();
    int resWidth = rec[2];
    int resHeight = rec[3];
    BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
    resMatrix.clear();
    for (int i = 0; i < resWidth; i++) {
      for (int j = 0; j < resHeight; j++) {
        if (matrix.get(i + rec[0], j + rec[1])) {
          resMatrix.set(i, j);
        }
      }
    }
    return resMatrix;
  }

  private static BufferedImage generate(int size, String content) throws WriterException {
    Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
    QRCodeWriter writer = new QRCodeWriter();
    BitMatrix bitMatrix;
    bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
    bitMatrix = deleteWhite(bitMatrix);
    return zoomOutImage(MatrixToImageWriter.
      toBufferedImage(bitMatrix,
        new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF)), size, size);
  }


  private static BufferedImage zoomOutImage(BufferedImage originalImage, int width, int height) {
    BufferedImage newImage = new BufferedImage(width, height, originalImage.getType());
    Graphics g = newImage.getGraphics();
    g.drawImage(originalImage, 0, 0, width, height, null);
    g.dispose();
    return newImage;
  }



  public static byte[] result(String content) throws IOException, WriterException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    BufferedImage qrImage = generate(300, content);
    ImageIO.write(qrImage, "png", os);
    return os.toByteArray();
  }


}
