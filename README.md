tangible-data
=============

Tangible Interaction Techniques for Multivariate Data Analysis

Instructions:
Tangibles can be used to represent one attribute of the data.

Dust and Magnets
The attributes need to be assigned before you can begin using the fiducials.
The steps to assign are as follows
Note: in order for the system to enter the assign mode, only two fiducials must be present. a) Menu fiducial b) Unassigned fiducial (in the range 0-8)
1. Introduce the menu fiducial (currently, the one with id = 12). A menu will be displayed, with the attribute to be assigned highlighted in red. 
2. Now introduce any unassigned fiducial with id in the range 0-9. The fiducial gets assigned one attribute.
3. The entry of an already assigned menu gets removed, and the next attribute gets highlighted in the menu. 

Moving a magnets attracts dust particles to the magnet. The higher the value of the dust particle in the assigned attribute, the more the particle is attracted to the magnet.

Once the points are loaded on the screen, a special pointing fiducial (curently assigned to id = 31) can be used to know the name of each data point.
	
FLINA (Scatterplots & Parallel Sets)
Attributes do not need to be assigned for the axes used in FLINA.

An axis consists of two aligned fiducials in the id range of 111-119. 

Introducing one axis alignes the dust particles to the axis based on their value for the attribute assigned to that axis. Dust particles aligned to the axis will still respond to a magnet.

If another axis is introduced close to the previous axis at a near-right angle a scatterplot will be created. Other axes can be placed at right angles to chain multiple scatterplots together. Dust particles in a scatterplot will still respond to a magnet.

If another axis is introduced close to another axis in parallel a parallel coordinate plot will be created between them. The lines in the parallel coordinate plot will not respond to magnets, but several axes can be chained together, drawing a line for each record across the full series of axes. 

