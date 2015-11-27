# LinearDistance
Plugin for ImageJ to measure linear distances in binary images in X and Y direction.
It scans an image linewise (with a given skip distance) in both directions and finds stripes of only black or white and calculates the mean length of these stripes. 

# Application
Used for the calcuation of mean free paths in a two phase material.

# Dependecies
* [ImageJ](http://rsb.info.nih.gov/ij/) (tested with ImageJ 1.50e)
* [Apache Commons Math](https://commons.apache.org/proper/commons-math/) (tested with commons-math3-3.5, included in the .jar file)

# Installation
Copy the File **LinearDistance_.jar** in the plugins/jars folder. 

# Usage
Open one or more binary (black/white) images or binarize an image. Start the plugin by selecting "Plugins>Analyze>Measure linear distances" and select the results you want to obtain. ~~**All given lengths will be in pixels**, no scale is applied~~.

#Available Options:
Option                         |  Description
-------------------------------|----------------------------------------
Apply image calibration        | Applies the set scale of each image to the results (by multiplying it)
Step distance between measures | The number of pixels to skip between the analyzed lines
Calibrate step distance        | Skip units instead of pixels
Measure all opened images      | When not selected, only the active image will be analyzed
Standard Deviations            | Print standard deviations for all measurements
Numbers                        | Print the number of counted stripes
Both Phases                    | Calculates also the mean of both (all) Phases (Black and White)

