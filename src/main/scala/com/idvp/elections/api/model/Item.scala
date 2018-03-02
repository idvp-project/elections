package com.idvp.elections.api.model

import com.fasterxml.jackson.annotation.JsonProperty

import scala.beans.BeanProperty

/**
  * @author Oleg Zinoviev
  * @since 02.03.18.
  */
case class Item(
                  @JsonProperty(value = "row", access = JsonProperty.Access.READ_ONLY) @BeanProperty row: String,
                  @JsonProperty(value = "column", access = JsonProperty.Access.READ_ONLY) @BeanProperty column: String,
                  @JsonProperty(value = "value", access = JsonProperty.Access.READ_ONLY) @BeanProperty value: String) {

}
