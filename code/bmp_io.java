import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public final class bmp_io {

	public static void main(String[] args) throws IOException {
		String[] files = {"Nature", "Manmade"};
		for (String filestring : files) {

			BmpImage bmp = null;
			String file = "pics/u6/" + filestring + "_FOOBAR_gray.bmp";
			InputStream in = new FileInputStream(file);
			bmp = BmpReader.read_bmp(in);
			
			bmp = lowPassFilter(bmp, 3);

			createImages(bmp, filestring);
		}
	}
	
	private static void createImages(BmpImage bmp, String filestring) throws IOException {
		String outFilename = null;

		outFilename = "pics/u6/" + filestring + "_FOOBAR_lowpassfilter_3.bmp";
		OutputStream out = new FileOutputStream(outFilename);

		try {
			BmpWriter.write_bmp(out, bmp);
		} finally {
			out.close();
		}
	}

	private static BmpImage lowPassFilter(BmpImage bmp, int kernelSize) {
		if (kernelSize % 2 == 0) throw new Error("Kernel size can't be even");
		for (int y = 0; y < bmp.image.getHeight(); y++) {
			for (int x = 0; x < bmp.image.getWidth(); x++) {
				PixelColor[][] kernel = new PixelColor[kernelSize][kernelSize];
				
				for(int i = 0; i < kernelSize; i ++) {
					for (int j = 0; j < kernelSize; j++) {
						int targetX = -(kernelSize /  2) + i;
						int targetY = -(kernelSize / 2) + i;
						if (x+targetX < 0 || y+targetY < 0 || y+targetY >= bmp.image.getHeight() || x+targetX >= bmp.image.getWidth()) {
							kernel[i][j] = null;
							continue; 
						}
						kernel[i][j] = bmp.image.getRgbPixel(x+targetX, y+targetY);
					}
				}
				PixelColor px = calculatePxFromKernel(kernel);
				bmp.image.setRgbPixel(x, y, px);
			}
		}
		return bmp;
	}

	private static PixelColor calculatePxFromKernel(PixelColor[][] kernel) {
		PixelColor px = new PixelColor(0, 0, 0);
		int notInImage = 0;
		for (PixelColor[] row : kernel) {
			for (PixelColor column : row) {
				if (column == null) notInImage++;
				px.r += column != null? column.r : 0;
				px.g += column != null? column.g : 0;
				px.b += column != null? column.b : 0;
			}
		}
		px.r /= kernel.length * kernel.length - notInImage;
		px.g /= kernel.length * kernel.length - notInImage;
		px.b /= kernel.length * kernel.length - notInImage;
		
		return px;
	}

	private static void printHistogramOfY (BmpImage bmp, String filestring) throws FileNotFoundException {
		int[] values = new int[256];
		PrintWriter writer = new PrintWriter("files/u4/" + filestring + "_Y_Histogramm.txt");
		for (int y = 0; y < bmp.image.getHeight(); y++) {
			for (int x = 0; x < bmp.image.getWidth(); x++) {
				PixelColor px = bmp.image.getRgbPixel(x, y);
				values[px.r] += 1;
			}
		}
		for (int i = 0; i < values.length; i++) {
			writer.println(i + ":" + values[i]);
		}
		writer.close();
	}

	private static BmpImage toYCbCr(BmpImage bmp) {
		for (int y = 0; y < bmp.image.getHeight(); y++) {
			for (int x = 0; x < bmp.image.getWidth(); x++) {
				PixelColor px = bmp.image.getRgbPixel(x, y);
				int Y = (int) (px.r * 0.299 + px.g * 0.587 + px.b * 0.114);
				int Cb = (int) (px.r * -0.169 + px.g * -0.331 + px.b * 0.5) + 128;
				int Cr = (int) (px.r * 0.5 + px.g * -0.419 + px.b * -0.081) + 128;
				px.r = Math.max(Math.min(Y, 255), 0);
				px.g = Math.max(Math.min(Cb, 255), 0);
				px.b = Math.max(Math.min(Cr, 255), 0);
			}
		}
		return bmp;
	}

	private static BmpImage toRGB(BmpImage bmp, String channel) {
		for (int y = 0; y < bmp.image.getHeight(); y++) {
			for (int x = 0; x < bmp.image.getWidth(); x++) {
				PixelColor px = bmp.image.getRgbPixel(x, y);
				int newR = (int) (px.r + (px.b - 128) * 1.403);
				int newG = (int) (px.r + (px.g - 128) * -0.344 + (px.b - 128) * -0.714);
				int newB = (int) (px.r + (px.g - 128) * 1.773);
				px.r = Math.max(Math.min(newR, 255), 0);
				px.g = Math.max(Math.min(newG, 255), 0);
				px.b = Math.max(Math.min(newB, 255), 0);
			}
		}
		return bmp;
	}
	private static BmpImage toGrayChannel(BmpImage bmp) {
		for (int y = 0; y < bmp.image.getHeight(); y++) {
			for (int x = 0; x < bmp.image.getWidth(); x++) {
				PixelColor px = bmp.image.getRgbPixel(x, y);
				int Y = (int) (px.r * 0.3 + px.g * 0.6 + px.b * 0.1);
				px.r = Y;
				px.g = Y;
				px.b = Y;
			}
		}
		return bmp;
	}

	private static BmpImage toRGBImage(BmpImage bmp, String channel) {
		for (int y = 0; y < bmp.image.getHeight(); y++) {
			for (int x = 0; x < bmp.image.getWidth(); x++) {
				PixelColor px = bmp.image.getRgbPixel(x, y);
				int newR = 0;
				int newG = 0;
				int newB = 0;
				switch (channel) {
					case "Y":
						newR = px.r;
						newG = 128;
						newB = 128;
						break;
					case "Cb":
						newR = 128;
						newG = px.g;
						newB = 128;
						break;
					case "Cr":
						newR = 128;
						newG = 128;
						newB = px.b;
						break;
					default:
						throw new RuntimeException("Channel name " + channel + " not valid");
				}
				px.r = (int) (newR + (newB - 128) * 1.403);
				px.g = (int) (newR + (newG - 128) * -0.344 + (newB - 128) * -0.714);
				px.b = (int) (newR + (newG - 128) * 1.773);
				px.r = Math.max(Math.min(px.r, 255), 0);
				px.g = Math.max(Math.min(px.g, 255), 0);
				px.b = Math.max(Math.min(px.b, 255), 0);
			}
		}
		return bmp;
	}

	private static BmpImage toYCbCrChannel(BmpImage bmp, String channel) {
		for (int y = 0; y < bmp.image.getHeight(); y++) {
			for (int x = 0; x < bmp.image.getWidth(); x++) {
				PixelColor px = bmp.image.getRgbPixel(x, y);
				int Y = (int) (px.r * 0.299 + px.g * 0.587 + px.b * 0.114);
				int Cb = (int) (px.r * -0.169 + px.g * -0.331 + px.b * 0.5) + 128;
				int Cr = (int) (px.r * 0.5 + px.g * -0.419 + px.b * -0.081) + 128;
				int newR = Math.max(Math.min(Y, 255), 0);
				int newG = Math.max(Math.min(Cb, 255), 0);
				int newB = Math.max(Math.min(Cr, 255), 0);
				switch (channel) {
					case "Y":
						px.r = newR;
						px.g = 128;
						px.b = 128;
						break;
					case "Cb":
						px.r = 128;
						px.g = newG;
						px.b = 128;
						break;
					case "Cr":
						px.r = 128;
						px.g = 128;
						px.b = newB;
						break;
					default:
						throw new RuntimeException("Channel name " + channel + " not valid");
				}
			}
		}
		return bmp;
	}

	private static BmpImage oneColorTone(BmpImage bmp) {
		for (int y = 0; y < bmp.image.getHeight(); y++) {
			for (int x = 0; x < bmp.image.getWidth(); x++) {
				PixelColor px = bmp.image.getRgbPixel(x, y);
				px.r = 0;
				px.g = 0;
				// px.b = 0;
				bmp.image.setRgbPixel(x, y, px);
			}
		}
		return bmp;
	}
}
