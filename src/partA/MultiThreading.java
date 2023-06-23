package partA;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.DataBufferByte;

//main class
public class MultiThreading {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String originalImagePath = "C:\\\\\\\\Users\\\\\\\\razve\\\\\\\\OneDrive\\\\\\\\Desktop\\\\\\\\331_project\\\\\\\\Rain_Tree1.jpg";
		String greyImagePath = "C:\\\\\\\\Users\\\\\\\\razve\\\\\\\\OneDrive\\\\\\\\Desktop\\\\\\\\331_project\\\\\\\\GRAYED_Rain_Tree1.jpg";
		
		BufferedImage greyImg = gImage(originalImagePath, greyImagePath);
		int[][] array = convertTo2DArray(greyImg);
		int[] oneArray = convertTo1DArray(array);
		
		
	}//main ends here
	
	// get gray scale image 
	public static BufferedImage gImage(String originalFile, String grayFile) {
		 BufferedImage img = null;
		    File f = null;
		    File f1 = null;

		    //read image
		    try{
		      f = new File(originalFile);
		      img = ImageIO.read(f);
		    }
		    catch(IOException e){
		      System.out.println("File not Found!");
		    }

		    //get image width and height
		    int width = img.getWidth();
		    int height = img.getHeight();

		    //convert to gray scale
		    for(int y = 0; y < height; y++){
		      for(int x = 0; x < width; x++){
		        int p = img.getRGB(x,y);

		        int a = (p>>24)&0xff;
		        int r = (p>>16)&0xff;
		        int g = (p>>8)&0xff;
		        int b = p&0xff;

		        //calculate average
		        int avg = (r+g+b)/3;

		        //replace RGB value with average
		        p = (a<<24) | (avg<<16) | (avg<<8) | avg;

		        img.setRGB(x, y, p);
		      }
		    }

		    //write image
		    try{
		      f1 = new File(grayFile);
		      ImageIO.write(img, "jpg", f1);
		    }catch(IOException e){
		      System.out.println("File couldn't be written!");
		    }
		    
		    //pass the buffered image to convert it to array
		    return img;
	}//Buffered Image ends here
	
	
	// convert the buffered image into an array
	public static int[][] convertTo2DArray(BufferedImage img) {
        
    final byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData(); // get pixel value as single array from buffered Image
    final int width = img.getWidth(); //get image width value
    final int height = img.getHeight(); //get image height value
    int[][] result = new int[height][width]; //Initialize the array with height and width
    
    
    
    //this loop allocates pixels value to two dimensional array
    for (int pixel = 0, row = 0, col = 0; row < height; row++) {
        for (int col1 = 0; col1 < width; col1++, pixel++) {
            int argb = pixels[pixel] & 0xFF;
            result[row][col1] = argb;
        }
    }
 // Print the values of the result array
   /* for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            System.out.print(result[i][j] + " ");
        }
        System.out.println();
    }*/
    return result; //return the result as two dimensional array
	} //convert to 2D array ends here
	
	
	// define method which converts the 2D array into 1D array
	public static int[] convertTo1DArray(int[][]array2D) {

		int totalElements = array2D.length * array2D[0].length;
	    int[] b = new int[totalElements];
	    int c = 0;

	    for (int k = 0; k < array2D.length; k++) {
	        for (int m = 0; m < array2D[k].length; m++) {
	            b[c] = array2D[k][m];
	            System.out.println(b[c]);
	            c++;
	        }
	    }
	    return b;
	}//convert to 1D Array ends here
	
	
	
   }//class ends here
