package com.example.esamemobile.data.staticData

import android.graphics.drawable.Icon
import com.example.esamemobile.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import java.util.UUID

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

data class ClassAbility(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val isAdvanced: Boolean
)

data class GameClass(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val baseAbilities: List<ClassAbility>,
    val advancedAbilities: List<ClassAbility>
)

val ALL_GAME_CLASSES = listOf(
    GameClass(
        name = "Soggetto Alfa",
        description = "Specializzato nel subire i danni al posto dei propri alleati.",
        baseAbilities = listOf(
            ClassAbility(name = "Ipertrofia Muscolare", description = "Hai vantaggio alle prove di Forza.", isAdvanced = false),
            ClassAbility(name = "Cinetica d'Impatto", description = "Come parte dell'azione di attacco contro una creatura entro 1,5 m da te, se hai successo, oltre a fare il normale danno, puoi scegliere una delle seguenti opzioni:\n-afferri il nemico\n-spingi il nemico di 3 m\n-butti a terra il nemico che cade quindi prono", isAdvanced = false),
            ClassAbility(name = "Scudo Epiteliale", description = "Come reazione, quando una creatura entro 3 m da te viene colpita, puoi subire metà dei danni al suo posto.", isAdvanced = false)
        ),
        advancedAbilities = listOf(
            ClassAbility(name = "Arresto Omeostatico", description = "Come azione bonus, entrare in uno stato di arresto omeostatico.\n\n" +
                    "Finché sei in questo stato, niente e nessuno può spostarti contro la tua volontà, ottieni resistenza a tutti i danni e diventi immune alla condizione prono durante questo periodo.\n\n" +
                    "Se arrivi a 0 HP mentre sei in questo stato, non muori fino al termine del minuto.", isAdvanced = true),
            ClassAbility(name = "Onda d'Urto", description = "come azione, esegui una prova di Forza contrapposta ad Agilità.\n\n" +
                    "Tutti i nemici entro 12 m che falliscono il tiro su agilità cadono proni e subiscono svantaggio ad attaccare tutte le creature diverse da te fino all'inizio del tuo prossimo turno.\n\n" +
                    "Guadagni inoltre HP extra pari al tuo modificatore di Forza (minimo 1)", isAdvanced = true),
            ClassAbility(name = "Deflessione Cinetica", description = "Quando vieni preso di mira da un attacco o da una abilità, puoi utilizzare Forza anziché Agilità per evitarli e, se hai successo, rispedisci al mittente tutto il danno che avresti dovuto subire + il tuo modificatore di Forza, azzerando il danno che avresti subito.", isAdvanced = true)
        )
    ),
    GameClass(
        name = "Vettore Elusivo",
        description = "Specializzato nell'evitare attacchi e responsabilità.",
        baseAbilities = listOf(
            ClassAbility(name = "Riflessi Dinamici", description = "Hai vantaggio alle prove di Agilità.", isAdvanced = false),
            ClassAbility(name = "Adrenalina Reattiva", description = "Una volta per round, quando subisci un danno da un attacco, ottieni un vantaggio spendibile entro la fine del tuo prossimo turno.", isAdvanced = false),
            ClassAbility(name = "Sfruttamento Tattico", description = "Quando possiedi almeno un vantaggio, puoi consumarne uno per usare un'azione di scatto, nascondersi o disingaggio come azione gratuita per ogni vantaggio consumato.", isAdvanced = false)
        ),
        advancedAbilities = listOf(
            ClassAbility(name = "Disorientamento Motorio", description = "Come azione bonus esegui una prova di Agilità contrapposta all'Intelligenza nemica.\n\n" +
                    "I nemici in grado di vederti che falliscono il tiro, vengono confusi dai tuoi movimenti scattanti e imprevedibili e fino alla fine del tuo prossimo turno ottieni vantaggio contro i tuoi avversari mentre loro ottengono svantaggio per colpirti.\n\n" +
                    "Se la prova di Agilità effettuata per attivare questa abilità è un critico, tutti gli attacchi contro di te mancano automaticamente per la durata dell'effetto.", isAdvanced = true),
            ClassAbility(name = "Scatto Neurologico", description = "Alla fine del tuo turno puoi usare la tua reazione per effettuare un turno extra.\n\n" +
                    "Durante questo turno extra, il tuo movimento non causa attacchi d'opportunità.", isAdvanced = true),
            ClassAbility(name = "Iper-Consapevolezza Sensoriale", description = "Come azione, puoi amplificare i tuoi sensi per 1 minuto.\n\n" +
                    "Quando vieni mancato da un attacco durante questo periodo, puoi usare la tua reazione per scegliere uno dei seguenti effetti:\n\n" +
                    "-Effettua immediatamente un attacco d'opportunità contro una creatura che sei in grado di vedere.\n-Teletrasportati immediatamente entro 1,5 m ad una creatura che sei in grado di vedere, anche se fuori dalla tua portate\n-Ottieni doppio vantaggio spendibile quando vuoi entro la fine del tuo prossimo turno", isAdvanced = true)
        )
    ),
    GameClass(
        name = "Genetista Mutogeno",
        description = "Specializzato nella manipolazione e nello studio dei poteri evoluzione.",
        baseAbilities = listOf(
            ClassAbility(name = "Analisi Cognitiva", description = "Hai vantaggio alle prove di Intelligenza.", isAdvanced = false),
            ClassAbility(name = "Sinergia di Campo", description = "Quando utilizzi un'azione di studiare o cercare, puoi attivare un potere evoluzione in tuo possesso con un'azione bonus.", isAdvanced = false),
            ClassAbility(name = "Adattamento Genico", description = "Quando sei bersaglio di un potere evoluzione che sei in grado di vedere, puoi usare Intelligenza anziché la statistica base richiesta per evitarne gli effetti.", isAdvanced = false)
        ),
        advancedAbilities = listOf(
            ClassAbility(name = "Inibizione Mutogena", description = "Come reazione, esegui un tiro di Intelligenza contro il Carisma avversario.\n\n" +
                    "Se hai successo, puoi modificare leggermente, a discrezione del master, un effetto di un potere evoluzione che ha origine entro 12 m da te, fino alla fine del tuo prossimo turno", isAdvanced = true),
            ClassAbility(name = "Determinismo Cellulare", description = "Come azione, scegli una creatura entro 6 m di distanza: puoi trasformare la prossima prova di quella creatura in un successo o in un fallimento, a tua scelta.", isAdvanced = true),
            ClassAbility(name = "Catalizzatore d'Essenza", description = "Come azione bonus, trasferisci in un oggetto, non più grande della tua mano, un tuo potere evoluzione o quello di una creatura a te adiacente per 1 ora.\n\n" +
                    "Durante questo periodo, la creatura scelta non possiede direttamente quel potere.\n\n" +
                    "Una creatura che tiene in mano l'oggetto infuso, può attivare il potere in esso contenuto, utilizzando le proprie statistiche come se fosse il legittimo proprietario, come di consueto.\n\n" +
                    "Puoi far terminare prematuramente questa abilità con un'azione gratuita.", isAdvanced = true)
        )
    ),
    GameClass(
        name = "Leader Carismatico",
        description = "Specializzato nell'aiutare i propri compagni tramite un Carisma fuori dal comune.",
        baseAbilities = listOf(
            ClassAbility(name = "Ascendente Psicosociale", description = "Hai vantaggio alle prove di Carisma.", isAdvanced = false),
            ClassAbility(name = "Coordinamento Tattico", description = "Una volta per turno puoi utilizzare l'azione aiuto come azione bonus.\n\n" +
                    "Puoi scegliere una creatura da aiutare entro 6 m da te che sia in grado di sentirti.", isAdvanced = false),
            ClassAbility(name = "Iniezione di Endorfine", description = "Quando usi l'azione aiuto, la creatura che ne beneficia può scegliere di usare la propria reazione per una delle seguenti opzioni:\n\n" +
                    "-Tirare il suo dado di vantaggio e curarsi di un ammontare di HP pari al risultato del dado\n-Effettuare un attacco d'opportunità, utilizzando il vantaggio conferito dall'aiuto come di consueto.", isAdvanced = false)
        ),
        advancedAbilities = listOf(
            ClassAbility(name = "Soggezione Empatica", description = "Come azione, esegui una prova di Carisma contrapposta al Potere avversario.\n\n" +
                    "Per 1 min, i nemici che hanno fallito il tiro contrapposto, se sono in grado di sentirti e vederti, diventano neutrali nei tuoi confronti.\n\n" +
                    "Inoltre, tu e tutti i tuoi alleati, guadagnate HP extra pari al tuo livello + il tuo modificatore di Carisma (minimo 1).\n\n" +
                    "Se una creatura neutrale subisce danni durante questo periodo l'effetto termina.", isAdvanced = true),
            ClassAbility(name = "Sinergia Neuromodulare", description = "Una volta per turno, quando tu o un alleato a te adiacente fallite una prova di abilità, puoi scegliere una creatura cosciente entro 6 m e riprovare il tiro usando le sue statistiche.", isAdvanced = true),
            ClassAbility(name = "Sincronia di Picco", description = "Come azione bonus, qualsiasi dado di svantaggio o vantaggio venga tirato entro la fine del tuo prossimo turno, segna automaticamente il massimo risultato possibile.\n\n" +
                    "Questa abilità può essere usata solo una volta per creatura.", isAdvanced = true)
        )
    ),
    GameClass(
        name = "Incursore d'Attacco",
        description = "Specializzato nell'attaccare, prendere di mira singoli nemici e far danni.",
        baseAbilities = listOf(
            ClassAbility(name = "Efficienza Bellica", description = "Hai vantaggio alle prove di Potenza.", isAdvanced = false),
            ClassAbility(name = "Target Acquisito", description = "Come azione bonus scegli una creatura che sei in grado di vedere, quella creatura viene presa di mira:\nPer ogni attacco andato a segno in sequenza contro quella creatura, infliggi +1 danni extra cumulativi contro quel bersaglio, fino ad un massimo di +3 danni extra.\n\n" +
                    "Se colpisci una creatura diversa, l'effetto sulla creatura precedente termina immediatamente.", isAdvanced = false),
            ClassAbility(name = "Fuoco di Copertura", description = "Se un tuo alleato si trova entro 1,5 m dal tuo bersaglio, quando ottieni un 1 o un 2 a un tiro per i danni di un attacco, puoi trattare il risultato come se fosse un 3.", isAdvanced = false)
        ),
        advancedAbilities = listOf(
            ClassAbility(name = "Colpo Letale", description = "Come parte dell'azione d'attacco, esegui una prova di Potere contro la Forza di un nemico che hai preso di mira.\n\n" +
                    "Se il nemico fallisce il tiro di abilità e i suoi HP rimanenti sono inferiori alla metà, quella creatura viene annichilita e muore istantaneamente.\n\n" +
                    "In caso contrario, il tuo attacco infligge +2d6 danni extra.", isAdvanced = true),
            ClassAbility(name = "Raffica di Colpi", description = "Come azione bonus, puoi raddoppiare il numero dei tuoi attacchi fino all'inizio del tuo prossimo turno.", isAdvanced = true),
            ClassAbility(name = "Impatto Transitorio", description = "Quando riduci una creatura a 0 HP con un attacco, e rimane danno in eccesso, come reazione, trasferisci quel danno eccedente ad una creatura entro 3 m dalla prima, a patto che anche quest'ultima non superi il tiro su Agilità per evitare il tuo attacco.\n\n" +
                    "Aggiungi il tuo modificatore di Potenza a questi danni.", isAdvanced = true)
        )
    ),
)