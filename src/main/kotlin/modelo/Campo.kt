package modelo

enum class CampoEvento { ABERTURA, MARCACAO, DESMARCACAO, EXPLOSAO, REINICIALIZACAO }

/**
 * This class represents a field in a game, such as a minefield in Minesweeper.
 *
 * @property linha The row of the field.
 * @property coluna The column of the field.
 * @property marcado Indicates whether the field is marked.
 * @property aberto Indicates whether the field is open.
 * @property minado Indicates whether the field is mined.
 * @property desmarcado Read-only property. Indicates whether the field is unmarked.
 * @property fechado Read-only property. Indicates whether the field is closed.
 * @property seguro Read-only property. Indicates whether the field is safe (not mined).
 * @property objetivoAlcancado Read-only property. Indicates whether the objective has been reached.
 * @property qtdeVizinhosMinados Read-only property. Indicates the number of mined neighbors.
 * @property vizinhancaSegura Read-only property. Indicates whether the neighborhood is safe.
 */
data class Campo(
    val linha: Int,
    val coluna: Int,
) {

    private val vizinhos = ArrayList<Campo>()
    private val callbacks = ArrayList<(Campo, CampoEvento) -> Unit>()

    var marcado: Boolean = false
    var aberto: Boolean = false
    var minado: Boolean = false

    // Somente leitura
    val desmarcado: Boolean get() = !marcado
    val fechado: Boolean get() = !aberto
    val seguro: Boolean get() = !minado
    val objetivoAlcancado: Boolean get() = seguro && aberto || minado && marcado
    val qtdeVizinhosMinados: Int get() = vizinhos.filter { it.minado }.size
    val vizinhancaSegura: Boolean
        get() = vizinhos.map { it.seguro }.reduce { resultado, seguro -> resultado && seguro }

    /**
     * Adds a neighbor to the field.
     *
     * @param vizinho The neighbor to be added.
     */
    fun addVizinho(vizinho: Campo) {
        vizinhos.add(vizinho)
    }

    /**
     * Registers a callback to be called when an event occurs.
     *
     * @param callback The callback to be registered.
     */
    fun onEvento(callback: (Campo, CampoEvento) -> Unit) {
        callbacks.add(callback)
    }

    /**
     * Opens the field. If the field is mined, triggers an explosion event. Otherwise, triggers an opening event.
     */
    fun abrir() {
        if (fechado) {
            aberto = true
            if (minado) {
                callbacks.forEach { it(this, CampoEvento.EXPLOSAO) }
            } else {
                callbacks.forEach { it(this, CampoEvento.ABERTURA) }
                vizinhos.filter { it.fechado && it.seguro && vizinhancaSegura }.forEach { it.abrir() }
            }
        }
    }

    /**
     * Toggles the marking of the field. Triggers a marking or unmarking event.
     */
    fun alterarMarcacao() {
        if (fechado) {
            marcado = !marcado
            val evento = if (marcado) CampoEvento.MARCACAO else CampoEvento.DESMARCACAO
            callbacks.forEach { it(this, evento) }
        }
    }

    /**
     * Mines the field.
     */
    fun minar() {
        minado = true
    }

    /**
     * Resets the field to its initial state and triggers a reset event.
     */
    fun reiniciar() {
        aberto = false
        minado = false
        marcado = false
        callbacks.forEach { it(this, CampoEvento.REINICIALIZACAO) }
    }
}
