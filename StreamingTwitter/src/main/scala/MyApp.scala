import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

/*
* This app is developped to get tweets from TwitterAPI and process with Spark Streaming
* */
object MyApp {

  def main(args: Array[String]): Unit = {
    //credentials
    val apiKey = ""
    val apiSecretKey = ""
    val accessToken = ""
    val accessTokenSecret = ""

    //set authentication credentials for Twitter API
    System.setProperty("twitter4j.oauth.consumerKey", apiKey)
    System.setProperty("twitter4j.oauth.consumerSecret", apiSecretKey)
    System.setProperty("twitter4j.oauth.accessToken", accessToken)
    System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret)

    //Set Some configs for the memory, appName, ...
    val sparkConf = new SparkConf().setAppName("StreamingTweets").setMaster("local[4]")
    sparkConf.set("spark.executor.memory", "4g")
    sparkConf.set("spark.storage.memoryFraction", "0.8")
    sparkConf.set("spark.driver.memory", "2g")
    sparkConf.set("spark.driver.allowMultipleContexts", "true")

    //create instance of sparkContext
    val sc = new SparkContext(sparkConf)
    //create instance of sparkStreaming with the config created and with 2s batchDuration
    val ssc = new StreamingContext(sc, Seconds(1))

    //to avoid lot of logs, show only errors
    sc.setLogLevel("ERROR")

    //get the stream from the api
    val stream = TwitterUtils.createStream(ssc, None)
    //stream.print()
    
    //get the most influencers of the stream based on the numbers of followers
    stream.filter(_.getUser.getFollowersCount >= 10000)
      .map(status => (status.getUser.getScreenName, status.getUser.getFollowersCount))
      .map { case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))
      .foreachRDD(_.take(20).foreach{ case (count, userName) => println("%s (%s followers)".format(userName, count)) })


    ssc.start()
    ssc.awaitTermination()
  }
}
