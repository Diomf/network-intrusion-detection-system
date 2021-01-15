package intrusionDetection
import common._
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.feature.{OneHotEncoder}

object playground {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("hjhj")
      .master("local[*]")
      .getOrCreate()

    val df = spark.createDataFrame(Seq(
      (0.0, 1.0),
      (1.0, 0.0),
      (2.0, 1.0),
      (0.0, 2.0),
      (0.0, 1.0),
      (2.0, 0.0)
    )).toDF("categoryIndex1", "categoryIndex2")

    val encoder = new OneHotEncoder()
      .setInputCols(Array("categoryIndex1", "categoryIndex2"))
      .setOutputCols(Array("categoryVec1", "categoryVec2"))
    val model = encoder.fit(df)

    val encoded = model.transform(df)
    encoded.show()
  }
}
