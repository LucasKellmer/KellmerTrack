package br.com.example.kellmertrack.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.databinding.ConfirmacaoFragmentBinding
import br.com.example.kellmertrack.services.location.LocationService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConfirmacaoFragment : Fragment() {

    private lateinit var binding : ConfirmacaoFragmentBinding
    private lateinit var context : Context
    private val routerController by lazy { findNavController() }
    private val args by navArgs<ConfirmacaoFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.confirmacao_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ConfirmacaoFragmentBinding.bind(view)
        binding.quantidade = args.entregaDTO.quantidade.toString()
        binding.edtQuantidade.requestFocus()
        configuraBotoes()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    private fun configuraBotoes(){
        binding.btnAddQuant.setOnClickListener {
            aumentaNumero()
        }
        binding.btnMinQuant.setOnClickListener {
            diminuiNumero()
        }

        binding.btnVoltar.setOnClickListener {
            routerController.popBackStack()
        }

        binding.btnConfirmar.setOnClickListener {
            if (validateFields()){
                val lastLocation = LocationService.getLastLocation()
                args.entregaDTO.recebedor = binding.edtRecebedor.text.toString().trim()
                args.entregaDTO.quantidade = binding.edtQuantidade.text.toString().toDouble()
                args.entregaDTO.latitude = lastLocation?.latitude
                args.entregaDTO.longitude = lastLocation?.longitude
                val bundle = Bundle().apply {
                    putParcelable("entregaDTO", args.entregaDTO)
                }
                routerController.navigate(R.id.action_confirmacaoFragment_to_AssinaturaFragment, bundle)
            }
        }
    }

    private fun diminuiNumero() {
        val numeroAtual = binding.edtQuantidade.text.toString().toDoubleOrNull() ?: 0.0
        val novoNumero = numeroAtual - 1
        if (novoNumero >= 0) {
            binding.edtQuantidade.text = novoNumero.toString()
        }
    }

    private fun aumentaNumero() {
        val numeroAtual = binding.edtQuantidade.text.toString().toDoubleOrNull() ?: 0.0
        val novoNumero = numeroAtual + 1
        binding.edtQuantidade.text = novoNumero.toString()
    }

    private fun validateFields() : Boolean{
        return if (binding.edtQuantidade.text.toString() == "0.0"){
            Toast.makeText(context, "Quantidade não pode ser 0", Toast.LENGTH_SHORT).show()
            false
        }else if  (binding.edtRecebedor.text.toString() == ""){
            binding.edtRecebedor.error = "Campo obrigatório"
            binding.tiRecebedor.helperText = "Informe o nome do recebedor!"
            false
        } else true
    }

}