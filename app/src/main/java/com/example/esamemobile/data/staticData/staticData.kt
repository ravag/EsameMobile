package com.example.esamemobile.data.staticData

import android.graphics.drawable.Icon
import com.example.esamemobile.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

data class AgeMalus(
    val drawableId: Int? = null,
    val name: String,
    val desc: String
    )

val ALL_AGE_MALUS = listOf(
    AgeMalus(
        drawableId = R.drawable.ic_young,
        name = "Spirito da Ragazzino",
        desc = "Ti rifiuti di accettare l'età che hai.\n\n" +
                "Racconti a tutti che i giovani d'oggi non si divertono più come una volta e provi a fare acrobazie e stunt (spesso fallendo)."
    ),
    AgeMalus(
        drawableId = R.drawable.ic_nohand,
        name = "Mani di Burro",
        desc = "I riflessi sono intatti, ma la precisione non è più quella di una vota: gli oggetti piccoli tendono a scivolarti di mano."
    ),
    AgeMalus(
        drawableId = R.drawable.ic_cognition,
        name = "Nostalgia dei Tempi Andati",
        desc = "La memoria a breve termine vacilla, ma ricordi nei dettagli nomi e avvenimenti di 50 anni fa.\n\n" +
                "Spesso confondi i nomi dei tuoi alleati con quelli di alleati passati e ricordi i luoghi per come erano e non per come sono adesso."
    ),
    AgeMalus(
        drawableId = R.drawable.ic_noeye,
        name = "Vista Appannata",
        desc = "Purtroppo la vista non è più quella di un tempo: vedi il mondo come se fosse coperto da una lieve nebbia di alta quota."
    ),
    AgeMalus(
        drawableId = R.drawable.ic_badbone,
        name = "Ossa Scricchiolanti",
        desc = "Le tue articolazioni fanno rumore ad ogni passo falso o movimento brusco. Muoverti furtivamente ti richiede una cautela molto maggiore."
    ),
    AgeMalus(
        drawableId = R.drawable.ic_waterloss,
        name = "Vescica Impaziente",
        desc = "Il tuo corpo ha i suoi ritmi e non sente ragioni, nemmeno durante un'imboscata.\n\n" +
                "Il gruppo potrebbe spazientirsi alle tue costanti richieste di \"pause tecniche\"."
    ),
    AgeMalus(
        drawableId = R.drawable.ic_breath,
        name = "Fiato Corto",
        desc = "Il tuo cuore è nobile, ma i tuoi polmoni si stancano in fretta.\n\n" +
                "Dopo una corsa o una rampa di scale, hai bisogno di qualche minuto per riprendere fiato."
    ),
    AgeMalus(
        drawableId = R.drawable.ic_nomouth,
        name = "Parola Tartaglina",
        desc = "A volte i pensieri corrono più veloci della tua lingua, facendoti incastrare le parole tra i denti.\n\n" +
                "In momenti di particolare tensione o emozione, potresti incepparti sulle consonanti o parole complesse."
    ),
    AgeMalus(
        drawableId = R.drawable.ic_nohearing,
        name = "Fischi d'Orecchie",
        desc = "Senti costantemente un ronzio di sottofondo, come se una cicala magica avesse preso dimora nella tua testa.\n\n" +
                "Se un personaggio non ti parla a volume abbastanza alto, potresti rischiare di non capire cosa ti dice."
    ),
    AgeMalus(
        drawableId = R.drawable.ic_climate,
        name = "Sensibile al Clima",
        desc = "Le tue vecchie ginocchia prevedono il tempo meglio dei maghi della natura.\n\n" +
                "Quando ti fanno male le ginocchia, significa che sta per piovere e anche se tutti dicono che ti lamenti e basta, raramente hai torto."
    ),
)