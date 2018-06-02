# [![bitmovin](http://bitmovin-a.akamaihd.net/webpages/bitmovin-logo-github.png)](http://www.bitmovin.com)
JAVA-Client which enables you to seamlessly integrate the [Bitmovin API](https://bitmovin.com/video-infrastructure-service-bitmovin-api/) into your projects.
Using this API client requires an active account. [Sign up for a Bitmovin API key](https://bitmovin.com/bitmovins-video-api/).

The full [Bitmovin API reference](https://bitmovin.com/encoding-documentation/bitmovin-api/) can be found on our website.

# Getting started
## Maven
Add this to your pom.xml:
```xml
<dependency>
      <groupId>com.bitmovin.api</groupId>
      <artifactId>bitmovin-java</artifactId>
      <version>1.19.0</version>
</dependency>
```

## Examples


Encode example with Java Client 
Here is a starting guide based on a full example (Non-DRM), which is using the Bitmovin JAVA API client and is available in Bitmovin Github repository. A DRM example with CENC (PlayReady/Widevine) and FairPlay can be found here.

These are the common steps required 
* Define an Input
* Define an Output
* Define a Codec Configuration
* Create an Encoding - (Optional DRM Step)
* Initiate an Encoding
* Create a Manifest (MPD/HLS) - (Optional DRM Step)
* Webhook

Create an Encoding with Java Client Example

Pre-requisites: Please ensure you have read README.md from https://github.com/bitmovin/bitmovin-java and retrieve your encoding API key
private static String ApiKey = "<INSERT_YOUR_APIKEY>";
 
Define an Input
Bitmovin support a vast amount of input storage source, in this example we will create an “S3 Input” with the following code:
```java
private static String S3_INPUT_ACCESSKEY = "<INSERT_YOUR_ACCESSKEY>";
private static String S3_INPUT_SECRET_KEY = "<INSERT_YOUR_SECRETKEY>";
private static String S3_INPUT_BUCKET_NAME = "BUCKET_NAME";
private static String S3_INPUT_PATH = "<INSERT_YOUR_PATH_TO_INPUT_FILE>";

S3Input input = new S3Input();
input.setAccessKey(S3_INPUT_ACCESSKEY);
input.setSecretKey(S3_INPUT_SECRET_KEY);
input.setBucketName(S3_INPUT_BUCKET_NAME);
input = bitmovinApi.input.s3.create(input)
```


An Input does not refer to a specific file but to a storage location, we can easily re-use an input for other encodings as follows:
```java
private static String S3_INPUT_ID = "<S3_INPUT_ID>"; // Reuse Input
private static String S3_INPUT_PATH = "/INSERT_YOUR_PATH_TO_INPUT_FILE/SampleVideo_1280x720_5mb.mp4";
```

 
#### Define an Output
Similar to input, we must declare an output storage location needed to access a specific type of storage in order to make the encoded content accessible for playback. For this example we will create an “S3 Output”, with the following code:
```java
private static String S3_OUTPUT_ACCESSKEY = "<INSERT_YOUR_ACCESSKEY>";
private static String S3_OUTPUT_SECRET_KEY = "<INSERT_YOUR_SECRETKEY>";
private static String S3_OUTPUT_BUCKET_NAME = "BUCKET_NAME";
private static String OUTPUT_BASE_PATH = "path/to/your/outputs/" + new Date().getTime();
 
S3Output output = new S3Output();
output.setAccessKey(S3_OUTPUT_ACCESSKEY);
output.setSecretKey(S3_OUTPUT_SECRET_KEY);
output.setBucketName(S3_OUTPUT_BUCKET_NAME);
output = bitmovinApi.output.s3.create(output);
```

Similar to input, we can reuse and output for other encodings by using:
```java
private static String S3_OUTPUT_ID ="<S3_OUTPUT_ID>";
private static String OUTPUT_BASE_PATH =  "outputs/" + ENCODING_JOB_NAME + "/";
```

 
#### Create Codec Configurations
A Codec Configuration defines, which codec and encoding configuration shall be used, to create one audio or video rendition out of a audio or video stream of the given input file.
H264 Video Codec Configuration Example 1080p
```java
H264VideoConfiguration videoConfiguration1080p = new H264VideoConfiguration();
videoConfiguration1080p.setHeight(1080);
videoConfiguration1080p.setBitrate(4800000L);
videoConfiguration1080p.setProfile(ProfileH264.HIGH);
videoConfiguration1080p = bitmovinApi.configuration.videoH264.create(videoConfiguration1080p);
AAC Audio Codec Configuration Example
AACAudioConfig aacConfiguration = new AACAudioConfig();
aacConfiguration.setBitrate(128000L);
aacConfiguration.setRate(48000f);
aacConfiguration = bitmovinApi.configuration.audioAAC.create(aacConfiguration);
```
If you want to use an existing video or audio codec configuration you can do so as well by loading them by their codec configuration id
```java
AACAudioConfig aacConfiguration = bitmovinApi.configuration.audioAAC.get(AAC_CONFIGURATION_ID);
H264VideoConfiguration videoConfiguration1080p = bitmovinApi.configuration.videoH264.get(VIDEO_CONFIGURATION_1080P);
```


 
#### Create an Encoding
```java
Encoding encoding = new Encoding();
encoding.setName("Encoding JAVA");
encoding.setCloudRegion(cloudRegion);
encoding = bitmovinApi.encoding.create(encoding);
```

Encoding is a collection of resources that are mapped to each other. Other than Inputs, Output, Codec Configurations, and Filters, there are also resources, which require an encoding in order to create them:
Streams - A stream maps a specific audio or video input stream of the input file (called “input stream”) to one audio or video codec configuration. The following example using the SelectionMode “AUTO”, which will select the first available video input stream.
```java
InputStream inputStreamAudio = new InputStream();
inputStreamAudio.setInputPath(S3_INPUT_PATH);
inputStreamAudio.setInputId(input.getId());
inputStreamAudio.setSelectionMode(StreamSelectionMode.AUTO);
inputStreamAudio.setPosition(0);

InputStream inputStreamVideo = new InputStream();
inputStreamVideo.setInputPath(S3_INPUT_PATH);
inputStreamVideo.setInputId(input.getId());
inputStreamVideo.setSelectionMode(StreamSelectionMode.AUTO);
inputStreamVideo.setPosition(0);

Stream audioStream = new Stream();
audioStream.setCodecConfigId(aacConfiguration.getId());
audioStream.setInputStreams(Collections.singleton(inputStreamAudio));
audioStream = bitmovinApi.encoding.stream.addStream(encoding, audioStream);

Stream videoStream1080p = new Stream();
videoStream1080p.setCodecConfigId(videoConfiguration1080p.getId());
videoStream1080p.setInputStreams(Collections.singleton(inputStreamVideo));
videoStream1080p = bitmovinApi.encoding.stream.addStream(encoding, videoStream1080p);
```

Muxings - A Muxing defines which container format will be used for the encoded video or audio files (segmented TS, progressive TS, MP4, WebM, ...). It requires a Stream, an Output, and the output path, where the resulting segments will be stored at. The example below shows a fMP4 and TS Muxings takes the encoded segments and stores them using the MP4 and TS container format respectively. Those segments will be uploaded to the given output path on the given Output.
```java
EncodingOutput encodingOutput = new EncodingOutput();
encodingOutput.setOutputId(output.getId());
encodingOutput.setOutputPath(OUTPUT_BASE_PATH);

FMP4Muxing fmp4Muxing1080 = this.createFMP4Muxing(encoding, videoStream1080p, output, OUTPUT_BASE_PATH + "/video/1080p_dash", AclPermission.PUBLIC_READ);
FMP4Muxing fmp4Audio = this.createFMP4Muxing(encoding, audioStream, output, OUTPUT_BASE_PATH + "/audio/128kbps_dash", AclPermission.PUBLIC_READ);

TSMuxing tsMuxing1080 = this.createTSMuxing(encoding, videoStream1080p, output, OUTPUT_BASE_PATH + "/video/1080p_hls", AclPermission.PUBLIC_READ);
TSMuxing tsAudio = this.createTSMuxing(encoding, audioStream, output, OUTPUT_BASE_PATH + "/audio/128kbps_hls", AclPermission.PUBLIC_READ);
```

 
#### Create an Encoding with DRM
In order to protect Premium content whilst ensuring the maximum devices coverage, we recommend the use of Multi-DRM solution. In this example we will demonstrate the use of CENC (PlayReady + widevine) and FairPlay.
We will begin by specifying DRM parameters. These values will be provided by your multi-DRM licensing server provider such as Irdeto, EZDRM, ExpressPlay, Axinom, etc. 
```java
private static String CENC_KEY = "<INSERT_YOUR_CENC_KEY>";
private static String CENC_KID = "<INSERT_YOUR_CENC_KID>";
private static String PLAYREADY_LAURL = "http://playready.directtaps.net/pr/svc/rightsmanager.asmx?UseSimpleNonPersistentLicense=1";
private static String WIDEVINE_PSSH = "<INSERT_YOUR_WIDEVINE_PSSH>";

private static String FairPlay_KEY = "<INSERT_YOUR_FairPlay_KEY>";
private static String FairPlay_IV = "<INSERT_YOUR_FairPlay_IV>";
private static String FairPlay_URI = "skd://userspecifc?custom=information";
```

* key: This is the common content encryption key
* kid: This is the common unique identifier for your content key in hex format
* widevinePssh: This is the value for the Widevine pssh box
* playreadyLaUrl: This is the URL to the PlayReady license server
* FairPlayKey: A key that will be used to encrypt the content (16 byte; 32 hexadecimal characters)
* FairPlayIV: The initialization vector is optional. If it is not provided we will generate one for you. (16 byte; 32 hexadecimal characters)
* FairPlayUri: If provided, this URI will be used for license acquisition


We will use these values to create a CENC DRM and FairPlay DRM resources, will be created for each fMP4 and TS Muxing you want to be encrypted and protected with Widevine and PlayReady DRM as well as FairPlay respectively, together with a output location, where those encrypted segments should be stored at. Hence we don't provide the output location with the fMP4 or TS Muxing, but with the CENC and FairPlay DRM resource instead.
```java
// fMP4 Muxing
FMP4Muxing fmp4Muxing1080 = this.createFMP4MuxingNoOutput(encoding, videoStream1080p);
FMP4Muxing fmp4Audio = this.createFMP4MuxingNoOutput(encoding, audioStream);
// CENC DRM resource
CencDrm videoDRM1080p = this.getCencDRMWithWidevineAndPlayready();
CencDrm audioDRM = this.getCencDRMWithWidevineAndPlayready();
// Add output to CENC DRM resource
this.addOutputToDRM(videoDRM1080p, output, OUTPUT_BASE_PATH + "/video/1080p_dash/drm");
this.addOutputToDRM(audioDRM, output, OUTPUT_BASE_PATH + "/audio/128kbps_dash/drm");
// Add CENC DRM to fMP4 Muxing
videoDRM1080p = this.addCencDrmToFmp4Muxing(encoding, fmp4Muxing1080, videoDRM1080p);
audioDRM = this.addCencDrmToFmp4Muxing(encoding, fmp4Audio, audioDRM);

// TS Muxing
TSMuxing tsMuxing1080 = this.createTSMuxingNoOutput(encoding, videoStream1080p);
TSMuxing tsAudio = this.createTSMuxingNoOutput(encoding, audioStream);
// FP DRM resource
FairPlayDrm videoFairPlayDRM1080p = this.getFairPlayDRM();
FairPlayDrm audioFairPlayDRM = this.getFairPlayDRM();
// Add output to FP DRM resource
this.addOutputToDRM(videoFairPlayDRM1080p, output, OUTPUT_BASE_PATH + "/video/1080p_hls/FairPlay_drm");
this.addOutputToDRM(audioFairPlayDRM, output, OUTPUT_BASE_PATH + "/audio/128kbps_hls/FairPlay_drm");
// Add FP DRM to TS Muxing
videoFairPlayDRM1080p = this.addFairPlayDrmToTssMuxing(encoding, tsMuxing1080, videoFairPlayDRM1080p);
audioFairPlayDRM = this.addFairPlayDrmToTssMuxing(encoding, tsAudio, audioFairPlayDRM);
```
* Additional steps are required in manifest creation to support DRM, see later section.
Note: If a output location for the muxing as well, our API would store unencrypted segments to the given output location as well.


#### Initiate an Encoding
The configuration of the encoding by now, we will initiate the encoding task.
bitmovinApi.encoding.start(encoding);
```java
Task status = bitmovinApi.encoding.getStatus(encoding);

while (status.getStatus() != Status.FINISHED && status.getStatus() != Status.ERROR) {
   status = bitmovinApi.encoding.getStatus(encoding);
   Thread.sleep(2500);
}

System.out.println(String.format("Encoding finished with status %s", status.getStatus().toString()));

if (status.getStatus() != Status.FINISHED) {
   System.out.println("Encoding has status error ... can not create manifest");
   return;
}

```

Encoding tasks are orchestrated and monitored by Bitmovin encoding service, task can be managed and monitored using Bitmovin REST API (bitmovingApi.encoding.getStatus) or via the Bitmovin user console. 
In order to play segmented content, a manifest is required, which contains all the information where and how the player can the content for playback.
 
#### Create a Manifest (MPD/HLS)
An MPD Manifest will need an output configuration (Output + output path information), a manifest name, and a manifest type:
```java
// MANIFEST OUTPUT DESTINATION
EncodingOutput manifestDestination = new EncodingOutput();
manifestDestination.setOutputId(output.getId());
manifestDestination.setOutputPath(OUTPUT_BASE_PATH);
manifestDestination.setAcl(Collections.singletonList(new AclEntry(AclPermission.PUBLIC_READ)));

// DASH MANIFEST
DashManifest manifest = this.createDashManifest("manifest.mpd", manifestDestination);
Add a Period to the Manifest
// Add a Period to the Manifest
Period period = this.addPeriodToDashManifest(manifest);
Add a Video and Audio AdaptationSet to the Period
// CREATE VIDEO ADAPTATION SET
VideoAdaptationSet videoAdaptationSet = this.addVideoAdaptationSetToPeriod(manifest, period);
// CREATE AUDIO ADAPTATION SET FOR EACH LANGUAGE
AudioAdaptationSet audioAdaptationSet = this.addAudioAdaptationSetToPeriodWithRoles(manifest, period, "en");
Add Muxings to Video and Audio AdaptationSet
this.addDashRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), fmp4Muxing1080.getId(), "video/1080p_dash", manifest, period, videoAdaptationSet);
this.addDashRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), fmp4Audio.getId(), "audio/128kbps_dash", manifest, period, audioAdaptationSet);
 
Optional DRM steps (CENC example)
// Add Muxings to DRM Video fMP4 AdaptationSet
DashDRMRepresentation playReadyDrmRepresentationVideo1080 = this.addDashDRMRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), videoStream1080p.getId(), fmp4Muxing1080.getId(), videoDRM1080p.getId(), "video/1080p_dash/drm/", manifest, period, videoAdaptationSet);
// Add Muxings to DRM Audio fMP4 AdaptationSet
DashDRMRepresentation playReadyDrmRepresentationAudio = this.addDashDRMRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), audioStream.getId(), fmp4Audio.getId(), audioDRM.getId(), "audio/128kbps_dash/drm/", manifest, period, audioAdaptationSet);
 
// Add content protection to DRM Video fMP4 Representation
this.addContentProtectionToDRMfMP4Representation(manifest, period, videoAdaptationSet, playReadyDrmRepresentationVideo1080, this.getContentProtection(encoding.getId(), videoStream1080p.getId(), fmp4Muxing1080.getId(), videoDRM1080p.getId()));
// Add content protection to DRM Audio fMP4 Representation
this.addContentProtectionToDRMfMP4Representation(manifest, period, audioAdaptationSet, playReadyDrmRepresentationAudio, this.getContentProtection(encoding.getId(), audioStream.getId(), fmp4Audio.getId(), audioDRM.getId()));
```

 
#### Start MPD Manifest Creation
```java
bitmovinApi.manifest.dash.startGeneration(manifest);
Status dashStatus = bitmovinApi.manifest.dash.getGenerationStatus(manifest);
while (dashStatus != Status.FINISHED && dashStatus != Status.ERROR) {
   dashStatus = bitmovinApi.manifest.dash.getGenerationStatus(manifest);
   Thread.sleep(2500);
}
if (dashStatus != Status.FINISHED) {
   System.out.println("Could not create DASH manifest");
   return;
}
Once configuration has been defined, the MPD manifest creation can start. Once this is completed the manifest will be stored in the output location specified.
For an HLS manifest works similar than for MPEG-DASH. First we define the output configuration and name for the HLS manifest. In this example, we will use the same output previously used for MPD manifest:
// HLS Manifest
HlsManifest manifestHls = this.createHlsManifest("manifest.m3u8", manifestDestination);
Add Audio Media Info and Video Stream Info
// Add Audio Media Info
MediaInfo audioMediaInfo = new MediaInfo();
audioMediaInfo.setName("audio.m3u8");
audioMediaInfo.setUri("audio.m3u8");
audioMediaInfo.setGroupId("audio");
audioMediaInfo.setType(MediaInfoType.AUDIO);
audioMediaInfo.setEncodingId(encoding.getId());
audioMediaInfo.setStreamId(audioStream.getId());
audioMediaInfo.setMuxingId(tsAudio.getId());
audioMediaInfo.setLanguage("en");
audioMediaInfo.setAssocLanguage("en");
audioMediaInfo.setAutoselect(false);
audioMediaInfo.setIsDefault(false);
audioMediaInfo.setForced(false);
audioMediaInfo.setSegmentPath("audio/128kbps_hls");
bitmovinApi.manifest.hls.createMediaInfo(manifestHls, audioMediaInfo);

// Add Video Stream Info
this.addStreamInfoToHlsManifest("video_1080p.m3u8", encoding.getId(), videoStream1080p.getId(), tsMuxing1080.getId(), "audio", "video/1080p_hls", manifestHls);
```
 
#### Optional DRM steps (FairPlay example)

DRM info must be specified in Stream Info.
```java
MediaInfo audioMediaInfo = new MediaInfo();
audioMediaInfo.setName("audio.m3u8");
audioMediaInfo.setUri("audio.m3u8");
audioMediaInfo.setGroupId("audio");
audioMediaInfo.setType(MediaInfoType.AUDIO);
audioMediaInfo.setEncodingId(encoding.getId());
audioMediaInfo.setStreamId(audioStream.getId());
audioMediaInfo.setMuxingId(tsAudio.getId());
audioMediaInfo.setDrmId(audioFairPlayDRM.getId()); // Added FP DRM Id audioMediaInfo.setLanguage("en");
audioMediaInfo.setAssocLanguage("en");
audioMediaInfo.setAutoselect(true);
audioMediaInfo.setIsDefault(true);
audioMediaInfo.setForced(false);
audioMediaInfo.setSegmentPath("audio/128kbps_hls/fairplay_drm");
audioMediaInfo = bitmovinApi.manifest.hls.createMediaInfo(manifestHls, audioMediaInfo);
```

Note: the segment Path is also different, typically it is one level below the non-encrypted "audio/128kbps_hls/fairplay_drm" as suppose to audio/128kbps_hls/
 
 
The difference for video stream info is located in the method addStreamInfoToHlsManifest whereby the example above has 7 arguments, the example below has 8 arguments.

```java
this.addStreamInfoToHlsManifest("video_1080p.m3u8", encoding.getId(), videoStream1080p.getId(), tsMuxing1080.getId(), videoFairPlayDRM1080p.getId(), audioMediaInfo.getGroupId(), "video/1080p_hls/fairplay_drm", manifestHls);
 
private StreamInfo addStreamInfoToHlsManifest(String uri, String encodingId, String streamId, String muxingId, String drmId, String audioGroupId, String segmentPath, HlsManifest manifest)
       throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException {
   StreamInfo s = new StreamInfo();
   s.setUri(uri);
   s.setEncodingId(encodingId);
   s.setStreamId(streamId);
   s.setMuxingId(muxingId);
   s.setDrmId(drmId); // FP DRM ID
   s.setAudio(audioGroupId);
   s.setSegmentPath(segmentPath);
   s = bitmovinApi.manifest.hls.createStreamInfo(manifest, s);
   return s;
}
```

 
#### Start HLS Manifest Creation
```java
bitmovinApi.manifest.hls.startGeneration(manifestHls);
Status hlsStatus = bitmovinApi.manifest.hls.getGenerationStatus(manifestHls);
while (hlsStatus != Status.FINISHED && hlsStatus != Status.ERROR) {
   hlsStatus = bitmovinApi.manifest.hls.getGenerationStatus(manifestHls);
   Thread.sleep(2500);
}
if (hlsStatus != Status.FINISHED) {
   System.out.println("Could not create HLS manifest");
   return;
}
System.out.println("Encoding completed successfully");

Once configuration has been defined, the HLS manifest creation can start. Once this is completed the manifest will be stored in the output location specified. The content is ready for playback from the output destination as specified. 
```

#### Webhook
The use of webhook allows you to push notification of the encoding job rather than pulling status via Bitmovin API.

Set up is very simple, specify the URL you wish notifications get push to and initiate before the encoding job stats.

```java
private static String NOTIFICATION_URL = "<INSERT_YOUR_NOTIFICATION_URL>";

// Create webhook for push notification
      this.createWebHook(encoding)
  // Starts encoding job
       bitmovinApi.encoding.start(encoding);

``` 



For examples go to our [example page](https://github.com/bitmovin/bitmovin-java/tree/develop/src/test/java/com/bitmovin/api/examples/).
