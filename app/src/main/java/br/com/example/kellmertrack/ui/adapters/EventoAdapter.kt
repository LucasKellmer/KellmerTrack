package br.com.example.kellmertrack.ui.adapters

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.databinding.ItemEventoBinding
import br.com.example.kellmertrack.local.model.entities.EventoEntity
import br.com.example.kellmertrack.local.model.TipoEvento
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class EventoAdapter @Inject constructor() : RecyclerView.Adapter<EventoAdapter.ViewHolder>(){
        private var eventos : List<EventoEntity> = emptyList()
        private val formatterHorario =  SimpleDateFormat("HH:mm", Locale("pt","BR"))
        private val formatterData =  SimpleDateFormat("dd/MM", Locale("pt","BR"))
        private var click:(item: EventoEntity) -> Unit = {}

        fun setEventos(eventos: List<EventoEntity>) {
            this.eventos = eventos
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemEventoBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun getItemCount() = this.eventos.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(eventos[position], this.click, position)
            /*if (position == 0){
                holder.itemView
            }*/
        }

        inner class ViewHolder(private val binding: ItemEventoBinding) : RecyclerView.ViewHolder(binding.root) {

            fun bind(evento: EventoEntity, click: (item: EventoEntity) -> Unit, position: Int){
                binding.tvTituloEvento.text = "Contrato : ${evento.contrato}"
                binding.tvData.text = formatterData.format(evento.momento!!)
                binding.tvHorario.text = formatterHorario.format(evento.momento!!)
                binding.tvMsgEvento.text = evento.texto

                if(evento.tipo == TipoEvento.SAIDA.toString()){
                    if (evento.entregaId == "0"){
                        binding.tvTituloEvento.text = "Usina"
                        binding.timeLineItemImg.setImageResource(R.drawable.ic_action_entrega)
                    }else{
                        binding.timeLineItemImg.setImageResource(R.drawable.ic_action_location)
                    }
                } else if (evento.tipo == TipoEvento.PERMANECEU.toString()){
                    if (evento.entregaId == "0")
                        binding.tvTituloEvento.text = "Usina"
                    binding.timeLineItemImg.setImageResource(R.drawable.ic_action_time)
                } else if (evento.tipo == TipoEvento.ENTRADA.toString()){
                    if (evento.entregaId == "0"){
                        binding.tvTituloEvento.text = "Usina"
                        binding.timeLineItemImg.setImageResource(R.drawable.ic_check_circle)
                    }else{
                        binding.timeLineItemImg.setImageResource(R.drawable.ic_action_location)
                    }
                } else if (evento.tipo == TipoEvento.NOVA_ENTREGA.toString()){
                    binding.timeLineItemImg.setImageResource(R.drawable.ic_action_nova_entrega)
                }

                if (position == 0){
                    binding.tvTituloEvento.setTextColor(ContextCompat.getColor(itemView.context, R.color.ciano))
                    binding.tvTituloEvento.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                    binding.tvMsgEvento.setTextColor(ContextCompat.getColor(itemView.context, R.color.ciano))
                    binding.tvMsgEvento.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                    binding.tvData.setTextColor(ContextCompat.getColor(itemView.context, R.color.ciano))
                    binding.tvData.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                    binding.tvHorario.setTextColor(ContextCompat.getColor(itemView.context, R.color.ciano))
                    binding.tvHorario.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14f)
                }
            }
        }
}