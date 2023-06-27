package partA;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicIntegerArray;
import javax.imageio.ImageIO;

public class MultiThreading2 {
    private static final int L = 255;

    public static void main(String[] args) {
        String originalImagePath = "C:\\Users\\razve\\OneDrive\\Desktop\\331_project\\rk_331_Q1\\Rain_Tree.jpg";
        String greyImagePath = "C:\\Users\\razve\\OneDrive\\Desktop\\331_project\\rk_331_Q1\\GRAYED_Rain_TreeMT.jpg";
        String equalImagePath = "C:\\Users\\razve\\OneDrive\\Desktop\\331_project\\rk_331_Q1\\EQ_Rain_TreeMT.jpg";

        // Start measuring time for the entire process
        long startTotalTime = System.nanoTime();

        // Start measuring time for grayscaling
        long startGrayscaleTime = System.nanoTime();
        BufferedImage greyImg = gImage(originalImagePath, greyImagePath);
        long endGrayscaleTime = System.nanoTime();
        long grayscaleElapsedTime = endGrayscaleTime - startGrayscaleTime;

        byte[][] array = convertTo2DArray(greyImg);
        byte[] oneArray = convertTo1DArray(array);

        final int numOfThreads = 1000;
        AtomicIntegerArray histogram = new AtomicIntegerArray(L + 1);
        AtomicIntegerArray[] subHistograms = new AtomicIntegerArray[numOfThreads];
        for (int i = 0; i < numOfThreads; i++) {
            subHistograms[i] = new AtomicIntegerArray(L + 1);
        }

        int totalPix = greyImg.getHeight() * greyImg.getWidth();
        final int pixThread = totalPix / numOfThreads;
        int start = 0;
        int end = 0;

        Thread[] threads = new Thread[numOfThreads];

        // Start measuring time for dividing threads and starting them
        long startThreadTime = System.nanoTime();

        for (int r = 0; r < numOfThreads; r++) {
            start = r * pixThread;
            end = ((r + 1) * pixThread) - 1;
            threads[r] = new Thread(new EqualizeRunnable(start, end, oneArray, subHistograms[r]));
            threads[r].start();
        }

        // Wait for all threads to complete
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Stop measuring time for dividing threads and joining them
        long endThreadTime = System.nanoTime();
        long threadElapsedTime = endThreadTime - startThreadTime;

        // Combine sub-histograms into the final histogram
        for (AtomicIntegerArray subHistogram : subHistograms) {
            for (int i = 0; i <= L; i++) {
                histogram.getAndAdd(i, subHistogram.get(i));
            }
        }

        // Start measuring time for histogram equalization and generating the equalized image
        long startEqualizationTime = System.nanoTime();

        int[] cumulativeHist = new int[L + 1];
        cumulativeHist[0] = histogram.get(0);
        for (int i = 1; i <= L; i++) {
            cumulativeHist[i] = cumulativeHist[i - 1] + histogram.get(i);
        }

        byte[] equalizedValues = new byte[totalPix];
        for (int i = 0; i < totalPix; i++) {
            int pixelValue = oneArray[i] & 0xFF;
            equalizedValues[i] = (byte) ((cumulativeHist[pixelValue] * L) / totalPix);
        }

        // Stop measuring time for histogram equalization and generating the equalized image
        long endEqualizationTime = System.nanoTime();
        long equalizationElapsedTime = endEqualizationTime - startEqualizationTime;

        BufferedImage equalizedImage = convertToBufferedImage(equalizedValues, greyImg.getWidth(), greyImg.getHeight());

        try {
            File equalizedFile = new File(equalImagePath);
            ImageIO.write(equalizedImage, "jpg", equalizedFile);
        } catch (IOException e) {
            System.out.println("Equalized file couldn't be written!");
        }

        // Stop measuring time for the entire process
        long endTotalTime = System.nanoTime();
        long totalElapsedTime = endTotalTime - startTotalTime;

        // Print the elapsed times
        double grayscaleElapsedTimeMs = grayscaleElapsedTime / 1000000.0;
        double threadElapsedTimeMs = threadElapsedTime / 1000000.0;
        double equalizationElapsedTimeMs = equalizationElapsedTime / 1000000.0;
        double totalElapsedTimeMs = totalElapsedTime / 1000000.0;

        System.out.println("Elapsed Time for grayscaling image: " + grayscaleElapsedTimeMs + " ms");
        System.out.println("Elapsed Time for dividing threads and starting them: " + threadElapsedTimeMs + " ms");
        System.out.println("Elapsed Time for histogram equalization and generating the equalized image: " + equalizationElapsedTimeMs + " ms");
        System.out.println("Total Elapsed Time for the entire process: " + totalElapsedTimeMs + " ms");
    }



    public static BufferedImage gImage(String originalFile, String grayFile) {
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

    public static byte[][] convertTo2DArray(BufferedImage img) {
        final byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        final int width = img.getWidth();
        final int height = img.getHeight();
        byte[][] result = new byte[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int pixel = (row * width + col);
                result[row][col] = pixels[pixel];
            }
        }

        return result;
    }

    public static byte[] convertTo1DArray(byte[][] array2D) {
        int totalElements = array2D.length * array2D[0].length;
        byte[] result = new byte[totalElements];
        int c = 0;

        for (int k = 0; k < array2D.length; k++) {
            for (int m = 0; m < array2D[k].length; m++) {
                result[c] = array2D[k][m];
                c++;
            }
        }

        return result;
    }

    private static class EqualizeRunnable implements Runnable {
        private final int start;
        private final int end;
        private final byte[] oneArray;
        private final AtomicIntegerArray subHistogram;

        public EqualizeRunnable(int start, int end, byte[] oneArray, AtomicIntegerArray subHistogram) {
            this.start = start;
            this.end = end;
            this.oneArray = oneArray;
            this.subHistogram = subHistogram;
        }

        @Override
        public void run() {
            for (int i = start; i <= end; i++) {
                int pixelValue = oneArray[i] & 0xFF;
                subHistogram.incrementAndGet(pixelValue);
            }
        }
    }

    public static BufferedImage convertToBufferedImage(byte[] array, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        final byte[] imgPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(array, 0, imgPixels, 0, array.length);
        return image;
    }
}
