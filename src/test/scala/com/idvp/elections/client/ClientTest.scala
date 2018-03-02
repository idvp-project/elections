package com.idvp.elections.client

import com.idvp.elections.ApplicationConfig
import org.junit.{Assert, Test}
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
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


    @Test
    def test1(): Unit = {
        val path = client.download()
        Assert.assertNotNull(path.orNull)
    }
}
