# LinearDistance
Plugin for ImageJ to measure linear distances for binary images in X and Y Direction.
It scans an image linewise (with a gives skip factor) in both directions and finds stripes of only black or white lines and calculates the mean length of these stripes. 

# Application
Used for the calcuation of mean free paths in a two phase material.

# Dependecies
ImageJ (tested with ImageJ 1.50e)
Apache Commons Math (commons-math3-3.5 included)

# Installation
Copy the File LinearDistance_.jar in the plugins folder. 

# Usage
Open one or more binary (black/white) images or binarize an image.
Start the plugin and select the results you want to obtain. The plugin can than be called by selecting "Plugins>Analyze>Measure linear distances"

#Available Options:
```
-Distance between measures      : the number of pixels to skip between the analyzed lines
-Measure all opened images      : When not selected, only the active image will be analyzed
-Standard Deviations            : Print standard deviations for all measurements
-Numbers                        : Print the number of counted stripes
-White/Black                    : Counts only white / black stripes
-Both Phases                    : Calculates also the mean of both (all) Phases (Black and White)
-X / Y                          : Counts only in X / Y or both directions
-Both Directions                : Calculates also the mean of both Directions
-Both Directions and Phases     : Calculates also the mean of both Phases and both Directions (All Stripes)
```
