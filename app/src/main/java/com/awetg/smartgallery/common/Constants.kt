package com.awetg.smartgallery.common


// notification constants
const val FOREGROUND_WORK_NOTIFICATION_CHANNEL_ID = "foreground_channel"
const val FOREGROUND_WORK_NOTIFICATION_ID = 111

// media scan worker input and output keys
const val DATA_INPUT_KEY_MEDIA_SCAN_TYPE = "scan_type"
const val DATA_INPUT_KEY_MEDIA_COUNT = "last_media_count"
const val DATA_OUTPUT_KEY_MEDIA_COUNT = "media_count_output"

const val DATA_KEY_NEW_MEDIAS = "new_media_items"
const val DATA_KEY_DELETED_MEDIAS = "deleted_media_items"

// media scan types
const val MEDIA_SCAN_TYPE_SYNC = "sync"
const val MEDIA_SCAN_TYPE_RE_SYNC = "re_sync"
const val MEDIA_SCAN_TYPE_UPDATE = "update"

const val FACE_NET_ASSET_NAME = "facenet_128.tflite"


// face cluster worker input and output keys
const val DATA_INPUT_KEY_FACE_CLUSTER_TYPE = "cluster_job_type"

const val FACE_CLUSTER_JOB_ALL = "cluster_all_media"
const val FACE_CLUSTER_JOB_UPDATE = "update_cluster"
const val FACE_CLUSTER_JOB_PARTIAL = "cluster_chunks"

const val LOG_TAG = "smartGallery"

// dirs
const val FACES_DIR = "faces"
const val CLUSTER_DIR = "cluster"

// media group types
const val ALBUM_GROUP = "albums"
const val CLUSTER_GROUP = "clusters"