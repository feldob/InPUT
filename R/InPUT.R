#  
#
# Author: Felix Dobslaw
###################################################

getNumericValue<-function(inputNode) {
	return(as.numeric(getValue(inputNode)))
}

getValue<-function(inputNode) {
	return(xmlAttrs(inputNode)[c("value")][[1]])
}

getElements<-function(input, elementId){
	query <- paste("//*[@id='",elementId, "']", sep = "")
	elements <- getNodeSet(input, query)[[1]]
	return (getNumericValues(elements))
}

getValues<-function(inputNode){
	output <- rep(NA, xmlSize(inputNode))
	for (i in 1:xmlSize(inputNode)) {
		output[i] <- getValue(inputNode[[i]])
	}
	return(output)
}

getDesignSpace<-function(fileName){
	return(xmlParse(fileName))
}

getDesign<-function(fileName){
	return(xmlParse(fileName))
}

getNumericValues<-function(inputNode){
	output <- NULL
	for (i in 1:xmlSize(inputNode)) {
		node <- inputNode[[i]]
		value <- getNumericValue(node)
		if (xmlSize(node) > 0) {
			output <- cbind(output, getNumericValues(xmlChildren(node)))
		}else{
			output <- rbind(output,value)
		}
	}
	return(as.numeric(output))
}

getInPUT<-function(archiveName){
	return(xmlParse(archiveName))
}

getXMLFileInArchive<-function(archivePath, fileName){
	conn <- unz(archivePath, fileName)
	on.exit(close(conn), add = TRUE)
	file <- xmlParse(readLines(conn))
	return(file)
}

getNumericStatistics<-function(archive, xmlFile){
	results = getXMLFileInArchive(archive, xmlFile)
	resultsXML <- xmlRoot(results)[[1]]
	return(getNumericValues(resultsXML))
}