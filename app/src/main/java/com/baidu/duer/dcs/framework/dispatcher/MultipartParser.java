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

import com.baidu.duer.dcs.framework.message.DcsResponseBody;
import com.baidu.duer.dcs.http.HttpConfig;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

/**
 * 把从服务器返回multipart中json对象和二进制解析成directive对象
 * <p>
 * Created by wuruisheng on 2017/5/12.
 */
public class MultipartParser extends Parser {
    private static final int BUFFER_SIZE = 512;
    private final IMultipartParserListener multipartParserListener;

    public MultipartParser(IMultipartParserListener listener) {
        this.multipartParserListener = listener;
    }

    public void parseResponse(Response response) throws IOException {
        String boundary = getBoundary(response);
        if (boundary != null) {
            parseStream(response.body().byteStream(), boundary);
        }
    }

    private void parseStream(InputStream inputStream, String boundary) throws IOException {
        MultipartStream multipartStream = new MultipartStream(inputStream, boundary.getBytes(), BUFFER_SIZE, null);
        parseMultipartStream(multipartStream);
    }

    private void parseMultipartStream(MultipartStream multipartStream) throws IOException {
        try {
            Boolean hasNextPart = multipartStream.skipPreamble();
            while (hasNextPart) {
                handlePart(multipartStream);
                hasNextPart = multipartStream.readBoundary();
            }
        } catch (DcsJsonProcessingException exception) {
            if (multipartParserListener != null) {
                multipartParserListener.onParseFailed(exception.getUnparsedCotent());
            }
        } catch (MultipartStream.MalformedStreamException exception) {
            // 用于处理 empty part
        }
    }

    private void handlePart(MultipartStream multipartStream) throws IOException {
        Map<String, String> headers = getPartHeaders(multipartStream);
        if (headers != null) {
            byte[] partBytes = getPartBytes(multipartStream);
            Boolean isJsonData = isPartJSON(headers);
            if (isJsonData) {
                handleJsonData(partBytes);
            } else {
                handleAudio(headers, partBytes);
            }
        }
    }

    private void handleJsonData(byte[] partBytes) throws IOException {
        final DcsResponseBody responseBody = parse(partBytes, DcsResponseBody.class);
        if (multipartParserListener != null) {
            multipartParserListener.onResponseBody(responseBody);
        }
    }

    private void handleAudio(Map<String, String> headers, byte[] partBytes) {
        String contentId = getMultipartContentId(headers);
        final AudioData audioData = new AudioData(contentId, partBytes);
        if (multipartParserListener != null) {
            multipartParserListener.onAudioData(audioData);
        }
    }

    private byte[] getPartBytes(MultipartStream multipartStream) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        multipartStream.readBodyData(data);
        return data.toByteArray();
    }

    private Map<String, String> getPartHeaders(MultipartStream multipartStream) throws IOException {
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
    }
}
