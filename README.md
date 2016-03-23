# LinearDistance
Plugin for ImageJ to measure linear distances in images in X and Y direction.
At this time, two methods are implemented: First, the automatic scan of a binarized, two phased image. Second, the interactive placement of marks which will be measured afterwards (Linear Interception).

# Application
Measure linear distances (two phases, 8 bit): Used for the calculation of mean free paths in a two phase material.
Measure linear distances (single phase, interactive): Used for the measurement of grain size in single phase metal.

# Dependecies
* [ImageJ](http://rsb.info.nih.gov/ij/) (tested with ImageJ 1.50e)

# Installation
Copy the File **LinearDistance_.jar** in the plugins/jars folder. 
When using Fiji, you can simply add the update site **LinearDistance**.

# Measure linear distances (two phases, 8 bit)
Open one or more binary (black/white) images or binarize an image. Start the plugin by selecting "Plugins>Analyze>Measure linear distances (two phases, 8 bit)" and select the results you want to obtain.

**Available Options:**

Option                         |  Description
-------------------------------|----------------------------------------
Apply image calibration        | Applies the set scale of each image to the results (by multiplying it)
Step distance between measures | The number of pixels/units to skip between the analyzed lines
Calibrate step distance        | Skip units instead of pixels
Measure all opened images      | When not selected, only the active image will be analyzed
Standard Deviations            | Print standard deviations for all measurements
Numbers                        | Print the number of counted stripes
Both Phases                    | Calculates also the mean of both (all) Phases (Black and White)

# Measure linear distances (single phase, interactive)
Open any kind of image. Start the plugin by selecting "Plugins>Analyze>Measure linear distances (single phase, interactive)". Set the distance and offset you prefer in either pixels or units (if calibrated). Set marks in the image by left click, remove marks with right click. When done, press OK at the bottom of the image and select the results you want to obtain. The selected statistics for the stripe-length between marks will be presented afterwards.
