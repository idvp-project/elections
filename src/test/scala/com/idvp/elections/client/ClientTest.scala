package com.idvp.elections.client

import com.idvp.elections.ApplicationConfig
import org.junit.{Assert, Test}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.{Autowired, Value}
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
@RunWith(classOf[SpringJUnit4ClassRunner])
@SpringBootTest
@Import(Array(classOf[ApplicationConfig]))
class ClientTest {
    //noinspection VarCouldBeVal
    @Autowired
    private var client: Client = _

    //noinspection VarCouldBeVal
    @Value("${com.idvp.elections.source.uri}")
    private var electionUri: String = _

    //noinspection VarCouldBeVal
    @Value("${com.idvp.presence.source.uri}")
    private var presenceUri: String = _

    @Test
    def test1(): Unit = {
        val path = client.download(electionUri)
        Assert.assertNotNull(path.orNull)
    }

    @Test
    def test2(): Unit = {
        val path = client.download(presenceUri)
        Assert.assertNotNull(path.orNull)
    }
}
