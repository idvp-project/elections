package com.idvp.elections.api

import com.idvp.elections.service.PresenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.http.{HttpStatus, MediaType, ResponseEntity}
import org.springframework.web.bind.annotation._

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
@RestController
@RequestMapping(
    path = Array("/presence"),
    produces = Array(MediaType.APPLICATION_JSON_UTF8_VALUE))
class PresenceController {

    //noinspection VarCouldBeVal
    @Autowired
    private var service: PresenceService = _

    @GetMapping(consumes = Array(MediaType.ALL_VALUE))
    def getData: ResponseEntity[InputStreamResource] = {

        val stream = service.getLatest

        if (stream.isEmpty) {
            return ResponseEntity.noContent().build()
        }

        ResponseEntity.ok(new InputStreamResource(stream.get))
    }

    @PostMapping(consumes = Array(MediaType.TEXT_PLAIN_VALUE))
    @ResponseStatus(HttpStatus.NO_CONTENT)
    def forceUpdate(@RequestBody(required = false) path: String): Unit = {
        service.forceUpdate(path)
    }
}
