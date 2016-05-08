package com.luminous.pick.utils;

/**
 * Created by rahul on 14/9/15.
 * Copyright (c) 2015 Rental Host All rights reserved.
 */
public enum VideoQuality {
    HIGH_QUALITY(1), LOW_QUALITY(0);

    int quality;

    VideoQuality(int i) {
        quality = i;
    }

    public int getQuality() {
        return quality;
    }
}
