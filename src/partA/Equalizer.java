package partA;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class Equalizer {
	
	public static void main(String[] args) {
		
	String imagePath = "C:\\\\Users\\\\razve\\\\OneDrive\\\\Desktop\\\\331_project";
	String photoPath = imagePath + "\\\\Rain_Tree.jpg";
	String writePhotoPath = imagePath + "\\\\GRAYED_Rain_Tree.jpg";
	
	
	 BufferedImage img = null;
	    File f = null;

	    //read image
	    try{
	      f = new File(photoPath);
	      img = ImageIO.read(f);
	    }
	    catch(IOException e){
	      System.out.println("File not Found!");
	    }
	
		Equalizer photoProcessor = new Equalizer();
		// turn the image into gray scale
		photoProcessor.GrayImage(f, img, photoPath, writePhotoPath);
		// equalize the image as a single thread
		photoProcessor.PhotoEqualizer(img,writePhotoPath,imagePath);
	
	}
	
	public File GrayImage(File file, BufferedImage image, String srcImg, String modImg) {
		    

		    //get image width and height
		    int width = image.getWidth();
		    int height = image.getHeight();

		    //convert to gray scale
		    for(int y = 0; y < height; y++){
		      for(int x = 0; x < width; x++){
		        int p = image.getRGB(x,y);

		        int a = (p>>24)&0xff; // shift total average by 24 pixels
		        int r = (p>>16)&0xff; // shift red pixels by 16 pixels
		        int g = (p>>8)&0xff; // shift green pixels by 8 pixels
		        int b = p&0xff; // blue pixels are non-shifted 

		        //calculate average
		        int avg = (r+g+b)/3;

		        //replace RGB value with avg
		        p = (a<<24) | (avg<<16) | (avg<<8) | avg;

		        image.setRGB(x, y, p);
		      }
		    }

		    //write image
		    try{
		      file = new File(modImg);
		      ImageIO.write(image, "jpg", file);
		    }catch(IOException e){
		      System.out.println("File couldn't be written!");
		    }
		    
		    return file;
		  }

	
	public void PhotoEqualizer(BufferedImage srcImage, String srcImg, String photoPath) {
		
		//define the path for equalized image to be stored in
		String writeImag = photoPath + "\\\\EQ_Image.jpg";
		
		// get the width and height of source image
		int width1 = srcImage.getWidth();
		int height1 = srcImage.getHeight();
		
		// find the total pixels of the image
		int size = width1*height1;
		
		// create an empty array for histogram for 8-bits
		int[] Histogram = new int[256];
		
		// calculate histogram
		for (int i=0; i< width1; i++) {
			for (int j =0; j<height1; j++) {
				int pixel = srcImage.getRGB(i, j);
		        Histogram[(pixel >> 16) & 0xFF]++;		
			}
		}
		
		int[] CumulativeHist = new int[256];
		
		// setting first value of the cumulative histogram array
		CumulativeHist[0] = Histogram[0];
		
		for (int z=1; z<256; z++) {
		CumulativeHist[z] = CumulativeHist[z-1] + Histogram[z];
		}
		
		// creating a new image using nested loops 
		
		// first creating the image
		BufferedImage eqImage = new BufferedImage(width1,height1,BufferedImage.TYPE_INT_RGB);
		
		//set equalized values using nested loops
		for (int a=0; a<width1; a++) {
			for (int b=0; b<height1; b++) {
				int pixels = srcImage.getRGB(a, b);
		        int graypx = (pixels >> 16) & 0xFF;
		        
		        int EQgray = CumulativeHist[graypx]*255/size;
		        
		        int EQRGB = (EQgray<<16)|(EQgray<<8)|EQgray;
		        
		        eqImage.setRGB(a, b, EQRGB);     
			}
			
		// write the equalized image
			try{
				File file1 = new File(writeImag);
			      ImageIO.write(eqImage, "jpg", file1);
			    }
			catch(IOException e){
			      System.out.println("Equalised file couldn't be written!");    
		}
	}
	}
}
		
		
		
		
		

		    
		    
		
	
