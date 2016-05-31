# ironSource.atom SDK for Android
[![License][license-image]][license-url]
[![Docs][docs-image]][docs-url]
[![Coverage Status][coveralls-image]][coveralls-url]
[![Build status][travis-image]][travis-url]


Atom-Android is the official [ironSource.atom](http://www.ironsrc.com/data-flow-management) SDK for the Android.

###Installation

Currently, there is one way to integrate. soon, it will be available on jcenter and Github as well.

1. Add the SDK jar into libs directory.

2. Add the following lines to AndroidManifest.xml
```html
        <service android:name="io.ironsourceatom.sdk.ReportService" />
        <service android:name="io.ironsourceatom.sdk.SimpleReportService" />
```
3. Add dependency to app/build.gradle
```html
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
}
```
###Getting Started

Add IronSourceAtom to your main activity. For example:
```html
...
import io.ironsourceatom.sdk.HttpMethod;
import io.ironsourceatom.sdk.IronSourceAtom;
import io.ironsourceatom.sdk.IronSourceAtomEventSender;

public class BaseMainActivity extends Activity {
    private IronSourceAtom ironSourceAtom;
    private final String STREAM="YOUR_IRONSOURCEATOM_STREAM_NAME";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);

        // Create and config IronSourceAtom instance
        ironSourceAtom = IronSourceAtom.getInstance(this);
        ironSourceAtom.setAllowedNetworkTypes(IronSourceAtom.NETWORK_MOBILE | IronSourceAtom.NETWORK_WIFI);
        ironSourceAtom.setAllowedOverRoaming(true);
        
         String url = "https://track.atom-data.io/";
         IronSourceAtomEventSender sender = ironSourceAtom.newSender("YOUR_AUTH_KEY");
         sender.setEndPoint(url);
         
         JSONObject params = new JSONObject();
         
         try {
               params.put("action", "Action 1");
               params.put("id", "1");
              } catch (JSONException e) {
                 Log.d("TAG", "Failed to put your json");
              }
           sender.sendEvent(STREAM, params.toString());
                
    }
```
Make sure you have replaced "YOUR_AUTH_KEY with your IronSourceAtom auth key, and "YOUR_IRONSOURCEATOM_STREAM_NAME" to the desired destination (e.g: “cluster.schema.table”)
### Events traker usage
You can use event tracker as shown below
```html
...
import io.ironsourceatom.sdk.HttpMethod;
import io.ironsourceatom.sdk.IronSourceAtom;
import io.ironsourceatom.sdk.IronSourceAtomEventSender;

public class BaseMainActivity extends Activity {
    private IronSourceAtom ironSourceAtom;
    private final String STREAM="YOUR_IRONSOURCEATOM_STREAM_NAME";
    @Override
       protected void onCreate(Bundle savedInstanceState) {
            ...
            // Configure IronSourceAtom
           ironSourceAtom = IronSourceAtom.getInstance(this);
           ironSourceAtom.setAllowedNetworkTypes(ironSourceAtom.NETWORK_MOBILE | ironSourceAtom.NETWORK_WIFI);
           ironSourceAtom.setAllowedOverRoaming(true);
           // Create and config ironSourceAtomTracker
           ironSourceAtomTracker tracker = ironSourceAtom.newTracker("YOUR_AUTH_KEY");
           try {
               JSONObject events = new JSONObject();
               events.put("action", "click on ...");
               events.put("user_id", user.id);
               tracker.track("STREAM", event);
           } catch (JSONException e) {
               ...
           }
           ...
        
    }
```


### Example

You can use our [example][example-url] for sending data to Atom:

![alt text][example]

### License
MIT

[example-url]: https://github.com/ironSource/ironbeast-android/tree/master/ironbeast-samples
[example]: https://cloud.githubusercontent.com/assets/19428452/15633715/2f12d29e-25bd-11e6-88bc-22649390fff5.png "example"
[license-image]: https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square
[license-url]: LICENSE
[travis-image]: https://travis-ci.org/ironSource/ironbeast-android.svg?branch=service%2FISA-221
[travis-url]: https://travis-ci.org/ironSource/ironbeast-android
[coveralls-image]: https://coveralls.io/repos/github/ironSource/ironbeast-android/badge.svg?branch=service%2FISA-221
[coveralls-url]: https://coveralls.io/github/ironSource/ironbeast-android?branch=master
[docs-image]: https://img.shields.io/badge/docs-latest-blue.svg
[docs-url]: https://ironsource.github.io/ironbeast-android/
[sauce-image]: https://saucelabs.com/browser-matrix/jacckson.svg?auth=433c2b373dfd86bc7d78fc8bf36dbc3b
[sauce-url]: https://saucelabs.com/u/jacckson?auth=433c2b373dfd86bc7d78fc8bf36dbc3b

