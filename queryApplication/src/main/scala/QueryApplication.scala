import com.typesafe.config.Config
import org.apache.spark.sql.SQLContext
import spark.jobserver.{SparkJobInvalid, SparkJobValid, SparkJobValidation, SparkSqlJob}

object QueryApplication extends SparkSqlJob {

  def runJob(sc: SQLContext, jobConfig: Config): Any = {
    val df = sc
      .read
      .format("com.databricks.spark.csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(jobConfig.getString("path"))
    df.registerTempTable(jobConfig.getString("tablename"))
    val result = sc.sql(jobConfig.getString("query"))
    val columns = result.columns
    val rows = result.collect()
    Map(
      "names" -> columns,
      "values" -> rows.map(_.toSeq.map({ thing => // this is bad
        if (thing == null) "null" else thing.toString
      }))
    )
  }

  def validate(sc: SQLContext, config: Config): SparkJobValidation =
    if(config.hasPath("query") && config.hasPath("tablename") && config.hasPath("path"))
      SparkJobValid
    else
      SparkJobInvalid("needs keys query, tablename, and path")

}
