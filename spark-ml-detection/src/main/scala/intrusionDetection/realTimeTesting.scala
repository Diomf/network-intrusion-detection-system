package intrusionDetection

import common._
import intrusionDetection.featurePreprocessing.{assembleKdd, indexCategoricalKdd}
import intrusionDetection.utility.{asDense, categorizeKdd2Labels, categorizeKdd5Labels}
import org.apache.spark.ml.classification.{DecisionTreeClassificationModel, RandomForestClassificationModel}
import org.apache.spark.ml.evaluation.{BinaryClassificationEvaluator, MulticlassClassificationEvaluator}
import org.apache.spark.ml.feature.{ChiSqSelectorModel, IndexToString, VectorSlicer}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, conv}
import org.slf4j.LoggerFactory

object realTimeTesting {
  def main(args: Array[String]): Unit = {

    val logger = LoggerFactory.getLogger("batchTraining")

    val spark = SparkSession.builder()
      .appName("Model Testing")
      .master("local[*]")
      .getOrCreate()

    val testFile = "/home/diomfeas/Desktop/Diplomatiki/Datasets/NSL-KDD/KDDTest+.txt"
    val chiSqLocation = "/home/diomfeas/Desktop/Diplomatiki/Models/chiSqModel"
    val dtLocation = "/home/diomfeas/Desktop/Diplomatiki/Models/dtModel"
    val rfLocation = "/home/diomfeas/Desktop/Diplomatiki/Models/rfModel"

    //val chiSqModel = ChiSqSelectorModel.load(chiSqLocation)
    val chiSqModel = VectorSlicer.load(chiSqLocation)
    val dtModel = DecisionTreeClassificationModel.load(dtLocation)
    val rfModel = RandomForestClassificationModel.load(rfLocation)

    /******************************
     ** Testing file preparation ***
     ******************************/
    val testDF = spark.read
      .schema(kddDataSchema)
      .csv(testFile)

    //Drop score row and rows with null or nan values
    val cleanTestDF = testDF
      .drop(col("score"))//.drop(testDF.columns.last)
      .na.drop("any")

    //Replace labels with their attack group name
    val replacedLabelsTestDF = cleanTestDF.withColumn("label", categorizeKdd5Labels(col("label")))

    //Create String indexer for categorical kdd features
    val stringIndexer = indexCategoricalKdd()

    //Assemble features for model testing
    val stages = Array(stringIndexer)
    val assembledTestDF = assembleKdd(replacedLabelsTestDF, stages, assembledCols)

    //Select features with the chiSq model
    val selectedTestDF = chiSqModel.transform(assembledTestDF)
      .cache()

    /******************************
     ******* Model Testing ********
     ******************************/

    val labelConverter = new IndexToString()
      .setInputCol("prediction")
      .setOutputCol("predictedLabel")
      .setLabels(stringIndexer.fit(replacedLabelsTestDF).labelsArray(3))

    //Decision Tree testing
    val dtPredictions = dtModel.transform(selectedTestDF)
    val convertedDtPredictions = labelConverter.transform(dtPredictions)

    convertedDtPredictions.select(col("label_Indexed"), col("prediction"),col("label"),col("predictedLabel"))
      .show(10, false)

    //Random Forest Testing
    val rfPredictions = rfModel.transform(selectedTestDF)
    val convertedRfPredictions = labelConverter.transform(rfPredictions)

    convertedRfPredictions.select(col("label_Indexed"), col("prediction"),col("label"),col("predictedLabel"))
      .show(10, false)

    //Evaluation of models
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("accuracy")
      .setLabelCol("label_Indexed")
      .setPredictionCol("prediction")

    val accuracyDt = evaluator.evaluate(dtPredictions)
    println(s"Decision Tree accuracy = $accuracyDt")

    val accuracyRf = evaluator.evaluate(rfPredictions)
    println(s"Random Forest accuracy = $accuracyRf")

    spark.stop()
  }

}
