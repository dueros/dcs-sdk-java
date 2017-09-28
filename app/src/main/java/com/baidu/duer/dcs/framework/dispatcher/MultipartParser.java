/*
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.duer.dcs.framework.dispatcher;

import android.util.Log;

import com.baidu.dcs.okhttp3.Response;
import com.baidu.dcs.okhttp3.internal.http2.StreamResetException;
import com.baidu.duer.dcs.framework.decoder.IDecoder;
import com.baidu.duer.dcs.framework.message.DcsResponseBody;
import com.baidu.duer.dcs.framework.DcsStream;
import com.baidu.duer.dcs.http.HttpConfig;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 把从服务器返回multipart中json对象和二进制解析成directive对象
 * <p>
 * Created by wuruisheng on 2017/5/12.
 */
public class MultipartParser extends Parser {
    private static final String TAG = "MultipartParser";
    private static final int BUFFER_SIZE = 8192;
    private final IDecoder decoder;
    private final IMultipartParserListener multipartParserListener;
    private static final byte[] HEARTBEAT_BODY = new byte[]{0x7b, 0x7d, 0x0d, 0x0a};
    private static final byte[] EMPTY_PART = new byte[]{0x0d, 0x0a};

    public MultipartParser(IDecoder decoder, IMultipartParserListener listener) {
        this.decoder = decoder;
        if (this.decoder == null) {
            throw new NullPointerException("decoder is null.");
        }
        this.multipartParserListener = listener;
    }

    public synchronized void parseResponse(Response response) throws IOException {
        String boundary = getBoundary(response);
        if (boundary != null) {
            if (HttpConfig.HTTP_VOICE_TAG.equals(response.request().tag())) {
                parseStream(new ResponseWrapInputStream(response.body().byteStream()), boundary);
            } else {
                parseStream(response.body().byteStream(), boundary);
            }
        }
    }

    private void parseStream(InputStream inputStream, String boundary) throws IOException {
        MultipartStreamCopy multipartStream =
                new MultipartStreamCopy(inputStream, boundary.getBytes(), BUFFER_SIZE, null);
        parseMultipartStream(multipartStream);
    }

    private void parseMultipartStream(MultipartStreamCopy multipartStream) throws IOException {
        try {
            boolean hasNextPart = multipartStream.skipPreamble();
            while (hasNextPart) {
                handlePart(multipartStream);
                hasNextPart = multipartStream.readBoundary();
            }
        } catch (DcsJsonProcessingException exception) {
            if (multipartParserListener != null) {
                multipartParserListener.onParseFailed(exception.getUnparsedCotent());
            }
        } catch (MultipartStreamCopy.MalformedStreamException exception) {
            fireOnClose();
        } catch (StreamResetException streamResetException) {
            fireOnClose();
        }
    }

    private void fireOnClose() {
        if (multipartParserListener != null) {
            multipartParserListener.onClose();
        }
    }

    private void handlePart(MultipartStreamCopy multipartStream) throws IOException {
        Map<String, String> headers = getPartHeaders(multipartStream);
        if (headers != null) {
            if (isPartJSON(headers)) {
                long start = System.currentTimeMillis();
                byte[] partBytes = getPartBytes(multipartStream);
                String content = new String(partBytes);
                Log.d(TAG, "jsonContent: \r\n" + content);
                handleJsonData(partBytes);
                Log.d(TAG, "json parse:" + (System.currentTimeMillis() - start));
            } else if (isOctetStream(headers)) {
                handleAudio(headers, multipartStream);
            }
        }
    }

    private void handleJsonData(byte[] partBytes) throws IOException {
        if (multipartParserListener != null) {
            if (Arrays.equals(HEARTBEAT_BODY, partBytes)) {
                multipartParserListener.onHeartBeat();
            } else if (Arrays.equals(EMPTY_PART, partBytes)) {
                // empty part
            } else {
                final DcsResponseBody responseBody = parse(partBytes, DcsResponseBody.class);
                multipartParserListener.onResponseBody(responseBody);
            }
        }
    }

    private IDecoder.IDecodeListener decodeListener;

    private void handleAudio(Map<String, String> headers, MultipartStreamCopy multipartStream)
            throws IOException {
        synchronized (decoder) {
        String contentId = getMultipartContentId(headers);
        final DcsStream dcsStream = new DcsStream();
        InputStream inputStream = multipartStream.newInputStream();
        try {
            final AudioData audioData = new AudioData(contentId, dcsStream);
            if (multipartParserListener != null) {
                multipartParserListener.onAudioData(audioData);
            }
            decodeListener = new IDecoder.IDecodeListener() {
                @Override
                public void onDecodeInfo(int sampleRate, int channels) {
                    Log.d(TAG, "Decoder-onDecodeInfo-sampleRate=" + sampleRate);
                    Log.d(TAG, "Decoder-onDecodeInfo-channels=" + channels);
                    dcsStream.sampleRate = sampleRate;
                    dcsStream.channels = channels;
                }

                @Override
                public void onDecodePcm(byte[] pcmData) {
                    dcsStream.dataQueue.add(pcmData);
                }

                @Override
                public void onDecodeFinished() {
                    dcsStream.isFin = true;
                    decoder.removeOnDecodeListener(this);
                }
            };
            decoder.addOnDecodeListener(decodeListener);
            decoder.decode(new WrapInputStream(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Decoder-handleAudio Exception: ", e);
            dcsStream.isFin = true;
            if (decodeListener != null) {
                decoder.removeOnDecodeListener(decodeListener);
                }
            }
        }
    }

    private byte[] getPartBytes(MultipartStreamCopy multipartStream) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        multipartStream.readBodyData(data);
        return data.toByteArray();
    }

    private Map<String, String> getPartHeaders(MultipartStreamCopy multipartStream) throws IOException {
        String headers = multipartStream.readHeaders();
        BufferedReader reader = new BufferedReader(new StringReader(headers));
        Map<String, String> headerMap = new HashMap<>();
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                if (!StringUtils.isBlank(line) && line.contains(":")) {
                    int colon = line.indexOf(":");
                    String headerName = line.substring(0, colon).trim();
                    String headerValue = line.substring(colon + 1).trim();
                    headerMap.put(headerName.toLowerCase(), headerValue);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return headerMap;
    }

    private String getMultipartHeaderValue(Map<String, String> headers, String searchHeader) {
        return headers.get(searchHeader.toLowerCase());
    }

    private String getMultipartContentId(Map<String, String> headers) {
        String contentId = getMultipartHeaderValue(headers, HttpConfig.HttpHeaders.CONTENT_ID);
        contentId = contentId.substring(1, contentId.length() - 1);
        return contentId;
    }

    private boolean isPartJSON(Map<String, String> headers) {
        String contentType = getMultipartHeaderValue(headers, HttpConfig.HttpHeaders.CONTENT_TYPE);
        return StringUtils.contains(contentType, HttpConfig.ContentTypes.JSON);
    }

    private boolean isOctetStream(Map<String, String> headers) {
        String contentType = getMultipartHeaderValue(headers, HttpConfig.HttpHeaders.CONTENT_TYPE);
        return StringUtils.contains(contentType, HttpConfig.ContentTypes.APPLICATION_AUDIO);
    }

    private static String getBoundary(Response response) {
        String headerValue = response.header(HttpConfig.HttpHeaders.CONTENT_TYPE);
        String boundary = getHeaderParameter(headerValue, HttpConfig.Parameters.BOUNDARY);
        return boundary;
    }

    private static String getHeaderParameter(final String headerValue, final String key) {
        if ((headerValue == null) || (key == null)) {
            return null;
        }

        String[] parts = headerValue.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith(key)) {
                return part.substring(key.length() + 1).replaceAll("(^\")|(\"$)", "").trim();
            }
        }

        return null;
    }

    public interface IMultipartParserListener {
        void onResponseBody(DcsResponseBody responseBody);

        void onAudioData(AudioData audioData);

        void onParseFailed(String unParseMessage);

        void onHeartBeat();

        void onClose();
    }
}
