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
package com.baidu.duer.dcs.framework;

import com.baidu.duer.dcs.systeminterface.IMediaPlayer;
import com.baidu.duer.dcs.systeminterface.IPlatformFactory;
import com.baidu.duer.dcs.util.LogUtil;

import java.util.Iterator;
import java.util.Map;

/**
 * 暂停模式的播放控制策略
 * <p>
 * Created by guxiuzhong@baidu.com on 2017/6/6.
 */
public class PauseStrategyMultiChannelMediaPlayer extends BaseMultiChannelMediaPlayer {
    private static final String TAG = BaseMultiChannelMediaPlayer.class.getSimpleName();

    public PauseStrategyMultiChannelMediaPlayer(IPlatformFactory factory) {
        super(factory);
    }

    @Override
    protected void handlePlay(String channelName, IMediaPlayer.MediaResource mediaResource) {
        int priority = getPriorityByChannelName(channelName);
        LogUtil.d(TAG, "handlePlay-priority:" + priority);
        if (priority == UNKNOWN_PRIORITY) {
            return;
        }
        ChannelMediaPlayerInfo info = new ChannelMediaPlayerInfo();
        info.mediaPlayer = getMediaPlayer(channelName);
        info.priority = priority;
        info.channelName = channelName;
        info.mediaResource = mediaResource;
        currentPlayMap.put(priority, info);

        // 把比当前优先级低的播放器都暂停
        Iterator<Map.Entry<Integer, ChannelMediaPlayerInfo>> it = currentPlayMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ChannelMediaPlayerInfo> entry = it.next();
            ChannelMediaPlayerInfo value = entry.getValue();
            LogUtil.d(TAG, "handlePlay-value:" + value.priority);
            if (priority > value.priority) {
                LogUtil.d(TAG, "handlePlay-value-pause:" + value.priority);
                value.mediaPlayer.pause();
            }
        }
        findToPlay();
    }
}
