package no.nav.familie.ba.infotrygd.skatt

import com.opencsv.bean.CsvToBeanBuilder
import no.nav.familie.eksterne.kontrakter.skatteetaten.SkatteetatenPersonerResponse
import no.nav.familie.kontrakter.felles.objectMapper
import org.junit.Test
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import kotlin.io.path.Path

class SkattValidateResponseTest {


    @Test
    fun `diff forskjell i resultat fra personer-tjenesten med data i gammel batch`() {
        val file = File(".","2021.csv")
        val fileReader = BufferedReader(FileReader(file))

        val records: List<CsvData> = CsvToBeanBuilder<CsvData>(fileReader)
            .withSeparator(';')
            .withType(CsvData::class.java).build().parse()

        val csvRecordsGruppertPåFnr = records.groupBy{it.fnr}
        val personerBatchTjeneste = csvRecordsGruppertPåFnr.keys


        val personerRestTjeneste = objectMapper.readValue(
            Path("./2021-personer-2.json").toFile(),
            SkatteetatenPersonerResponse::class.java
        ).brukere.map { it.ident }.sorted()



        println("Finnes i batch, men ikke i rest: ${personerBatchTjeneste.minus(personerRestTjeneste.toHashSet())}")
        println("Finnes i rest, men ikke i batch: ${personerRestTjeneste.minus(personerBatchTjeneste.toHashSet()).size}")

        personerRestTjeneste.minus(personerBatchTjeneste.toHashSet()).forEach{
            println("\"$it\",")
        }
    }
}