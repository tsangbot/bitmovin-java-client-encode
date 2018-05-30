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
import com.bitmovin.api.encoding.encodings.conditions.AbstractCondition;
import com.bitmovin.api.encoding.encodings.conditions.AndConjunction;
import com.bitmovin.api.encoding.encodings.conditions.Condition;
import com.bitmovin.api.encoding.encodings.conditions.ConditionAttribute;
import com.bitmovin.api.encoding.encodings.muxing.MP4Muxing;
import com.bitmovin.api.encoding.encodings.muxing.MuxingStream;
import com.bitmovin.api.encoding.encodings.streams.Stream;
import com.bitmovin.api.encoding.enums.CloudRegion;
import com.bitmovin.api.encoding.enums.StreamSelectionMode;
import com.bitmovin.api.encoding.inputs.HttpsInput;
import com.bitmovin.api.encoding.outputs.Output;
import com.bitmovin.api.encoding.outputs.S3Output;
import com.bitmovin.api.encoding.status.Task;
import com.bitmovin.api.enums.Status;
import com.bitmovin.api.exceptions.BitmovinApiException;
import com.bitmovin.api.http.RestException;
import com.bitmovin.api.webhooks.Webhook;
import com.bitmovin.api.webhooks.enums.WebhookHttpMethod;
import com.bitmovin.api.webhooks.enums.WebhookType;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Roland Kersche on 30.05.17.
 */
public class CreateEncodingWithMP4MuxingAndConditionsOnS3
{

    private static String API_KEY = "<INSERT_YOUR_APIKEY>";

    private static CloudRegion CLOUD_REGION = CloudRegion.AWS_EU_WEST_1;
    private static String HTTPS_INPUT_HOST = "<INSERT_YOUR_HTTP_HOST>"; // ex.: storage.googleapis.com/
    private static String HTTPS_INPUT_PATH = "<INSERT_YOUR_PATH_TO_INPUT_FILE>";
    private static String S3_OUTPUT_ACCESSKEY = "<INSERT_YOUR_ACCESSKEY>";
    private static String S3_OUTPUT_SECRET_KEY = "<INSERT_YOUR_SECRETKEY>";
    private static String S3_OUTPUT_BUCKET_NAME = "BUCKET_NAME";
    private static String OUTPUT_BASE_PATH = "path/to/your/outputs/" + new Date().getTime();
    private static String NOTIFICATION_URL = "<INSERT_YOUR_NOTIFICATION_URL>";
    private static double INPUT_FPS = 23.98;
    private static double KEYFRAME_INTERVAL = 0.5;


    private static BitmovinApi bitmovinApi;

    @Test
    public void testEncoding() throws IOException, BitmovinApiException, UnirestException, URISyntaxException, RestException, InterruptedException
    {
        int gop = (int) Math.round(INPUT_FPS / KEYFRAME_INTERVAL);

        bitmovinApi = new BitmovinApi(API_KEY);
        Encoding encoding = new Encoding();
        encoding.setName("Encoding JAVA");
        encoding.setCloudRegion(CLOUD_REGION);
        encoding = bitmovinApi.encoding.create(encoding);

        HttpsInput input = new HttpsInput();
        input.setHost(HTTPS_INPUT_HOST);
        input = bitmovinApi.input.https.create(input);

        S3Output output = new S3Output();
        output.setAccessKey(S3_OUTPUT_ACCESSKEY);
        output.setSecretKey(S3_OUTPUT_SECRET_KEY);
        output.setBucketName(S3_OUTPUT_BUCKET_NAME);
        output = bitmovinApi.output.s3.create(output);

        AACAudioConfig aacConfiguration = new AACAudioConfig();
        aacConfiguration.setBitrate(96000L);
        aacConfiguration.setRate(48000f);
        aacConfiguration = bitmovinApi.configuration.audioAAC.create(aacConfiguration);

        H264VideoConfiguration videoConfiguration240p = new H264VideoConfiguration();
        videoConfiguration240p.setHeight(240);
        videoConfiguration240p.setBitrate(195000L);
        videoConfiguration240p.setProfile(ProfileH264.BASELINE);
        videoConfiguration240p.setMinGop(gop);
        videoConfiguration240p.setMaxGop(gop);
        videoConfiguration240p = bitmovinApi.configuration.videoH264.create(videoConfiguration240p);

        H264VideoConfiguration videoConfiguration360p = new H264VideoConfiguration();
        videoConfiguration360p.setHeight(360);
        videoConfiguration360p.setBitrate(750000L);
        videoConfiguration360p.setProfile(ProfileH264.MAIN);
        videoConfiguration360p.setMinGop(gop);
        videoConfiguration360p.setMaxGop(gop);
        videoConfiguration360p = bitmovinApi.configuration.videoH264.create(videoConfiguration360p);

        H264VideoConfiguration videoConfiguration480p = new H264VideoConfiguration();
        videoConfiguration480p.setHeight(480);
        videoConfiguration480p.setBitrate(1750000L);
        videoConfiguration480p.setProfile(ProfileH264.MAIN);
        videoConfiguration480p.setMinGop(gop);
        videoConfiguration480p.setMaxGop(gop);
        videoConfiguration480p = bitmovinApi.configuration.videoH264.create(videoConfiguration480p);

        H264VideoConfiguration videoConfiguration720p = new H264VideoConfiguration();
        videoConfiguration720p.setHeight(720);
        videoConfiguration720p.setBitrate(3000000L);
        videoConfiguration720p.setProfile(ProfileH264.HIGH);
        videoConfiguration720p.setMinGop(gop);
        videoConfiguration720p.setMaxGop(gop);
        videoConfiguration720p = bitmovinApi.configuration.videoH264.create(videoConfiguration720p);

        H264VideoConfiguration videoConfiguration1080p = new H264VideoConfiguration();
        videoConfiguration1080p.setHeight(1080);
        videoConfiguration1080p.setBitrate(4500000L);
        videoConfiguration1080p.setProfile(ProfileH264.HIGH);
        videoConfiguration1080p.setMinGop(gop);
        videoConfiguration1080p.setMaxGop(gop);
        videoConfiguration1080p = bitmovinApi.configuration.videoH264.create(videoConfiguration1080p);

        InputStream inputStreamVideo = new InputStream();
        inputStreamVideo.setInputPath(HTTPS_INPUT_PATH);
        inputStreamVideo.setInputId(input.getId());
        inputStreamVideo.setSelectionMode(StreamSelectionMode.VIDEO_RELATIVE);
        inputStreamVideo.setPosition(0);

        InputStream inputStreamAudio = new InputStream();
        inputStreamAudio.setInputPath(HTTPS_INPUT_PATH);
        inputStreamAudio.setInputId(input.getId());
        inputStreamAudio.setSelectionMode(StreamSelectionMode.AUDIO_RELATIVE);
        inputStreamAudio.setPosition(0);

        Stream videoStream240p = new Stream();
        videoStream240p.setCodecConfigId(videoConfiguration240p.getId());
        videoStream240p.setInputStreams(Collections.singleton(inputStreamVideo));
        AndConjunction andConjunction240 = new AndConjunction();
        andConjunction240.setConditions(new ArrayList<AbstractCondition>() {{ new Condition(ConditionAttribute.HEIGHT, ">=", "240"); }});
        videoStream240p.setConditions(andConjunction240);
        videoStream240p = bitmovinApi.encoding.stream.addStream(encoding, videoStream240p);

        Stream videoStream360p = new Stream();
        videoStream360p.setCodecConfigId(videoConfiguration360p.getId());
        videoStream360p.setInputStreams(Collections.singleton(inputStreamVideo));
        AndConjunction andConjunction360 = new AndConjunction();
        andConjunction360.setConditions(new ArrayList<AbstractCondition>() {{ new Condition(ConditionAttribute.HEIGHT, ">=", "360"); }});
        videoStream360p.setConditions(andConjunction360);
        videoStream360p = bitmovinApi.encoding.stream.addStream(encoding, videoStream360p);

        Stream videoStream480p = new Stream();
        videoStream480p.setCodecConfigId(videoConfiguration480p.getId());
        videoStream480p.setInputStreams(Collections.singleton(inputStreamVideo));
        AndConjunction andConjunction480 = new AndConjunction();
        andConjunction480.setConditions(new ArrayList<AbstractCondition>() {{ new Condition(ConditionAttribute.HEIGHT, ">=", "480"); }});
        videoStream480p.setConditions(andConjunction480);
        videoStream480p = bitmovinApi.encoding.stream.addStream(encoding, videoStream480p);

        Stream videoStream720p = new Stream();
        videoStream720p.setCodecConfigId(videoConfiguration720p.getId());
        videoStream720p.setInputStreams(Collections.singleton(inputStreamVideo));
        AndConjunction andConjunction720 = new AndConjunction();
        andConjunction720.setConditions(new ArrayList<AbstractCondition>() {{ new Condition(ConditionAttribute.HEIGHT, ">=", "720"); }});
        videoStream720p.setConditions(andConjunction720);
        videoStream720p = bitmovinApi.encoding.stream.addStream(encoding, videoStream720p);

        Stream videoStream1080p = new Stream();
        videoStream1080p.setCodecConfigId(videoConfiguration1080p.getId());
        videoStream1080p.setInputStreams(Collections.singleton(inputStreamVideo));
        AndConjunction andConjunction1080 = new AndConjunction();
        andConjunction1080.setConditions(new ArrayList<AbstractCondition>() {{ new Condition(ConditionAttribute.HEIGHT, ">=", "1080"); }});
        videoStream1080p.setConditions(andConjunction1080);
        videoStream1080p = bitmovinApi.encoding.stream.addStream(encoding, videoStream1080p);

        Stream audioStream = new Stream();
        audioStream.setCodecConfigId(aacConfiguration.getId());
        audioStream.setInputStreams(Collections.singleton(inputStreamAudio));
        audioStream = bitmovinApi.encoding.stream.addStream(encoding, audioStream);

        this.createMP4Muxing(encoding, output, videoStream240p, audioStream, "video_audio_240p.mp4");
        this.createMP4Muxing(encoding, output, videoStream360p, audioStream, "video_audio_360p.mp4");
        this.createMP4Muxing(encoding, output, videoStream480p, audioStream, "video_audio_480p.mp4");
        this.createMP4Muxing(encoding, output, videoStream720p, audioStream, "video_audio_720p.mp4");
        this.createMP4Muxing(encoding, output, videoStream1080p, audioStream, "video_audio_1080p.mp4");

        this.createWebHook(encoding);

        bitmovinApi.encoding.start(encoding);

        Task status = bitmovinApi.encoding.getStatus(encoding);

        while (status.getStatus() != Status.FINISHED && status.getStatus() != Status.ERROR)
        {
            status = bitmovinApi.encoding.getStatus(encoding);
            Thread.sleep(2500);
        }

        System.out.println(String.format("Encoding finished with status %s", status.getStatus().toString()));

    }

    private void createWebHook(Encoding encoding) throws URISyntaxException, BitmovinApiException, RestException, UnirestException, IOException
    {
        Webhook webhook = new Webhook();
        webhook.setUrl(NOTIFICATION_URL);
        webhook.setMethod(WebhookHttpMethod.POST);
        bitmovinApi.notifications.webhooks.create(webhook, WebhookType.ENCODING_FINISHED, encoding.getId());
        bitmovinApi.notifications.webhooks.create(webhook, WebhookType.ENCODING_ERROR, encoding.getId());
    }

    private void createMP4Muxing(Encoding encoding, Output output, Stream videoStream240p, Stream audioStream, String filename) throws BitmovinApiException, IOException, RestException, URISyntaxException, UnirestException
    {
        EncodingOutput encodingOutput = new EncodingOutput();
        encodingOutput.setOutputId(output.getId());
        encodingOutput.setOutputPath(OUTPUT_BASE_PATH);
        encodingOutput.setAcl(new ArrayList<AclEntry>()
        {{
            this.add(new AclEntry(AclPermission.PUBLIC_READ));
        }});
        MP4Muxing mp4Muxing = new MP4Muxing();
        mp4Muxing.setFilename(filename);
        mp4Muxing.setOutputs(Collections.singletonList(encodingOutput));
        List<MuxingStream> muxingStreams = new ArrayList<>();
        MuxingStream muxingStreamVideo = new MuxingStream();
        muxingStreamVideo.setStreamId(videoStream240p.getId());
        MuxingStream muxingStreamAudio = new MuxingStream();
        muxingStreamAudio.setStreamId(audioStream.getId());
        muxingStreams.add(muxingStreamVideo);
        muxingStreams.add(muxingStreamAudio);
        mp4Muxing.setStreams(muxingStreams);
        bitmovinApi.encoding.muxing.addMp4MuxingToEncoding(encoding, mp4Muxing);
    }

}
