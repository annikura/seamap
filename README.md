# SeaMap

GUI naval records visualiser supporting longtitude/latitude and Kriegsmarine Marinequadratkarte coordinate systems.

### Features

* Export/import of journals represented as CSV file.
* Manual records creation.
* Weather records visualisation.
* Map offline cashing.
* Adding images to the map.

## Usage manual

Java8 is required for correct execution. Can be run via maven from console:

~~~
mvn clean package
java -jar target/seamap-1.0-SNAPSHOT-jar-with-dependencies.jar
~~~

Once application started, you will see the following:

%

Go to the Journal pane on the top.

%

Here we can either add a new record manually, using the form on the left or upload a
CSV file containing the records by hitting 'Add from CSV' button (file format is described in the Journal sections below).

%

Let's return to the Map tab and upload data from table by hitting 'Reload data from table' button in the Content settings section.

%

Controls on the left can help to filter out not required information.

%

Click on any marker will open an info panel giving more information about the marker. It aldo contains a delete button which will remove marker from both map and table pane.

%

Let's go to the image controls. Note that while it's open map area is frozen.
Let's  upload an image using 'Upload new image' button
and put it on the proper place using the controls on the left.

%

Now let's move back to Content settings panel. An image will disappear once we switch from Image controls pane.
If you want it to overlay the map, switch on 'Show images layer'.

%

You can also upload weather in the Weather tab.

%

Back in the Map tap you can turn on 'Show relevant wind directions' option.
Colour of the arrow shows the strength of the wind (from green to red).

%

That's it! 

### Additional hints

* Data in any table can be sorted by column by clicking on the corresponding column header.
* If you hover over the visible path will show an approximate time when the ship was in this point calculated linearly.
* You can switch OpenStreetMap layout to Bing maps in MapSettings panel in the Map tab.
Once bing API key is entered, switch toggle will unlock.
* Scrolling on the image while in image controls panel will resize image with ration preserved.
* Marker info panel can also be closed with ESC.
* You can select map area with ctrl held to go to the selected area.
* Double click on map zooms it in.


### Journal records

### Weather records