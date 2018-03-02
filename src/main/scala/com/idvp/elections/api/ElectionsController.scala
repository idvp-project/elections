package com.idvp.elections.api

import java.util

import com.idvp.elections.api.model.Item
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.{GetMapping, RequestMapping}

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
@Service
@RequestMapping(
    path = Array("/elections"),
    produces = Array(MediaType.APPLICATION_JSON_UTF8_VALUE))
class ElectionsController {

    @GetMapping(consumes = Array(MediaType.ALL_VALUE))
    def getData: util.List[Item] = {
        util.Collections.emptyList()
    }
}
