package com.idvp.elections.client

import java.io.IOException
import java.net.ProxySelector
import java.nio.file._
import javax.annotation.{PostConstruct, PreDestroy}

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.{HttpResponseException, ResponseHandler}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder, StandardHttpRequestRetryHandler}
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpEntity, HttpResponse}
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
  * @author Oleg Zinoviev
  * @since 01.03.18.
  */
//noinspection VarCouldBeVal
@Component
class ClientImpl extends Client {

    private val logger = LoggerFactory.getLogger(classOf[Client])

    @Value("${com.idvp.elections.source.uri}")
    private var sourceUri: String = _

    @Value("${com.idvp.elections.source.retry:3}")
    private var sourceRetry: Int = 3

    private var client: CloseableHttpClient = _

    @PostConstruct
    def init(): Unit = {
        client = HttpClientBuilder.create()
            .setRetryHandler(new StandardHttpRequestRetryHandler(sourceRetry, true))
            .setRoutePlanner(new SystemDefaultRoutePlanner(ProxySelector.getDefault))
            .setMaxConnPerRoute(8)
            .build()
    }

    @PreDestroy
    def close(): Unit = {
        if (client != null) {
            client.close()
        }
    }

    override def download(): Option[Path] = {
        val request = new HttpGet(sourceUri)
        client.execute(request, new FileResponseHandler())
    }

    class FileResponseHandler extends ResponseHandler[Option[Path]] {

        @throws[HttpResponseException]
        @throws[IOException]
        override def handleResponse(response: HttpResponse): Option[Path] = {
            val statusLine = response.getStatusLine
            val entity = response.getEntity
            if (statusLine.getStatusCode >= 300) {
                EntityUtils.consume(entity)
                logger.error(s"Ошибка при выполнении запроса: ${statusLine.getStatusCode} ${statusLine.getReasonPhrase}")
                None
            }

            if (entity == null) {
                None
            } else {
                handleEntity(entity)
            }
        }

        @throws(classOf[IOException])
        def handleEntity(entity: HttpEntity): Option[Path] = {
            val tempPath = Files.createTempFile("elections", null)

            val stream = Files.newOutputStream(tempPath,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING)
            try {
                entity.writeTo(stream)
            } catch {
                case e: Exception =>
                    logger.error("Ощибка при записи файла", e)
                    return None
            } finally {
                stream.close()
            }

            Some(tempPath)
        }
    }

}
