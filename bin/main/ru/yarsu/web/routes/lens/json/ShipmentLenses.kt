package ru.yarsu.web.routes.lens.json

import org.http4k.core.Body
import org.http4k.format.Jackson.auto
import org.http4k.lens.BiDiBodyLens
import ru.yarsu.web.routes.dto.json.ShipmentRequest

val rawShipmentRequestLens: BiDiBodyLens<ShipmentRequest> = Body.auto<ShipmentRequest>().toLens()
