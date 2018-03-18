package com.idvp.elections.transformation

import java.nio.file.Paths

import com.idvp.elections.ApplicationConfig
import com.idvp.elections.client.Client
import org.junit.runner.RunWith
import org.junit.{Assert, Test}
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
class PresenceTransformationTest {

    //noinspection VarCouldBeVal
    @Value("${com.idvp.presence.source.uri}")
    private var sourceUri: String = _

    //noinspection VarCouldBeVal
    @Autowired
    private var transformation: PresenceTransformation = _

    //noinspection VarCouldBeVal
    @Autowired
    private var client: Client = _


    @Test
    def test1(): Unit = {
        Assert.assertNotNull(transformation)
    }

    @Test
    def test2(): Unit = {
        val path = Paths.get("./src/test/resources/presence.xls")
        val result = transformation.transform(path)
        Assert.assertNotNull(result.orNull)
    }

    @Test
    def test3(): Unit = {
        val path = client.download(sourceUri)
        Assert.assertNotNull(path.orNull)
        val transformed = transformation.transform(path.get)
        Assert.assertNotNull(transformed.orNull)
    }




}
