import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad para noticias guardadas (persistentes)
 */
@Entity(tableName = "noticias_guardadas")
data class NoticiaGuardadaEntity(
    @PrimaryKey
    val id: String,
    val titulo: String,
    val resumenEjecutivo: String,
    val categoria: String,
    val diario: String,
    val fechaPublicacion: Long,
    val fechaGuardado: Long,
    val urlOriginal: String,
    val tags: String, // JSON string
    val recordatorioFecha: Long?,
    val recordatorioMensaje: String?
)