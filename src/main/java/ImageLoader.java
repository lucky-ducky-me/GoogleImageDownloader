import lombok.Data;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Data
public class ImageLoader {
    String destinationDir;

    int imageId = 1;

    public ImageLoader(String destinationDir) {
        setDestinationDir(destinationDir);
    }

    public String getDestinationDir() {
        return destinationDir;
    }

    public void setDestinationDir(String destinationDir) {
        if (destinationDir == null || destinationDir.isEmpty()) {
            throw new IllegalArgumentException("Путь до папки неопределён.");
        }

        this.destinationDir = destinationDir;
    }

    public void saveImage(String imageUrl) throws IOException {
        saveImage(imageUrl, "");
    }

    public void saveImage(String imageUrl, String patternWord) throws IOException {
        var url = new URL(imageUrl);

        var httpUrlConnection = (HttpURLConnection) url.openConnection();

        httpUrlConnection.setRequestMethod("GET");
        httpUrlConnection.setRequestProperty("Accept", "image/*");
        httpUrlConnection.addRequestProperty("User-Agent", "Mozilla/4.76");
        httpUrlConnection.connect();

        var file = new File(destinationDir);

        if (!file.exists()) {
            file.mkdir();
        }

        file = new File(destinationDir + "\\" + patternWord + imageId + ".jpg");

        imageId++;

        if (!file.exists()) {
            file.createNewFile();
        }
        else {
            file.delete();
            file.createNewFile();
        }

        var img = ImageIO.read(httpUrlConnection.getInputStream());

        if (img != null) {
            img = removeAlphaChannel(img);

            var val = ImageIO.write(img, "jpg", file);
        }
    }

    private static BufferedImage removeAlphaChannel(BufferedImage img) {
        if (!img.getColorModel().hasAlpha()) {
            return img;
        }

        BufferedImage target = createImage(img.getWidth(), img.getHeight(), false);
        Graphics2D g = target.createGraphics();

        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return target;
    }

    private static BufferedImage createImage(int width, int height, boolean hasAlpha) {
        return new BufferedImage(width, height, hasAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
    }
}
