package org.nlp4l.framework.processors

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.asScalaSet
import scala.collection.mutable
import scala.concurrent.Await

import com.typesafe.config.ConfigFactory

import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import org.nlp4l.framework.dao.JobDAO
import org.nlp4l.framework.models.Dictionary

class ValidatorChain (val chain: List[Validator]) {
  private val logger = Logger(this.getClass)
  
  def process(dic: Dictionary): Seq[String] = {
    var errMsg: Seq[String] = Seq()
    def loop(li: List[Validator], data:Option[Dictionary] = None): Unit = li match {
      case Nil => ()
      case head :: Nil =>
        val out: Tuple2[Boolean, Seq[String]] = head.validate(data)
        if(!out._1) errMsg = errMsg union out._2
      case head :: tail =>
        val out: Tuple2[Boolean, Seq[String]] = head.validate(data)
        if(!out._1) errMsg = errMsg union out._2
        loop(tail, data)
    }
    loop(chain, Some(dic))
    errMsg
  }
}

object ValidatorChain {

  // Processor
  private var mapP: Map[Int, ValidatorChain] = null
  def chainMap: Map[Int, ValidatorChain] = mapP
  
  def loadChain(jobDAO: JobDAO, jobId: Int): Unit = {
    jobDAO.get(jobId).map(
        job => 
           mapP += (jobId -> new ValidatorChainBuilder().build(job.config).result())
    )
  }
  
  def getChain(jobDAO: JobDAO, jobId: Int): ValidatorChain = {
    val job = Await.result(jobDAO.get(jobId), scala.concurrent.duration.Duration.Inf)
    new ValidatorChainBuilder().build(job.config).result()
  }
}


class ValidatorChainBuilder() {
  val logger = Logger(this.getClass)
  val buf = mutable.ArrayBuffer[Validator]()

  def build(confStr: String): ValidatorChainBuilder = {
    val config = ConfigFactory.parseString(confStr)

    val v = config.getConfigList("validators")
    v.foreach {
      pConf =>
        try {
          val className = pConf.getString("class")
          val constructor = Class.forName(className).getConstructor(classOf[Map[String, String]])
          val settings = pConf.getConfig("settings").entrySet().map(f => f.getKey -> f.getValue.unwrapped()).toMap
          val facP = constructor.newInstance(settings).asInstanceOf[ValidatorFactory]
          val p:Validator = facP.getInstance()
          buf += p
        } catch {
          case e: Exception => logger.error(e.getMessage)
        }
    }
    this
  }

  def result() = new ValidatorChain(buf.toList)
}
