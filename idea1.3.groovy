def interval = 0.005;
def minTh2 = 0.1;
def maxTh2 = 0.25;

cores_list = getSelectedObjects();
cores_name = [];

for (int i = 0; i < cores_list.size(); i++)
    cores_name.add(cores_list[i].getDisplayedName().toString())

print(String.format("Selected objetcs: %s", cores_list.asList()))

for (int i = 0; i < Math.round((maxTh2 - minTh2)/interval + 1); i++){
    th2 = minTh2 + i * interval;
    def str = String.format('{"downsampleFactor": 2,"gaussianSigmaMicrons": 1,"thresholdStain1": 0.1,"thresholdStain2": %f,"addSummaryMeasurements": true,"appendDetectionParameters": true,"clearParentMeasurements": false}', th2); //,"clearParentMeasurements": false}', th2);
    runPlugin('qupath.imagej.detect.tissue.PositivePixelCounterIJ', str);
    
    //def detefile = 'U:/FTDC-IrwinLab/TMA Take 2/' + getProjectEntry().getImageName() + '_thresholdStain2=' + Float.toString(th2) + '_detections.txt'
    //def annofile = 'U:/FTDC-IrwinLab/TMA Take 2/' + getProjectEntry().getImageName() + '_annotations.txt'
    //print(annofile.getClass())
    
    //saveDetectionMeasurements(detefile);
    //saveAnnotationMeasurements(annofile);
}

def annoFile = 'U:/FTDC-IrwinLab/TMA Take 2/AT8 TMA results/Run 2 Slide 2/%s_%s_' + getProjectEntry().getImageName() + '_annotations.txt'
    
saveAnnotationMeasurements(annoFile);

area = getDetectionObjects().findAll {it.getROI().getRoiName().contains("Area") == true};
removeObjects(area, false)

def outputFile = String.format('U:/FTDC-IrwinLab/TMA Take 2/AT8 TMA results/Run 2 Slide 2/%s_%sannotations_permutation_2_0.005_inter_AVG.txt', getProjectEntry().getImageName(), cores_list.asList());

def file = new File(annoFile);
def fileResults = new File(outputFile);

def lines = file.readLines();
def iter = lines.iterator();

String delimiter = '\t';

def results = new ArrayList<Map<String, String>>()
def allColumns = new LinkedHashSet<String>()

def columns = iter.next().split(delimiter)
allColumns.addAll(columns)

// Create the entries
while (iter.hasNext()) {
    def line = iter.next();
    def map = ['File name': file.getName()];
    def values = line.split(delimiter);
    if (values.size() == columns.size() && cores_name.contains(values[0])){
        for (int i = 0; i < columns.size(); i++)
            map[columns[i]] = values[i];
        results.add(map);
    }
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

print('Image analysis complete! .txt file generated and saved to directory')