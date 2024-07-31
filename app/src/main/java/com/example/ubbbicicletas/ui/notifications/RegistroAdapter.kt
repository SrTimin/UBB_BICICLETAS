
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ubbbicicletas.R
import models.Registro

class RegistroAdapter(private val registros: List<Registro>) : RecyclerView.Adapter<RegistroAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.nombre)
        val fecha: TextView = view.findViewById(R.id.fecha)
        val estacionamiento: TextView = view.findViewById(R.id.estacionamiento)
        val bicimarca: TextView = view.findViewById(R.id.bicimarca)
        val bicimodelo: TextView = view.findViewById(R.id.bicimodelo)
        val bicicolor: TextView = view.findViewById(R.id.bicicolor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_registro, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val registro = registros[position]
        holder.nombre.text = registro.user.nombre
        holder.fecha.text = "Fecha de Registro: ${registro.fecha_ingreso}"
        holder.estacionamiento.text = "Estacionamiento: ${registro.estacionamiento?.lastOrNull()}"
        holder.bicimarca.text = "Marca Bicicleta: ${registro.bicicleta?.marca}"
        holder.bicimodelo.text = "Modelo Bicicleta: ${registro.bicicleta?.modelo}"
        holder.bicicolor.text = "Color Bicicleta: ${registro.bicicleta?.color}"
    }

    override fun getItemCount() = registros.size
}