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
package com.baidu.duer.dcs.androidsystemimpl.player;

import android.os.Handler;

import com.baidu.duer.dcs.util.FileUtil;
import com.baidu.duer.dcs.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 将stream的mp3流保存到文件中
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/5/17.
 */
public class AudioStoreThread extends Thread {
    private static final String TAG = AudioStoreThread.class.getSimpleName();
    private static final int BUFFER_SIZE = 8192;
    private volatile boolean mThreadExitFlag;
    private InputStream mInputStream;
    private Handler mHandler = new Handler();

    private File file;
    private File completedFile;
    private FileOutputStream mOutputStream;

    public AudioStoreThread(InputStream is) {
        this.mInputStream = is;
    }

    @Override
    public void run() {
        super.run();
        try {
            file = FileUtil.getSpeakFile();
            if (file != null) {
                LogUtil.d(TAG, "AudioStoreThread  file： " + file.getAbsolutePath());
                this.mOutputStream = new FileOutputStream(file);
            } else {
                if (mOnDownListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mOnDownListener.onDownError(
                                        new JSONObject("AudioStoreThread  create temp file failed!"));
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "AudioStoreThread  FileNotFoundException ");
        }
        if (mOnDownListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mOnDownListener.onDownStart();
                }
            });
        }
        byte[] audioData = new byte[BUFFER_SIZE];
        int readBytes;
        try {
            while (!mThreadExitFlag && (readBytes = mInputStream.read(audioData)) != -1) {
                LogUtil.d(TAG, "readBytes=" + readBytes);
                mOutputStream.write(audioData, 0, readBytes);
            }
            mOutputStream.flush();
            LogUtil.d(TAG, "AudioStoreThread  ok ");
        } catch (final IOException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "AudioStoreThread  write error ", e);
            if (mOnDownListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mOnDownListener.onDownError(new JSONObject("AudioStoreThread  write error :"
                                    + e.getMessage()));
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        } finally {
            try {
                String fileName = file.getName().substring(0, file.getName().length()
                        - FileUtil.TEMP_POSTFIX.length());
                LogUtil.d(TAG, "AudioStoreThread  fileName : " + fileName);
                completedFile = new File(file.getParentFile(), fileName);
                boolean renamed = file.renameTo(completedFile);
                if (!renamed) {
                    if (mOnDownListener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mOnDownListener.onDownError(new JSONObject("Error renaming file "
                                            + file
                                            + "to"
                                            + completedFile
                                            + " for completion!"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
                mOutputStream.close();
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mOnDownListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.d(TAG, "completedFile  path： " + completedFile.getAbsolutePath());
                        mOnDownListener.onDownComplete(completedFile.getAbsolutePath());
                    }
                });
            }
        }
    }

    /**
     * 停止保存/下载
     */
    public void stopDown() {
        LogUtil.d(TAG, "stopDown");
        mThreadExitFlag = true;
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 删除文件
     *
     * @return 是否删除成功
     */
    public boolean delDownFile() {
        LogUtil.d(TAG, "delDownFile");
        if (completedFile != null && completedFile.exists()) {
            return completedFile.delete();
        }
        return false;
    }

    public static class SimpleOnDownListener implements OnDownListener {
        @Override
        public void onDownStart() {
        }

        @Override
        public void onDownComplete(String path) {
        }

        @Override
        public void onDownError(JSONObject jsonObject) {
        }
    }

    public OnDownListener mOnDownListener;

    public void setOnDownListener(OnDownListener listener) {
        this.mOnDownListener = listener;
    }

    /**
     * 流保存回调接口
     */
    public interface OnDownListener {
        void onDownStart();

        void onDownComplete(String path);

        void onDownError(JSONObject jsonObject);
    }
}
