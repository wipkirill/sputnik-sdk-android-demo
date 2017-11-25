# Sputnik library for Android

Features:
* Cross-platform framework for Android
* [**Offline** search API](https://github.com/urban-labs/sputnik-sdk-android-demo/wiki/Search-guide) for OSM data, customizable queries with lots of settings and fast response time.
* Address decoding functionality.
* [**Offline** routing](https://github.com/urban-labs/sputnik-sdk-android-demo/wiki/Routing-guide) for auto and pedestrian mode. 
* High-quality Android native slippy map implementation gives fast offline access to cached maps on your device.

The location-aware platform provides a complete solution for your mobile application. Our goal is to deliver simple and extensible search, routing and map-rendering API suitable for many mobile applications. In particular, our SDK is ready to use out of the box with minimal configuration on your part.

Pic. 1 | Pic. 2
------------ | -------------
![Im1](https://raw.githubusercontent.com/urban-labs/sputnik-sdk-android-demo/master/images/device-2015-03-09-130106.png) | ![Im2](https://raw.githubusercontent.com/urban-labs/sputnik-sdk-android-demo/master/images//device-2015-03-09-130138.png) |
Pic. 3 | Pic. 4
![Im2](https://raw.githubusercontent.com/urban-labs/sputnik-sdk-android-demo/master/images//device-2015-03-09-130206.png) | ![Im2](https://raw.githubusercontent.com/urban-labs/sputnik-sdk-android-demo/master/images//device-2015-03-09-130251.png)


## Introduction
Check out our Sputnik SDK Demo project to get notion on what you can achieve while using the framework. It comes up with a bunch of predefined maps and a small Android map application for testing. You are free to modify the demo code.

## Initializing the Sputnik in your application
Suppose you have an Android Application class and first you might want to do is some kind of initialization before using any Sputnik functions:
```java
 @Override
 public final void onCreate() {
        super.onCreate(); 
        Log.v("", "Starting Sputnik");
        // call init and use our callback mechanism
        Sputnik.init(getApplicationContext(), new BasicCallback() {
            @Override
            public void done(SputnikException e) {
                if(e == null) {
                    Log.v(TAG, "Started successfully");
                    notifySuccess();
                } else {
                    Log.e(TAG, "Error while starting");
                    notifyError(e.getMessage());
                }
            }
        });
    }
```
Provide two functions `notifySuccess()` and `notifyError()` in order to figure out Sputnik startup status in your application.

### Listing of cached offline maps
First we need to know which maps are cached on device disk drive.
NOTE: If you use our demo for testing, please do download at least one of predefined maps.

Now you can use ```Sputnik.listMaps``` after successful startup step and see what is stored for offline usage.

```java
Sputnik.listMaps(new GetCallback<MapList>() {
            @Override
            public void done(MapList mapList, SputnikException e) {
                if(e == null) {
                    if(mapList.getMaps().size() > 0) {
                        adapter = new RecentMapsAdapter(getView().getContext(),
                                R.layout.recent_map_item, mapList.getMaps());
                        rm.setAdapter(adapter);
                        
                    } 
                } else {
                    progress.setVisibility(LinearLayout.GONE);
                    rm.setEmptyView(noItems);
                }
            }
        });
```
The `mapList` is an array of map file names. Each element represents an independent file containing all data you need. Go for next step to see how to query map features out of it.


### Getting meta infromation from a map
Lets make a simple handler to get map meta data for an element of the `mapList` we got from the previous step.
```java
String mapName = mapList.get(0);
private GetCallback<MapInfo> mapInfoHandler = new GetCallback<MapInfo>() {
    @Override
    public void done(MapInfo mapInfo, SputnikException parseException) {
        try {
            if(mapInfo.hasFeature(SputnikConsts.FEATURE_TILES)) {
                Log.d("", "This map has data for rendering tiles");
            } 
            if(mapInfo.hasFeature(SputnikConsts.FEATURE_SEARCH)) {
                Log.d("", "This map is searchable");
            } 
            if(mapInfo.hasFeature(SputnikConsts.FEATURE_ROUTING)) {
                Log.d("", "This map is routable");
            }
        } catch (Exception e) {
            e.printStackTrace();
            reportError();
        }
    }
Sputnik.getMapInfo(mapName, mapInfoHandler);
```
See API docs for MapInfo class for details.

### Search for OSM tags, objects, names, addresses

Consider a simple scenario when we search for some string value among all objects in a city:
```java
String term = "Snowden ave"
SearchQuery q = Sputnik.getSearchQuery(mapName_);
// set search string to the query
q.searchTerm(term);
// actual search call
q.find(new ListCallback<SearchResult>() {
    @Override
    public void done(List<SearchResult> list, SputnikException e) {
        if(e == null) {
            // refresh listviews or any other containers for results
            clear();
            addAll(list);
            notifyDataSetChanged();
        } else {
            Log.v("[ERROR]", "Error while searching:"+e.getMessage());
        }
    }
});
```
Really simple, isn't it? No lets dive into details of the [search functionality](https://github.com/urban-labs/sputnik-sdk-android-demo/wiki/Search-guide).


### Address decoding
For those of you who is familiar with OpenStreetMap data might be confusing that many objects don't have address tags. One has to figure out a nearest OSM object having full address tags like ```addr:street``` or ```addr:housenumber``` and assume that the original OSM object is somewhere nearby. Look at the following code where we make address decoding for some of ```SearchResult```'s with empty addresses:
```java
// accumulate coordinates of objects without an address
List<LatLon> latlons = new ArrayList<LatLon>();
for (int i = 0; i < getCount(); ++i) {
    if (!TUtil.hasAddress(getItem(i))) {
        indexToUpdate.add(i);
        latlons.add(getItem(i).getCoords());
    }
}
// find nearest objects with address tags
if(latlons.size() > 0)
Sputnik.searchNearest(mapName_, latlons, null, true, new ListCallback<SearchResult>() {
    @Override
    public void done(List<SearchResult> list, SputnikException e) {
        if(e == null) {
            for(int i = 0; i < list.size(); ++i) {
                int id = indexToUpdate.get(i);
                if(id >= 0 && id < getCount()) {
                    SearchResult rOld = getItem(id);
                    SearchResult rNearest = list.get(i);
                    for (String addrKey : TUtil.ADDR_TAGS) {
                        if(rNearest.getTag(addrKey) != null && rOld.getTag(addrKey) == null)
                            rOld.putTagVal(addrKey, rNearest.getTag(addrKey));
                    }
                }
            }
            notifyDataSetChanged();
        } else {
            Log.v("[ERROR]", "Error while fetching addresses: " + e.getMessage());
        }
    }
});
```
In this example we finally extend existing object tags with address tags found by ```Sputnik.searchNearest``` call.

### Searching for OSM tags stored in a map file
For better searching experience it might be useful to retrieve a list of available OSM tags stored in a map file. Assume a user inputs ```sh``` in your search bar. There might be some OSM tags matching the query: ```shop```, ```shoes```, etc. Each map file has a special index of all tags and by using ```Sputnik.matchTags``` API call.

```java
Sputnik.matchTags(mapName_, term, new GetCallback<TagList>() {
            @Override
            public void done(TagList tagList, SputnikException e) {
                if(e == null) {
                    availableTags = tagList.getTags();
                    renderAvailableTags();
                } else {
                    Log.e("[MatchTagContainer]", "Failed to match tags: " + e.getMessage());
                }
            }
        });

```
