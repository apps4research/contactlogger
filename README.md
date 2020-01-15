# README

This software was programmed and developed by Tina Keil, as part of a Ph.D. Research project supervised by Dr. Miriam Koschate-Reis and Prof. Mark Levine, Department of Psychology, College of Life and Environmental Sciences, University of Exeter. The project was funded by the University of Exeter (exeter.ac.uk) and the UK Engineering and Physical Sciences Research Council (epsrc.ac.uk).

It can be used as a tool to collect intergroup contact data in near-time and <i>in situ</i>. Participants were asked to log each contact they had with a specific outgroup member directly after the contact occurred and while still in the same location.

## What is this repository for?
* Historical reference

### Quick summary
* Version 1.0 (Nov. 2015) of Contact Logger used in Usability study
* Target SDK Platform: Android 5.1 (Lollipop), API Level 22
* **Please note:** This version, although functional, has a number of bugs and should not be used for research purposes.

## Basic setup / configuration

### Configuration of the map functionality
- **Google Maps**: For map function to work at all you must generate and enter your own API key in the AndroidManifest.xml for the "Maps SDK for Android" service, see https://developers.google.com/maps/documentation/android-sdk/signup
- **Other tile providers**: All other tile providers (Mapbox and Openstreet) are provided via the Mapbox API. For this you must generate and enter your own "mapbox_id" and "mapbox_token" in the strings.xml in the res/values folder. Create an account with Mapbox and setup an access token, see https://www.mapbox.com/help/how-access-tokens-work/
- **Offline map**: Offline maps must be in mbtiles format. There are many open-source tools available to generate your own maps, e.g., https://openmaptiles.org/ or http://maperitive.net/. The mbtiles file can be either copied per USB to the phone or downloaded from a server and then imported via the app's administration menu. The default download url for a map can set in the strings.xml under the name "default_mapurl".
- **Password for app's administration area**: The password is hardcoded and can be found/changed in the LoginDialog.java in the main app dialog folder. The default password is "password".

## Who do I talk to?

* Tina Keil, tina.keil@a4r.eu
* Dr. Miriam Koschate-Reis, M.Koschate-Reis@exeter.ac.uk

## Licence
© 2015, Tina Keil and Dr. Miriam Koschate-Reis, BSD-3-Clause
