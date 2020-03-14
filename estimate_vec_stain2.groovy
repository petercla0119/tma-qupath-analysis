import java.awt.image.BufferedImage;

import qupath.lib.algorithms.color.EstimateStainVectors;
import qupath.lib.color.ColorDeconvolutionHelper;
import qupath.lib.color.ColorDeconvolutionStains;
import qupath.lib.color.StainVector;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.ColorToolsFX;
import qupath.lib.gui.helpers.DisplayHelpers;
import qupath.lib.gui.helpers.DisplayHelpers.DialogButton;
import qupath.lib.gui.helpers.dialogs.ParameterPanelFX;
import qupath.lib.gui.plots.ScatterPlot;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.plugins.parameters.ParameterList;
import qupath.lib.regions.RegionRequest;
import qupath.lib.roi.RectangleROI;
import qupath.lib.gui.scripting.QPEx
import qupath.lib.roi.interfaces.ROI;

ImageData<BufferedImage> imageData = QPEx.getCurrentImageData();
if (imageData == null || !imageData.isBrightfield() || imageData.getServer() == null || !imageData.getServer().isRGB()) {
    DisplayHelpers.showErrorMessage("Estimate stain vectors", "No brightfield, RGB image selected!");
    return;
}
ColorDeconvolutionStains stains = imageData.getColorDeconvolutionStains();
if (stains == null || !stains.getStain(3).isResidual()) {
    DisplayHelpers.showErrorMessage("Estimate stain vectors", "Sorry, stain editing is only possible for brightfield, RGB images with 2 stains");
    return;
}

///////Uncomment(comment) this setion if you (don't) want to select objects manually///////
/*cores_list = getSelectedObjects();
cores_name = [];
print(String.format("Selected objetcs: %s", cores_list.asList()))

for (int i = 0; i < cores_list.size(); i++)
    cores_name.add(cores_list[i].getDisplayedName().toString())*/
//////////////////////////////////////////////////////////////////////////  
    
///////Uncomment(comment) this setion if you (don't) want to select objects with a list///////   
cores_name = ['A-2','C-6','F-1']; //change this list to your will

selectObjects { p -> cores_name.contains(p.getDisplayedName().toString()) == true };
cores_list = getSelectedObjects();
print(String.format("Selected objetcs: %s", cores_list.asList()))
//////////////////////////////////////////////////////////////////////////////////////////////
    
def outputFile = String.format('C:/Users/vpisk/Desktop/TMA script/detections/%s_%sestimate_stain.txt', getProjectEntry().getImageName(), cores_list.asList()); //change the path if needed
def fileResults = new File(outputFile);
String delimiter = '\t';
    
def results = new ArrayList<Map<String, String>>()
def allColumns = new LinkedHashSet<String>()

def columns = ['Core', 'Hematoxylin', 'DAB', 'Residual']
allColumns.addAll(columns)    

for (int i = 0; i < cores_list.size(); i++){
	
    PathObject pathObject = cores_list[i];
    ROI roi = pathObject == null ? null : pathObject.getROI();
    if (roi == null)
        roi = new RectangleROI(0, 0, imageData.getServer().getWidth(), imageData.getServer().getHeight());
    
    int MAX_PIXELS = 4000*4000;		
    double downsample = Math.max(1, Math.sqrt((roi.getBoundsWidth() * roi.getBoundsHeight()) / MAX_PIXELS));
    RegionRequest request = RegionRequest.createInstance(imageData.getServerPath(), downsample, roi);
    BufferedImage img = imageData.getServer().readBufferedImage(request);
    		
    // Apply small amount of smoothing to reduce compression artefacts
    img = EstimateStainVectors.smoothImage(img);
    		
    // Check modes for background
    int[] rgb = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
    int[] rgbMode = EstimateStainVectors.getModeRGB(rgb);
    int rMax = rgbMode[0];
    int gMax = rgbMode[1];
    int bMax = rgbMode[2];
    
    double minStain = 0.05;
    double maxStain = 1.0;
    double ignorePercentage = 1.0;
    		
    ColorDeconvolutionStains stain_vec = EstimateStainVectors.estimateStains(img, stains, false)
        
    def hema_vec = stain_vec.getStain(1).toString().split(' ')[1..-1];
    def DAB_vec = stain_vec.getStain(2).toString().split(' ')[1..-1];
    def resi_vec = stain_vec.getStain(3).toString().split(' ')[1..-1];
    
    def map = ['File name': fileResults.getName()];
    map[columns[0]] = cores_name[i];
    map[columns[1]] = hema_vec.asList();
    map[columns[2]] = DAB_vec.asList();
    map[columns[3]] = resi_vec.asList();
    
    results.add(map);
}

fileResults.withPrintWriter {
    def header = String.join(delimiter, allColumns)
    it.println(header)
    // Add each of the results, with blank columns for missing values
    for (result in results) {
        for (column in allColumns) {
            it.print(result.getOrDefault(column, ''))
            it.print(delimiter)
        }
        it.println()
    }
}  

print('Done')