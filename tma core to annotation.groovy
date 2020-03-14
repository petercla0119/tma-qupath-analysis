def annotations = getTMACoreList().collect {
    def annotation = new qupath.lib.objects.PathAnnotationObject(it.getROI())
    annotation.setName(it.getName())
    return annotation
}
clearAllObjects()
addObjects(annotations)
fireHierarchyUpdate()