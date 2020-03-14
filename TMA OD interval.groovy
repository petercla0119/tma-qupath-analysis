def interval = 0.05;
def minTh2 = 0.1;
def maxTh2 = 0.25;

cores_list = getSelectedObjects();

print(String.format("Selected objetcs: %s", cores_list.asList()))

for (int i = 0; i < Math.round((maxTh2 - minTh2)/interval + 1); i++){
    th2 = minTh2 + i * interval;
    def str = String.format('{"downsampleFactor": 2,  "gaussianSigmaMicrons": 1.0,  "thresholdStain1": 0.085,  "thresholdStain2": %f,"addSummaryMeasurements": true, "clearParentMeasurements": true,  "appendDetectionParameters": true,  "legacyMeasurements0.1.2": false}', th2); //,"clearParentMeasurements": false}', th2);
    runPlugin('qupath.imagej.detect.tissue.PositivePixelCounterIJ', str);
    
    saveDetectionMeasurements('U:/FTDC-IrwinLab/TMA Take 2' + getProjectEntry().getImageName() + '_thresholdStain2=' + Float.toString(th2) + '_detections.txt');
}

area = getDetectionObjects().findAll {it.getROI().getRoiName().contains("Area") == true};
removeObjects(area, false)