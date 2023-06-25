package partA;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;

public class MultiThreading {

	public static void main(String[] args) {
	    String originalImagePath = "C:\\Users\\razve\\OneDrive\\Desktop\\331_project\\rk_331_Q1\\Rain_Tree1.jpg";
	    String greyImagePath = "C:\\Users\\razve\\OneDrive\\Desktop\\331_project\\rk_331_Q1\\GRAYED_Rain_Tree1.jpg";
	    String equalImagePath = "C:\\Users\\razve\\OneDrive\\Desktop\\331_project\\rk_331_Q1\\EQ_Rain_Tree1.jpg";

	    BufferedImage greyImg = gImage(originalImagePath, greyImagePath);
	    byte[][] array = convertTo2DArray(greyImg);
	    byte[] oneArray = convertTo1DArray(array);

	    int[] histogram = new int[256];

	    final int numOfThreads = 50;
	    int totalPix = greyImg.getHeight() * greyImg.getWidth();
	    final int pixThread = totalPix / numOfThreads;
	    int start = 0;
	    int end = 0;

	    Thread[] threads = new Thread[numOfThreads];

	    for (int r = 0; r < numOfThreads; r++) {
	        start = r * pixThread;
	        end = (r == numOfThreads - 1) ? totalPix - 1 : ((r + 1) * pixThread) - 1;
	        threads[r] = new Thread(new EqualizeRunnable(start, end, oneArray, histogram));
	        threads[r].start();
	    }

	    try {
	        for (Thread t : threads) {
	            t.join();
	        }
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    }

	    int[] cumulativeHist = new int[256];
	    cumulativeHist[0] = histogram[0];
	    for (int i = 1; i < 256; i++) {
	        cumulativeHist[i] = cumulativeHist[i - 1] + histogram[i];
	    }

	    byte[] equalizedValues = new byte[totalPix];
	    for (int i = 0; i < totalPix; i++) {
	        int pixelValue = oneArray[i] & 0xFF;
	        equalizedValues[i] = (byte) ((cumulativeHist[pixelValue] * 255) / totalPix);
	    }

	    BufferedImage equalizedImage = convertToBufferedImage(equalizedValues, greyImg.getWidth(), greyImg.getHeight());

	    try {
	        File equalizedFile = new File(equalImagePath);
	        ImageIO.write(equalizedImage, "jpg", equalizedFile);
	    } catch (IOException e) {
	        System.out.println("Equalized file couldn't be written!");
	    }
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

    public static byte[] allHistEQ(int start, int end, byte[] oneArray) {
        int L = 255;
        int size = (end - start + 1);

        int[] histogram = new int[L + 1];
        for (int i = start; i <= end; i++) {
            int pixelValue = oneArray[i] & 0xFF;
            histogram[pixelValue]++;
        }

        int[] cumulativeHist = new int[L + 1];
        cumulativeHist[0] = histogram[0];
        for (int i = 1; i <= L; i++) {
            cumulativeHist[i] = cumulativeHist[i - 1] + histogram[i];
        }

        byte[] equalizedValues = new byte[size];
        for (int i = start; i <= end; i++) {
            int pixelValue = oneArray[i] & 0xFF;
            equalizedValues[i - start] = (byte) ((cumulativeHist[pixelValue] * L) / (end - start + 1));
        }

        return equalizedValues;
    }


    public static byte[] concatenateArrays(byte[][] arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        byte[] result = new byte[totalLength];
        int currentIndex = 0;

        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

        return result;
    }

    private static class EqualizeRunnable implements Runnable {
        private final int start;
        private final int end;
        private final byte[] oneArray;
        private final int[] histogram;

        public EqualizeRunnable(int start, int end, byte[] oneArray, int[] histogram) {
            this.start = start;
            this.end = end;
            this.oneArray = oneArray;
            this.histogram = histogram;
        }

        @Override
        public void run() {
            for (int i = start; i <= end; i++) {
                int pixelValue = oneArray[i] & 0xFF;
                synchronized (histogram) {
                    histogram[pixelValue]++;
                }
            }
        }
    }

    public static BufferedImage convertToBufferedImage(byte[] outArray, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        byte[] imagePixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < outArray.length; i++) {
            int row = i / width;
            int col = i % width;
            int pixelValue = outArray[i] & 0xFF;
            imagePixels[row * width + col] = (byte) pixelValue;
        }

        return image;
    }

}
