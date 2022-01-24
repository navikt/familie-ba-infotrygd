package no.nav.familie.ba.infotrygd.rest.controller

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import no.nav.commons.foedselsnummer.FoedselsNr
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdLøpendeBarnetrygdResponse
import no.nav.familie.ba.infotrygd.rest.api.InfotrygdÅpenSakResponse
import no.nav.familie.ba.infotrygd.service.BarnetrygdService
import no.nav.familie.ba.infotrygd.service.ClientValidator
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkRequest
import no.nav.familie.kontrakter.ba.infotrygd.InfotrygdSøkResponse
import no.nav.security.token.support.core.api.Protected
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


@Protected
@RestController
@Timed(value = "infotrygd_historikk_barnetrygd_controller", percentiles = [0.5, 0.95])
@RequestMapping("/infotrygd/barnetrygd")
class BarnetrygdController(
    private val barnetrygdService: BarnetrygdService,
    private val clientValidator: ClientValidator
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(summary = "Avgjør hvorvidt det finnes løpende barnetrygd på søker eller barn i Infotrygd.")
    @PostMapping(path = ["lopende-barnetrygd"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = INFOTRYGD_SØK_EKSEMPEL)])])
    fun harLopendeBarnetrygd(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<InfotrygdLøpendeBarnetrygdResponse> {
        clientValidator.authorizeClient()

        val harLøpendeBarnetrygd = hentStønaderPåBrukereOgBarn(request.brukere, request.barn, false).let {
            it.first.isNotEmpty() || it.second.isNotEmpty()
        }

        return ResponseEntity.ok(InfotrygdLøpendeBarnetrygdResponse(harLøpendeBarnetrygd))
    }
    @Operation(summary = "Svarer hvorvidt det finnes en åpen sak til beslutning, på søker eller barn i Infotrygd.")
    @PostMapping(path = ["aapen-sak"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = INFOTRYGD_SØK_EKSEMPEL)])])
    fun harÅpenSak(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<InfotrygdÅpenSakResponse> {
        clientValidator.authorizeClient()

        return barnetrygdService.tellAntallÅpneSaker(request.brukere, request.barn).let {
            ResponseEntity.ok(InfotrygdÅpenSakResponse(it > 0))
        }
    }

    @Operation(summary = "Uttrekk fra tabellen \"BA_STOENAD_20\".")
    @PostMapping(path = ["stonad"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = INFOTRYGD_SØK_EKSEMPEL)])])
    fun stønad(
        @RequestBody request: InfotrygdSøkRequest,
        @RequestParam(required = false) historikk: Boolean?
    ): ResponseEntity<InfotrygdSøkResponse<StønadDto>> {
        clientValidator.authorizeClient()

        return hentStønaderPåBrukereOgBarn(request.brukere, request.barn, historikk).let {
            ResponseEntity.ok(InfotrygdSøkResponse(bruker = it.first, barn = it.second))
        }
    }

    @Operation(summary = "Uttrekk fra tabellen \"SA_SAK_10\".")
    @PostMapping(path = ["saker"], consumes = ["application/json"])
    @ApiRequestBody(content = [Content(examples = [ExampleObject(value = INFOTRYGD_SØK_EKSEMPEL)])])
    fun saker(@RequestBody request: InfotrygdSøkRequest): ResponseEntity<InfotrygdSøkResponse<SakDto>> {
        clientValidator.authorizeClient()

        val brukere = request.brukere.map { FoedselsNr(it) }
        val barn = request.barn?.takeUnless { it.isEmpty() }?.map { FoedselsNr(it) }

        return ResponseEntity.ok(InfotrygdSøkResponse(bruker = barnetrygdService.findSakerByBrukerFnr(brukere),
                                           barn = barnetrygdService.findSakerByBarnFnr(barn ?: emptyList())))
    }

    @Operation(summary = "Uttrekk personer med ytelse. F.eks OS OS for barnetrygd, UT EF for småbarnstillegg")
    @PostMapping(path = ["migrering"])
    fun migrering(@RequestBody request: MigreringRequest): ResponseEntity<Set<String>> {
        clientValidator.authorizeClient()

        return ResponseEntity.ok(
            barnetrygdService.finnPersonerKlarForMigrering(
                request.page,
                request.size,
                request.valg,
                request.undervalg,
                request.maksAntallBarn,
                request.minimumAlder
            )
        )
    }


    @Operation(summary = "Finn stønad med id")
    @GetMapping(path = ["stonad/{id}"])
    @Deprecated(message="Erstattes av findStønad som henter basert på B01_PERSONKEY, B20_IVERFOM_SEQ, B20_VIRKFOM_SEQ og REGION")
    fun findStønadById(@PathVariable id: Long): ResponseEntity<StønadDto> {
        clientValidator.authorizeClient()

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
        clientValidator.authorizeClient()

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
    fun harSendtBrevForrigeMåned(@RequestBody sendtBrevRequest: SendtBrevRequest): ResponseEntity<Boolean> {
        clientValidator.authorizeClient()

        return ResponseEntity.ok(
            barnetrygdService.harSendtBrevForrigeMåned(
                FoedselsNr(sendtBrevRequest.personIdent),
                sendtBrevRequest.brevkoder
            )
        )
    }

    private fun hentStønaderPåBrukereOgBarn(brukere: List<String>,
                                            barn: List<String>?,
                                            historikk: Boolean?): Pair<List<StønadDto>, List<StønadDto>> {
        val brukere = brukere.map { FoedselsNr(it) }
        val barn = barn?.map { FoedselsNr(it) } ?: emptyList()

        return Pair(barnetrygdService.findStønadByBrukerFnr(brukere, historikk),
                    barnetrygdService.findStønadByBarnFnr(barn, historikk))
    }

    data class MigreringRequest(
        val page: Int,
        val size: Int,
        val valg: String,
        val undervalg: String,
        val maksAntallBarn: Int = 99,
        val minimumAlder: Int = 7
    )

    class StønadRequest(
        val personIdent: String,
        val tknr: String,
        val iverksattFom: String,
        val virkningFom: String,
        val region: String
    )

    class SendtBrevRequest(
        val personIdent: String,
        val brevkoder: List<String>
    )

    companion object {
        const val INFOTRYGD_SØK_EKSEMPEL = "{\n  \"brukere\": [\"12345678910\"]," + "\n  \"barn\": [\n\"23456789101\",\n\"34567891012\"\n]\n}"
    }
}


