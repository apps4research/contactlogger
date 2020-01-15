# README

This software was programmed and developed by Tina Keil, as part of a Ph.D. Research project 
supervised by Dr. Miriam Koschate-Reis and Prof. Mark Levine, Department of Psychology, College 
of Life and Environmental Sciences, University of Exeter. The project was funded by the 
University of Exeter (exeter.ac.uk) and the UK Engineering and Physical Sciences Research 
Council (epsrc.ac.uk).

It can be used as a tool to collect intergroup contact data in near-time and <i>in situ</i>. 
Participants were asked to log each contact they had with a specific outgroup member directly 
after the contact occurred and while still in the same location.

### Quick summary
* Version 2.0 of Contact Logger
* Includes bug fixes and improvements after Usability Study
* Target SDK Platform: API Level 23, but aimed for 22

## Basic setup / configuration

### Configuration of the map functionality
- **Google Maps**: For map function to work at all you must generate and enter your own API key for 
the "Maps SDK for Android" service, see https://developers.google.com/maps/documentation/android-sdk/signup
- **Other tile providers**: All other tile providers (Mapbox and Openstreet) are provided via the 
Mapbox API. For this you must generate and enter your own "mapbox_id" and "mapbox_token". Create 
an account with Mapbox and setup an access token, see https://www.mapbox.com/help/how-access-tokens-work/
- **Offline map**: Offline maps must be in mbtiles format. There are many open-source tools 
available to generate your own maps, e.g., https://openmaptiles.org/ or http://maperitive.net/. 
The mbtiles file can be either copied per USB to the phone or downloaded from a server and then 
imported via the app's administration menu. The current download url for a map covering most of 
Devon (UK), can be found in the strings.xml under the name "default_mapurl".
- API keys for Google Maps and Mapbox, as well as the password for the admin area are now 
outsourced to **keys.xml** in the **app/src/main/res/values**.
Please edit keys.xml with the following schema:
````xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="google_api_key">REPLACE WITH OWN KEY</string>
    <string name="mapbox_id">REPLACE WITH OWN ID</string>
    <string name="mapbox_token">REPLACE WITH OWN TOKEN</string>
    <string name="admin_pwd">REPLACE WITH OWN PASSWORD</string>
</resources>
````
## Who do I talk to?

* Tina Keil, tina.keil@a4r.eu
* Dr. Miriam Koschate-Reis, M.Koschate-Reis@exeter.ac.uk

## Licence
© 2016, Tina Keil and Dr. Miriam Koschate-Reis, BSD-3-Clause
