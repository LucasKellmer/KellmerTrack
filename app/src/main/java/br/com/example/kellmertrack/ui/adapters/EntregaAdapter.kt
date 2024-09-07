package br.com.example.kellmertrack.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import br.com.example.kellmertrack.databinding.ItemEntregaBinding
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra

class EntregaAdapter(
    private val context: Context,
    private var entregas : MutableList<EntregaWithObra?> = mutableListOf(),
    private var click:(item: EntregaWithObra) -> Unit
): RecyclerView.Adapter<EntregaAdapter.ViewHolder>() {

    /*fun atualizaEntregas(entregas: List<EntregaEntity>?) {
        if (entregas != null) {
            this.entregas = entregas
        }
        notifyDataSetChanged()
    }*/

    fun atualizaEntregas(entregaList: List<EntregaWithObra?>){
        notifyItemRangeRemoved(0, this.entregas.size)
        if (entregaList != null) {
            this.entregas.clear()
            this.entregas.addAll(entregaList)
        }
        notifyItemRangeInserted(0, this.entregas.size)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemEntregaBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entrega = entregas[position]
        entrega?.let{holder.bind(it)}
    }

    override fun getItemCount() = this.entregas.size

    inner class ViewHolder(private val binding: ItemEntregaBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var entrega : EntregaWithObra

        init {
            binding.root.setOnClickListener {
                if (::entrega.isInitialized)
                    click(entrega)
            }
        }

        fun bind(entrega: EntregaWithObra) {
            this.entrega = entrega
            binding.entrega = entrega
            /*binding.tvEntrega.text = entrega.entregaEntity?.id.toString()
            binding.tvObra.text = entrega.obraEntity?.descricao
            binding.tvQuantidade.text = "${entrega.entregaEntity?.quantidade.toString()} M³"
            if (entrega.entregaEntity?.status != 2){
                binding.iconComplete.isVisible = false
                binding.iconImcomplete.isVisible = true
            }else{
                binding.iconComplete.isVisible = true
                binding.iconImcomplete.isVisible = false
            }*/
        }
    }
}