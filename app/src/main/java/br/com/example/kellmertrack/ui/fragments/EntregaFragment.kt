package br.com.example.kellmertrack.ui.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.com.example.kellmertrack.R
import br.com.example.kellmertrack.core.Sistema
import br.com.example.kellmertrack.databinding.DetalhesEntregaFragmentBinding
import br.com.example.kellmertrack.databinding.EntregaFragmentBinding
import br.com.example.kellmertrack.local.model.DTO.EntregaDTO
import br.com.example.kellmertrack.local.model.entities.EntregaEntity
import br.com.example.kellmertrack.ui.adapters.EntregaAdapter
import br.com.example.kellmertrack.local.model.entities.relation.EntregaWithObra
import br.com.example.kellmertrack.ui.viewmodel.AppViewModel
import br.com.example.kellmertrack.ui.viewmodel.ComponentesFragments
import br.com.example.kellmertrack.ui.viewmodel.EntregaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class EntregaFragment : Fragment() {

    private lateinit var binding : EntregaFragmentBinding
    private lateinit var bindingDialog : DetalhesEntregaFragmentBinding
    private lateinit var context : Context
    private val viewModel: EntregaViewModel by viewModels()
    private val appViewModel: AppViewModel by activityViewModels()

    private lateinit var entregasAdapter: EntregaAdapter
    private var listEntregas: List<EntregaWithObra?> = listOf()
    private val routerController by lazy { findNavController()}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.entrega_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = EntregaFragmentBinding.bind(view)
        configuraList()
    }

    override fun onResume() {
        super.onResume()
        appViewModel.setBottomBar(ComponentesFragments(bottomBar = true))
    }

    private fun configuraList() {
        val visualizaEntrega = { entrega: EntregaWithObra? ->
            showDialog(entrega)
            viewModel.setEntregaSelecionada(entrega)
        }

        Sistema.getSetup()?.veiculosId.let { veiculo ->
            entregasAdapter = EntregaAdapter(
                context = requireContext(),
                click = visualizaEntrega
            )

            binding.entregaRecyclerView.adapter = entregasAdapter

            lifecycleScope.launch {
                if (veiculo != null) {
                    viewModel.entregas.observe(viewLifecycleOwner) { entregas ->
                        println(entregas)
                        if (entregas.isEmpty()) {
                            binding.entregaRecyclerView.visibility = View.GONE
                            binding.llWaiting.visibility = View.VISIBLE
                        } else {
                            binding.entregaRecyclerView.visibility = View.VISIBLE
                            binding.llWaiting.visibility = View.GONE
                            entregas.forEach { entrega ->
                                if (!listEntregas.contains(entrega))
                                    listEntregas = entregas
                            }
                        }
                        entregasAdapter.atualizaEntregas(listEntregas)
                    }
                }
            }
        }
    }

    //========================================================== DIÁLOGO DETALHES ENTREGA =================================================================================

    private fun showDialog(entrega : EntregaWithObra?){
        bindingDialog = DetalhesEntregaFragmentBinding.inflate(layoutInflater)
        bindingDialog.entrega = entrega
        val dialog = Dialog(context)
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(bindingDialog.root)

        setDialogButtons(dialog, entrega?.entregaEntity)

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun setDialogButtons(dialog: Dialog, entrega: EntregaEntity?){
        bindingDialog.btnVoltar.setOnClickListener {
            dialog.dismiss()
        }

        bindingDialog.btnConfirmar.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(
                "entregaDTO",
                EntregaDTO(
                    entregaId = entrega?.id!!,
                    momento = Date(),
                    quantidade = entrega.quantidade
                )
            )
            routerController.navigate(R.id.action_EntregaFragment_to_confirmacaoFragment, bundle)
            dialog.dismiss()
        }

        bindingDialog.btnVerComprovante.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("comprovante", "VER")
            bundle.putParcelable(
                "entregaDTO",
                EntregaDTO(
                    entregaId = entrega?.id!!,
                    momento = Date(),
                )
            )
            routerController.navigate(R.id.action_EntregaFragment_to_ComprovanteFragment, bundle)
            dialog.dismiss()
        }

        bindingDialog.btnTrajeto.setOnClickListener {
            dialog.dismiss()
            abrirGoogleMaps()
        }
    }

    private fun abrirGoogleMaps() {
        val latitude = viewModel.entregaSelecionada.value?.contratoEntity?.obraEntity?.latitude
        val longitude = viewModel.entregaSelecionada.value?.contratoEntity?.obraEntity?.longitude

        val uri = Uri.parse("google.navigation:q=$latitude,$longitude")

        val mapIntent = Intent(Intent.ACTION_VIEW, uri)

        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(context, "Google Maps não instalado", Toast.LENGTH_SHORT).show()
        }
    }
}
