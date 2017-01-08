# PointAnalysis
Plugin for ImageJ to measure domains (e.g. phases) in images using a grid of points.

# Application
Plugins -> Analyze -> Point Analysis (multiple domains)

# Dependecies
* [ImageJ](http://rsb.info.nih.gov/ij/) (tested with ImageJ 1.50e)

# Installation
Copy the File **PointAnalysis_.jar** in the plugins/jars folder. 
When using Fiji, you can simply add the update site **SimonKlein**.

# Point Analysis (multiple domains)
Open one image. Start the plugin by selecting "Plugins>Analyze>Point Analysis (multiple domains)" and enter the number of domains you want to measure and the number of points to seed. Optionally, the points can be placed randomly instead of a grid. 

Now you can try to automatically assign the points by entering a threshold between 0 and 255 and a porperty to match (H=Hue, S=Saturation, B=Brightness). This maybe only useful for two domains. By clicking single points, you can change the assigned domain (left=circle, right=reverse). When clicking OK, the number of points for each domain is printed.
