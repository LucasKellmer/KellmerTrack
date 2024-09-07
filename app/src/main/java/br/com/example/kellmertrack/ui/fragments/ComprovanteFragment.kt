package br.com.example.kellmertrack.ui.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.databinding.ComprovanteFragmentBinding
import br.com.example.kellmertrack.databinding.MensagemDialogBinding
import br.com.example.kellmertrack.extensions.toBitmap
import br.com.example.kellmertrack.ui.viewmodel.ComprovanteViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ComprovanteFragment : Fragment() {

    private lateinit var binding: ComprovanteFragmentBinding
    private val viewModel by viewModels<ComprovanteViewModel>()
    private val routerController by lazy { findNavController() }
    val args : ComprovanteFragmentArgs by navArgs()
    private lateinit var bindingMensagemDialogBinding: MensagemDialogBinding
    private lateinit var context : Context
    private val mensagemDialog by lazy {
        MaterialAlertDialogBuilder(context)
            .setCancelable(false)
            .setView(bindingMensagemDialogBinding.root)
            .setBackground(ContextCompat.getDrawable(context, R.drawable.custom_dialog))
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.comprovante_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ComprovanteFragmentBinding.bind(view)
        binding.viewModel = viewModel
        bindingMensagemDialogBinding = MensagemDialogBinding.inflate(layoutInflater)

        viewModel.buscaEntrega(args.entregaDTO.entregaId.toString())
        Log.d("comprovante", "args.comprovante: ${args.comprovante}")
        if (args.comprovante == "NOVO"){
            viewModel.gerarComprovante(args.entregaDTO)
        }else{
            viewModel.buscarComprovante(args.entregaDTO.entregaId)
        }
        if (viewModel.comprovante.value?.assinatura != null){
            binding.imgAssinatura.setImageBitmap(viewModel.comprovante.value?.assinatura!!.toBitmap())
        }
        viewModel.setContext(context)
        configuraBotoes()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    private fun StringToBitmap(bitmapString: String): Bitmap {
        val decodedString = Base64.decode(bitmapString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

    }

    fun configuraBotoes(){
        binding.btnConfirmar.setOnClickListener {
            viewModel.setBitmap(viewModel.geraImgComprovanteBitmap(binding.clComprovante))
            lifecycleScope.launch {
                viewModel.confirmarEntrega(args.entregaDTO.quantidade.toString())
                viewModel.salvarComprovante()
            }

            bindingMensagemDialogBinding.tvMensagem.text = "Entrega confirmada com sucesso!"
            mensagemDialog.show()
            bindingMensagemDialogBinding.btnOk.setOnClickListener {
                routerController.navigate(R.id.action_ComprovanteFragment_to_EntregaFragment)
                mensagemDialog.dismiss()
            }
        }

        binding.btnVoltar.setOnClickListener {
            routerController.popBackStack()
        }
    }
}