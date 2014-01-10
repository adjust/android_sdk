/*
 * Copyright (c) 2012 adeven GmbH, http://www.adeven.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.adeven.adjustio;

/**
 * @author keyboardsurfer
 * @since 8.11.13
 */
public interface Constants {
    int ONE_SECOND     = 1000;
    int ONE_MINUTE     = 60 * ONE_SECOND;
    int THIRTY_MINUTES = 30 * ONE_MINUTE;

    String BASE_URL   = "https://app.adjust.io";
    String CLIENT_SDK = "android2.1.5";
    String LOGTAG     = "AdjustIo";

    String SESSION_STATE_FILENAME    = "AdjustIoActivityState";
    String NO_ACTIVITY_HANDLER_FOUND = "No activity handler found";

    String UNKNOWN   = "unknown";
    String MALFORMED = "malformed";
    String SMALL     = "small";
    String NORMAL    = "normal";
    String LONG      = "long";
    String LARGE     = "large";
    String XLARGE    = "xlarge";
    String LOW       = "low";
    String MEDIUM    = "medium";
    String HIGH      = "high";
    String REFERRER  = "referrer";

    String ENCODING = "UTF-8";
    String MD5      = "MD5";
    String SHA1     = "SHA-1";
}
