variable "users" {
  description = "Список пользователей для Kafka"
  type = list(object({
    name       = string
    password   = optional(string)
    permissions = optional(list(object({
      topic_name  = string
      role        = string
      allow_hosts = optional(list(string))
    })))
  }))
  default = []
}

variable "topics" {
  type = list(object({
    name               = string
    partitions         = number
    replication_factor = number
    topic_config = object({
      cleanup_policy        = string
      compression_type      = string
      delete_retention_ms   = number
      file_delete_delay_ms  = number
      flush_messages        = number
      flush_ms              = number
      min_compaction_lag_ms = number
      retention_bytes       = number
      retention_ms          = number
      max_message_bytes     = number
      min_insync_replicas   = number
      segment_bytes         = number
    })
  }))
  default = []
}