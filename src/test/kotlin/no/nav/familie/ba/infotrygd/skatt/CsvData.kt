package no.nav.familie.ba.infotrygd.skatt

import com.opencsv.bean.CsvBindByName
import com.opencsv.bean.CsvDate
import java.util.Date

class CsvData {

    @CsvBindByName(column = "FNR")
    lateinit var fnr: String

    @CsvBindByName(column = "FOM")
    @CsvDate("MMyyyy")
    lateinit var fom: Date

    @CsvBindByName(column = "TOM")
    @CsvDate("MMyyyy")
    val tom: Date? = null

    @CsvBindByName(column = "MND IVERKSATT")
    @CsvDate("MMyyyy")
    lateinit var iverksatt: Date

    @CsvBindByName(column = "BTRYGD-H-H")
    var status: Int = 4


    override fun toString(): String {
        return "CsvData(fnr=$fnr, fom=$fom, tom=$tom), status=$status, iverksatt=$iverksatt"
    }


}