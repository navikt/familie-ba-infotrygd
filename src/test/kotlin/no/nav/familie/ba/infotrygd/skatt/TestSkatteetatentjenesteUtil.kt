package no.nav.familie.ba.infotrygd.skatt

import com.opencsv.bean.CsvToBeanBuilder
import no.nav.familie.ba.infotrygd.rest.controller.BisysController
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderRequest
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPerioderResponse
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPersonerResponse
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.time.YearMonth

const val FILNAVN_BATCH_CSV = "2021.csv"

//const val BASE_URL = "https://familie-ba-infotrygd.dev.intern.nav.no"
const val BASE_URL = "https://familie-ba-infotrygd.intern.nav.no"
const val ÅR = 2021
const val TOKEN = "TOKEN"

/**
 * Sammenligner uttrekk i gammel batchjobb med det som ligger i infotrygd
 *
 * Generer ett token og inject i TOKEN
 * Velg et miljø å gå mot med BASEN_URL
 * log på mot onprem-k8-prod for å kjøring mot prod
 *
 */
fun main() {
    //Leser fil og konverterer til
    val file = File(".", FILNAVN_BATCH_CSV)
    val fileReader = BufferedReader(FileReader(file))

    val records: List<CsvData> = CsvToBeanBuilder<CsvData>(fileReader)
        .withSeparator(';')
        .withType(CsvData::class.java).build().parse()

    val csvRecordsGruppertPåFnr = records.groupBy { it.fnr }
    val personerBatchTjeneste = csvRecordsGruppertPåFnr.keys

    //Kommenter inn hvis man sammenligne mot fil
//        val personerRestTjeneste = objectMapper.readValue(
//            Path("./response.json").toFile(),
//            SkatteetatenPersonerResponse::class.java
//        ).brukere.map { it.ident }.sorted()

    val personerRestTjeneste = kallInfotrygdPersonerUtvidet().brukere.map { it.ident }.sorted()

    println("Finnes i batch, men ikke i rest: ${personerBatchTjeneste.minus(personerRestTjeneste.toHashSet())}")
    println("Finnes i rest, men ikke i batch: ${personerRestTjeneste.minus(personerBatchTjeneste.toHashSet()).size}")

    //Sammenligner alle differ mot periodetjenesten
    val resultatSammenlignetMedPeriodeTjeneste = personerRestTjeneste.minus(personerBatchTjeneste.toHashSet()).partition { fnr ->

        val perioder = kallInfotrygdSkattPeriodetjeneste(fnr).first().brukere.firstOrNull()?.perioder?.map {
            Pair(
                YearMonth.parse(it.fraMaaned),
                if (it.tomMaaned == null) null else YearMonth.parse(it.tomMaaned)
            )
        }
//        println("$fnr harPeriodeIÅr=${harPeriodeIAar(ÅR, perioder)} $perioder")
        harPeriodeIAar(ÅR, perioder)
    }

    println("har ikke perioder i skatt $ÅR: ${resultatSammenlignetMedPeriodeTjeneste.second.size} ${resultatSammenlignetMedPeriodeTjeneste.second}")
    val resultatSammenlignetMedBisys = personerRestTjeneste.minus(personerBatchTjeneste.toHashSet()).partition { fnr ->
        val bisysPerioder = kallInfotrygdBisystjeneste(fnr).perioder.map { Pair(it.fomMåned, it.tomMåned) }
//        println("$fnr harPeriodeIÅr=${harPeriodeIAar(ÅR, bisysPerioder)} $bisysPerioder")
        harPeriodeIAar(ÅR, bisysPerioder)
    }
    println("har ikke perioder i Bisys $ÅR: ${resultatSammenlignetMedBisys.second.size} ${resultatSammenlignetMedBisys.second}")
}

fun kallInfotrygdPersonerUtvidet(): SkatteetatenPersonerResponse {
    val restTemplate = RestTemplate()
    val uri = "$BASE_URL/infotrygd/barnetrygd/utvidet?aar=$ÅR"
    val headers = HttpHeaders()
    headers.setBearerAuth(TOKEN)
    headers.contentType = MediaType.APPLICATION_JSON

    val entity = HttpEntity(null, headers)


    return restTemplate.exchange(uri, HttpMethod.GET, entity, SkatteetatenPersonerResponse::class.java).body!!
}

fun kallInfotrygdSkattPeriodetjeneste(personIdent: String): List<SkatteetatenPerioderResponse> {
    val restTemplate = RestTemplate()
    val uri = "$BASE_URL/infotrygd/barnetrygd/utvidet/skatteetaten/perioder"
    val headers = HttpHeaders()
    headers.setBearerAuth(TOKEN)
    headers.contentType = MediaType.APPLICATION_JSON

    val entity = HttpEntity(SkatteetatenPerioderRequest(ÅR.toString(), listOf(personIdent)), headers)

    val response: String? = restTemplate.postForObject(uri, entity, String::class.java)
    val r: List<SkatteetatenPerioderResponse> =
        objectMapper.readerForListOf(SkatteetatenPerioderResponse::class.java).readValue(response)

    return r
}


fun kallInfotrygdBisystjeneste(personIdent: String): BisysController.InfotrygdUtvidetBarnetrygdResponse {
    val restTemplate = RestTemplate()
    val uri = "$BASE_URL/infotrygd/barnetrygd/utvidet"
    val headers = HttpHeaders()
    headers.setBearerAuth(TOKEN)
    headers.contentType = MediaType.APPLICATION_JSON

    val entity = HttpEntity(BisysController.InfotrygdUtvidetBarnetrygdRequest(personIdent, YearMonth.of(ÅR - 1, 12)), headers)


    return restTemplate.postForEntity(
        uri, entity,
        BisysController.InfotrygdUtvidetBarnetrygdResponse::class.java
    ).body!!
}

fun harPeriodeIAar(år: Int, perioder: List<Pair<YearMonth, YearMonth?>>?): Boolean {
    if (perioder == null) {
        return false
    }
    return perioder.any {
        it.first.year == år || it.second?.year == år || (it.first.year <= år && (it.second == null || it.second!!.year > år))
    }
}



