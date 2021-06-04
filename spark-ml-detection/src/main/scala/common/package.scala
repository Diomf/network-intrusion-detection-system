import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}

package object common {

  val dosType = Set("apache2","back","land","neptune","mailbomb","pod","processtable",
    "smurf","teardrop","udpstorm","worm")
  val probeType = Set("ipsweep","mscan","nmap","portsweep","saint","satan")
  val u2rType = Set("buffer_overflow","loadmodule","perl","ps","rootkit","sqlattack","xterm")
  val r2lType = Set("ftp_write","guess_passwd","httptunnel","imap","multihop","named",
    "phf","sendmail","snmpgetattack","spy","snmpguess","warezclient","warezmaster","xlock","xsnoop")

  val categoricalCols = Array("protocol_type","service","flag","label")

  val largeContinuousCols = Array("duration","src_bytes","dst_bytes")

  //"features" for Selector
  val assembledForSelectionCols = Array("duration_Discretized","protocol_type_Indexed","service_Indexed","flag_Indexed",
    "src_bytes_Discretized","dst_bytes_Discretized","land","wrong_fragment","urgent","hot","num_failed_logins",
    "logged_in","num_compromised","root_shell","su_attempted","num_root",
    "num_file_creations","num_shells","num_access_files","num_outbound_cmds",
    "is_host_login","is_guest_login","count","srv_count","serror_rate",
    "srv_serror_rate","rerror_rate","srv_rerror_rate","same_srv_rate",
    "diff_srv_rate","srv_diff_host_rate","dst_host_count","dst_host_srv_count",
    "dst_host_same_srv_rate","dst_host_diff_srv_rate","dst_host_same_src_port_rate",
    "dst_host_srv_diff_host_rate","dst_host_serror_rate","dst_host_srv_serror_rate",
    "dst_host_rerror_rate","dst_host_srv_rerror_rate")

  //"features for ML Model
  val assembledCols = Array("duration","protocol_type_Indexed","service_Indexed","flag_Indexed",
    "src_bytes","dst_bytes","land","wrong_fragment","urgent","hot","num_failed_logins",
    "logged_in","num_compromised","root_shell","su_attempted","num_root",
    "num_file_creations","num_shells","num_access_files","num_outbound_cmds",
    "is_host_login","is_guest_login","count","srv_count","serror_rate",
    "srv_serror_rate","rerror_rate","srv_rerror_rate","same_srv_rate",
    "diff_srv_rate","srv_diff_host_rate","dst_host_count","dst_host_srv_count",
    "dst_host_same_srv_rate","dst_host_diff_srv_rate","dst_host_same_src_port_rate",
    "dst_host_srv_diff_host_rate","dst_host_serror_rate","dst_host_srv_serror_rate",
    "dst_host_rerror_rate","dst_host_srv_rerror_rate")

  val outputCols = Array("duration","protocol_type","service","flag","src_bytes","dst_bytes","label",
    "num_failed_logins","logged_in","num_compromised","count","predictedLabel","timestamp")

  val kddDataSchema = StructType(Array(
    StructField("duration",DoubleType),
    StructField("protocol_type",StringType),
    StructField("service",StringType),
    StructField("flag",StringType),
    StructField("src_bytes",DoubleType),
    StructField("dst_bytes",DoubleType),
    StructField("land",DoubleType),
    StructField("wrong_fragment",DoubleType),
    StructField("urgent",DoubleType),
    StructField("hot",DoubleType),
    StructField("num_failed_logins",DoubleType),
    StructField("logged_in",DoubleType),
    StructField("num_compromised",DoubleType),
    StructField("root_shell",DoubleType),
    StructField("su_attempted",DoubleType),
    StructField("num_root",DoubleType),
    StructField("num_file_creations",DoubleType),
    StructField("num_shells",DoubleType),
    StructField("num_access_files",DoubleType),
    StructField("num_outbound_cmds",DoubleType),
    StructField("is_host_login",DoubleType),
    StructField("is_guest_login",DoubleType),
    StructField("count",DoubleType),
    StructField("srv_count",DoubleType),
    StructField("serror_rate",DoubleType),
    StructField("srv_serror_rate",DoubleType),
    StructField("rerror_rate",DoubleType),
    StructField("srv_rerror_rate",DoubleType),
    StructField("same_srv_rate",DoubleType),
    StructField("diff_srv_rate",DoubleType),
    StructField("srv_diff_host_rate",DoubleType),
    StructField("dst_host_count",DoubleType),
    StructField("dst_host_srv_count",DoubleType),
    StructField("dst_host_same_srv_rate",DoubleType),
    StructField("dst_host_diff_srv_rate",DoubleType),
    StructField("dst_host_same_src_port_rate",DoubleType),
    StructField("dst_host_srv_diff_host_rate",DoubleType),
    StructField("dst_host_serror_rate",DoubleType),
    StructField("dst_host_srv_serror_rate",DoubleType),
    StructField("dst_host_rerror_rate",DoubleType),
    StructField("dst_host_srv_rerror_rate",DoubleType),
    StructField("label",StringType),
    StructField("score",DoubleType)
  ))
}
