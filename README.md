# Can I Park Here?
Android Application that checks if location is risky to park in based on historical data from NYC Open Data (https://nycopendata.socrata.com).

https://data.cityofnewyork.us/City-Government/Parking-Violations-Issued-Fiscal-Year-2016/kiv2-tbus

Based on the Parking Violations Issued in 2016 (updating in real-time), we provide estimates of likeliness of parking infractions. We use google maps API to find user location and specific GPS Coordinates. Using the spot selected by the user, we query the nearby parking infractions of the closest street, weighted based on distance and relative frequency.

<img src="http://i.imgur.com/FKAzZiz.png" width="260" /> 
<img src="http://i.imgur.com/qhskVxA.png" width="260" />
<img src="http://i.imgur.com/VGGw4Fw.png" width="260" />
<img src="http://i.imgur.com/GLZYCU0.png" width="785" />

Developed at HackNY by Ryan Lim and Ayoub Sbai.
