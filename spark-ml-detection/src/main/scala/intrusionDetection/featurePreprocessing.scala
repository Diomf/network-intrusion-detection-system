package intrusionDetection

import common._
import intrusionDetection.utility.asDense
import org.apache.spark.ml.{Pipeline, PipelineStage}
import org.apache.spark.ml.feature.{ChiSqSelector, ChiSqSelectorModel, OneHotEncoder, QuantileDiscretizer, StringIndexer, VectorAssembler, VectorSlicer}
import org.apache.spark.ml.stat.ChiSquareTest
import org.apache.spark.sql._
import org.apache.spark.sql.functions.col
import org.apache.spark.ml.linalg.DenseVector

object featurePreprocessing {

  def indexCategoricalKdd(): StringIndexer = {

    //Categorical features ---> Numerical
    val categoricalIndexedCols = categoricalCols.map(_ + "_Indexed")

    val indexer = new StringIndexer()
      .setInputCols(categoricalCols)
      .setOutputCols(categoricalIndexedCols)
      .setHandleInvalid("keep")

//    //Numerical categories ---> OneHotEncoded
//    val categoricalEncodedCols = categoricalCols.map(_ + "_Encoded")
//    val encoder = new OneHotEncoder()
//      .setInputCols(categoricalIndexedCols)
//      .setOutputCols(categoricalEncodedCols)
//      .setHandleInvalid("keep")

    indexer
  }

  def discretizeLargeContinuousKdd(): QuantileDiscretizer = {

    val discretizedLargeContinuousCols = largeContinuousCols.map(_ + "_Discretized")

    val discretizer =  new QuantileDiscretizer()
      .setInputCols(largeContinuousCols)
      .setOutputCols(discretizedLargeContinuousCols)
      .setNumBuckets(10000)

    discretizer
  }

  def assembleKdd(df: DataFrame, stages: Array[_ <: PipelineStage], featureCols: Array[String]): DataFrame = {

    val assembler = new VectorAssembler()
      .setInputCols(featureCols)
      .setOutputCol("features")

    val pipeline = new Pipeline()
      .setStages(stages :+ assembler)

    val resultDf = pipeline.fit(df).transform(df)
    resultDf
  }

  def applyChiSqSelection1(df: DataFrame): ChiSqSelectorModel = {

    val chiSqTest = ChiSquareTest.test(df, "features", "label_Indexed").head
    val pValues = chiSqTest.getAs[DenseVector](0)

    //We begin with the lowest pValue as threshold and increase it till we get at least half of the features
    var pValueThreshold = pValues.values.min
    val half_of_Total_Features = (pValues.size * 0.5).toInt

    //Select at least half of total features to minimize elimination of possible crucial features
    while (pValues.values.count(_ <= pValueThreshold) < half_of_Total_Features) {
      pValueThreshold += 0.001
    }

    val selector = new ChiSqSelector()
      .setSelectorType("fpr")
      .setFpr(pValueThreshold + 0.000001) //Add a small number to include the threshold in the chosen values
      .setFeaturesCol("features")
      .setLabelCol("label_Indexed")
      .setOutputCol("selectedFeatures")

    val chiSqModel = selector.fit(df)
    val importantFeatures = chiSqModel.selectedFeatures
    println(s"Selected ${importantFeatures.size} out of ${pValues.size} total features which are: \n " +
      s"${importantFeatures.mkString("(", ", ", ")")}")

//    val resultDf = chiSqModel.transform(df)
//    result.select(col("features"), col("selectedFeatures"))
//      .withColumn("features", asDense(col("features")))
//      .withColumn("selectedFeatures", asDense(col("selectedFeatures")))
//      .show(5, false)

    chiSqModel
  }

  def applyChiSqSelection2(df: DataFrame): VectorSlicer  = {

    val chiSqTest = ChiSquareTest.test(df, "features", "label_Indexed").head
    val pValues = chiSqTest.getAs[DenseVector](0)
    val degreesOfFreedom = chiSqTest.getSeq[Int](1)//.mkString(", ")
    val chiSqStats = chiSqTest.getAs[DenseVector](2)

    println(s"pValues = $pValues \n")
    println(s"Degrees of Freedom = $degreesOfFreedom \n")
    println(s"Chi squared stats = $chiSqStats \n")

    val mappedStats = chiSqStats.values.zipWithIndex
    val sortedStats = mappedStats.sortBy(_._1).reverse
    println(sortedStats.mkString(", "))

    val selectedFeatures = sortedStats.map(_._2).take(7).sorted
    println(s"Selected ${selectedFeatures.mkString(",")}")

    val chiSqModel = new VectorSlicer()
      .setIndices(selectedFeatures)
      .setInputCol("features")
      .setOutputCol("selectedFeatures")

    chiSqModel
    }

}
