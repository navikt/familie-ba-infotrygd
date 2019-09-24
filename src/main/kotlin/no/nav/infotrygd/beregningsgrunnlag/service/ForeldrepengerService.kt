package no.nav.infotrygd.beregningsgrunnlag.service

import no.nav.infotrygd.beregningsgrunnlag.model.Ytelse
import no.nav.infotrygd.beregningsgrunnlag.model.kodeverk.Stoenadstype
import no.nav.infotrygd.beregningsgrunnlag.repository.PeriodeRepository
import no.nav.infotrygd.beregningsgrunnlag.rest.dto.*
import no.nav.infotrygd.beregningsgrunnlag.values.FodselNr
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ForeldrepengerService(private val periodeRepository: PeriodeRepository) {
    fun hentForeldrepenger(fodselNr: FodselNr, fom: LocalDate, tom: LocalDate?): List<Foreldrepenger> {

        val stoenadstyper = listOf(
            Stoenadstype.ADOPSJON,
            Stoenadstype.FOEDSEL,
            Stoenadstype.RISIKOFYLT_ARBMILJOE,
            Stoenadstype.SVANGERSKAP
        )

        val result = if(tom != null) {
            periodeRepository.findByFnrAndStoenadstypeAndDates(fodselNr, stoenadstyper, fom, tom)
        } else {
            periodeRepository.findByFnrAndStoenadstypeAndDates(fodselNr, stoenadstyper, fom)
        }

        return result.map {
            periodeToForeldrepenger(it)
        }
    }
}

fun periodeToForeldrepenger(p: no.nav.infotrygd.beregningsgrunnlag.model.Periode): Foreldrepenger {
    check(p.ytelse == Ytelse.FORELDREPENGER) { "Forventet ytelse == FORELDREPENGER" }

    return Foreldrepenger(
        generelt = periodeToGrunnlag(p),
        opprinneligIdentdato = p.arbufoerOpprinnelig,
        dekningsgrad = p.dekningsgrad,
        gradering = null, // todo: implement
        foedselsdatoBarn = p.foedselsdatoBarn !!
    )
}

fun periodeToGrunnlag(p: no.nav.infotrygd.beregningsgrunnlag.model.Periode): GrunnlagGenerelt {

    val utbetaltFom = p.utbetaltFom
    val utbetaltTom = p.utbetaltTom

    val periode: Periode? = if(utbetaltFom != null && utbetaltTom != null)
        Periode(utbetaltFom, utbetaltTom) else null

    val arbeidskategori: Kodeverdi? = {
        val kat = p.arbeidskategori
        if (kat == null) {
            null
        } else {
            Kodeverdi(kat.kode, kat.tekst)
        }
    }()

    return GrunnlagGenerelt(
        behandlingstema = p.stoenadstype!!.toBehandlingstema(),
        identdato = p.arbufoer, // todo: pårørende sykdom
        periode = periode, // todo: pårørende sykdom
        arbeidskategori = arbeidskategori,
        arbeidsforhold = p.inntekter.map {
            Arbeidsforhold(
                inntektForPerioden = it.loenn, // todo: pårørende sykdom
                inntektsperiode = Kodeverdi(it.periode.kode, it.periode.tekst),
                arbeidsgiverOrgnr = it.arbgiverNr
            )
        },
        vedtak = p.utbetalinger.map {
            Vedtak(
                utbetalingsgrad = it.grad,
                periode = Periode(it.utbetaltFom, it.utbetaltTom) // todo: pårørende sykdom
            )
        }
    )
}
