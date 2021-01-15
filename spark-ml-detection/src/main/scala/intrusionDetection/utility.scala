package intrusionDetection

import common._
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.{col, regexp_replace, udf}
import org.apache.spark.ml.linalg.{DenseVector, SparseMatrix, SparseVector}

object utility {

  def categorizeKdd2Labels(label: Column): Column =
    regexp_replace(col("label"), "^(?!normal).*$", "attack")

  val categorizeKdd5Labels = udf((label:String) => {
    if (dosType(label))
      "dos"
    else if (probeType(label))
      "probe"
    else if (u2rType(label))
      "u2r"
    else if (r2lType(label))
      "r2l"
    else
      "normal"
  })

//  val selectFeatures = udf((features: DenseVector) => {
//    val total_Indices = features.values[]
//  })

  val asDense = udf((v: SparseVector) => v.toDense)

  //how many nulls in each column
  //select(trainingData.columns.map(c => sum(col(c).isNull.cast("int")).alias(c)): _*)
}

