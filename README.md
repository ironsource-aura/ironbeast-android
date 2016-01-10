## IronBeast-Android

- [Installation](#installation)
- [Getting Started](#getting-started)
- [Documentation](#documentation)

#### Installation
Currently, there is one way to integrate.
soon, it will be available on jcenter and Github as well.  

**1.** Add the SDK [jar](https://drive.google.com/folderview?id=0BzvglFf2CT9kY0FzZ1J1SzZoelk&usp=sharing)
into `libs` directory.

**2.** Add the following line to `AndroidManifest.xml`

```xml
<service android:name="io.ironbeast.sdk.ReportService" />
```

**3.** Add dependency to app/build.gradle

```
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
}
```

#### Getting Started
Add IronBeast to your main activity. for example:

```java
...
import io.ironbeast.sdk.IronBeast;
import io.ironbeast.sdk.IronBeastTracker;
...
...
@Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        // Configure IronBeast
        IronBeast ironBeast = IronBeast.getInstance(this);
        ironBeast.setAllowedNetworkTypes(IronBeast.NETWORK_MOBILE | IronBeast.NETWORK_WIFI);
        ironBeast.setAllowedOverRoaming(true);
        // Create and config IronBeastTracker
        IronBeastTracker tracker = ironBeast.newTracker("YOUR_API_TOKEN");
        try {
            JSONObject events = new JSONObject();
            events.put("action", "click on ...");
            events.put("user_id", user.id);
            tracker.track("TABLE_TO_TRACK_INTO", event);
        } catch (JSONException e) {
            ...
        }
        ...
    }
```

Make sure you have replaced `"YOUR_API_TOKEN"` with your IronBeast api token,
and `"TABLE_TO_TRACK_INTO"` to the desired destination (e.g: "cluster.schema.table")



#### Documentation - TODO
- track string, map and json.
- flush (control interval size, bulk and byte size, flush manually, etc..)
- how to track events immediately ?
- set whether the SDK can keep sending over a roaming connection.
- restrict the types of networks over which this SDK can keep making HTTP requests.
- enabling IronBeast error tracker.
- set custom endpoint.

