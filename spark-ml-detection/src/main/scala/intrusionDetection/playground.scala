package intrusionDetection
import common._
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.feature.OneHotEncoder
import org.apache.spark.ml.linalg.{DenseVector, Vectors}

object playground {
  def main(args: Array[String]): Unit = {


    val ena = Seq(1,3,5,6,2,5)
    val dio = Vectors.dense(1.0,3.0,4.0,5.0,1.0,3.0).toDense

    val mazi = ena.zip(dio.values).toMap

//    val spark = SparkSession.builder()
//      .appName("hjhj")
//      .master("local[*]")
//      .getOrCreate()
//
//    val df = spark.createDataFrame(Seq(
//      (0.0, 1.0),
//      (1.0, 0.0),
//      (2.0, 1.0),
//      (0.0, 2.0),
//      (0.0, 1.0),
//      (2.0, 0.0)
//    )).toDF("categoryIndex1", "categoryIndex2")
//
//    val encoder = new OneHotEncoder()
//      .setInputCols(Array("categoryIndex1", "categoryIndex2"))
//      .setOutputCols(Array("categoryVec1", "categoryVec2"))
//    val model = encoder.fit(df)
//
//    val encoded = model.transform(df)
//    encoded.show()
  }
}
