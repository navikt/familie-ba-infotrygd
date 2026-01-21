package no.nav.familie.ba.infotrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.KonsumeresAv
import no.nav.familie.ba.infotrygd.model.dl1.Hendelse
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdLøpendeBarnetrygdResponse
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdÅpenSakResponse
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.service.TilgangskontrollService
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkRequest
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkResponse
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.parameters.RequestBody as ApiRequestBody
import no.nav.familie.kontrakter.ba.infotrygd.Sak as SakDto
import no.nav.familie.kontrakter.ba.infotrygd.Stønad as StønadDto


@RestController
@ProtectedWithClaims(issuer = "azuread")
@Timed(value = "infotrygd_historikk_barnetrygd_controller", percentiles = [0.5, 0.95])
@RequestMapping("/infotrygd/barnetrygd")
class BarnetrygdController(
    private val barnetrygdService: BarnetrygdService,
    private val tilgangskontrollService: TilgangskontrollService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "Avgjør hvorvidt det finnes løpende barnetrygd på søker eller barn i Infotrygd.")
    @PostMapping(path = ["lopende-barnetrygd"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = INFOTRYGD_SØK_EKSEMPEL)])])
    @KonsumeresAv(apper = ["familie-ba-sak"] )
    fun harLopendeBarnetrygd(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<InfotrygdLøpendeBarnetrygdResponse> {
        tilgangskontrollService.sjekkTilgang()

        val harLøpendeBarnetrygd = hentStønaderPåBrukereOgBarn(request.brukere, request.barn, false).let {
            it.first.isNotEmpty() || it.second.isNotEmpty()
        }

        return ResponseEntity.ok(InfotrygdLøpendeBarnetrygdResponse(harLøpendeBarnetrygd))
    }
    @Operation(summary = "Svarer hvorvidt det finnes en åpen sak til beslutning, på søker eller barn i Infotrygd.")
    @PostMapping(path = ["aapen-sak"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = INFOTRYGD_SØK_EKSEMPEL)])])
    @KonsumeresAv(apper = ["familie-ba-sak"] )
    fun harÅpenSak(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<InfotrygdÅpenSakResponse> {
        tilgangskontrollService.sjekkTilgang()

        return barnetrygdService.tellAntallÅpneSaker(request.brukere, request.barn).let {
            ResponseEntity.ok(InfotrygdÅpenSakResponse(it > 0))
        }
    }

    @Operation(summary = "Uttrekk fra tabellen \"BA_STOENAD_20\".")
    @PostMapping(path = ["stonad"], consumes = ["application/json"], produces = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = INFOTRYGD_SØK_EKSEMPEL)])])
    @KonsumeresAv(apper = ["familie-baks-mottak"] )
    fun stønad(
        @RequestBody request: InfotrygdSøkRequest,
        @RequestParam(required = false) historikk: Boolean?
    ): ResponseEntity<InfotrygdSøkResponse<StønadDto>> {
        tilgangskontrollService.sjekkTilgang()

        return hentStønaderPåBrukereOgBarn(request.brukere, request.barn, historikk).let {
            ResponseEntity.ok(InfotrygdSøkResponse(bruker = it.first, barn = it.second))
        }
    }

    @Operation(summary = "Uttrekk fra tabellen \"SA_SAK_10\".")
    @PostMapping(path = ["saker"], consumes = ["application/json"], produces = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = INFOTRYGD_SØK_EKSEMPEL)])])
    @KonsumeresAv(apper = ["familie-baks-mottak", "familie-ba-sak"] )
    fun saker(@RequestBody request: no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkRequest): ResponseEntity<InfotrygdSøkResponse<SakDto>> {
        tilgangskontrollService.sjekkTilgang()

        val brukere = request.brukere.map { FoedselsNr(it) }
        val barn = request.barn?.takeUnless { it.isEmpty() }?.map { FoedselsNr(it) }

        return ResponseEntity.ok(InfotrygdSøkResponse(bruker = barnetrygdService.findSakerByBrukerFnr(brukere),
                                           barn = barnetrygdService.findSakerByBarnFnr(barn ?: emptyList())))
    }

    @Operation(summary = "Finn stønad med id")
    @GetMapping(path = ["stonad/{id}"])
    @Deprecated(message="Erstattes av findStønad som henter basert på B01_PERSONKEY, B20_IVERFOM_SEQ, B20_VIRKFOM_SEQ og REGION")
    fun findStønadById(@PathVariable id: Long): ResponseEntity<StønadDto> {
        tilgangskontrollService.sjekkTilgang()

        try {
            return ResponseEntity.ok(
                barnetrygdService.findStønadById(id)
            )
        } catch (nsee: NoSuchElementException) {
            return ResponseEntity.notFound().build()
        }


    }

    @Operation(summary = "Finn stønad basert på personKey, iverksattFom, virkningFom og region")
    @PostMapping(path = ["stonad/sok"])
    fun findStønad(@RequestBody stønadRequest: StønadRequest): ResponseEntity<StønadDto> {
        tilgangskontrollService.sjekkTilgang()

        try {
            return ResponseEntity.ok(
                barnetrygdService.findStønad(
                    stønadRequest.personIdent,
                    stønadRequest.tknr,
                    stønadRequest.iverksattFom,
                    stønadRequest.virkningFom,
                    stønadRequest.region
                )
            )
        } catch (nsee: NoSuchElementException) {
            return ResponseEntity.notFound().build()
        }
    }


    @Operation(summary = "Finn om brev med brevkode er sendt for en person i forrige måned")
    @PostMapping(path = ["/brev"])
    @KonsumeresAv(apper = ["familie-ba-sak"] )
    fun harSendtBrevForrigeMåned(@RequestBody sendtBrevRequest: SendtBrevRequest): ResponseEntity<SendtBrevResponse> {
        tilgangskontrollService.sjekkTilgang()

        val listeMedBrevhendelser = barnetrygdService.harSendtBrevForrigeMåned(
            sendtBrevRequest.personidenter.map { FoedselsNr(it)},
            sendtBrevRequest.brevkoder
        )

        return ResponseEntity.ok(
            SendtBrevResponse(listeMedBrevhendelser.isNotEmpty(), listeMedBrevhendelser)
        )
    }

    @GetMapping(path = ["/testlog"])
    fun error(): ResponseEntity<String> {
        logger.error("Test av alarm")
        return ResponseEntity.ok("Test av alarm")
    }

    private fun hentStønaderPåBrukereOgBarn(brukere: List<String>,
                                            barn: List<String>?,
                                            historikk: Boolean?): Pair<List<StønadDto>, List<StønadDto>> {
        val brukere = brukere.map { FoedselsNr(it) }
        val barn = barn?.map { FoedselsNr(it) } ?: emptyList()

        return Pair(barnetrygdService.findStønadByBrukerFnr(brukere, historikk),
                    barnetrygdService.findStønadByBarnFnr(barn, historikk))
    }

    class StønadRequest(
        val personIdent: String,
        val tknr: String,
        val iverksattFom: String,
        val virkningFom: String,
        val region: String
    )

    class SendtBrevRequest(
        val personidenter: List<String>,
        val brevkoder: List<String>
    )

    class SendtBrevResponse(
        val harSendtBrev: Boolean,
        val listeBrevhendelser: List<Hendelse> = emptyList()
    )

    companion object {
        const val INFOTRYGD_SØK_EKSEMPEL = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"
    }
}


