package partA;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Equalizer {
    
    public static void main(String[] args) {
        
        String imagePath = "C:\\Users\\razve\\OneDrive\\Desktop\\331_project\\rk_331_Q1";
        String photoPath = imagePath + "\\Rain_Tree.jpg";
        String writePhotoPath = imagePath + "\\GRAYED_Rain_TreeST.jpg";
        
        
        BufferedImage img = null;
        File f = null;

        //start clock to see time to read image
        long startTime1 = System.nanoTime();//read image(only)
        long startTime2 = System.nanoTime();//whole process
        //read image
        try {
            f = new File(photoPath);
            img = ImageIO.read(f);
        } catch(IOException e) {
            System.out.println("File not found!");
            return;
        }
        long endTime1 = System.nanoTime(); //time stopped for reading image
        
        Equalizer photoProcessor = new Equalizer();
        
        // turn the image into grayscale
        long startTime = System.nanoTime();
        BufferedImage grayImage = photoProcessor.gImage(photoPath,writePhotoPath);
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        // write the grayscale image
        try {
            File grayFile = new File(writePhotoPath);
            ImageIO.write(grayImage, "jpg", grayFile);
        } catch(IOException e) {
            System.out.println("Failed to write the grayscale image!");
            return;
        }

        // equalize the image
        startTime = System.nanoTime();
        BufferedImage equalizedImage = photoProcessor.photoEqualizer(grayImage);
        endTime = System.nanoTime();
        elapsedTime += endTime - startTime;

        //elapsed time for reading image
        long elapsedTime1 = endTime1 - startTime1;
        elapsedTime1 += endTime1 - startTime1;
        
        
        // write the equalized image
        try {
            File equalizedFile = new File(imagePath + "\\EQ_ImageST.jpg");
            ImageIO.write(equalizedImage, "jpg", equalizedFile);
        } catch(IOException e) {
            System.out.println("Failed to write the equalized image!");
            return;
        }
        
        //end time for single thread
        long endTime2 = System.nanoTime();
        long elapsedTime2 = endTime2 - startTime2;
        elapsedTime2 += endTime2 - startTime2;

        // Calculate and print the elapsed times
        
     // Calculate and print the elapsed time for reading image
        double elapsedTimeMs1 = elapsedTime1 / 1_000_000.0;
        System.out.println("Elapsed Time for reading image: " + elapsedTimeMs1 + " ms");
        
        //print elapsed time for gray scaling image
        double elapsedTimeMs = elapsedTime / 1_000_000.0;
        System.out.println("Elapsed Time for gray scaling image: " + elapsedTimeMs + " ms");
        
        //print elapsed time for overall single thread process
        double elapsedTimeMs2 = elapsedTime2 / 1_000_000.0;
        System.out.println("Elapsed Time for single thread implementation: " + elapsedTimeMs2 + " ms");
    }
    
    public BufferedImage gImage(String originalFile, String grayFile) {
        BufferedImage img = null;
        File f = null;
        File f1 = null;

        try {
            f = new File(originalFile);
            img = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println("File not Found!");
        }

        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage grayImg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        grayImg.getGraphics().drawImage(img, 0, 0, null);

        try {
            f1 = new File(grayFile);
            ImageIO.write(grayImg, "jpg", f1);
        } catch (IOException e) {
            System.out.println("File couldn't be written!");
        }

        return grayImg;
    }

    
    public BufferedImage photoEqualizer(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        BufferedImage equalizedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        
        int[] histogram = new int[256];
        int size = width * height;
        
        // Calculate histogram
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                histogram[pixel]++;
            }
        }
        
        int[] cumulativeHist = new int[256];
        cumulativeHist[0] = histogram[0];
        
        // Calculate cumulative histogram
        for (int i = 1; i < 256; i++) {
            cumulativeHist[i] = cumulativeHist[i - 1] + histogram[i];
        }
        
        // Apply equalization
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                int equalizedPixel = cumulativeHist[pixel] * 255 / size;
                int equalizedRGB = (equalizedPixel << 16) | (equalizedPixel << 8) | equalizedPixel;
                
                equalizedImage.setRGB(x, y, equalizedRGB);
            }
        }
        
        return equalizedImage;
    }
}
