/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bikefleetmonitoring;

import com.google.android.gms.location.DetectedActivity;

/**
 * Constants used in this sample.
 */
final class Constants {

    private Constants() {}

    private static final String PACKAGE_NAME =
            "com.google.android.gms.location.activityrecognition";

    static final String KEY_ACTIVITY_UPDATES_REQUESTED = PACKAGE_NAME +
            ".ACTIVITY_UPDATES_REQUESTED";

    static final String KEY_DETECTED_ACTIVITIES = PACKAGE_NAME + ".DETECTED_ACTIVITIES";

    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate.
     */
    static final long DETECTION_INTERVAL_IN_MILLISECONDS = 0;
    /**
     * List of DetectedActivity types that we monitor in this sample.
     */
    static final int[] MONITORED_ACTIVITIES = {
            DetectedActivity.WALKING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.UNKNOWN
    };
}
