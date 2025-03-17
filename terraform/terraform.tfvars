users = [
  {
    name        = "producer"
    password    = "kafkaSuperPuper"
    permissions = [
      {
        topic_name  = "*"
        role        = "ACCESS_ROLE_PRODUCER"
        allow_hosts = ["0.0.0.0/0"]
      }
    ]
  },
  {
    name        = "consumer"
    password    = "kafkaSuperPuper"
    permissions = [
      {
        topic_name  = "*"
        role        = "ACCESS_ROLE_CONSUMER"
        allow_hosts = ["0.0.0.0/0"]
      }
    ]
  }
]
topics = [
    {
      name               = "my_topic"
      partitions         = 3
      replication_factor = 3
      topic_config = {
        cleanup_policy        = "CLEANUP_POLICY_COMPACT"
        compression_type      = "COMPRESSION_TYPE_LZ4"
        delete_retention_ms   = 86400000
        file_delete_delay_ms  = 60000
        flush_messages        = 10000
        flush_ms              = 1000
        min_compaction_lag_ms = 5000
        retention_bytes       = 104857600
        retention_ms          = 604800000
        max_message_bytes     = 1048576
        min_insync_replicas   = 2
        segment_bytes         = 1073741824
      }
    },
    {
      name               = "nifi_topic"
      partitions         = 3
      replication_factor = 3
      topic_config = {
        cleanup_policy        = "CLEANUP_POLICY_COMPACT"
        compression_type      = "COMPRESSION_TYPE_LZ4"
        delete_retention_ms   = 86400000
        file_delete_delay_ms  = 60000
        flush_messages        = 10000
        flush_ms              = 1000
        min_compaction_lag_ms = 5000
        retention_bytes       = 104857600
        retention_ms          = 604800000
        max_message_bytes     = 1048576
        min_insync_replicas   = 2
        segment_bytes         = 1073741824
      }
    }
]
