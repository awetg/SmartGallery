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
const val PYTORCH_MOBILENET_V3_SMALL = "mobileNet_v3_small.pt"
const val PYTORCH_YOLO5_MODEL = "yolov5s.torchscript.ptl"


// job types for worker and input key
const val DATA_INPUT_KEY_JOB_TYPE = "job_type"

const val JOB_TYPE_ALL = "job_type_all"
const val JOB_TYPE_UPDATE = "job_type_update"
const val JOB_TYPE_PARTIAL = "job_type_partial"

const val LOG_TAG = "smartGallery"

// dirs
const val FACES_DIR = "faces"
const val CLUSTER_DIR = "cluster"

// media group types
const val ALBUM_GROUP = "albums"
const val CLUSTER_GROUP = "clusters"
const val STRING_SEARCH_GROUP = "string"