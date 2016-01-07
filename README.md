## IronBeast-Android

- [Installation](#installation)
- [Getting Started](#getting-started)

#### Installation
Currentlly, there is 1 way to integrate.  
soon, it will be available on jcenter and Github as well.  

**1.** Add the SDK [jar](http://link-to-jar) into `libs` directory.

**2.** Add the following line to `AndroidManifest.xml`

```
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

```
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

Make sure you that have replaced `"YOUR_API_TOKEN"` with your IronBeast api token,  
and `"TABLE_TO_TRACK_INTO"` to the desired destination (e.g: "cluster.schema.table")

