package com.bitmovin.api.examples;

import com.bitmovin.api.BitmovinApi;
import com.bitmovin.api.encoding.AclEntry;
import com.bitmovin.api.encoding.AclPermission;
import com.bitmovin.api.encoding.EncodingOutput;
import com.bitmovin.api.encoding.InputStream;
import com.bitmovin.api.encoding.codecConfigurations.AACAudioConfig;
import com.bitmovin.api.encoding.codecConfigurations.H264VideoConfiguration;
import com.bitmovin.api.encoding.codecConfigurations.enums.ProfileH264;
import com.bitmovin.api.encoding.encodings.Encoding;
import com.bitmovin.api.encoding.encodings.drms.CencDrm;
import com.bitmovin.api.encoding.encodings.drms.Drm;
import com.bitmovin.api.encoding.encodings.drms.FairPlayDrm;
import com.bitmovin.api.encoding.encodings.drms.cencSystems.CencPlayReady;
import com.bitmovin.api.encoding.encodings.drms.cencSystems.CencWidevine;
import com.bitmovin.api.encoding.encodings.muxing.FMP4Muxing;
import com.bitmovin.api.encoding.encodings.muxing.MuxingStream;
import com.bitmovin.api.encoding.encodings.muxing.TSMuxing;
import com.bitmovin.api.encoding.encodings.streams.Stream;
import com.bitmovin.api.encoding.enums.CloudRegion;
import com.bitmovin.api.encoding.enums.DashMuxingType;
import com.bitmovin.api.encoding.enums.StreamSelectionMode;
import com.bitmovin.api.encoding.inputs.S3Input;
import com.bitmovin.api.encoding.manifest.dash.AdaptationSet;
import com.bitmovin.api.encoding.manifest.dash.AudioAdaptationSet;
import com.bitmovin.api.encoding.manifest.dash.ContentProtection;
import com.bitmovin.api.encoding.manifest.dash.DashDRMRepresentation;
import com.bitmovin.api.encoding.manifest.dash.DashFmp4Representation;
import com.bitmovin.api.encoding.manifest.dash.DashManifest;
import com.bitmovin.api.encoding.manifest.dash.Period;
import com.bitmovin.api.encoding.manifest.dash.VideoAdaptationSet;
import com.bitmovin.api.encoding.manifest.hls.HlsManifest;
import com.bitmovin.api.encoding.manifest.hls.MediaInfo;
import com.bitmovin.api.encoding.manifest.hls.MediaInfoType;
import com.bitmovin.api.encoding.manifest.hls.StreamInfo;
import com.bitmovin.api.encoding.outputs.Output;
import com.bitmovin.api.encoding.outputs.S3Output;
import com.bitmovin.api.encoding.status.Task;
import com.bitmovin.api.enums.Status;
import com.bitmovin.api.exceptions.BitmovinApiException;
import com.bitmovin.api.http.RestException;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Andreas Rudich on 25.07.17.
 **/
public class CreateEncodingWithCencDRM
{
    private static String ApiKey = "d8e098d1-85e3-4b49-aa13-f8ac8acb443c";;
    private static CloudRegion cloudRegion = CloudRegion.AWS_EU_WEST_1;;
     

    
    private static Boolean ENCRYPTION_FLAG = false; // set to true if you need to encrypt the content
    private static String ENCRYTION_IN_TITLE = (ENCRYPTION_FLAG) ? "with Encryption" : "";
    private static String ENCODING_JOB_NAME = "Java encoding example " + ENCRYTION_IN_TITLE + " " + new Date().getTime();
    
    // Inputs
    
    // HTTP
    // private static String HTTPS_INPUT_HOST = "<INSERT_YOUR_HTTP_HOST>"; // ex.: storage.googleapis.com/
    // private static String HTTPS_INPUT_PATH = "<INSERT_YOUR_PATH_TO_INPUT_FILE>";
    
    // S3
    // private static String S3_INPUT_ACCESSKEY = "<INSERT_YOUR_ACCESSKEY>";
    // private static String S3_INPUT_SECRET_KEY = "<INSERT_YOUR_SECRETKEY>";
    // private static String S3_INPUT_BUCKET_NAME = "BUCKET_NAME";
    // private static String S3_INPUT_PATH = "<INSERT_YOUR_PATH_TO_INPUT_FILE>";
    
    private static String S3_INPUT_ID = "d9de38f7-47ac-4097-a640-1f355f619be7"; // Reuse Input set in bitmovin
    private static String S3_INPUT_PATH = "/inputs/SampleVideo_1280x720_5mb.mp4";

    // Outputs

    // S3
    // private static String S3_OUTPUT_ACCESSKEY = "<INSERT_YOUR_ACCESSKEY>";
    // private static String S3_OUTPUT_SECRET_KEY = "<INSERT_YOUR_SECRETKEY>";
    // private static String S3_OUTPUT_BUCKET_NAME = "BUCKET_NAME";
    // private static String OUTPUT_BASE_PATH = "path/to/your/outputs/" + new Date().getTime();
    
    private static String S3_OUTPUT_ID ="a3881e62-03f5-482b-8f8a-6ac8603f0c83";
    private static String S3_OUTPUT_BASE_PATH =  "outputs/" + ENCODING_JOB_NAME + "/";
    
    private static String CENC_KEY = "<INSERT_YOUR_CENC_KEY>";
    private static String CENC_KID = "<INSERT_YOUR_CENC_KID>";
    private static String PLAYREADY_LAURL = "http://playready.directtaps.net/pr/svc/rightsmanager.asmx?UseSimpleNonPersistentLicense=1";
    private static String WIDEVINE_PSSH = "<INSERT_YOUR_WIDEVINE_PSSH>";

    private static String FAIRPLAY_KEY = "<INSERT_YOUR_FAIRPLAY_KEY>";
    private static String FAIRPLAY_IV = "<INSERT_YOUR_FAIRPLAY_IV>";
    private static String FAIRPLAY_URI = "skd://userspecifc?custom=information";

    // Reusable Codec configuration
    private static String AAC_CONFIGURATION_ID = "c16acdb0-35e8-4754-b731-fcd5b1eb1ca3";
    private static String VIDEO_CONFIGURATION_240P = "70d7e030-add3-4196-9c68-b9731d93533a";
    private static String VIDEO_CONFIGURATION_360P = "2b61653c-8f03-466f-84ae-ae73f5a0d572";
    private static String VIDEO_CONFIGURATION_480P = "8aaf58c1-c738-41de-9e3c-821c47e12b32";
    private static String VIDEO_CONFIGURATION_720P = "55faf897-6325-419a-8b4c-b86dbcc3c104";
    private static String VIDEO_CONFIGURATION_1080P = "5df433a1-0edd-4311-893d-177149775c36";

    private static BitmovinApi bitmovinApi;


    @Test
    public void testDrmEncoding() throws IOException, BitmovinApiException, UnirestException, URISyntaxException, RestException, InterruptedException
    {
        bitmovinApi = new BitmovinApi(ApiKey);

        Encoding encoding = new Encoding();
        encoding.setName(ENCODING_JOB_NAME);
        encoding.setCloudRegion(cloudRegion);
        encoding = bitmovinApi.encoding.create(encoding);

        //  HttpsInput input = new HttpsInput();
        //  input.setHost(HTTPS_INPUT_HOST);
        //  input = bitmovinApi.input.https.create(input);

        //  S3Output output = new S3Output();
        //  output.setAccessKey(S3_OUTPUT_ACCESSKEY);
        //  output.setSecretKey(S3_OUTPUT_SECRET_KEY);
        //  output.setBucketName(S3_OUTPUT_BUCKET_NAME);
        //  output = bitmovinApi.output.s3.create(output);

//        AACAudioConfig aacConfiguration = new AACAudioConfig();
//        aacConfiguration.setBitrate(128000L);
//        aacConfiguration.setRate(48000f);
//        aacConfiguration = bitmovinApi.configuration.audioAAC.create(aacConfiguration);
//
//        H264VideoConfiguration videoConfiguration240p = new H264VideoConfiguration();
//        videoConfiguration240p.setHeight(240);
//        videoConfiguration240p.setBitrate(400000L);
//        videoConfiguration240p.setProfile(ProfileH264.HIGH);
//        videoConfiguration240p = bitmovinApi.configuration.videoH264.create(videoConfiguration240p);
//
//        H264VideoConfiguration videoConfiguration360p = new H264VideoConfiguration();
//        videoConfiguration360p.setHeight(360);
//        videoConfiguration360p.setBitrate(800000L);
//        videoConfiguration360p.setProfile(ProfileH264.HIGH);
//        videoConfiguration360p = bitmovinApi.configuration.videoH264.create(videoConfiguration360p);
//
//        H264VideoConfiguration videoConfiguration480p = new H264VideoConfiguration();
//        videoConfiguration480p.setHeight(480);
//        videoConfiguration480p.setBitrate(1200000L);
//        videoConfiguration480p.setProfile(ProfileH264.HIGH);
//        videoConfiguration480p = bitmovinApi.configuration.videoH264.create(videoConfiguration480p);
//
//        H264VideoConfiguration videoConfiguration720p = new H264VideoConfiguration();
//        videoConfiguration720p.setHeight(720);
//        videoConfiguration720p.setBitrate(2400000L);
//        videoConfiguration720p.setProfile(ProfileH264.HIGH);
//        videoConfiguration720p = bitmovinApi.configuration.videoH264.create(videoConfiguration720p);
//
//        H264VideoConfiguration videoConfiguration1080p = new H264VideoConfiguration();
//        videoConfiguration1080p.setHeight(1080);
//        videoConfiguration1080p.setBitrate(4800000L);
//        videoConfiguration1080p.setProfile(ProfileH264.HIGH);
//        videoConfiguration1080p = bitmovinApi.configuration.videoH264.create(videoConfiguration1080p);
        
        S3Input input = bitmovinApi.input.s3.get(S3_INPUT_ID); // input id
        S3Output output = bitmovinApi.output.s3.get(S3_OUTPUT_ID); // output id
        
        AACAudioConfig aacConfiguration = bitmovinApi.configuration.audioAAC.get(AAC_CONFIGURATION_ID);
        H264VideoConfiguration videoConfiguration240p = bitmovinApi.configuration.videoH264.get(VIDEO_CONFIGURATION_240P);
        H264VideoConfiguration videoConfiguration360p = bitmovinApi.configuration.videoH264.get(VIDEO_CONFIGURATION_360P);
        H264VideoConfiguration videoConfiguration480p = bitmovinApi.configuration.videoH264.get(VIDEO_CONFIGURATION_480P);
        H264VideoConfiguration videoConfiguration720p = bitmovinApi.configuration.videoH264.get(VIDEO_CONFIGURATION_720P);
        H264VideoConfiguration videoConfiguration1080p = bitmovinApi.configuration.videoH264.get(VIDEO_CONFIGURATION_1080P);

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

        Stream videoStream240p = new Stream();
        videoStream240p.setCodecConfigId(videoConfiguration240p.getId());
        videoStream240p.setInputStreams(Collections.singleton(inputStreamVideo));
        videoStream240p = bitmovinApi.encoding.stream.addStream(encoding, videoStream240p);

        Stream videoStream360p = new Stream();
        videoStream360p.setCodecConfigId(videoConfiguration360p.getId());
        videoStream360p.setInputStreams(Collections.singleton(inputStreamVideo));
        videoStream360p = bitmovinApi.encoding.stream.addStream(encoding, videoStream360p);

        Stream videoStream480p = new Stream();
        videoStream480p.setCodecConfigId(videoConfiguration480p.getId());
        videoStream480p.setInputStreams(Collections.singleton(inputStreamVideo));
        videoStream480p = bitmovinApi.encoding.stream.addStream(encoding, videoStream480p);

        Stream videoStream720p = new Stream();
        videoStream720p.setCodecConfigId(videoConfiguration720p.getId());
        videoStream720p.setInputStreams(Collections.singleton(inputStreamVideo));
        videoStream720p = bitmovinApi.encoding.stream.addStream(encoding, videoStream720p);

        Stream videoStream1080p = new Stream();
        videoStream1080p.setCodecConfigId(videoConfiguration1080p.getId());
        videoStream1080p.setInputStreams(Collections.singleton(inputStreamVideo));
        videoStream1080p = bitmovinApi.encoding.stream.addStream(encoding, videoStream1080p);

        EncodingOutput encodingOutput = new EncodingOutput();
        encodingOutput.setOutputId(output.getId());
        encodingOutput.setOutputPath(S3_OUTPUT_BASE_PATH);

            FMP4Muxing fmp4Muxing240;
            FMP4Muxing fmp4Muxing360;
            FMP4Muxing fmp4Muxing480;
            FMP4Muxing fmp4Muxing720;
            FMP4Muxing fmp4Muxing1080;
            FMP4Muxing fmp4Audio;

            TSMuxing tsMuxing240;
            TSMuxing tsMuxing360;
            TSMuxing tsMuxing480;
            TSMuxing tsMuxing720;
            TSMuxing tsMuxing1080;
            TSMuxing tsAudio;

            CencDrm videoDRM240p = new CencDrm();
            CencDrm videoDRM360p = new CencDrm();
            CencDrm videoDRM480p = new CencDrm();
            CencDrm videoDRM720p = new CencDrm();
            CencDrm videoDRM1080p = new CencDrm();
            CencDrm audioDRM = new CencDrm();

            FairPlayDrm videoFairPlayDRM240p = new FairPlayDrm();
            FairPlayDrm videoFairPlayDRM360p = new FairPlayDrm();
            FairPlayDrm videoFairPlayDRM480p = new FairPlayDrm();
            FairPlayDrm videoFairPlayDRM720p = new FairPlayDrm();
            FairPlayDrm videoFairPlayDRM1080p = new FairPlayDrm();
            FairPlayDrm audioFairPlayDRM = new FairPlayDrm();

        if (ENCRYPTION_FLAG) {
            fmp4Muxing240 = this.createFMP4MuxingNoOutput(encoding, videoStream240p);
            fmp4Muxing360 = this.createFMP4MuxingNoOutput(encoding, videoStream360p);
            fmp4Muxing480 = this.createFMP4MuxingNoOutput(encoding, videoStream480p);
            fmp4Muxing720 = this.createFMP4MuxingNoOutput(encoding, videoStream720p);
            fmp4Muxing1080 = this.createFMP4MuxingNoOutput(encoding, videoStream1080p);
            fmp4Audio = this.createFMP4MuxingNoOutput(encoding, audioStream);
    
            videoDRM240p = this.getCencDRMWithWidevineAndPlayready();
            videoDRM360p = this.getCencDRMWithWidevineAndPlayready();
            videoDRM480p = this.getCencDRMWithWidevineAndPlayready();
            videoDRM720p = this.getCencDRMWithWidevineAndPlayready();
            videoDRM1080p = this.getCencDRMWithWidevineAndPlayready();
            audioDRM = this.getCencDRMWithWidevineAndPlayready();
    
            this.addOutputToDRM(videoDRM240p, output, S3_OUTPUT_BASE_PATH + "/video/240p_dash/drm");
            this.addOutputToDRM(videoDRM360p, output, S3_OUTPUT_BASE_PATH + "/video/360p_dash/drm");
            this.addOutputToDRM(videoDRM480p, output, S3_OUTPUT_BASE_PATH + "/video/480p_dash/drm");
            this.addOutputToDRM(videoDRM720p, output, S3_OUTPUT_BASE_PATH + "/video/720p_dash/drm");
            this.addOutputToDRM(videoDRM1080p, output, S3_OUTPUT_BASE_PATH + "/video/1080p_dash/drm");
            this.addOutputToDRM(audioDRM, output, S3_OUTPUT_BASE_PATH + "/audio/128kbps_dash/drm");
    
            videoDRM240p = this.addCencDrmToFmp4Muxing(encoding, fmp4Muxing240, videoDRM240p);
            videoDRM360p = this.addCencDrmToFmp4Muxing(encoding, fmp4Muxing360, videoDRM360p);
            videoDRM480p = this.addCencDrmToFmp4Muxing(encoding, fmp4Muxing480, videoDRM480p);
            videoDRM720p = this.addCencDrmToFmp4Muxing(encoding, fmp4Muxing720, videoDRM720p);
            videoDRM1080p = this.addCencDrmToFmp4Muxing(encoding, fmp4Muxing1080, videoDRM1080p);
            audioDRM = this.addCencDrmToFmp4Muxing(encoding, fmp4Audio, audioDRM);
    
            tsMuxing240 = this.createTSMuxingNoOutput(encoding, videoStream240p);
            tsMuxing360 = this.createTSMuxingNoOutput(encoding, videoStream360p);
            tsMuxing480 = this.createTSMuxingNoOutput(encoding, videoStream480p);
            tsMuxing720 = this.createTSMuxingNoOutput(encoding, videoStream720p);
            tsMuxing1080 = this.createTSMuxingNoOutput(encoding, videoStream1080p);
            tsAudio = this.createTSMuxingNoOutput(encoding, audioStream);
    
            videoFairPlayDRM240p = this.getFairPlayDRM();
            videoFairPlayDRM360p = this.getFairPlayDRM();
            videoFairPlayDRM480p = this.getFairPlayDRM();
            videoFairPlayDRM720p = this.getFairPlayDRM();
            videoFairPlayDRM1080p = this.getFairPlayDRM();
            audioFairPlayDRM = this.getFairPlayDRM();
    
            this.addOutputToDRM(videoFairPlayDRM240p, output, S3_OUTPUT_BASE_PATH + "/video/240p_hls/fairplay_drm");
            this.addOutputToDRM(videoFairPlayDRM360p, output, S3_OUTPUT_BASE_PATH + "/video/360p_hls/fairplay_drm");
            this.addOutputToDRM(videoFairPlayDRM480p, output, S3_OUTPUT_BASE_PATH + "/video/480p_hls/fairplay_drm");
            this.addOutputToDRM(videoFairPlayDRM720p, output, S3_OUTPUT_BASE_PATH + "/video/720p_hls/fairplay_drm");
            this.addOutputToDRM(videoFairPlayDRM1080p, output, S3_OUTPUT_BASE_PATH + "/video/1080p_hls/fairplay_drm");
            this.addOutputToDRM(audioFairPlayDRM, output, S3_OUTPUT_BASE_PATH + "/audio/128kbps_hls/fairplay_drm");
    
            videoFairPlayDRM240p = this.addFairPlayDrmToTssMuxing(encoding, tsMuxing240, videoFairPlayDRM240p);
            videoFairPlayDRM360p = this.addFairPlayDrmToTssMuxing(encoding, tsMuxing360, videoFairPlayDRM360p);
            videoFairPlayDRM480p = this.addFairPlayDrmToTssMuxing(encoding, tsMuxing480, videoFairPlayDRM480p);
            videoFairPlayDRM720p = this.addFairPlayDrmToTssMuxing(encoding, tsMuxing720, videoFairPlayDRM720p);
            videoFairPlayDRM1080p = this.addFairPlayDrmToTssMuxing(encoding, tsMuxing1080, videoFairPlayDRM1080p);
            audioFairPlayDRM = this.addFairPlayDrmToTssMuxing(encoding, tsAudio, audioFairPlayDRM);

        } else {
        
            fmp4Muxing240 = this.createFMP4Muxing(encoding, videoStream240p, output, S3_OUTPUT_BASE_PATH + "/video/240p_dash", AclPermission.PUBLIC_READ);
            fmp4Muxing360 = this.createFMP4Muxing(encoding, videoStream360p, output, S3_OUTPUT_BASE_PATH + "/video/360p_dash", AclPermission.PUBLIC_READ);
            fmp4Muxing480 = this.createFMP4Muxing(encoding, videoStream480p, output, S3_OUTPUT_BASE_PATH + "/video/480p_dash", AclPermission.PUBLIC_READ);
            fmp4Muxing720 = this.createFMP4Muxing(encoding, videoStream720p, output, S3_OUTPUT_BASE_PATH + "/video/720p_dash", AclPermission.PUBLIC_READ);
            fmp4Muxing1080 = this.createFMP4Muxing(encoding, videoStream1080p, output, S3_OUTPUT_BASE_PATH + "/video/1080p_dash", AclPermission.PUBLIC_READ);
            fmp4Audio = this.createFMP4Muxing(encoding, audioStream, output, S3_OUTPUT_BASE_PATH + "/audio/128kbps_dash", AclPermission.PUBLIC_READ);

            tsMuxing240 = this.createTSMuxing(encoding, videoStream240p, output, S3_OUTPUT_BASE_PATH + "/video/240p_hls", AclPermission.PUBLIC_READ);
            tsMuxing360 = this.createTSMuxing(encoding, videoStream360p, output, S3_OUTPUT_BASE_PATH + "/video/360p_hls", AclPermission.PUBLIC_READ);
            tsMuxing480 = this.createTSMuxing(encoding, videoStream480p, output, S3_OUTPUT_BASE_PATH + "/video/480p_hls", AclPermission.PUBLIC_READ);
            tsMuxing720 = this.createTSMuxing(encoding, videoStream720p, output, S3_OUTPUT_BASE_PATH + "/video/720p_hls", AclPermission.PUBLIC_READ);
            tsMuxing1080 = this.createTSMuxing(encoding, videoStream1080p, output, S3_OUTPUT_BASE_PATH + "/video/1080p_hls", AclPermission.PUBLIC_READ);
            tsAudio = this.createTSMuxing(encoding, audioStream, output, S3_OUTPUT_BASE_PATH + "/audio/128kbps_hls", AclPermission.PUBLIC_READ);
        }
     

        bitmovinApi.encoding.start(encoding);

        Task status = bitmovinApi.encoding.getStatus(encoding);

        while (status.getStatus() != Status.FINISHED && status.getStatus() != Status.ERROR)
        {
            status = bitmovinApi.encoding.getStatus(encoding);
            Thread.sleep(2500);
        }

        System.out.println(String.format("Encoding finished with status %s", status.getStatus().toString()));

        if (status.getStatus() != Status.FINISHED)
        {
            System.out.println("Encoding has status error ... can not create manifest");
            Assert.fail("Encoding has status error ... can not create manifest");
        }

        System.out.println("Creating DASH manifest");

        EncodingOutput manifestDestination = new EncodingOutput();
        manifestDestination.setOutputId(output.getId());
        manifestDestination.setOutputPath(S3_OUTPUT_BASE_PATH);
        manifestDestination.setAcl(Collections.singletonList(new AclEntry(AclPermission.PUBLIC_READ)));

        DashManifest manifest = this.createDashManifest("manifest.mpd", manifestDestination);
        Period period = this.addPeriodToDashManifest(manifest);
        VideoAdaptationSet videoAdaptationSet = this.addVideoAdaptationSetToPeriod(manifest, period);
        AudioAdaptationSet audioAdaptationSet = this.addAudioAdaptationSetToPeriodWithRoles(manifest, period, "en");

        if (ENCRYPTION_FLAG) {
        DashDRMRepresentation playReadyDrmRepresentationVideo240 = this.addDashDRMRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), videoStream240p.getId(), fmp4Muxing240.getId(), videoDRM240p.getId(), "video/240p_dash/drm/", manifest, period, videoAdaptationSet);
        DashDRMRepresentation playReadyDrmRepresentationVideo360 = this.addDashDRMRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), videoStream360p.getId(), fmp4Muxing360.getId(), videoDRM360p.getId(), "video/360p_dash/drm/", manifest, period, videoAdaptationSet);
        DashDRMRepresentation playReadyDrmRepresentationVideo480 = this.addDashDRMRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), videoStream480p.getId(), fmp4Muxing480.getId(), videoDRM480p.getId(), "video/480p_dash/drm/", manifest, period, videoAdaptationSet);
        DashDRMRepresentation playReadyDrmRepresentationVideo720 = this.addDashDRMRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), videoStream720p.getId(), fmp4Muxing720.getId(), videoDRM720p.getId(), "video/720p_dash/drm/", manifest, period, videoAdaptationSet);
        DashDRMRepresentation playReadyDrmRepresentationVideo1080 = this.addDashDRMRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), videoStream1080p.getId(), fmp4Muxing1080.getId(), videoDRM1080p.getId(), "video/1080p_dash/drm/", manifest, period, videoAdaptationSet);
        DashDRMRepresentation playReadyDrmRepresentationAudio = this.addDashDRMRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), audioStream.getId(), fmp4Audio.getId(), audioDRM.getId(), "audio/128kbps_dash/drm/", manifest, period, audioAdaptationSet);

        this.addContentProtectionToDRMfMP4Representation(manifest, period, videoAdaptationSet, playReadyDrmRepresentationVideo240, this.getContentProtection(encoding.getId(), videoStream240p.getId(), fmp4Muxing240.getId(), videoDRM240p.getId()));
        this.addContentProtectionToDRMfMP4Representation(manifest, period, videoAdaptationSet, playReadyDrmRepresentationVideo360, this.getContentProtection(encoding.getId(), videoStream360p.getId(), fmp4Muxing360.getId(), videoDRM360p.getId()));
        this.addContentProtectionToDRMfMP4Representation(manifest, period, videoAdaptationSet, playReadyDrmRepresentationVideo480, this.getContentProtection(encoding.getId(), videoStream480p.getId(), fmp4Muxing480.getId(), videoDRM480p.getId()));
        this.addContentProtectionToDRMfMP4Representation(manifest, period, videoAdaptationSet, playReadyDrmRepresentationVideo720, this.getContentProtection(encoding.getId(), videoStream720p.getId(), fmp4Muxing720.getId(), videoDRM720p.getId()));
        this.addContentProtectionToDRMfMP4Representation(manifest, period, videoAdaptationSet, playReadyDrmRepresentationVideo1080, this.getContentProtection(encoding.getId(), videoStream1080p.getId(), fmp4Muxing1080.getId(), videoDRM1080p.getId()));
        this.addContentProtectionToDRMfMP4Representation(manifest, period, audioAdaptationSet, playReadyDrmRepresentationAudio, this.getContentProtection(encoding.getId(), audioStream.getId(), fmp4Audio.getId(), audioDRM.getId()));

        } else {
        this.addDashRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), fmp4Muxing1080.getId(), "video/1080p_dash", manifest, period, videoAdaptationSet);
        this.addDashRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), fmp4Muxing720.getId(), "video/720p_dash", manifest, period, videoAdaptationSet);
        this.addDashRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), fmp4Muxing480.getId(), "video/480p_dash", manifest, period, videoAdaptationSet);
        this.addDashRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), fmp4Muxing360.getId(), "video/360p_dash", manifest, period, videoAdaptationSet);
        this.addDashRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), fmp4Muxing240.getId(), "video/240p_dash", manifest, period, videoAdaptationSet);

        this.addDashRepresentationToAdaptationSet(DashMuxingType.TEMPLATE, encoding.getId(), fmp4Audio.getId(), "audio/128kbps_dash", manifest, period, audioAdaptationSet);

        }
        
        bitmovinApi.manifest.dash.startGeneration(manifest);
        Status dashStatus = bitmovinApi.manifest.dash.getGenerationStatus(manifest);
        while (dashStatus != Status.FINISHED && dashStatus != Status.ERROR)
        {
            dashStatus = bitmovinApi.manifest.dash.getGenerationStatus(manifest);
            Thread.sleep(2500);
        }
        if (dashStatus != Status.FINISHED)
        {
            System.out.println("Could not create DASH manifest");
            Assert.fail("Could not create DASH manifest");
        }
        System.out.println("Creating HLS manifest");
        HlsManifest manifestHls = this.createHlsManifest("manifest.m3u8", manifestDestination);

        MediaInfo audioMediaInfo = new MediaInfo();
        if (ENCRYPTION_FLAG) {
        audioMediaInfo.setName("audio.m3u8");
        audioMediaInfo.setUri("audio.m3u8");
        audioMediaInfo.setGroupId("audio");
        audioMediaInfo.setType(MediaInfoType.AUDIO);
        audioMediaInfo.setEncodingId(encoding.getId());
        audioMediaInfo.setStreamId(audioStream.getId());
        audioMediaInfo.setMuxingId(tsAudio.getId());
        audioMediaInfo.setDrmId(audioFairPlayDRM.getId());
        audioMediaInfo.setLanguage("en");
        audioMediaInfo.setAssocLanguage("en");
        audioMediaInfo.setAutoselect(true);
        audioMediaInfo.setIsDefault(true);
        audioMediaInfo.setForced(false);
        audioMediaInfo.setSegmentPath("audio/128kbps_hls/fairplay_drm");
        bitmovinApi.manifest.hls.createMediaInfo(manifestHls, audioMediaInfo);

        this.addStreamInfoToHlsFPManifest("video_240p.m3u8", encoding.getId(), videoStream240p.getId(), tsMuxing240.getId(), videoFairPlayDRM240p.getId(), audioMediaInfo.getGroupId(), "video/240p_hls/fairplay_drm", manifestHls);
        this.addStreamInfoToHlsFPManifest("video_360p.m3u8", encoding.getId(), videoStream360p.getId(), tsMuxing360.getId(), videoFairPlayDRM360p.getId(), audioMediaInfo.getGroupId(),"video/360p_hls/fairplay_drm", manifestHls);
        this.addStreamInfoToHlsFPManifest("video_480p.m3u8", encoding.getId(), videoStream480p.getId(), tsMuxing480.getId(), videoFairPlayDRM480p.getId(), audioMediaInfo.getGroupId(),"video/480p_hls/fairplay_drm", manifestHls);
        this.addStreamInfoToHlsFPManifest("video_720p.m3u8", encoding.getId(), videoStream720p.getId(), tsMuxing720.getId(), videoFairPlayDRM720p.getId(), audioMediaInfo.getGroupId(),"video/720p_hls/fairplay_drm", manifestHls);
        this.addStreamInfoToHlsFPManifest("video_1080p.m3u8", encoding.getId(), videoStream1080p.getId(), tsMuxing1080.getId(), videoFairPlayDRM1080p.getId(), audioMediaInfo.getGroupId(), "video/1080p_hls/fairplay_drm", manifestHls);

        } else {
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
    
            this.addStreamInfoToHlsManifest("video_1080p.m3u8", encoding.getId(), videoStream1080p.getId(), tsMuxing1080.getId(), "audio", "video/1080p_hls", manifestHls);
            this.addStreamInfoToHlsManifest("video_720p.m3u8", encoding.getId(), videoStream720p.getId(), tsMuxing720.getId(), "audio", "video/720p_hls", manifestHls);
            this.addStreamInfoToHlsManifest("video_480p.m3u8", encoding.getId(), videoStream480p.getId(), tsMuxing480.getId(), "audio", "video/480p_hls", manifestHls);
            this.addStreamInfoToHlsManifest("video_360p.m3u8", encoding.getId(), videoStream360p.getId(), tsMuxing360.getId(), "audio", "video/360p_hls", manifestHls);
            this.addStreamInfoToHlsManifest("video_240p.m3u8", encoding.getId(), videoStream240p.getId(), tsMuxing240.getId(), "audio", "video/240p_hls", manifestHls);
        }
        
        bitmovinApi.manifest.hls.startGeneration(manifestHls);
        Status hlsStatus = bitmovinApi.manifest.hls.getGenerationStatus(manifestHls);
        while (hlsStatus != Status.FINISHED && hlsStatus != Status.ERROR)
        {
            hlsStatus = bitmovinApi.manifest.hls.getGenerationStatus(manifestHls);
            Thread.sleep(2500);
        }
        if (hlsStatus != Status.FINISHED)
        {
            System.out.println("Could not create HLS manifest");
            Assert.fail("Could not create HLS manifest");
        }

        System.out.println("Encoding completed successfully");
    }

    private StreamInfo addStreamInfoToHlsManifest(String uri, String encodingId, String streamId, String muxingId,
                                                  String audioGroupId, String segmentPath, HlsManifest manifest) throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException {
        StreamInfo s = new StreamInfo();
        s.setUri(uri);
        s.setEncodingId(encodingId);
        s.setStreamId(streamId);
        s.setMuxingId(muxingId);
        s.setAudio(audioGroupId);
        s.setSegmentPath(segmentPath);
        s = bitmovinApi.manifest.hls.createStreamInfo(manifest, s);
        return s;
    }

    private StreamInfo addStreamInfoToHlsFPManifest(String uri, String encodingId, String streamId, String muxingId,
                                                  String drmId, String audioGroupId, String segmentPath,
                                                  HlsManifest manifest)
            throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException
    {
        StreamInfo s = new StreamInfo();
        s.setUri(uri);
        s.setEncodingId(encodingId);
        s.setStreamId(streamId);
        s.setMuxingId(muxingId);
        s.setDrmId(drmId);
        s.setAudio(audioGroupId);
        s.setSegmentPath(segmentPath);
        s = bitmovinApi.manifest.hls.createStreamInfo(manifest, s);
        return s;
    }


    private HlsManifest createHlsManifest(String name, EncodingOutput output)
            throws URISyntaxException, BitmovinApiException, UnirestException, IOException
    {
        HlsManifest m = new HlsManifest();
        m.setName(name);
        m.addOutput(output);
        return bitmovinApi.manifest.hls.create(m);
    }

    private DashDRMRepresentation addDashDRMRepresentationToAdaptationSet(DashMuxingType type, String encodingId,
                                                                          String streamId, String muxingId, String drmId,
                                                                          String segmentPath, DashManifest manifest,
                                                                          Period period, AdaptationSet adaptationSet)
            throws BitmovinApiException, URISyntaxException, RestException, UnirestException, IOException
    {
        DashDRMRepresentation r = this.getDashDRMRepresentation(type, encodingId, streamId, muxingId, drmId, segmentPath);
        return bitmovinApi.manifest.dash.addDrmRepresentationToAdaptationSet(manifest, period, adaptationSet, r);
    }

    private DashDRMRepresentation getDashDRMRepresentation(DashMuxingType type, String encodingId, String streamId,
                                                           String muxingId, String drmId, String segmentPath)
    {
        DashDRMRepresentation r = new DashDRMRepresentation();
        r.setType(type);
        r.setEncodingId(encodingId);
        r.setStreamId(streamId);
        r.setMuxingId(muxingId);
        r.setDrmId(drmId);
        r.setSegmentPath(segmentPath);
        return r;
    }

    private AudioAdaptationSet addAudioAdaptationSetToPeriodWithRoles(DashManifest manifest, Period period, String lang)
            throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException
    {
        AudioAdaptationSet a = new AudioAdaptationSet();
        a.setLang(lang);
        a = bitmovinApi.manifest.dash.addAudioAdaptationSetToPeriod(manifest, period, a);
        return a;
    }

    private VideoAdaptationSet addVideoAdaptationSetToPeriod(DashManifest manifest, Period period)
            throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException
    {
        VideoAdaptationSet adaptationSet = new VideoAdaptationSet();
        adaptationSet = bitmovinApi.manifest.dash.addVideoAdaptationSetToPeriod(manifest, period, adaptationSet);
        return adaptationSet;
    }

    private DashManifest createDashManifest(String name, EncodingOutput output)
            throws URISyntaxException, BitmovinApiException, UnirestException, IOException
    {
        DashManifest manifest = new DashManifest();
        manifest.setName(name);
        manifest.addOutput(output);
        manifest = bitmovinApi.manifest.dash.create(manifest);
        return manifest;
    }

    private Period addPeriodToDashManifest(DashManifest manifest)
            throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException
    {
        Period period = new Period();
        period = bitmovinApi.manifest.dash.createPeriod(manifest, period);
        return period;
    }

    private FMP4Muxing createFMP4MuxingNoOutput(Encoding encoding, Stream stream)
            throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException
    {
        FMP4Muxing muxing = new FMP4Muxing();
        MuxingStream list = new MuxingStream();
        list.setStreamId(stream.getId());
        muxing.addStream(list);
        muxing.setSegmentLength(4.0);
        muxing = bitmovinApi.encoding.muxing.addFmp4MuxingToEncoding(encoding, muxing);
        return muxing;
    }

    private TSMuxing createTSMuxingNoOutput(Encoding encoding, Stream stream)
            throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException
    {
        TSMuxing muxing = new TSMuxing();
        MuxingStream list = new MuxingStream();
        list.setStreamId(stream.getId());
        muxing.addStream(list);
        muxing.setSegmentLength(4.0);
        muxing = bitmovinApi.encoding.muxing.addTSMuxingToEncoding(encoding, muxing);
        return muxing;
    }

    private CencDrm getCencDRMWithWidevineAndPlayready() {
        CencPlayReady cencPlayReady = this.getCencPlayReady();
        CencWidevine cencWidevine = this.getCencWidevine();
        CencDrm cencDrm = new CencDrm();

        cencDrm.setKey(CENC_KEY);
        cencDrm.setKid(CENC_KID);
        cencDrm.setWidevine(cencWidevine);
        cencDrm.setPlayReady(cencPlayReady);
        return cencDrm;
    }

    private CencWidevine getCencWidevine() {
        CencWidevine cencWidevine = new CencWidevine();
        cencWidevine.setPssh(WIDEVINE_PSSH);
        return cencWidevine;
    }

    private CencPlayReady getCencPlayReady() {
        CencPlayReady cencPlayReady = new CencPlayReady();
        cencPlayReady.setLaUrl(PLAYREADY_LAURL);
        return cencPlayReady;
    }

    private FairPlayDrm getFairPlayDRM()
    {
        FairPlayDrm fairPlayDrm = new FairPlayDrm();
        fairPlayDrm.setKey(FAIRPLAY_KEY);
        fairPlayDrm.setIv(FAIRPLAY_IV);
        fairPlayDrm.setUri(FAIRPLAY_URI);
        return fairPlayDrm;
    }

    private void addOutputToDRM(Drm drm, Output output, String outputPath)
    {
        List<EncodingOutput> drmOutputs = drm.getOutputs();

        if (drmOutputs == null)
        {
            drmOutputs = new ArrayList<>();
            drm.setOutputs(drmOutputs);
        }

        EncodingOutput drmOutput = new EncodingOutput();
        drmOutput.setOutputId(output.getId());
        drmOutput.setOutputPath(outputPath);
        drmOutputs.add(drmOutput);
    }

    private CencDrm addCencDrmToFmp4Muxing(Encoding encoding, FMP4Muxing fmp4Muxing, CencDrm cencDrm)
            throws BitmovinApiException, IOException, RestException, UnirestException, URISyntaxException
    {
        CencDrm result = bitmovinApi.encoding.muxing.addCencDrmToFmp4Muxing(encoding, fmp4Muxing, cencDrm);
        Assert.assertNotNull(result.getId());
        fmp4Muxing.getDrmConfigs().add(result);
        return result;
    }

    private FairPlayDrm addFairPlayDrmToTssMuxing(Encoding encoding, TSMuxing tsMuxing, FairPlayDrm fairplayDrm)
            throws BitmovinApiException, IOException, RestException, UnirestException, URISyntaxException
    {
        FairPlayDrm result = bitmovinApi.encoding.muxing.addFairPlayDrmToTssMuxing(encoding, tsMuxing, fairplayDrm);
        Assert.assertNotNull(result.getId());
        tsMuxing.getDrmConfigs().add(result);
        return result;
    }

    private ContentProtection addContentProtectionToDRMfMP4Representation(DashManifest manifestDash,
                                                                          Period period,
                                                                          AdaptationSet adaptationSet,
                                                                          DashFmp4Representation representation,
                                                                          ContentProtection contentProtection)
            throws BitmovinApiException, IOException, RestException, UnirestException, URISyntaxException
    {
        return bitmovinApi.manifest.dash.addContentProtectionToDRMfMP4Representation(manifestDash,
                period,
                adaptationSet,
                representation,
                contentProtection);
    }

    private ContentProtection getContentProtection(String encodingId, String streamId, String muxingId, String drmId)
    {
        ContentProtection contentProtection = new ContentProtection();
        contentProtection.setEncodingId(encodingId);
        contentProtection.setStreamId(streamId);
        contentProtection.setMuxingId(muxingId);
        contentProtection.setDrmId(drmId);
        return contentProtection;
    }

    private FMP4Muxing createFMP4Muxing(Encoding encoding, Stream stream, Output output, String outputPath, AclPermission defaultAclPermission)
throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException {
EncodingOutput encodingOutput = this.createEncodingOutput(output, outputPath, defaultAclPermission);
FMP4Muxing muxing = new FMP4Muxing();
muxing.addOutput(encodingOutput);
MuxingStream list = new MuxingStream();
list.setStreamId(stream.getId());
muxing.addStream(list);
muxing.setSegmentLength(4.0);
muxing = bitmovinApi.encoding.muxing.addFmp4MuxingToEncoding(encoding, muxing);
return muxing;
}

private TSMuxing createTSMuxing(Encoding encoding, Stream stream, Output output, String outputPath, AclPermission defaultAclPermission)
throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException {
EncodingOutput encodingOutput = this.createEncodingOutput(output, outputPath, defaultAclPermission);
TSMuxing muxing = new TSMuxing();
muxing.addOutput(encodingOutput);
MuxingStream list = new MuxingStream();
list.setStreamId(stream.getId());
muxing.addStream(list);
muxing.setSegmentLength(4.0);
muxing = bitmovinApi.encoding.muxing.addTSMuxingToEncoding(encoding, muxing);
return muxing;
}

private EncodingOutput createEncodingOutput(Output output, String outputPath, AclPermission defaultAclPermission) {
    EncodingOutput encodingOutput = new EncodingOutput();
    encodingOutput.setOutputPath(outputPath);
    encodingOutput.setOutputId(output.getId());

    if (output.getAcl() != null && output.getAcl().size() > 0) {
        encodingOutput.setAcl(output.getAcl());
    } else {
        ArrayList<AclEntry> aclEntries = new ArrayList<>();
        aclEntries.add(new AclEntry(defaultAclPermission));
        encodingOutput.setAcl(aclEntries);
    }

    return encodingOutput;
}

private void addDashRepresentationToAdaptationSet(DashMuxingType type, String encodingId, String muxingId,
String segmentPath, DashManifest manifest, Period period,
AdaptationSet adaptationSet) throws BitmovinApiException, URISyntaxException, RestException, UnirestException, IOException {
DashFmp4Representation r = new DashFmp4Representation();
r.setType(type);
r.setEncodingId(encodingId);
r.setMuxingId(muxingId);
r.setSegmentPath(segmentPath);
bitmovinApi.manifest.dash.addRepresentationToAdaptationSet(manifest, period, adaptationSet, r);
}
}

