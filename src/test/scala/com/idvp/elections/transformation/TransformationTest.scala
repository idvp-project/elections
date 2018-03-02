package com.idvp.elections.transformation

import java.nio.file.Paths

import com.idvp.elections.ApplicationConfig
import com.idvp.elections.client.Client
import org.junit.runner.RunWith
import org.junit.{Assert, Test}
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
class TransformationTest {

    //noinspection VarCouldBeVal
    @Autowired
    private var transformation: Transformation = _

    //noinspection VarCouldBeVal
    @Autowired
    private var client: Client = _


    @Test
    def test1(): Unit = {
        Assert.assertNotNull(transformation)
    }

    @Test
    def test2(): Unit = {
        val path = Paths.get("./src/test/resources/report.xls")
        val result = transformation.transform(path)
        Assert.assertNotNull(result.orNull)
    }

    @Test
    def test3(): Unit = {
        val path = client.download()
        Assert.assertNotNull(path.orNull)
        val transformed = transformation.transform(path.get)
        Assert.assertNotNull(transformed.orNull)
    }




}
