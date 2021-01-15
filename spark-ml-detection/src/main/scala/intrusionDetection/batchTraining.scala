package intrusionDetection

import common._
import intrusionDetection.featurePreprocessing._
import intrusionDetection.utility._
import org.apache.spark.ml.classification.{DecisionTreeClassifier, RandomForestClassifier}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.slf4j.LoggerFactory

object batchTraining {
  def main(args: Array[String]): Unit = {

    val logger = LoggerFactory.getLogger("batchTraining")

    val spark = SparkSession.builder()
      .appName("Model Training")
      .master("local[*]")
      .getOrCreate()

    val trainFile = "/home/diomfeas/Desktop/Diplomatiki/Datasets/NSL-KDD/KDDTrain+.txt"
    val chiSqLocation = "/home/diomfeas/Desktop/Diplomatiki/Models"
    val dtLocation = "/home/diomfeas/Desktop/Diplomatiki/Models"
    val rfLocation = "/home/diomfeas/Desktop/Diplomatiki/Models"

    /*******************************
    ** Training file preparation ***
    ********************************/

    val trainDF = spark.read
      .schema(kddDataSchema)
      .csv(trainFile)

    //Drop score row and rows with null or nan values
    val cleanTrainDF = trainDF
      .drop(col("score"))//.drop(trainDF.columns.last)
      .na.drop("any")

    //Replace labels with their attack group name
    val replacedLabelsTrainDF = cleanTrainDF.withColumn("label", categorizeKdd5Labels(col("label")))
      .cache()

    //Create String indexer for categorical kdd features
    val stringIndexer = indexCategoricalKdd()

    //Create Discretizer for large continuous kdd features for Chi Square Selection
    val discretizer = discretizeLargeContinuousKdd()

    //Assemble features for model training (without discretization)
    //and for chi square selection (with discretization)
    val stages = Array(stringIndexer)
    val stagesForChiSq = Array(stringIndexer, discretizer)
    val assembledChiSqTrainDF = assembleKdd(replacedLabelsTrainDF, stagesForChiSq, assembledForSelectionCols)
    val assembledTrainDF = assembleKdd(replacedLabelsTrainDF, stages, assembledCols)

    //Chi Squared feature selection
    val chiSqModel = applyChiSqSelection2(assembledChiSqTrainDF)
    chiSqModel.write.overwrite().save(chiSqLocation + "/chiSqModel")

    //Select features with the chiSq model
    val selectedTrainDF = chiSqModel.transform(assembledTrainDF)

    /********************************
     ******** Model training ********
     ********************************/

    //Decision tree training
    val dt = new DecisionTreeClassifier()
      .setLabelCol("label_Indexed")
      .setFeaturesCol("selectedFeatures")
      //.setFeaturesCol("features")
      .setMaxBins(72)
    val dtModel = dt.fit(selectedTrainDF)
    dtModel.write.overwrite().save(dtLocation + "/dtModel")

    //Random forest training
    val rf = new RandomForestClassifier()
      .setLabelCol("label_Indexed")
      .setFeaturesCol("selectedFeatures")
      //.setFeaturesCol("features")
      .setMaxBins(72)
    val rfModel = rf.fit(selectedTrainDF)
    rfModel.write.overwrite().save(rfLocation + "/rfModel")

    spark.stop()
  }

}
