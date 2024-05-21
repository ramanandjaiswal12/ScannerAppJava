import static org.bytedeco.opencv_imgcodecs.cvLoadImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.lept.PIX;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage;
import org.bytedeco.opencv.opencv_core.CvContour;
import org.bytedeco.opencv.opencv_core.CvMemStorage;
import org.bytedeco.opencv.opencv_core.CvPoint;
import org.bytedeco.opencv.opencv_core.CvRect;
import org.bytedeco.opencv.opencv_core.CvScalar;
import org.bytedeco.opencv.opencv_core.CvSeq;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;





public final class ReceiptScannerImpl implements ReceiptScanner{
	private static final String IPL_DEPTH_80 = null;
	private static final String BORDER_DEFAAULT = null;

	@Override
	public String getTextFromReceiptImage(final String receiptFileImagePath) {

	final File receiptImageFile=new File(receiptFileImagePath);
	final String receiptImagePathFile=receiptImageFile.getAbsolutePath(); 
	System.out.println(receiptImagePathFile);

	
	IplImage receiptImage=cvLoadImage(receiptImagePathFile);
	IplImage cannyEdgeImage=applyCannySquareEdgedetectionOnImage(receiptImage, 30);	
	CvSeq largestSquare=findLargestSquareOnCannyDetectedImage(cannyEdgeImage); 
	receiptImage=applyPerspectiveTransformThresholdOnOriginalImage(receiptImage, largestSquare, 30);
	receiptImage=cleanImageSmoothingForOCR(receiptImage);
	final File cleanedReceiptFile=new File(receiptFileImagePath);
	final String cleanedReceiptPathFile=cleanedReceiptFile.getAbsolutePath();
	
	cvSaveImage(cleanedReceiptPathFile, receiptImage);
	System.out.println(cleanedReceiptPathFile);
	cvReleaseImage(cannyEdgeImage);
	cannyEdgeImage = null;
	cvReleaseImage(receiptImage);
	receiptImage = null;
	return getStringFromImage(cleanedReceiptPathFile);
	}
	
	



private void cvSaveImage(String cleanedReceiptPathFile, IplImage receiptImage) throws IOException {
		// TODO Auto-generated method stub
	File dir = new File(receiptImage);
	dir.mkdirs();
	File tmp = new File(dir, cleanedReceiptPathFile);
	tmp.createNewFile();		
	}





private IplImage cvLoadImage(String receiptImagePathFile) {
		// TODO Auto-generated method stub
	BufferedImage img = null;
	IplImage iplImage=null;

	try 
	{
	    img = ImageIO.read(new File(receiptImagePathFile)); // eventually C:\\ImageTest\\pic2.jpg
	    ToIplImage iplConverter = new OpenCVFrameConverter.ToIplImage();
	    Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
	    iplImage = iplConverter.convert(java2dConverter.convert(img));
	} 
	catch (IOException e) 
	{
	    e.printStackTrace();
	}
		return iplImage;
	}


//	private IplImage cvLoadImage(String receiptImagePathFile) {
//		// TODO Auto-generated method stub
//		BufferedImage img =  ImageIO.read(new File(receiptImagePathFile));
//		Object image = IplImage.createBuffer();
//
//		return image;
//	}


	private IplImage downScaleImage(IplImage scrImage,int percent) {
		System.out.println("srcImage- height "+scrImage.height()+", width-"+
				scrImage.width());
		IplImage destImage=cvCreateImage(
				cvSize((scrImage.width()*percent)/100,
				(scrImage.height()*percent)/100),scrImage.depth(),
				scrImage.nChannels());
				
	    cvResize(scrImage, destImage);
	    System.out.println("destimage- height-"+destImage.height()+", width-"+destImage.width());
	return destImage;
	}
	
	private IplImage applyCannySquareEdgedetectionOnImage(IplImage srcImage, int percent) {
		
		IplImage destImage=downScaleImage(srcImage, percent);
		IplImage grayImage=Cv_iplCreateImageHeader(cvGetSize(destImage),IPL_DEPTH_80,1);
		cvCvtColor(destImage, grayImage, CV_BGR2GRAY);
		OpenCVFrameConverter.ToMat converterToMat=new OpenCVFrameConverter.ToMat();
		org.bytedeco.javacv.Frame grayImageFrame=converterToMat.convert(grayImage);
		Mat grayImageMat=converterToMat.convert(grayImageFrame);
		GaussianBlur(grayImageMat, grayImageMat, new Size(5,5),0.0,0.0, BORDER_DEFAAULT);
		destImage=converterToMat.convertToIplImage(grayImageFrame);
		cvErode(destImage, destImage);
		cvDilate(destImage, destImage);
		cvCanny(destImage, destImage, 75.0,200.0);
		File f=new File(System.getProperty("user.home")+File.separator+"receipt-canny-detect.jpeg");
		cvSaveImage(f.getAbsolutePath(), destImage);
		return destImage;
	}
	
	private CvSeq findLargestSquareOnCannyDetectedImage(IplImage cannyEdgeDetectedImage) {

			IplImage foundedContoursImage=cvCloneImage(cannyEdgeDetectedImage);

			CvMemStorage memory=CvMemStorage.create();

			CvSeq contours=new CvSeq();

			cvFindContours(founded ContoursImage, memory, contours,

			Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN APPROX_SIMPLE, cvPoint(0, 0));

			int maxWidth = 0;

			int maxHeight = 0;

			CvRect contour = null;

			CvSeq seqFounded=null;
			CvSeq nextSea new CvSeq();
			for (nextSeq=contours; nextSeq!=null; nextSeq=nextSeq.h_next()) {


			contour=cvBoundingRect(nextSeq, 0);

			if ((contour.width() >= maxWidth)

			&& (contour.height()> maxHeight)) {

			maxWidth=contour.width();

			maxHeight=contour.height();

			seqFounded=nextSeq;
			}
			}

			CvSeq result=cvApproxPoly(seqFounded, Loader.sizeof(CvContour.class), memory, CV_POLY_APPROX_DP, 
			cvContourPerimeter(seqFounded)*0.02, 0);

			for (int i=0; i < result.total(); i++) {

			CvPoint v=new CvPoint(cvGetSeqElem(result, i));

			cvDrawCircle(foundedContoursImage, v, 5, CvScalar.BLUE, 20, 8, 0);

			System.out.println("found point(" + v.x() + "," + v.y() + ")");
			}

			File f=new File(System.getProperty("user.home") + File.separator+"receipt-find-contours.jpeg");
			cvSaveImage(f.getAbsolutePath(),foundedContoursImage);
			return result;
	}
	
	private IplImage applyPerspectiveTransformThresholdOnOriginalImage(
			IplImage srcImage, CvSeq contour, int percent) {
			IplImage warpImage cvCloneImage(srcImage);
			for (int i=0; i < contour.total(); i++) {
			point.x((int) (point.x()*100) / percent); 
			point.y((int) (point.y()*100) / percent);

			CvPoint topRightPoint=new CvPoint(cvGetSeqElem (contour, 0)); 
			CvPoint topleftPoint=new CvPoint(cvGetSeqElem(contour, 1));

			CvPoint bottomLeftPoint = new CvPoint(cvGetSeqElem(contour, 2)); 
			CvPoint bottomRightPoint=new CvPoint(cvGetSeqElem(contour, 3));

			int resultWidth=(int)(topRightPoint.x()-topLeftPoint.x()); 
			int bottom Width=(int)(bottomRightPoint.x()-bottomLeftPoint.x());

			if(bottomWidth > resultWidth)
              resultWidth=bottomWidth;
			
			int resultHeight=(int) (bottomLeftPoint.y()-topLeftPoint.y());

			int bottomHeight =(int) (bottomRightPoint.y()-topRightPoint.y());
			if(bottomHeight > resultHeight)
			 resultHeight=bottomHeight;
			resultWidth, resultHeight };
			float[] sourcePoints ={ topLeftPoint.x(), topLeftPoint.y(), 
					topRightPoint.x(), topRightPoint.y(), bottomLeftPoint.x(), 
					bottomLeftPoint.y(), bottomRightPoint.x(), bottomRightPoint.y()};

			float[] destinationPoints={0, 0, resultWidth, 0, 0, resultHeight,
					resultWidth,resultHeight};

//			CyMet 
			homography = cvCreateMat(3, 3, CV_32FC1); 
			cvGetPerspectiveTransform(sourcePoints, destinationPoints, homography);
			System.out.println(homography.toString());
			IplImage destimage=cvCloneImage(warpImage);
			cvWarpPerspective(warpImage, dest Image, homography, CV_INTER_LINEAR, CvScalar.ZERO);
           
			return cropImage (destImage, 0, 0, resultWidth, resultHeight);
	
	}
	
	private IplImage cleanImageSmoothingForOCR(IplImage srcImage) {

		IplImage destImage = cvCreateImage(cvGetSize(srcImage), IPL_DEPTH_8U, 1);
		cvCvtColor(srcImage, destImage, CV_BGR2GRAY);
		cvSmooth(destImage, destImage, CV_MEDIAN, 3, 0, 0, 0);
		cvThreshold(destImage, dest Image, 0, 255, CV_THRESH_OTSU);
		return destImage;

		}
	
	private String getStringFromImage(final String pathToReceiptImageFile) {
		try {

		final URL tessDataResource = getClass().getResource("/");
		Final File tessFolder = new File(tessDataResource.toURI());
		final String tessFolderPath = tessFolder.getAbsolutePath();
		System.out.println(tessFolderPath);

		BytePointer outText;

		TessBaseAPI api = new TessBaseAPI();

		api.SetVariable("tessedit_char_whitelist", "0123456789,/ABCDEFGHJKLMNPQRSTUVWXY");
    	if (api.Init(tessFolderPath, "spa") != 0) {

		System.err.println("Could not initialize tesseract.");

		}



		PIX image=pixRead(pathToReceiptImageFile);
		api.SetImage(image);
		outText api.GetUTF8Text();
		String string=outText.getString();
		api.End();
		outText.deallocate();
		pixDestray(image);
		return string;
		} catch (Exception e) {
          e.printStackTrace();
          return null;
		}
	}
	
	
	
		
	

}
